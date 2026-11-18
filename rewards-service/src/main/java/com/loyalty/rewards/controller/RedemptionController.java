package com.loyalty.rewards.controller;

import com.loyalty.common.dto.ApiResponse;
import com.loyalty.common.security.JwtTokenProvider;
import com.loyalty.rewards.dto.RedemptionResponse;
import com.loyalty.rewards.dto.RedemptionVerifyResponse;
import com.loyalty.rewards.service.RedemptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards/redemptions")
@RequiredArgsConstructor
@Tag(name = "Reward Redemptions", description = "Endpoints for viewing, verifying, and claiming reward redemption vouchers")
public class RedemptionController {

    private final RedemptionService redemptionService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/my-redemptions")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get list of reward vouchers redeemed by the current customer")
    public ResponseEntity<ApiResponse<Page<RedemptionResponse>>> getMyRedemptions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long customerId = extractCustomerIdFromToken(authHeader);
        if (customerId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unable to identify customer"));
        }
        Page<RedemptionResponse> redemptions = redemptionService.getCustomerRedemptions(customerId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(redemptions));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get all customer redemptions (Admin/Staff)")
    public ResponseEntity<ApiResponse<Page<RedemptionResponse>>> getAllRedemptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RedemptionResponse> redemptions = redemptionService.getAllRedemptions(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(redemptions));
    }

    @GetMapping("/verify/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Verify a redemption voucher code (Staff/Admin)")
    public ResponseEntity<ApiResponse<RedemptionVerifyResponse>> verifyVoucher(@PathVariable String code) {
        RedemptionVerifyResponse verification = redemptionService.verifyVoucher(code);
        return ResponseEntity.ok(ApiResponse.success(verification));
    }

    @PostMapping("/use/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Mark a redemption voucher code as used/claimed (Staff/Admin)")
    public ResponseEntity<ApiResponse<RedemptionResponse>> useVoucher(@PathVariable String code) {
        RedemptionResponse response = redemptionService.markVoucherUsed(code);
        return ResponseEntity.ok(ApiResponse.success("Voucher successfully claimed", response));
    }

    private Long extractCustomerIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtTokenProvider.getCustomerIdFromToken(authHeader.substring(7));
        }
        return null;
    }
}

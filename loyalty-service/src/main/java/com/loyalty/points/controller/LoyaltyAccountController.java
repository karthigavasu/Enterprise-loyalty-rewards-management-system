package com.loyalty.points.controller;

import com.loyalty.common.dto.ApiResponse;
import com.loyalty.common.security.JwtTokenProvider;
import com.loyalty.points.dto.*;
import com.loyalty.points.service.LoyaltyAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty/accounts")
@RequiredArgsConstructor
@Tag(name = "Loyalty Points", description = "Endpoints for loyalty balances, points calculation, ledger history, and adjustments")
public class LoyaltyAccountController {

    private final LoyaltyAccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get loyalty account balance and tier by customer ID")
    public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> getByCustomerId(@PathVariable Long customerId) {
        LoyaltyAccountResponse response = accountService.getAccountByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-number/{customerNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    @Operation(summary = "Get loyalty account by customer number")
    public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> getByCustomerNumber(@PathVariable String customerNumber) {
        LoyaltyAccountResponse response = accountService.getAccountByCustomerNumber(customerNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-account")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current logged-in customer's loyalty account")
    public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> getMyAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long customerId = extractCustomerIdFromToken(authHeader);
        if (customerId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unable to identify customer account"));
        }
        LoyaltyAccountResponse response = accountService.getAccountByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{accountId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Get points transaction ledger history for account (Admin/Staff)")
    public ResponseEntity<ApiResponse<Page<LoyaltyTransactionResponse>>> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<LoyaltyTransactionResponse> transactions = accountService.getAccountTransactions(accountId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/my-transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current logged-in customer's points ledger history")
    public ResponseEntity<ApiResponse<Page<LoyaltyTransactionResponse>>> getMyTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long customerId = extractCustomerIdFromToken(authHeader);
        if (customerId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Unable to identify customer account"));
        }
        Page<LoyaltyTransactionResponse> transactions = accountService.getCustomerTransactions(customerId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually credit or deduct points (Admin only)")
    public ResponseEntity<ApiResponse<LoyaltyAccountResponse>> adjustPoints(@Valid @RequestBody PointsAdjustmentRequest request) {
        LoyaltyAccountResponse response = accountService.adjustPoints(request);
        return ResponseEntity.ok(ApiResponse.success("Points successfully adjusted", response));
    }

    @GetMapping("/tiers")
    @Operation(summary = "Get list of all tier levels, multipliers, and perks")
    public ResponseEntity<ApiResponse<List<TierConfigDto>>> getTiers() {
        List<TierConfigDto> tiers = accountService.getAllTierConfigs();
        return ResponseEntity.ok(ApiResponse.success(tiers));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get loyalty points aggregate analytics (Admin only)")
    public ResponseEntity<ApiResponse<LoyaltyAnalyticsSummary>> getAnalytics() {
        LoyaltyAnalyticsSummary summary = accountService.getAnalyticsSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    private Long extractCustomerIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenProvider.getCustomerIdFromToken(token);
        }
        return null;
    }
}

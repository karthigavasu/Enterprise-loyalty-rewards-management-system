package com.loyalty.rewards.controller;

import com.loyalty.common.dto.ApiResponse;
import com.loyalty.common.security.JwtTokenProvider;
import com.loyalty.rewards.dto.RedemptionRequest;
import com.loyalty.rewards.dto.RedemptionResponse;
import com.loyalty.rewards.dto.RewardRequest;
import com.loyalty.rewards.dto.RewardResponse;
import com.loyalty.rewards.service.RedemptionService;
import com.loyalty.rewards.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards Catalog", description = "Endpoints for creating, managing, browsing, and redeeming rewards")
public class RewardController {

    private final RewardService rewardService;
    private final RedemptionService redemptionService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Browse available active rewards with optional filters")
    public ResponseEntity<ApiResponse<Page<RewardResponse>>> getActiveRewards(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long maxPoints,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "pointsRequired") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<RewardResponse> rewards = rewardService.searchActiveRewards(category, maxPoints, query, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(rewards));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all rewards including inactive ones (Admin only)")
    public ResponseEntity<ApiResponse<Page<RewardResponse>>> getAllRewards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<RewardResponse> rewards = rewardService.getAllRewards(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(rewards));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reward details by ID")
    public ResponseEntity<ApiResponse<RewardResponse>> getRewardById(@PathVariable Long id) {
        RewardResponse reward = rewardService.getRewardById(id);
        return ResponseEntity.ok(ApiResponse.success(reward));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new reward item (Admin only)")
    public ResponseEntity<ApiResponse<RewardResponse>> createReward(@Valid @RequestBody RewardRequest request) {
        RewardResponse created = rewardService.createReward(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reward created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update reward details (Admin only)")
    public ResponseEntity<ApiResponse<RewardResponse>> updateReward(
            @PathVariable Long id,
            @Valid @RequestBody RewardRequest request
    ) {
        RewardResponse updated = rewardService.updateReward(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reward updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle reward availability status (Admin only)")
    public ResponseEntity<ApiResponse<RewardResponse>> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        RewardResponse updated = rewardService.toggleStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success("Reward status updated", updated));
    }

    @PostMapping("/{id}/redeem")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @Operation(summary = "Redeem reward for points")
    public ResponseEntity<ApiResponse<RedemptionResponse>> redeemReward(
            @PathVariable Long id,
            @RequestBody(required = false) RedemptionRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long customerId = (request != null && request.getCustomerId() != null)
                ? request.getCustomerId()
                : extractCustomerIdFromToken(authHeader);
        String customerNumber = (request != null) ? request.getCustomerNumber() : null;

        if (customerId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Customer ID is required to redeem reward"));
        }

        RedemptionResponse response = redemptionService.initiateRedemption(id, customerId, customerNumber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reward redemption successful", response));
    }

    private Long extractCustomerIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtTokenProvider.getCustomerIdFromToken(authHeader.substring(7));
        }
        return null;
    }
}

package com.loyalty.points.dto;

import com.loyalty.common.enums.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyAccountResponse {
    private Long id;
    private Long customerId;
    private String customerNumber;
    private Long currentPointsBalance;
    private Long lifetimeEarnedPoints;
    private Long lifetimeRedeemedPoints;
    private TierLevel tierLevel;
    private String status;
    private Double earnMultiplier;
    private Long pointsToNextTier;
    private TierLevel nextTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

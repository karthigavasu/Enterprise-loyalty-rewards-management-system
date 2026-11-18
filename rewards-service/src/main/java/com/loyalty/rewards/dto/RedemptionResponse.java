package com.loyalty.rewards.dto;

import com.loyalty.common.enums.RedemptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionResponse {
    private Long id;
    private String redemptionCode;
    private Long customerId;
    private String customerNumber;
    private Long rewardId;
    private String rewardTitle;
    private Long pointsSpent;
    private RedemptionStatus status;
    private String failureReason;
    private LocalDateTime redeemedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}

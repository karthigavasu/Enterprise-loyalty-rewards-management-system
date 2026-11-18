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
public class RedemptionVerifyResponse {
    private boolean valid;
    private String message;
    private String redemptionCode;
    private String rewardTitle;
    private Long customerId;
    private RedemptionStatus status;
    private LocalDateTime redeemedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}

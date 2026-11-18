package com.loyalty.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardRedemptionCompletedEvent implements Serializable {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    @Builder.Default
    private String eventType = "RewardRedemptionCompletedEvent";
    private String correlationId;

    private Long redemptionId;
    private Long customerId;
    private Long rewardId;
    private Long pointsDeducted;
    private String status; // "SUCCESS" or "FAILED"
    private String failureReason;
    private String redemptionCode;
    private Long remainingBalance;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

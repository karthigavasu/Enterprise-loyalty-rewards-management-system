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
public class RewardRedemptionRequestedEvent implements Serializable {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    @Builder.Default
    private String eventType = "RewardRedemptionRequestedEvent";
    private String correlationId;

    private Long redemptionId;
    private Long customerId;
    private String customerNumber;
    private Long rewardId;
    private String rewardTitle;
    private Long pointsRequired;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

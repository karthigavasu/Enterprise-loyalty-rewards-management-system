package com.loyalty.common.events;

import com.loyalty.common.enums.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyPointsAwardedEvent implements Serializable {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    @Builder.Default
    private String eventType = "LoyaltyPointsAwardedEvent";
    private String correlationId;
    
    private Long transactionId;
    private String transactionReference;
    private Long customerId;
    private String customerNumber;
    private BigDecimal transactionAmount;
    private Long pointsAwarded;
    private Long newPointsBalance;
    private Long lifetimePoints;
    private TierLevel tierLevel;
    private boolean tierUpgraded;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

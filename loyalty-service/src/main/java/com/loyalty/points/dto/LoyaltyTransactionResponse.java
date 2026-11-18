package com.loyalty.points.dto;

import com.loyalty.common.enums.PointsTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyTransactionResponse {
    private Long id;
    private Long accountId;
    private Long customerId;
    private PointsTransactionType type;
    private Long points;
    private Long balanceAfter;
    private String referenceType;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
}

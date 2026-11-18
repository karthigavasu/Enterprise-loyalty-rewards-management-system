package com.loyalty.points.dto;

import com.loyalty.common.enums.PointsTransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsAdjustmentRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Transaction type is required (ADJUSTMENT, BONUS, etc.)")
    private PointsTransactionType type;

    @NotNull(message = "Points amount is required")
    @Positive(message = "Points must be positive")
    private Long points;

    private boolean deduct; // true to deduct, false to credit

    private String reason;
}

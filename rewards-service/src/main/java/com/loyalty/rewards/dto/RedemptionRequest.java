package com.loyalty.rewards.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedemptionRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String customerNumber;
}

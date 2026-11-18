package com.loyalty.rewards.dto;

import com.loyalty.common.enums.TierLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Points required cannot be null")
    @Positive(message = "Points required must be positive")
    private Long pointsRequired;

    private BigDecimal originalValue;
    private String currency;
    private Integer stockQuantity;
    private Boolean isUnlimitedStock;
    private TierLevel minimumTierRequired;
    private Boolean active;
    private String imageUrl;
    private String termsAndConditions;
    private Integer expiryDays;
}

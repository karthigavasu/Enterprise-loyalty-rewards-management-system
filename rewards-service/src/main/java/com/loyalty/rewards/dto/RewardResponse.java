package com.loyalty.rewards.dto;

import com.loyalty.common.enums.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

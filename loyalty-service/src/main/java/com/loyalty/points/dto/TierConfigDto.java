package com.loyalty.points.dto;

import com.loyalty.common.enums.TierLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierConfigDto {
    private TierLevel tier;
    private Long minPointsRequired;
    private Double earnMultiplier;
    private String perksDescription;
}

package com.loyalty.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyAnalyticsSummary {
    private Long totalActiveAccounts;
    private Long totalPointsInCirculation;
    private Long totalLifetimePointsIssued;
    private Long totalLifetimePointsRedeemed;
}

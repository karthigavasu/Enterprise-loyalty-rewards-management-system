package com.loyalty.points.service;

import com.loyalty.common.enums.TierLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LoyaltyCalculationServiceTest {

    private LoyaltyCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new LoyaltyCalculationService();
    }

    @Test
    void testCalculatePointsBronze() {
        // $100 * 10 * 1.0 = 1000 pts
        long points = calculationService.calculatePoints(new BigDecimal("100.00"), TierLevel.BRONZE);
        assertThat(points).isEqualTo(1000L);
    }

    @Test
    void testCalculatePointsGold() {
        // $100 * 10 * 1.5 = 1500 pts
        long points = calculationService.calculatePoints(new BigDecimal("100.00"), TierLevel.GOLD);
        assertThat(points).isEqualTo(1500L);
    }

    @Test
    void testCalculatePointsPlatinum() {
        // $100 * 10 * 2.0 = 2000 pts
        long points = calculationService.calculatePoints(new BigDecimal("100.00"), TierLevel.PLATINUM);
        assertThat(points).isEqualTo(2000L);
    }

    @Test
    void testZeroOrNegativeAmount() {
        assertThat(calculationService.calculatePoints(BigDecimal.ZERO, TierLevel.GOLD)).isEqualTo(0L);
        assertThat(calculationService.calculatePoints(new BigDecimal("-50.00"), TierLevel.GOLD)).isEqualTo(0L);
    }

    @Test
    void testEvaluateTier() {
        assertThat(calculationService.evaluateTier(1000L)).isEqualTo(TierLevel.BRONZE);
        assertThat(calculationService.evaluateTier(5000L)).isEqualTo(TierLevel.SILVER);
        assertThat(calculationService.evaluateTier(15000L)).isEqualTo(TierLevel.GOLD);
        assertThat(calculationService.evaluateTier(30000L)).isEqualTo(TierLevel.PLATINUM);
    }
}

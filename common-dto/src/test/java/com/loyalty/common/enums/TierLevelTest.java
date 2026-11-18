package com.loyalty.common.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TierLevelTest {

    @Test
    void testCalculateTier() {
        assertThat(TierLevel.calculateTier(0)).isEqualTo(TierLevel.BRONZE);
        assertThat(TierLevel.calculateTier(4999)).isEqualTo(TierLevel.BRONZE);
        assertThat(TierLevel.calculateTier(5000)).isEqualTo(TierLevel.SILVER);
        assertThat(TierLevel.calculateTier(14999)).isEqualTo(TierLevel.SILVER);
        assertThat(TierLevel.calculateTier(15000)).isEqualTo(TierLevel.GOLD);
        assertThat(TierLevel.calculateTier(29999)).isEqualTo(TierLevel.GOLD);
        assertThat(TierLevel.calculateTier(30000)).isEqualTo(TierLevel.PLATINUM);
        assertThat(TierLevel.calculateTier(100000)).isEqualTo(TierLevel.PLATINUM);
    }

    @Test
    void testMultipliers() {
        assertThat(TierLevel.BRONZE.getMultiplier()).isEqualTo(1.0);
        assertThat(TierLevel.SILVER.getMultiplier()).isEqualTo(1.25);
        assertThat(TierLevel.GOLD.getMultiplier()).isEqualTo(1.5);
        assertThat(TierLevel.PLATINUM.getMultiplier()).isEqualTo(2.0);
    }
}

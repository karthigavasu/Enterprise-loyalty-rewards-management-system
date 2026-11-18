package com.loyalty.common.enums;

public enum TierLevel {
    BRONZE(1.0, 0),
    SILVER(1.25, 5000),
    GOLD(1.5, 15000),
    PLATINUM(2.0, 30000);

    private final double multiplier;
    private final int minPointsRequired;

    TierLevel(double multiplier, int minPointsRequired) {
        this.multiplier = multiplier;
        this.minPointsRequired = minPointsRequired;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getMinPointsRequired() {
        return minPointsRequired;
    }

    public static TierLevel calculateTier(long lifetimePoints) {
        if (lifetimePoints >= PLATINUM.minPointsRequired) {
            return PLATINUM;
        } else if (lifetimePoints >= GOLD.minPointsRequired) {
            return GOLD;
        } else if (lifetimePoints >= SILVER.minPointsRequired) {
            return SILVER;
        } else {
            return BRONZE;
        }
    }
}

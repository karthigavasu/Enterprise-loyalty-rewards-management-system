package com.loyalty.points.service;

import com.loyalty.common.enums.TierLevel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoyaltyCalculationService {

    public static final int BASE_POINTS_PER_CURRENCY_UNIT = 10;

    public long calculatePoints(BigDecimal transactionAmount, TierLevel tier) {
        if (transactionAmount == null || transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }

        double multiplier = (tier != null) ? tier.getMultiplier() : TierLevel.BRONZE.getMultiplier();
        BigDecimal basePoints = transactionAmount.multiply(BigDecimal.valueOf(BASE_POINTS_PER_CURRENCY_UNIT));
        BigDecimal finalPoints = basePoints.multiply(BigDecimal.valueOf(multiplier));

        return finalPoints.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public TierLevel evaluateTier(long lifetimePoints) {
        return TierLevel.calculateTier(lifetimePoints);
    }
}

package com.loyalty.rewards.config;

import com.loyalty.common.enums.TierLevel;
import com.loyalty.rewards.entity.Reward;
import com.loyalty.rewards.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RewardRepository rewardRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (rewardRepository.count() == 0) {
            log.info("Seeding initial rewards catalog...");

            createReward("$10 Coffee Shop Gift Card", "Enjoy your favorite beverage at participating coffee chains nationwide.",
                    "GIFT_CARD", 1000L, new BigDecimal("10.00"), 500, TierLevel.BRONZE,
                    "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=500&auto=format&fit=crop&q=60", 60);

            createReward("$25 Amazon eGift Card", "Redeemable towards millions of items across online store.",
                    "GIFT_CARD", 2500L, new BigDecimal("25.00"), 300, TierLevel.BRONZE,
                    "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=500&auto=format&fit=crop&q=60", 90);

            createReward("$50 Retail Shopping Voucher", "Valid at top partner department stores and fashion boutiques.",
                    "VOUCHER", 5000L, new BigDecimal("50.00"), 200, TierLevel.SILVER,
                    "https://images.unsplash.com/photo-1472851294608-062f824d29cc?w=500&auto=format&fit=crop&q=60", 90);

            createReward("VIP Cinema Experience (2 Tickets)", "Premium reclining seats with complimentary popcorn and drinks.",
                    "EXPERIENCE", 1500L, new BigDecimal("35.00"), 150, TierLevel.BRONZE,
                    "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=60", 120);

            createReward("$200 Airline Flight Voucher", "Use towards domestic and international flights on partner airlines.",
                    "TRAVEL", 20000L, new BigDecimal("200.00"), 50, TierLevel.SILVER,
                    "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=500&auto=format&fit=crop&q=60", 365);

            createReward("Apple AirPods Pro", "Noise-cancelling wireless earbuds with MagSafe charging case.",
                    "MERCHANDISE", 25000L, new BigDecimal("249.00"), 30, TierLevel.GOLD,
                    "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=500&auto=format&fit=crop&q=60", 180);

            createReward("Weekend Luxury Resort Getaway", "2-night stay for two at a 5-star partner spa and resort.",
                    "EXPERIENCE", 35000L, new BigDecimal("600.00"), 15, TierLevel.PLATINUM,
                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=500&auto=format&fit=crop&q=60", 365);

            createReward("25% Off Chef's Tasting Menu", "Fine dining experience discount at Michelin-rated partner restaurants.",
                    "DISCOUNT", 800L, new BigDecimal("40.00"), 250, TierLevel.BRONZE,
                    "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=500&auto=format&fit=crop&q=60", 45);

            log.info("Rewards catalog seeded successfully.");
        }
    }

    private void createReward(String title, String desc, String category, Long points, BigDecimal value, int stock, TierLevel tier, String imageUrl, int expiryDays) {
        rewardRepository.save(Reward.builder()
                .title(title)
                .description(desc)
                .category(category)
                .pointsRequired(points)
                .originalValue(value)
                .currency("USD")
                .stockQuantity(stock)
                .isUnlimitedStock(false)
                .minimumTierRequired(tier)
                .active(true)
                .imageUrl(imageUrl)
                .termsAndConditions("Standard terms apply. Non-refundable once claimed.")
                .expiryDays(expiryDays)
                .build());
    }
}

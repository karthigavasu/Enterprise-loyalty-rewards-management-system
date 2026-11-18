package com.loyalty.points.config;

import com.loyalty.common.enums.PointsTransactionType;
import com.loyalty.common.enums.TierLevel;
import com.loyalty.points.entity.LoyaltyAccount;
import com.loyalty.points.entity.LoyaltyTransaction;
import com.loyalty.points.entity.TierConfig;
import com.loyalty.points.repository.LoyaltyAccountRepository;
import com.loyalty.points.repository.LoyaltyTransactionRepository;
import com.loyalty.points.repository.TierConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TierConfigRepository tierConfigRepository;
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing loyalty tier configurations and initial accounts...");

        initTier(TierLevel.BRONZE, 0L, 1.0, "Base earn rate (10 pts/$), standard member rewards access");
        initTier(TierLevel.SILVER, 5000L, 1.25, "1.25x points multiplier, priority customer support, birthday bonus");
        initTier(TierLevel.GOLD, 15000L, 1.5, "1.5x points multiplier, dedicated account manager, free shipping, exclusive events");
        initTier(TierLevel.PLATINUM, 30000L, 2.0, "2.0x points multiplier, VIP lounge access, concierge service, waived fees");

        // Seed demo customer accounts matching customer-service seed users
        initDemoAccount(1L, "CUST-10001", 18500L, 22000L, 3500L, TierLevel.GOLD);
        initDemoAccount(2L, "CUST-10002", 45000L, 50000L, 5000L, TierLevel.PLATINUM);
        initDemoAccount(3L, "CUST-10003", 7200L, 8000L, 800L, TierLevel.SILVER);
        initDemoAccount(4L, "CUST-10004", 1500L, 1500L, 0L, TierLevel.BRONZE);

        log.info("Loyalty service data initialization complete.");
    }

    private void initTier(TierLevel tier, Long minPoints, Double multiplier, String perks) {
        if (tierConfigRepository.findByTier(tier).isEmpty()) {
            tierConfigRepository.save(TierConfig.builder()
                    .tier(tier)
                    .minPointsRequired(minPoints)
                    .earnMultiplier(multiplier)
                    .perksDescription(perks)
                    .build());
        }
    }

    private void initDemoAccount(Long customerId, String customerNumber, Long currentBalance, Long lifetimeEarned, Long lifetimeRedeemed, TierLevel tier) {
        if (accountRepository.findByCustomerId(customerId).isEmpty()) {
            LoyaltyAccount account = accountRepository.save(LoyaltyAccount.builder()
                    .customerId(customerId)
                    .customerNumber(customerNumber)
                    .currentPointsBalance(currentBalance)
                    .lifetimeEarnedPoints(lifetimeEarned)
                    .lifetimeRedeemedPoints(lifetimeRedeemed)
                    .tierLevel(tier)
                    .status("ACTIVE")
                    .build());

            transactionRepository.save(LoyaltyTransaction.builder()
                    .accountId(account.getId())
                    .customerId(customerId)
                    .type(PointsTransactionType.EARN)
                    .points(currentBalance)
                    .balanceAfter(currentBalance)
                    .referenceType("INITIAL_MIGRATION")
                    .referenceId("INIT-" + customerId)
                    .description("Initial loyalty account balance grant")
                    .build());
        }
    }
}

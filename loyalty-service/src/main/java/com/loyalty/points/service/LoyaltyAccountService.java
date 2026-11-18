package com.loyalty.points.service;

import com.loyalty.common.enums.PointsTransactionType;
import com.loyalty.common.enums.TierLevel;
import com.loyalty.points.dto.*;
import com.loyalty.points.entity.LoyaltyAccount;
import com.loyalty.points.entity.LoyaltyTransaction;
import com.loyalty.points.entity.TierConfig;
import com.loyalty.points.repository.LoyaltyAccountRepository;
import com.loyalty.points.repository.LoyaltyTransactionRepository;
import com.loyalty.points.repository.TierConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyAccountService {

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final TierConfigRepository tierConfigRepository;
    private final LoyaltyCalculationService calculationService;

    @Transactional
    public LoyaltyAccount getOrCreateAccount(Long customerId, String customerNumber) {
        return accountRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    LoyaltyAccount account = LoyaltyAccount.builder()
                            .customerId(customerId)
                            .customerNumber(customerNumber != null ? customerNumber : "CUST-" + customerId)
                            .currentPointsBalance(0L)
                            .lifetimeEarnedPoints(0L)
                            .lifetimeRedeemedPoints(0L)
                            .tierLevel(TierLevel.BRONZE)
                            .status("ACTIVE")
                            .build();
                    LoyaltyAccount saved = accountRepository.save(account);
                    log.info("Created new loyalty account for customer: {}", customerId);
                    return saved;
                });
    }

    @Transactional
    public LoyaltyPointsAwardResult awardPoints(Long customerId, String customerNumber, BigDecimal amount, String referenceId, String description) {
        LoyaltyAccount account = getOrCreateAccount(customerId, customerNumber);

        long pointsToAward = calculationService.calculatePoints(amount, account.getTierLevel());
        long newBalance = account.getCurrentPointsBalance() + pointsToAward;
        long newLifetime = account.getLifetimeEarnedPoints() + pointsToAward;

        TierLevel oldTier = account.getTierLevel();
        TierLevel newTier = calculationService.evaluateTier(newLifetime);
        boolean tierUpgraded = (newTier.ordinal() > oldTier.ordinal());

        account.setCurrentPointsBalance(newBalance);
        account.setLifetimeEarnedPoints(newLifetime);
        account.setTierLevel(newTier);
        accountRepository.save(account);

        LoyaltyTransaction txn = LoyaltyTransaction.builder()
                .accountId(account.getId())
                .customerId(customerId)
                .type(PointsTransactionType.EARN)
                .points(pointsToAward)
                .balanceAfter(newBalance)
                .referenceType("TRANSACTION")
                .referenceId(referenceId)
                .description(description != null ? description : "Points earned from purchase")
                .build();
        transactionRepository.save(txn);

        log.info("Awarded {} points to customer {}. New balance: {}. Upgraded tier: {}",
                pointsToAward, customerId, newBalance, tierUpgraded);

        return new LoyaltyPointsAwardResult(pointsToAward, newBalance, newLifetime, newTier, tierUpgraded);
    }

    @Transactional
    public LoyaltyPointsDeductResult deductPoints(Long customerId, Long points, String referenceId, String description) {
        LoyaltyAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Loyalty account not found for customer: " + customerId));

        if (account.getCurrentPointsBalance() < points) {
            log.warn("Customer {} has insufficient points: {} requested, {} available",
                    customerId, points, account.getCurrentPointsBalance());
            return new LoyaltyPointsDeductResult(false, account.getCurrentPointsBalance(), "Insufficient points balance");
        }

        long newBalance = account.getCurrentPointsBalance() - points;
        long newRedeemed = account.getLifetimeRedeemedPoints() + points;

        account.setCurrentPointsBalance(newBalance);
        account.setLifetimeRedeemedPoints(newRedeemed);
        accountRepository.save(account);

        LoyaltyTransaction txn = LoyaltyTransaction.builder()
                .accountId(account.getId())
                .customerId(customerId)
                .type(PointsTransactionType.REDEEM)
                .points(-points)
                .balanceAfter(newBalance)
                .referenceType("REDEMPTION")
                .referenceId(referenceId)
                .description(description != null ? description : "Points redeemed for reward")
                .build();
        transactionRepository.save(txn);

        log.info("Deducted {} points from customer {}. Remaining balance: {}", points, customerId, newBalance);
        return new LoyaltyPointsDeductResult(true, newBalance, null);
    }

    @Transactional
    public LoyaltyAccountResponse adjustPoints(PointsAdjustmentRequest request) {
        LoyaltyAccount account = getOrCreateAccount(request.getCustomerId(), null);

        long delta = request.isDeduct() ? -request.getPoints() : request.getPoints();
        long newBalance = account.getCurrentPointsBalance() + delta;

        if (newBalance < 0) {
            throw new IllegalArgumentException("Point adjustment cannot result in negative balance");
        }

        account.setCurrentPointsBalance(newBalance);
        if (!request.isDeduct()) {
            long newLifetime = account.getLifetimeEarnedPoints() + request.getPoints();
            account.setLifetimeEarnedPoints(newLifetime);
            account.setTierLevel(calculationService.evaluateTier(newLifetime));
        } else {
            account.setLifetimeRedeemedPoints(account.getLifetimeRedeemedPoints() + request.getPoints());
        }
        accountRepository.save(account);

        LoyaltyTransaction txn = LoyaltyTransaction.builder()
                .accountId(account.getId())
                .customerId(account.getCustomerId())
                .type(request.getType() != null ? request.getType() : PointsTransactionType.ADJUSTMENT)
                .points(delta)
                .balanceAfter(newBalance)
                .referenceType("MANUAL_ADJUSTMENT")
                .referenceId("ADJ-" + System.currentTimeMillis())
                .description(request.getReason() != null ? request.getReason() : "Manual point adjustment by admin")
                .build();
        transactionRepository.save(txn);

        log.info("Manually adjusted points for customer {}: delta = {}, new balance = {}",
                request.getCustomerId(), delta, newBalance);

        return mapToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getAccountByCustomerId(Long customerId) {
        LoyaltyAccount account = getOrCreateAccount(customerId, null);
        return mapToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getAccountByCustomerNumber(String customerNumber) {
        LoyaltyAccount account = accountRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new IllegalArgumentException("Loyalty account not found for number: " + customerNumber));
        return mapToAccountResponse(account);
    }

    @Transactional(readOnly = true)
    public Page<LoyaltyTransactionResponse> getAccountTransactions(Long accountId, Pageable pageable) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(this::mapToTransactionResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoyaltyTransactionResponse> getCustomerTransactions(Long customerId, Pageable pageable) {
        return transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::mapToTransactionResponse);
    }

    @Transactional(readOnly = true)
    public LoyaltyAnalyticsSummary getAnalyticsSummary() {
        return LoyaltyAnalyticsSummary.builder()
                .totalActiveAccounts(accountRepository.count())
                .totalPointsInCirculation(accountRepository.sumTotalCurrentPoints())
                .totalLifetimePointsIssued(accountRepository.sumTotalLifetimeEarnedPoints())
                .totalLifetimePointsRedeemed(accountRepository.sumTotalLifetimeRedeemedPoints())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TierConfigDto> getAllTierConfigs() {
        return tierConfigRepository.findAll().stream()
                .map(tc -> TierConfigDto.builder()
                        .tier(tc.getTier())
                        .minPointsRequired(tc.getMinPointsRequired())
                        .earnMultiplier(tc.getEarnMultiplier())
                        .perksDescription(tc.getPerksDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public LoyaltyAccountResponse mapToAccountResponse(LoyaltyAccount account) {
        TierLevel currentTier = account.getTierLevel();
        TierLevel nextTier = null;
        Long pointsToNextTier = 0L;

        if (currentTier == TierLevel.BRONZE) {
            nextTier = TierLevel.SILVER;
            pointsToNextTier = Math.max(0, TierLevel.SILVER.getMinPointsRequired() - account.getLifetimeEarnedPoints());
        } else if (currentTier == TierLevel.SILVER) {
            nextTier = TierLevel.GOLD;
            pointsToNextTier = Math.max(0, TierLevel.GOLD.getMinPointsRequired() - account.getLifetimeEarnedPoints());
        } else if (currentTier == TierLevel.GOLD) {
            nextTier = TierLevel.PLATINUM;
            pointsToNextTier = Math.max(0, TierLevel.PLATINUM.getMinPointsRequired() - account.getLifetimeEarnedPoints());
        }

        return LoyaltyAccountResponse.builder()
                .id(account.getId())
                .customerId(account.getCustomerId())
                .customerNumber(account.getCustomerNumber())
                .currentPointsBalance(account.getCurrentPointsBalance())
                .lifetimeEarnedPoints(account.getLifetimeEarnedPoints())
                .lifetimeRedeemedPoints(account.getLifetimeRedeemedPoints())
                .tierLevel(account.getTierLevel())
                .status(account.getStatus())
                .earnMultiplier(account.getTierLevel().getMultiplier())
                .pointsToNextTier(pointsToNextTier)
                .nextTier(nextTier)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public LoyaltyTransactionResponse mapToTransactionResponse(LoyaltyTransaction txn) {
        return LoyaltyTransactionResponse.builder()
                .id(txn.getId())
                .accountId(txn.getAccountId())
                .customerId(txn.getCustomerId())
                .type(txn.getType())
                .points(txn.getPoints())
                .balanceAfter(txn.getBalanceAfter())
                .referenceType(txn.getReferenceType())
                .referenceId(txn.getReferenceId())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .build();
    }

    public record LoyaltyPointsAwardResult(long pointsAwarded, long newBalance, long lifetimePoints, TierLevel tierLevel, boolean tierUpgraded) {}
    public record LoyaltyPointsDeductResult(boolean success, long remainingBalance, String errorMessage) {}
}

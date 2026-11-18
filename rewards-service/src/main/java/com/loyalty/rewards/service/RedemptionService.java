package com.loyalty.rewards.service;

import com.loyalty.common.enums.RedemptionStatus;
import com.loyalty.common.events.RewardRedemptionCompletedEvent;
import com.loyalty.common.events.RewardRedemptionRequestedEvent;
import com.loyalty.rewards.dto.RedemptionResponse;
import com.loyalty.rewards.dto.RedemptionVerifyResponse;
import com.loyalty.rewards.entity.Reward;
import com.loyalty.rewards.entity.RewardRedemption;
import com.loyalty.rewards.repository.RewardRedemptionRepository;
import com.loyalty.rewards.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedemptionService {

    private final RewardRepository rewardRepository;
    private final RewardRedemptionRepository redemptionRepository;
    private final RewardsKafkaProducerService producerService;

    @Transactional
    public RedemptionResponse initiateRedemption(Long rewardId, Long customerId, String customerNumber) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found with id: " + rewardId));

        if (!reward.getActive()) {
            throw new IllegalStateException("Reward is currently unavailable");
        }

        if (!reward.getIsUnlimitedStock() && reward.getStockQuantity() <= 0) {
            throw new IllegalStateException("Reward is out of stock");
        }

        // Deduct stock
        if (!reward.getIsUnlimitedStock()) {
            reward.setStockQuantity(reward.getStockQuantity() - 1);
            rewardRepository.save(reward);
        }

        String redemptionCode = generateRedemptionCode();
        int expiryDays = reward.getExpiryDays() != null ? reward.getExpiryDays() : 90;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(expiryDays);

        RewardRedemption redemption = RewardRedemption.builder()
                .redemptionCode(redemptionCode)
                .customerId(customerId)
                .customerNumber(customerNumber)
                .reward(reward)
                .pointsSpent(reward.getPointsRequired())
                .status(RedemptionStatus.COMPLETED)
                .expiresAt(expiresAt)
                .build();

        RewardRedemption saved = redemptionRepository.save(redemption);
        log.info("Processed redemption: code={}, customer={}, reward={}", redemptionCode, customerId, reward.getTitle());

        // Publish event to Kafka
        RewardRedemptionRequestedEvent event = RewardRedemptionRequestedEvent.builder()
                .correlationId(UUID.randomUUID().toString())
                .redemptionId(saved.getId())
                .customerId(customerId)
                .customerNumber(customerNumber)
                .rewardId(reward.getId())
                .rewardTitle(reward.getTitle())
                .pointsRequired(reward.getPointsRequired())
                .build();

        producerService.publishRedemptionRequestedEvent(event);

        return mapToResponse(saved);
    }

    @Transactional
    public void handleRedemptionResult(RewardRedemptionCompletedEvent event) {
        log.info("Processing redemption completed event for id: {}, status: {}", event.getRedemptionId(), event.getStatus());
        redemptionRepository.findById(event.getRedemptionId()).ifPresent(redemption -> {
            if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
                redemption.setStatus(RedemptionStatus.COMPLETED);
            } else {
                redemption.setStatus(RedemptionStatus.FAILED);
                redemption.setFailureReason(event.getFailureReason());

                // Restore stock
                Reward reward = redemption.getReward();
                if (!reward.getIsUnlimitedStock()) {
                    reward.setStockQuantity(reward.getStockQuantity() + 1);
                    rewardRepository.save(reward);
                }
            }
            redemptionRepository.save(redemption);
        });
    }

    @Transactional(readOnly = true)
    public RedemptionVerifyResponse verifyVoucher(String code) {
        var opt = redemptionRepository.findByRedemptionCode(code.trim().toUpperCase());
        if (opt.isEmpty()) {
            return RedemptionVerifyResponse.builder()
                    .valid(false)
                    .message("Invalid voucher code: Voucher does not exist")
                    .build();
        }

        RewardRedemption redemption = opt.get();

        if (redemption.getStatus() == RedemptionStatus.USED) {
            return RedemptionVerifyResponse.builder()
                    .valid(false)
                    .message("Voucher has already been used on " + redemption.getUsedAt())
                    .redemptionCode(redemption.getRedemptionCode())
                    .rewardTitle(redemption.getReward().getTitle())
                    .customerId(redemption.getCustomerId())
                    .status(redemption.getStatus())
                    .usedAt(redemption.getUsedAt())
                    .build();
        }

        if (redemption.getExpiresAt() != null && redemption.getExpiresAt().isBefore(LocalDateTime.now())) {
            return RedemptionVerifyResponse.builder()
                    .valid(false)
                    .message("Voucher has expired on " + redemption.getExpiresAt())
                    .redemptionCode(redemption.getRedemptionCode())
                    .rewardTitle(redemption.getReward().getTitle())
                    .customerId(redemption.getCustomerId())
                    .status(RedemptionStatus.EXPIRED)
                    .expiresAt(redemption.getExpiresAt())
                    .build();
        }

        if (redemption.getStatus() != RedemptionStatus.COMPLETED) {
            return RedemptionVerifyResponse.builder()
                    .valid(false)
                    .message("Voucher status is " + redemption.getStatus())
                    .redemptionCode(redemption.getRedemptionCode())
                    .rewardTitle(redemption.getReward().getTitle())
                    .status(redemption.getStatus())
                    .build();
        }

        return RedemptionVerifyResponse.builder()
                .valid(true)
                .message("Voucher is valid and ready to be claimed")
                .redemptionCode(redemption.getRedemptionCode())
                .rewardTitle(redemption.getReward().getTitle())
                .customerId(redemption.getCustomerId())
                .status(redemption.getStatus())
                .redeemedAt(redemption.getRedeemedAt())
                .expiresAt(redemption.getExpiresAt())
                .build();
    }

    @Transactional
    public RedemptionResponse markVoucherUsed(String code) {
        RewardRedemption redemption = redemptionRepository.findByRedemptionCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found with code: " + code));

        if (redemption.getStatus() == RedemptionStatus.USED) {
            throw new IllegalStateException("Voucher has already been redeemed/used");
        }

        if (redemption.getExpiresAt() != null && redemption.getExpiresAt().isBefore(LocalDateTime.now())) {
            redemption.setStatus(RedemptionStatus.EXPIRED);
            redemptionRepository.save(redemption);
            throw new IllegalStateException("Voucher has expired");
        }

        redemption.setStatus(RedemptionStatus.USED);
        redemption.setUsedAt(LocalDateTime.now());
        RewardRedemption saved = redemptionRepository.save(redemption);
        log.info("Marked voucher {} as USED", code);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<RedemptionResponse> getCustomerRedemptions(Long customerId, Pageable pageable) {
        return redemptionRepository.findByCustomerIdOrderByRedeemedAtDesc(customerId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RedemptionResponse> getAllRedemptions(Pageable pageable) {
        return redemptionRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private String generateRedemptionCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("RWD-");
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }

    public RedemptionResponse mapToResponse(RewardRedemption redemption) {
        return RedemptionResponse.builder()
                .id(redemption.getId())
                .redemptionCode(redemption.getRedemptionCode())
                .customerId(redemption.getCustomerId())
                .customerNumber(redemption.getCustomerNumber())
                .rewardId(redemption.getReward().getId())
                .rewardTitle(redemption.getReward().getTitle())
                .pointsSpent(redemption.getPointsSpent())
                .status(redemption.getStatus())
                .failureReason(redemption.getFailureReason())
                .redeemedAt(redemption.getRedeemedAt())
                .expiresAt(redemption.getExpiresAt())
                .usedAt(redemption.getUsedAt())
                .build();
    }
}

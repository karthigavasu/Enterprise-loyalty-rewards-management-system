package com.loyalty.rewards.service;

import com.loyalty.common.enums.TierLevel;
import com.loyalty.rewards.dto.RewardRequest;
import com.loyalty.rewards.dto.RewardResponse;
import com.loyalty.rewards.entity.Reward;
import com.loyalty.rewards.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardService {

    private final RewardRepository rewardRepository;

    @Transactional(readOnly = true)
    public Page<RewardResponse> searchActiveRewards(String category, Long maxPoints, String query, Pageable pageable) {
        return rewardRepository.searchActiveRewards(category, maxPoints, query, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<RewardResponse> getAllRewards(Pageable pageable) {
        return rewardRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public RewardResponse getRewardById(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found with id: " + id));
        return mapToResponse(reward);
    }

    @Transactional
    public RewardResponse createReward(RewardRequest request) {
        Reward reward = Reward.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .pointsRequired(request.getPointsRequired())
                .originalValue(request.getOriginalValue())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 100)
                .isUnlimitedStock(request.getIsUnlimitedStock() != null && request.getIsUnlimitedStock())
                .minimumTierRequired(request.getMinimumTierRequired() != null ? request.getMinimumTierRequired() : TierLevel.BRONZE)
                .active(request.getActive() != null ? request.getActive() : true)
                .imageUrl(request.getImageUrl())
                .termsAndConditions(request.getTermsAndConditions())
                .expiryDays(request.getExpiryDays() != null ? request.getExpiryDays() : 90)
                .build();

        Reward saved = rewardRepository.save(reward);
        log.info("Created reward: {} (id: {})", saved.getTitle(), saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public RewardResponse updateReward(Long id, RewardRequest request) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found with id: " + id));

        reward.setTitle(request.getTitle());
        reward.setDescription(request.getDescription());
        reward.setCategory(request.getCategory());
        reward.setPointsRequired(request.getPointsRequired());
        if (request.getOriginalValue() != null) reward.setOriginalValue(request.getOriginalValue());
        if (request.getCurrency() != null) reward.setCurrency(request.getCurrency());
        if (request.getStockQuantity() != null) reward.setStockQuantity(request.getStockQuantity());
        if (request.getIsUnlimitedStock() != null) reward.setIsUnlimitedStock(request.getIsUnlimitedStock());
        if (request.getMinimumTierRequired() != null) reward.setMinimumTierRequired(request.getMinimumTierRequired());
        if (request.getActive() != null) reward.setActive(request.getActive());
        if (request.getImageUrl() != null) reward.setImageUrl(request.getImageUrl());
        if (request.getTermsAndConditions() != null) reward.setTermsAndConditions(request.getTermsAndConditions());
        if (request.getExpiryDays() != null) reward.setExpiryDays(request.getExpiryDays());

        Reward saved = rewardRepository.save(reward);
        log.info("Updated reward id: {}", id);
        return mapToResponse(saved);
    }

    @Transactional
    public RewardResponse toggleStatus(Long id, boolean active) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found with id: " + id));

        reward.setActive(active);
        Reward saved = rewardRepository.save(reward);
        log.info("Toggled reward id {} active status to: {}", id, active);
        return mapToResponse(saved);
    }

    public RewardResponse mapToResponse(Reward reward) {
        return RewardResponse.builder()
                .id(reward.getId())
                .title(reward.getTitle())
                .description(reward.getDescription())
                .category(reward.getCategory())
                .pointsRequired(reward.getPointsRequired())
                .originalValue(reward.getOriginalValue())
                .currency(reward.getCurrency())
                .stockQuantity(reward.getStockQuantity())
                .isUnlimitedStock(reward.getIsUnlimitedStock())
                .minimumTierRequired(reward.getMinimumTierRequired())
                .active(reward.getActive())
                .imageUrl(reward.getImageUrl())
                .termsAndConditions(reward.getTermsAndConditions())
                .expiryDays(reward.getExpiryDays())
                .createdAt(reward.getCreatedAt())
                .updatedAt(reward.getUpdatedAt())
                .build();
    }
}

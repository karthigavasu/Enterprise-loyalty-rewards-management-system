package com.loyalty.rewards.repository;

import com.loyalty.common.enums.RedemptionStatus;
import com.loyalty.rewards.entity.RewardRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardRedemptionRepository extends JpaRepository<RewardRedemption, Long> {
    Optional<RewardRedemption> findByRedemptionCode(String redemptionCode);
    Page<RewardRedemption> findByCustomerIdOrderByRedeemedAtDesc(Long customerId, Pageable pageable);
    long countByStatus(RedemptionStatus status);
}

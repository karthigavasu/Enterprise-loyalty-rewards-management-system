package com.loyalty.points.repository;

import com.loyalty.common.enums.TierLevel;
import com.loyalty.points.entity.TierConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TierConfigRepository extends JpaRepository<TierConfig, Long> {
    Optional<TierConfig> findByTier(TierLevel tier);
}

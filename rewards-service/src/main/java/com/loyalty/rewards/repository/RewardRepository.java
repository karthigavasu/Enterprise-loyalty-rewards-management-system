package com.loyalty.rewards.repository;

import com.loyalty.rewards.entity.Reward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    Page<Reward> findByActiveTrue(Pageable pageable);

    @Query("SELECT r FROM Reward r WHERE r.active = true AND " +
           "(:category IS NULL OR LOWER(r.category) = LOWER(:category)) AND " +
           "(:maxPoints IS NULL OR r.pointsRequired <= :maxPoints) AND " +
           "(:query IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Reward> searchActiveRewards(
            @Param("category") String category,
            @Param("maxPoints") Long maxPoints,
            @Param("query") String query,
            Pageable pageable
    );

    List<Reward> findTop6ByActiveTrueOrderByPointsRequiredAsc();

    long countByActiveTrue();
}

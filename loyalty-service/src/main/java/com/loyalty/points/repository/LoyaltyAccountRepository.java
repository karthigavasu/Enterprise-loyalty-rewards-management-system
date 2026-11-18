package com.loyalty.points.repository;

import com.loyalty.points.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByCustomerId(Long customerId);
    Optional<LoyaltyAccount> findByCustomerNumber(String customerNumber);

    @Query("SELECT COALESCE(SUM(la.currentPointsBalance), 0) FROM LoyaltyAccount la")
    Long sumTotalCurrentPoints();

    @Query("SELECT COALESCE(SUM(la.lifetimeEarnedPoints), 0) FROM LoyaltyAccount la")
    Long sumTotalLifetimeEarnedPoints();

    @Query("SELECT COALESCE(SUM(la.lifetimeRedeemedPoints), 0) FROM LoyaltyAccount la")
    Long sumTotalLifetimeRedeemedPoints();
}

package com.loyalty.points.repository;

import com.loyalty.points.entity.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    Page<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Page<LoyaltyTransaction> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
    List<LoyaltyTransaction> findTop10ByCustomerIdOrderByCreatedAtDesc(Long customerId);
}

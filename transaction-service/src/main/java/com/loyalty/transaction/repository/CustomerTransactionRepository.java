package com.loyalty.transaction.repository;

import com.loyalty.common.enums.TransactionCategory;
import com.loyalty.transaction.entity.CustomerTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CustomerTransactionRepository extends JpaRepository<CustomerTransaction, Long> {
    Optional<CustomerTransaction> findByTransactionReference(String transactionReference);
    boolean existsByTransactionReference(String transactionReference);

    Page<CustomerTransaction> findByCustomerIdOrderByTransactionDateDesc(Long customerId, Pageable pageable);

    @Query("SELECT t FROM CustomerTransaction t WHERE " +
           "(:customerId IS NULL OR t.customerId = :customerId) AND " +
           "(:merchant IS NULL OR LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :merchant, '%'))) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate)")
    Page<CustomerTransaction> searchTransactions(
            @Param("customerId") Long customerId,
            @Param("merchant") String merchant,
            @Param("category") TransactionCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM CustomerTransaction t WHERE t.status = 'COMPLETED'")
    BigDecimal sumTotalCompletedVolume();

    @Query("SELECT COALESCE(AVG(t.amount), 0) FROM CustomerTransaction t WHERE t.status = 'COMPLETED'")
    BigDecimal avgCompletedTransactionAmount();
}

package com.loyalty.customer.repository;

import com.loyalty.common.enums.CustomerStatus;
import com.loyalty.common.enums.TierLevel;
import com.loyalty.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerNumber(String customerNumber);
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByUserEmail(String email);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.user.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.user.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.user.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.user.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:tier IS NULL OR c.tier = :tier) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<Customer> searchCustomers(
            @Param("query") String query,
            @Param("tier") TierLevel tier,
            @Param("status") CustomerStatus status,
            Pageable pageable
    );

    long countByStatus(CustomerStatus status);
    long countByTier(TierLevel tier);
}

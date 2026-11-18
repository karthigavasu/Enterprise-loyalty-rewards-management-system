package com.loyalty.transaction.entity;

import com.loyalty.common.enums.PaymentMethod;
import com.loyalty.common.enums.TransactionCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_transactions", indexes = {
        @Index(name = "idx_txn_reference", columnList = "transaction_reference", unique = true),
        @Index(name = "idx_txn_customer", columnList = "customer_id"),
        @Index(name = "idx_txn_date", columnList = "transaction_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
    private String transactionReference;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_number", length = 32)
    private String customerNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "store_location", length = 100)
    private String storeLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TransactionCategory category = TransactionCategory.RETAIL;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "processed_for_points", nullable = false)
    @Builder.Default
    private Boolean processedForPoints = false;

    @Column(name = "points_awarded")
    @Builder.Default
    private Long pointsAwarded = 0L;

    @Column(length = 255)
    private String notes;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransactionItem> items = new ArrayList<>();

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

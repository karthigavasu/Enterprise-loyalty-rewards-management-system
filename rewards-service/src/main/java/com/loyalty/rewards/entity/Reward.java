package com.loyalty.rewards.entity;

import com.loyalty.common.enums.TierLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category; // VOUCHER, GIFT_CARD, MERCHANDISE, EXPERIENCE, DISCOUNT

    @Column(name = "points_required", nullable = false)
    private Long pointsRequired;

    @Column(name = "original_value", precision = 12, scale = 2)
    private BigDecimal originalValue;

    @Column(length = 10)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 100;

    @Column(name = "is_unlimited_stock", nullable = false)
    @Builder.Default
    private Boolean isUnlimitedStock = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimum_tier_required", nullable = false, length = 20)
    @Builder.Default
    private TierLevel minimumTierRequired = TierLevel.BRONZE;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "expiry_days")
    @Builder.Default
    private Integer expiryDays = 90;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

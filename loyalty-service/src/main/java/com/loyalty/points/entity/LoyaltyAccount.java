package com.loyalty.points.entity;

import com.loyalty.common.enums.TierLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "loyalty_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "customer_number", nullable = false, unique = true, length = 32)
    private String customerNumber;

    @Column(name = "current_points_balance", nullable = false)
    @Builder.Default
    private Long currentPointsBalance = 0L;

    @Column(name = "lifetime_earned_points", nullable = false)
    @Builder.Default
    private Long lifetimeEarnedPoints = 0L;

    @Column(name = "lifetime_redeemed_points", nullable = false)
    @Builder.Default
    private Long lifetimeRedeemedPoints = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_level", nullable = false, length = 20)
    @Builder.Default
    private TierLevel tierLevel = TierLevel.BRONZE;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

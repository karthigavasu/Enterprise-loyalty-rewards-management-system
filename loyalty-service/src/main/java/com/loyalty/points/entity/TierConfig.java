package com.loyalty.points.entity;

import com.loyalty.common.enums.TierLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tier_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private TierLevel tier;

    @Column(name = "min_points_required", nullable = false)
    private Long minPointsRequired;

    @Column(name = "earn_multiplier", nullable = false)
    private Double earnMultiplier;

    @Column(name = "perks_description", length = 500)
    private String perksDescription;
}

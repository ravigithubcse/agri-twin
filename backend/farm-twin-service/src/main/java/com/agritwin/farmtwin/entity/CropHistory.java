package com.agritwin.farmtwin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single historical crop cycle on a land parcel. This is the raw
 * ground-truth data that will eventually feed the Income Prediction Models
 * (blueprint 6.4) once a real training pipeline exists — every record
 * logged here today is tomorrow's training data.
 */
@Entity
@Table(name = "crop_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "land_parcel_id", nullable = false)
    private LandParcel landParcel;

    @Column(name = "crop_name", nullable = false, length = 100)
    private String cropName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Season season;

    @Column(name = "yield_quintals", precision = 10, scale = 2)
    private BigDecimal yieldQuintals;

    @Column(name = "income_inr", precision = 12, scale = 2)
    private BigDecimal incomeInr;

    @Column(name = "input_cost_inr", precision = 12, scale = 2)
    private BigDecimal inputCostInr;

    @Column(name = "market_name", length = 150)
    private String marketName;

    @Column(name = "sale_date")
    private LocalDate saleDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

package com.agritwin.farmtwin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A single plot of land belonging to a FarmTwin. Stores a lat/lng centroid
 * rather than a full PostGIS polygon for now — see migration V1 comment for
 * why, and what upgrading to a real geo_boundary would involve.
 */
@Entity
@Table(name = "land_parcels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandParcel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_twin_id", nullable = false)
    private FarmTwin farmTwin;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "soil_type", length = 50)
    private String soilType;

    @Column(name = "irrigation_type", length = 50)
    private String irrigationType;

    @Column(name = "area_acres", nullable = false, precision = 8, scale = 3)
    private BigDecimal areaAcres;

    @Column(name = "current_crop", length = 100)
    private String currentCrop;

    @Column(name = "sowing_date")
    private LocalDate sowingDate;

    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate;

    @OneToMany(mappedBy = "landParcel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CropHistory> cropHistory = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

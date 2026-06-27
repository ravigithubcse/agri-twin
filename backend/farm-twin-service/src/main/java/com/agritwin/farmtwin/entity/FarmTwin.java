package com.agritwin.farmtwin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Farm Commodity Digital Twin (FCDT) root entity — blueprint Phase 4.1.
 * One twin per user (enforced by unique constraint on user_id). twinData is
 * an open JSONB bucket reserved for future FCDT layers (crop graph
 * snapshot, climate risk summary, etc.) that don't yet have first-class
 * relational tables.
 */
@Entity
@Table(name = "farm_twins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmTwin {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "profile_completeness_score", nullable = false)
    @Builder.Default
    private Short profileCompletenessScore = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "twin_data", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private String twinData = "{}";

    @OneToMany(mappedBy = "farmTwin", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LandParcel> landParcels = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
}

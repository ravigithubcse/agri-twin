package com.agritwin.farmtwin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AGRI-TWIN AI - Farm Twin Service
 *
 * Owns the Farm Commodity Digital Twin (FCDT) — blueprint Phase 4: each
 * farmer's land parcels, crop history, and the twin's rollup metadata
 * (completeness score, version). This service does NOT issue or validate
 * its own logins; it trusts JWTs minted by user-service (shared secret for
 * now — see SecurityConfig) and enforces ownership (user_id match) on every
 * query, per blueprint 5.5: "Resource-level permissions enforced per
 * user_id in every query."
 *
 * Neo4j (crop knowledge graph) and Pinecone (embeddings) from the blueprint's
 * six-layer FCDT model are NOT wired in this module — they require their own
 * managed services/credentials and are a separate module once this
 * relational core is solid. JSONB twin_data snapshot and PostGIS land
 * boundaries are likewise deferred; land_parcels currently stores lat/lng
 * centroid only, not a full geo_boundary polygon.
 */
@SpringBootApplication
public class FarmTwinServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmTwinServiceApplication.class, args);
    }
}

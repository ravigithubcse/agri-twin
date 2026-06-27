-- AGRI-TWIN AI :: farm-twin-service :: V1__create_farm_twin_tables.sql
-- Matches blueprint Section 5.4 (farm_twins, land_parcels, crop_history)
-- and Section 8.1 ER relationships: User(1)--(1)FarmTwin,
-- FarmTwin(1)--(N)LandParcel, FarmTwin(1)--(N)CropHistory.
--
-- NOTE on scope reduction vs. blueprint 5.4:
--   - embedding_id (Pinecone ref) and geo_hash: omitted for now — no
--     Pinecone integration exists yet in this module.
--   - geo_boundary (PostGIS polygon): land_parcels stores a simple
--     latitude/longitude centroid instead of a full PostGIS boundary.
--     PostGIS requires the postgis extension to be enabled on the target
--     database, which is an infra decision for the deployment module;
--     swapping centroid -> polygon later is a additive migration, not a
--     breaking one.
--   - twin_data JSONB snapshot: included, since it's cheap to support and
--     gives the FCDT a place to grow into without further migrations.

CREATE TABLE farm_twins (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID NOT NULL,
    version                     INTEGER NOT NULL DEFAULT 1,
    profile_completeness_score  SMALLINT NOT NULL DEFAULT 0,
    twin_data                   JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    last_updated                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT uq_farm_twins_user_id UNIQUE (user_id),
    CONSTRAINT chk_farm_twins_completeness CHECK (profile_completeness_score BETWEEN 0 AND 100)
);

-- Twin-by-user fetch is the hot path (blueprint 8.2)
CREATE INDEX idx_farm_twins_user_id ON farm_twins (user_id);

CREATE TABLE land_parcels (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_twin_id        UUID NOT NULL REFERENCES farm_twins (id) ON DELETE CASCADE,
    label               VARCHAR(100) NOT NULL,
    latitude            DECIMAL(9,6),
    longitude           DECIMAL(9,6),
    soil_type           VARCHAR(50),
    irrigation_type     VARCHAR(50),
    area_acres          DECIMAL(8,3) NOT NULL,
    current_crop        VARCHAR(100),
    sowing_date         DATE,
    expected_harvest_date DATE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT chk_land_parcels_area_positive CHECK (area_acres > 0)
);

CREATE INDEX idx_land_parcels_farm_twin_id ON land_parcels (farm_twin_id);

CREATE TABLE crop_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    land_parcel_id  UUID NOT NULL REFERENCES land_parcels (id) ON DELETE CASCADE,
    crop_name       VARCHAR(100) NOT NULL,
    season          VARCHAR(20) NOT NULL,
    yield_quintals  DECIMAL(10,2),
    income_inr      DECIMAL(12,2),
    input_cost_inr  DECIMAL(12,2),
    market_name     VARCHAR(150),
    sale_date       DATE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT chk_crop_history_season CHECK (season IN ('KHARIF', 'RABI', 'ZAID', 'PERENNIAL'))
);

CREATE INDEX idx_crop_history_land_parcel_id ON crop_history (land_parcel_id);
CREATE INDEX idx_crop_history_sale_date ON crop_history (sale_date);

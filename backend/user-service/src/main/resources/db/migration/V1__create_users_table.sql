-- AGRI-TWIN AI :: user-service :: V1__create_users_table.sql
-- Core identity table. Matches blueprint Section 5.4 (users table) plus
-- password_hash and role for auth, which the blueprint's narrative section
-- implies (JWT + RBAC) but doesn't enumerate as columns explicitly.

CREATE TABLE users (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone                VARCHAR(15)  NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    full_name            VARCHAR(150) NOT NULL,
    aadhaar_hash         VARCHAR(255),
    role                 VARCHAR(30)  NOT NULL DEFAULT 'FARMER',
    tier                 VARCHAR(30)  NOT NULL DEFAULT 'FREE',
    state_code           VARCHAR(10),
    district_code        VARCHAR(10),
    language_preference  VARCHAR(10)  NOT NULL DEFAULT 'hi',
    literacy_flag        BOOLEAN      NOT NULL DEFAULT TRUE,
    account_status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    last_login           TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_phone UNIQUE (phone),
    CONSTRAINT chk_users_role CHECK (role IN ('FARMER', 'COOPERATIVE_ADMIN', 'ENTERPRISE_BUYER', 'PLATFORM_ADMIN')),
    CONSTRAINT chk_users_tier CHECK (tier IN ('FREE', 'KISAN_PRO', 'KISAN_EXPERT', 'COOPERATIVE', 'ENTERPRISE')),
    CONSTRAINT chk_users_account_status CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

-- Login lookup is the hottest path in the system (blueprint 8.2: P99 <5ms target)
CREATE UNIQUE INDEX idx_users_phone ON users (phone);
CREATE INDEX idx_users_account_status ON users (account_status) WHERE account_status != 'ACTIVE';

CREATE TABLE refresh_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token_hash   VARCHAR(255) NOT NULL,
    issued_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked      BOOLEAN NOT NULL DEFAULT FALSE,
    device_info  VARCHAR(255),

    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at) WHERE revoked = FALSE;

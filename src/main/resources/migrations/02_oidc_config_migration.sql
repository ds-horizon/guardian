-- OIDC Configuration Migration
-- This migration adds OpenID Connect discovery document configuration tables

-- Static infrastructure OIDC configuration
CREATE TABLE oidc_tenant_config (
    tenant_id CHAR(10) PRIMARY KEY,
    issuer VARCHAR(255) NOT NULL,
    authorization_endpoint VARCHAR(255) NOT NULL,
    token_endpoint VARCHAR(255) NOT NULL,
    userinfo_endpoint VARCHAR(255) NOT NULL,
    revocation_endpoint VARCHAR(255) NOT NULL,
    jwks_uri VARCHAR(255) NOT NULL,
    grant_types_supported JSON NOT NULL,
    response_types_supported JSON NOT NULL,
    subject_types_supported JSON NOT NULL,
    id_token_signing_alg_values_supported JSON NOT NULL,
    userinfo_signing_alg_values_supported JSON NOT NULL,
    token_endpoint_auth_methods_supported JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_oidc_tenant_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- Scope management with embedded claims
CREATE TABLE scope (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    scope VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    claims JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY `idx_tenant_scope` (`tenant_id`, `scope`),
    INDEX `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
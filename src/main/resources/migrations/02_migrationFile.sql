-- liquibase formatted sql
-- changeset guardian:2

ALTER TABLE client ADD COLUMN mfa_policy ENUM('not_required', 'mandatory') NOT NULL DEFAULT 'not_required';
ALTER TABLE client ADD COLUMN allowed_mfa_methods JSON NULL; -- e.g., ["pin", "social-login", "webauthn"];

CREATE TABLE credentials (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tenant_id         VARCHAR(36)     NOT NULL,
  client_id         VARCHAR(20)     NOT NULL,
  user_id           VARCHAR(36)     NOT NULL,
  credential_id     VARCHAR(255)  NOT NULL,
  public_key        TEXT            NOT NULL,
  binding_type      ENUM('webauthn','appkey') NOT NULL,
  alg               INT             NOT NULL,
  sign_count        BIGINT UNSIGNED NOT NULL DEFAULT 0,
  aaguid            VARCHAR(128)    NULL,
  revoked_at        TIMESTAMP       NULL DEFAULT NULL,
  is_active         TINYINT(1) GENERATED ALWAYS AS (revoked_at IS NULL) STORED,
  first_use_complete BOOLEAN        NOT NULL DEFAULT FALSE,
  created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_user_credential (tenant_id, client_id, user_id, credential_id),
  INDEX idx_credentials_lookup (tenant_id, client_id, user_id, is_active),
  INDEX idx_credentials_credential_id (tenant_id, client_id, user_id, credential_id, is_active)
);

-- Add columns to capture the complete authentication context
ALTER TABLE refresh_tokens ADD COLUMN credential_id VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE refresh_tokens ADD COLUMN platform VARCHAR(50) NULL;
ALTER TABLE refresh_tokens ADD COLUMN device_model VARCHAR(100) NULL;
ALTER TABLE refresh_tokens ADD COLUMN os_version VARCHAR(50) NULL;
ALTER TABLE refresh_tokens ADD COLUMN app_version VARCHAR(50) NULL;
CREATE INDEX idx_rt_tenant_user_cred ON refresh_tokens (tenant_id, user_id, credential_id, is_active);

CREATE TABLE webauthn_config (
  tenant_id               CHAR(10)     NOT NULL,
  client_id               VARCHAR(32)     NOT NULL,
  rp_id                   VARCHAR(255) NOT NULL,
  allowed_web_origins     JSON         NOT NULL DEFAULT (JSON_ARRAY()),
  allowed_algorithms      JSON         NOT NULL DEFAULT (JSON_ARRAY('ES256')),
  aaguid_policy_mode      ENUM('allowlist','mds_enforced','any') NOT NULL DEFAULT 'allowlist',
  allowed_aaguids         JSON         NOT NULL DEFAULT (JSON_ARRAY()),
  blocked_aaguids         JSON         NOT NULL DEFAULT (JSON_ARRAY()),
  require_uv_enrollment   BOOLEAN      NOT NULL DEFAULT TRUE,
  require_uv_auth         BOOLEAN      NOT NULL DEFAULT TRUE,
  allowed_transports      JSON         NOT NULL DEFAULT (JSON_ARRAY('internal','hybrid','nfc','usb','ble')),
  require_device_bound    BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (tenant_id, client_id),
  CONSTRAINT fk_mfa_tenant FOREIGN KEY (tenant_id) REFERENCES tenant(id) ON DELETE CASCADE,
  CONSTRAINT fk_mfa_client FOREIGN KEY (tenant_id, client_id) REFERENCES client(tenant_id, client_id) ON DELETE CASCADE
);

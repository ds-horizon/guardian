-- liquibase formatted sql
-- changeset guardian:1

CREATE TABLE tenant
(
    id         CHAR(10)     NOT NULL,
    name       VARCHAR(256) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),

    UNIQUE KEY `tenant_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE user_config
(
    tenant_id              CHAR(10) PRIMARY KEY,
    is_ssl_enabled         BOOLEAN               DEFAULT FALSE,
    host                   VARCHAR(256) NOT NULL,
    port                   INT          NOT NULL DEFAULT 80,
    get_user_path          VARCHAR(256) NOT NULL,
    create_user_path       VARCHAR(256) NOT NULL,
    authenticate_user_path VARCHAR(256) NOT NULL,
    add_provider_path      VARCHAR(256) NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_user_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE email_config
(
    tenant_id       CHAR(10) PRIMARY KEY,
    is_ssl_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    host            VARCHAR(256) NOT NULL,
    port            INT          NOT NULL DEFAULT 80,
    send_email_path VARCHAR(256) NOT NULL,
    template_name   VARCHAR(256) NOT NULL,
    template_params JSON         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_email_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE sms_config
(
    tenant_id       CHAR(10) PRIMARY KEY,
    is_ssl_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    host            VARCHAR(256) NOT NULL,
    port            INT          NOT NULL,
    send_sms_path   VARCHAR(256) NOT NULL,
    template_name   VARCHAR(256) NOT NULL,
    template_params JSON         NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_sms_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE fb_config
(
    tenant_id       CHAR(10) PRIMARY KEY,
    app_id          VARCHAR(256) NOT NULL,
    app_secret      VARCHAR(256) NOT NULL,
    send_app_secret BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_fb_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE google_config
(
    tenant_id     CHAR(10) PRIMARY KEY,
    client_id     VARCHAR(256) NOT NULL,
    client_secret VARCHAR(256) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_google_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE token_config
(
    tenant_id            CHAR(10) PRIMARY KEY,
    algorithm            VARCHAR(10)  NOT NULL,
    issuer               VARCHAR(256) NOT NULL,
    rsa_keys             JSON         NOT NULL,
    access_token_expiry  INT          NOT NULL,
    refresh_token_expiry INT          NOT NULL,
    id_token_expiry      INT          NOT NULL,
    id_token_claims      JSON         NOT NULL,
    cookie_same_site     VARCHAR(20)  NOT NULL DEFAULT 'LAX',
    cookie_domain        VARCHAR(256) NOT NULL,
    cookie_path          VARCHAR(256) NOT NULL DEFAULT '/',
    cookie_secure        BOOLEAN      NOT NULL DEFAULT FALSE,
    cookie_http_only     BOOLEAN      NOT NULL DEFAULT TRUE,
    access_token_claims  JSON         NOT NULL DEFAULT (JSON_ARRAY()),
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_token_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE auth_code_config
(
    tenant_id  CHAR(10) PRIMARY KEY,
    ttl        INT       NOT NULL,
    length     INT       NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_auth_code_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE otp_config
(
    tenant_id           CHAR(10) PRIMARY KEY,
    is_otp_mocked       BOOLEAN   NOT NULL DEFAULT FALSE,
    otp_length          INT       NOT NULL DEFAULT 6,
    try_limit           INT       NOT NULL DEFAULT 5,
    resend_limit        INT       NOT NULL DEFAULT 5,
    otp_resend_interval INT       NOT NULL DEFAULT 30,
    otp_validity        INT       NOT NULL DEFAULT 900,
    whitelisted_inputs  JSON      NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_otp_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE refresh_tokens
(
    id                BIGINT                     NOT NULL AUTO_INCREMENT,
    tenant_id         CHAR(10)                   NOT NULL,
    user_id           CHAR(64)                   NOT NULL,
    is_active         BOOLEAN                    NOT NULL DEFAULT TRUE,
    refresh_token     CHAR(32) COLLATE ascii_bin NOT NULL,
    refresh_token_exp BIGINT UNSIGNED NOT NULL,
    source            VARCHAR(50),
    device_name       VARCHAR(256),
    location          VARCHAR(256),
    ip                VARBINARY(16),
    created_at        TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY               `idx_refresh_token` (`tenant_id`, `refresh_token`, `is_active`, `refresh_token_exp`, `user_id`),
    KEY               `idx_refresh_token_user` (`tenant_id`, `user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE contact_verify_config
(
    tenant_id           CHAR(10) PRIMARY KEY,
    is_otp_mocked       BOOLEAN   NOT NULL DEFAULT FALSE,
    otp_length          INT       NOT NULL DEFAULT 6,
    try_limit           INT       NOT NULL DEFAULT 5,
    resend_limit        INT       NOT NULL DEFAULT 5,
    otp_resend_interval INT       NOT NULL DEFAULT 30,
    otp_validity        INT       NOT NULL DEFAULT 900,
    whitelisted_inputs  JSON      NOT NULL DEFAULT (JSON_OBJECT()),
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_contact_verify_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE oidc_provider_config
(
    tenant_id          CHAR(10)     NOT NULL,
    provider_name      VARCHAR(50)  NOT NULL,
    issuer             TEXT         NOT NULL,
    jwks_url           TEXT         NOT NULL,
    token_url          TEXT         NOT NULL,
    client_id          VARCHAR(256) NOT NULL,
    client_secret      TEXT         NOT NULL,
    redirect_uri       TEXT         NOT NULL,
    client_auth_method VARCHAR(256) NOT NULL,
    is_ssl_enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    user_identifier    VARCHAR(20)  NOT NULL DEFAULT 'email',
    audience_claims    JSON         NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tenant_oidc_provider_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,
    PRIMARY KEY `idx_tenant_provider` (`tenant_id`, `provider_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_flow_block
(
    tenant_id       CHAR(10)    NOT NULL,
    user_identifier VARCHAR(64) NOT NULL,
    flow_name       CHAR(20)    NOT NULL,
    reason          VARCHAR(500),
    unblocked_at    BIGINT,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_user_flow_block FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    PRIMARY KEY pk_tenant_user_flow (tenant_id, flow_name, user_identifier),

    KEY             idx_user_flow_block_tenant_user_flow (tenant_id, flow_name, user_identifier, unblocked_at)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE admin_config
(
    tenant_id  CHAR(10) PRIMARY KEY,
    username   VARCHAR(50) NOT NULL,
    password   VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_admin_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE scope
(
    tenant_id    CHAR(10)     NOT NULL,
    name         VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    description  VARCHAR(1000),
    icon_url     VARCHAR(2083),
    claims       JSON         NOT NULL,
    is_oidc      BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY `uniq_tenant_scope` (`tenant_id`, `name`),
    KEY          `idx_tenant_oidc` (`tenant_id`, `is_oidc`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE oidc_config
(
    tenant_id                             CHAR(10)     NOT NULL,
    issuer                                VARCHAR(255) NOT NULL,
    authorization_endpoint                VARCHAR(255) NOT NULL,
    token_endpoint                        VARCHAR(255) NOT NULL,
    userinfo_endpoint                     VARCHAR(255) NOT NULL,
    revocation_endpoint                   VARCHAR(255) NOT NULL,
    jwks_uri                              VARCHAR(255) NOT NULL,
    grant_types_supported                 JSON         NOT NULL DEFAULT (JSON_ARRAY()),
    response_types_supported              JSON         NOT NULL DEFAULT (JSON_ARRAY()),
    subject_types_supported               JSON         NOT NULL DEFAULT (JSON_ARRAY()),
    id_token_signing_alg_values_supported JSON         NOT NULL DEFAULT (JSON_ARRAY()),
    token_endpoint_auth_methods_supported JSON         NOT NULL DEFAULT (JSON_ARRAY()),
    login_page_uri                        VARCHAR(512)          DEFAULT NULL,
    consent_page_uri                      VARCHAR(512)          DEFAULT NULL,
    authorize_ttl                         INT                   DEFAULT NULL,
    updated_at                            TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at                            TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY `uniq_tenant_config` (`tenant_id`),

    CONSTRAINT `fk_oidc_config` FOREIGN KEY (`tenant_id`)
        REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE client
(
    tenant_id      CHAR(10)     NOT NULL,
    client_id      VARCHAR(100) NOT NULL,
    client_name    VARCHAR(100) NOT NULL,
    client_secret  VARCHAR(100) NOT NULL,
    client_uri     VARCHAR(2083),
    contacts       JSON,
    grant_types    JSON         NOT NULL,
    logo_uri       VARCHAR(2083),
    policy_uri     VARCHAR(2083),
    redirect_uris  JSON         NOT NULL,
    response_types JSON         NOT NULL,
    skip_consent   BOOLEAN               DEFAULT FALSE,
    client_type    CHAR(11)     NOT NULL DEFAULT "first_party",
    is_default     BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_id, client_id),
    UNIQUE KEY unique_tenant_client_name (tenant_id, client_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE client_scope
(
    id         INT AUTO_INCREMENT,
    tenant_id  CHAR(10)     NOT NULL,
    scope      VARCHAR(100) NOT NULL,
    client_id  VARCHAR(100) NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY unique_tenant_client_scope (tenant_id, client_id, scope),
    FOREIGN KEY (tenant_id, client_id) REFERENCES client (tenant_id, client_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id, scope) REFERENCES scope (tenant_id, name) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE consent
(
    tenant_id  CHAR(10)     NOT NULL,
    client_id  VARCHAR(100) NOT NULL,
    user_id    CHAR(64)     NOT NULL,
    scope      VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`tenant_id`, `client_id`, `user_id`, `scope`),
    KEY        `idx_tenant_scope` (`tenant_id`, `scope`),

    CONSTRAINT `fk_consent_tenant` FOREIGN KEY (`tenant_id`)
        REFERENCES `tenant` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_consent_client` FOREIGN KEY (`tenant_id`, `client_id`)
        REFERENCES `client` (`tenant_id`, `client_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_consent_scope` FOREIGN KEY (`tenant_id`, `scope`)
        REFERENCES `scope` (`tenant_id`, `name`) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE oidc_refresh_token
(
    id                BIGINT                     NOT NULL AUTO_INCREMENT,
    client_id         VARCHAR(100)               NOT NULL,
    tenant_id         CHAR(10)                   NOT NULL,
    user_id           CHAR(64)                   NOT NULL,
    is_active         BOOLEAN                    NOT NULL DEFAULT TRUE,
    refresh_token     CHAR(32) COLLATE ascii_bin NOT NULL,
    refresh_token_exp BIGINT UNSIGNED NOT NULL,
    scope             JSON                       NOT NULL DEFAULT (JSON_ARRAY()),
    device_name       VARCHAR(256),
    ip                VARBINARY(16),
    created_at        TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP                  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY               `idx_oidc_refresh_token` (`tenant_id`, `client_id`, `refresh_token`, `is_active`, `refresh_token_exp`, `user_id`),
    KEY               `idx_oidc_refresh_token_user` (`tenant_id`, `user_id`),
    KEY               `idx_oidc_refresh_token_no_client` (`tenant_id`, `refresh_token`, `is_active`, `refresh_token_exp`, `user_id`),
    CONSTRAINT `fk_oidc_refresh_token_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_oidc_refresh_token_client` FOREIGN KEY (`tenant_id`, `client_id`) REFERENCES `client` (`tenant_id`, `client_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE guest_config
(
    tenant_id      CHAR(10) PRIMARY KEY,
    is_encrypted   BOOLEAN   NOT NULL DEFAULT true,
    secret_key     VARCHAR(16)
        COLLATE utf8mb4_0900_as_cs,
    allowed_scopes JSON      NOT NULL DEFAULT (JSON_ARRAY()),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    KEY            `idx_guest` (`tenant_id`, `is_encrypted`),
    CONSTRAINT `fk_guest_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

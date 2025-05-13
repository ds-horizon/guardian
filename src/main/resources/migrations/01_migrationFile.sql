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
    is_ssl_enabled         BOOLEAN                DEFAULT FALSE,
    host                   VARCHAR(256) NOT NULL,
    port                   INT          NOT NULL  DEFAULT 80,
    get_user_path          VARCHAR(256) NOT NULL,
    create_user_path       VARCHAR(256) NOT NULL,
    authenticate_user_path VARCHAR(256) NOT NULL,
    add_provider_path      VARCHAR(256) NOT NULL,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_user_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE email_config
(
    tenant_id       CHAR(10)     PRIMARY KEY,
    is_ssl_enabled  BOOLEAN      NOT NULL     DEFAULT FALSE,
    host            VARCHAR(256) NOT NULL,
    port            INT          NOT NULL     DEFAULT 80,
    send_email_path VARCHAR(256) NOT NULL,
    template_name   VARCHAR(256) NOT NULL,
    template_params JSON         NOT NULL,
    created_at      TIMESTAMP    NOT NULL     DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_email_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE sms_config
(
    tenant_id       CHAR(10)      PRIMARY KEY,
    is_ssl_enabled  BOOLEAN       NOT NULL     DEFAULT FALSE,
    host            VARCHAR(256)  NOT NULL,
    port            INT           NOT NULL,
    send_sms_path   VARCHAR(256)  NOT NULL,
    template_name   VARCHAR(256)  NOT NULL,
    template_params JSON          NOT NULL,
    created_at      TIMESTAMP     NOT NULL     DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_sms_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE fb_config
(
    tenant_id  CHAR(10)     PRIMARY KEY,
    app_id     VARCHAR(256) NOT NULL,
    app_secret VARCHAR(256) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_fb_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE google_config
(
    tenant_id     CHAR(10)     PRIMARY KEY,
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
    tenant_id            CHAR(10)     PRIMARY KEY,
    algorithm            VARCHAR(10)  NOT NULL,
    issuer               VARCHAR(256) NOT NULL,
    rsa_keys             JSON         NOT NULL,
    access_token_expiry  INT          NOT NULL,
    refresh_token_expiry INT          NOT NULL,
    id_token_expiry      INT          NOT NULL,
    id_token_claims      JSON         NOT NULL,
    created_at           TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_token_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE auth_code_config
(
    tenant_id  CHAR(10)  PRIMARY KEY,
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
    tenant_id                  CHAR(10)  PRIMARY KEY,
    is_otp_mocked              BOOLEAN   NOT NULL DEFAULT FALSE,
    otp_length                 INT       NOT NULL DEFAULT 6,
    try_limit                  INT       NOT NULL DEFAULT 5,
    resend_limit               INT       NOT NULL DEFAULT 5,
    otp_resend_interval        INT       NOT NULL DEFAULT 30,
    otp_validity               INT       NOT NULL DEFAULT 900,
    whitelisted_inputs         JSON      NOT NULL,
    created_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant_otp_config FOREIGN KEY (tenant_id)
        REFERENCES tenant (id) ON DELETE CASCADE,

    UNIQUE KEY `idx_tenant_id` (`tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE refresh_tokens
(
    id                BIGINT                      NOT NULL AUTO_INCREMENT,
    tenant_id         CHAR(10)                    NOT NULL,
    user_id           CHAR(64)                    NOT NULL,
    is_active         BOOLEAN                     NOT NULL DEFAULT TRUE,
    refresh_token     CHAR(32)  COLLATE ascii_bin NOT NULL,
    refresh_token_exp BIGINT UNSIGNED NOT NULL,
    source            VARCHAR(50),
    device_name       VARCHAR(256),
    location          VARCHAR(256),
    ip                VARBINARY(16),
    created_at        TIMESTAMP                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP                   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY               `idx_refresh_token` (`tenant_id`, `refresh_token`, `is_active`, `refresh_token_exp`, `user_id`),
    KEY               `idx_refresh_token_user` (`tenant_id`, `user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

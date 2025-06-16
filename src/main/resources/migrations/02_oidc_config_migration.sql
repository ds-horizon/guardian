--liquibase formatted sql

--changeset guardian:create-oidc-tenant-config
CREATE TABLE `oidc_tenant_config` (
                                      `id` INT NOT NULL AUTO_INCREMENT,
                                      `tenant_id` CHAR(10) NOT NULL,
                                      `issuer` VARCHAR(255) NOT NULL,
                                      `authorization_endpoint` VARCHAR(255) NOT NULL,
                                      `token_endpoint` VARCHAR(255) NOT NULL,
                                      `userinfo_endpoint` VARCHAR(255) NOT NULL,
                                      `revocation_endpoint` VARCHAR(255) NOT NULL,
                                      `jwks_uri` VARCHAR(255) NOT NULL,
                                      `grant_types_supported` JSON NOT NULL DEFAULT (JSON_ARRAY()),
                                      `response_types_supported` JSON NOT NULL DEFAULT (JSON_ARRAY()),
                                      `subject_types_supported` JSON NOT NULL DEFAULT (JSON_ARRAY()),
                                      `id_token_signing_alg_values_supported` JSON NOT NULL DEFAULT (JSON_ARRAY()),
                                      `userinfo_signing_alg_values_supported` JSON NOT NULL DEFAULT (JSON_ARRAY()),
                                      `token_endpoint_auth_methods_supported` JSON NOT NULL DEFAULT (JSON_ARRAY()),
                                      `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uniq_tenant_config` (`tenant_id`),
                                      CONSTRAINT `fk_oidc_tenant_config` FOREIGN KEY (`tenant_id`)
                                          REFERENCES `tenant`(`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

--changeset guardian:create-scope-table
CREATE TABLE `scope` (
                         `id` INT NOT NULL AUTO_INCREMENT,
                         `tenant_id` CHAR(10) NOT NULL,
                         `scope` VARCHAR(100) NOT NULL,
                         `display_name` VARCHAR(100),
                         `description` VARCHAR(1000),
                         `icon_url` VARCHAR(2083),
                         `claims` JSON NOT NULL DEFAULT (JSON_OBJECT()),
                         `is_oidc` BOOLEAN DEFAULT FALSE,
                         `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uniq_tenant_scope` (`tenant_id`, `scope`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
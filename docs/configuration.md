# Configuration Guide

This guide covers all configuration options available in Guardian.
There are two types of configurations-

1. Guardian application configuration, that includes configuration like database endpoints, server configuration etc,
   configs that are required to start guardian.
2. Tenant Configurations that dictate behaviour of guardian for a tenant.

## Guardian Configuration

Guardian application can be configured via environment variables

| Configuration Name             | Environment Variable                    | Type    | Description                                            |
|--------------------------------|-----------------------------------------|---------|--------------------------------------------------------|
| mysql_writer_host              | GUARDIAN_MYSQL_WRITER_HOST              | String  | JDBC connection URL for the database writer            |
| mysql_reader_host              | GUARDIAN_MYSQL_READER_HOST              | String  | JDBC connection URL for read replica                   |
| mysql_database                 | GUARDIAN_MYSQL_DATABASE                 | String  | Database name                                          |
| mysql_user                     | GUARDIAN_MYSQL_USER                     | String  | Username for database authentication                   |
| mysql_password                 | GUARDIAN_MYSQL_PASSWORD                 | String  | Password for database authentication                   |
| mysql_writer_max_pool_size     | GUARDIAN_MYSQL_WRITER_MAX_POOL_SIZE     | Integer | Maximum database connection pool size for writer       |
| mysql_reader_max_pool_size     | GUARDIAN_MYSQL_READER_MAX_POOL_SIZE     | Integer | Maximum database connection pool size for reader       |
| port                           | GUARDIAN_PORT                           | Integer | Port number for the Guardian server (default: 8080)    |
| redis_host                     | GUARDIAN_REDIS_HOST                     | String  | Hostname for Redis cache server                        |
| redis_port                     | GUARDIAN_REDIS_PORT                     | Integer | Port number for Redis server (default: 6379)           |
| redis_type                     | GUARDIAN_REDIS_TYPE                     | String  | Type of redis setup, valid values: STANDALONE, ClUSTER |
| http_connect_timeout           | GUARDIAN_HTTP_CONNECT_TIMEOUT           | Integer | Connection timeout value for external services in ms   |
| http_read_timeout              | GUARDIAN_HTTP_READ_TIMEOUT              | Integer | Read timeout value for external services in ms         |
| http_write_timeout             | GUARDIAN_HTTP_WRITE_TIMEOUT             | Integer | Write timeout value for external services in ms        |
| tenant_config_refresh_interval | GUARDIAN_TENANT_CONFIG_REFRESH_INTERVAL | Integer | Expiry time for tenant config in seconds               |

## Tenant Configuration

### User Configuration

| Field                  | Type         | Description                                  |
|------------------------|--------------|----------------------------------------------|
| is_ssl_enabled         | BOOLEAN      | Whether SSL is enabled for user service      |
| host                   | VARCHAR(256) | Host address for user service                |
| port                   | INT          | Port number for user service                 |
| get_user_path          | VARCHAR(256) | API path for getting user details            |
| create_user_path       | VARCHAR(256) | API path for creating users                  |
| authenticate_user_path | VARCHAR(256) | API path for user authentication             |
| add_provider_path      | VARCHAR(256) | API path for adding authentication providers |

### Email Configuration

| Field           | Type         | Description                              |
|-----------------|--------------|------------------------------------------|
| is_ssl_enabled  | BOOLEAN      | Whether SSL is enabled for email service |
| host            | VARCHAR(256) | Email service host address               |
| port            | INT          | Email service port number                |
| send_email_path | VARCHAR(256) | API path for sending emails              |
| template_name   | VARCHAR(256) | Name of the email template               |
| template_params | JSON         | Template parameters in JSON format       |

### SMS Configuration

| Field           | Type         | Description                            |
|-----------------|--------------|----------------------------------------|
| is_ssl_enabled  | BOOLEAN      | Whether SSL is enabled for SMS service |
| host            | VARCHAR(256) | SMS service host address               |
| port            | INT          | SMS service port number                |
| send_sms_path   | VARCHAR(256) | API path for sending SMS               |
| template_name   | VARCHAR(256) | Name of the SMS template               |
| template_params | JSON         | Template parameters in JSON format     |

### Facebook Configuration

| Field      | Type         | Description                 |
|------------|--------------|-----------------------------|
| app_id     | VARCHAR(256) | Facebook application ID     |
| app_secret | VARCHAR(256) | Facebook application secret |

### Google Configuration

| Field         | Type         | Description                |
|---------------|--------------|----------------------------|
| client_id     | VARCHAR(256) | Google OAuth client ID     |
| client_secret | VARCHAR(256) | Google OAuth client secret |

### Token Configuration

| Field                | Type         | Description                                                                                |
|----------------------|--------------|--------------------------------------------------------------------------------------------|
| algorithm            | VARCHAR(10)  | Token signing algorithm                                                                    |
| issuer               | VARCHAR(256) | Token issuer identifier                                                                    |
| rsa_keys             | JSON         | RSA key pair in JSON format, list of objects where each object has keys                    |
| access_token_expiry  | INT          | Access token JWT expiration time in seconds                                                |
| refresh_token_expiry | INT          | Refresh token expiration time in seconds                                                   |
| id_token_expiry      | INT          | ID token JWT expiration time in seconds                                                    |
| id_token_claims      | JSON         | Claims (key-values) to inlucde in the ID Token payload (must be part of get user response) |

### Auth Code Configuration

| Field  | Type | Description                                     |
|--------|------|-------------------------------------------------|
| ttl    | INT  | Time-to-live in seconds for authorization codes |
| length | INT  | Length of authorization codes                   |

### OTP Configuration

| Field               | Type    | Description                                                                                         |
|---------------------|---------|-----------------------------------------------------------------------------------------------------|
| is_otp_mocked       | BOOLEAN | Whether OTP is mocked for testing                                                                   |
| otp_length          | INT     | Length of OTP codes                                                                                 |
| try_limit           | INT     | Maximum number of OTP attempts                                                                      |
| resend_limit        | INT     | Maximum number of OTP resend attempts, not including the first one that is implicitly sent via init |
| otp_resend_interval | INT     | Minimum interval in seconds between OTP resends                                                     |
| otp_validity        | INT     | OTP validity duration in seconds                                                                    |
| whitelisted_inputs  | JSON    | Whitelisted OTP input patterns                                                                      |

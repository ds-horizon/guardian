---
title: Configuration Guide
description: Complete guide covering all configuration options available in Guardian including application and tenant configurations
---

This guide covers all configuration options available in Guardian.
There are two types of configurations-

1. Guardian application configuration, that includes configuration like database endpoints, server configuration etc,
   configs that dictate the behaviour of guardian.
2. Tenant Configurations that dictate behaviour of guardian for a tenant.

## Guardian Configuration

Guardian application can be configured via environment variables

## Guardian Configuration

Guardian application can be configured via environment variables

| Configuration Name             | Environment Variable                    | Type    | Default Value | Description                                            |
|--------------------------------|-----------------------------------------|---------|---------------|--------------------------------------------------------|
| mysql_writer_host              | GUARDIAN_MYSQL_WRITER_HOST              | String  | localhost     | JDBC connection URL for the database writer            |
| mysql_reader_host              | GUARDIAN_MYSQL_READER_HOST              | String  | localhost     | JDBC connection URL for read replica                   |
| mysql_database                 | GUARDIAN_MYSQL_DATABASE                 | String  | guardian      | Database name                                          |
| mysql_user                     | GUARDIAN_MYSQL_USER                     | String  | root          | Username for database authentication                   |
| mysql_password                 | GUARDIAN_MYSQL_PASSWORD                 | String  | root          | Password for database authentication                   |
| mysql_writer_max_pool_size     | GUARDIAN_MYSQL_WRITER_MAX_POOL_SIZE     | Integer | 10            | Maximum database connection pool size for writer       |
| mysql_reader_max_pool_size     | GUARDIAN_MYSQL_READER_MAX_POOL_SIZE     | Integer | 40            | Maximum database connection pool size for reader       |
| port                           | GUARDIAN_PORT                           | Integer | 8080          | Port number for the Guardian server                    |
| redis_host                     | GUARDIAN_REDIS_HOST                     | String  | localhost     | Hostname for Redis cache server                        |
| redis_port                     | GUARDIAN_REDIS_PORT                     | Integer | 6379          | Port number for Redis server                           |
| redis_type                     | GUARDIAN_REDIS_TYPE                     | String  | STANDALONE    | Type of redis setup, valid values: STANDALONE, ClUSTER |
| http_connect_timeout           | GUARDIAN_HTTP_CONNECT_TIMEOUT           | Integer | 1000          | Connection timeout value for external services in ms   |
| http_read_timeout              | GUARDIAN_HTTP_READ_TIMEOUT              | Integer | 1000          | Read timeout value for external services in ms         |
| http_write_timeout             | GUARDIAN_HTTP_WRITE_TIMEOUT             | Integer | 1000          | Write timeout value for external services in ms        |
| tenant_config_refresh_interval | GUARDIAN_TENANT_CONFIG_REFRESH_INTERVAL | Integer | 10            | Expiry time for tenant config in seconds               |
| http_client_keep_alive         | GUARDIAN_HTTP_CLIENT_KEEP_ALIVE         | Boolean | true          | Enable HTTP keep-alive for client connections          |
| http_client_keep_alive_timeout | GUARDIAN_HTTP_CLIENT_KEEP_ALIVE_TIMEOUT | Integer | 8000          | Keep-alive timeo_INTERVAL | Integer | 10            | Expiry time for tenant config in seconds               |
| http_client_keep_alive         | GUARDIAN_HTTP_CLIENT_KEEP_ALIVE         | Boolean | true          | Enable HTTP keep-alive for client connections          |
| http_client_keep_alive_timeout | GUARDIAN_HTTP_CLIENT_KEEP_ALIVE_TIMEOUT | Integer | 8000          | Keep-alive timeout for HTTP client connections in ms (must be > 1000) |
| http_client_idle_timeout       | GUARDIAN_HTTP_CLIENT_IDLE_TIMEOUT       | Integer | 6000          | Idle timeout for HTTP client connections in ms         |
| http_client_connection_pool_max_size | GUARDIAN_HTTP_CLIENT_CONNECTION_POOL_MAX_SIZE | Integer | 256            | Maximum size of HTTP client connection pool            |

## Tenant Configuration

### Tenant Onboarding

Before configuring any tenant-specific settings, you need to create a tenant in Guardian. The tenant is the top-level entity that groups all configurations for a specific organization or application.

#### Tenant Table Schema

| Field      | Type         | Description                          |
|------------|--------------|--------------------------------------|
| id         | CHAR(10)     | Unique tenant identifier (Primary Key) |
| name       | VARCHAR(256) | Human-readable tenant name            |
| created_at | TIMESTAMP    | Timestamp when tenant was created    |
| updated_at | TIMESTAMP    | Timestamp when tenant was last updated |

#### Creating a Tenant

To onboard a new tenant, insert a record into the `tenant` table:

```sql
INSERT INTO tenant (id, name) 
VALUES ('tenant1', 'My Application');
```

**Important Notes:**
- The `id` must be unique and exactly 10 characters (CHAR(10))
- The `name` must be unique across all tenants
- Once a tenant is created, you can configure all tenant-specific settings using the tenant `id`

### Client Configuration

OAuth 2.0 clients are applications that can authenticate users and access protected resources. Each client belongs to a tenant and must be configured before it can be used in authentication flows.

#### Client Table Schema

| Field          | Type         | Description                                                                 |
|----------------|--------------|-----------------------------------------------------------------------------|
| tenant_id      | CHAR(10)     | Tenant identifier (Part of Primary Key)                                    |
| client_id      | VARCHAR(100) | Unique client identifier (Part of Primary Key, auto-generated)              |
| client_name    | VARCHAR(100) | Human-readable client name (must be unique within tenant)                  |
| client_secret  | VARCHAR(100) | Client secret for authentication (auto-generated)                          |
| client_uri     | VARCHAR(2083)| URL of the client's home page                                               |
| contacts       | JSON         | Array of contact email addresses                                           |
| grant_types    | JSON         | OAuth 2.0 grant types supported (e.g., ["authorization_code", "refresh_token"]) |
| logo_uri       | VARCHAR(2083)| URL of the client's logo                                                   |
| policy_uri     | VARCHAR(2083)| URL of the client's privacy policy                                          |
| redirect_uris  | JSON         | Array of authorized redirect URIs                                          |
| response_types | JSON         | OAuth 2.0 response types supported (e.g., ["code"])                        |
| client_type    | CHAR(11)     | Client type: "first_party" or "third_party" (default: "third_party")      |
| is_default     | BOOLEAN      | Whether this is the default client for the tenant (default: false)         |
| created_at     | TIMESTAMP    | Timestamp when client was created                                          |
| updated_at     | TIMESTAMP    | Timestamp when client was last updated                                     |

#### Client Configuration via API

Guardian provides REST API endpoints for managing OAuth 2.0 clients. All client management endpoints require the `tenant-id` header.

##### Create Client

**Endpoint**: `POST /v1/admin/client`

**Headers**:
- `Content-Type: application/json`
- `tenant-id: <your-tenant-id>` (required)

**Request Body**:
```json
{
  "client_name": "My Application",
  "client_uri": "https://myapp.com",
  "contacts": ["admin@myapp.com", "support@myapp.com"],
  "grant_types": ["authorization_code", "refresh_token"],
  "logo_uri": "https://myapp.com/logo.png",
  "policy_uri": "https://myapp.com/privacy",
  "redirect_uris": ["https://myapp.com/callback", "https://myapp.com/silent-renew"],
  "response_types": ["code"],
  "client_type": "third_party",
  "is_default": false
}
```

**Request Parameters**:

| Parameter      | Type   | Required | Description                                                                 |
|----------------|--------|----------|-----------------------------------------------------------------------------|
| client_name    | string | Yes      | Human-readable name for the client (must be unique within tenant)         |
| grant_types    | array  | Yes      | OAuth 2.0 grant types: ["authorization_code", "client_credentials", "refresh_token"] |
| redirect_uris  | array  | Yes      | List of authorized redirect URIs (must be valid URIs)                       |
| response_types | array  | Yes      | OAuth 2.0 response types: ["code"]                                          |
| client_uri     | string | No       | URL of the client's home page                                               |
| contacts       | array  | No       | List of contact email addresses                                             |
| logo_uri       | string | No       | URL of the client's logo                                                    |
| policy_uri     | string | No       | URL of the client's privacy policy                                          |
| client_type    | string | No       | Client type: "first_party" or "third_party" (default: "third_party")      |
| is_default     | boolean| No       | Whether this is the default client (default: false)                        |

**Response**: `201 Created`
```json
{
  "client_id": "aB3dE5fG7hI9jK1lM",
  "client_name": "My Application",
  "client_secret": "xyz789abc123...",
  "client_uri": "https://myapp.com",
  "contacts": ["admin@myapp.com"],
  "grant_types": ["authorization_code", "refresh_token"],
  "logo_uri": "https://myapp.com/logo.png",
  "policy_uri": "https://myapp.com/privacy",
  "redirect_uris": ["https://myapp.com/callback"],
  "response_types": ["code"],
  "client_type": "third_party",
  "is_default": false
}
```

**Important Notes:**
- `client_id` and `client_secret` are automatically generated
- Store the `client_secret` securely as it cannot be retrieved later
- Redirect URIs are validated to prevent open redirect attacks
- Grant types and response types must be valid OAuth 2.0 values

## Tenant-Specific Configuration

After creating a tenant and client, you can configure the following tenant-specific settings:

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
| cookie_domain        | VARCHAR(256) | Domain for setting cookies                                                                 |
| cookie_path          | VARCHAR(256) | Path for setting cookies                                                                   |
| cookie_secure        | BOOLEAN      | Whether cookies should be secure (HTTPS only)                                              |
| cookie_http_only     | BOOLEAN      | Whether cookies should be HTTP-only (not accessible via JavaScript)                        |
| cookie_same_site     | VARCHAR(10)  | SameSite attribute for cookies (LAX, STRICT, NONE)                                         |

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

### Contact Verify Configuration

| Field             | Type    | Description                                                              |
|-------------------|---------|--------------------------------------------------------------------------|
| otpLength         | INT     | Length of the OTP code to be generated                                   |
| tryLimit          | INT     | Maximum number of verification attempts allowed                          |
| isOtpMocked       | BOOLEAN | Whether to use mock OTPs for testing purposes                            |
| resendLimit       | INT     | Maximum number of times OTP can be resent                                |
| otpResendInterval | INT     | Minimum time (in seconds) that must elapse before requesting another OTP |
| otpValidity       | INT     | Duration (in seconds) for which the OTP remains valid                    |
| whitelistedInputs | JSON    | Map of allowed input patterns for different channels                     |
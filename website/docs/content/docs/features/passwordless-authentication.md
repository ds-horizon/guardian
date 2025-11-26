---
title: Passwordless Authentication
description: Complete guide for implementing passwordless authentication using Guardian.
---


Complete guide for implementing passwordless authentication using Guardian's `/v2/passwordless` endpoint.


## Overview

Guardian implements passwordless authentication using OTP (One-Time Password) delivery via SMS or Email. This authentication method eliminates the need for users to remember passwords by sending a time-limited OTP code to their registered phone number or email address.

### How It Works

The passwordless authentication flow consists of two phases:

1.  **Init**: User initiates authentication by providing their phone number or email address → Guardian sends an OTP code

2.  **Complete**: User verifies the received OTP code → Guardian returns access tokens, refresh tokens, and ID tokens

**Supported Flows**:

*   `SIGNIN`: User must already exist in your user service. If user doesn't exist, authentication fails.

*   `SIGNUP`: User must not exist. If user already exists, authentication fails.

*   `SIGNINUP` (default): Works for both existing and new users. Automatically creates user if they don't exist.

## Prerequisites

Before implementing passwordless authentication, you need:

1.  **Guardian Tenant**: A tenant configured in Guardian. A tenant is an isolated configuration space that allows multiple organizations or applications to use the same Guardian instance. Contact your Guardian administrator to obtain your tenant ID.

2.  **OAuth Client**: A client created in Guardian (for `client_id`). The client ID is a unique identifier for your application within the tenant. You can create a client using Guardian's client management API or through your Guardian administrator.

3.  **User Service**: An external service that you must implement for user management. This service handles user lookup and creation. See [External Service Integration](#external-service-integration) for details.

4.  **OTP Service**: An external service that you must implement for SMS/Email delivery. This service sends OTP codes to users via SMS or email. See [External Service Integration](#external-service-integration) for details.

## Configuration

All configuration is tenant-specific and stored in database tables. You need to configure OTP settings and service endpoints before using passwordless authentication.

### Step 1: Configure OTP Settings

Configure OTP behavior in the `otp_config` table:

```sql
INSERT INTO otp_config (tenant_id, is_otp_mocked, otp_length, try_limit, resend_limit, otp_resend_interval, otp_validity, whitelisted_inputs)
VALUES ('tenant1', false, 6, 5, 5, 30, 900, '{}');
```

**Table Schema**:

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `tenant_id` | CHAR(10) | | Your tenant identifier |
| `is_otp_mocked` | BOOLEAN | FALSE | Returns static OTP `999999` if true (useful for testing) |
| `otp_length` | INT | 6 | OTP code length |
| `try_limit` | INT | 5 | Maximum OTP verification attempts |
| `resend_limit` | INT | 5 | Maximum resend attempts (excluding initial send) |
| `otp_resend_interval` | INT | 30 | Minimum seconds between resends |
| `otp_validity` | INT | 900 | OTP validity in seconds (15 minutes) |
| `whitelisted_inputs` | JSON | {} | Map of identifier → OTP for testing (e.g., `{"9999999999": "123456"}`) |

### Step 2: Configure SMS Service

If you want to send OTP via SMS, configure the SMS service in the `sms_config` table:

```sql
INSERT INTO sms_config (tenant_id, host, port, is_ssl_enabled, send_sms_path, template_name, template_params)
VALUES ('tenant1', 'sms-service.example.com', 443, true, '/api/v1/send-sms', 'otp_template', '{"app_name": "My App"}');
```

**Table Schema**:

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `tenant_id` | CHAR(10) | | Your tenant identifier |
| `host` | VARCHAR(256) | | SMS service hostname |
| `port` | INT | | SMS service port |
| `is_ssl_enabled` | BOOLEAN | FALSE | Enable SSL/TLS for SMS service |
| `send_sms_path` | VARCHAR(256) | | API path for sending SMS |
| `template_name` | VARCHAR(256) | | Default SMS template name |
| `template_params` | JSON | {} | Default template parameters |

### Step 3: Configure Email Service

If you want to send OTP via Email, configure the email service in the `email_config` table:

```sql
INSERT INTO email_config (tenant_id, host, port, is_ssl_enabled, send_email_path, template_name, template_params)
VALUES ('tenant1', 'email-service.example.com', 443, true, '/api/v1/send-email', 'otp_template', '{"app_name": "My App"}');
```

**Table Schema**:

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `tenant_id` | CHAR(10) | | Your tenant identifier |
| `host` | VARCHAR(256) | | Email service hostname |
| `port` | INT | 80 | Email service port |
| `is_ssl_enabled` | BOOLEAN | FALSE | Enable SSL/TLS for email service |
| `send_email_path` | VARCHAR(256) | | API path for sending email |
| `template_name` | VARCHAR(256) | | Default email template name |
| `template_params` | JSON | {} | Default template parameters |

### Step 4: Configure User Service

You must also configure your user service. See the [Username/Password Authentication guide](docs/features/username-password-authentication) for user service configuration details.

## API Endpoints

### Init Endpoint

**Endpoint**: `POST /v2/passwordless/init`

**Purpose**: Initiates the passwordless authentication flow and sends an OTP to the user

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)

**Request Body**:

```json
{
  "client_id": "my-client-id",
  "scopes": ["openid", "email"],
  "flow": "signinup",
  "response_type": "token",
  "contacts": [{
    "channel": "sms",
    "identifier": "9999999999",
    "template": {
      "name": "templateName",
      "params": {
        "variable-1": "value-1"
      }
    }
  }],
  "state": "string",
  "meta_info": {
    "ip": "string",
    "location": "string",
    "device_name": "string",
    "source": "string"
  }
}
```

**Request Parameters**:

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| client_id | string | Yes | Guardian OAuth client ID. This must be created in Guardian before use. |
| scopes | array[string] | No | OAuth scopes to request (e.g., ["openid", "email", "profile"]) |
| flow | string | No | Authentication flow type: "signin", "signup", or "signinup" (default: "signinup") |
| response_type | string | No | Response type: "token" or "code" (default: "token") |
| contacts | array[object] | Yes | Array of contact objects for OTP delivery |
| contacts[].channel | string | Yes | Contact channel: "sms" or "email" |
| contacts[].identifier | string | Yes | Phone number (SMS) or email address (email) |
| contacts[].template | object | No | Template override (uses tenant default if not provided) |
| contacts[].template.name | string | No | Template name |
| contacts[].template.params | object | No | Template parameters as key-value pairs |
| state | string | No | State key from previous `/init` response. Required only when resending OTP. Omit for initial request |
| meta_info | object | No | Metadata object |
| meta_info.ip | string | No | Client IP address |
| meta_info.location | string | No | Geographic location |
| meta_info.device_name | string | No | Device identifier |
| meta_info.source | string | No | Application source (e.g., "mobile_app", "web") |

**Response**: `200 OK`

```json
{
  "state": "aB3dE5fG7h",
  "tries": 0,
  "retries_left": 5,
  "resends": 0,
  "resends_left": 5,
  "resend_after": 1640995230,
  "is_new_user": true
}
```

**Response Parameters**:

| Parameter | Type | Description |
| --- | --- | --- |
| state | string | Unique state for subsequent requests. Store this for the complete endpoint. |
| tries | integer | Number of OTP verification attempts made |
| retries_left | integer | Remaining verification attempts |
| resends | integer | Number of times OTP has been resent |
| resends_left | integer | Remaining resend attempts |
| resend_after | integer | Unix timestamp after which resend is allowed |
| is_new_user | boolean | Boolean indicating whether the user was created during this flow |

**Error Responses**:

| Code | Status | Message | When | Resolution |
| --- | --- | --- | --- | --- |
| invalid_state | 400 | "Invalid state" | State not found/expired | Start new flow |
| resends_exhausted | 400 | "Resends exhausted" | resends >= maxResends | Start new flow |
| resends_not_allowed | 400 | "Resend triggered too quick" | Resend before interval | Wait until resendAfter |

### Complete Endpoint

**Endpoint**: `POST /v2/passwordless/complete`

**Purpose**: Verifies OTP and completes authentication

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)

**Request Body**:

```json
{
  "state": "aB3dE5fG7h",
  "otp": "123456"
}
```

**Request Parameters**:

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| state | string | Yes | State key from init response |
| otp | string | Yes | OTP code received by user |

**Response**: `200 OK`

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "aB3dE5fG7hI9jK1lM3nO5pQ7rS9tU1v",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "sso_token": "aB3dE5fG7hI9jK",
  "token_type": "Bearer",
  "expires_in": 900,
  "is_new_user": false
}
```

**Response Parameters**:

| Parameter | Type | Description |
| --- | --- | --- |
| access_token | string | JWT access token for API authentication |
| refresh_token | string | 32-character alphanumeric refresh token for obtaining new access tokens |
| id_token | string | JWT ID token containing user information (if OIDC enabled) |
| sso_token | string | 15-character alphanumeric Single Sign-On token |
| token_type | string | Token type. Always "Bearer" |
| expires_in | integer | Access token expiration time in seconds |
| is_new_user | boolean | Boolean indicating if user was created in this flow |

**Error Responses**:

| Code | Status | Message | When | Resolution |
| --- | --- | --- | --- | --- |
| invalid_state | 400 | "Invalid state" | State not found/expired | Start new flow |
| incorrect_otp | 400 | "Incorrect otp" | OTP mismatch | Retry (check metadata.otp_retries_left) |
| retries_exhausted | 400 | "Retries exhausted" | tries >= maxTries | Start new flow |
| flow_blocked | 403 | "API is blocked for this userIdentifier" | User identifier is blocked | Check error response metadata.retryAfter for unblock time |

**Error Response Example** (for flow_blocked):

```json
{
  "error": "flow_blocked",
  "error_description": "API is blocked for this userIdentifier",
  "metadata": {
    "retryAfter": 1640995230
  }
}
```

## API Specification

### Request Schema

#### V2PasswordlessInitRequestBody

```yaml
type: object
required:
  - client_id
  - contacts

properties:
  client_id:
    type: string
    description: Guardian OAuth client ID
    example: "my-client-id"

  scopes:
    type: array
    items:
      type: string
    description: OAuth scopes to request
    example: ["openid", "email", "profile"]

  flow:
    type: string
    enum: ["signin", "signup", "signinup"]
    default: "signinup"
    description: Authentication flow type

  response_type:
    type: string
    enum: ["token", "code"]
    default: "token"
    description: Desired response type

  contacts:
    type: array
    items:
      type: object
      required:
        - channel
        - identifier
      properties:
        channel:
          type: string
          enum: ["sms", "email"]
          description: Contact channel
        identifier:
          type: string
          description: Phone number (SMS) or email address (email)
        template:
          type: object
          properties:
            name:
              type: string
              description: Template name
            params:
              type: object
              description: Template parameters as key-value pairs

  state:
    type: string
    description: State key from previous request (for OTP resend)

  meta_info:
    type: object
    properties:
      ip:
        type: string
        description: Client IP address
      location:
        type: string
        description: Geographic location
      device_name:
        type: string
        description: Device identifier
      source:
        type: string
        description: Application source
```

#### V2PasswordlessCompleteRequestBody

```yaml
type: object
required:
  - state
  - otp

properties:
  state:
    type: string
    description: State key from init response
    example: "aB3dE5fG7h"

  otp:
    type: string
    description: OTP code received by user
    example: "123456"
```

### Response Schema

#### PasswordlessInitResponse

```yaml
type: object
properties:
  state:
    type: string
    description: Unique state for subsequent requests

  tries:
    type: integer
    description: Number of OTP verification attempts made

  retries_left:
    type: integer
    description: Remaining verification attempts

  resends:
    type: integer
    description: Number of times OTP has been resent

  resends_left:
    type: integer
    description: Remaining resend attempts

  resend_after:
    type: integer
    description: Unix timestamp after which resend is allowed

  is_new_user:
    type: boolean
    description: Whether the user was created during this flow
```

#### TokenResponse

```yaml
type: object
properties:
  access_token:
    type: string
    description: JWT access token for API authentication

  refresh_token:
    type: string
    description: Opaque refresh token for obtaining new access tokens

  id_token:
    type: string
    description: OpenID Connect ID token containing user information

  sso_token:
    type: string
    description: Single Sign-On token for cross-application authentication

  token_type:
    type: string
    example: "Bearer"

  expires_in:
    type: integer
    description: Access token expiration time in seconds

  is_new_user:
    type: boolean
    description: Whether this is a newly created user
```

## Examples

### cURL Examples

**Init Request**:

```bash
curl --location 'http://localhost:8080/v2/passwordless/init' \
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \
--data '{
    "client_id": "my-client-id",
    "scopes": ["openid", "email"],
    "flow": "signinup",
    "response_type": "token",
    "contacts": [{
        "channel": "sms",
        "identifier": "9999999999"
    }],
    "meta_info": {
        "ip": "127.0.0.1",
        "location": "localhost",
        "device_name": "Chrome Browser",
        "source": "web"
    }
}'
```

**Complete Request**:

```bash
curl --location 'http://localhost:8080/v2/passwordless/complete' \
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \
--data '{
    "state": "aB3dE5fG7h",
    "otp": "123456"
}'
```

**Resend OTP** (using state from previous init response):

```bash
curl --location 'http://localhost:8080/v2/passwordless/init' \
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \
--data '{
    "state": "aB3dE5fG7h"
}'
```

## Flow Diagram

### Complete Passwordless Authentication Flow

```text
┌─────────┐                    ┌──────────┐                    ┌─────────────┐                    ┌──────────┐
│ Client  │                    │ Guardian │                    │ User Service│                    │OTP Service│
│         │                    │          │                    │             │                    │           │
└────┬────┘                    └────┬─────┘                    └────┬────────┘                    └────┬──────┘
     │                              │                               │                                   │
     │ 1. POST /v2/passwordless/init│                               │                                   │
     │─────────────────────────────>│                               │                                   │
     │ {client_id, contacts, ...}   │                               │                                   │
     │                              │                               │                                   │
     │                              │ 2. GET /user?email=...        │                                   │
     │                              │    OR ?phoneNumber=...        │                                   │
     │                              │──────────────────────────────>│                                   │
     │                              │                               │                                   │
     │                              │ 3. User lookup response       │                                   │
     │                              │<──────────────────────────────│                                   │
     │                              │                               │                                   │
     │                              │ 4. Generate OTP               │                                   │
     │                              │                               │                                   │
     │                              │ 5. POST /sendSms or /sendEmail│                                   │
     │                              │──────────────────────────────────────────────────────────────────>│
     │                              │                               │                                   │
     │                              │ 6. OTP sent confirmation      │                                   │
     │                              │<──────────────────────────────────────────────────────────────────│
     │                              │                               │                                   │
     │                              │ 7. Store state in Redis       │                                   │
     │                              │                               │                                   │
     │ 8. Return state & metadata  │                                │                                   │
     │<─────────────────────────────│                               │                                   │
     │ {state, tries, resends, ...} │                               │                                   │
     │                              │                               │                                   │
     │                              │                               │                                   │
     │ 9. POST /v2/passwordless/    │                               │                                   │
     │    complete                  │                               │                                   │
     │─────────────────────────────>│                               │                                   │
     │ {state, otp}                 │                               │                                   │
     │                              │                               │                                   │
     │                              │ 10. Get state from Redis      │                                   │
     │                              │                               │                                   │
     │                              │ 11. Verify OTP                │                                   │
     │                              │                               │                                   │
     │                              │ 12. POST /user (if new user)  │                                   │
     │                              │──────────────────────────────>│                                   │
     │                              │                               │                                   │
     │                              │ 13. User created response     │                                   │
     │                              │<──────────────────────────────│                                   │
     │                              │                               │                                   │
     │                              │ 14. Generate & store tokens   │                                   │
     │                              │                               │                                   │
     │ 15. Return tokens            │                               │                                   │
     │<─────────────────────────────│                               │                                   │
     │ {access_token, refresh_token}│                               │                                   │
     │                              │                               │                                   │
```

## External Service Integration

Guardian requires two external services that you must implement: User Service and OTP Service. These services are called by Guardian during the authentication flow.

### User Service

Your User Service must implement endpoints for user lookup and creation. Guardian calls these endpoints to manage users during authentication.

**Required Endpoints**:

#### GET /user (User Lookup)

Guardian calls this endpoint to check if a user exists.

**Request**:

```text
GET {user_service_host}:{port}{get_user_path}?email={email}
OR
GET {user_service_host}:{port}{get_user_path}?phoneNumber={phone}
```

**Response** (User exists):

```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "9999999999"
}
```

**Response** (User does not exist):

```json
{
  "userId": null
}
```

**Called When**: Guardian needs to check if a user exists before sending OTP

#### POST /user (User Creation)

Guardian calls this endpoint to create a new user when the flow is `SIGNUP` or `SIGNINUP` and the user doesn't exist.

**Request**:

```text
POST {user_service_host}:{port}{create_user_path}
Content-Type: application/json

{
  "email": "user@example.com",
  "phoneNumber": "9999999999",
  "additionalInfo": {}
}
```

**Response**:

```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "9999999999"
}
```

**Called When**: User doesn't exist and flow is `SIGNUP` or `SIGNINUP`

**Error Handling**:

*   If user service returns an error, Guardian will return a 500 error to the client
*   Ensure your user service is accessible and returns responses within Guardian's timeout limits
*   User service should handle duplicate user creation gracefully

### OTP Service

Your OTP Service must implement endpoints for sending OTP codes via SMS or Email. Guardian calls these endpoints to deliver OTP codes to users.

**Required Endpoints**:

#### POST /sendSms (Send SMS OTP)

Guardian calls this endpoint to send OTP via SMS.

**Request**:

```text
POST {sms_service_host}:{port}{send_sms_path}
Content-Type: application/json

{
  "channel": "sms",
  "to": "9999999999",
  "template_name": "otp_template",
  "template_params": {
    "otp": "123456",
    "app_name": "My App"
  }
}
```

**Response** (Success):

```json
{
  "success": true,
  "messageId": "msg123"
}
```

**Response** (Error):

```json
{
  "success": false,
  "error": "Invalid phone number"
}
```

**Called When**: Guardian needs to send OTP via SMS

#### POST /sendEmail (Send Email OTP)

Guardian calls this endpoint to send OTP via Email.

**Request**:

```text
POST {email_service_host}:{port}{send_email_path}
Content-Type: application/json

{
  "channel": "email",
  "to": "user@example.com",
  "template_name": "otp_template",
  "template_params": {
    "otp": "123456",
    "app_name": "My App"
  }
}
```

**Response** (Success):

```json
{
  "success": true,
  "messageId": "msg123"
}
```

**Response** (Error):

```json
{
  "success": false,
  "error": "Invalid email address"
}
```

**Called When**: Guardian needs to send OTP via Email

**Error Handling**:

*   If OTP service returns an error, Guardian will return a 500 error to the client
*   Ensure your OTP service is accessible and returns responses within Guardian's timeout limits
*   OTP service should handle invalid phone numbers/email addresses gracefully

**Authentication**:

*   If your OTP service requires authentication, configure it in Guardian's service configuration
*   Guardian supports basic authentication and custom headers for service authentication

## Rate Limiting

Guardian enforces rate limiting to prevent abuse and ensure security:

| Limit | Default | Enforcement | Action on Exhaustion |
| --- | --- | --- | --- |
| OTP Verification | 5 attempts | Per state | State deleted, flow terminated |
| OTP Resend | 5 attempts | Per state | State deleted, flow terminated |
| Resend Interval | 30 seconds | Time-based | resendAfter = currentTime + interval |
| OTP Validity | 900 seconds (15 min) | Redis TTL | State auto-deleted on expiry |

**Important Notes**:

*   Rate limits are enforced per authentication state
*   Once limits are exhausted, users must start a new authentication flow
*   OTP state expires after 15 minutes (default), after which a new OTP must be requested
*   Resend interval prevents users from requesting too many OTPs in a short time

## Troubleshooting

### "Invalid state" Error

**Problem**: Request fails with "Invalid state" error

**Possible Causes**:

*   State has expired (default: 15 minutes)
*   State was already used
*   State doesn't exist

**Solutions**:

*   Start a new authentication flow by calling `/v2/passwordless/init` again
*   Store the state securely and use it within the validity period
*   Don't reuse states - each complete request should use a fresh state from init

### "OTP not received" Issue

**Problem**: User doesn't receive OTP code

**Possible Causes**:

*   Email/Sms service is not configured correctly
*   Email/Sms service is unavailable or timing out
*   Invalid phone number or email address
*   SMS/Email delivery issues

**Solutions**:

*   Verify Email/Sms service configuration in `sms_config` or `email_config` table
*   Check Email/Sms service logs for delivery errors
*   Test Email/Sms service endpoints directly
*   Verify phone number/email format is correct
*   Check if Email/Sms service provider has delivery issues
*   Use `is_otp_mocked: true` for testing (returns static OTP `999999`)

### "Incorrect OTP" Error

**Problem**: OTP verification fails even with correct code

**Possible Causes**:

*   OTP has expired
*   OTP was already used
*   Wrong OTP entered
*   Case sensitivity issues

**Solutions**:

*   Request a new OTP if the current one has expired
*   Ensure OTP is entered correctly (check for typos)
*   OTP codes are case-sensitive - enter exactly as received
*   Check `retries_left` in error response to see remaining attempts

### "Retries exhausted" Error

**Problem**: Maximum OTP verification attempts reached

**Solutions**:

*   Start a new authentication flow by calling `/v2/passwordless/init` again
*   Inform user that they've exceeded maximum attempts
*   Consider implementing account lockout for security

### "Resends exhausted" Error

**Problem**: Maximum OTP resend attempts reached

**Solutions**:

*   Start a new authentication flow by calling `/v2/passwordless/init` again
*   Wait for the resend interval before requesting again
*   Check `resend_after` timestamp in response to know when resend is allowed

### "User service error"

**Problem**: Guardian cannot communicate with user service

**Solutions**:

*   Check user service is accessible and running
*   Verify user service configuration in `user_config` table
*   Check network connectivity between Guardian and user service
*   Verify SSL settings if using HTTPS
*   Check user service logs for errors
*   Ensure user service endpoints return correct response format

### "OTP service error"

**Problem**: Guardian cannot communicate with Email/Sms service

**Solutions**:

*   Check Email/Sms service is accessible and running
*   Verify Email/Sms service configuration in `email_config` or `sms_config` table
*   Check network connectivity between Guardian and Email/Sms service
*   Verify SSL settings if using HTTPS
*   Check Email/Sms service logs for errors
*   Test Email/Sms service endpoints directly

### "Flow blocked" Error

**Problem**: Authentication is blocked for user identifier

**Solutions**:

*   Check error response metadata for `retryAfter` timestamp
*   Wait until the retry time before attempting again
*   This is a security feature to prevent abuse
*   Contact Guardian administrator if blocking persists

## Best Practices

1.  Set appropriate OTP validity period (default: 15 minutes)

2.  Enforce rate limiting to prevent brute force attacks

3.  Use HTTPS for all API calls in production

4.  Store state securely on client side

5.  Don't expose state in URLs or logs

6.  Use state only once - don't reuse states

7.  Store tokens securely (HttpOnly cookies or secure storage)

8.  Never expose tokens in client-side code or URLs

9.  Implement token refresh mechanism

10. Clear tokens on logout

11. Use HTTPS for user service and Email/Sms service

## Related Documentation

*   [Post-Authentication](docs/features/post-authentication) - Session management and token refresh
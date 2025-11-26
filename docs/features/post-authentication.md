# Post-Authentication: Session Management and Logout

This guide covers everything you need to know about managing user sessions after authentication, including token management and logout functionality.

## Table of Contents


*   [Overview](#overview)

*   [Understanding Tokens](#understanding-tokens)

*   [Tokens Issued by Authentication Method](#tokens-issued-by-authentication-method)

*   [Session Management](#session-management)

*   [API Endpoints](#api-endpoints)

*   [Token Refresh](#token-refresh)

*   [Logout](#logout)

*   [Token Revocation](#token-revocation)

*   [Configuration](#configuration)

*   [Examples](#examples)

*   [Best Practices](#best-practices)

*   [Troubleshooting](#troubleshooting)

## Overview


After a user successfully authenticates, Guardian provides comprehensive session management capabilities:

*   **Token Management**: Provides access tokens, refresh tokens, ID tokens, and SSO tokens on successful authentication

*   **Token Refresh**: Get new access tokens using refresh tokens

*   **Single Sign-On (SSO)**: Cross-application authentication using SSO tokens

*   **Logout**: Single device logout, client-wide logout, and tenant-wide logout

*   **Token Revocation**: Immediate token invalidation list

*   **Session Persistence**: Secure session storage


## Understanding Tokens


Guardian issues four types of tokens after successful authentication:

### 1\. Access Token


**Type**: JWT (JSON Web Token), stateless  
**Algorithm**: RS256 (RSA Signature with SHA-256) and RS512 are supported. 
**Purpose**: An access token is a credential used by an application to authorize access to an API and define the permitted actions (scope).  
**Validity**: Configurable (default: 1 day)

**Structure**:

**Header**:

```json
{ 
  "alg": "RS256",
  "typ": "at+jwt", 
  "kid": "key1"
}
```

**JWT Payload**:

```json
{ 
  "sub": "user123",
  "aud": "client123",
  "iss": "https://guardian.example.com",
  "exp": 1642096800, 
  "iat": 1642093200,
  "scope": "read write",
  "client_id": "client123",
  "tid": "tenant1", 
  "jti": "unique_token_id",
  "rft_id": "refresh_token_id",
  "amr": ["otp"]
}
```
**Claims**:

*   `sub`: User identifier

*   `aud`: Audience (client ID)

*   `iss`: Issuer (Guardian issuer URL)

*   `exp`: Expiration time (Unix timestamp)

*   `iat`: Issued at time (Unix timestamp)

*   `scope`: Space-separated list of granted scopes

*   `client_id`: OAuth client ID

*   `tid`: Tenant ID

*   `jti`: Unique token identifier

*   `rft_id`: Refresh token ID (links to refresh token)

*   `amr`: Authentication methods used (array)


### 2\. Refresh Token


**Type**: Opaque string (random alphanumeric, 32 characters)  
**Purpose**: A refresh token is a long-lived credential that allows an application to securely mint new access tokens, allowing continuous access without re-authenticating.  
**Validity**: Configurable (default: 6 months)  
**Storage**: Database

**Characteristics**:

*   **Opaque**: Not a JWT, cannot be decoded

*   **Persistent**: Remains the same across refresh calls (not rotated)

*   **Revocable**: Can be immediately invalidated

*   **Device-Linked**: Associated with specific device/session

*   **Storable**: Can be stored in your application (secure storage recommended)


**Usage**: Get a new access token from the refresh token using `/v2/refresh-token` endpoint

**Storage Recommendations**:

*   **Web**: Use HttpOnly cookies (preferred) or secure localStorage

*   **Mobile**: Use secure keychain/keystore

*   **Never**: Expose in URLs, logs, or client-side JavaScript (if using cookies)


### 3\. ID Token


**Type**: JWT (JSON Web Token)  
**Algorithm**: RS256 & RS512 supported  
**Purpose**: A parsable token containing user-specific data, intended for client applications.  
**Validity**: Configurable (default: 1 day)

**Structure**:

**Header**:

```json
{ 
  "alg": "RS256",
  "kid": "key1" 
 }
 ```

**Payload**:

```json
{ 
  "sub": "user123",
  "aud": "client123",
  "iss": "https://guardian.example.com", 
  "exp": 1642096800, 
  "iat": 1642093200, 
  "email": "user@example.com",
  "email_verified": true, 
  "phone_number": "+1234567890",
  "phone_number_verified": true,
  "name": "John Doe"
}
```

**Claims**: Configured via `id_token_claims` in token configuration. Common claims:

*   `sub`: User identifier (required)

*   `email`: User email

*   `phone_number`: User phone number

*   `name`: User's full name


**Usage**: Client-side user identification, OpenID Connect flows

### 4\. SSO Token


**Type**: Opaque string (alphanumeric, 15 characters)  
**Purpose**: Single Sign-On token for cross-application authentication  
**Usage**: Used in `/login-accept` endpoint to get access and refresh tokens for a different application  
**Validity**: Configurable (typically matches refresh token expiry)  
**Storage**: Database

**Characteristics**:

*   **Opaque**: Not a JWT, cannot be decoded

*   **Cross-Application**: Used to authenticate users across different applications

*   **Short**: 15 characters

*   **Multi-Client**: Can be used to get tokens for multiple applications

*   **Session-Linked**: Associated with user session


**SSO Flow**:

1.  User logs into App A

2.  User receives SSO token along with access/refresh tokens

3.  User wants to login to App B

4.  User sends SSO token to `/login-accept` endpoint with `login_challenge`

5.  Guardian validates SSO token and returns access/refresh tokens for App B


**Example Flow**:

```text
User logs into App A 
      ↓ 
Receives: access_token, refresh_token, id_token, sso_token
      ↓ 
User wants to login to App B 
      ↓ 
Sends SSO token to /login-accept 
      ↓ 
Receives: access_token, refresh_token, id_token for App B
```

## Tokens Issued by Authentication Method

### Regular Authentication (Passwordless, Username/Password, Social)


When users authenticate using passwordless, username/password, or social authentication methods, Guardian issues:

*   ✅ **Access Token**: JWT for API access

*   ✅ **Refresh Token**: Opaque token for refreshing access tokens

*   ✅ **ID Token**: JWT containing user information (OpenID Connect)

*   ✅ **SSO Token**: Opaque token for cross-application Single Sign-On


**Response Format**:

```json
{ 
  "access_token": "eyJhbGci...",
  "refresh_token": "xyz789abc123",
  "id_token": "eyJhbGci...",
  "sso_token": "abc123def456", 
  "token_type": "Bearer", 
  "expires_in": 3600, 
  "is_new_user": false
}
```
### Guest Authentication


When users authenticate as guests, Guardian issues:

*   ✅ **Access Token**: JWT for API access (limited scopes)

*   ❌ **Refresh Token**: Not issued

*   ❌ **ID Token**: Not issued

*   ❌ **SSO Token**: Not issued (SSO not available for guest users)


**Response Format**:

```json
{ "access_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```
**Note**: Guest tokens typically have shorter expiry times. Users must call the same /v1/guest/login endpoint again to refresh token.

## Session Management

### Session Lifecycle


```text
Authentication 
       ↓ 
Tokens Issued (Access, Refresh, ID) 
       ↓ 
Session Created (stored in Redis) 
       ↓ 
Access Token Used (for API calls) 
       ↓ 
Access Token Expires 
       ↓ 
Refresh Token Used (to get new access token) 
       ↓ 
New Access Token Issued (refresh token remains same) 
       ↓ 
Session Continues or Logout
```

### Single Sign-On (SSO) Flow


SSO tokens enable users to authenticate across multiple applications:

```text
User logs into App A (Dream11) 
      ↓ 
Receives: access_token, refresh_token, id_token, sso_token 
      ↓ 
User navigates to App B 
      ↓ 
App B initiates OAuth flow → /authorize endpoint 
      ↓ 
User redirected to login page 
      ↓ 
User sends SSO token to /login-accept 
      ↓ 
Guardian validates SSO token 
      ↓ 
Guardian returns access_token, refresh_token, id_token for App B 
      ↓ 
User is logged into App B without re-authenticating
```
### Multi-Device Sessions

Guardian tracks sessions per device:

*   **Device Identification**: Based on `device_name` from `meta_info`

*   **Multiple Sessions**: Users can have multiple active sessions

*   **Session Isolation**: Each device has independent session

## API Endpoints

### Refresh Token


**Endpoint**: `POST /v2/refresh-token`

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)


**Request Body**:

```json
{ 
"refresh_token": "xyz789abc123", 
"client_id": "aB3dE5fG7hI9jK1lM" 
}
```

**Required Fields**:

*   `refresh_token` (string): Refresh token to use

**Optional Fields**
*   `client_id` (string): validates against client id if provided


**Response**: `200 OK`

```json
{ 
"access_token": "eyJhbGci...", 
"token_type": "Bearer", 
"expires_in": 3600 
}
```

**Important**:

*   The refresh token **remains the same** (not rotated)

*   You can continue using the same refresh token for future refresh calls

*   Access token is also set as cookie (if using cookies)


**Error Responses**:

*   `400 Bad Request`: Missing or invalid refresh token

*   `401 Unauthorized`: Refresh token expired or invalid

*   `500 Internal Server Error`: Server error


### Logout


**Endpoint**: `POST /v2/logout`

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)


**Request Body**:

```json
{ 
"refresh_token": "xyz789abc123", 
"logout_type": "token",
"client_id": "aB3dE5fG7hI9jK1lM"
}
```

**Request Parameters** :

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| refresh_token | string | Yes | Refresh token to invalidate. Can also be provided via RT cookie |
| logout_type | string | No | Logout scope. Options: "token" (invalidate only this refresh token, default), "client" (invalidate all refresh tokens for this user on this client), "tenant" (invalidate all refresh tokens for this user across all clients) |
| client_id | string | No | Guardian OAuth client ID. Required if logout_type is "client" |

**Note**: Refresh token can also be provided via cookie (`RT` cookie name)

**Response**: `204 No Content`

**Cookies Cleared**:

*   Access Token cookie (`AT`)

*   Refresh Token cookie (`RT`)

*   SSO Token cookie (`SSO`)


**Error Responses**:

*   `400 Bad Request`: Missing or invalid refresh token

*   `401 Unauthorized`: Invalid refresh token

*   `500 Internal Server Error`: Server error


### Revocation List


**Use Case**: Validate tokens in distributed systems by checking revocation list  
**Endpoint**: `GET /revocations?from=<timestamp>`

**Headers**:

*   `tenant-id: <your-tenant-id>` (required)


**Query Parameters**:

*   `from` (integer, optional): Start timestamp (epoch seconds). Defaults to current time minus access token expiry


**Response**: `200 OK`

```json
{
  "revoked_tokens": [
    "token1",
    "token2",
    "token3"
  ],
  "time_range": {
    "from": 1640995200,
    "to": 1641081600
  },
  "access_token_expiry": 3600
}
```

## Token Refresh

### How Token Refresh Works


1.  **Client sends refresh token** to `/v2/refresh-token`

2.  **Guardian validates** refresh token

3.  **Guardian generates** new access token

4.  **Guardian returns** new access token

5.  **Refresh token remains the same** - you can continue using it for future refreshes


### Refresh Token Behavior


*   **Not Rotated**: The refresh token remains the same across refresh calls

*   **Reusable**: You can use the same refresh token multiple times until it expires

*   **Storable**: Store the refresh token securely in your application

*   **Long-Lived**: Refresh tokens have longer expiry than access tokens


### When to Refresh


*   **Before Expiry**: Refresh access token before it expires

*   **Reactively (on failure)**: If an API call returns an HTTP 401 Unauthorized error, use the refresh token to get a new access token

*   **Proactively**: Refresh 5 minutes before access token expiry


### Refresh Token Storage


**Recommended Storage**:

*   **Web Applications**: Use HttpOnly cookies (set by Guardian) or secure localStorage

*   **Mobile Applications**: Use secure keychain/keystore

*   **Desktop Applications**: Use secure credential store


**Security Best Practices**:

*   Never expose refresh tokens in URLs

*   Never log refresh tokens

*   Use HTTPS for all token transmission

*   Store securely with appropriate encryption


## Logout

### Logout Types


Guardian supports three types of logout:

#### 1\. Token Logout (Default)


**Type**: `"token"`  
**Scope**: Invalidates only the provided refresh token  
**Use Case**: User logs out from current device

```json
{
    "refresh_token": "xyz789abc123",
    "logout_type": "token"
}
```

#### 2\. Client Logout


**Type**: `"client"`  
**Scope**: Invalidates all refresh tokens for this user on this client  
**Use Case**: User logs out from all devices using this application

```json
{
  "refresh_token": "xyz789abc123",
  "logout_type": "client",
  "client_id": "aB3dE5fG7hI9jK1lM"
}
```
#### 3\. Tenant Logout


**Type**: `"tenant"`  
**Scope**: Invalidates all refresh tokens for this user across all clients  
**Use Case**: Complete account logout from all applications

```json
{ 
  "refresh_token": "xyz789abc123", 
  "logout_type": "tenant"
}
```

### Logout Flow

```text
User Initiates Logout 
        ↓ 
Client sends refresh_token to /v2/logout 
        ↓ 
Guardian validates refresh_token 
        ↓ 
Guardian determines logout scope (token/client/tenant) 
        ↓ 
Guardian invalidates refresh token(s) 
        ↓ 
Guardian clears cookies (AT, RT, SSO) 
        ↓ 
Client clears local storage 
        ↓ 
Client redirects to login
```

## Token Revocation

### When to Revoke Tokens


*   **Security breach**: Suspected token compromise

*   **User request**: User wants to invalidate specific token

*   **Account changes**: Password change, security settings update

*   **Application uninstall**: User removes application


### How Token Revocation Works


Guardian does not expose a public API endpoint for revoking tokens. Token revocation happens internally when:

*   **User logout**: Refresh tokens are invalidated when users log out

*   **Admin operations**: Administrators can perform logout operations


When refresh tokens are invalidated, their associated `rft_id` claim in access\_token are added to a revocation list. Access tokens remain valid until their natural expiry, but can be checked against the revocation list for validation.

### Checking Revocation List


Guardian provides a revocation list endpoint that clients and API gateways can use to check if refresh tokens have been revoked.

**Purpose**: This API returns a list of revoked refresh token IDs (`rft_id`). To invalidate requests, applications must check if the `rft_id` embedded in the current access token is present in this revocation list.  
**Usage**: Distributed systems and API gateways can check the revocation list to validate access tokens before allowing API access.

**Implementation Pattern**:

1.  **Periodic Sync**: API gateway periodically fetches the revocation list

2.  **Token Validation**: When an access token is presented, check if it's in the revocation list

3.  **Reject Revoked Tokens**: Deny access if token is found in the revocation list


**Endpoint**: `GET /revocations?from=<timestamp>`

**Headers**:

*   `tenant-id: <your-tenant-id>` (required)


**Query Parameters**:

*   `from` (integer, optional): The start time (epoch seconds) for listing revoked refresh tokens. If omitted, it defaults to the current time minus the access token's standard expiry duration.


| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| from | integer | No(Defaults to current time minus access token expiry) | The start time (epoch seconds) for listing revoked refresh tokens. |

**Response**: `200 OK`

```json
{
    "revoked_tokens": [
        "token1",
        "token2",
        "token3"
    ],
    "time_range": {
        "from": 1640995200,
        "to": 1641081600
    },
    "access_token_expiry": 3600
}
```

**Response Parameters**:

| Parameter | Type | Description |
| --- | --- | --- |
| revoked_tokens | array | List of revoked token identifiers (JTI values from access tokens) |
| time_range | object | Time range used for the query |
| time_range.from | integer | Start timestamp (epoch seconds) |
| time_range.to | integer | End timestamp (epoch seconds) |
| access_token_expiry | integer | Access token expiry duration in seconds (for reference) |

## Configuration

### Token Expiry Configuration


Configure token lifetimes in `token_config`:

```text
UPDATE token_config SET access_token_expiry = 3600, -- 1 hour refresh_token_expiry = 86400, -- 24 hours id_token_expiry = 3600 -- 1 hour WHERE tenant_id = 'tenant1';
```
### Cookie Configuration

Configure cookie settings for token storage:

```text
UPDATE token_config SET cookie_secure = true, -- HTTPS only cookie_http_only = true, -- No JavaScript access cookie_same_site = 'STRICT', -- CSRF protection cookie_domain = '.example.com', cookie_path = '/' WHERE tenant_id = 'tenant1';
```
## Examples

### cURL Examples


**Refresh Token**:

```text
curl --location 'http://localhost:8080/v2/refresh-token' \
--header 'tenant-id: tenant1' \
--header 'Cookie: RT=xyz789abc123' \
--header 'Content-Type: application/json' \
--data '{
    "refresh_token": "xyz789abc123",
    "client_id": "aB3dE5fG7hI9jK1lM"
}'
```

**Logout (Token)**:

```text
curl --location 'http://localhost:8080/v2/logout' \ 
--header 'Content-Type: application/json' \ 
--header 'tenant-id: tenant1' \
--cookie 'RT=xyz789abc123' \ 
--data '{ 
    "refresh_token": "xyz789abc123", 
    "logout_type": "token" 
}'
```

**Logout (Client)**:

```text
curl --location 'http://localhost:8080/v2/logout' \ 
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \ 
--data '{ 
"refresh_token": "xyz789abc123", 
"logout_type": "client", 
"client_id": "aB3dE5fG7hI9jK1lM"
}'
```

**Logout (Tenant)**:

```text
curl --location 'http://localhost:8080/v2/logout' \ 
--header 'Content-Type: application/json' \ 
--header 'tenant-id: tenant1' \ 
--data '{ 
"refresh_token": "xyz789abc123", 
"logout_type": "tenant" 
}'
```

### Best Practices

### Security


1.  **Token Storage**:

    *   **Web**: Use HttpOnly cookies for refresh tokens (preferred)

    *   **Mobile**: Use secure keychain/keystore

    *   **Never**: Store tokens in URLs or logs

2.  **Token Refresh**:

    *   Refresh access token before expiry

    *   Handle refresh failures gracefully

    *   Store refresh token securely

3.  **Logout**:

    *   Always call logout API before clearing local storage

    *   Clear all tokens on logout

    *   Redirect to login after logout

4.  **Token Validation**:

    *   Validate token expiry before use

    *   Check revocation list in distributed systems

    *   Verify token signature (for JWTs)


### User Experience


1.  **Seamless Refresh**: Implement automatic token refresh

2.  **Error Handling**: Handle token expiration gracefully

3.  **Loading States**: Show loading during token refresh

4.  **Session Persistence**: Persist sessions across page reloads

5.  **Clear Logout**: Provide clear logout options (current device, all devices)


## Troubleshooting

### "Refresh token invalid"


**Problem**: Refresh token is rejected

**Solutions**:

*   Check refresh token hasn't expired

*   Verify refresh token hasn't been revoked

*   Ensure refresh token matches the tenant

*   Check if token was revoked via logout


### "Access token expired"


**Problem**: Access token is expired

**Solutions**:

*   Call `/v2/refresh-token` to get new access token

*   Check token expiry before making requests


### "Logout failed"


**Problem**: Logout API call fails

**Solutions**:

*   Verify refresh token is valid

*   Check tenant-id header is correct

*   Ensure refresh token hasn't already been invalidated

*   Check network connectivity


### "Token refresh returns 401"


**Problem**: Token refresh returns unauthorized

**Solutions**:

*   Check refresh token hasn't expired

*   Verify refresh token hasn't been revoked

*   Ensure client\_id matches (if provided)

*   Check tenant-id header is correct
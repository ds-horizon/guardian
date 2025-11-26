# Google Authentication

Complete guide for implementing Google authentication using Guardian's `/v2/auth/google` endpoint.

## Table of Contents

*   [Overview](#overview)

*   [Prerequisites](#prerequisites)

*   [Configuration](#configuration)

*   [API Endpoint](#api-endpoint)

*   [API Specification](#api-specification)

*   [Frontend Implementation](#frontend-implementation)

*   [Flow Diagram](#flow-diagram)

*   [Troubleshooting](#troubleshooting)


## Overview

Google authentication uses OpenID Connect (OIDC) protocol with Google ID tokens. Guardian verifies the ID token signature using Google's JWKS and extracts user information from the token claims.

### How It Works

1.  Client obtains Google ID token from Google Sign-In

2.  Client sends ID token to Guardian `/v2/auth/google`

3.  Guardian verifies ID token signature using Google's JWKS

4.  Guardian extracts user information from ID token

5.  Guardian creates/retrieves user via user service

6.  Guardian returns access token, refresh token, and ID token


## Prerequisites

Before implementing Google authentication, you need:

1.  **Guardian Tenant**: A [tenant configured](../configuration/configuration.md#tenant-onboarding) in Guardian

2.  **OAuth Client**: A [client created](../configuration/configuration.md#create-client) in Guardian (for `client_id`)

3.  **Google Credentials**: Client ID and Client Secret from Google Cloud Console


## Configuration

### Step 1: Get Google Credentials

1.  Go to [Google Cloud Console](https://console.cloud.google.com/ "https://console.cloud.google.com/")

2.  Create a new project or select an existing one

3.  Enable **Google+ API** (or **Google Identity Services API**)

4.  Navigate to **APIs & Services** → **Credentials**

5.  Click **Create Credentials** → **OAuth client ID**

6.  Configure OAuth consent screen (if not done):

    *   User Type: External (for public apps) or Internal (for G Suite)

    *   App name, support email, developer contact

    *   Scopes: `openid`, `profile`, `email`

7.  Create OAuth 2.0 Client ID:

    *   Application type: **Web application**

    *   Name: Your application name

    *   Authorized redirect URIs: Add your callback URLs

        *   Example: `https://your-app.com/auth/google/callback`

    *   Authorized JavaScript origins: Add your domain

        *   Example: `https://your-app.com`

8.  Copy **Client ID** and **Client Secret**


**Required Information for Guardian**:

*   Client ID (format: `xxxxx.apps.googleusercontent.com`)

*   Client Secret


### Step 2: Configure Guardian Database

Insert Google credentials into the `google_config` table:

```text
INSERT INTO google_config ( tenant_id, client_id, client_secret ) VALUES ( 'tenant1', 'your_google_client_id.apps.googleusercontent.com', 'your_google_client_secret' );
```

**Table Schema**:

*   `tenant_id` (CHAR(10)): Your tenant identifier

*   `client_id` (VARCHAR(256)): Google OAuth Client ID

*   `client_secret` (VARCHAR(256)): Google OAuth Client Secret


## API Endpoint

### Google Authentication

**Endpoint**: `POST /v2/auth/google`

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)


**Request Body**:

```json
{
  "id_token": "eyJhbGciOiJSUzI1NiIs...",
  "response_type": "token",
  "client_id": "client1",
  "flow": "signinup",
  "scopes": [
    "default"
  ],
  "meta_info": {
    "ip": "127.0.0.1",
    "location": "localhost",
    "device_name": "Chrome Browser",
    "source": "web"
  }
}
```

**Request Parameters**:

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id_token | string | Yes | Google ID token (JWT) obtained from Google Sign-In |
| response_type | string | Yes | Desired response type. Options: "token", "code" |
| client_id | string | Yes | Guardian OAuth client ID |
| flow | string | No | Authentication flow type. Options: "signinup" (default), "signin", "signup" |
| scopes | array | No | Array of scope names to include in the access token |
| meta_info | object | No | Request metadata |

**Response**: `200 OK`

```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "xyz789...",
  "id_token": "eyJhbGci...",
  "sso_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "is_new_user": false
}
```
**Response Parameters**:

| Parameter | Type | Description |
| --- | --- | --- |
| access_token | string | JWT access token for API authentication |
| refresh_token | string | Opaque refresh token for obtaining new access tokens |
| id_token | string | OpenID Connect ID token containing user information |
| sso_token | string | Single Sign-On token for cross-application authentication |
| token_type | string | Token type. Always "Bearer" |
| expires_in | integer | Access token expiration time in seconds |
| is_new_user | boolean | Indicates if this is a newly created user |

**Error Responses**:

*   `400 Bad Request`: Invalid request (missing fields, invalid token)

*   `401 Unauthorized`: Invalid Google credentials or token

*   `500 Internal Server Error`: Server error

### cURL Example

```text
  curl --location 'http://localhost:8080/v2/auth/google' \
  --header 'Content-Type: application/json' \ 
  --header 'tenant-id: tenant1' \ 
  --data '{
    "id_token": "eyJhbGciOiJSUzI1NiIs...",
    "response_type": "token",
    "client_id": "aB3dE5fG7hI9jK1lM",
    "scopes": ["default"],
    "flow": "signinup",
    "meta_info": {
        "ip": "127.0.0.1",
        "location": "localhost",
        "device_name": "Chrome Browser",
        "source": "web"
    }
}'
```

## API Specification

### Request Schema

#### V2AuthGoogleRequestBody

```yaml
type: object
required:
  - id_token
  - response_type
  - client_id

properties:
  id_token:
    type: string
    description: Google ID token (JWT)
    example: "eyJhbGciOiJSUzI1NiIs..."

  response_type:
    type: string
    enum: ["token", "code"]
    description: Desired response type
    example: "token"

  client_id:
    type: string
    description: Guardian OAuth client ID
    example: "aB3dE5fG7hI9jK1lM"

  flow:
    type: string
    enum: ["signinup", "signin", "signup"]
    default: "signinup"
    description: Authentication flow type

  scopes:
    type: array
    items:
      type: string
    description: Requested scopes

  meta_info:
    type: object
    properties:
      ip:
        type: string
      location:
        type: string
      device_name:
        type: string
      source:
        type: string
```

### Response Schema

#### TokenResponse
```yaml
type: object
properties:
  access_token:
    type: string
    description: Short-lived Bearer JWT token

  refresh_token:
    type: string
    description: Long-lived token for refreshing access token

  id_token:
    type: string
    description: OpenID Connect ID token

  sso_token:
    type: string
    description: SSO token

  token_type:
    type: string
    example: "Bearer"

  expires_in:
    type: integer
    description: Access token expiration in seconds

  is_new_user:
    type: boolean
    description: Whether this is a newly created user
```
## Frontend Implementation

For implementing Google authentication in your frontend application, please refer to the official Google documentation:

*   [Google Identity Services - Sign In With Google](https://developers.google.com/identity/gsi/web) - Official guide for implementing Google Sign-In in web applications
*   [Google Sign-In for iOS](https://developers.google.com/identity/sign-in/ios) - Official guide for implementing Google Sign-In in iOS applications
*   [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android) - Official guide for implementing Google Sign-In in Android applications
## Flow Diagram

```text
┌─────────┐                    ┌──────────┐                    ┌──────────┐
│ Client  │                    │ Guardian │                    │  Google  │
│         │                    │          │                    │          │
└────┬────┘                    └────┬─────┘                    └────┬─────┘
     │                              │                               │
     │ 1. Initialize Google Sign-In │                               │
     │    (with Google Client ID)   │                               │
     │                              │                               │
     │ 2. User clicks "Sign in"     │                               │
     │─────────────────────────────────────────────────────────────>│
     │                              │                               │
     │ 3. User authenticates        │                               │
     │─────────────────────────────────────────────────────────────>│
     │                              │                               │
     │ 4. Return ID Token           │                               │
     │<─────────────────────────────────────────────────────────────│
     │                              │                               │
     │ 5. POST /v2/auth/google      │                               │
     │─────────────────────────────>│                               │
     │   {id_token, client_id, ...} │                               │
     │                              │                               │
     │                              │ 6. Verify ID Token            │
     │                              │    (JWKS signature check)     │
     │                              │    Extract user info          │
     │                              │                               │
     │                              │ 7. Get/Create user            │
     │                              │    (via user service)         │
     │                              │                               │
     │                              │ 8. Generate Guardian tokens   │
     │                              │                               │
     │ 9. Return tokens             │                               │
     │<─────────────────────────────│                               │
     │ {access_token, refresh_token}│                               │
     │                              │                               │

```
## Troubleshooting

### "Invalid Google credentials"

**Problem**: Guardian cannot verify Google credentials

**Solutions**:

*   Verify Google Client ID and Secret in `google_config` table

*   Ensure credentials match those from Google Cloud Console

*   Check that credentials are for the correct tenant


### "ID token verification failed"

**Problem**: Guardian cannot verify the ID token signature

**Solutions**:

*   Check that Google+ API is enabled in Google Cloud Console

*   Verify ID token hasn't expired (typically valid for 1 hour)

*   Ensure ID token is from the correct Google Client ID


### "Redirect URI mismatch"

**Problem**: Google rejects the redirect URI

**Solutions**:

*   Add your redirect URI to Google Cloud Console OAuth settings

*   Ensure redirect URI exactly matches (including protocol and port)

*   Check Authorized JavaScript origins includes your domain


### "User service error"

**Problem**: Guardian cannot create/retrieve user

**Solutions**:

*   Check user service is accessible

*   Verify user service endpoints return correct format

*   Check user service logs for errors


## Best Practices

1.  **Token Validation**: Always validate tokens on the server side

2.  **HTTPS**: Use HTTPS for all API calls in production

3.  **Token Storage**: Store tokens securely (httpOnly cookies or secure storage)

4.  **Error Handling**: Handle errors gracefully and provide user feedback

5.  **Loading States**: Show loading states during authentication


## Related Documentation

*   [Social Authentication](./social-authentication.md) - Overview of social auth

*   [Facebook Authentication](./facebook-authentication.md) - Facebook integration
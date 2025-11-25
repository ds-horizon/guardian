# Facebook Authentication

Complete guide for implementing Facebook authentication using Guardian's `/v2/auth/fb` endpoint.

## Table of Contents


*   [Overview](#overview)

*   [Prerequisites](#prerequisites)

*   [Configuration](#configuration)

*   [API Endpoint](#api-endpoint)

*   [API Specification](#api-specification)

*   [Frontend Implementation](#frontend-implementation)

*   [Troubleshooting](#troubleshooting)


## Overview


Facebook authentication uses OAuth 2.0 protocol with Facebook access tokens. Guardian validates the access token via Facebook Graph API and retrieves user information from the API.

### How It Works


1.  Client obtains Facebook access token from Facebook SDK

2.  Client sends access token to Guardian `/v2/auth/fb`

3.  Guardian validates access token via Facebook Graph API

4.  Guardian retrieves user information from Graph API

5.  Guardian creates/retrieves user via user service

6.  Guardian returns access token, refresh token, and ID token


## Prerequisites


Before implementing Facebook authentication, you need:

1.  **Guardian Tenant**: A tenant configured in Guardian

2.  **OAuth Client**: A client created in Guardian (for `client_id`)

3.  **Facebook Credentials**: App ID and App Secret from Facebook Developers


## Configuration

### Step 1: Get Facebook Credentials


1.  Go to [Facebook Developers](https://developers.facebook.com/ "https://developers.facebook.com/")

2.  Click **My Apps** → **Create App**

3.  Select app type: **Consumer** or **Business**

4.  Fill in app details (name, contact email)

5.  Add **Facebook Login** product:

    *   Go to **Products** → **Facebook Login** → **Set Up**

6.  Configure Facebook Login:

    *   **Settings** → **Basic**:

        *   App Domains: Your domain (e.g., `your-app.com`)

        *   Privacy Policy URL

        *   Terms of Service URL

    *   **Settings** → **Facebook Login**:

        *   Valid OAuth Redirect URIs: Add your callback URLs

            *   Example: `https://your-app.com/auth/facebook/callback`

        *   Deauthorize Callback URL (optional)

7.  Copy **App ID** and **App Secret** (from **Settings** → **Basic**)


**Required Information for Guardian**:

*   App ID

*   App Secret


### Step 2: Configure Guardian Database


Insert Facebook credentials into the `fb_config` table:

```text
 INSERT INTO fb_config ( tenant_id, app_id, app_secret, send_app_secret ) VALUES ( 'tenant1', 'your_facebook_app_id', 'your_facebook_app_secret', true );
```

**Table Schema**:

*   `tenant_id` (CHAR(10)): Your tenant identifier

*   `app_id` (VARCHAR(256)): Facebook App ID

*   `app_secret` (VARCHAR(256)): Facebook App Secret

*   `send_app_secret` (BOOLEAN): Whether to send app secret in requests (usually `true`)


## API Endpoint

### Facebook Authentication


**Endpoint**: `POST /v2/auth/fb`

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)


**Request Body**:

```json
{ "access_token": "facebook_access_token_here", 
  "response_type": "token", 
  "client_id": "client1",
  "flow": "signinup",
  "scopes": ["default"],
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
| access_token | string | Yes | Facebook access token obtained from Facebook Login |
| response_type | string | Yes | Desired response type. Options: "token", "code" |
| client_id | string | Yes | Guardian OAuth client ID |
| flow | string | No | Authentication flow type. Options: "signinup" (default), "signin", "signup" |
| meta_info | object | No | Request metadata |

**Response**: `200 OK`

```json
{ "access_token": "eyJhbGci...",
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

*   `401 Unauthorized`: Invalid Facebook credentials or token

*   `500 Internal Server Error`: Server error

### cURL Example


```text
curl --location 'http://localhost:8080/v2/auth/fb' \ 
--header 'Content-Type: application/json' \ 
--header 'tenant-id: tenant1' \ 
--data '{
    "access_token": "facebook_access_token_here",
    "response_type": "token",
    "client_id": "aB3dE5fG7hI9jK1lM",
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

#### V2AuthFbRequestBody

```yaml
type: object
required:
  - access_token
  - response_type
  - client_id

properties:
  access_token:
    type: string
    description: Facebook access token
    example: "facebook_access_token_here"

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

  scopes:
    type: array
    items:
      type: string

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

For implementing Facebook authentication in your frontend application, please refer to the official Facebook documentation:

*   [Facebook Login for Web](https://developers.facebook.com/docs/facebook-login/web) - Official guide for implementing Facebook Login in web applications
*   [Facebook Login for iOS](https://developers.facebook.com/docs/facebook-login/ios) - Official guide for implementing Facebook Login in iOS applications
*   [Facebook Login for Android](https://developers.facebook.com/docs/facebook-login/android) - Official guide for implementing Facebook Login in Android applications

## Flow Diagram


```text
┌─────────┐                    ┌──────────┐                    ┌──────────┐
│ Client  │                    │ Guardian │                    │ Facebook │
│         │                    │          │                    │          │
└────┬────┘                    └────┬─────┘                    └────┬─────┘
     │                              │                               │
     │ 1. Initialize Facebook SDK   │                               │
     │    (with Facebook App ID)    │                               │
     │                              │                               │
     │ 2. User clicks "Sign in"     │                               │
     │─────────────────────────────────────────────────────────────>│
     │                              │                               │
     │ 3. User authenticates        │                               │
     │─────────────────────────────────────────────────────────────>│
     │                              │                               │
     │ 4. Return Access Token       │                               │
     │<─────────────────────────────────────────────────────────────│
     │                              │                               │
     │ 5. POST /v2/auth/fb          │                               │
     │─────────────────────────────>│                               │
     │{access_token, client_id, ...}│                               │
     │                              │                               │
     │                              │ 6. Validate Access Token      │
     │                              │──────────────────────────────>│
     │                              │    GET /me?access_token=...   │
     │                              │                               │
     │                              │ 7. Return user info           │
     │                              │<──────────────────────────────│
     │                              │                               │
     │                              │ 8. Get/Create user            │
     │                              │    (via user service)         │
     │                              │                               │
     │                              │ 9. Generate Guardian tokens   │
     │                              │                               │
     │ 10. Return tokens            │                               │
     │<─────────────────────────────│                               │
     │ {access_token, refresh_token}│                               │
     │                              │                               │

```
## Troubleshooting

### Invalid Facebook credentials


**Problem**: Guardian cannot validate Facebook credentials

**Solutions**:

*   Verify Facebook App ID and App Secret in `fb_config` table

*   Ensure credentials match those from Facebook Developers

*   Check that credentials are for the correct tenant


### Access token validation failed


**Problem**: Guardian cannot validate the Facebook access token

**Solutions**:

*   Check Facebook Login product is added to your app

*   Verify access token permissions include `email` and `public_profile`

*   Ensure access token hasn't expired

*   Check that access token is from the correct Facebook App ID


### Redirect URI mismatch


**Problem**: Facebook rejects the redirect URI

**Solutions**:

*   Add redirect URI to Facebook App settings

*   Ensure redirect URI is in valid OAuth redirect URIs list

*   Check App Domains includes your domain


### User service error


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

6.  **Scopes**: Request only necessary permissions (`email`, `public_profile`)


## Related Documentation


*   [Social Authentication](https://github.com/ds-horizon/guardian/tree/main/docs/features/SocialAuthentication "https://github.com/ds-horizon/guardian/tree/main/docs/features/SocialAuthentication") - Overview of social auth

*   [Google Authentication](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication") - Google integration
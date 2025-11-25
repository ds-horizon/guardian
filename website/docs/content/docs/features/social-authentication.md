---
title: Social Authentication
description: Guide for implementing social authentication allowing users to sign in with their existing social media accounts
---


Social authentication allows users to sign in using their existing social media accounts, providing a seamless authentication experience without requiring users to create new accounts.

## Table of Contents

* [Overview](#overview)
* [Supported Providers](#supported-providers)
* [Quick Start](#quick-start)
* [Provider-Specific Guides](#provider-specific-guides)
* [Common Configuration](#common-configuration)
* [OIDC Provider Connect](#oidc-provider-connect)
* [Best Practices](#best-practices)
* [Troubleshooting](#troubleshooting)

## Overview

Social authentication enables users to authenticate using their existing social media accounts, eliminating the need for separate username/password credentials. Guardian supports multiple social authentication providers with an unified API.

### Benefits

*   **Faster Onboarding**: Users can signin/up with one click

*   **Reduced Friction**: No need to remember passwords

*   **Higher Conversion**: Easier sign-up process

*   **Trust**: Users trust established providers


### Authentication Flows

*   **Sign In**: Authenticate existing users

*   **Sign Up**: Register new users

*   **Sign In/Up**: Automatically sign in if user exists, otherwise sign up (default)


## Supported Providers

Guardian supports the following social authentication providers:

### Google

*   **Protocol**: OpenID Connect (OIDC)

*   **Token Type**: ID Token (JWT)

*   **Verification**: JWKS-based signature verification

*   **User Info**: Extracted from ID token claims


**📖** [**Complete Google Authentication Guide**](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md")

### Facebook

*   **Protocol**: OAuth 2.0

*   **Token Type**: Access Token

*   **Verification**: Graph API validation

*   **User Info**: Retrieved from Graph API


**📖** [**Complete Facebook Authentication Guide**](https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md")

### Custom OIDC Providers

*   **Protocol**: OpenID Connect

*   **Token Type**: ID Token or Authorization Code

*   **Verification**: Provider-specific JWKS

*   **User Info**: From ID token or UserInfo endpoint


**See** [**OIDC Provider Connect**](#oidc-provider-connect) **section below**

## Quick Start

### 1\. Choose Your Provider

Select which provider(s) you want to support:

*   [Google Authentication](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md") - Recommended for most use cases

*   [Facebook Authentication](https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md") - Popular for consumer apps

*   Custom OIDC Provider - For enterprise or custom providers(e.g.: apple)


### 2\. Get Provider Credentials

Each provider requires specific credentials:

**Google**:

*   Client ID and Client Secret from [Google Cloud Console](https://console.cloud.google.com/ "https://console.cloud.google.com/")

*   See [Google Authentication Guide](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md") for detailed steps


**Facebook**:

*   App ID and App Secret from [Facebook Developers](https://developers.facebook.com/ "https://developers.facebook.com/")

*   See [Facebook Authentication Guide](https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md") for detailed steps


### 3\. Configure Guardian

**Insert provider credentials into the appropriate database table**:

**Google**:

```text
INSERT INTO google_config (tenant_id, client_id, client_secret) VALUES ('tenant1', 'your_google_client_id', 'your_google_client_secret');
```

**Facebook**:

```text
INSERT INTO fb_config (tenant_id, app_id, app_secret, send_app_secret) VALUES ('tenant1', 'your_facebook_app_id', 'your_facebook_app_secret', true);
```

### 4\. Implement Authentication

Follow the provider-specific guide:

*   [Google Implementation](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md")

*   [Facebook Implementation](https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md")


## Provider-Specific Guides

For complete implementation details, see the dedicated guides:

### 📘 [Google Authentication Guide](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md")

Complete guide for Google authentication including:

*   Step-by-step credential setup

*   Database configuration

*   API endpoint details

*   JavaScript and React examples

*   Troubleshooting


**Endpoint**: `POST /v2/auth/google`

### 📘 [Facebook Authentication Guide](https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md")

Complete guide for Facebook authentication including:

*   Step-by-step credential setup

*   Database configuration

*   API endpoint details

*   JavaScript and React examples

*   Troubleshooting


**Endpoint**: `POST /v2/auth/fb`

## Common Configuration

### User Service Integration

Guardian integrates with your user service to create and retrieve users. Your user service must implement:

*   `GET /user` - Get user by email, phone, or provider info

*   `POST /user` - Create new user

*   `POST /provider` - Add provider to existing user


User service Swagger

## OIDC Provider Connect

For custom OIDC providers, use the `/v2/idp/connect` endpoint.

### Configuration

```text
INSERT INTO oidc_provider_config ( tenant_id, provider_name, issuer, jwks_url, token_url, client_id, client_secret, redirect_uri, client_auth_method, is_ssl_enabled, user_identifier, audience_claims ) VALUES ( 'tenant1', 'custom_provider', 'https://provider.example.com', 'https://provider.example.com/.well-known/jwks.json', 'https://provider.example.com/oauth/token', 'your_client_id', 'your_client_secret', 'https://guardian.example.com/callback', 'client_secret_basic', true, 'email', '["audience1", "audience2"]' );
```
### API Endpoint

**Endpoint**: `POST /v2/idp/connect`

**Request**:

```json
{ 
  "id_provider": "custom_provider", 
  "identifier": "authorization_code_or_id_token", 
  "identifier_type": "code", 
  "response_type": "token", 
  "client_id": "aB3dE5fG7hI9jK1lM", 
  "code_verifier": "pkce_code_verifier",
  "nonce": "optional_nonce", 
  "flow": "signinup"
}
```
**Request Parameters**:

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| id_provider | string | Yes | Identity provider name (configured provider identifier) |
| identifier | string | Yes | Provider-specific identifier (authorization code or ID token) |
| identifier_type | string | Yes | Type of identifier. Options: "code" (default), "id_token" |
| response_type | string | Yes | Desired response type. Options: "token", "code" |
| client_id | string | Yes | Guardian OAuth client ID |
| code_verifier | string | No | PKCE code verifier (required if code_challenge was used) |
| nonce | string | No | Nonce value for OIDC flows |
| flow | string | No | Authentication flow type. Options: "signinup" (default), "signin", "signup" |
| meta_info | object | No | Request metadata (same structure as passwordless) |

**Response**: `200 OK`

```json
{
    "access_token": "eyJhbGci...",
    "refresh_token": "xyz789...",
    "id_token": "eyJhbGci...",
    "sso_token": "eyJhbGci...",
    "token_type": "Bearer",
    "expires_in": 3600,
    "is_new_user": false,
    "idp_credentials": {
        "access_token": "provider_access_token",
        "refresh_token": "provider_refresh_token",
        "id_token": "provider_id_token"
    }
}
```

**Response Parameters**:

| Parameter | Type | Description |
| --- | --- | --- |
| access_token | string | JWT access token for API authentication |
| refresh_token | string | Opaque refresh token for obtaining new access tokens |
| id_token | string | OpenID Connect ID token containing user information |
| sso_token | string | Single Sign-On token for cross-application authentication |
| idp_credentials | json | A JSON holding the authentication tokens from the external identity provider. |
| token_type | string | Token type. Always "Bearer" |
| expires_in | integer | Access token expiration time in seconds |
| is_new_user | boolean | Indicates if this is a newly created user |

## Best Practices

### Security

1.  **Token Validation**: Always validate tokens on the server side

2.  **HTTPS**: Use HTTPS for all API calls in production

3.  **Token Storage**: Store tokens securely (httpOnly cookies or secure storage)

4.  **Credential Security**: Never expose client secrets in client-side code

5.  **Nonce**: Use nonce for OIDC flows to prevent replay attacks

6.  **PKCE**: Use PKCE for authorization code flow


### User Experience

1.  **Provider Selection**: Allow users to choose their preferred provider

2.  **Error Handling**: Handle provider errors gracefully

3.  **Loading States**: Show loading states during authentication

4.  **Account Linking**: Link social accounts to existing accounts when possible

5.  **Clear Messages**: Provide clear error messages to users


### Configuration

1.  **Redirect URIs**: Configure correct redirect URIs for each provider

2.  **Scopes**: Request only necessary scopes

3.  **User Identifier**: Use consistent user identifier (email recommended)

4.  **Provider Updates**: Keep provider SDKs updated

5.  **Testing**: Test with provider's test/sandbox environments first


## Troubleshooting

### Common Issues

**Problem**: "Invalid credentials"

*   **Solution**: Verify provider credentials in database match those from provider console

*   **Check**: Ensure credentials are for the correct tenant


**Problem**: "Token validation failed"

*   **Solution**: Check provider API is enabled and accessible

*   **Check**: Verify token hasn't expired


**Problem**: "Redirect URI mismatch"

*   **Solution**: Add redirect URI to provider's OAuth settings

*   **Check**: Ensure redirect URI exactly matches


**Problem**: "User service error"

*   **Solution**: Check user service is accessible and returns correct format

*   **Check**: Verify user service endpoints are implemented correctly


### Provider-Specific Troubleshooting

*   **Google Issues**: See [Google Authentication Troubleshooting](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md#Troubleshooting "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md#Troubleshooting")

*   **Facebook Issues**: See [Facebook Authentication Troubleshooting](https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md#Troubleshooting "https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md#Troubleshooting")


## Related Documentation

*   [Google Authentication](https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/GoogleAuthentication.md") - Complete Google integration guide

*   [Facebook Authentication](https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md "https://github.com/ds-horizon/guardian/tree/main/docs/features/FacebookAuthentication.md") - Complete Facebook integration guide
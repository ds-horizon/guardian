---
title: Username/Password Authentication
description: Complete guide for implementing username/password authentication using Guardian's signin and signup endpoints
---

Complete guide for implementing username/password authentication using Guardian's `/v1/signin` and `/v1/signup` endpoints.

## Overview

Username/password authentication is the traditional authentication method where users provide a username and password to sign in or sign up. Guardian validates credentials through user service and returns access token, refresh token, and ID token upon successful authentication.

### How It Works

**Sign Up Flow**:

1.  Client sends username, password, and response type to Guardian `/v1/signup`

2.  Guardian checks if user exists via user service `GET /user` API

3.  If user doesn't exist, Guardian creates user via user service `POST /user` API

4. Guardian returns tokens if `response_type` is set to `token`, otherwise it returns a `code`.

**Sign In Flow**:

1.  Client sends username, password, and response type to Guardian `/v1/signin`

2.  Guardian validates credentials via user service `POST /authenticate` API

3. Guardian returns tokens if `response_type` is set to `token`, otherwise it returns a `code`.

## Prerequisites

Before implementing username/password authentication, you need:

1.  **Guardian Tenant**: A [tenant configured](/docs/configuration/configuration#tenant-onboarding) in Guardian

2.  **User Service**: A user service that implements the following endpoints:

    *   `GET /user` - Get user by username, email, or phone

    *   `POST /user` - Create a new user

    *   `POST /authenticate` - Authenticate user with username and password

3.  **User Service Configuration**: User service endpoints configured in Guardian tenant configuration

## Configuration

### Step 1: Configure User Service

Ensure your user service is accessible and implements the required endpoints. The user service configuration should be set in the Guardian tenant configuration.

Refer [userConfiguration](/docs/configuration/user-configuration)


## API Endpoints

### Sign Up

**Endpoint**: `POST /v1/signup`

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)

**Request Body**:

```json
{
  "username": "john.doe@example.com",
  "password": "SecurePassword123!",
  "responseType": "token",
  "metaInfo": {
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
| username | string | Yes | Username, email, or phone number for the user |
| password | string | Yes | User's password |
| responseType | string | Yes | Desired response type. Options: "token", "code" |
| metaInfo | object | No | Request metadata (ip, location, device_name, source) |

**Response**: `200 OK`

```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "xyz789...",
  "idToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "isNewUser": true
}
```

**Response Parameters**:

| Parameter | Type | Description |
| --- | --- | --- |
| accessToken | string | JWT access token for API authentication |
| refreshToken | string | Opaque refresh token for obtaining new access tokens |
| idToken | string | OpenID Connect ID token containing user information |
| tokenType | string | Token type. Always "Bearer" |
| expiresIn | integer | Access token expiration time in seconds |
| isNewUser | boolean | Indicates if this is a newly created user |

**Error Responses**:

*   `400 Bad Request`: Invalid request (missing fields, username already exists, invalid response type)

*   `500 Internal Server Error`: Server error

### Sign In

**Endpoint**: `POST /v1/signin`

**Headers**:

*   `Content-Type: application/json`

*   `tenant-id: <your-tenant-id>` (required)

**Request Body**:

```json
{
  "username": "john.doe@example.com",
  "password": "SecurePassword123!",
  "responseType": "token",
  "metaInfo": {
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
| username | string | Yes | Username, email, or phone number for the user |
| password | string | Yes | User's password |
| responseType | string | Yes | Desired response type. Options: "token", "code" |
| metaInfo | object | No | Request metadata (ip, location, device_name, source) |

**Response**: `200 OK`

```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "xyz789...",
  "idToken": "eyJhbGci...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "isNewUser": false
}
```

**Response Parameters**:

| Parameter | Type | Description |
| --- | --- | --- |
| accessToken | string | JWT access token for API authentication |
| refreshToken | string | Opaque refresh token for obtaining new access tokens |
| idToken | string | OpenID Connect ID token containing user information |
| tokenType | string | Token type. Always "Bearer" |
| expiresIn | integer | Access token expiration time in seconds |
| isNewUser | boolean | Indicates if this is a newly created user (always `false` for signin) |

**Error Responses**:

*   `400 Bad Request`: Invalid request (missing fields, invalid credentials, invalid response type)

*   `401 Unauthorized`: Invalid username or password

*   `500 Internal Server Error`: Server error

## API Specification

### Request Schema

#### V1SignupRequestBody

```yaml
type: object
required:
  - username
  - password
  - responseType

properties:
  username:
    type: string
    description: Username, email, or phone number for the user
    example: "john.doe@example.com"

  password:
    type: string
    description: User's password
    example: "SecurePassword123!"

  responseType:
    type: string
    enum: ["token", "code"]
    description: Desired response type
    example: "token"

  metaInfo:
    type: object
    properties:
      ip:
        type: string
        description: Client IP address
        example: "127.0.0.1"
      location:
        type: string
        description: Client location
        example: "localhost"
      device_name:
        type: string
        description: Device name
        example: "Chrome Browser"
      source:
        type: string
        description: Request source
        example: "web"
```

#### V1SigninRequestBody

```yaml
type: object
required:
  - username
  - password
  - responseType

properties:
  username:
    type: string
    description: Username, email, or phone number for the user
    example: "john.doe@example.com"

  password:
    type: string
    description: User's password
    example: "SecurePassword123!"

  responseType:
    type: string
    enum: ["token", "code"]
    description: Desired response type
    example: "token"

  metaInfo:
    type: object
    properties:
      ip:
        type: string
        description: Client IP address
        example: "127.0.0.1"
      location:
        type: string
        description: Client location
        example: "localhost"
      device_name:
        type: string
        description: Device name
        example: "Chrome Browser"
      source:
        type: string
        description: Request source
        example: "web"
```

### Response Schema

#### TokenResponse

```yaml
type: object
properties:
  accessToken:
    type: string
    description: Short-lived Bearer JWT token

  refreshToken:
    type: string
    description: Long-lived token for refreshing access token

  idToken:
    type: string
    description: OpenID Connect ID token

  tokenType:
    type: string
    example: "Bearer"

  expiresIn:
    type: integer
    description: Access token expiration in seconds

  isNewUser:
    type: boolean
    description: Whether this is a newly created user
```

## Examples

### cURL Examples

**Sign Up**:

```bash
    curl --location 'http://localhost:8080/v1/signup' \
    --header 'Content-Type: application/json' \
    --header 'tenant-id: tenant1' \
    --data '{
        "username": "john.doe@example.com",
        "password": "SecurePassword123!",
        "responseType": "token",
        "metaInfo": {
            "ip": "127.0.0.1",
            "location": "localhost",
            "device_name": "Chrome Browser",
            "source": "web"
        }
    }'
```

**Sign In**:

```bash
    curl --location 'http://localhost:8080/v1/signin' \
    --header 'Content-Type: application/json' \
    --header 'tenant-id: tenant1' \
    --data '{
        "username": "john.doe@example.com",
        "password": "SecurePassword123!",
        "responseType": "token",
        "metaInfo": {
            "ip": "127.0.0.1",
            "location": "localhost",
            "device_name": "Chrome Browser",
            "source": "web"
        }
    }'
```

## Flow Diagram

### Sign Up Flow

```text
┌─────────┐                    ┌──────────┐                    ┌─────────────┐
│ Client  │                    │ Guardian │                    │ User Service│
│         │                    │          │                    │             │
└────┬────┘                    └────┬─────┘                    └────┬────────┘
     │                              │                               │
     │ 1. POST /v1/signup           │                               │
     │─────────────────────────────>│                               │
     │ {username, password, ...}    │                               │
     │                              │                               │
     │                              │ 2. GET /user?identifier=...   │
     │                              │──────────────────────────────>│
     │                              │                               │
     │                              │ 3. User not found             │
     │                              │<──────────────────────────────│
     │                              │                               │
     │                              │ 4. POST /user                 │
     │                              │──────────────────────────────>│
     │                              │ {username, password, ...}     │
     │                              │                               │
     │                              │ 5. User created               │
     │                              │<──────────────────────────────│
     │                              │                               │
     │                              │ 6. Generate tokens            │
     │                              │                               │
     │ 7. Return tokens             │                               │
     │<─────────────────────────────│                               │
     │ {accessToken, refreshToken}  │                               │
     │                              │                               │
```

### Sign In Flow

```text
┌─────────┐                    ┌──────────┐                    ┌─────────────┐
│ Client  │                    │ Guardian │                    │ User Service│
│         │                    │          │                    │             │
└────┬────┘                    └────┬─────┘                    └────┬────────┘
     │                              │                               │
     │ 1. POST /v1/signin           │                               │
     │─────────────────────────────>│                               │
     │ {username, password, ...}    │                               │
     │                              │                               │
     │                              │ 2. POST /authenticate         │
     │                              │──────────────────────────────>│
     │                              │ {username, password}          │
     │                              │                               │
     │                              │ 3. Authentication successful  │
     │                              │<──────────────────────────────│
     │                              │ {user details}                │
     │                              │                               │
     │                              │ 4. Generate tokens            │
     │                              │                               │
     │ 5. Return tokens             │                               │
     │<─────────────────────────────│                               │
     │ {accessToken, refreshToken}  │                               │
     │                              │                               │
```

## Troubleshooting

### "Username already exists"

**Problem**: Attempting to sign up with a username that already exists

**Solutions**:

*   Check if the username exists in your user service

*   Use sign in endpoint instead if user already exists

*   Verify user service `GET /user` endpoint is working correctly

### "Invalid credentials"

**Problem**: Sign in fails with invalid username or password

**Solutions**:

*   Verify username and password are correct

*   Check user service `POST /authenticate` endpoint is working correctly

*   Ensure password is being hashed/validated correctly in user service

*   Check user service logs for authentication errors

### "User service error"

**Problem**: Guardian cannot communicate with user service

**Solutions**:

*   Check user service is accessible and running

*   Verify user service configuration in `user_config` table

*   Check network connectivity between Guardian and user service

*   Verify SSL settings if using HTTPS

*   Check user service logs for errors

### "Invalid response type"

**Problem**: Request fails with invalid response type error

**Solutions**:

*   Ensure `responseType` is either "token" or "code"

*   Check that response type is included in the request body

*   Verify response type matches expected values

### "Missing username" or "Missing password"

**Problem**: Request fails due to missing required fields

**Solutions**:

*   Ensure both `username` and `password` are included in request body

*   Verify fields are not null or empty

*   Check request body format is correct JSON

## Best Practices

1.  **Password Security**: 

    *   Enforce strong password policies (minimum length, complexity requirements)

    *   Never store passwords in plain text - always hash passwords

    *   Use secure password hashing algorithms (bcrypt, Argon2, etc.)

    *   Implement password reset functionality

2.  **Token Storage**: 

    *   Store tokens securely (httpOnly cookies or secure storage)

    *   Never expose tokens in client-side code or URLs

    *   Implement token refresh mechanism

3.  **HTTPS**: 

    *   Use HTTPS for all API calls in production

    *   Never send credentials over unencrypted connections

4.  **Error Handling**: 

    *   Handle errors gracefully and provide user feedback

    *   Don't expose sensitive information in error messages

    *   Log errors for debugging but don't show technical details to users

5.  **Rate Limiting**: 

    *   Implement rate limiting to prevent brute force attacks

    *   Lock accounts after multiple failed login attempts

6.  **User Experience**: 

    *   Show loading states during authentication

    *   Provide clear error messages

    *   Implement "Remember Me" functionality using refresh tokens

7.  **Validation**: 

    *   Validate username format (email, phone, etc.)

    *   Validate password strength on client and server side

    *   Sanitize user inputs to prevent injection attacks


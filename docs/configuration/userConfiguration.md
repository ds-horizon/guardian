# User Configuration

This guide explains how to configure the user service integration with Guardian, including the required endpoints and database configuration.

## Overview

Guardian integrates with your user service to manage user accounts, authentication, and identity provider associations. Your user service must implement specific endpoints that Guardian will call during authentication flows.

## Required Endpoints

Your user service must implement the following endpoints:

### 1. GET /user - Get User Details

Retrieves user information based on query parameters.

**Query Parameters:**
- `userId` (string, optional) - User identifier
- `email` (string, optional) - User email address
- `phoneNumber` (string, optional) - User phone number (recommended format E.164)
- `providerName` (string, optional) - Name of the identity provider
- `providerUserId` (string, optional) - Provider-specific user identifier

**Request Example:**
```
GET /user?email=user@example.com
GET /user?phoneNumber=+1234567890
GET /user?email=user@example.com&providerName=google&providerUserId=12345
```

**Response (200 OK) - User Found:**
```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "emailVerified": true,
  "phoneNumberVerified": true,
  "name": "John Doe",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (200 OK) - User Not Found:**
```json
{}
```

**Important Notes:**
- Guardian can invoke this API with various combinations of query parameters
- If a user is not found, return an empty JSON object `{}` with status code 200
- Any response other than status code 200 will be considered a failure
- The user service is responsible for determining the correct user based on the provided query parameters

### 2. POST /user - Create User

Creates a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "emailVerified": true,
  "phoneNumber": "+1234567890",
  "phoneNumberVerified": true,
  "username": "johndoe",
  "password": "securePassword123",
  "name": "John Doe",
  "firstName": "John",
  "lastName": "Doe",
  "provider": {
    "name": "google",
    "providerUserId": "12345",
    "data": {},
    "credentials": {}
  }
}
```

**Required Fields:**
At least one of the following combinations must be present:
- `phoneNumber` and `phoneNumberVerified`
- `email` and `emailVerified`
- `username` and `password`

**Response (200 OK):**
```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "name": "John Doe",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Important Notes:**
- `emailVerified` will be `true` if email was verified via passwordless flow or IDP login
- `phoneNumberVerified` will be `true` if phone was verified via passwordless flow or IDP login
- If verification fields are missing or `false`, treat the contact as unverified
- The `userId` field is required in the response

### 3. POST /user/authenticate - Authenticate User

Validates username and password credentials.

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "name": "John Doe",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Important Notes:**
- The `userId` field is required in the response
- Returns user details if authentication succeeds
- Any non-200 status code indicates authentication failure

### 4. POST /provider - Add Provider to User

Adds or updates identity provider details for an existing user.

**Request Body:**
```json
{
  "userId": "user123",
  "provider": {
    "name": "google",
    "providerUserId": "12345",
    "data": {},
    "credentials": {}
  }
}
```

**Response (200 OK):**
```json
{
  "userId": "user123",
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
  "name": "John Doe"
}
```

**Important Notes:**
- Used when linking social authentication providers to existing user accounts
- The `userId` and `provider` fields are required
- Returns user details after successful provider addition

## Database Configuration

Configure the user service in the `user_config` table:

```sql
INSERT INTO user_config (
  tenant_id,
  is_ssl_enabled,
  host,
  port,
  get_user_path,
  create_user_path,
  authenticate_user_path,
  add_provider_path,
  send_provider_details
) VALUES (
  'tenant1',
  false,
  'localhost',
  8081,
  '/user',
  '/user',
  '/user/authenticate',
  '/provider',
  false
);
```

### Table Schema

| Field                  | Type         | Description                                  |
|------------------------|--------------|----------------------------------------------|
| tenant_id              | CHAR(10)     | Your tenant identifier (Primary Key)          |
| is_ssl_enabled         | BOOLEAN      | Whether SSL is enabled for user service      |
| host                   | VARCHAR(256) | Host address for user service                |
| port                   | INT          | Port number for user service                 |
| get_user_path          | VARCHAR(256) | API path for getting user details            |
| create_user_path       | VARCHAR(256) | API path for creating users                  |
| authenticate_user_path | VARCHAR(256) | API path for user authentication             |
| add_provider_path      | VARCHAR(256) | API path for adding authentication providers |
| send_provider_details  | BOOLEAN      | Whether to send provider details in requests |

## Integration Reference

For detailed API specifications, refer to the [Integration Endpoints Specification](https://github.com/ds-horizon/guardian/blob/main/src/main/resources/oas/integrations.yaml).

## Best Practices

1. **User Identification**: Implement flexible user lookup logic that can handle various query parameter combinations
2. **Error Handling**: Return appropriate HTTP status codes (200 for success, 400 for bad requests, 500 for server errors)
3. **Empty Responses**: Return `{}` with status 200 when a user is not found (not an error condition)
4. **Verification Status**: Properly handle `emailVerified` and `phoneNumberVerified` flags
5. **Security**: Use HTTPS (`is_ssl_enabled: true`) in production environments
6. **Response Format**: Always include `userId` in successful responses for create and authenticate endpoints


# User Configuration

This guide explains how to configure the user service integration with Guardian, including the required endpoints and database configuration.

## Overview

Guardian integrates with your user service to manage user accounts, authentication, and identity provider associations. Your user service must implement specific endpoints that Guardian will call during authentication flows.

## Required Endpoints

Your user service must implement the following endpoints:

### 1. GET {get_user_path} - Get User Details

Retrieves user information based on query parameters.

**Query Parameters:**
- `userId` (string, optional) - User identifier
- `email` (string, optional) - User email address
- `phoneNumber` (string, optional) - User phone number
- `providerName` (string, optional) - Name of the identity provider
- `providerUserId` (string, optional) - Provider-specific user identifier

**Request Example:**

* If {get_user_path} is `/user`
```
GET /user?email=user@example.com
GET /user?phoneNumber=+1234567890
GET /user?email=user@example.com&providerName=google&providerUserId=12345
```

**Response (2xx) - User Found:**
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

**Response (2xx) - User Not Found:**
```json
{}
```

**Important Notes:**
- Guardian can invoke this API with various combinations of query parameters
- In case a user exists, user service must return a value with key `userId` in response JSON
- If a user is not found, return an empty JSON object `{}` with status code 2xx
- Any response other than status code 2xx will be considered as an error
- The user service is responsible for determining the correct user based on the provided query parameters

### 2. POST {create_user_path} - Create User

Creates a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "phoneNumber": "+1234567890",
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
**Request Parameters**:
- `name` - Name of the external authentication provider (e.g., Google, Facebook).
- `providerUserId` - This is the unique identifier assigned to the user by the external authentication provider
- `data` - This contains the decoded and verified payload (claims) from the ID token provided by the external identity source.
- `credentials` - This holds the security tokens necessary to interact with the provider's API on the user's behalf, such as the access token and refresh token.

**Response (2xx):**
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

### 3. POST {authenticate_user_path} - Authenticate User

Validates username and password credentials

**Request Body:**
* The request body will contain exactly one identifier, which can be a username, email, or phone number
```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response (2xx):**
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
- Any non-2xx status code indicates authentication failure

### 4. POST {add_provider_path} - Add Provider to User

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
**Request parameters**

- `name` - Name of the external authentication provider (e.g., Google, Facebook).
- `providerUserId` - This is the unique identifier assigned to the user by the external authentication provider
- `data` - This contains the decoded and verified payload (claims) from the ID token provided by the external identity source.
- `credentials` - This holds the security tokens necessary to interact with the provider's API on the user's behalf, such as the access token and refresh token.


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
2. **Error Handling**: Return appropriate HTTP status codes (2xx for success, 400 for bad requests, 500 for server errors)
3. **Empty Responses**: Return `{}` with status 2xx when a user is not found (not an error condition)
4. **Security**: Use HTTPS (`is_ssl_enabled: true`) in production environments
5. **Response Format**: Always include `userId` in successful responses for create and authenticate endpoints

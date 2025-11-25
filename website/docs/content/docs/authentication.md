---
title: Authentication Flows
description: Learn about Guardian's authentication methods and flows
---

Guardian supports multiple authentication methods to suit different use cases and security requirements.

## Overview

Guardian provides three main authentication methods:

1. **Passwordless Authentication** - Secure authentication using OTP via SMS, Email, or other channels
2. **Password-based Authentication** - Traditional username/password authentication
3. **Social Authentication** - OAuth-based authentication with Google, Facebook, and other providers

## Passwordless Authentication

Passwordless authentication eliminates the need for passwords, reducing security risks and improving user experience.

### How it Works

1. **Initialize Flow**: User provides their contact information (email/phone)
2. **Send OTP**: Guardian sends a one-time password to the user
3. **Verify OTP**: User enters the OTP to complete authentication
4. **Issue Tokens**: Guardian issues access and refresh tokens

### Flows Supported

- `signin` - Sign in existing users only
- `signup` - Sign up new users only
- `signinup` - Sign in existing users or sign up new users

### Example Flow

```typescript
// Step 1: Initialize
const initResponse = await guardian.passwordless.init({
  flow: 'signinup',
  contacts: [{
    channel: 'sms',
    identifier: '+1234567890',
    template: {
      name: 'otp-template',
      params: { appName: 'MyApp' }
    }
  }]
});

// Step 2: Complete with OTP
const tokens = await guardian.passwordless.complete({
  state: initResponse.state,
  otp: '123456'
});
```

### Rate Limiting

- **OTP Retries**: 5 attempts before lockout
- **Resend Limit**: 5 resends per session
- **Cooldown**: 30 seconds between resends

## Password-based Authentication

Traditional username and password authentication for applications that require it.

### Sign Up

Create a new user account with username and password:

```typescript
const response = await guardian.signup({
  username: 'user@example.com',
  password: 'SecurePass123!',
  metaInfo: {
    ip: '192.168.1.1',
    deviceName: 'iPhone 12'
  }
});
```

### Sign In

Authenticate existing users:

```typescript
const response = await guardian.signin({
  username: 'user@example.com',
  password: 'SecurePass123!',
  metaInfo: {
    ip: '192.168.1.1',
    deviceName: 'iPhone 12'
  }
});
```

### Password Requirements

Configure password requirements for your tenant:

- Minimum length: 8 characters (configurable)
- Must contain: uppercase, lowercase, numbers, special characters
- Cannot be common passwords
- Password history: prevent reuse of last N passwords

## Social Authentication

Integrate with popular identity providers for seamless user experience.

### Supported Providers

- **Google** - OAuth 2.0 with OpenID Connect
- **Facebook** - OAuth 2.0
- **Custom OIDC** - Any OpenID Connect provider

### Google Authentication

```typescript
const response = await guardian.auth.google({
  idToken: 'google-id-token',
  flow: 'signinup'
});
```

### Facebook Authentication

```typescript
const response = await guardian.auth.facebook({
  accessToken: 'facebook-access-token',
  flow: 'signinup'
});
```

### Custom Identity Provider

```typescript
const response = await guardian.idp.connect({
  id_provider: 'custom-provider',
  identifier: 'authorization-code',
  identifier_type: 'code',
  code_verifier: 'pkce-code-verifier'
});
```

## Guest Authentication

Allow users to access your application without creating an account.

```typescript
const response = await guardian.guest.login({
  guest_identifier: 'device-id-or-app-instance-id',
  client_id: 'your-client-id',
  scopes: ['read', 'basic']
});
```

## Token Management

### Access Tokens

- Short-lived JWT tokens (default: 1 hour)
- Used to authenticate API requests
- Contains user information and scopes

### Refresh Tokens

- Long-lived tokens (default: 30 days)
- Used to obtain new access tokens
- Can be revoked for security

### Refresh Access Token

```typescript
const response = await guardian.refreshToken({
  refresh_token: 'refresh-token-value'
});
```

### Logout

```typescript
// Token logout (invalidates single token)
await guardian.logout({
  refresh_token: 'token',
  logout_type: 'token'
});

// Universal logout (invalidates all tokens)
await guardian.logout({
  refresh_token: 'token',
  logout_type: 'universal'
});
```

## User Flow Control

Block or unblock specific authentication methods for users.

### Block Flows

```typescript
await guardian.userFlow.block({
  userIdentifier: 'user@example.com',
  blockFlows: ['passwordless', 'social_auth'],
  reason: 'Suspicious activity detected',
  unblockedAt: Math.floor(Date.now() / 1000) + 86400 // 24 hours
});
```

### Unblock Flows

```typescript
await guardian.userFlow.unblock({
  userIdentifier: 'user@example.com',
  unblockFlows: ['passwordless', 'social_auth']
});
```

### Get Blocked Flows

```typescript
const response = await guardian.userFlow.getBlocked({
  userIdentifier: 'user@example.com'
});
```

## Best Practices

1. **Use Passwordless** - Most secure and user-friendly option
2. **Implement Rate Limiting** - Protect against brute force attacks
3. **Store Tokens Securely** - Use HTTP-only cookies or secure storage
4. **Refresh Tokens Regularly** - Keep sessions secure
5. **Monitor Authentication Attempts** - Detect and prevent abuse
6. **Use HTTPS** - Always use secure connections
7. **Implement Logout** - Properly clean up sessions

## Next Steps

- [Configure Authentication](/configuration/)
- [Security Best Practices](/security/)


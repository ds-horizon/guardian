---
title: Security Best Practices
description: Guardian security best practices and guidelines
---

Guardian is built with security as a top priority. Follow these best practices to ensure your implementation is secure.

## Token Security

### Access Tokens

- **Storage**: Never store access tokens in localStorage
- **Transport**: Always use HTTPS for token transmission
- **Expiry**: Use short expiration times (default: 1 hour)
- **Validation**: Always validate tokens on the backend

```typescript
// ✅ Good: Store in memory or HTTP-only cookies
const accessToken = response.accessToken;

// ❌ Bad: Don't store in localStorage
localStorage.setItem('accessToken', response.accessToken);
```

### Refresh Tokens

- **Storage**: Use HTTP-only cookies or secure storage
- **Rotation**: Enable automatic rotation on each use
- **Revocation**: Implement logout to revoke tokens
- **One-time Use**: Refresh tokens should only be used once

```typescript
// ✅ Good: HTTP-only cookie
document.cookie = `refreshToken=${token}; HttpOnly; Secure; SameSite=Strict`;

// ❌ Bad: Accessible JavaScript storage
sessionStorage.setItem('refreshToken', token);
```

## Password Security

### Password Requirements

Enforce strong password policies:

- Minimum 8 characters (recommend 12+)
- Mix of uppercase, lowercase, numbers, and special characters
- No common passwords or dictionary words
- No personal information (name, birthday, etc.)

```typescript
const passwordPolicy = {
  minLength: 12,
  requireUppercase: true,
  requireLowercase: true,
  requireNumbers: true,
  requireSpecialChars: true,
  preventCommonPasswords: true,
  preventReuse: 5
};
```

### Password Storage

Guardian handles password storage securely:

- Passwords are hashed using bcrypt with salt
- Never stored in plain text
- Never logged or transmitted except during initial setup

## OTP Security

### OTP Best Practices

- **Expiry**: Keep OTP validity short (5-15 minutes)
- **Length**: Use 6-digit OTPs minimum
- **Rate Limiting**: Limit attempts and resends
- **Secure Transmission**: Only send via verified channels

```typescript
const otpConfig = {
  length: 6,
  validity: 900, // 15 minutes
  maxRetries: 5,
  maxResends: 3,
  resendInterval: 60 // 1 minute
};
```

### Prevent OTP Abuse

- Implement CAPTCHA for multiple failed attempts
- Monitor and alert on suspicious OTP patterns
- Block users after excessive failed attempts
- Use state tokens to prevent replay attacks

## API Security

### Authentication

Always include the tenant-id header:

```typescript
const headers = {
  'tenant-id': 'your-tenant-id',
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${accessToken}`
};
```

### Rate Limiting

Implement rate limiting to prevent abuse:

- Login attempts: 5 per 15 minutes
- OTP requests: 5 per hour
- API calls: 100 per minute per user

### CORS Configuration

Configure CORS properly for browser-based applications:

```typescript
const corsConfig = {
  origin: ['https://myapp.com'],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization', 'tenant-id']
};
```

## OAuth 2.0 / OIDC Security

### PKCE (Proof Key for Code Exchange)

Always use PKCE for public clients:

```typescript
// Generate code verifier
const codeVerifier = generateRandomString(128);

// Generate code challenge
const codeChallenge = base64URLEncode(sha256(codeVerifier));

// Authorization request
const authUrl = `${guardianUrl}/authorize?
  response_type=code&
  client_id=${clientId}&
  redirect_uri=${redirectUri}&
  code_challenge=${codeChallenge}&
  code_challenge_method=S256`;
```

### State Parameter

Use state parameter to prevent CSRF attacks:

```typescript
const state = generateRandomString(32);
sessionStorage.setItem('oauth_state', state);

const authUrl = `${guardianUrl}/authorize?
  response_type=code&
  client_id=${clientId}&
  state=${state}`;
```

### Redirect URI Validation

- Whitelist all redirect URIs
- Use exact matching (no wildcards)
- Always use HTTPS in production
- Validate redirect URI on callback

## Social Authentication Security

### Provider Configuration

- Use official SDKs from providers
- Validate ID tokens server-side
- Check token expiration
- Verify token audience matches your client ID

```typescript
// ✅ Good: Validate ID token server-side
const response = await guardian.auth.google({
  idToken: googleIdToken, // Validated by Guardian
  flow: 'signinup'
});

// ❌ Bad: Trust client-side validation only
const user = parseJWT(googleIdToken); // Don't do this
```

### Token Validation

Always validate social provider tokens:

1. Verify signature using provider's public keys
2. Check token expiration
3. Validate audience claim
4. Verify issuer

## Session Security

### Session Management

- Implement session timeouts
- Extend sessions on activity
- Provide explicit logout
- Track active sessions

```typescript
const sessionConfig = {
  timeout: 3600, // 1 hour
  extendOnActivity: true,
  maxConcurrentSessions: 5,
  requireReauthForSensitive: true
};
```

### Universal Logout

Implement universal logout for security incidents:

```typescript
await guardian.logout({
  logout_type: 'universal', // Invalidates all tokens
  refresh_token: currentRefreshToken
});
```

## User Flow Control

### Blocking Suspicious Users

Block authentication methods for suspicious activity:

```typescript
await guardian.userFlow.block({
  userIdentifier: 'suspicious@example.com',
  blockFlows: ['passwordless', 'social_auth'],
  reason: 'Multiple failed login attempts',
  unblockedAt: Math.floor(Date.now() / 1000) + 86400 // 24 hours
});
```

### Monitoring

Monitor and alert on:

- Multiple failed login attempts
- Login from unusual locations
- Token refresh patterns
- Multiple concurrent sessions
- Account changes

## Network Security

### HTTPS Only

- Always use HTTPS in production
- Use HSTS headers
- Implement certificate pinning for mobile apps

### IP Whitelisting

Restrict API access by IP address:

```typescript
const ipWhitelist = [
  '192.168.1.0/24',
  '10.0.0.0/8'
];
```

### Request Validation

- Validate all input parameters
- Sanitize user input
- Use parameterized queries
- Implement CSRF protection

## Logging and Monitoring

### Security Logging

Log security-relevant events:

```typescript
const securityEvents = [
  'user.login',
  'user.logout',
  'user.failed_login',
  'token.refreshed',
  'token.revoked',
  'user.flow_blocked'
];
```

### Sensitive Data

- Never log passwords or tokens
- Mask sensitive information
- Use structured logging
- Implement log retention policies

```typescript
// ✅ Good: Mask sensitive data
logger.info('User login', { userId: user.id, email: maskEmail(user.email) });

// ❌ Bad: Log sensitive data
logger.info('User login', { password: user.password }); // Never do this
```

## Compliance

### GDPR

- Implement right to erasure
- Provide data portability
- Obtain consent for data processing
- Implement privacy by design

### Data Retention

- Define retention periods
- Implement automatic deletion
- Secure data disposal
- Audit data access

## Incident Response

### Security Incident Plan

1. **Detect**: Monitor for suspicious activity
2. **Contain**: Block affected users/tokens
3. **Investigate**: Analyze logs and patterns
4. **Recover**: Reset credentials, issue new tokens
5. **Learn**: Update security measures

### Emergency Actions

```typescript
// Revoke all tokens for a user
await guardian.admin.revokeAllTokens(userId);

// Block all authentication methods
await guardian.userFlow.block({
  userIdentifier: userId,
  blockFlows: ['passwordless', 'password', 'social_auth'],
  reason: 'Security incident'
});

// Force password reset
await guardian.admin.forcePasswordReset(userId);
```

## Security Checklist

- [ ] Use HTTPS for all communication
- [ ] Store tokens securely (HTTP-only cookies)
- [ ] Implement rate limiting
- [ ] Use PKCE for OAuth flows
- [ ] Validate redirect URIs
- [ ] Enable refresh token rotation
- [ ] Implement proper logging
- [ ] Monitor for suspicious activity
- [ ] Use strong password policies
- [ ] Keep Guardian SDK updated
- [ ] Implement session timeouts
- [ ] Use state parameter in OAuth
- [ ] Validate social provider tokens
- [ ] Implement CORS properly
- [ ] Regular security audits

## Next Steps

- [Performance Optimization](/performance/)
- [Configuration Guide](/configuration/)


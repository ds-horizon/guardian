---
title: Configuration
description: Learn how to configure Guardian for your application's specific requirements
---

Learn how to configure Guardian for your application's specific requirements.

## Tenant Configuration

Guardian uses a multi-tenant architecture. Each tenant has its own configuration, users, and settings.

### Tenant ID

Every API request requires a `tenant-id` header:

```typescript
const headers = {
  'tenant-id': 'your-tenant-id',
  'Content-Type': 'application/json'
};
```

## Client Configuration

Clients represent applications that use Guardian for authentication.

### Creating a Client

```typescript
const client = await guardian.admin.client.create({
  client_name: 'My Application',
  grant_types: ['authorization_code', 'refresh_token'],
  redirect_uris: ['https://myapp.com/callback'],
  response_types: ['code'],
  skip_consent: false
});
```

### Client Properties

- **client_id**: Unique identifier for the client
- **client_secret**: Secret key for authentication
- **grant_types**: OAuth 2.0 grant types supported
- **redirect_uris**: Allowed redirect URIs after authentication
- **response_types**: Response types supported (code, token)
- **skip_consent**: Whether to skip user consent screen

## Scope Configuration

Scopes define permissions and claims that can be granted to users.

### Creating a Scope

```typescript
const scope = await guardian.admin.scope.create({
  name: 'read:profile',
  display_name: 'Read Profile',
  description: 'Allows reading user profile information',
  claims: ['name', 'email', 'profile'],
  is_oidc: false
});
```

### Standard OIDC Scopes

- **openid**: Required for OIDC, includes `sub` claim
- **profile**: User profile information (`name`, `given_name`, `family_name`, etc.)
- **email**: Email address and verification status
- **phone**: Phone number and verification status
- **address**: Postal address

## Token Configuration

### Access Token

Configure access token settings:

- **Expiry**: Default 3600 seconds (1 hour)
- **Algorithm**: RS256 (RSA with SHA-256)
- **Claims**: Custom claims can be added

### Refresh Token

Configure refresh token settings:

- **Expiry**: Default 2592000 seconds (30 days)
- **Rotation**: Automatically rotates on use
- **Revocation**: Can be revoked manually

### Key Management

Generate new RSA key pairs for token signing:

```typescript
await guardian.admin.keys.generate({
  keySize: 4096,
  algorithm: 'RS256'
});
```

## OTP Configuration

Configure OTP (One-Time Password) settings for passwordless authentication.

### OTP Settings

- **Length**: 4-8 digits (default: 6)
- **Validity**: 5-30 minutes (default: 15 minutes)
- **Max Retries**: 1-10 attempts (default: 5)
- **Max Resends**: 1-10 resends (default: 5)
- **Resend Interval**: 30-300 seconds (default: 30 seconds)

### Channel Configuration

Configure SMS and Email channels for OTP delivery:

```typescript
const otpConfig = {
  sms: {
    provider: 'twilio',
    from: '+1234567890',
    template: 'Your OTP is: {{otp}}'
  },
  email: {
    provider: 'sendgrid',
    from: 'noreply@example.com',
    template: 'verification-email'
  }
};
```

## Rate Limiting

Configure rate limiting to protect against abuse.

### Authentication Rate Limits

- **Login Attempts**: 5 attempts per 15 minutes
- **OTP Requests**: 5 requests per hour
- **Token Refresh**: 10 requests per minute

### Custom Rate Limits

Set custom rate limits for specific endpoints or users.

## Security Configuration

### Password Policy

```typescript
const passwordPolicy = {
  minLength: 8,
  requireUppercase: true,
  requireLowercase: true,
  requireNumbers: true,
  requireSpecialChars: true,
  preventReuse: 5, // Last 5 passwords
  expiryDays: 90
};
```

### Session Configuration

```typescript
const sessionConfig = {
  maxConcurrentSessions: 5,
  sessionTimeout: 3600, // 1 hour
  extendOnActivity: true,
  secureOnly: true,
  sameSite: 'strict'
};
```

### IP Whitelisting

Restrict access to specific IP addresses or ranges:

```typescript
const ipWhitelist = [
  '192.168.1.0/24',
  '10.0.0.0/8',
  '203.0.113.0'
];
```

## Webhook Configuration

Configure webhooks to receive notifications for authentication events.

### Webhook Events

- `user.created`: New user registered
- `user.login`: User logged in
- `user.logout`: User logged out
- `token.refreshed`: Access token refreshed
- `otp.sent`: OTP sent to user
- `otp.verified`: OTP verified successfully

### Webhook Setup

```typescript
const webhook = {
  url: 'https://myapp.com/webhooks/guardian',
  events: ['user.created', 'user.login'],
  secret: 'webhook-secret-key',
  retryAttempts: 3
};
```

## Environment Configuration

### Development

```typescript
const guardianDev = new Guardian({
  tenantId: 'dev-tenant-id',
  apiUrl: 'https://guardian-api-dev.example.com',
  debug: true,
  timeout: 10000
});
```

### Production

```typescript
const guardianProd = new Guardian({
  tenantId: 'prod-tenant-id',
  apiUrl: 'https://guardian-api.example.com',
  debug: false,
  timeout: 5000,
  retryAttempts: 3
});
```

## Cache Configuration

Configure caching for improved performance:

```typescript
const cacheConfig = {
  enabled: true,
  ttl: 300, // 5 minutes
  provider: 'redis',
  connection: {
    host: 'localhost',
    port: 6379
  }
};
```

## Logging Configuration

Configure logging for debugging and monitoring:

```typescript
const loggingConfig = {
  level: 'info', // debug, info, warn, error
  format: 'json',
  destination: 'stdout',
  includeTimestamp: true,
  maskSensitiveData: true
};
```

## Best Practices

1. **Use Environment Variables**: Store sensitive configuration in environment variables
2. **Rotate Secrets Regularly**: Change client secrets and signing keys periodically
3. **Enable Debug Mode in Development**: Help troubleshoot issues during development
4. **Monitor Rate Limits**: Adjust rate limits based on usage patterns
5. **Use Webhooks**: Stay informed about important authentication events
6. **Test Configuration Changes**: Test in development before applying to production

## Next Steps

- [Security Best Practices](/security/)
- [Performance Optimization](/performance/)


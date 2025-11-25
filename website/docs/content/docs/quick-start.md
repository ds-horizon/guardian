---
title: Quick Start
description: Get up and running with Guardian in 5 minutes!
---

Get up and running with Guardian in 5 minutes!

## Step 1: Install SDK

```bash
npm install @guardian/sdk
```

## Step 2: Initialize

```typescript
import { Guardian } from '@guardian/sdk';

const guardian = new Guardian({
  tenantId: 'your-tenant-id',
  apiUrl: 'https://guardian-api.example.com'
});
```

## Step 3: Authenticate

### Option 1: Passwordless (Recommended)

```typescript
// Send OTP
const { state } = await guardian.passwordless.init({
  flow: 'signinup',
  contacts: [{
    channel: 'sms',
    identifier: '+1234567890'
  }]
});

// Verify OTP
const { accessToken } = await guardian.passwordless.complete({
  state,
  otp: '123456'
});
```

### Option 2: Password-based

```typescript
// Sign up
const { accessToken } = await guardian.signup({
  username: 'user@example.com',
  password: 'SecurePass123!'
});

// Sign in
const { accessToken } = await guardian.signin({
  username: 'user@example.com',
  password: 'SecurePass123!'
});
```

### Option 3: Social Login

```typescript
// Google
const { accessToken } = await guardian.auth.google({
  idToken: googleIdToken
});

// Facebook
const { accessToken } = await guardian.auth.facebook({
  accessToken: fbAccessToken
});
```

## Step 4: Use Access Token

```typescript
// Make authenticated API call
fetch('https://your-api.com/protected', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
```

## Step 5: Refresh Token

```typescript
// Before token expires
const { accessToken: newToken } = await guardian.refreshToken({
  refresh_token: refreshToken
});
```

## Next Steps

- [Full Authentication Guide](/authentication/)
- [Configuration Options](/configuration/)
- [Security Best Practices](/security/)

## Example App

Check out our example applications:

- [React Example](https://github.com/guardian/examples/react)
- [Next.js Example](https://github.com/guardian/examples/nextjs)
- [Node.js Backend](https://github.com/guardian/examples/nodejs)

## Get Help

- 📚 [Full Documentation](/getting-started/)
- 💬 [Community Discord](https://discord.gg/guardian)
- 🐛 [Report Issues](https://github.com/guardian/issues)
- 📧 [Email Support](mailto:support@guardian.com)


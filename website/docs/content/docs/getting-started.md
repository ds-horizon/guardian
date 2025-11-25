---
title: Getting Started with Guardian
description: Complete guide to setting up and using Guardian authentication service
---

Guardian is a powerful authentication and authorization service that provides multiple authentication methods including passwordless, password-based, and social login.

### Prerequisites

* **Docker** ([Download Docker Desktop](https://www.docker.com/products/docker-desktop/))
* **Docker Compose CLI** ([Install instructions](https://docs.docker.com/compose/install/))
* **Maven**
* **Java 17**

You can verify the installations by running the following commands in your terminal:

```bash
docker --version
mvn --version
java -version
```

Ensure that **Java 17** is the active version in use. Maven should also be configured to use Java 17 - you can verify this by checking that `mvn --version` shows Java 17 in its output.

Additionally, make sure the following ports are free and not in use by other services:

* `3306` – MySQL
* `6379` – Redis
* `8080` – Application server
* `6000` – Auxiliary services/API

These ports are required for the application to run without conflicts.

1. Clone the repository:
```bash
git clone https://github.com/ds-horizon/guardian.git
cd guardian
```

2. Start Guardian:
```bash
./quick-start.sh
```

3. Test the setup with a passwordless flow:

```bash
# Initialize passwordless authentication
curl --location 'localhost:8080/v1/passwordless/init' \
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \
--data '{
  "flow": "signinup",
  "responseType": "token",
  "contacts": [{
    "channel": "sms",
    "identifier": "9999999999"
  }],
  "metaInfo": {
    "ip": "127.0.0.1",
    "location": "localhost",
    "deviceName": "localhost",
    "source": "app"
  }
}'

# Complete authentication (using mock OTP for development)
curl --location 'localhost:8080/v1/passwordless/complete' \
--header 'Content-Type: application/json' \
--header 'tenant-id: tenant1' \
--data '{
  "state": "<state-from-init-response>",
  "otp": "999999"
}'
```

## Installation

Install Guardian SDK in your application:

```bash
npm install @guardian/sdk
# or
yarn add @guardian/sdk
```

## Initialize Guardian

Configure Guardian with your tenant credentials:

```typescript
import { Guardian } from '@guardian/sdk';

const guardian = new Guardian({
  tenantId: 'your-tenant-id',
  apiUrl: 'https://guardian-api.dreamsportslabs.com'
});
```

## Implement Authentication

Choose your preferred authentication method:

### Passwordless Authentication

```typescript
// Initialize passwordless flow
const { state } = await guardian.passwordless.init({
  flow: 'signinup',
  contacts: [{
    channel: 'sms',
    identifier: '+1234567890',
    template: {
      name: 'otp-template',
      params: {}
    }
  }]
});

// Complete with OTP
const { accessToken, refreshToken } = await guardian.passwordless.complete({
  state: state,
  otp: '123456'
});
```

### Password-based Authentication

```typescript
// Sign up
const { accessToken } = await guardian.signup({
  username: 'user@example.com',
  password: 'securePassword123'
});

// Sign in
const { accessToken } = await guardian.signin({
  username: 'user@example.com',
  password: 'securePassword123'
});
```

### Social Authentication

```typescript
// Google Sign-in
const { accessToken } = await guardian.auth.google({
  idToken: 'google-id-token'
});

// Facebook Sign-in
const { accessToken } = await guardian.auth.facebook({
  accessToken: 'facebook-access-token'
});
```

## Next Steps

- [Explore Authentication Flows](/authentication/)
- [Configure Guardian](/configuration/)
- [Security Best Practices](/security/)

## Need Help?

- Check out our [API Documentation](/api-docs/)
- Visit our [GitHub Repository](https://github.com/ds-horizon/guardian)
- Contact support at support@guardian.com


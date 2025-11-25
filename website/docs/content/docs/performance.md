---
title: Performance Optimization
description: Learn how to optimize Guardian integration for better performance and scalability
---

Learn how to optimize Guardian integration for better performance and scalability.

## Token Caching

### Access Token Caching

Cache access tokens to reduce API calls:

```typescript
class TokenCache {
  private cache: Map<string, { token: string, expiresAt: number }>;

  constructor() {
    this.cache = new Map();
  }

  get(key: string): string | null {
    const entry = this.cache.get(key);
    if (!entry || entry.expiresAt < Date.now()) {
      return null;
    }
    return entry.token;
  }

  set(key: string, token: string, expiresIn: number): void {
    this.cache.set(key, {
      token,
      expiresAt: Date.now() + (expiresIn * 1000)
    });
  }
}
```

### JWKS Caching

Cache JSON Web Key Sets for token validation:

```typescript
const jwksCache = {
  maxAge: 3600000, // 1 hour
  cacheTime: 600000 // 10 minutes
};
```

## Connection Pooling

Reuse HTTP connections for better performance:

```typescript
import axios from 'axios';
import { Agent } from 'https';

const httpsAgent = new Agent({
  keepAlive: true,
  maxSockets: 50,
  maxFreeSockets: 10,
  timeout: 60000,
});

const guardianClient = axios.create({
  baseURL: 'https://guardian-api.example.com',
  httpsAgent,
  timeout: 5000,
});
```

## Request Optimization

### Batch Requests

Minimize API calls by batching operations:

```typescript
// ❌ Bad: Multiple sequential requests
for (const user of users) {
  await guardian.userFlow.block({ userIdentifier: user.id });
}

// ✅ Good: Batch operation (if supported)
await guardian.userFlow.batchBlock(users.map(u => ({ userIdentifier: u.id })));
```

### Parallel Requests

Use Promise.all for independent operations:

```typescript
// ❌ Bad: Sequential requests
const user = await guardian.getUser(userId);
const blockedFlows = await guardian.userFlow.getBlocked(userId);
const tokens = await guardian.getActiveTokens(userId);

// ✅ Good: Parallel requests
const [user, blockedFlows, tokens] = await Promise.all([
  guardian.getUser(userId),
  guardian.userFlow.getBlocked(userId),
  guardian.getActiveTokens(userId)
]);
```

## Caching Strategy

### Redis Configuration

Use Redis for distributed caching:

```typescript
import Redis from 'ioredis';

const redis = new Redis({
  host: 'localhost',
  port: 6379,
  password: 'secure_password',
  db: 0,
  retryStrategy: (times) => {
    return Math.min(times * 50, 2000);
  },
  maxRetriesPerRequest: 3,
});

// Cache user data
await redis.setex(`user:${userId}`, 300, JSON.stringify(userData));

// Retrieve cached data
const cachedUser = await redis.get(`user:${userId}`);
```

### Cache Invalidation

Implement proper cache invalidation:

```typescript
class UserCache {
  async invalidate(userId: string): Promise<void> {
    await Promise.all([
      redis.del(`user:${userId}`),
      redis.del(`user:blocked_flows:${userId}`),
      redis.del(`user:active_sessions:${userId}`)
    ]);
  }

  async invalidatePattern(pattern: string): Promise<void> {
    const keys = await redis.keys(pattern);
    if (keys.length > 0) {
      await redis.del(...keys);
    }
  }
}
```

## Load Balancing

### Horizontal Scaling

Deploy multiple Guardian instances behind a load balancer.

## Rate Limiting

### Client-Side Rate Limiting

Implement client-side rate limiting to prevent unnecessary requests:

```typescript
class RateLimiter {
  private requests: number[] = [];
  private maxRequests: number;
  private windowMs: number;

  constructor(maxRequests: number, windowMs: number) {
    this.maxRequests = maxRequests;
    this.windowMs = windowMs;
  }

  async acquire(): Promise<boolean> {
    const now = Date.now();
    this.requests = this.requests.filter(time => now - time < this.windowMs);

    if (this.requests.length >= this.maxRequests) {
      return false;
    }

    this.requests.push(now);
    return true;
  }
}

const rateLimiter = new RateLimiter(100, 60000); // 100 requests per minute

async function callGuardianAPI() {
  if (!await rateLimiter.acquire()) {
    throw new Error('Rate limit exceeded');
  }
  return await guardian.api.call();
}
```

## Best Practices

### 1. Token Refresh Strategy

Proactively refresh tokens before expiry:

```typescript
function shouldRefreshToken(expiresAt: number): boolean {
  const timeUntilExpiry = expiresAt - Date.now();
  const bufferTime = 5 * 60 * 1000; // 5 minutes
  return timeUntilExpiry < bufferTime;
}

if (shouldRefreshToken(tokenExpiresAt)) {
  const newToken = await guardian.refreshToken(refreshToken);
}
```

### 2. Graceful Degradation

Handle Guardian service unavailability:

```typescript
async function authenticateWithFallback(credentials) {
  try {
    return await guardian.authenticate(credentials);
  } catch (error) {
    if (error.code === 'SERVICE_UNAVAILABLE') {
      // Fallback to cached credentials (with caution)
      return await getCachedAuth(credentials);
    }
    throw error;
  }
}
```

### 3. Request Timeout

Set appropriate timeouts:

```typescript
const guardianClient = axios.create({
  timeout: 5000, // 5 seconds
  timeoutErrorMessage: 'Guardian API timeout'
});
```

### 4. Compression

Enable gzip compression for API requests:

```typescript
import compression from 'compression';

app.use(compression({
  filter: (req, res) => {
    if (req.headers['x-no-compression']) {
      return false;
    }
    return compression.filter(req, res);
  },
  level: 6
}));
```

## Performance Checklist

- [ ] Implement token caching
- [ ] Use connection pooling
- [ ] Enable HTTP keep-alive
- [ ] Implement Redis caching
- [ ] Optimize database queries
- [ ] Use CDN for static assets
- [ ] Implement rate limiting
- [ ] Enable compression
- [ ] Monitor performance metrics
- [ ] Set up health checks
- [ ] Implement load balancing
- [ ] Use parallel requests
- [ ] Proactive token refresh
- [ ] Proper error handling
- [ ] Regular performance audits

## Benchmarks

Expected performance for optimized implementation:

| Operation | Latency (p50) | Latency (p95) | Throughput |
|-----------|---------------|---------------|------------|
| Token Validation | 5ms | 15ms | 10,000 req/s |
| Login (cached) | 50ms | 150ms | 1,000 req/s |
| Login (no cache) | 200ms | 500ms | 500 req/s |
| Token Refresh | 30ms | 100ms | 2,000 req/s |
| OTP Send | 100ms | 300ms | 500 req/s |

## Next Steps

- [Security Best Practices](/security/)
- [Configuration Guide](/configuration/)


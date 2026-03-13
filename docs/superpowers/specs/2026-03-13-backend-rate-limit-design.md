# Backend Rate Limit Design

Date: 2026-03-13
Scope: `backend/api-gateway`
Status: Draft approved for spec review

## Summary

This design adds application-level rate limiting to `api-gateway`, the sole HTTP entry point of the backend. The first implementation uses Redis-backed distributed counters and a small set of route-specific rules to protect authentication endpoints, AI endpoints, upload-signing endpoints, and admin-heavy operations.

The design intentionally stays small:

- Apply rate limiting only at `api-gateway`
- Use Redis, which is already required by the gateway
- Support two enforcement types: request frequency and concurrent request limits
- Support three identity dimensions: `IP`, `USER`, `ACCOUNT`
- Support a global dry-run mode via `app.rate-limit.log-only`

The design does not add infrastructure outside the application, and does not attempt to solve general bot protection, CAPTCHA, or WAF policy management.

## Context

The backend already has the right placement for centralized rate limiting:

- `api-gateway` is the only HTTP entry point
- Spring Security is already used for authentication and authorization
- JWT authentication stores `userId` in the request context after token validation
- Redis is already configured in the gateway
- The gateway proxies high-cost AI requests and upload-signing requests

This means rate limiting can be enforced before requests reach Dubbo services, storage, or the AI service.

## Goals

- Reduce abuse and accidental overload on public and high-cost endpoints
- Keep the first implementation simple enough to operate and debug
- Centralize rules and enforcement so new endpoints are less likely to bypass protection
- Preserve the current API response style and observability conventions
- Allow safe rollout with a dry-run mode before hard enforcement

## Non-Goals

- Replacing external traffic protection such as CDN, WAF, or reverse-proxy rate limiting
- Building a generic rate-limiting framework for every future service
- Implementing sliding-window precision or token-bucket shaping in the first version
- Applying strict limits to every read endpoint from day one

## Recommended Architecture

Rate limiting is implemented only in `api-gateway`.

The implementation is split into six units:

1. `RateLimitRuleProperties`
   Loads rule definitions and top-level switches from configuration.
2. `RateLimitKeyResolver`
   Resolves the identity value for a rule, such as client IP, authenticated user ID, or login account identifier.
3. `RedisRateLimitService`
   Performs atomic Redis operations for fixed-window frequency limits and concurrent request limits.
4. `CachedBodyRequestFactory`
   Wraps only request-body-inspected routes in a replayable request wrapper so account-based extraction never consumes the body seen by controllers.
5. `RateLimitLeaseManager`
   Owns concurrency lease lifecycle for synchronous and asynchronous requests, including async listener registration.
6. `RateLimitFilter`
   Matches rules, checks limits, emits logs, and either lets the request proceed or returns `429`.

These units should remain small and independently testable:

- `RateLimitRuleProperties` knows configuration shape only
- `RateLimitKeyResolver` knows request-to-identity mapping only
- `RedisRateLimitService` knows Redis key behavior only
- `CachedBodyRequestFactory` knows request wrapping only
- `RateLimitLeaseManager` knows lease attachment and release semantics only
- `RateLimitFilter` knows orchestration and HTTP behavior only

## Filter Placement

`RateLimitFilter` is registered in the Spring Security chain after `JwtAuthenticationFilter`.

Reason:

- Anonymous endpoints still work because IP-based rules do not need JWT
- Authenticated endpoints can use `userId` because JWT processing has already populated request attributes
- Authorization decisions remain separate from rate-limiting decisions

The filter must return the response directly when a request is blocked. It must not rely on controller advice to translate filter-layer failures.

## Rule Model

The first version uses explicit route rules defined in configuration. Each rule contains:

- `ruleId`: stable identifier used in Redis keys and logs
- `enabled`: whether the rule is active
- `pathPattern`: Spring `PathPattern` syntax, for example `/api/auth/user/login` or `/api/admin/foods/{id}/image/upload-url`
- `httpMethod`: request method or `*`
- `dimension`: one of `IP`, `USER`, `ACCOUNT`
- `windowSeconds`: fixed window size for frequency limiting
- `maxRequests`: max requests allowed in one window
- `concurrencyLimit`: optional max in-flight requests for the same identity
- `concurrencyLeaseSeconds`: required when `concurrencyLimit` is set

Rules are matched with Spring's `PathPatternParser`. Ant-only wildcard semantics are not used.

Rules are evaluated independently. A request may match more than one rule. If any matching rule blocks, the request is blocked.

This is intentional for login endpoints, which need both IP-level and account-level protection.

Rule evaluation order is deterministic:

- matched rules preserve configuration order
- all frequency checks run before any concurrency acquisition
- the first blocking rule in configuration order becomes the canonical decision
- only the canonical decision is used for `Retry-After`, `X-RateLimit-Rule`, and the primary structured warning log

## Supported Dimensions

### IP

Used for anonymous or externally exposed entry points:

- login
- registration
- WeChat login

Identity source:

- `X-Forwarded-For` first IP when present
- otherwise `X-Real-IP`
- otherwise `request.getRemoteAddr()`

Trust assumption:

- forwarded headers are trusted only when the gateway is deployed behind a trusted reverse proxy
- if that assumption is not true in an environment, IP extraction must ignore forwarded headers and fall back to the direct remote address

### USER

Used for authenticated endpoints.

Identity source:

- `userId` from the request attribute written by JWT authentication

If a `USER` rule matches but no authenticated user ID is available, the rule is treated as non-applicable and skipped.

### ACCOUNT

Used only for login/password-sensitive endpoints.

Identity source:

- `/api/auth/user/login`: request body `email`
- `/api/auth/admin/login`: request body `username`
- future account-sensitive routes may define an extractor explicitly

If the identifier is absent or blank, the rule is skipped and normal request validation continues.

To make this safe at filter level:

- only routes with an `ACCOUNT` rule are wrapped by `CachedBodyRequestFactory`
- the wrapper eagerly copies the request body once and exposes a replayable input stream for downstream MVC handling
- body parsing is limited to `POST` requests with `application/json`
- parse failure, invalid JSON, or unsupported content type does not block the request; it only makes the `ACCOUNT` rule non-applicable and emits a debug log
- controllers continue to own request validation and error responses

## Endpoint Tiers and Initial Rules

The first rollout covers only high-value endpoints.

All initial rules use `httpMethod: POST`.

### Authentication Public

- `/api/auth/admin/login`
  - `IP`: `5 / 60s`
  - `ACCOUNT`: `10 / 1800s`
- `/api/auth/user/login`
  - `IP`: `10 / 60s`
  - `ACCOUNT`: `20 / 1800s`
- `/api/auth/wechat-login`
  - `IP`: `20 / 60s`
- `/api/auth/register`
  - `IP`: `3 / 60s`
  - `IP`: `10 / 86400s`

### Authentication Sensitive

- `/api/auth/change-password`
  - `USER`: `5 / 900s`

### AI Heavy

- `/api/ai/recommend/recipes`
  - `USER`: `10 / 60s`
- `/api/ai/chat/nutrition`
  - `USER`: `10 / 60s`
- `/api/ai/chat/nutrition/stream`
  - `USER`: `5 / 60s`
  - concurrency: `1`, lease TTL: `300s`
- `/api/ai/recognition/analyze`
  - `USER`: `10 / 600s`
  - concurrency: `2`, lease TTL: `90s`

### Upload Heavy

- `/api/files/upload-url`
  - `USER`: `20 / 60s`
- `/api/users/me/avatar/upload-url`
  - `USER`: `10 / 60s`
- `/api/admin/foods/{id}/image/upload-url`
  - `USER`: `20 / 60s`

### Admin Heavy

- `/api/admin/foods/batch-import`
  - `USER`: `1 / 60s`

Other endpoints are out of scope for the first rollout. A broad default rule is intentionally avoided to reduce false positives during initial adoption.

## Redis Key Design

Two key families are used.

### Frequency Keys

Format:

`rl:fw:{ruleId}:{dimension}:{value}:{windowStart}`

Examples:

- `rl:fw:user-login:IP:1.2.3.4:1710241320`
- `rl:fw:user-login:ACCOUNT:test@example.com:1710241320`
- `rl:fw:ai-chat:USER:123:1710241320`

Behavior:

- `windowStart` is the aligned start timestamp of the current fixed window
- request count is incremented atomically
- TTL is set to `windowSeconds + safetyBufferSeconds`

### Concurrency Keys

Format:

`rl:cc:{ruleId}:{dimension}:{value}`

Examples:

- `rl:cc:ai-chat-stream:USER:123`
- `rl:cc:ai-recognition:USER:123`

Behavior:

- increment before request execution
- decrement when request completes, errors, or times out
- key TTL is refreshed to `concurrencyLeaseSeconds` on increment as a leak-safety fallback

## Redis Algorithms

### Frequency Limit

The first version uses fixed-window counters with atomic Redis operations.

Preferred implementation:

- Lua script that increments the counter, initializes TTL when first seen, and returns current count plus remaining TTL

Acceptable first implementation:

- `INCR` followed by conditional `EXPIRE`

Lua is preferred because it avoids edge cases across concurrent requests and makes `Retry-After` easier to compute consistently.

### Concurrency Limit

Concurrent limits use Redis counters:

1. Increment key
2. If result exceeds the limit, decrement immediately and block
3. If result is within the limit, continue and register a release callback

Release behavior:

- synchronous requests: release in `finally`
- async or streaming requests: release through `RateLimitLeaseManager` via an `AsyncListener`

The implementation must guard against double release by tracking acquisition in the request context.

Rollback behavior:

- frequency rules are checked first, so no lease is acquired before frequency checks pass
- concurrency rules are then acquired in configuration order
- if concurrency acquisition for a later rule fails, all leases acquired earlier in the same request are released immediately before returning the block response

## Request Processing Flow

For every HTTP request:

1. If `app.rate-limit.enabled` is `false`, do nothing
2. Find all enabled rules matching path and method
3. For each rule, resolve the identity value
4. Skip rules with no applicable identity
5. Evaluate all frequency limits in configuration order
6. If one or more frequency rules would block, pick the first blocking rule as the canonical decision
7. If no frequency rule blocks, evaluate concurrency limits in configuration order and acquire leases through `RateLimitLeaseManager`
8. If a concurrency rule blocks, release any earlier leases acquired for this request and use that rule as the canonical decision
9. If the canonical decision blocks:
   - emit a structured log
   - if `log-only` is `true`, continue request processing
   - otherwise write a `429` JSON response and stop
10. If concurrency leases were acquired and the request proceeds, release them when the request lifecycle completes

Only the canonical decision is surfaced in response headers. Additional non-canonical matching rules are not emitted as separate headers or logs in v1.

## Response Contract

Blocked requests return HTTP `429 Too Many Requests`.

Response body:

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后重试",
  "data": null
}
```

Response headers:

- `Retry-After`: remaining seconds for the blocking rule when available
- `X-RateLimit-Rule`: `ruleId` of the rule that blocked or would have blocked

`Retry-After` behavior:

- for fixed-window frequency limits, derive the value from the aligned window end time, not from Redis TTL, because Redis TTL includes the safety buffer
- for concurrency-limit blocks, omit `Retry-After` in v1 because release timing depends on in-flight request completion rather than a deterministic window end

The filter writes this response directly and does not throw into controller exception handling.

## Configuration Shape

Suggested configuration:

```yaml
app:
  rate-limit:
    enabled: false
    log-only: true
    safety-buffer-seconds: 5
    rules:
      - rule-id: admin-login-ip
        enabled: true
        path-pattern: /api/auth/admin/login
        http-method: POST
        dimension: IP
        window-seconds: 60
        max-requests: 5
      - rule-id: admin-login-account
        enabled: true
        path-pattern: /api/auth/admin/login
        http-method: POST
        dimension: ACCOUNT
        window-seconds: 1800
        max-requests: 10
      - rule-id: ai-chat-stream-user
        enabled: true
        path-pattern: /api/ai/chat/nutrition/stream
        http-method: POST
        dimension: USER
        window-seconds: 60
        max-requests: 5
        concurrency-limit: 1
        concurrency-lease-seconds: 300
```

The gateway should fail fast on startup if a rule is malformed, for example:

- missing `ruleId`
- invalid `pathPattern`
- unsupported `httpMethod`
- non-positive `windowSeconds`
- non-positive `maxRequests`
- unsupported `dimension`
- non-positive `concurrencyLimit`
- missing or non-positive `concurrencyLeaseSeconds` when `concurrencyLimit` is configured

## Log-Only Mode

`app.rate-limit.log-only` enables shadow evaluation.

When enabled:

- matching rules are still evaluated through the normal canonical-decision flow defined in this spec
- logs are emitted exactly as if the request would be blocked
- the request is not rejected

This mode is used to validate thresholds before enforcement in production.

## Logging and Observability

The first version reuses the existing logging stack.

Every blocked or would-block event emits a structured warning with:

- `ruleId`
- `path`
- `method`
- `dimension`
- `dimensionValue`
- `userId`
- `clientIp`
- `limit`
- `windowSeconds`
- `retryAfterSeconds`
- `logOnly`

Message shape:

`rate_limit_decision action=block|would_block ruleId=... path=...`

The request should continue to produce normal access logs, so rate-limit decisions can be correlated with existing `traceId`, `userId`, and client IP fields.

## Error Handling

Rate-limiting infrastructure failures must fail open in the first version.

Reason:

- availability is more important than strict enforcement during the initial rollout
- Redis issues should not cause full API outage for normal traffic

Behavior on Redis/script failure:

- emit an error log with request context and rule information
- skip rate-limit enforcement for that request

This fail-open policy applies only to rate-limit infrastructure failure, not to a normal over-limit decision.

## Streaming and Async Edge Cases

`/api/ai/chat/nutrition/stream` requires special concurrency release handling.

Requirements:

- the concurrency slot is acquired before controller execution
- `RateLimitLeaseManager` owns async release and registers an `AsyncListener` after `chain.doFilter` when async processing starts
- release happens when the async response fully completes, errors, or times out
- abnormal client disconnects must still trigger release via async lifecycle hooks when available
- fallback TTL on concurrency keys limits damage from unreleased counters
- if async processing never starts, the request is treated as synchronous and leases are released in filter `finally`

The non-streaming AI recognition endpoint can use standard `finally` release semantics.

## Testing Strategy

The implementation must include:

### Unit Tests

- rule matching by path and method
- key resolution for `IP`, `USER`, `ACCOUNT`
- account-body replay behavior for login requests
- account-body parse-failure behavior falls back to non-applicable rule
- fixed-window counting behavior
- `Retry-After` calculation
- concurrency acquire/reject/release behavior
- canonical blocking-rule selection when multiple rules match
- rollback of earlier leases when a later concurrency rule blocks
- `log-only` behavior

### Web Layer Tests

- anonymous login request blocked by IP rule
- authenticated AI request blocked by `USER` rule
- `429` response body and headers
- concurrency-blocked response omits `Retry-After`
- `log-only` mode allows requests through
- malformed configuration fails application startup

### Integration-Focused Scenarios

- Redis unavailable during evaluation results in fail-open behavior
- streaming endpoint releases concurrency slot after completion

## Rollout Plan

Rollout occurs in three phases.

### Phase 1: Disabled

- ship code with `enabled=false`
- verify configuration binding and startup behavior

### Phase 2: Log-Only

- set `enabled=true`
- keep `log-only=true`
- observe logs for at least one release cycle
- inspect false positives on login and AI endpoints

### Phase 3: Enforcement

- set `log-only=false`
- enable only the first-batch rules listed in this spec
- monitor logs and support feedback

Any additional endpoint coverage should happen only after Phase 3 stabilizes.

## Why This Design

This design is recommended because it matches the existing backend architecture and minimizes moving parts:

- centralized at the only HTTP entry point
- uses Redis already required by the gateway
- protects the highest-risk endpoints first
- keeps implementation and operations understandable
- supports safe rollout before hard enforcement

It deliberately avoids introducing annotation sprawl, service-level duplication, or dependency on new external gateways.

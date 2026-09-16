# Add Rate Limiting to /api/auth Endpoints

> **Execution instruction:** When a step below is completed, mark its checkbox `[x]` in this file immediately before moving on to the next step. Do not batch status updates until the end.

## Context

`/api/auth/register` and `/api/auth/login` are `permitAll()` in `SecurityConfig` (unauthenticated, pre-auth endpoints), which makes them a brute-force / credential-stuffing target. There is currently no rate limiting anywhere in the app. This change adds per-IP rate limiting to all `/api/auth/**` requests so repeated rapid requests (e.g. password guessing) get rejected with HTTP 429, following the existing filter pattern already used for JWT auth.

## Approach

- Use **Bucket4j** (`com.bucket4j:bucket4j-core`) for token-bucket rate limiting — confirmed with the user as a new Maven dependency (per CLAUDE.md, new deps require confirmation; this has been given).
- Limit: **5 requests per minute per client IP**, applied only to `/api/auth/**` paths.
- On exceeding the limit: respond `429 Too Many Requests` with a small JSON error body (e.g. `{"error": "Too many requests, please try again later."}`) and do not call `chain.doFilter`.
- Key by client IP: prefer `X-Forwarded-For` (first entry) if present, else `request.getRemoteAddr()`.
- Buckets are kept in-memory (`ConcurrentHashMap<String, Bucket>`), consistent with the app being single-instance/dev-oriented (H2 in-memory DB, no external cache infra).

## Implementation

- [x] 1. **`backend/pom.xml`**: add dependency
   ```xml
   <dependency>
       <groupId>com.bucket4j</groupId>
       <artifactId>bucket4j-core</artifactId>
       <version>8.10.1</version>
   </dependency>
   ```

- [x] 2. **New filter**: `backend/src/main/java/com/technizer/taskapi/auth/RateLimitFilter.java`
   - `@Component`, extends `OncePerRequestFilter`, mirrors `JwtAuthFilter`'s shape/style (constructor injection, no field injection).
   - Only applies limiting when `request.getRequestURI().startsWith("/api/auth/")`; otherwise passes through immediately.
   - Maintains `ConcurrentHashMap<String, Bucket>` keyed by client IP; creates a new bucket per key on first use with a `Bandwidth` of 5 tokens refilled every 1 minute (`Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)))`).
   - `tryConsume(1)` — if it fails, set `response.setStatus(429)`, `response.setContentType("application/json")`, write the JSON error body, and return (skip `chain.doFilter`).
   - Extract client IP via a small helper checking `X-Forwarded-For` header first, falling back to `getRemoteAddr()`.

- [x] 3. **`backend/src/main/java/com/technizer/taskapi/config/SecurityConfig.java`**:
   - Inject `RateLimitFilter` via constructor alongside `JwtAuthFilter`.
   - Register it earlier in the chain: `.addFilterBefore(rateLimitFilter, JwtAuthFilter.class)` (so rate-limited requests are rejected before JWT/auth processing), keeping the existing `.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` line.
   - **Note:** `JwtAuthFilter` must be registered via `addFilterBefore` *before* it is used as the reference class for `rateLimitFilter`'s `addFilterBefore` — Spring Security doesn't know a custom filter's order until it's been added to the chain, so the `jwtAuthFilter` line must come first (order in the method chain matters, independent of runtime execution order).

- [x] 4. **Tests**: `backend/src/test/java/com/technizer/taskapi/auth/RateLimitFilterTest.java`
   - New JUnit 5 test class (mirrors `main/java` structure per CLAUDE.md testing requirement).
   - Cases: requests under the limit pass through to `chain.doFilter`; the 6th request within a minute in the window returns 429 and does not call the chain; requests to non-`/api/auth/**` paths always pass through regardless of count; different IPs get independent buckets.
   - Do not remove/modify existing placeholder tests (`AuthServiceTest`, etc.) per CLAUDE.md.

- [x] 5. **Docs**: `backend/CLAUDE.md`
   - Update the "Architecture" section's auth-flow bullet to mention `RateLimitFilter`: it runs in the security chain (registered before `JwtAuthFilter`), enforces 5 requests/minute per client IP on `/api/auth/**`, and returns `429` when exceeded.
   - Optionally note the new `bucket4j-core` Maven dependency added for this.

## Verification

- [x] `mvn test` — new `RateLimitFilterTest` passes, no existing tests broken.
- [x] `mvn spring-boot:run`, then from a shell send 6 rapid `POST http://localhost:8080/api/auth/login` requests (e.g. with `curl` in a loop) with a bad/valid body — confirm the first 5 return normal responses (200/401 depending on credentials) and the 6th returns `429` with the JSON error body.
- [x] Confirm `/api/tasks/**` or other authenticated endpoints are unaffected (no rate limiting applied there).
- [x] Wait 60s and confirm the bucket refills (a subsequent request succeeds again).

# Backend — CLAUDE.md

Spring Boot REST API for the Task Manager app. See the root `CLAUDE.md` for the
overall project/tech stack; this file covers backend-specific detail.

## Commands

Run from `backend/`:

```bash
mvn spring-boot:run                          # start API on http://localhost:8080
mvn test                                     # run all tests
mvn test -Dtest=TaskServiceTest              # run a single test class
mvn test -Dtest=TaskServiceTest#methodName   # run a single test method
mvn package                                  # build jar
```

H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:taskdb`, user `sa`, blank password).

## Architecture

Package-by-feature under `src/main/java/com/technizer/taskapi/`: `auth/`, `task/`,
`user/`, `config/`, `exception/`.

- **Auth flow:** `AuthController` → `AuthService` (registers/authenticates against
  `UserRepository`, hashes passwords with `BCryptPasswordEncoder`) → `JwtUtil` issues
  a JWT whose subject is the user id. `JwtAuthFilter` runs before
  `UsernamePasswordAuthenticationFilter` on every request, extracts the bearer token,
  and populates the `SecurityContext` from the user id in the token. Auth is fully
  stateless — no server-side session.
- **Authorization:** `SecurityConfig` permits `/api/auth/**` and `/h2-console/**` and
  requires authentication for everything else. There are no role/permission checks —
  any authenticated user is equivalent; per-resource ownership must be enforced
  manually in the service layer.
- **Tasks:** `TaskController` delegates to `TaskService`, which loads/saves via
  `TaskRepository` (Spring Data JPA). The authenticated user's id is threaded through
  as `ownerId` on create/list, but `markCompleted()` and `delete()` currently look up
  tasks by id alone and do **not** verify the task belongs to the caller (see Known
  issues below).
- **Config:** `src/main/resources/application.properties` holds the JWT secret/expiry
  and H2 datasource settings — demo values, not production config.

## Coding standards

- Constructor injection only — no field injection (`@Autowired` on fields).
- Prefer Java records for DTOs where practical.
- Tests mirror the `main/java` package structure under `src/test/java` (JUnit 5).

## Testing requirement

For every code change — new code or modification of existing code — add or update
the corresponding JUnit 5 test(s) in the same change, covering the new/changed
behavior (including relevant edge cases). Do not consider a change complete until
its tests are added/updated. This applies to bug fixes too, unless the user
explicitly asks to skip it for a given change.

## What Claude must never do

- Never modify `application.properties` secrets (e.g. `app.jwt.secret`) without
  flagging it explicitly.
- Never remove existing tests.
- Never install/upgrade Maven dependencies without confirming first.

## Known, intentional issues — do not fix unless asked

This repo ships with deliberate bugs used in live training exercises. Do not "clean
these up" as a drive-by fix; only touch them if explicitly asked (e.g. via a
security-review or bug-fix exercise):

1. `JwtUtil.isTokenValid()` always returns `true` — expired tokens are still accepted.
2. `TaskService.markCompleted()` and `.delete()` don't check that the task belongs to
   the requesting user (broken object-level authorization / IDOR).
3. `app.jwt.secret` in `application.properties` is a hardcoded secret committed to the repo.
4. `TaskServiceTest` and `AuthServiceTest` are placeholder-only test coverage.

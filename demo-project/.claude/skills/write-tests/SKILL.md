---
name: write-tests
description: >
  Use this skill automatically whenever a new method, endpoint, or
  component is added or modified in this Spring Boot / Angular codebase.
  Do not wait to be asked.
---

# Write Tests Skill

Backend: JUnit 5, tests mirror src/main/java under src/test/java, class
name Test-suffixed. Frontend: Jasmine specs alongside the component/service.

For each new or modified unit, generate tests covering:
1. The happy path
2. Edge cases: null, empty, boundary values
3. Error cases and expected exceptions/HTTP statuses

Frontend: services own HTTP calls, components never call HttpClient
directly. Component specs must mock the service, not HttpClient/
HttpClientTestingModule — keep that boundary in tests too.

Never remove or weaken an existing test to make a new one pass. Only add
or extend coverage.

This repo ships deliberate, known bugs for training use (e.g.
`JwtUtil.isTokenValid()` always returning `true`, missing ownership
checks in `TaskService.markCompleted()`/`.delete()`, placeholder-only
`TaskServiceTest`/`AuthServiceTest`). Write tests against actual current
behavior — do not change production code to make the "correct" behavior
pass, and do not treat these as bugs to fix unless explicitly asked.

For controller tests involving authenticated/authorized endpoints, follow
the existing pattern in the module (plain unit test with a mocked
service) unless the change specifically requires exercising the security
filter chain, in which case use `@WebMvcTest` with mocked security.

Run tests after writing to confirm everything passes:
- Backend: `mvn test` (from `backend/`), or `mvn test -Dtest=ClassName`
  / `mvn test -Dtest=ClassName#methodName` for a single class/method.
- Frontend: `ng test` (from `frontend/`).
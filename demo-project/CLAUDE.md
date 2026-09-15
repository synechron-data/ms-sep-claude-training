# Task Manager — CLAUDE.md

## Tech Stack
- Backend: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, H2 (dev)
- Frontend: Angular 17 (standalone components), TypeScript
- Auth: JWT, stateless

## Architecture
- backend/src/main/java/com/technizer/taskapi/auth/  — authentication & JWT
- backend/src/main/java/com/technizer/taskapi/task/   — task CRUD
- backend/src/main/java/com/technizer/taskapi/user/   — user entity/repo
- frontend/src/app/auth/                               — login/register
- frontend/src/app/tasks/                               — task list UI

## Coding Standards
- Backend: constructor injection only, no field injection
- Backend: DTOs as Java records where practical
- Frontend: standalone components, no NgModules for new features
- Frontend: services own HTTP calls — components never call HttpClient directly

## Testing
- Backend: JUnit 5, tests mirror main/java structure under src/test/java
- Frontend: Jasmine/Karma via `ng test`

## What Claude Must Never Do
- Never modify application.properties secrets without flagging it explicitly
- Never push directly to main
- Never remove existing tests
- Never install new dependencies without confirming first

## Git Standards
- Conventional Commits: feat:, fix:, docs:, test:, refactor:
- PR descriptions include: what changed, why, how to test

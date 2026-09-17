# Implementation Plan: Task Priority

**Branch**: `001-task-priority` | **Date**: 2026-09-17 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-task-priority/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Add a required `priority` (LOW / MEDIUM / HIGH) attribute to `Task`, defaulting to `MEDIUM`
whenever a client omits it (both for new tasks and for tasks that existed before this feature).
Extend `GET /api/tasks` with optional `priority`, `status`, and `sort` query parameters that
filter (priority AND completion status, combined) and sort (priority, High→Medium→Low) the
caller's own tasks, without changing the existing response shape for callers that don't use the
new parameters. Add matching filter/sort controls to the existing Angular task list component.
Enum persistence uses Hibernate `ddl-auto` against the H2 dev database (no Flyway/Liquibase in
this repo); "migration" here means the new column appearing with a Java-side default so
pre-existing rows read as `MEDIUM` — there is no separate migration script to write.

## Technical Context

**Language/Version**: Java 17 (backend), TypeScript / Angular 17 (frontend)

**Primary Dependencies**: Spring Boot 3.2, Spring Security, Spring Data JPA (backend); Angular
standalone components, `HttpClient` (frontend). No new dependencies required.

**Storage**: H2 in-memory (`jdbc:h2:mem:taskdb`), schema managed by Hibernate `ddl-auto` — no
Flyway/Liquibase migration tooling exists in this repo today.

**Testing**: JUnit 5 + Mockito + AssertJ (backend, plain-object tests — no `@SpringBootTest`/
`@DataJpaTest`/MockMvc currently used for `task/`); Jasmine/Karma with `TestBed` +
`HttpClientTestingModule`/`HttpTestingController` (frontend).

**Target Platform**: Existing web app — Spring Boot REST API served to an Angular SPA.

**Project Type**: Web application (existing `backend/` + `frontend/` split).

**Performance Goals**: No new performance targets; task lists are per-user and small (no
pagination exists today) — filtering/sorting must not introduce a full-table scan across users.

**Constraints**: Must preserve the current `GET /api/tasks` response shape for clients that don't
send the new query parameters (Constitution Principle V); must not require existing clients to
change (spec FR-009); no DTO layer exists today — `Task` entity is serialized directly, and this
feature keeps that convention rather than introducing one for this change alone.

**Scale/Scope**: Single new entity attribute, one endpoint extended (no new endpoints), one
frontend component extended. No auth/security model changes beyond enforcing existing ownership
checks on the new query parameters.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Check | Status |
|---|---|---|
| I. Backend Construction Standards | New/changed constructors keep using constructor injection (no new beans need it here). No new DTOs are introduced — the codebase already serializes the `Task` entity directly in `GET /api/tasks`; adding a `priority` field to that entity is consistent with the existing (pre-constitution) convention, not a new violation. `CreateTaskRequest`/`UpdateTaskRequest` remain records; `priority` is added to them as a plain field. | PASS |
| II. Frontend Architecture | `TaskListComponent` stays a standalone component; no NgModule introduced. New filter/sort HTTP params are built and sent via `TaskService.list(...)` — the component still never touches `HttpClient` directly. | PASS |
| III. Task Ownership Enforcement (NON-NEGOTIABLE) | The new `priority`/`status`/`sort` query parameters are added to `GET /api/tasks`, which already scopes to `@AuthenticationPrincipal` `ownerId` at the service layer (`listForOwner`). The extended list method MUST continue to filter by `ownerId` first, then apply priority/status/sort — never query across owners. This feature does not touch `markCompleted()`/`delete()` (pre-existing, out-of-scope IDOR gap documented in `backend/CLAUDE.md` — not introduced or worsened by this change). | PASS (existing gap in unrelated endpoints out of scope, not touched) |
| IV. Test-First Delivery | New JUnit tests required for: entity default, service filter/sort logic (including ownership scoping), controller param wiring. New Jasmine tests required for: `TaskService.list()` param building, `TaskListComponent` filter/sort UI state and rendering. `mvn test` and `npm test` must pass before the change is done. | PASS (planned in tasks phase) |
| V. API Backward Compatibility | `priority` is additive only on `Task`/`GET /api/tasks`; existing fields, types, and the unfiltered response shape are unchanged. `priority`, `status`, `sort` are optional — omitting all three reproduces today's exact response. | PASS |

No violations requiring Complexity Tracking justification.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/technizer/taskapi/task/
│   ├── Task.java              # + priority field (new Priority enum)
│   ├── Priority.java          # new enum: LOW, MEDIUM, HIGH
│   ├── TaskController.java    # GET /api/tasks gains priority/status/sort params;
│   │                          #   CreateTaskRequest/UpdateTaskRequest gain priority
│   ├── TaskService.java       # listForOwner(...) extended with filter/sort args
│   └── TaskRepository.java    # query method(s) or Specification for filter+sort
└── src/test/java/com/technizer/taskapi/task/
    ├── TaskControllerTest.java
    └── TaskServiceTest.java

frontend/
├── src/app/tasks/
│   ├── task.service.ts             # Task interface + priority; list() builds HttpParams
│   ├── task.service.spec.ts
│   ├── task-list.component.ts      # filter/sort controls + state
│   └── task-list.component.spec.ts
```

**Structure Decision**: Existing web application split (`backend/` Spring Boot API,
`frontend/` Angular SPA) — this feature extends existing files in the `task/` (backend) and
`tasks/` (frontend) modules per the constitution's module-boundary rule; no new modules,
projects, or top-level directories are introduced.

## Post-Design Constitution Check

*Re-checked after Phase 1 (data-model.md, contracts/, quickstart.md).*

The design does not introduce any new violation beyond what was already assessed pre-design:
- `Priority` enum + additive fields on the `Task` entity, `CreateTaskRequest`, and
  `UpdateTaskRequest` keep constructor injection and record DTOs (Principle I) — PASS.
- Frontend changes (`TaskService.list()` params, `TaskListComponent` controls) stay within the
  standalone-component / services-own-HTTP pattern (Principle II) — PASS.
- The extended `GET /api/tasks` query path is scoped by `ownerId` before any priority/status/sort
  logic is applied, per `data-model.md` and `contracts/tasks-api.md` (Principle III) — PASS.
- Test additions are enumerated in `quickstart.md` and will be captured as concrete tasks in
  `/speckit-tasks` (Principle IV) — PASS, pending task generation.
- `contracts/tasks-api.md` confirms every new parameter and field is additive/optional, and that
  omitting them reproduces today's exact response (Principle V) — PASS.

No entries required in Complexity Tracking below.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally left empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

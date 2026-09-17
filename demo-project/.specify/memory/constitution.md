<!--
Sync Impact Report
- Version change: [TEMPLATE] → 1.0.0 (initial ratification)
- Modified principles: n/a (first substantive adoption; all 5 principle slots filled)
- Added sections:
  - Core Principles I–V (Backend Construction Standards, Frontend Architecture,
    Task Ownership Enforcement, Test-First Delivery, API Backward Compatibility)
  - Technology & Architecture Constraints
  - Development Workflow & Quality Gates
  - Governance
- Removed sections: none (template placeholders replaced with concrete content)
- Templates requiring follow-up: none — this command does not modify dependent
  templates; downstream commands (/speckit-plan, /speckit-tasks, etc.) read this
  file at runtime.
- Deferred placeholders: none
-->

# Task Manager Constitution

## Core Principles

### I. Backend Construction Standards
Backend code MUST use constructor injection exclusively; field injection (`@Autowired` on
fields) MUST NOT be used. Data transfer objects MUST be implemented as Java `record` types
wherever the shape is a plain, immutable data carrier; a class is only justified when a
record cannot express the required behavior (e.g., custom equality or builder needs), and
that justification MUST be stated in the PR description.

**Rationale**: Constructor injection makes dependencies explicit and testable without a
Spring context, and keeps components immutable. Record DTOs eliminate boilerplate
getters/setters and guarantee immutability at the API boundary, reducing accidental mutation
bugs in request/response handling.

### II. Frontend Architecture
Frontend features MUST be built as Angular standalone components; NgModules MUST NOT be
introduced for new features. Components MUST NOT call `HttpClient` directly — all HTTP
communication MUST be encapsulated in Angular services, which components consume through
dependency injection.

**Rationale**: Standalone components remove NgModule ceremony and keep the frontend aligned
with modern Angular conventions. Centralizing HTTP calls in services keeps components focused
on presentation, makes API interactions mockable in tests, and prevents duplicated fetch
logic scattered across the UI.

### III. Task Ownership Enforcement (NON-NEGOTIABLE)
Every task-related endpoint MUST verify that the authenticated user (derived from the JWT
principal) owns the task being read, modified, or deleted, before performing the operation.
Ownership checks MUST occur at the service layer, not only in the repository query, so that
authorization logic is testable independently of persistence. Any endpoint that returns or
mutates task data without an explicit, tested ownership check is a constitution violation.

**Rationale**: This is a multi-tenant task manager secured by stateless JWT auth. Missing
ownership checks are a Broken Object-Level Authorization (BOLA) vulnerability — one of the
most common and severe flaws in API-driven applications — and must be treated as
non-negotiable rather than a style preference.

### IV. Test-First Delivery
Every change to backend code MUST ship with corresponding JUnit 5 tests, and every change to
frontend code MUST ship with corresponding Jasmine/Karma tests, mirroring the structure
conventions defined in the project's `CLAUDE.md`. A change is not considered complete until
`mvn test` (backend) and `npm test` (frontend) both pass locally. Existing tests MUST NOT be
removed or weakened to make a change pass; if a test is genuinely obsolete, its removal MUST
be called out explicitly in the PR description with justification.

**Rationale**: Tests are the executable specification of correct behavior, particularly for
authorization logic (Principle III) and API contracts (Principle V). Requiring passing test
suites before a change is "done" prevents regressions from silently reaching main.

### V. API Backward Compatibility
Existing API response shapes (fields, types, status codes, and error formats) MUST remain
backward compatible. New fields MAY be added additively; existing fields MUST NOT be renamed,
removed, retyped, or repurposed without a deliberate, explicitly flagged breaking-change
decision made with the user. Backward-incompatible changes MUST be called out before
implementation, not discovered after the fact via a failing frontend integration.

**Rationale**: The Angular frontend and any external consumers depend on stable response
contracts. Silent breaking changes cause hard-to-diagnose frontend failures and erode trust
in the API for any future integrations.

## Technology & Architecture Constraints

- Backend: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, H2 for development.
- Frontend: Angular 17 using standalone components, TypeScript.
- Auth: JWT, stateless — no server-side session state.
- Module boundaries follow the existing package/folder layout: `auth/`, `task/`, and `user/`
  on the backend; `auth/` and `tasks/` on the frontend. New code MUST be placed in the
  boundary matching its responsibility rather than introducing parallel structures.
- `application.properties` secrets MUST NOT be modified without explicitly flagging the
  change to the user first. New dependencies MUST NOT be introduced without user
  confirmation.

## Development Workflow & Quality Gates

- Commit messages MUST follow Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`,
  `refactor:`).
- Pull request descriptions MUST include: what changed, why, and how to test.
- Direct pushes to `main` MUST NOT occur; changes land via reviewed pull requests.
- Before a change is proposed as complete, both `mvn test` and `npm test` MUST be run and
  MUST pass; a change that has not been verified this way is not done.
- Code review MUST confirm compliance with Principles I–V above; a reviewer finding a
  violation blocks the merge until resolved or explicitly re-scoped with the user.

## Governance

This constitution supersedes ad hoc conventions and prior undocumented practice for this
project. Amendments are made by editing this file via `/speckit-constitution`, and MUST
include an updated Sync Impact Report describing what changed and why.

**Versioning policy** (semantic versioning applied to governance):
- MAJOR: Backward-incompatible removal or redefinition of a principle or governance rule.
- MINOR: A new principle or materially expanded section is added.
- PATCH: Clarifications, wording fixes, or non-semantic refinements.

**Compliance review**: Every PR and every `/speckit-plan` or `/speckit-implement` run MUST be
checked against the Core Principles above. Any complexity or deviation that conflicts with a
principle MUST be justified in writing (PR description or plan's Complexity Tracking section)
or the work MUST be re-scoped to comply. Use this constitution, alongside the project's
`CLAUDE.md`, as the authoritative source for day-to-day development guidance.

**Version**: 1.0.0 | **Ratified**: 2026-09-17 | **Last Amended**: 2026-09-17

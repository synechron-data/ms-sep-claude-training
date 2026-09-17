# Phase 0 Research: Task Priority

All unknowns from the Technical Context were resolvable from the existing codebase and the
constitution; no external research was required. This document records the decisions made and
the alternatives considered.

## Decision: Represent priority as a Java `enum` mapped with `@Enumerated(EnumType.STRING)`

- **Decision**: Add `Priority.java` (`LOW`, `MEDIUM`, `HIGH`) and map it on `Task` with
  `@Enumerated(EnumType.STRING)`, defaulting the field to `Priority.MEDIUM`.
- **Rationale**: No enum-backed column exists yet in this codebase, so there's no established
  in-repo convention to follow — `EnumType.STRING` is the standard JPA choice because it stores a
  readable value (`"HIGH"`, not `2`), so column values are unambiguous and stable across future
  reordering of the enum constants — unlike `EnumType.ORDINAL`, which encodes the enum's declared
  order as an integer and can silently corrupt existing data if a constant is added or reordered.
- **Alternatives considered**:
  - `EnumType.ORDINAL` — rejected: fragile if enum constants are ever reordered/inserted; H2 dev
    data wouldn't survive a restart anyway, but production-style guidance still applies.
  - Plain `String` column with validation in the service — rejected: loses compile-time safety
    and the JPA layer wouldn't reject invalid values as cleanly as `@Enumerated`.

## Decision: No Flyway/Liquibase migration script — rely on Hibernate `ddl-auto` + Java-side default

- **Decision**: Do not introduce a migration framework for this feature. The `Priority` column
  gets a `nullable = false` constraint with a Java-side default of `MEDIUM` on the entity. Since
  the dev database (H2 in-memory) is recreated fresh on every application start via
  `ddl-auto`, there are no pre-existing persisted rows to backfill in practice — the "existing
  rows default to MEDIUM" requirement is satisfied by construction (every `Task` object always
  has a non-null `Priority`, defaulted in Java, before it's ever persisted).
- **Rationale**: The repository has no `db/migration` directory, no Flyway or Liquibase
  dependency, and no prior use of either. Introducing migration tooling is a new dependency and
  is out of scope for this feature per the constitution ("New dependencies MUST NOT be introduced
  without user confirmation") and per the user's own framing ("migration" is used loosely here,
  not as a request for a specific tool). If this app later moves to a persistent database or
  Flyway is introduced, a real migration (`ALTER TABLE task ADD COLUMN priority VARCHAR(10) NOT
  NULL DEFAULT 'MEDIUM'`) would replace this note — flagged here for future awareness, not
  actioned now.
- **Alternatives considered**:
  - Introduce Flyway now — rejected: new dependency requiring explicit user confirmation
    (constitution constraint), disproportionate to an H2 dev-only setup with no persisted data to
    migrate.

## Decision: Extend `GET /api/tasks` with optional `priority`, `status`, `sort` params rather than adding new endpoints

- **Decision**: Add three optional query parameters to the existing `GET /api/tasks`:
  `priority` (repeatable, e.g. `?priority=HIGH&priority=MEDIUM` for inclusive multi-select),
  `status` (`complete` | `incomplete`), and `sort` (`priority` to request the High→Medium→Low
  ordering; parameter absent = today's unspecified/insertion order, preserving current
  behavior).
- **Rationale**: The spec requires that clients not sending these params keep working with an
  unchanged response shape (FR-009, FR-010) — extending the existing endpoint additively achieves
  that directly. The existing separate `GET /api/tasks/completed` endpoint predates this feature;
  it is left untouched (out of scope) since removing/merging it would be a breaking change for
  any caller already using it, which the constitution's backward-compatibility principle rules
  out without an explicit, flagged decision.
- **Alternatives considered**:
  - New endpoint (e.g. `GET /api/tasks/search`) — rejected: fragments the list API and doesn't
    match the user's explicit instruction to extend `GET /api/tasks`.
  - Request body on a `POST /api/tasks/query` — rejected: unnecessary complexity for simple
    filter/sort parameters; query params are the idiomatic REST approach and match existing
    conventions in this controller.

## Decision: Implement combined filter query via repository method(s) rather than introducing Specifications/Querydsl

- **Decision**: Add a Spring Data JPA derived query (or a single `@Query`) on `TaskRepository`
  that filters by `ownerId`, optional priority set, and optional completed flag, e.g.
  `findByOwnerIdAndPriorityInAndCompleted(...)` variants selected in `TaskService` based on which
  optional filters are present, or one `@Query` with nullable-parameter `OR :param IS NULL`
  clauses. Sorting is applied via a `Sort`/`OrderBy` on the same query rather than an in-memory
  sort, since there's no pagination to reconcile it with.
- **Rationale**: The repository currently uses only derived-query methods
  (`findByOwnerId`, `findByOwnerIdAndCompletedTrue`) with no `Specification`/Querydsl
  infrastructure. Introducing `JpaSpecificationExecutor` for three optional filters is more
  machinery than this small, single-table query needs; a small number of explicit query methods
  (or one parameterized `@Query`) is easier to unit-test and matches the existing simple style.
- **Alternatives considered**:
  - `JpaSpecificationExecutor` — rejected: adds a new pattern to a codebase that doesn't use it
    yet, for a filter combination (priority × status) small enough not to need it.
  - Fetch all owner tasks and filter/sort in Java (`TaskService`) — rejected: works at current
    scale but pushes filtering out of the database for no reason; a `@Query`/derived method is
    no harder to write and keeps behavior consistent if the query needs an index later.

## Decision: Keep serializing `Task` entity directly (no new DTO layer)

- **Decision**: Add `priority` directly to the `Task` entity and let it flow through the existing
  direct entity-serialization pattern used by `GET /api/tasks` today; do not introduce a
  response DTO as part of this feature.
- **Rationale**: The constitution prefers DTOs as records "where practical," but no DTO layer
  exists anywhere in `task/` today — introducing one here would be a larger, unrelated refactor
  of every task endpoint's response shape, which risks violating Principle V (backward
  compatibility) if done carelessly, and is disproportionate to adding one field. This is
  recorded as a known pre-existing style gap, not a new violation introduced by this feature.
- **Alternatives considered**:
  - Introduce `TaskResponse` record now and migrate all task endpoints to it — rejected as
    out-of-scope scope creep for a priority-filtering feature; would touch every existing task
    test and endpoint unnecessarily.

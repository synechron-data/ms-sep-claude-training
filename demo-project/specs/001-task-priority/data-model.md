# Phase 1 Data Model: Task Priority

## Entity: Task (extended)

Existing entity (`backend/src/main/java/com/technizer/taskapi/task/Task.java`), gaining one new
field.

| Field | Type | Constraints | Notes |
|---|---|---|---|
| id | Long | PK, generated | unchanged |
| title | String | not null | unchanged |
| description | String | nullable | unchanged |
| completed | boolean | not null, default false | unchanged — this is the existing "completion status" (spec: FR-006) |
| ownerId | Long | not null | unchanged — identifies the owning user (spec: FR-011) |
| dueDate | LocalDate | nullable | unchanged |
| createdAt | Instant | not null, default now | unchanged |
| **priority** | **Priority (enum)** | **not null, default `MEDIUM`** | **new** — persisted as `EnumType.STRING`; see FR-001–FR-004, FR-012 |

Derived behavior `isOverdue()` is unaffected by this feature.

### Invariants

- A `Task` always has exactly one `priority` value; it is never null and never an "unset" state
  (spec FR-012, Edge Cases). There is no API path that can null it out — create/update requests
  either supply one of `LOW`/`MEDIUM`/`HIGH` or omit the field, in which case the default applies.
- A `Task` created before this feature shipped reads as `priority = MEDIUM` (spec FR-004, SC-002)
  — satisfied by the entity-level default combined with the dev database having no
  pre-existing persisted rows to backfill (see `research.md`).

## New Enum: Priority

`backend/src/main/java/com/technizer/taskapi/task/Priority.java`

```java
public enum Priority {
    LOW, MEDIUM, HIGH
}
```

- Declaration order `LOW, MEDIUM, HIGH` is for readability only — persistence uses
  `EnumType.STRING`, so declaration order has no effect on stored values. Sort ordering
  (High → Medium → Low, spec FR-008) is implemented explicitly in the repository query/sort
  clause, not by relying on enum ordinal order.
- Serializes to/from JSON as its name (`"LOW"`, `"MEDIUM"`, `"HIGH"`) via Jackson's default enum
  handling — matches the plain, uppercase-string style already used for other simple values in
  this API (e.g., `status` query values below).

## API-facing shapes (request/response records, `TaskController`)

`CreateTaskRequest` and `UpdateTaskRequest` (currently inline records in `TaskController`) each
gain one new, optional field:

```java
public record CreateTaskRequest(@NotBlank String title, String description, LocalDate dueDate,
                                 Priority priority) { }

public record UpdateTaskRequest(@NotBlank String title, String description, LocalDate dueDate,
                                 Priority priority) { }
```

- `priority` is nullable in both records; `TaskService.create`/`update` MUST default a null
  value to `Priority.MEDIUM` before persisting (spec FR-003, FR-009).
- The `Task` entity returned in every response (create, update, list, complete) gains the
  `priority` field automatically since it's serialized directly — no separate response type to
  update. Clients that don't read the new field are unaffected (spec FR-010).

## Query parameters (`GET /api/tasks`)

| Param | Type | Cardinality | Meaning |
|---|---|---|---|
| `priority` | `Priority` (`LOW`\|`MEDIUM`\|`HIGH`) | 0..N (repeatable) | Inclusive match: task shown if its priority is any of the given values (spec FR-005, Assumptions). Absent = no priority filtering. |
| `status` | `complete` \| `incomplete` | 0..1 | Filters by the existing `completed` boolean (spec FR-006). Absent = no status filtering. |
| `sort` | `priority` | 0..1 | When present, orders results High → Medium → Low (spec FR-008). Absent = existing unspecified/insertion order (unchanged from today). |

When both `priority` and `status` are present, a task must satisfy both to be included (AND
combination — spec FR-007, Clarifications 2026-09-17). Malformed enum/status values MUST produce
a `400 Bad Request` (Spring's default behavior for an unparseable `@RequestParam` enum), not a
silent ignore, consistent with existing error handling conventions in this controller.

## State transitions

Priority has no lifecycle/workflow — it is a plain, always-set classification value that a user
may change (to any of the three values) at any time via the existing update path. There are no
transition restrictions (e.g., no "can't go from HIGH to LOW" rule).

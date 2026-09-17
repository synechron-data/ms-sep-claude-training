# Contract: Task API changes

Base path: `/api/tasks` (unchanged). Auth: existing JWT bearer, `ownerId` resolved from the
token subject via `@AuthenticationPrincipal` (unchanged).

## GET /api/tasks (extended)

**Existing behavior (unchanged when no new params are sent)**: returns all tasks owned by the
caller, in the same shape as today.

**New optional query parameters**:

| Param | Values | Repeatable | Default when absent |
|---|---|---|---|
| `priority` | `LOW`, `MEDIUM`, `HIGH` | Yes (`?priority=HIGH&priority=MEDIUM`) | No priority filter applied |
| `status` | `complete`, `incomplete` | No | No status filter applied |
| `sort` | `priority` | No | No explicit sort (today's order) |

**Combination rule**: `priority` and `status`, when both present, are AND-combined — a task must
match the priority set AND the status to be included.

**Response**: unchanged shape — a JSON array of `Task` objects, each now additionally including
`"priority": "LOW" | "MEDIUM" | "HIGH"`. Example (existing fields elided for brevity):

```json
[
  { "id": 1, "title": "Renew passport", "completed": false, "priority": "HIGH", "...": "..." },
  { "id": 2, "title": "Water plants", "completed": true, "priority": "MEDIUM", "...": "..." }
]
```

**Examples**:

- `GET /api/tasks` → unchanged: all of the caller's tasks, current order.
- `GET /api/tasks?priority=HIGH` → only the caller's HIGH-priority tasks.
- `GET /api/tasks?status=incomplete` → only the caller's incomplete tasks.
- `GET /api/tasks?priority=HIGH&priority=MEDIUM&status=incomplete` → caller's incomplete tasks
  that are HIGH or MEDIUM priority.
- `GET /api/tasks?sort=priority` → all of the caller's tasks, ordered High → Medium → Low.

**Errors**:
- `400 Bad Request` if `priority` or `status` contains a value outside the allowed set.
- `401 Unauthorized` if the caller is unauthenticated (unchanged, existing filter chain).
- Ownership scoping is enforced identically to today: results are always pre-scoped to
  `ownerId` before any priority/status/sort logic runs — a caller can never see or filter
  another user's tasks.

## POST /api/tasks (extended)

**Request body** gains one new, optional field:

```json
{ "title": "string", "description": "string", "dueDate": "YYYY-MM-DD", "priority": "HIGH" }
```

- `priority` omitted or `null` → task is created with `priority: "MEDIUM"`.
- `priority` present → task is created with that value.
- Response: unchanged shape, `Task` object, now including `priority`.

## PUT /api/tasks/{id} (extended)

**Request body** gains one new, optional field, same semantics as `POST` above:

```json
{ "title": "string", "description": "string", "dueDate": "YYYY-MM-DD", "priority": "LOW" }
```

- `priority` omitted or `null` on an update does **not** clear the task's existing priority —
  per spec FR-012, priority can never become empty. Omitting it on update MUST leave the task's
  current priority unchanged (this differs from `POST`, where omission means "default to
  MEDIUM for a brand-new task" — there is no existing value to preserve on create).
- `priority` present → task's priority is set to that value.
- Response: unchanged shape, `Task` object, now including `priority`.

## PATCH /api/tasks/{id}/complete and DELETE /api/tasks/{id}

No change in this feature. `priority` simply appears in the `complete` response's `Task` object
like any other existing field. (Note: these two endpoints have a pre-existing ownership-check gap
documented in `backend/CLAUDE.md` — out of scope for this feature, not touched here.)

## GET /api/tasks/completed

No change in this feature — left as-is; the new `status=complete` filter on `GET /api/tasks` is
an additive alternative, not a replacement.

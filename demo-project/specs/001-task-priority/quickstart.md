# Quickstart: Validating Task Priority

Validates the feature end-to-end against a running backend + frontend. See
[`data-model.md`](./data-model.md) and [`contracts/tasks-api.md`](./contracts/tasks-api.md) for
field/parameter details.

## Prerequisites

- Backend and frontend build/test clean: `mvn test` (from `backend/`) and `npm test` (from
  `frontend/`) both pass.
- A registered user with an auth token (via `/api/auth/register` + `/api/auth/login`, or the
  Angular login screen at `http://localhost:4200`).

## 1. Start the app

```bash
# terminal 1
cd backend && mvn spring-boot:run

# terminal 2
cd frontend && npm start
```

## 2. Backend validation (API-level)

With `$TOKEN` set to a valid bearer token for a logged-in user:

```bash
# Create a task without priority -> expect priority: "MEDIUM" in the response
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"No priority sent"}' | grep -o '"priority":"[A-Z]*"'

# Create a task with priority=HIGH -> expect priority: "HIGH"
curl -s -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Urgent","priority":"HIGH"}' | grep -o '"priority":"[A-Z]*"'

# Filter by priority -> expect only HIGH tasks back
curl -s "http://localhost:8080/api/tasks?priority=HIGH" -H "Authorization: Bearer $TOKEN"

# Filter by priority + status together (AND) -> expect only incomplete HIGH tasks
curl -s "http://localhost:8080/api/tasks?priority=HIGH&status=incomplete" \
  -H "Authorization: Bearer $TOKEN"

# Sort by priority -> expect HIGH tasks before MEDIUM before LOW
curl -s "http://localhost:8080/api/tasks?sort=priority" -H "Authorization: Bearer $TOKEN"

# Existing plain call still works unchanged -> same shape as before this feature, plus "priority"
curl -s http://localhost:8080/api/tasks -H "Authorization: Bearer $TOKEN"
```

**Expected outcomes** (maps to spec Success Criteria):
- First call's task has `"priority":"MEDIUM"` (SC-002, FR-003).
- Second call's task has `"priority":"HIGH"` (FR-001).
- Third call returns only HIGH-priority tasks for this user (FR-005).
- Fourth call returns only incomplete, HIGH-priority tasks (FR-007).
- Fifth call returns tasks ordered HIGH, then MEDIUM, then LOW (FR-008).
- Sixth call succeeds and returns the same fields as before, now with `priority` included
  (FR-009, FR-010, SC-005).

## 3. Ownership check

Log in as a second user (`$TOKEN_2`) and confirm their `GET /api/tasks?priority=HIGH` never
returns the first user's tasks, and that attempting `PUT /api/tasks/{id}` on the first user's
task id with `$TOKEN_2` is rejected (FR-011).

## 4. Frontend validation (UI-level)

1. Open `http://localhost:4200`, log in.
2. Create a task without touching the priority control → it appears in the list as Medium.
3. Create a task and explicitly pick "High" → it appears as High.
4. Edit an existing task's priority to "Low" → the list reflects Low after saving.
5. Use the priority filter control to show only "High" → only High tasks remain visible.
6. Use the completion-status filter alongside the priority filter → list narrows to tasks
   matching both.
7. Use the sort-by-priority control → list re-orders High → Medium → Low.

## 5. Regression check

Run the full suites one more time to confirm no existing behavior broke:

```bash
cd backend && mvn test
cd frontend && npm test
```

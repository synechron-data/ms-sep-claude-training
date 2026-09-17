---

description: "Task list template for feature implementation"
---

# Tasks: Task Priority

**Input**: Design documents from `/specs/001-task-priority/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/tasks-api.md, quickstart.md

**Tests**: Included — the constitution (Principle IV, Test-First Delivery) requires JUnit 5
tests for every backend change and Jasmine/Karma tests for every frontend change; test tasks
below are mandatory, not optional.

**Organization**: Tasks are grouped by user story (from spec.md) to enable independent
implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Paths are exact, per plan.md's Project Structure (`backend/src/main/java/com/technizer/taskapi/task/`, `backend/src/test/java/com/technizer/taskapi/task/`, `frontend/src/app/tasks/`)

## Phase 1: Setup

**Purpose**: Confirm a clean, green baseline before making any change (no new dependencies or
project scaffolding are needed — this feature extends existing files only).

- [X] T001 [P] Run `mvn test` from `backend/` and confirm all existing tests pass before making changes
- [X] T002 [P] Run `npm test` from `frontend/` and confirm all existing tests pass before making changes

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Introduce the `Priority` type on both sides so every user story below can build on
it. No user story is independently testable until this phase is done.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T003 [P] Create `Priority` enum (`LOW`, `MEDIUM`, `HIGH`) in `backend/src/main/java/com/technizer/taskapi/task/Priority.java`, per data-model.md
- [X] T004 Add `priority` field to `Task` in `backend/src/main/java/com/technizer/taskapi/task/Task.java`: `@Enumerated(EnumType.STRING)`, `@Column(nullable = false)`, Java-side default `Priority.MEDIUM`, plus `getPriority()`/`setPriority(Priority)` accessors (depends on T003; per data-model.md's "not null, default MEDIUM" constraint and FR-004/FR-012)
- [X] T005 [P] Add `priority: 'LOW' | 'MEDIUM' | 'HIGH'` field to the `Task` interface in `frontend/src/app/tasks/task.service.ts`, per data-model.md

**Checkpoint**: `Priority` exists on the entity and the frontend model — user story phases can now begin.

---

## Phase 3: User Story 1 - Set a Task's Priority (Priority: P1) 🎯 MVP

**Goal**: A user creating or editing a task can choose Low, Medium, or High; omitting it
defaults to Medium; priority can never be cleared to empty.

**Independent Test**: Create a task with each of the three priority values and edit an existing
task to change its priority; confirm the saved value is returned/displayed correctly, and that
omitting priority on create yields Medium.

### Tests for User Story 1 ⚠️

> Write these tests FIRST; ensure they FAIL before implementation.

- [ ] T006 [P] [US1] Add tests in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java`: `create()` defaults `priority` to `MEDIUM` when the caller passes `null` (FR-003, FR-009); `create()` persists the given `priority` when provided (FR-001); `update()` sets a new `priority` when provided (FR-002); `update()` leaves the task's existing `priority` unchanged when the caller passes `null` — it MUST NOT be cleared (FR-012, contracts/tasks-api.md PUT semantics); and assert that a bare `new Task(...)` (no explicit priority set) has `getPriority() == Priority.MEDIUM` by construction (FR-004, SC-002)
- [ ] T007 [P] [US1] Add tests in `backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java` verifying `CreateTaskRequest`/`UpdateTaskRequest` accept an optional `priority` field and that `create()`/`update()` pass it through to `TaskService` unchanged, and verify a `GET /api/tasks` response for a caller sending no new params retains every pre-existing field (id, title, description, completed, ownerId, dueDate, createdAt) unchanged in name and type, with `priority` the only addition (FR-010)
- [ ] T008 [P] [US1] Add tests in `frontend/src/app/tasks/task.service.spec.ts` verifying `create()`/`update()` include `priority` in the outgoing request body when the caller supplies it, per `contracts/tasks-api.md`
- [ ] T009 [P] [US1] Add tests in `frontend/src/app/tasks/task-list.component.spec.ts` verifying the create-task form exposes a Low/Medium/High priority control, the edit-task form does too, and each task rendered in the list displays its priority

### Implementation for User Story 1

- [ ] T010 [US1] Add an optional `Priority priority` field to both `CreateTaskRequest` and `UpdateTaskRequest` records in `backend/src/main/java/com/technizer/taskapi/task/TaskController.java` (depends on T004)
- [ ] T011 [US1] Update `TaskService.create(...)` in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` to accept the new `priority` argument and default a `null` value to `Priority.MEDIUM` before saving (depends on T010; FR-003, FR-009)
- [ ] T012 [US1] Update `TaskService.update(...)` in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` to accept the new `priority` argument, setting it on the task when non-null and leaving the task's current `priority` untouched when `null` (depends on T010; FR-012)
- [ ] T013 [US1] Add a priority `<select>` (Low/Medium/High) to the new-task form in `frontend/src/app/tasks/task-list.component.ts`, following the existing two-way-bound field pattern (e.g. `newTitle`) (depends on T005)
- [ ] T014 [US1] Add a priority `<select>` to the edit-task form in `frontend/src/app/tasks/task-list.component.ts`, and extend `create(...)`/`update(...)` in `frontend/src/app/tasks/task.service.ts` to accept and send `priority` in the request body (depends on T005, T013)
- [ ] T015 [US1] Render each task's `priority` in the task list template in `frontend/src/app/tasks/task-list.component.ts` (e.g., a text label or badge next to the title) so the set/changed value is visibly confirmed (depends on T005)

**Checkpoint**: User Story 1 is fully functional and independently testable — priority can be
set on create, changed on edit, defaults to Medium, and is always visible.

---

## Phase 4: User Story 2 - Filter the Task List by Priority and Completion Status (Priority: P2)

**Goal**: A user can narrow their task list to tasks matching a chosen priority (or set of
priorities), a completion status, or both combined (AND).

**Independent Test**: Seed a user's task list with varying priority/completion combinations;
confirm each filter, and the two combined, return only the matching tasks, still scoped to the
authenticated user.

### Tests for User Story 2 ⚠️

- [ ] T016 [P] [US2] Add tests in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java` (or a new `TaskRepositoryTest.java`) verifying the list query filters by one or more `priority` values (FR-005), by `completed` status (FR-006), by both combined with AND semantics (FR-007), and always stays scoped to the given `ownerId` even when filters are applied (FR-011)
- [ ] T017 [P] [US2] Add tests in `backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java` verifying `GET /api/tasks` accepts repeatable `priority` and single `status` (`complete`|`incomplete`) query params and passes them to `TaskService`, and that an out-of-range value produces `400 Bad Request` (contracts/tasks-api.md)
- [ ] T018 [P] [US2] Add tests in `frontend/src/app/tasks/task.service.spec.ts` verifying `list(...)` builds request params for `priority` (repeated) and `status` only when supplied, and sends a plain unparameterized request when neither is supplied
- [ ] T019 [P] [US2] Add tests in `frontend/src/app/tasks/task-list.component.spec.ts` verifying selecting a priority filter, a completion-status filter, or both together calls `TaskService.list(...)` with the matching params and re-renders only the returned tasks

### Implementation for User Story 2

- [ ] T020 [US2] Add a repository query method (or a parameterized `@Query`) to `backend/src/main/java/com/technizer/taskapi/task/TaskRepository.java` that filters by `ownerId` plus an optional set of `priority` values and an optional `completed` flag, per research.md's decision to avoid Specifications/Querydsl
- [ ] T021 [US2] Extend `TaskService.listForOwner(...)` in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` to accept optional `List<Priority>` and optional `Boolean completed` arguments and delegate to the new repository method (depends on T020)
- [ ] T022 [US2] Extend `GET /api/tasks` in `backend/src/main/java/com/technizer/taskapi/task/TaskController.java` to accept optional repeatable `priority` and optional `status` (`complete`|`incomplete`) query params, translate `status` to a boolean, and pass both through to `TaskService.listForOwner(...)` (depends on T021)
- [ ] T023 [US2] Extend `list(...)` in `frontend/src/app/tasks/task.service.ts` to accept optional `priority: string[]` and `status: 'complete' | 'incomplete'` arguments and build `HttpParams` that include only the params actually supplied
- [ ] T024 [US2] Add priority and completion-status filter controls to `frontend/src/app/tasks/task-list.component.ts`, wired to call the extended `TaskService.list(...)` and re-render the returned list (depends on T023)

**Checkpoint**: User Stories 1 AND 2 both work independently — filtering narrows the list
correctly, alone and combined, without breaking priority-setting from US1.

---

## Phase 5: User Story 3 - Sort the Task List by Priority (Priority: P3)

**Goal**: A user can order their task list High → Medium → Low.

**Independent Test**: Seed a user's task list with mixed priorities; confirm the returned order
matches High → Medium → Low, with a stable relative order for tasks sharing a priority.

### Tests for User Story 3 ⚠️

- [ ] T025 [P] [US3] Add tests in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java` verifying the list query returns tasks ordered High → Medium → Low when priority sorting is requested (FR-008), with a stable, consistent relative order for same-priority tasks
- [ ] T026 [P] [US3] Add tests in `backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java` verifying `GET /api/tasks?sort=priority` requests the sorted order and that omitting `sort` preserves today's existing (unsorted) order
- [ ] T027 [P] [US3] Add tests in `frontend/src/app/tasks/task.service.spec.ts` verifying `list(...)` includes a `sort=priority` param only when sorting is requested
- [ ] T028 [P] [US3] Add tests in `frontend/src/app/tasks/task-list.component.spec.ts` verifying the sort-by-priority control calls `TaskService.list(...)` with sorting requested and the rendered list appears High → Medium → Low

### Implementation for User Story 3

- [ ] T029 [US3] Add an explicit High → Medium → Low ordering clause to the repository query from T020 in `backend/src/main/java/com/technizer/taskapi/task/TaskRepository.java`, applied only when priority sorting is requested — not derived from enum ordinal order, per research.md (depends on T020)
- [ ] T030 [US3] Extend `TaskService.listForOwner(...)` and `GET /api/tasks` in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` and `TaskController.java` to accept an optional `sort=priority` param and apply the T029 ordering (depends on T021, T022, T029)
- [ ] T031 [US3] Add a sort-by-priority control to `frontend/src/app/tasks/task-list.component.ts` and extend `list(...)` in `frontend/src/app/tasks/task.service.ts` to send `sort=priority` when selected (depends on T023)

**Checkpoint**: All three user stories now work independently and together — set, filter, and
sort all function correctly in combination.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification that the feature meets its success criteria end-to-end.

- [ ] T032 [P] Walk through every step in `specs/001-task-priority/quickstart.md` against the running app (backend `mvn spring-boot:run` + frontend `npm start`) and confirm each expected outcome
- [ ] T033 Run `mvn test` from `backend/` and `npm test` from `frontend/` one final time and confirm all tests (existing and new) pass, per the constitution's Test-First Delivery gate

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Setup — BLOCKS all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational only.
- **User Story 2 (Phase 4)**: Depends on Foundational only. Independently testable from US1,
  though both extend the same `GET /api/tasks` method — see note below.
- **User Story 3 (Phase 5)**: Depends on Foundational only, and on the repository query
  introduced in T020 (US2's implementation) since sorting is applied to the same query. If US2
  is skipped, T029 must instead add the base filter-less query itself.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: No dependency on US2/US3 — fully independent.
- **User Story 2 (P2)**: No functional dependency on US1, but both stories touch
  `TaskController.list(...)`/`TaskService.listForOwner(...)`, so implement US1 first to avoid
  merge conflicts within the same methods (recommended order, not a hard requirement).
- **User Story 3 (P3)**: Builds on the repository query method introduced by US2 (T020); if
  implementing US3 before US2, T029 must create that query method itself instead of extending it.

### Within Each User Story

- Tests are written first and MUST fail before implementation begins.
- Backend model/record changes before service changes; service changes before controller
  changes; controller changes before frontend service changes; frontend service changes before
  frontend component changes.

### Parallel Opportunities

- T001 and T002 (Setup) run in parallel.
- T003 and T005 (Foundational) run in parallel; T004 depends on T003.
- Within each story's test phase, all `[P]` test tasks run in parallel (different files).
- US1 and US2 can be staffed in parallel after Foundational, with the merge-order caveat above.

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Add tests in backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java for create()/update() priority defaulting and preservation"
Task: "Add tests in backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java for optional priority on CreateTaskRequest/UpdateTaskRequest"
Task: "Add tests in frontend/src/app/tasks/task.service.spec.ts for priority in create()/update() request bodies"
Task: "Add tests in frontend/src/app/tasks/task-list.component.spec.ts for priority controls and display"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational (blocks all stories).
3. Complete Phase 3: User Story 1.
4. **STOP and VALIDATE**: run T006–T009's tests plus a manual create/edit pass; confirm priority
   sets, defaults to Medium, and never clears.
5. This alone delivers value: users can set and see priority, even before filtering/sorting ship.

### Incremental Delivery

1. Setup + Foundational → priority type exists everywhere.
2. Add User Story 1 → validate independently → MVP.
3. Add User Story 2 → validate independently (filtering, including the AND combination).
4. Add User Story 3 → validate independently (sorting).
5. Run Phase 6 Polish once all three stories are in.

## Notes

- `[P]` tasks touch different files with no unmet dependencies.
- `[Story]` labels trace every implementation and test task back to its user story.
- Tests are mandatory per the project constitution (Principle IV) — do not skip T006–T009,
  T016–T019, or T025–T028.
- Every new/changed backend method that touches task data must keep results scoped to the
  authenticated `ownerId` (Constitution Principle III) — verified explicitly by T016.

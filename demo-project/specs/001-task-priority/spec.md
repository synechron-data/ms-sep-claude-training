# Feature Specification: Task Priority

**Feature Branch**: `001-task-priority`

**Created**: 2026-09-17

**Status**: Draft

**Input**: User description: "Let users set a priority of Low, Medium, or High on a task when creating or editing it. In the task list, users can filter tasks by priority and by completion status, and sort by priority. Tasks that existed before this change are treated as Medium priority. Clients that don't send a priority must keep working, existing API responses must not break, and users only ever see their own tasks."

## Clarifications

### Session 2026-09-17

- Q: When a user sorts the task list by priority, which order should tasks appear in by default? → A: High to Low
- Q: Once a task has a priority set, can a user clear it back to "no priority," or must every task always carry one of Low, Medium, or High? → A: Always one of Low/Medium/High — priority can never be cleared to empty
- Q: When a user applies both a priority filter and a completion-status filter at the same time, should the list show tasks matching both conditions, or tasks matching either one? → A: Both must match (AND)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Set a Task's Priority (Priority: P1)

A user creating a new task, or editing an existing one, chooses a priority of Low, Medium, or
High so that important tasks stand out from the rest of their list.

**Why this priority**: Priority is the foundational data point the rest of the feature (filtering,
sorting) depends on. Without the ability to set it, filtering and sorting by priority have
nothing to act on.

**Independent Test**: Can be fully tested by creating a task with each of the three priority
values and by editing an existing task to change its priority, then confirming the saved value
is reflected back to the user — delivers value on its own even before filtering/sorting exist.

**Acceptance Scenarios**:

1. **Given** a user is creating a new task, **When** they select "High" as the priority and save,
   **Then** the task is created with High priority.
2. **Given** a user is creating a new task, **When** they save without choosing a priority,
   **Then** the task is created with Medium priority.
3. **Given** a user is editing an existing task, **When** they change its priority from Medium to
   Low and save, **Then** the task reflects Low priority afterward.
4. **Given** a user attempts to set a task's priority, **When** the task belongs to another user,
   **Then** the change is rejected and no other user's task is modified.
5. **Given** a user is editing an existing High-priority task, **When** they save without
   changing the priority field, **Then** the task remains High priority.

---

### User Story 2 - Filter the Task List by Priority and Completion Status (Priority: P2)

A user viewing their task list narrows it down to just the tasks that match a priority (or set of
priorities) and/or a completion status, so they can focus on what matters right now.

**Why this priority**: Filtering is the primary way priority data becomes useful day-to-day, once
tasks can actually carry a priority (User Story 1).

**Independent Test**: Can be fully tested by seeding a user's task list with tasks of varying
priority and completion status, then confirming each filter (and combinations of filters) returns
only the matching tasks.

**Acceptance Scenarios**:

1. **Given** a user has tasks of all three priorities, **When** they filter by "High", **Then**
   only High-priority tasks are shown.
2. **Given** a user has both complete and incomplete tasks, **When** they filter by "incomplete",
   **Then** only incomplete tasks are shown.
3. **Given** a user applies both a priority filter and a completion-status filter, **When** the
   filters are combined, **Then** only tasks matching both conditions are shown.
4. **Given** a user's filter matches no tasks, **When** the filter is applied, **Then** the list
   is shown empty rather than showing unrelated tasks.

---

### User Story 3 - Sort the Task List by Priority (Priority: P3)

A user viewing their task list orders it by priority so that High-priority tasks are easy to find
at a glance.

**Why this priority**: Sorting is a convenience layered on top of filtering — valuable, but the
list is still usable via filtering alone without it.

**Independent Test**: Can be fully tested by seeding a user's task list with mixed priorities and
confirming the returned order matches the requested priority sort.

**Acceptance Scenarios**:

1. **Given** a user has tasks of all three priorities, **When** they sort the list by priority,
   **Then** tasks are ordered from High to Low priority.
2. **Given** a user has multiple tasks with the same priority, **When** the list is sorted by
   priority, **Then** those tasks keep a stable, consistent relative order.
3. **Given** a user sorts by priority while a filter is also applied, **When** the list is
   displayed, **Then** it reflects both the filter and the sort order together.

---

### Edge Cases

- What happens to tasks created before this feature existed? They MUST display and behave as
  Medium priority, without requiring the user to manually update them.
- What happens when an existing client creates or updates a task without sending a priority
  value at all? The request MUST succeed; a new task defaults to Medium priority, and an edited
  task keeps its current priority unchanged.
- What happens when a user filters or sorts a list that contains no tasks? The list is shown
  empty; no error occurs.
- What happens when a user tries to filter, sort, or view priority data for a task they don't
  own? The task MUST NOT appear in their results and MUST NOT be exposed to them.
- What happens when a user tries to remove or blank out a task's priority? The request MUST be
  ignored — the task's current priority is preserved — a task always carries exactly one of
  Low, Medium, or High.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to set a task's priority to Low, Medium, or High when creating
  a task.
- **FR-002**: Users MUST be able to change a task's priority to Low, Medium, or High when editing
  an existing task; omitting priority on an edit MUST leave the task's current priority
  unchanged (see FR-012).
- **FR-003**: System MUST default a new task's priority to Medium whenever a create request does
  not include a priority value.
- **FR-004**: System MUST treat every task that existed before this feature shipped as Medium
  priority.
- **FR-005**: Users MUST be able to filter their task list by priority (one, several, or all of
  Low/Medium/High).
- **FR-006**: Users MUST be able to filter their task list by completion status (complete or
  incomplete).
- **FR-007**: Users MUST be able to apply a priority filter and a completion-status filter at the
  same time, seeing only tasks that satisfy both.
- **FR-008**: Users MUST be able to sort their task list by priority, ordered High → Medium →
  Low by default.
- **FR-009**: System MUST continue to accept task create/edit requests that omit priority,
  without error — treating the omission as Medium on creation (FR-003) and as no change to an
  existing task's priority on edit (FR-002) — existing clients MUST NOT need to change.
- **FR-010**: System MUST add priority as new, additive information only — existing fields,
  meanings, and structure of current task responses MUST remain unchanged for clients that don't
  ask for priority.
- **FR-011**: System MUST only ever show, filter, sort, or modify tasks owned by the
  authenticated user making the request, regardless of the priority or completion-status values
  involved.
- **FR-012**: System MUST NOT allow a task's priority to be cleared to empty; every create or
  edit that changes priority MUST set it to one of Low, Medium, or High.

### Key Entities

- **Task**: Represents a unit of work owned by a single user. Gains one new attribute, priority
  (Low, Medium, or High), in addition to its existing attributes (e.g., description, completion
  status, ownership). Every task has exactly one priority value at all times.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can set or change a task's priority in the same action they already use to
  create or edit a task, with no additional steps beyond picking one of three options.
- **SC-002**: 100% of tasks that existed before this feature shipped display as Medium priority
  once the change is live.
- **SC-003**: A user can narrow a task list of any size down to just the tasks matching a chosen
  priority and completion status combination in a single filtering action.
- **SC-004**: A user can reorder any task list by priority in a single action, with High-priority
  tasks always appearing before Medium, and Medium always before Low.
- **SC-005**: Existing clients that do not send a priority value continue to create and edit tasks
  successfully with zero new failures after this feature ships.

## Assumptions

- Priority filtering and sorting apply within a single user's own task list only; there is no
  cross-user or admin-level view in scope.
- Filtering by multiple priorities at once (e.g., "High or Medium") is treated as an inclusive
  match (task shown if its priority is any of the selected values); combining a priority filter
  with a completion-status filter follows the AND rule in FR-007.
- "Completion status" refers to the task's existing complete/incomplete state; no new status
  values are introduced by this feature.
- No new user-facing roles or permissions are introduced; the existing rule that users only see
  their own tasks continues to apply unchanged.

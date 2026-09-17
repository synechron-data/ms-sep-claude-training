# Add Task Due Dates

> **Execution instruction:** When a step below is completed, mark its checkbox `[x]` in this file immediately before moving on to the next step. Do not batch status updates until the end.

## Context

Spec: `manual/feature.task-due-date.md`. Users should be able to optionally set a due date on a task, and incomplete overdue tasks should be visually distinguishable in the task list. `Task` currently has no `dueDate` field; `CreateTaskRequest`/`UpdateTaskRequest` (records nested in `TaskController`) and `TaskService.create`/`update` take positional primitive params (no DTO layer, no separate request/response objects). The frontend `TaskListComponent` is a standalone component with an inline template/styles (no separate `.html`/`.scss`), and `TaskService` exposes a single `Task` interface with positional-arg HTTP methods.

## Approach

- Add `dueDate` as a nullable `LocalDate` field on the `Task` entity — no `@Column(nullable = false)`, consistent with `description` being optional today.
- Compute the `overdue` flag **server-side** as an `isOverdue()` getter on `Task` (`dueDate != null && !completed && dueDate.isBefore(LocalDate.now())`), which Jackson auto-serializes as `overdue` in the JSON response since `Task` is returned directly (no response DTO exists to add it to). Confirmed with the user: server-side over client-side computation, to keep due-date logic as a single source of truth.
- Thread `dueDate` through `TaskService.create`/`update` as another positional parameter, continuing the existing style rather than introducing a request-object refactor.
- No new repository query method — overdue is computed per-row on read, not filtered server-side; not required by the spec.
- Do not touch the known, intentional IDOR gap in `TaskService.markCompleted`/`delete` (missing ownership check) — out of scope for this feature.
- Frontend: extend the existing `Task` interface and `TaskService` methods with `dueDate`, add a date input to the inline template's create/edit rows, and render the due date with an `overdue`-driven CSS class using the backend's computed flag (no client-side recomputation).

## Implementation

- [x] 1. **`backend/src/main/java/com/technizer/taskapi/task/Task.java`**
   - Add `private LocalDate dueDate;` with getter/setter.
   - Add `public boolean isOverdue()` returning `dueDate != null && !completed && dueDate.isBefore(LocalDate.now())` (no backing field — computed, Jackson will serialize it as `overdue`).
   - Import `java.time.LocalDate`.
   - Leave the existing `Task(String title, String description, Long ownerId)` constructor unchanged; `dueDate` is set via the setter after construction.

- [x] 2. **`backend/src/main/java/com/technizer/taskapi/task/TaskController.java`**
   - Add `LocalDate dueDate` to `CreateTaskRequest` and `UpdateTaskRequest` records.
   - Pass `request.dueDate()` through to `taskService.create(...)` / `taskService.update(...)`.
   - Import `java.time.LocalDate`.

- [x] 3. **`backend/src/main/java/com/technizer/taskapi/task/TaskService.java`**
   - Add `LocalDate dueDate` as an additional positional parameter to `create(Long ownerId, String title, String description, LocalDate dueDate)` and `update(Long ownerId, Long taskId, String title, String description, LocalDate dueDate)`.
   - Set it on the `Task` via the setter in both methods.

- [x] 4. **`backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java`**
   - Update existing `create`/`update` call sites to pass a `dueDate` argument (or `null`) so the file still compiles.
   - Add cases: `create` persists `dueDate`; `update` persists a changed `dueDate`; `isOverdue()` is `true` for a past date on an incomplete task, `false` when `completed`, `false` when `dueDate` is `null`, `false` for a future date.
   - Do not remove/modify the existing placeholder/ownership tests per CLAUDE.md.

- [x] 5. **`backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java`**
   - Update existing call sites for the new `dueDate` param.
   - Add a case asserting `dueDate` flows from `CreateTaskRequest`/`UpdateTaskRequest` into the `TaskService` call.

- [x] 6. **`frontend/src/app/tasks/task.service.ts`**
   - Add `dueDate: string | null` and `overdue: boolean` to the `Task` interface.
   - Add a `dueDate: string | null` param to `create(...)` and `update(...)`, including it in the POST/PUT request bodies.

- [x] 7. **`frontend/src/app/tasks/task-list.component.ts`**
   - Add `newDueDate` and `editDueDate` component fields (`string`, empty by default).
   - Add `<input type="date">` bound to `newDueDate` in the "new task" row, and to `editDueDate` in the edit `ng-container`.
   - Update `addTask()` and `saveEdit()` to pass the due date through to `taskService.create`/`update`; update `startEdit()` to populate `editDueDate` from `task.dueDate`.
   - In `viewMode`, render the due date next to the title and add `[class.overdue]="task.overdue"` on the `<li>` (or a dedicated span), with a new `.overdue` style rule (e.g. red text) in the component's inline `styles` array.

- [x] 8. **`frontend/src/app/tasks/task-list.component.spec.ts`**
   - Extend the `task` fixture with `dueDate`/`overdue` fields.
   - Add/extend assertions that `saveEdit`/`addTask` call `TaskService.update`/`create` with the due date.
   - Add a rendering test that an overdue task gets the `overdue` class/style and a non-overdue one does not.

- [x] 9. **`frontend/src/app/tasks/task.service.spec.ts`**
   - Extend the `HttpTestingController`-based tests to assert `dueDate` is included in the `create`/`update` request bodies.

## Verification

- [x] `mvn test` (backend) — new and updated `TaskServiceTest`/`TaskControllerTest` cases pass, no existing tests broken.
- [x] `ng test` (frontend) — updated `task-list.component.spec.ts`/`task.service.spec.ts` pass (8/8 SUCCESS).
- [x] Manually run the app: create a task with a past due date, confirm the list shows it as overdue (red bold "Due 2026-01-01"); create one with a future due date (2026-12-31), confirm it renders normally, not overdue; mark the overdue task complete and confirm the overdue styling clears (strikethrough, no red).
- [x] Confirm a task created without a due date (existing behavior) still displays and behaves normally — created "No due date task" with no dueDate, shows no due-date text, behaves like any other task.

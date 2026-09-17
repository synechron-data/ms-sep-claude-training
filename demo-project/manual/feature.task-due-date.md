## Feature: Task Due Dates

### Requirement
Users can optionally set a due date on a task. Overdue incomplete tasks
should be visually distinguishable in the task list.

### Acceptance Criteria
- [ ] Task entity has an optional dueDate field
- [ ] POST /api/tasks accepts an optional dueDate
- [ ] GET /api/tasks response includes dueDate and a computed `overdue` flag
- [ ] Angular task list shows overdue tasks with a visual indicator
- [ ] Existing tasks without a due date are unaffected
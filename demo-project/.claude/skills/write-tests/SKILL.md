---
name: write-tests
description: >
  Use this skill automatically whenever a new method, endpoint, or
  component is added or modified in this Spring Boot / Angular codebase.
  Do not wait to be asked.
---

# Write Tests Skill

Backend: JUnit 5, tests mirror src/main/java under src/test/java, class
name Test-suffixed. Frontend: Jasmine specs alongside the component/service.

For each new or modified unit, generate tests covering:
1. The happy path
2. Edge cases: null, empty, boundary values
3. Error cases and expected exceptions/HTTP statuses

Run the relevant test command after writing to confirm everything passes.
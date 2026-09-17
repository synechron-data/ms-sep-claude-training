---
name: code-review
description: >
  Use this skill when the task involves reviewing, auditing or assessing
  code quality, security or test coverage in this Spring Boot / Angular codebase.
  Triggers on phrases like "review this", "check this code", "audit the task module"
  or any request to evaluate the existing code.
---

# Code Review Skill

Cover all four dimensions, in order:

## 1. Security
- Authorization checks on every endpoint (watch for IDOR on per-user data)
- JWT validation correctness (signature AND expiry)
- No secrets or credentials in code or logs

## 2. Performance
- N+1 query patterns in JPA repositories
- Missing @Transactional boundaries where needed
- Unnecessary synchronous calls on the Angular side

## 3. Test Coverage
- List methods/functions with no test coverage
- Identify missing edge cases
- Suggest specific test cases with example inputs/outputs

## 4. Code Quality
- Flag violations of CLAUDE.md standards
- Duplicated logic that should be extracted
- Missing error handling

Output as a structured report with severity (Critical/High/Medium/Low)
per finding. End with a score out of 10 and one recommended first action.
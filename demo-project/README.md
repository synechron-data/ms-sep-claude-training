# Task Manager — Claude Code Training Demo Repo

A small, real, full-stack app used as the live demo project for the Claude Code
training. It's intentionally simple: a Task Manager with email/password auth
and a per-user task list.

- **Backend:** Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, H2 (in-memory), JWT auth
- **Frontend:** Angular 17 (standalone components), TypeScript

## Structure

```
demo-project/
├── backend/     ← Spring Boot REST API (port 8080)
├── frontend/    ← Angular app (port 4200)
└── README.md
```

This repo does **not** include a `CLAUDE.md`, `.claude/` folder, or a
Spec Kit setup — those are created live by participants during the session
(Modules 4, 6, 7, 8, 9–10, and 11). Do not pre-create them.

## Prerequisites

- Java 17+ and Maven 3.9+ (or use the included `mvnw` wrapper if you add one)
- Node.js 18+ and npm
- Claude Code installed and authenticated (`npm install -g @anthropic-ai/claude-code`)
- A GitHub repo this project has been pushed to (needed for the GitHub MCP
  and bug-lifecycle demo in Module 13)
- A Jira project/board (needed for the Jira MCP demo in Module 13)

## Running the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. The H2 console is available at
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskdb`, user `sa`,
blank password) if you want to show participants the raw data.

## Running the frontend

```bash
cd frontend
npm install
npm start
```

The app starts on `http://localhost:4200` and expects the backend to be
running on port 8080 (CORS is already configured for this).

## API quick reference

```
POST /api/auth/register   { email, password, displayName }  → { token }
POST /api/auth/login      { email, password }                → { token }
GET    /api/tasks                                             (auth required)
POST   /api/tasks         { title, description }              (auth required)
PATCH  /api/tasks/{id}/complete                                (auth required)
DELETE /api/tasks/{id}                                         (auth required)
```

All `/api/tasks/**` endpoints require `Authorization: Bearer <token>`.

## Deliberate issues (for training demos — do not fix before the session)

These are intentional and are the basis for specific module demos. See the
Trainer Handbook for exactly where each one is used.

1. **`JwtUtil.isTokenValid()`** (backend) always returns `true` — expired
   tokens are still accepted. Used in the Module 13 `/security-review` →
   `/create-issue` → `/fix-issue` → `/commit` bug-lifecycle demo.
2. **`TaskService.markCompleted()` and `.delete()`** (backend) do not check
   that the task belongs to the requesting user (broken object-level
   authorization / IDOR). Used as a secondary finding in the same demo, or
   as a standalone code-review skill demo in Module 6.
3. **`app.jwt.secret`** (backend `application.properties`) is a hardcoded
   demo secret committed to the repo. Used to illustrate the `deny` list in
   `settings.json` (Module 4) and as a finding in the code-review skill
   (Module 6).
4. **Test coverage is placeholder-only** (`TaskServiceTest.java`,
   `AuthServiceTest.java`) — used for the auto test-writing skill demo
   (Module 6) and as a Spec Kit implementation target (Modules 9–10).

## Trainer setup checklist

- [ ] Push this repo to a real GitHub repository (private is fine) before
      the session — Module 13's GitHub MCP demo needs a live remote
- [ ] Create a matching Jira project/board with at least one sample issue
      type configured — Module 13's Jira MCP demo needs a live project
- [ ] Confirm `mvn spring-boot:run` and `npm start` both work on the
      training machine the day before
- [ ] Do **not** run `/security-review` or fix the deliberate bugs before
      the session

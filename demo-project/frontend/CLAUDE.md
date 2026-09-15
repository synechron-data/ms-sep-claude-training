# Frontend — CLAUDE.md

Angular app for the Task Manager. See the root `CLAUDE.md` for the overall
project/tech stack; this file covers frontend-specific detail.

## Commands

Run from `frontend/`:

```bash
npm install
npm start                  # ng serve on http://localhost:4200
npm test                   # karma/jasmine unit tests
npm run build
npm run watch               # build --watch, development config
```

The frontend expects the backend on port 8080; CORS is preconfigured for
`http://localhost:4200` only (see backend `SecurityConfig.corsConfigurationSource()`).

## Architecture

Standalone Angular components wired up via `app.config.ts` (no `AppModule`).

- **Routing** (`app.routes.ts`): `''` → `login`, plus `login` and `tasks` routes. No
  route guard yet — `tasks` is reachable without a token; the backend still rejects
  unauthenticated API calls.
- **Auth:** `auth/auth.service.ts` stores the JWT and exposes it via `getToken()`.
  `core/auth.interceptor.ts` is a functional `HttpInterceptorFn` registered in
  `app.config.ts` via `provideHttpClient(withInterceptors([authInterceptor]))` — it
  attaches `Authorization: Bearer <token>` to every outgoing HTTP request when a
  token exists.
- **Tasks:** `tasks/task.service.ts` wraps the `/api/tasks` endpoints;
  `tasks/task-list.component.ts` is the main authenticated view.

## Coding standards

- Standalone components — no NgModules for new features.
- Services own HTTP calls; components never call `HttpClient` directly.

## Testing requirement

For every code change — new code or modification of existing code — add or update
the corresponding Jasmine/Karma test(s) in the same change, covering the new/changed
behavior (including relevant edge cases). Do not consider a change complete until
its tests are added/updated. This applies to bug fixes too, unless the user
explicitly asks to skip it for a given change.

## What Claude must never do

- Never install/upgrade npm dependencies without confirming first.
- Never remove existing tests.

## Known, intentional issues — do not fix unless asked

This repo ships with deliberate bugs used in live training exercises, mostly on the
backend (see `backend/CLAUDE.md`). On the frontend specifically: the `tasks` route
has no auth guard, so an unauthenticated user can navigate to it directly (API calls
will still fail without a token). Don't "clean this up" as a drive-by fix; only touch
it if explicitly asked.

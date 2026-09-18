# Bug & Feature Lifecycle — Jira + Spec Kit + Claude Code

*End-to-end workflow: Jira tracks the work, Spec Kit structures it, GitHub holds the code and pull requests, and Claude Code does the work.*

> **Sources:** [Spec Kit bug fixing quickstart](https://github.github.com/spec-kit/guides/bugfix.html), [Spec Kit quickstart](https://github.github.com/spec-kit/quickstart.html), [Atlassian Rovo MCP Server](https://github.com/atlassian/atlassian-mcp-server), [Claude Code MCP docs](https://code.claude.com/docs/en/mcp), [Claude Code agent teams](https://code.claude.com/docs/en/agent-teams)
> **Related notes:** `spec-kit-installation.md`, `spec-kit-workflow.md`

## The Big Picture

| Tool | Role |
|---|---|
| **Jira** (via Atlassian MCP) | Source of truth for work items: bugs, stories, sub-tasks, status, comments |
| **Spec Kit** | Structure: `assess → fix → test` for bugs; `specify → plan → tasks → implement → converge` for features |
| **GitHub** (via `gh`) | Branches, pull requests, merges |
| **Claude Code** | Runs every step: audits, Jira updates, Spec Kit skills, commits, reviews |

**Linking rule used throughout:** put the **Jira key** (e.g. `TASK-12`) in the branch name, the commit message, and the PR title. If your Jira site has the GitHub for Jira app installed, Jira then shows the branch, commits and PR on the issue automatically.

Two flows:

- **Part A — Bug:** Jira Bug → Spec Kit bug extension → PR → agent-team review → merge → Jira Done
- **Part B — Feature:** Jira Story → Spec Kit feature workflow → Jira sub-tasks → PR → merge → Jira Done

---

## Prerequisites (one-time setup)

### 1. Spec Kit in the repository

Follow `spec-kit-installation.md`, then add the bug extension:

```bash
specify init --here --force --integration claude     # if not already initialized
specify extension add bug                            # adds /speckit-bug-assess, -fix, -test
```

Commit the generated `.specify/` and `.claude/skills/` folders.

### 2. Jira project setup

Everything in this guide assumes a Jira project that has the right key, issue types, statuses and fields. Set it up once, in the browser, as a Jira admin (you need **Administer projects** permission, or site admin to create a project).

> **Naming in the new Jira UI:** projects are shown as **spaces** and issue types as **work types**. "Project settings" is **Space settings**. The steps below give both names where they differ.

#### 2.1 Create the project (or reuse one)

**New project:**

1. In Jira, go to **Projects (Spaces) → Create project**.
2. Pick **Software development → Scrum** (or **Kanban**).
3. Choose **Team-managed**. The steps below are for team-managed projects; company-managed projects configure the same things through shared schemes instead.
4. Name it, and set the **Key** to `TASK` (or your own key).
5. Click **Create**.

**Reusing an existing, empty project:** open **Space settings → Details**, change **Key** to `TASK`, and save. Jira keeps the old key as an alias, so old links still work.

**Choosing a key:** avoid JQL reserved words such as `FOR`, `AND`, `OR`, `NOT`, `IN`, `IS`, `ORDER`, `BY`, `EMPTY` and `NULL`. Jira allows them as keys, but then every search has to quote the key (`project = "FOR"`), and unquoted searches from Claude fail with *"'FOR' is a reserved JQL word"*.

#### 2.2 Check the issue (work) types

Open **Space settings → Work types**. You need:

| Work type | Used in |
|---|---|
| **Bug** | Part A |
| **Story** | Part B |
| **Subtask** | Part B, B7 (one per `tasks.md` phase) |

Scrum and Kanban templates include all three. If one is missing: **Add work type** and pick it.

#### 2.3 Check the workflow statuses

This guide moves issues **To Do → In Progress → In Review → Done**. Templates often have only To Do / In Progress / Done, so check **In Review**.

In a team-managed project, each work type has its own workflow. Do this for **Bug**, **Story** and **Subtask**:

1. **Space settings → Work types → Bug → Edit workflow**.
2. If **In Review** is missing: **Add status** → name it `In Review`, category **In progress**.
3. Tick **Allow all statuses to transition to this one** for it (and check the other statuses allow it too), so Claude can move an issue straight to any status.
4. Click **Update workflow**.

Use exactly these names — the prompts in Parts A and B refer to them.

#### 2.4 Add the Priority field

Step A2 sets `Priority: Highest`. Team-managed projects often leave Priority off the work types, and then the value is silently ignored or the create fails.

For **Bug**, **Story** and **Subtask**:

1. **Space settings → Work types → Bug**.
2. In the **Fields** panel on the right, under **System fields**, find **Priority**.
3. Drag it into **Context fields** (e.g. under Assignee).
4. Click **Save changes**.

Also check that these are already under **Description fields** / **Context fields** on each type: **Summary**, **Description**, **Assignee**, **Labels** (for `security`), and **Parent** (Subtasks link to their Story through it).

#### 2.5 Check permissions

**Space settings → Access.** Everyone who will run this workflow through Claude Code needs to be able to create, edit, transition, assign and comment on issues — in team-managed projects the **Member** role covers this (**Administrator** also works). Claude acts as the signed-in user, so it can only do what that person can do.

#### 2.6 Connect GitHub (GitHub for Jira)

This is what shows branches, commits and PRs on the Jira issue when the key is in their names. You need to be a **Jira admin** and an **owner of the GitHub organization** (or an owner-approved request) to do this.

**Install the app (skip if already installed):**

1. In Jira, **⚙ Settings → Apps → Explore more apps**, search **GitHub for Jira**, and install it.
2. It then appears in the admin sidebar as **GitHub for Atlassian**.

**Connect a GitHub organization:**

1. Go to **⚙ Settings → Apps → Marketplace apps → GitHub for Atlassian → Configure**.
   Direct link: `https://<site>.atlassian.net/plugins/servlet/ac/com.github.integration.production/github-post-install-page`
2. On **Connect GitHub to Atlassian**, check you have a GitHub account and owner permission on the organization, then click **Continue**.
3. Select **GitHub Cloud** (or GitHub Enterprise Server if you self-host) → **Next**. A GitHub window opens.
4. On **Authorize Atlassian**, pick the GitHub account to authorize with → **Continue** (or **Select** for another signed-in account). The app requests the `user` and `repo` scopes.
5. Choose the **organization** to connect. If the Jira app isn't installed on it yet, GitHub asks you to install it:
   - Pick **Only select repositories** and choose the repository for this workflow (recommended), or **All repositories**.
   - Click **Install** (or **Request**, if you're not an org owner — an owner must then approve it).
6. You land back on Jira's **GitHub configuration** page.

**Check the connection** — the **Connections** tab shows one row per organization:

| Column | Expect |
|---|---|
| Connected organization | Your GitHub org |
| Repository access | **Only select repos** (click it to see which) or **All repos** |
| Backfill status | **FINISHED** (shows *In progress* for a few minutes while existing branches and commits are scanned) |
| Permissions | **FULL ACCESS** |

To add more repositories later, click the ✏ edit icon next to **Repository access**. To connect another org, use **Connect a GitHub organization** at the top right.

Installing the app alone is not enough — nothing links until an organization is connected. Once it is, a branch, commit or PR in a connected repo with `TASK-12` in its name/message/title appears in the **Development** panel of `TASK-12`.

#### 2.7 Verify the project

Signed in to Jira in your browser, open these (read-only) URLs, replacing `<site>`:

| Check | URL | Expect |
|---|---|---|
| Key | `https://<site>.atlassian.net/rest/api/3/project/TASK` | `"key":"TASK"` |
| Work types | `https://<site>.atlassian.net/rest/api/3/issue/createmeta/TASK/issuetypes` | Bug, Story, Subtask |
| Statuses | `https://<site>.atlassian.net/rest/api/3/project/TASK/statuses` | To Do, In Progress, In Review, Done for each type |
| Priority on Bug | `https://<site>.atlassian.net/rest/api/3/issue/createmeta/TASK/issuetypes/<bug-id>` | a `priority` field with Highest … Lowest |

Or, once the MCP server (step 3) is connected, ask Claude:

```
For Jira project TASK, list the work types, the statuses for Bug, Story and Subtask, and whether Priority and Labels can be set when creating a Bug. Don't change anything.
```

### 3. Jira — Atlassian Rovo MCP Server

This lets Claude Code read and update Jira (create issues, comment, transition, assign) as you.

#### 3.1 Check the site allows it (organization admin, one-time)

Go to **admin.atlassian.com → (your organization) → Rovo → Rovo MCP server**. It has three tabs:

| Tab | Setting | Needed for this guide |
|---|---|---|
| **Domains** | **Allow Atlassian supported domains** | **On.** Claude is a supported AI tool, so nothing needs adding under *Your domains*. If it's off, an admin must turn it on (or add a domain) or sign-in is refused. |
| **Permissions** | **Read**, **Write**, **Search** toolsets | **Allowed.** Write covers create, comment, transition and assign. |
| | **Delete**, **Manage** | Can stay **blocked** — this guide never deletes issues or changes project configuration. |
| **Authentication** | OAuth 2.1 (default) | Enough for interactive use. Turn on **Allow API token authentication** only for headless/CI use. |

If **Rovo MCP server** isn't in the sidebar, you aren't an organization admin — ask one to check these settings.

#### 3.2 Add the server to Claude Code

From a terminal in your repository:

```bash
claude mcp add --transport http atlassian https://mcp.atlassian.com/v2/mcp
```

| Scope | Command | Stored in | Use when |
|---|---|---|---|
| Local (default) | as above | `~/.claude.json`, for this folder only | Just you, just this repo |
| Project | add `--scope project` | `.mcp.json` in the repo (commit it) | The whole team should get the server — each person still signs in |
| User | add `--scope user` | `~/.claude.json`, all folders | You want Jira in every project |

Check it was added:

```bash
claude mcp get atlassian
```

Expect `Type: http`, the `/v2/mcp` URL, and `Status: ! Needs authentication`.

**Worked example — project scope for the team repo (`demo-project`):**

1. Run the command from the **repository root** (project scope writes to the folder you run it in):

   ```bash
   cd demo-project
   claude mcp add --scope project --transport http atlassian https://mcp.atlassian.com/v2/mcp
   ```

2. This creates `demo-project/.mcp.json`:

   ```json
   {
     "mcpServers": {
       "atlassian": {
         "type": "http",
         "url": "https://mcp.atlassian.com/v2/mcp"
       }
     }
   }
   ```

   It holds only the server URL — no tokens or credentials — so it's safe to commit.

3. Check `.gitignore` doesn't exclude `.mcp.json`, then commit it:

   ```bash
   git add .mcp.json
   git commit -m "chore: add Atlassian MCP server for Jira"
   ```

4. Each teammate who opens Claude Code in the repo:
   - approves the project's MCP servers when prompted the first time (choose **Use this and all future MCP servers in this project**, or approve `atlassian` in `/mcp`),
   - signs in with **their own** Atlassian account (`/mcp` → `atlassian` → **Authenticate**, see 3.3). Claude then acts as that person in Jira.

A server with the same name at **local** scope takes precedence over the project one in that folder. If you added `atlassian` locally earlier and want the shared config instead, remove the local one from that folder: `claude mcp remove atlassian -s local`.

- Use `/v2/mcp`. The older endpoints (`/v1/sse`, and `/v1/mcp` still used by some plugins) are not supported after 30 June 2026.
- If a plugin already provides an Atlassian server (e.g. `plugin:design:atlassian`), you'll get two sets of Jira tools once both are signed in. Disable the plugin's one in `/mcp`, or leave it unauthenticated.

#### 3.3 Sign in

1. **Restart Claude Code** in the repository so it loads the new server.
2. Run `/mcp`, select **atlassian**, then **Authenticate**.
3. A browser window opens on Atlassian. Sign in if asked, pick your **site** (e.g. `technizeredge`), review the access requested, and click **Accept**.
4. The browser shows a success page; back in Claude Code, `/mcp` now lists `atlassian` as **connected**.

If the browser ends on a `http://localhost:<port>/callback?...` page that fails to load, copy the full URL from the address bar and paste it into Claude Code when asked.

Claude acts as **you** — it can only see and change what your Jira account can.

#### 3.4 Verify

Inside Claude Code:

```
List the Jira projects I can access.
```

Expect your project (this guide uses `TASK`). Then confirm it matches the project setup in step 2:

```
For Jira project TASK, list the work types, the statuses for Bug, Story and Subtask, and whether Priority and Labels can be set when creating a Bug. Don't change anything.
```

#### 3.5 Troubleshooting

| Symptom | Fix |
|---|---|
| `atlassian` missing from `/mcp` | Restart Claude Code in the same folder you ran `claude mcp add` from (local scope is per-folder), or check `claude mcp list` |
| Sign-in refused / "domain not allowed" | Admin: turn on **Allow Atlassian supported domains** (3.1) |
| Can read but create/transition fails | Admin: allow the **Write** toolset (3.1); also check your project role (step 2.5) |
| Project not listed | Your account doesn't have access to it — check **Space settings → Access** |
| Search errors mentioning a *reserved JQL word* | The project key needs quoting, or change it (step 2.1) |
| Headless / CI | Admin enables **Allow API token authentication** (3.1), then configure the server with an API token instead of OAuth |

### 4. GitHub — `gh` CLI and GitHub MCP Server

#### 4.1 `gh` CLI (required)

The PR and merge steps use `gh`:

```bash
gh auth login
gh auth status
```

#### 4.2 GitHub MCP Server (optional)

`gh` covers everything in this guide. Add GitHub's MCP server too if you want Claude to work with GitHub through tools rather than shell commands — reading PR diffs and review comments, posting the agent-team review on the PR (A9), checking CI runs before merge (A11), or managing GitHub issues.

Use GitHub's **official remote server** (`https://api.githubcopilot.com/mcp/`). Don't use the old `@modelcontextprotocol/server-github` npm package — it's archived and unmaintained (and on Windows `npx` also needs a `cmd /c` wrapper).

GitHub's server uses a **personal access token**, not a browser sign-in.

**Step 1 — Create a fine-grained token (each person)**

1. Sign in to **github.com** with the account you'll use for the demo.
2. Click your **profile picture** (top right) → **Settings**.
3. In the left sidebar, scroll to the bottom → **Developer settings**.
4. **Personal access tokens → Fine-grained tokens** → **Generate new token**.
   Direct link: `https://github.com/settings/personal-access-tokens/new`
   (GitHub may ask you to confirm your password or 2FA — do that yourself.)
5. Fill in the top of the form:
   - **Token name:** e.g. `claude-code-mcp`
   - **Description:** e.g. `Claude Code GitHub MCP for ms-sep-claude-training`
   - **Resource owner:** choose the **organization** that owns the repo (e.g. `synechron-data`), not your personal account — otherwise the org repo won't appear in the next step.
   - **Expiration:** 30 days (or a custom short date).
6. **Repository access:** select **Only select repositories** → pick your repo (e.g. `ms-sep-claude-training`).
7. **Permissions → Repository permissions** — click **Add permissions** (or expand the list) and set:

   | Permission | Access | Used for |
   |---|---|---|
   | **Contents** | Read and write | Read files, branches, commits |
   | **Pull requests** | Read and write | Read PRs and diffs, post review comments (A9) |
   | **Issues** | Read and write | Read/comment on GitHub issues |
   | **Actions** | Read-only | Check CI runs before merge (A11) |
   | **Metadata** | Read-only | Added automatically — required |

   Leave **Account permissions** and **Organization permissions** at *No access*.
8. Click **Generate token** (review the summary if prompted, then confirm).
9. **Copy the token now** (starts with `github_pat_`) — GitHub shows it only once. Go straight to Step 2; don't paste it into chat, notes or files.

**If the organization requires approval:** the token shows **Pending** under *Fine-grained tokens* and can't access org repos yet. An org owner approves it in **Organization → Settings → Personal access tokens → Pending requests**. If the org blocks fine-grained tokens entirely, an owner must allow them in **Organization → Settings → Personal access tokens → Settings**.

**Renewing:** when it expires, open the token under **Fine-grained tokens → Regenerate token**, then update `GITHUB_PAT` (Step 2).

**Step 2 — Store the token in an environment variable (each person)**

Never put the token in a file you commit.

Windows (PowerShell):

```powershell
setx GITHUB_PAT "github_pat_xxx"
```

macOS / Linux (add to `~/.zshrc` or `~/.bashrc`):

```bash
export GITHUB_PAT="github_pat_xxx"
```

Open a **new terminal** afterwards so the variable is loaded.

**Step 3 — Add the server to the project (once, committed)**

Add a `github` entry to the repo's `.mcp.json`, next to `atlassian` (3.2). For `demo-project` the file becomes:

```json
{
  "mcpServers": {
    "atlassian": {
      "type": "http",
      "url": "https://mcp.atlassian.com/v2/mcp"
    },
    "github": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/",
      "headers": {
        "Authorization": "Bearer ${GITHUB_PAT}"
      }
    }
  }
}
```

Claude Code replaces `${GITHUB_PAT}` with each person's own environment variable when it starts the server, so the committed file never contains a token.

Or, just for yourself (stores the real token in `~/.claude.json` — don't use for shared config):

```bash
claude mcp add --transport http github https://api.githubcopilot.com/mcp/ -H "Authorization: Bearer $GITHUB_PAT"
```

Check the config:

```bash
claude mcp get github
```

Expect `Scope: Project config (shared via .mcp.json)`, the URL, and the header showing `Bearer ${GITHUB_PAT}` (the variable, not the token).

**Step 4 — Approve and verify**

1. Start Claude Code in the repo (from the new terminal).
2. Approve the `github` project server when prompted (or in `/mcp`).
3. `/mcp` should show `github` as **connected**.
4. Ask:

   ```
   List the open pull requests in synechron-data/ms-sep-claude-training.
   ```

**Troubleshooting**

| Symptom | Fix |
|---|---|
| `401 Unauthorized` / fails to connect | `GITHUB_PAT` isn't set in the terminal Claude Code was started from — open a new terminal after `setx`, check with `echo $env:GITHUB_PAT` (PowerShell) |
| Server stays **Pending approval** | Start `claude` in the repo and approve it, or approve in `/mcp` |
| Repo not found / `403` | Token doesn't include the repo, lacks a permission, or the org hasn't approved it yet |
| Stops working after a few weeks | Token expired — generate a new one and update `GITHUB_PAT` |

**Permissions:** as with Jira, keep GitHub write tools (merge, push, create/close) as **ask** — see step 7.

### 5. Team commands and agents

This guide uses the handbook's team commands and agents:

| Used here | From |
|---|---|
| `/commit`, `/raise-pr`, `/standup` | `.claude/commands/` (handbook Module 6) |
| `@agent-security-analyst` and the other analyst agents | `.claude/agents/` (handbook Module 8) |

### 6. Agent teams (for the PR review step)

Add to `.claude/settings.json` (or `.claude/settings.local.json`):

```json
{
  "env": {
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1"
  }
}
```

While agent teams are on, subagents that Claude names may launch as teammates. Turn it back to `"0"` when you're done with the review step if that gets in the way.

### 7. Permissions

Jira writes (create, edit, transition, comment) and `git push` / `gh pr merge` should stay **ask**, not allow — they're visible to other people and hard to undo. Approve each one deliberately.

---

## Part A — Bug Lifecycle

```
A1 Find ─► A2 Raise in Jira ─► A3 Branch ─► A4 Assess ─► A5 Fix ─► A6 Test
   ─► A7 Commit ─► A8 PR ─► A9 Team review ─► A10 Address findings
   ─► A11 Merge ─► A12 Close in Jira ─► A13 Standup
```

### A1 — Find the problem

Use the security specialist agent to audit existing code:

```
@agent-security-analyst audit the auth package and list every finding with severity
```

**Why an agent, not `/security-review`:** the built-in `/security-review` only reviews the diff between your branch and origin's default branch. For code that's already on `main`, it has nothing to review.

**Check:** you have a list of findings with severity, file and method. Note **every** finding you intend to track, not just the first.

### A2 — Raise each finding as a Jira Bug

For each finding:

```
Create a Jira Bug in project TASK for the "JwtUtil.isTokenValid always returns true" finding.
Summary: short title. Description sections: Summary, Current Behaviour, Expected Behaviour,
Steps to Reproduce, Files Affected, Acceptance Criteria. Priority: Highest. Label: security.
Show me the draft before creating it.
```

**Check:** open each returned issue link in Jira. Note the keys, e.g. `TASK-12` (JWT validation) and `TASK-13` (task ownership check).

Repeat A3–A12 for **each** bug. The steps below use `TASK-12`.

### A3 — Create a branch and start work in Jira

```bash
git checkout main && git pull
git checkout -b fix/TASK-12-jwt-validation
```

Then in Claude Code:

```
Move Jira issue TASK-12 to In Progress, assign it to me, and add a comment with the branch name fix/TASK-12-jwt-validation.
```

Creating the branch **yourself, with the key in its name**, is what links the work to Jira. `/commit` commits to the current branch when you're not on `main`, so the key carries through.

### A4 — Assess the bug (Spec Kit, read-only)

```
/speckit-bug-assess Jira TASK-12 — JwtUtil.isTokenValid() in the auth package always returns true, so expired or tampered JWTs are accepted by every protected endpoint. Expected: invalid signature or expired token is rejected with 401. Read the full issue from Jira before assessing. slug=task-12-jwt-validation
```

**What happens:** Claude investigates the code, judges whether it's a real bug, locates the code paths, and proposes a remediation. It writes `.specify/bugs/task-12-jwt-validation/assessment.md` and **changes no source code**.

**Check before continuing:**
- The diagnosis is supported by the code it cites.
- The proposed remediation is scoped to the right files.
- It includes a reproduction you can actually run.

Don't proceed on an unsupported diagnosis — re-run assess with more evidence instead.

Optional — record it in Jira:

```
Add a comment to TASK-12 summarising the diagnosis and proposed fix from .specify/bugs/task-12-jwt-validation/assessment.md.
```

### A5 — Fix (Spec Kit)

```
/speckit-bug-fix slug=task-12-jwt-validation
```

**What happens:** Claude applies the assessed remediation — this is the **only** bug step that edits source code — and records the change in `.specify/bugs/task-12-jwt-validation/fix.md`. If it has to go beyond the assessed files, it logs that under **Deviations from Assessment** instead of silently expanding scope.

**Check:** review the diff (`/diff`) and read any deviations.

### A6 — Test (Spec Kit)

```
/speckit-bug-test slug=task-12-jwt-validation
```

**What happens:** Claude re-runs the reproduction and the relevant tests and writes `.specify/bugs/task-12-jwt-validation/test.md` with a verdict. It doesn't edit code.

| Verdict | Meaning | Next |
|---|---|---|
| `verified` | Reproduction and tests exercised successfully | Continue to A7 |
| `partial` | Some verification couldn't be completed | Supply the missing environment or evidence, re-run A6 |
| `failed` | A problem remains | Go back to A4 or A5 with the new evidence, then re-run A6 |

Also run the full suites so nothing else broke:

```
Run mvn -f backend/pom.xml test and npm --prefix frontend test -- --watch=false and report the results.
```

**Do not continue unless the verdict is `verified` and both suites pass.**

### A7 — Commit

```
/commit Fixes Jira TASK-12. Include TASK-12 at the start of the commit summary.
```

**What happens:** `/commit` stages everything (code, tests, and the `.specify/bugs/…` records), writes a Conventional Commits message, commits to `fix/TASK-12-jwt-validation`, and pushes. Approve the push when prompted.

**Check:** `git log -1` shows `TASK-12` in the message.

### A8 — Raise the pull request

```
/raise-pr This fixes Jira TASK-12. Start the PR title with TASK-12, add a "Jira" section linking the issue, and link the Spec Kit records in .specify/bugs/task-12-jwt-validation/.
```

**Check:** open the PR. Note the **PR number** from the output (don't assume it). Confirm the title contains `TASK-12`.

Then:

```
Move TASK-12 to In Review and add a comment with the PR link.
```

### A9 — Review the PR with an agent team

```
Spawn three teammates to review PR #<pr-number>:
- security: using the security-analyst agent type, verify the JWT fix rejects expired and tampered tokens and introduces no new bypass
- performance: using the performance-analyst agent type, check the change adds no per-request overhead in the filter chain
- tests: using the test-coverage-analyst agent type, check the tests cover valid, expired, malformed and wrongly-signed tokens, and compare them with .specify/bugs/task-12-jwt-validation/test.md

Have them review independently, then synthesize one report with findings grouped by severity.
```

If the lead starts reviewing itself: `Wait for your teammates to complete their tasks before proceeding`.

**Keep the report** — post it where the team can see it:

```
Post the synthesized review as a comment on PR #<pr-number> and add a one-paragraph summary as a comment on Jira TASK-12.
```

### A10 — Address review findings

If there are Critical/High findings:

```
Fix the Critical and High findings from the review on this branch. Don't change anything else.
```

Then repeat:

1. `/speckit-bug-test slug=task-12-jwt-validation` — must be `verified`
2. Run both test suites
3. `/commit Addresses review findings for TASK-12.`

Findings you decide not to fix now → raise them as new Jira issues (A2) and note the keys in a PR comment.

### A11 — Merge (human decision)

Merging to `main` is irreversible and visible to everyone — a person makes this call.

**Check first:** CI is green, review findings are resolved or ticketed, at least one human approval.

```bash
gh pr merge <pr-number> --squash --delete-branch
```

(Or merge in the GitHub web UI. Asking Claude to run the command is fine too — approve the prompt.)

### A12 — Close in Jira

```
Move TASK-12 to Done and add a comment with the merged PR link and the verification verdict from .specify/bugs/task-12-jwt-validation/test.md.
```

**Check:** TASK-12 shows Done in Jira, with the PR linked.

### A13 — Standup

```
/standup
```

---

## Part B — Feature Lifecycle with Spec Kit

Use this when the Jira item is a **Story** (new behaviour) rather than a Bug. See `spec-kit-workflow.md` for what each Spec Kit step does.

```
B1 Story in Jira ─► B2 Branch ─► B3 Specify ─► B4 Clarify ─► B5 Plan ─► B6 Tasks
   ─► B7 Sub-tasks in Jira ─► B8 Analyze ─► B9 Implement ─► B10 Converge
   ─► B11 Commit & PR ─► B12 Review & merge ─► B13 Close in Jira
```

### B1 — Start from a Jira Story

Use an existing story, or create one:

```
Create a Jira Story in project TASK: "Task priority with filtering and sorting". Description: users can set Low/Medium/High priority, filter by priority and completion status, and sort by priority; existing tasks default to Medium; existing API responses must not break. Show me the draft first.
```

Note the key, e.g. `TASK-20`.

### B2 — Branch and start work

```bash
git checkout main && git pull
git checkout -b feature/TASK-20-task-priority
```

```
Move TASK-20 to In Progress and assign it to me.
```

### B3 — Specify from the Story

```
/speckit-specify Implement Jira story TASK-20. Read the story and its acceptance criteria from Jira first. Describe what users need and why — no tech stack.
```

**Output:** `specs/<NNN>-<name>/spec.md` and `checklists/requirements.md`.

**Check:** every acceptance criterion in the Jira story appears in `spec.md`. Anything in the spec that's *not* in the story is scope creep — remove it or agree it with the story owner.

### B4 — Clarify (and sync decisions back to Jira)

```
/speckit-clarify
```

Answer the questions. Then keep Jira consistent with the spec:

```
Add a comment to TASK-20 listing the clarification decisions now recorded in spec.md.
```

### B5 — Plan

```
/speckit-plan Use the existing Spring Boot 3 / JPA backend and Angular frontend and follow the existing architecture and test conventions.
```

**Check:** `plan.md`, `data-model.md` and `contracts/` reference real code and respect the constitution.

### B6 — Tasks

```
/speckit-tasks
```

**Output:** `tasks.md` in phases — Setup, Foundational, one per user story, Polish.

### B7 — Mirror the work breakdown in Jira

Create one Jira **sub-task per phase** (not per task — per-task is usually too granular for Jira):

```
For Jira story TASK-20, create one sub-task per phase in specs/<NNN>-<name>/tasks.md (Setup, Foundational, each user story, Polish). Put the phase's task IDs and descriptions in each sub-task description. Show me the list before creating anything.
```

**Check:** sub-tasks appear under TASK-20. Note their keys.

> **Alternative:** Spec Kit's community catalog lists Jira integration extensions that sync specs and tasks into Jira automatically. They're community-maintained and not reviewed by the Spec Kit maintainers — evaluate one before adopting it (`specify extension info <name>`).

### B8 — Analyze

```
/speckit-analyze
```

Fix any findings at the source step (specify / plan / tasks) and re-run until clean.

### B9 — Implement, phase by phase

For each phase:

```
Move the Setup sub-task of TASK-20 to In Progress.
```
```
/speckit-implement Implement only the Setup phase. Run mvn test and npm test after each task and stop if anything fails.
```
```
Move the Setup sub-task of TASK-20 to Done and comment which tasks in tasks.md were completed.
```

Repeat for Foundational, each user story phase, and Polish. Commit after each phase:

```
/commit TASK-20 <phase name> phase.
```

### B10 — Converge

```
/speckit-converge
```

If it appends tasks, implement them (B9), then converge again until it reports **Converged**.

Record it:

```
Add a comment to TASK-20: implementation converged against specs/<NNN>-<name>/spec.md.
```

### B11 — Commit and raise the PR

```
/commit TASK-20 final changes.
```
```
/raise-pr Implements Jira TASK-20. Start the PR title with TASK-20, link the Jira story, and link specs/<NNN>-<name>/spec.md and plan.md.
```
```
Move TASK-20 to In Review and add the PR link as a comment.
```

### B12 — Review and merge

Run the agent-team review as in **A9**, with briefs based on the spec:

```
Spawn three teammates to review PR #<pr-number>:
- security: new query parameters are validated and never return another user's tasks
- performance: filtered/sorted queries use appropriate indexes, no N+1
- tests: every acceptance criterion in specs/<NNN>-<name>/spec.md has a test

Have them review independently, then synthesize one report.
```

Address findings (as **A10**), then a person merges (as **A11**).

### B13 — Close in Jira

```
Move all sub-tasks of TASK-20 and TASK-20 itself to Done, and add a comment with the merged PR link.
```

```
/standup
```

---

## Artifact & Link Map

| What | Where | Linked by |
|---|---|---|
| Bug report | Jira Bug `TASK-12` | — |
| Bug diagnosis / fix record / verification | `.specify/bugs/<slug>/assessment.md`, `fix.md`, `test.md` | Jira comments, PR description |
| Feature request | Jira Story `TASK-20` + phase sub-tasks | — |
| Spec / plan / tasks | `specs/<NNN>-<name>/` | Jira comments, PR description |
| Code changes | Branch `fix/TASK-12-…` / `feature/TASK-20-…` | Jira key in branch name |
| Commits | Commit messages starting with the key | Jira key in message |
| Review | PR comment (agent-team report) + Jira summary comment | PR number, Jira key in PR title |

## Quick Reference

| Step | Bug | Feature |
|---|---|---|
| Find / define | `@agent-security-analyst audit …` → Jira Bug | Jira Story |
| Branch | `fix/<KEY>-<slug>` | `feature/<KEY>-<slug>` |
| Understand | `/speckit-bug-assess … slug=<slug>` | `/speckit-specify` → `/speckit-clarify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-analyze` |
| Build | `/speckit-bug-fix slug=<slug>` | `/speckit-implement` per phase |
| Verify | `/speckit-bug-test slug=<slug>` (must be `verified`) + test suites | `/speckit-converge` (must be Converged) + test suites |
| Ship | `/commit <KEY>` → `/raise-pr <KEY>` → team review → human merge | same |
| Track | Jira: In Progress → In Review → Done, with comments | same, plus sub-tasks per phase |

## Common Mistakes

| Mistake | Instead |
|---|---|
| Hard-coding issue or PR numbers | Use the key/number each step returns |
| Letting `/commit` pick a branch name | Create `fix/<KEY>-…` or `feature/<KEY>-…` yourself before starting |
| Tracking only the first audit finding | Raise every finding you intend to fix; ticket the rest |
| Skipping the assessment review | Don't run `/speckit-bug-fix` on a diagnosis you haven't checked |
| Merging on a `partial` bug verdict | Get to `verified` first |
| Losing the agent-team report when the session ends | Post it on the PR and summarise it in Jira |
| Letting Claude merge without a human decision | Keep merge and Jira transitions as ask-permission actions |
| Creating a Jira sub-task for every line of `tasks.md` | One sub-task per phase |
| Using `/security-review` to audit code already on `main` | Use the security-analyst agent; `/security-review` reviews branch diffs |
| Project key that's a JQL reserved word (e.g. `FOR`) | Pick a key like `TASK`, or change it in Space settings → Details |
| Setting Priority on a work type that doesn't have the field | Add Priority to Bug, Story and Subtask first (Prerequisites 2.4) |
| Installing GitHub for Jira but not connecting an org | Connect the GitHub organization in the app (Prerequisites 2.6) |

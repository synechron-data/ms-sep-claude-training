# Claude Code — Built-in Tools Reference (Updated)

> **Last updated:** September 2026
> **Change summary:** Several tools described in the original PDF don't exist in the current build (`LSP`, `TaskCreate`/`TaskGet`/`TaskList`/`TaskUpdate`, `TeamCreate`/`TeamDelete`, `TodoWrite`); several real tools were missing entirely; and a few gating/behavior descriptions were stale. See "What Changed" at the bottom.

Claude Code ships with a set of built-in tools that it uses autonomously to understand and modify codebases. Tool names listed here are the exact strings used in permission rules, subagent tool lists, and hook matchers. Some tools are **deferred** — their full schema loads on demand via `ToolSearch` rather than being defined upfront.

## Permission Key

| Symbol | Meaning |
|---|---|
| ✅ | No explicit permission required |
| 🔐 | Requires user permission / approval |

## Tools Table

| Tool | Permission | What It Does |
|---|---|---|
| `Agent` | ✅ | Spawns a subagent — in-process, forked with full context, or a fresh general-purpose/specialized agent — to handle a complex or isolated task autonomously |
| `AskUserQuestion` | ✅ | Presents multiple-choice questions to the user to gather requirements or resolve ambiguity before proceeding |
| `Artifact` | ✅ | Publishes, reads, updates, and manages Artifacts — hosted HTML/Markdown pages shown to the user, including runtime capabilities, shared database, assets, comments, and watches |
| `Bash` | 🔐 | Executes shell commands in a persistent bash session; used for running tests, git commands, package managers, build tools, and general terminal operations |
| `PowerShell` | 🔐 | Executes PowerShell commands; on Windows this is a **standard, always-available** tool (alongside Bash), not an opt-in experimental feature |
| `CronCreate` | ✅ (deferred) | Creates a scheduled cloud agent (routine) that fires on a cron schedule — **persists beyond the current session**, not cleared on exit |
| `CronDelete` | ✅ (deferred) | Deletes a scheduled cron agent |
| `CronList` | ✅ (deferred) | Lists scheduled cron agents |
| `Edit` | 🔐 | Makes targeted, surgical edits to a specific file using exact string replacement — preferred over full rewrites for small changes |
| `EnterPlanMode` | ✅ (deferred) | Switches Claude into plan mode so it designs an approach and gets approval before writing any code |
| `ExitPlanMode` | 🔐 (deferred) | Presents the plan to the user for approval and exits plan mode to begin execution |
| `EnterWorktree` | ✅ (deferred) | Creates an isolated git worktree and switches into it — useful for parallel Claude Code sessions on separate branches |
| `ExitWorktree` | ✅ (deferred) | Exits the current worktree session and returns Claude to the original working directory |
| `Glob` | ✅ | Finds files by name/path pattern matching (e.g., `**/*.ts`); results sorted by modification time; does **not** respect `.gitignore` by default |
| `Grep` | ✅ | Searches file *contents* for text patterns using ripgrep syntax; respects `.gitignore` by default; can return file paths only, matching lines, or context around matches |
| `ListMcpResourcesTool` | ✅ (deferred) | Lists resources (prompts, data, schemas) exposed by connected MCP servers |
| `ReadMcpResourceTool` | ✅ (deferred) | Reads a specific MCP resource by its URI |
| `ReadMcpResourceDirTool` | ✅ (deferred) | Reads an MCP resource directory listing |
| `NotebookEdit` | 🔐 (deferred) | Modifies cells in Jupyter (`.ipynb`) notebooks — add, edit, delete, or reorder cells |
| `Read` | ✅ | Reads the contents of one or more files (including images and PDFs); core tool for understanding code before making changes |
| `Write` | 🔐 | Creates a new file or completely overwrites an existing one — use `Edit` for targeted changes |
| `ListAgents` | ✅ (deferred) | Lists agents you can message — subagents you spawned, team teammates, other local sessions, and cloud sessions — as addresses for `SendMessage` |
| `SendMessage` | ✅ (deferred) | Sends a message to a listed agent (subagent, teammate, or other session), or resumes a spawned agent by name/ID — standard tool, **no experimental flag required** |
| `Skill` | varies | Invokes a packaged skill — reusable instructions for a specific workflow; some skills run inline, others hand off to a subagent |
| `TaskOutput` | ✅ (deferred) | Retrieves output/status from a background task started with `run_in_background` — **still active, not deprecated** |
| `TaskStop` | ✅ (deferred) | Stops a running background task |
| `Monitor` | ✅ (deferred) | Streams live events/notifications from a background process |
| `ToolSearch` | ✅ | Fetches full schemas for deferred tools on demand — avoids stuffing every tool definition into context upfront |
| `ReportFindings` | ✅ | Reports structured, typed code-review findings for host-UI rendering |
| `ScheduleWakeup` | ✅ | Schedules the next self-paced resumption of a running `/loop` |
| `SendFeedback` | ✅ | Drafts (locally queued, not auto-sent) feedback about Claude Code product or model behavior |
| `DesignSync` | ✅ (deferred) | Syncs design-related state (e.g., Claude Design canvas) |
| `PushNotification` | ✅ (deferred) | Sends a push notification |
| `RemoteTrigger` | ✅ (deferred) | Triggers a remote/cloud action |
| `EndConversation` | ✅ (deferred) | Ends the current conversation session — reserved for sustained abuse or explicit user request |
| `WebFetch` | 🔐 (deferred) | Fetches content from a specified URL; converts to markdown; does **not** auto-follow cross-host redirects (fetches the redirect target in a second call) |
| `WebSearch` | 🔐 (deferred) | Runs a web search via Anthropic's search backend and returns result titles/URLs |

**Not real tools** (described in the original PDF but absent from the current tool surface — see "What Changed"): `LSP`, `TaskCreate`, `TaskGet`, `TaskList`, `TaskUpdate`, `TeamCreate`, `TeamDelete`, `TodoWrite`.

## Noteworthy Behaviors

### Bash
- Working directory **persists** across commands within a session
- Environment variables do **not** persist (re-export each command as needed)
- Default timeout: **2 minutes** per command (Claude can request up to **10 min**)
- Long-running processes (dev servers, watchers) can be run in the background with `run_in_background: true`, then tracked with `TaskOutput`/`TaskStop`/`Monitor`

### Edit vs Write
- Use `Edit` for targeted changes to existing files (exact string replacement)
- Use `Write` when creating new files or when a full rewrite is appropriate

### Glob vs Grep
- `Glob` → finds files by *name/path pattern* (does not respect `.gitignore` by default)
- `Grep` → finds files by *content pattern* (respects `.gitignore` by default)

### WebFetch
- Redirects to a different host are **not** followed automatically — Claude fetches the redirect target in a second call

### Deferred tools
- A growing share of tools (MCP resource tools, Cron*, plan/worktree mode, Web*, Task*, agent-messaging tools, and more) are **deferred**: only their name is known until `ToolSearch` loads the full schema. This keeps context usage down when many tools are configured, rather than stuffing every schema in upfront.

## Extending Claude Code Tools

| Method | What It Does |
|---|---|
| **MCP Server** | Connect an external server to add fully custom tools |
| **Skill** | Write a reusable prompt-based workflow; invoked via the `Skill` tool or `/<skill-name>` |
| **Permissions** | Use `/permissions` or `settings.json` to allow/deny specific tools |

## Check Available Tools in a Session

```
What tools do you have access to?
```

Ask Claude directly for a conversational summary. For exact MCP tool names, run `/mcp`.

---

## What Changed From the Original PDF

The source PDF (`claude-code-tools-reference.pdf`, last verified May 2026) had drifted from the real tool surface by September 2026:

1. **`LSP` tool removed/never real** — not present anywhere in the current tool set (main or deferred). No built-in code-intelligence tool by this name exists.
2. **Task/Team CRUD tools don't match reality** — `TaskCreate`, `TaskGet`, `TaskList`, `TaskUpdate`, `TeamCreate`, `TeamDelete`, and `TodoWrite` are all absent from the current tool set. Only `TaskOutput` and `TaskStop` are real, and `TaskOutput` is **not** deprecated as the PDF claimed — it's an active deferred tool, paired with `Monitor` for streaming background-task events.
3. **Agent teams / `SendMessage` no longer experimental** — the PDF said this required `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1` or `--agent-teams`. In the current build, `ListAgents` and `SendMessage` are standard deferred tools with no experimental-flag gating mentioned.
4. **`PowerShell` is not an opt-in preview** — the PDF said it required `CLAUDE_CODE_USE_POWERSHELL_TOOL=1`. On Windows sessions it now ships as a standard top-level tool alongside `Bash` (in fact the primary shell tool on Windows).
5. **`CronCreate`/`CronDelete`/`CronList` description was wrong** — the PDF described session-scoped, exit-cleared tasks. These actually create/manage **persistent cloud-scheduled agent routines** that survive well beyond the current session.
6. **Missing tools entirely** — `Artifact`, `ListAgents`, `ReportFindings`, `ScheduleWakeup`, `SendFeedback`, `DesignSync`, `Monitor`, `PushNotification`, `RemoteTrigger`, `EndConversation`, and `ReadMcpResourceDirTool` are all real, currently-available tools that the original reference never mentioned.
7. **Confirmed accurate and unchanged**: `Agent`, `AskUserQuestion`, `Bash` (timeout/backgrounding behavior matches exactly), `Edit`, `EnterPlanMode`/`ExitPlanMode`, `EnterWorktree`/`ExitWorktree`, `Glob`/`Grep` (including the `.gitignore` split), `ListMcpResourcesTool`/`ReadMcpResourceTool`, `NotebookEdit`, `Read`, `Skill`, `ToolSearch`, `Write`, and the `WebFetch` cross-host redirect behavior.

**Caveat:** the exact deferred-tool roster can vary by session, plan, and which MCP servers/integrations are connected — treat the "deferred" tags above as representative of a typical session, and confirm your own session's live list with `/mcp` or by asking Claude directly.

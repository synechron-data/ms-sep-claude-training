# Claude Code — Built-in Tools Reference

> **Source:** Rebuilt from the official tools reference ([code.claude.com/docs/en/tools-reference](https://code.claude.com/docs/en/tools-reference)).
> **Last verified:** September 2026

Claude Code ships with built-in tools it uses to understand and modify your codebase. The tool names below are the exact strings used in **permission rules**, **subagent `tools` lists**, and **hook matchers**. The tools present in any one session vary by platform, model, plan, and configuration — a tool missing from your session is not proof it doesn't exist.

## Permission Key

"Permission required" means the tool prompts in **Manual** (`default`) mode. On Pro, Max, and Team plans, sessions start in **auto mode**, where a classifier decides most of these prompts instead of you.

| Symbol | Meaning |
|---|---|
| ✅ | No permission prompt |
| 🔐 | Prompts for permission in Manual mode |

## Tools Table

| Tool | Permission | What It Does |
|---|---|---|
| `Agent` | ✅ | Spawns a subagent with its own context window. With agent teams enabled, a call that carries a `name` can launch a teammate |
| `Artifact` | 🔐 | Publishes an HTML or Markdown file as a private, interactive page on claude.ai |
| `AskUserQuestion` | ✅ | Asks multiple-choice questions to gather requirements or resolve ambiguity |
| `Bash` | 🔐 | Executes shell commands (see behaviour below) |
| `CronCreate` | ✅ | Schedules a recurring or one-shot prompt **within the current session**. Session-scoped; restored on `--resume`/`--continue` if unexpired |
| `CronDelete` | ✅ | Cancels a session scheduled task by ID |
| `CronList` | ✅ | Lists the session's scheduled tasks |
| `Edit` | 🔐 | Makes targeted edits to a file (exact string replacement) |
| `EndConversation` | ✅ | Ends the session — only for sustained abusive input or when you ask to see it demonstrated |
| `EnterPlanMode` | ✅ | Switches to plan mode to design an approach before coding |
| `EnterWorktree` | 🔐 | Creates an isolated git worktree (or enters an existing one) and switches into it |
| `ExitPlanMode` | 🔐 | Presents the plan for approval and exits plan mode |
| `ExitWorktree` | ✅ | Leaves the worktree and returns to the original directory |
| `Glob` | ✅ | Finds files by name pattern. **In the default tool set on Windows only** (see below) |
| `Grep` | ✅ | Searches file contents (ripgrep). **In the default tool set on Windows only** (see below) |
| `ListAgents` | ✅ | Lists agents Claude can message with `SendMessage`: subagents, teammates, and your other sessions |
| `ListMcpResourcesTool` | ✅ | Lists resources exposed by connected MCP servers |
| `LSP` | ✅ | Code intelligence from a language server: definitions, references, type info, symbols; reports type errors after edits. Needs a code-intelligence plugin for the language |
| `Monitor` | 🔐 | Runs a command in the background and feeds each output line back to Claude (or listens on a WebSocket) |
| `NotebookEdit` | 🔐 | Modifies Jupyter notebook cells |
| `PowerShell` | 🔐 | Executes PowerShell commands natively (availability below) |
| `PushNotification` | ✅ | Sends a desktop notification (and a phone push when Remote Control is connected) |
| `Read` | ✅ | Reads files, including images and PDFs |
| `ReadMcpResourceTool` | ✅ | Reads a specific MCP resource by URI |
| `RemoteTrigger` | ✅ | Creates, updates, runs, and lists cloud **Routines** — the **persistent** scheduled agents behind `/schedule` |
| `ReportFindings` | ✅ | Reports code-review findings as a structured list for the UI |
| `ScheduleWakeup` | ✅ | Picks when the next iteration of a self-paced `/loop` runs |
| `SendFeedback` | ✅ | Drafts a feedback report and queues it locally; nothing is sent until you choose to |
| `SendMessage` | ✅ | Messages a teammate, resumes a subagent by ID or name, or messages another of your sessions |
| `SendUserFile` | ✅ | Sends a generated file (report, screenshot, build output) to you |
| `ShareOnboardingGuide` | 🔐 | Uploads `ONBOARDING.md` from `/team-onboarding` and returns a share link |
| `Skill` | 🔐 | Runs a skill within the main conversation |
| `SubagentHandback` | ✅ | Delivers a subagent's final report (auto mode only) |
| `TaskCreate` | ✅ | Creates a task in the task checklist ¹ |
| `TaskGet` | ✅ | Gets full details of a task ¹ |
| `TaskList` | ✅ | Lists tasks and their status ¹ |
| `TaskUpdate` | ✅ | Updates task status, dependencies, or details; can delete tasks ¹ |
| `TaskOutput` | ✅ | Retrieves output from a background task. **Deprecated** — prefer `Read` on the task's output file |
| `TaskStop` | ✅ | Stops a background task, teammate, or named background agent |
| `TodoWrite` | ✅ | Session checklist. **Disabled by default** in favour of the Task tools; re-enable with `CLAUDE_CODE_ENABLE_TASKS=0` where task tools are available |
| `ToolSearch` | ✅ | Loads deferred tools on demand when tool search is enabled |
| `WaitForMcpServers` | ✅ | Waits for MCP servers still connecting in the background (tool-search sessions) |
| `WebFetch` | 🔐 | Fetches a URL and extracts content with a small model |
| `WebSearch` | 🔐 | Runs a web search |
| `Workflow` | 🔐 | Runs a dynamic workflow script that orchestrates many subagents |
| `Write` | 🔐 | Creates or overwrites a file |

¹ **Task tool availability:** `TaskCreate`, `TaskGet`, `TaskList`, `TaskUpdate` (and `TodoWrite`) are provided by default **only** on Claude 3.x models, Opus 4–4.7, Sonnet 4–4.6, and Haiku 4.5. On newer models (Opus 5, Sonnet 5, Fable) Claude tracks multi-step work without them unless you opt in with `CLAUDE_CODE_ENABLE_TODO_TOOLS=1`, `--allowedTools TaskCreate`, or `--tools`. Agent-team task lists depend on these tools being present.

## Noteworthy Behaviors

### Bash
- Each command runs in a **separate process**.
- A `cd` in the main session carries over to later commands **as long as it stays inside the project** (or an added directory); outside it, the directory resets and Claude sees `Shell cwd was reset to <dir>`. Subagents never carry over `cd`.
- Environment variables **don't** persist between commands.
- Default timeout **2 minutes** (`BASH_DEFAULT_TIMEOUT_MS`); Claude can request up to **10 minutes** (`BASH_MAX_TIMEOUT_MS`).
- Output reaches Claude inline up to roughly **30,000 characters** (`BASH_MAX_OUTPUT_LENGTH`, max 150,000); longer output is saved to a file Claude can read.
- Long-running processes can run in the background and be watched with `Monitor` or stopped with `TaskStop`.

### PowerShell
- **Windows without Git Bash:** enabled automatically (and there is no Bash tool).
- **Windows with Git Bash:** on by default for claude.ai and Console accounts; set `CLAUDE_CODE_USE_POWERSHELL_TOOL=1` to enable on Bedrock/Vertex/Foundry, `0` to turn off.
- **macOS, Linux, WSL:** opt-in.
- Hooks that inspect shell commands should match `Bash|PowerShell`.

### Edit vs Write
- `Edit` for targeted changes to existing files; `Write` for new files or full rewrites.

### Glob vs Grep
- **Windows:** both are in the default tool set.
- **macOS, Linux, WSL:** both are left out by default and Claude uses `find`/`grep` through Bash. They come back if you name them in `--tools`/`--allowedTools`, if Bash is removed from the session, or in a subagent that lists them and omits Bash.
- `Glob` results are sorted by modification time and **capped at 100 files**; it does **not** respect `.gitignore` by default.
- `Grep` **respects** `.gitignore`.

### WebFetch
- HTTP is upgraded to HTTPS; responses are cached for 15 minutes.
- Output is **lossy by design** — a small model extracts what the prompt asks for.
- Redirects to a **different host** aren't followed; Claude fetches the new URL in a second call.

### WebSearch
- At most **200 searches per session**, shared across the main conversation and all subagents.

### Deferred tools / tool search
- Tool search is on by default: **MCP tools are deferred** and loaded on demand through `ToolSearch`, keeping their schemas out of context until needed. It is turned off when `ANTHROPIC_BASE_URL` points to a non-first-party host.

## Extending Claude Code Tools

| Method | What It Does |
|---|---|
| **MCP server** | Connect an external server to add custom tools |
| **Skill** | Reusable prompt-based workflow, run through the existing `Skill` tool or `/<skill-name>` |
| **Permissions** | Allow/ask/deny tools with `/permissions` or `settings.json` |
| **Hooks** | Run your own checks before/after tool calls (`PreToolUse`, `PostToolUse`, …) |

## Check Available Tools in a Session

Ask Claude "What tools do you have access to?" for a summary. Run `/mcp` for exact MCP tool names.

# Claude Code — Hooks Lifecycle

> **Source:** [Hooks reference](https://code.claude.com/docs/en/hooks)
> **Last verified:** September 2026

There are **33 hook events**. The diagram shows where each one fires; the tables show what each matcher filters on and what the hook can do.

## Lifecycle at a Glance

```
SESSION START
  Setup ............................ only with --init-only, or --init / --maintenance in -p mode
  SessionStart ..................... startup | resume | clear | compact | fork
  InstructionsLoaded ............... each CLAUDE.md / .claude/rules/*.md loaded (also lazily later)

EACH TURN
  UserPromptExpansion .............. a typed /command expands into a prompt (can block)
  UserPromptSubmit ................. before Claude sees your prompt (can block)
  │
  ├─ AGENTIC LOOP (repeats per tool call)
  │    PreToolUse .................. before the tool runs (can block / rewrite input / ask)
  │    PermissionRequest ........... a permission decision is needed
  │    PermissionDenied ............ auto mode denied the call
  │    [tool executes]
  │    PostToolUse / PostToolUseFailure
  │    PostToolBatch ............... a batch of parallel tool calls resolved
  │    Elicitation / ElicitationResult ... an MCP server asks you for input
  │    SubagentStart / SubagentStop
  │    TaskCreated / TaskCompleted
  │    TeammateIdle ................ an agent-team teammate is about to go idle
  │
  MessageDisplay ................... assistant text is being displayed (display-only rewrite)
  Stop ............................. Claude finished responding (not on user interrupt)
  StopFailure ...................... the turn ended on an API error

ANY TIME DURING THE SESSION
  Notification ..................... permission prompt, idle, auth, elicitation, agent events …
  PreModelSwitch / PostModelSwitch . model is about to change / has changed
  PreCompact / PostCompact ......... around context compaction
  ConfigChange ..................... a settings/config file changed
  CwdChanged ....................... working directory changed (e.g. Claude ran cd)
  DirectoryAdded ................... /add-dir added a working directory
  FileChanged ...................... a watched file changed on disk
  WorktreeCreate / WorktreeRemove .. worktree created / removed

SESSION END
  SessionEnd ....................... clear | resume | logout | prompt_input_exit | other
```

## All Events

| Category | Event | Fires when |
|---|---|---|
| Session | `SessionStart` | A session begins or resumes |
| Session | `Setup` | `--init-only`, or `--init` / `--maintenance` in `-p` mode |
| Session | `SessionEnd` | A session terminates |
| Turn | `UserPromptSubmit` | You submit a prompt, before Claude processes it |
| Turn | `UserPromptExpansion` | A typed command expands into a prompt |
| Turn | `Stop` | Claude finishes responding |
| Turn | `StopFailure` | The turn ends due to an API error |
| Tools | `PreToolUse` | Before a tool call executes |
| Tools | `PermissionRequest` | A tool call needs a permission decision |
| Tools | `PermissionDenied` | Auto mode denies a tool call |
| Tools | `PostToolUse` | After a tool call succeeds |
| Tools | `PostToolUseFailure` | After a tool call fails |
| Tools | `PostToolBatch` | After a batch of parallel tool calls resolves |
| Agents / tasks | `SubagentStart` / `SubagentStop` | A subagent is spawned / finishes |
| Agents / tasks | `TaskCreated` / `TaskCompleted` | A task is created / marked complete |
| Agents / tasks | `TeammateIdle` | An agent-team teammate is about to go idle |
| Context / config | `InstructionsLoaded` | A CLAUDE.md or rules file is loaded |
| Context / config | `ConfigChange` | A configuration file changes |
| Context / config | `CwdChanged` | The working directory changes |
| Context / config | `DirectoryAdded` | A working directory is added mid-session |
| Context / config | `FileChanged` | A watched file changes on disk |
| Model | `PreModelSwitch` | Before a requested model switch applies (can block) |
| Model | `PostModelSwitch` | After the session's model changes |
| Compaction | `PreCompact` / `PostCompact` | Before / after context compaction |
| Worktree | `WorktreeCreate` / `WorktreeRemove` | A worktree is created / removed |
| MCP | `Elicitation` / `ElicitationResult` | An MCP server requests input / you respond |
| Display | `MessageDisplay` | Assistant text is displayed |
| Display | `Notification` | Claude Code sends a notification |

## What the Matcher Filters

| Event(s) | Matcher filters by | Example values |
|---|---|---|
| `PreToolUse`, `PostToolUse`, `PostToolUseFailure`, `PermissionRequest`, `PermissionDenied` | Tool name | `Bash`, `Edit\|Write`, `Bash\|PowerShell`, `mcp__.*` |
| `SessionStart` | How the session started | `startup`, `resume`, `clear`, `compact`, `fork` |
| `Setup` | Which flag triggered it | `init`, `maintenance` |
| `SessionEnd` | Why it ended | `clear`, `resume`, `logout`, `prompt_input_exit`, `other` |
| `Notification` | Notification type | `permission_prompt`, `idle_prompt`, `auth_success`, `elicitation_dialog`, `elicitation_url_dialog`, `elicitation_complete`, `elicitation_response`, `agent_needs_input`, `agent_completed`, `quota_auto_resume_*` |
| `SubagentStart`, `SubagentStop` | Agent type | `general-purpose`, `Explore`, `Plan`, custom agent names |
| `PreCompact`, `PostCompact` | Trigger | `manual`, `auto` |
| `PreModelSwitch`, `PostModelSwitch` | Target model name | `claude-opus-5`, `.*opus.*` |
| `ConfigChange` | Configuration source | `user_settings`, `project_settings`, `local_settings`, `policy_settings`, … |
| `DirectoryAdded` | How it was added | `slash_command`, `register_repo_root` |
| `FileChanged` | Literal filenames to watch | `.envrc\|.env` |
| `StopFailure` | Error type | `rate_limit`, `overloaded`, `authentication_failed`, … |
| `InstructionsLoaded` | Load reason | `session_start`, `nested_traversal`, `path_glob_match`, `include`, … |
| `UserPromptExpansion` | Command name | your skill or command names |
| `Elicitation`, `ElicitationResult` | MCP server name | your MCP server names |
| `UserPromptSubmit`, `PostToolBatch`, `Stop`, `TeammateIdle`, `TaskCreated`, `TaskCompleted`, `WorktreeCreate`, `WorktreeRemove`, `MessageDisplay`, `CwdChanged` | **No matcher** — always fire | — |

## What a Hook Can Do (Decision Control)

| Event(s) | How it decides | Key fields |
|---|---|---|
| `PreToolUse` | `hookSpecificOutput` (or exit 2 to block) | `permissionDecision`: `allow` / `deny` / `ask` / `defer`; `permissionDecisionReason`; `updatedInput` |
| `PreModelSwitch` | `hookSpecificOutput` or top-level `decision` | `permissionDecision` (allow/deny/ask); `decision: "block"` cancels the switch |
| `PermissionRequest` | `hookSpecificOutput` | `decision.behavior`: allow / deny |
| `PermissionDenied` | `hookSpecificOutput` | `retry: true` lets the model retry |
| `UserPromptSubmit`, `UserPromptExpansion`, `PostToolUse`, `PostToolUseFailure`, `PostToolBatch`, `Stop`, `SubagentStop`, `ConfigChange`, `PreCompact` | Top-level `decision` | `decision: "block"` + `reason` |
| `TeammateIdle`, `TaskCompleted` | Exit code or `continue: false` | Exit 2 blocks with stderr feedback |
| `TaskCreated` | Exit code or top-level `decision` | Exit 2 or `decision: "block"` cancels the task |
| `SessionStart`, `SubagentStart`, `PostModelSwitch` | Context only | `hookSpecificOutput.additionalContext` |
| `MessageDisplay` | `hookSpecificOutput` | `displayContent` (display only — the transcript keeps the original) |
| `WorktreeCreate` / `WorktreeRemove` | Path return / exit code | Print the worktree path / non-zero exit fails removal |
| `Elicitation`, `ElicitationResult` | `hookSpecificOutput` | `action` (accept/decline/cancel), `content` |
| `Setup`, `Notification`, `SessionEnd`, `PostCompact`, `InstructionsLoaded`, `StopFailure`, `CwdChanged`, `DirectoryAdded`, `FileChanged` | None | Side effects only (logging, alerts, cleanup) |

**Exit codes:** `0` = success (stdout parsed as JSON if it starts with `{`); `2` = block, on events that can block, and always wins over JSON `allow`; anything else = non-blocking error.

**Stdout visibility:** for most events plain stdout only goes to the debug log. It becomes context Claude sees only for `UserPromptSubmit`, `UserPromptExpansion`, `SessionStart`, and `PostModelSwitch`. To show a message to the user, return JSON `systemMessage`.

## Hook Types

| Type | What it does |
|---|---|
| `command` | Runs a shell command or executable; reads event JSON on stdin |
| `http` | POSTs the event JSON to a URL |
| `mcp_tool` | Calls a tool on a connected MCP server |
| `prompt` | Asks Claude for a single yes/no judgment |
| `agent` | *(experimental)* Spawns a verifier subagent with Read/Grep/Glob that returns `{"ok": true/false}` |

# Claude Code — Built-in Slash Commands Reference

> **Source:** Official commands reference ([code.claude.com/docs/en/commands](https://code.claude.com/docs/en/commands)), model configuration, interactive-mode, sessions, and CLI reference pages.
> **Last verified:** September 2026 (Claude Code v2.1.2xx)
> **Note:** Commands marked **[Skill]** are bundled skills (prompt-based). Not every command appears for every user — availability depends on platform, plan, and provider. Run `/help` in a session to see your own list.

## Session & Context Management

| Command | Purpose |
|---|---|
| `/clear [name]` | Start a new conversation with empty context. Pass a name to label the conversation you're leaving in the `/resume` picker. Aliases: `/reset`, `/new` |
| `/compact [instructions]` | Free up context by summarizing the conversation so far, optionally with focus instructions (e.g. `/compact keep architecture decisions`) |
| `/context [all]` | Visualize context usage as a colored grid, with optimization suggestions and capacity warnings |
| `/btw [question]` | Ask a side question about the session without adding it to the conversation |
| `/rewind` | Rewind the conversation and/or code to a previous point, or summarize from a selected message. Aliases: `/checkpoint`, `/undo` |
| `/resume [session]` | Resume a conversation by ID or name, or open the session picker. Alias: `/continue` |
| `/rename [name]` | Rename the current session (shown on the prompt bar). Without a name, auto-generates one |
| `/branch [name]` | Branch the conversation at this point and **switch into the branch**; the original is preserved and reachable with `/resume` |
| `/fork [prompt]` | Copy the conversation into a **new background session** and keep working here. With a prompt, the copy starts on it immediately |
| `/export [filename]` | Export the conversation as plain text. Without a filename, opens a dialog to copy or save |
| `/copy [N]` | Copy the last (or Nth-latest) assistant response. With code blocks, opens a picker; press `w` to write to a file |
| `/recap` | Generate a one-line summary of the current session |

## Model & Configuration

| Command | Purpose |
|---|---|
| `/model [model]` | Switch model and save it as your default. With no argument, opens a picker — press `s` on a row to switch for this session only; left/right arrows adjust effort |
| `/effort [level\|auto\|status]` | Set effort: `low`, `medium`, `high`, `xhigh`, `max`, `ultracode`, or `auto`. `max` and `ultracode` are session-only; the other levels are saved when confirmed |
| `/fast [on\|off]` | Toggle fast mode |
| `/config [key=value ...]` | Open Settings (theme, model, **output style**, editor mode, …) or set a key directly, e.g. `/config theme=dark`. Alias: `/settings` |
| `/theme` | Change the color theme (auto, light/dark, daltonized, ANSI, custom) |
| `/color [color\|default]` | Set the prompt bar color: red, blue, green, yellow, purple, orange, pink, cyan |
| `/statusline` | Configure the status line |
| `/keybindings` | Open your keyboard shortcuts file |

> Output style and Vim editing mode are set from `/config` (or the `outputStyle` setting).

## Agentic Workflows (Bundled Skills)

| Command | Purpose |
|---|---|
| `/batch <instruction>` | **[Skill]** Research the codebase, split the change into 5–30 independent units, present a plan, then run one background subagent per unit in its own git worktree; each opens a PR. Requires a git repo |
| `/simplify [target]` | **[Skill]** Review changed code for cleanup and apply fixes. **Four** review agents run in parallel (reuse, simplification, efficiency, abstraction level). Doesn't look for bugs — use `/code-review` for that |
| `/debug [description]` | **[Skill]** Turn on debug logging for the session and troubleshoot from the debug log |
| `/loop [interval] [prompt]` | **[Skill]** Run a prompt repeatedly while the session is open; Claude self-paces without an interval. Alias: `/proactive` |
| `/goal [condition\|clear]` | Keep Claude working across turns until a condition is met |
| `/subtask <task>` | Spawn a forked background subagent that inherits the conversation and reports back here |
| `/background [prompt]` | Detach the current session to run as a background agent. Alias: `/bg` |
| `/claude-api [subcommand]` | **[Skill]** Load Claude API / Managed Agents reference for your project's language; auto-activates when code imports the Anthropic SDK |

## Permissions & Tools

| Command | Purpose |
|---|---|
| `/permissions` | Manage allow/ask/deny rules and working directories. Alias: `/allowed-tools` |
| `/fewer-permission-prompts` | **[Skill]** Scan transcripts for common read-only calls and add an allowlist to project settings |
| `/hooks` | **View** hook configurations (read-only — edit settings files to change hooks) |
| `/mcp [reconnect <server>\|enable\|disable ...]` | Manage MCP server connections and OAuth |
| `/add-dir <path>` | Add a working directory for the session |
| `/cd <path>` | Move the session to a new working directory, keeping the conversation |
| `/sandbox` | Toggle sandbox mode (supported platforms only) |

## Project Initialization & Memory

| Command | Purpose |
|---|---|
| `/init` | Create a starter `CLAUDE.md`. Set `CLAUDE_CODE_NEW_INIT=1` for an interactive flow that also covers skills, hooks, and memory files |
| `/memory` | Edit `CLAUDE.md` files, toggle auto memory, view auto-memory entries |
| `/skills` | List available skills; filter, sort by token cost, toggle visibility |
| `/reload-skills` | Re-scan skill and command directories without restarting |
| `/agents` | Prints a reminder to ask Claude to create or manage subagents, or to edit `.claude/agents/` / `~/.claude/agents/` directly |

## Git, Code Review & CI

| Command | Purpose |
|---|---|
| `/diff` | Review working-tree changes, including Claude's edits |
| `/code-review [low…max\|ultra] [--fix] [--comment] [target]` | **[Skill]** Review the current diff, a PR, branch, or path for correctness bugs and cleanups. `--fix` applies findings, `--comment` posts to the PR, `ultra` runs a deep multi-agent cloud review. Alias: `/review` |
| `/ultrareview [PR or branch]` | Alias for `/code-review ultra` (cloud multi-agent review). Also available from the shell as `claude ultrareview [target]` |
| `/security-review` | Review **the diff between your current branch and origin's default branch** for vulnerabilities. Needs an `origin` remote; takes no file/folder argument |
| `/autofix-pr [prompt]` | Spawn a cloud session that watches the current branch's PR and pushes fixes for CI failures and review comments. Uses `gh` |
| `/install-github-app` | Install the Claude GitHub App for a github.com repo, optionally with Actions workflows |

## IDE & Interface

| Command | Purpose |
|---|---|
| `/ide` | Manage IDE integrations |
| `/desktop` | Continue the session in the Claude Code Desktop app (macOS or x64 Windows). Alias: `/app` |
| `/terminal-setup` | Install a Shift+Enter newline keybinding (VS Code, Cursor, Alacritty, Zed; Apple Terminal/iTerm2 variants) |
| `/focus` | Toggle the focus view |
| `/chrome` | Configure Claude in Chrome |

## Account & Usage

| Command | Purpose |
|---|---|
| `/login` / `/logout` | Sign in / out of your Anthropic account |
| `/usage` | Session cost, plan usage limits, and activity stats. Aliases: `/cost`, `/stats` (opens on the Stats tab) |
| `/usage-credits` | Configure usage credits (or request them from your admin) |
| `/rate-limit-options` | Options for continuing when a usage limit blocks a request |
| `/upgrade` | Open the plan upgrade page |
| `/privacy-settings` | View/update privacy settings (Pro and Max) |
| `/passes` | Share a free week of Claude Code (only if eligible) |
| `/insights` | Generate an HTML report analyzing your recent sessions |
| `/team-onboarding` | Generate a team onboarding guide from your last 30 days of usage |

## Cloud, Remote & Teleport

| Command | Purpose |
|---|---|
| `/schedule [description]` | Create, update, list, or run cloud **routines**. Alias: `/routines` |
| `/remote-control` | Make this session controllable from claude.ai. Alias: `/rc` |
| `/teleport` | Pull a cloud session into this terminal. Alias: `/tp` |
| `/remote-env` | Choose the default cloud environment for cloud sessions started from the CLI |
| `/web-setup` | Connect GitHub for cloud sessions using your local `gh` credentials |

## Utility & Information

| Command | Purpose |
|---|---|
| `/help` | Show help and available commands |
| `/doctor` | **[Skill]** Setup checkup that diagnoses and can fix issues (installs, PATH, settings, unused skills/MCP/plugins, slow hooks). From the shell, `claude doctor` prints read-only diagnostics |
| `/status` | Settings → Status tab (version, model, account, connectivity) |
| `/release-notes` | Interactive changelog |
| `/feedback [report]` | Send product feedback (same dialog as `/bug`) |
| `/bug [report]` | Report a bug or share your conversation. Alias: `/share` |
| `/plan [description]` | Enter plan mode; with a description, start on it immediately |
| `/tasks` | View and manage background work (shells, subagents). Alias: `/bashes` |
| `/powerup` | Interactive feature lessons |
| `/voice [hold\|tap\|off]` | Voice dictation (requires a Claude.ai account) |
| `/mobile` | QR code for the Claude mobile app. Aliases: `/ios`, `/android` |
| `/exit` | Exit (detaches when attached to a background session). Alias: `/quit` |

## Setup & Platform-Specific

| Command | Purpose |
|---|---|
| `/plugin [subcommand]` | Manage plugins (`list`, `install`, `enable`, `disable`, `validate`, `marketplace …`) |
| `/reload-plugins [--force]` | Reload active plugins without restarting |
| `/install-slack-app` | Install the Claude Slack app (OAuth in browser) |
| `/setup-bedrock` / `/setup-vertex` | Provider setup wizards (hidden until `CLAUDE_CODE_USE_BEDROCK=1` / `CLAUDE_CODE_USE_VERTEX=1`) |

**MCP prompts** appear dynamically as `/mcp__<server>__<prompt>`.

## Model Aliases (for `/model` and `--model`)

| Alias | Resolves To (Anthropic API) | Best For |
|---|---|---|
| `default` | Clears any override; uses your account's default (see below) | Resetting |
| `best` | **Fable** where available to you, otherwise the same as `opus` | Most capable available |
| `fable` | Fable 5.1 | Hardest, longest-running tasks |
| `opus` | Opus 5 | Complex reasoning |
| `sonnet` | Sonnet 5 | Daily coding |
| `haiku` | Haiku 4.5 | Fast, simple tasks |
| `sonnet[1m]` / `opus[1m]` | 1M-context variants | `sonnet[1m]` has no effect on the Anthropic API because Sonnet 5 is always 1M |
| `opusplan` | Opus in plan mode → Sonnet for execution | Plan deeply, execute fast |

**Account default model:** Max, Team Premium, Enterprise, and API → Opus 5. Pro and Team Standard → Sonnet 5.

Aliases resolve differently on other providers (e.g. `sonnet` is Sonnet 4.6 on Claude Platform on AWS and Sonnet 4.5 on Bedrock/Vertex/Foundry). Pin with full IDs: `claude-fable-5-1`, `claude-opus-5`, `claude-sonnet-5`, `claude-haiku-4-5-20251001`.

## Effort Levels (for `/effort` and `--effort`)

| Level | Behaviour | Persists |
|---|---|---|
| `low` | Short, latency-sensitive tasks | Yes, when confirmed with `Enter` (press `s` for this session only) |
| `medium` | Cost-sensitive work, some intelligence trade-off | Same as above |
| `high` | **Default on every effort-capable model** except Opus 4.7 | Same as above |
| `xhigh` | Deeper reasoning at higher token spend (default on Opus 4.7) | Same as above |
| `max` | Deepest; prone to overthinking — test before adopting | **No** — session only (unless set via `CLAUDE_CODE_EFFORT_LEVEL`) |
| `ultracode` | Claude Code setting: `xhigh` plus dynamic workflows for substantive tasks | Session only via `/effort` |
| `auto` | Reset to the model default | — |

`xhigh` is supported on Fable 5.1/5, Opus 5, Sonnet 5, Opus 4.8 and 4.7; Opus 4.6 and Sonnet 4.6 top out at `high`/`max`. Haiku 4.5 does not support effort.

## Thinking Keyword

| Keyword | Effect |
|---|---|
| `ultrathink` | Documented. Claude Code adds an in-context instruction to reason more deeply **on that turn**. It does **not** change the effort level sent to the API. Phrases like "think" or "think hard" are passed through as ordinary text |

## Key Keyboard Shortcuts

| Shortcut | Action |
|---|---|
| `Shift+Tab` | Cycle permission modes: `default` (Manual) → `acceptEdits` → `plan` → (when available) `bypassPermissions` → `auto` |
| `Ctrl+O` | Toggle the transcript viewer (detailed tool usage) |
| `Ctrl+G` | Open your **prompt** in your default text editor |
| `Ctrl+T` | Show/hide Claude's task checklist (not background tasks — use `/tasks`) |
| `Ctrl+B` | Send running Bash commands/agents to the background |
| `Ctrl+R` | Reverse-search prompt history |
| `Option+P` / `Alt+P` | Switch model without clearing your prompt |
| `Option+T` / `Alt+T` | Toggle extended thinking (no effect on Fable) |
| `Option+O` / `Alt+O` | Toggle fast mode |
| `Esc` | Interrupt Claude / close a dialog |
| `Esc` `Esc` | With text in the prompt: clear it (saved to history). With an empty prompt: open the rewind menu |
| `↑` / `↓` | Move within a multi-line prompt, then navigate history |

## CLI Flags (at Startup)

```
claude --model <alias|name>            # Model for this session (e.g. fable, opus, sonnet, claude-sonnet-5)
claude --effort <level>                # Effort for this session
claude -n, --name "session-name"       # Name the session
claude -c, --continue                  # Continue the most recent session in this directory
claude -r, --resume [session]          # Picker, or resume by name/ID
claude --resume <id> --fork-session    # Resume into a new session ID
claude --from-pr 142                   # Picker filtered to sessions linked to a PR
claude --teleport                      # Resume a cloud session locally
claude --cloud "task description"      # Create a cloud session (--remote is a deprecated alias)
claude --remote-control [name]         # Interactive session with Remote Control (alias --rc)
claude --permission-mode plan          # Start in plan mode
claude -w, --worktree [name]           # Start in an isolated git worktree
claude --debug                         # Debug logging; filter with the = form: --debug=mcp,hooks
claude -p "prompt"                     # Non-interactive (print) mode
```

Shell subcommands: `claude update`, `claude doctor`, `claude auth login|logout|status`, `claude mcp …`, `claude plugin …`, `claude agents`, `claude attach <id>`, `claude logs <id>`, `claude stop <id>`, `claude respawn <id>`, `claude rm <id>`, `claude ultrareview [target]`.

## Where Sessions Are Stored

`~/.claude/projects/<encoded-project-path>/<session-id>.jsonl` — one file per conversation. Transcripts are **deleted automatically after 30 days** by default; change this with `cleanupPeriodDays` in `settings.json`, or `/export` anything you need to keep.

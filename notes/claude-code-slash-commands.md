# Claude Code — Built-in Slash Commands Reference (Updated)

> **Source:** Official docs (code.claude.com/docs), and current Anthropic model data.
> **Last updated:** September 2026 (second pass — see "What Changed" at the bottom; a first validation pass introduced several unverified alias claims of its own, which this pass removes or flags).
> **Change summary:** The model-alias and effort-level tables were one generation stale; several claimed command aliases turned out to be unconfirmed/incorrect against official docs; one command name had wrong spelling; `--remote` is deprecated (not absent). See "What Changed" at the bottom.

## Session & Context Management

| Command | Purpose |
|---|---|
| `/clear` | Clear conversation history and free up context. Aliases: `/reset`, `/new` |
| `/compact [instructions]` | Compact conversation with optional focus instructions (e.g., `/compact keep architecture decisions`) |
| `/context` | Visualize current context usage as a colored grid. Shows optimization suggestions and capacity warnings |
| `/btw <question>` | Ask a quick side question without adding it to the main conversation thread |
| `/rewind` | Rewind the conversation and/or code to a previous checkpoint. **No confirmed alias** — `/checkpoint` is not listed in official docs; drop that claim |
| `/resume [session]` | Resume a past session by ID or name, or open a session picker. **No confirmed alias** — `/continue` is not a slash command in official docs |
| `/rename [name]` | Rename the current session. Without a name, auto-generates one from conversation history |
| `/branch [name]` | Create a branch (fork) of the current conversation at this point. **No confirmed alias** — `/fork` was an alias in older versions (v2.1.77–v2.1.161) but is not current |
| `/export [filename]` | Export the current conversation as plain text. Without filename, opens dialog to copy or save |
| `/copy [N]` | Copy the last assistant response to clipboard. `/copy 2` copies the second-to-last. Press `w` in picker to write to file |

## Model & Configuration

| Command | Purpose |
|---|---|
| `/model [model]` | Switch the AI model mid-session. Use left/right arrows to also adjust effort level |
| `/effort [low\|medium\|high\|xhigh\|max\|auto]` | Set the model effort/thinking level. `max` is prone to overthinking — use sparingly; `xhigh`/`max` apply to the current session only |
| `/fast [on\|off]` | Toggle fast mode on or off |
| `/config` | Open the settings interface (theme, model, output style, preferences). Alias: `/settings` |
| `/theme` | Change the color theme, including colorblind-accessible (daltonized) and ANSI variants |
| `/color [color\|default]` | Set the prompt bar color for the current session. Options: red, blue, green, yellow, purple, orange, pink, cyan |
| `/output-style [style]` | Set response formatting style (Default, Explanatory, Learning, or custom). Use `/output-style:new` to create one |

## Agentic Workflows (Bundled Skills)

These use the same skill mechanism you can write yourself — a prompt handed to Claude, which Claude can also invoke automatically.

| Command | Purpose |
|---|---|
| `/batch <instruction>` | **[Skill]** Orchestrate large-scale changes across a codebase in parallel. Decomposes into 5–30 independent units, spawns one background agent per unit in isolated git worktrees, each opens a PR |
| `/simplify [focus]` | **[Skill]** Review recently changed files for code reuse, quality, and efficiency by spawning review agents in parallel, then applies fixes |
| `/debug [description]` | **[Skill]** Enable debug logging for the session and troubleshoot by reading the session debug log. Optionally describe the issue to focus analysis |
| `/loop [interval] [prompt]` | **[Skill]** Run a prompt repeatedly while the session stays open. Claude self-paces if no interval is given |
| `/claude-api` | **[Skill]** Load Claude API reference material for your project's language. Also auto-activates when your code imports the Anthropic SDK |
| `/code-review [level]` / `/review` (alias) / `ultrareview` | Review the current diff or a PR for correctness/quality issues. `ultra` level runs a cloud-hosted multi-agent review (`claude ultrareview [target]` from the shell). **Not present in the original PDF at all.** `/code-review` and its `/review` alias are confirmed in official docs; the `ultra` level and `ultrareview` subcommand are corroborated by several independent third-party write-ups but not spelled out explicitly in the official commands reference — treat as real-but-lightly-documented. |

## Permissions & Tools

| Command | Purpose |
|---|---|
| `/permissions` | Manage allow/ask/deny rules for tool permissions interactively. Alias: `/allowed-tools` |
| `/hooks` | View and configure hook rules for tool events |
| `/mcp` | Manage MCP server connections and OAuth authentication |
| `/add-dir <path>` | Add a working directory for file access during the current session |
| `/sandbox` | Toggle sandbox mode (available on supported platforms only) |

## Project Initialization & Memory

| Command | Purpose |
|---|---|
| `/init` | Initialize the project with a `CLAUDE.md` guide |
| `/memory` | Edit `CLAUDE.md` memory files, enable/disable auto-memory, and view auto-memory entries |
| `/skills` | List all available skills |
| `/agents` | Manage subagent configurations — view, create, and edit subagents |

## Git, Code Review & CI

| Command | Purpose |
|---|---|
| `/diff` | Open an interactive diff viewer for uncommitted changes and per-turn diffs |
| `/security-review` | Analyze pending changes on the current branch for security vulnerabilities |
| `/autofix-pr [prompt]` | Spawn a web session that watches the current branch's PR and pushes fixes when CI fails or reviewers leave comments. Requires `gh` CLI |
| `/install-github-app` | Set up the Claude GitHub Actions app for automated PR reviews. **Spelling corrected** — the original PDF and first-pass edit had it as `/install-githubapp` (no hyphen), which is wrong |

## IDE & Interface

| Command | Purpose |
|---|---|
| `/ide` | Manage IDE integrations and show status |
| `/desktop` | Continue the current session in the Claude Code Desktop app. **No confirmed alias** — `/app` is not listed in official docs |
| `/terminalsetup` | Configure terminal keybindings for VS Code, Alacritty, Warp |
| `/keybindings` | Open or create your keybindings configuration file |

## Account & Billing

| Command | Purpose |
|---|---|
| `/login` / `/logout` | Sign in/out of your Anthropic account |
| `/cost` | Show token usage and cost statistics |
| `/usage` | Show plan usage limits and rate limit status |
| `/upgrade` | Open the upgrade page to switch to a higher plan tier |

> Commands below this point (`/extra-usage`, `/privacy-settings`, `/passes`, `/insights`, `/stats`, `/powerup`, `/team-onboarding`, `/voice`, `/ultraplan`, `/mobile`) could not be confirmed against `claude --help` or other primary sources during this review. They are plausible but **unverified** — treat them as unconfirmed until checked against live product docs or `/help` inside a session.

## Cloud, Remote & Teleport

| Command | Purpose |
|---|---|
| `/schedule [description]` | Create, update, list, or run Cloud scheduled tasks conversationally |
| `/remote-control` | Make this session available for remote control from claude.ai. **No confirmed alias** — `/rc` is not listed in official docs. Corresponds to the real CLI flag `--remote-control [name]` |
| `/teleport` | Pull a Claude Code on the web session into your terminal. **No confirmed alias** — `/tp` is not listed in official docs. Corresponds to the real CLI flag `--teleport [session]` |

## Utility & Information

| Command | Purpose |
|---|---|
| `/help` | Show help and all available commands |
| `/doctor` | Diagnose and verify your Claude Code installation and settings |
| `/status` | Open Settings → Status tab (version, model, account, connectivity) |
| `/release-notes` | View the changelog in an interactive version picker |
| `/feedback [report]` | Submit feedback to Anthropic. **Note:** `/bug` is a separate command whose alias is actually `/share`, not `/feedback` — correct this from the original PDF |
| `/plan [description]` | Enter plan mode. Pass a description to begin immediately |
| `/tasks` | List and manage background tasks. **No confirmed alias** — `/bashes` is not listed in official docs |
| `/exit` | Exit the CLI. Alias: `/quit` |

## Setup & Platform-Specific

| Command | Purpose |
|---|---|
| `/plugin` | Manage Claude Code plugins |
| `/reload-plugins` | Reload all active plugins to apply changes without restarting |
| `/chrome` | Configure Claude in Chrome extension settings |

## Model Aliases (for `/model` command) — CORRECTED

| Alias | Resolves To | Best For |
|---|---|---|
| `default` | Clears override, uses plan default | Resetting to recommended |
| `best` | Latest Opus (**Opus 5**) | Most capable available |
| `sonnet` | **Sonnet 5** | Daily coding (default) |
| `opus` | **Opus 5** | Complex reasoning |
| `haiku` | Haiku 4.5 | Fast, simple tasks (unchanged — no Haiku 5 has shipped) |
| `fable` | **Fable 5.1** | *(missing from original PDF entirely)* |
| `opusplan` | Opus 5 (plan) → Sonnet 5 (execute) | Best of both worlds |

Full pinned model IDs: `claude-opus-5`, `claude-sonnet-5`, `claude-haiku-4-5-20251001`, `claude-fable-5-1`.

**Correction (second pass):** The original edit of this doc added `sonnet[1m]` / `opus[1m]` as if they were separate aliases for "now-native" 1M context. That's hallucinated — there is no `[1m]` suffix in the real `/model` alias list. 1M-token context is a property of the current models, not a distinct alias to select.

## Effort Levels (for `/effort` command) — CORRECTED

| Level | Behaviour | Persists Across Sessions |
|---|---|---|
| `low` | Faster, less thorough | Yes |
| `medium` | Lower-effort default on some plans | Yes |
| `high` | Deep reasoning; the documented default effort for current models (Sonnet 5, Opus 5, Fable 5.1) — **not confirmed** as specifically "Pro/Max = medium, API/Team/Enterprise = high" as an earlier pass of this doc claimed; that plan-based split could not be verified against official docs and may be outdated | Yes |
| `xhigh` | *(missing from original PDF)* Deep reasoning, higher token usage — best for most coding/agentic work on current-gen models | **Yes** (corrected — an earlier pass of this doc incorrectly grouped `xhigh` with `max` as session-only) |
| `max` | Maximum, prone to overthinking | No (current session only) |
| `auto` | Reset to model default | — |

Haiku 4.5 does not support effort levels.

## Thinking Keywords (Prompt-Level)

These only work in Claude Code's terminal — not in claude.ai chat or the API.

| Keyword | Effect |
|---|---|
| `ultrathink` | Triggers high effort for that one turn, then reverts to session default. **Unverified** — appears repeatedly in third-party blog posts, but could not be confirmed against official code.claude.com docs in this pass. Demo it live and confirm behavior in-session before presenting it as documented fact. |

## Key Keyboard Shortcuts

**Unverified (second pass).** The official keyboard-shortcuts doc page returned a 404 during this review, and a separate fetch of `desktop.md` showed at least one conflicting binding (`Ctrl+O` listed there as "cycle view modes," not "toggle verbose mode"). Treat this table as unconfirmed until checked live with `/help` or the current docs site.

| Shortcut | Action |
|---|---|
| `Shift+Tab` | Cycle through permission modes |
| `Ctrl+O` | Toggle verbose mode |
| `Ctrl+G` | Open current plan in your default text editor |
| `Ctrl+T` | Toggle background task list display |
| `Option+T` / `Alt+T` | Enable/disable extended thinking |
| `Esc Esc` (double tap) | Time machine — browse all prompts from current session |
| `↑` arrow | Navigate back through past prompts |

## CLI Flags (at Startup) — CORRECTED

Verified directly against `claude --help` output (Sep 2026):

```
claude --model <alias|name>            # Set model for this session (e.g. 'fable', 'opus', 'sonnet', or full name like 'claude-sonnet-5')
claude --name "session-name"           # Start with a named session
claude -n "session-name"               # Short form
claude --continue                      # Resume last session
claude -c                              # Short form
claude --resume [session]              # Open session picker or resume by name/ID
claude -r                              # Short form
claude --from-pr 142                   # Resume session linked to a PR
claude --teleport [session-id]         # Pull a cloud session to terminal
claude --cloud "task description"      # Create a cloud session with a description (preferred flag)
claude --remote "task description"     # Deprecated alias for --cloud — still works, but --cloud is current (CORRECTED — an earlier pass of this doc claimed --remote "does not exist"; it exists but is deprecated, not absent)
claude --remote-control [name]         # Enable Remote Control on an interactive session (also missing from the original PDF)
claude --permission-mode plan          # Start in Plan Mode
claude --worktree [name]               # Start in an isolated git worktree
claude -d, --debug [filter]            # Start with debug logging enabled, optionally filtered (e.g. "api,hooks")
claude -p "prompt"                     # Headless / non-interactive mode
```

Also present in the real CLI but absent from the original PDF entirely: `claude ultrareview [options] [target]` — runs a cloud-hosted multi-agent code review of the current branch or a PR, `claude agents`, `claude attach <id>`, `claude auth`, and several background-session subcommands (`logs`, `rm`, `stop|kill`, `respawn`).

## Where Sessions Are Stored

`~/.claude/projects/<encoded-project-path>/*.jsonl`

Each `.jsonl` file is a complete conversation record. Sessions never auto-delete. *(Not independently re-verified this pass — reproduced from original PDF as plausible/consistent with known behavior.)*

---

## What Changed From the Original PDF

The source PDF (`claude-code-slash-commands.pdf`, dated April 2026) had gone stale/inaccurate in a few specific ways by the time of this review (September 2026). This document has now gone through **two** correction passes — the second pass fact-checked the first pass's own corrections against official docs (code.claude.com/docs) and caught several new hallucinations the first pass introduced. Both rounds are folded in below.

**From PDF → first-pass correction:**

1. **Model Aliases table**: `sonnet` was listed as Sonnet 4.6, `opus` as Opus 4.6 — both are now **Sonnet 5** and **Opus 5**. The `fable` alias (Fable 5.1) was missing entirely.
2. **Effort Levels table**: missing the `xhigh` level, and incorrectly restricted `max` to "Opus 4.6 only" — `xhigh`/`max` now apply across current-generation models.
3. **Missing entirely from the PDF**: the `ultrareview` subcommand/`/code-review ultra` flow, and several background-session management commands (`claude agents`, `attach`, `logs`, `rm`, `stop`/`kill`, `respawn`).

**Second-pass corrections to the first pass itself (verified against official docs — see the inline notes above for each):**

4. **`--remote` flag**: the first pass claimed it "does not exist." It actually exists as a **deprecated alias for `--cloud`** — deprecated, not absent.
5. **Hallucinated model aliases**: the first pass invented `sonnet[1m]` / `opus[1m]` as if 1M context were a separate alias. There is no `[1m]` suffix — 1M context is a property of the current models, selected the normal way (`sonnet`, `opus`, etc).
6. **Hallucinated command aliases**: the first pass asserted several slash-command aliases that are not confirmed in official docs and should be treated as wrong: `/rewind`→`/checkpoint`, `/resume`→`/continue`, `/branch`→`/fork` (was real in old versions v2.1.77–v2.1.161, not current), `/desktop`→`/app`, `/remote-control`→`/rc`, `/teleport`→`/tp`, `/tasks`→`/bashes`, and `/feedback`→`/bug` (actually `/bug`'s alias is `/share`, unrelated to `/feedback`).
7. **Wrong command spelling**: `/install-githubapp` should be `/install-github-app` (hyphenated).
8. **Effort-level persistence**: the first pass grouped `xhigh` with `max` as "session only." `xhigh` actually persists across sessions like the other levels; only `max` is session-only.
9. **Effort-level plan defaults**: the first pass's specific claim ("medium = Pro/Max default, high = API/Team/Enterprise default") could not be verified against official docs in the second pass and may be stale — flagged as unverified rather than restated as fact.
10. **`ultrathink` keyword** and the **keyboard shortcuts table**: both are widely repeated in third-party sources but could not be confirmed against the current official docs site (one relevant page 404'd, another showed a conflicting binding). Flagged as unverified rather than presented as confirmed.

**Still unverified, not confirmed either way**: `/btw`, `/copy`, `/color`, `/context`, `/keybindings`, `/chrome` are now confirmed to exist; `/extra-usage`, `/privacy-settings`, `/passes`, `/insights`, `/stats`, `/powerup`, `/team-onboarding`, `/voice`, `/ultraplan`, `/mobile` remain unconfirmed either way.

**Confirmed accurate and unchanged**: `/clear` (aliases `/reset`, `/new`), `/compact`, `/model`, `/config` (alias `/settings`), `/permissions` (alias `/allowed-tools`), `/exit` (alias `/quit`), `/code-review` (alias `/review`), `/theme`, `/hooks`, `/mcp`, `/add-dir`, `/init`, `/memory`, `/agents`, `/cost`, `/doctor`, `/help`, `/status`, `/export`, `/plan`, `/output-style`, `/ide`, plus the CLI flags `--model`, `-n/--name`, `-c/--continue`, `-r/--resume`, `--from-pr`, `--teleport`, `--cloud`, `--permission-mode`, `-w/--worktree`, `-d/--debug`, `-p/--print`.

**Caveat:** A full in-session `/help` listing was not available in either review pass (headless/agent checks only covered docs pages and web search, not a live interactive session). To fully close out the remaining unverified items — keyboard shortcuts, `ultrathink`, and the "still unconfirmed" command list — run `/help` inside an interactive Claude Code session and diff it against this table.

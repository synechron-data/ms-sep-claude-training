# Claude Code — Session Management Guide

## Save, Rename & Resume Chats/Sessions

> **Source:** Official Claude Code Docs — [Manage sessions](https://code.claude.com/docs/en/sessions), [CLI reference](https://code.claude.com/docs/en/cli-reference), [Commands](https://code.claude.com/docs/en/commands)
> **Last verified:** September 2026 | Claude Code v2.1.x

---

## How Sessions Work

All conversations in Claude Code are **automatically saved locally** as you work — no manual action needed. When you resume, the session restores:

- Complete message history, including tool calls and results
- The model the session was using (unless it's been retired, or you pick a different one at launch with `--model`)
- The permission mode — when you resume from the terminal with `claude --continue` or `claude --resume <name-or-id>` (not when you pick from the session picker or use `/resume`)

**Not restored:** launch flags such as `--mcp-config`, `--settings`, `--plugin-dir`, and `--add-dir` — pass them again when you resume. Settings in `settings.json` files are re-read at launch, so those don't need repeating.

**Storage location:**

```
~/.claude/projects/<project>/<session-id>.jsonl
```

`<project>` is your working directory path with non-alphanumeric characters replaced by `-`. Each `.jsonl` file is one conversation.

> **Retention:** Transcripts are **automatically deleted after 30 days** by default. To keep them longer, set `cleanupPeriodDays` in `settings.json`. If a conversation matters, `/export` it.

---

## Starting a Named Session

The single best habit — name a session **when you start it**, not after.

```bash
# Short form
claude -n "feature-auth-system"

# Long form
claude --name "bugfix-stripe-webhook"
```

### Naming Conventions That Work

```bash
# ✅ Good names
claude -n "feature-user-auth"
claude -n "bugfix-payment-retry"
claude -n "refactor-api-routes"
claude -n "review-pr-142"
claude -n "docs-api-reference"

# ❌ Avoid these
claude -n "work"          # Too vague
claude -n "test"          # What test?
claude -n "2026-04-15"    # Date alone doesn't convey purpose
```

> **Tip:** Consistent prefixes like `feature-`, `bugfix-`, `refactor-`, `docs-`, `review-` make your session list scannable at a glance.

> **Duplicate names:** If another live session on your machine already uses the name, Claude Code gives yours a variant with a two-word suffix (e.g. `feature-user-auth-graceful-unicorn`) and tells you. Use `/rename` to pick your own.

---

## Renaming a Session

### During an Active Session

```
/rename payment-integration      # the name also appears on the prompt bar
```

**Forgot to name it?** Unnamed sessions still get an AI-generated title (a short summary of your first prompt). It shows in the session picker and works as a resume handle, but a name you choose is easier to remember.

### From the Session Picker

```
/resume        # open the picker
               # highlight any session
               # press Ctrl+R to rename it
```

---

## Resuming Sessions — All Methods

### 1. Continue Last Session (Quickest)

Picks up exactly where you left off in the most recent session for the current directory. No picker, no selecting.

```bash
claude --continue
claude -c              # short form
```

**Best for:** "I stepped away for lunch and need to continue."

> `--continue` skips sessions created with `claude -p` (headless). If there's no session in this directory yet, it prints `No conversation found to continue`.

---

### 2. Browse All Sessions (Interactive Picker)

Opens an interactive list of your sessions for the current project.

```bash
claude --resume
claude -r              # short form
```

**Keyboard shortcuts inside the picker:**

| Key | Action |
|---|---|
| `↑` / `↓` | Navigate between sessions |
| `Enter` | Resume the highlighted session |
| `Space` | Preview the session before resuming |
| `Ctrl+R` | Rename the highlighted session |
| `/` or start typing | Search / filter sessions (you can also paste a PR URL) |
| `Ctrl+A` | Toggle between the current project and all projects |
| `Ctrl+W` | Toggle all worktrees of the current repo (multi-worktree repos only) |
| `Ctrl+B` | Filter to sessions from the current git branch |
| `→` / `←` | Expand / collapse grouped sessions |
| `Esc` | Exit the picker or search mode |

> Plain letters (like `P` or `R`) don't trigger actions — they start a search.

---

### 3. Resume a Specific Session by Name or ID

```bash
# By name (if you used --name or /rename)
claude --resume "feature-auth-system"
claude -r "bugfix-stripe-webhook"

# By session ID (a UUID)
claude --resume <session-id>
```

If the name matches more than one session, the picker opens with the name pre-filled as a search.

---

### 4. Switch Sessions Mid-Chat (Without Exiting)

You don't need to exit and relaunch. From inside any active session:

```
/resume                          # open the picker
/resume feature-payments-v2      # jump straight to a named session
```

> **This is huge if you bounce between tasks a lot** — no terminal juggling needed.
>
> ⚠️ Don't `/resume` a session that's **still open in another terminal** — messages from both terminals interleave into one transcript. Switch to that terminal window instead.

---

### 5. Find a Session Linked to a Pull Request

When Claude creates a PR (`gh pr create`) or GitLab MR (`glab mr create`), the session is automatically linked to it.

```bash
claude --from-pr 142
```

This opens the session picker **filtered** to sessions linked to that PR — select one to resume. It also accepts a full GitHub, GitLab, or Bitbucket PR URL.

---

### 6. Resume + Send a Prompt in One Step (Scripting / Headless)

```bash
claude --continue --print "continue with the frontend implementation"
claude -c -p "run the tests and fix any failures"
```

---

## Exporting a Session to a File

Save the conversation as plain text — useful for documentation, sharing with teammates, keeping a record before a risky refactor, or keeping a conversation past the 30-day retention window.

```
/export                          # opens menu: copy to clipboard or save to file
/export payments-context.md      # saves directly to that filename
```

---

## Branching and Forking a Session

When you want to try a different direction without losing your current thread. These are **two different commands**:

```
/branch                          # copy the conversation and SWITCH INTO the copy
/branch try-oauth2               # same, with a name for the branch
```

`/branch` leaves the original unchanged — return to it with `/resume <original-name>`.

```
/fork                            # copy the conversation into a new BACKGROUND session;
                                 # you keep working in the current one
/fork explore JWT refresh flow   # the copy starts working on this prompt immediately
```

Or from the CLI, when resuming:

```bash
claude --resume <session-id> --fork-session
claude --continue --fork-session
```

`--fork-session` is an on/off flag — it doesn't take a session ID itself. Each branch or fork gets its own session ID, and you can resume both independently.

**Use case:** "I've analysed the auth module. Now I want to explore OAuth2 in one branch and JWT in another — without losing either thread."

---

## Managing Context in Long Sessions

Sessions grow as you work. Once the context window fills up, quality can degrade.

### Check Context Usage

```
/context
```

Shows a visual grid of how full your context is, plus optimization suggestions.

### Compact (Summarise Older Messages)

```
/compact                                          # summarise the conversation so far
/compact keep the database design decisions       # guided compaction
/compact keep architecture choices, summarise the rest
```

Consider compacting when:

- Token usage exceeds 50%
- You're switching to a different topic
- After extensive trial-and-error (failed approaches can be summarised away)

### Clear and Start Fresh

```
/clear                # start a new conversation with empty context
/clear release-prep   # same, and label the conversation you're leaving
/reset                # alias
/new                  # alias
```

> After completing a feature, use `/clear` to reset context. The previous conversation is still saved — get back to it with `/resume`. Each session focused on a single topic is easier to resume and less prone to mistakes from overloaded context.

---

## Day-to-Day Workflow Pattern

```bash
# Monday morning — start a named session for a specific task
claude -n "feature-payments-v2"

# ... work all day ...
# Close terminal — session is auto-saved

# Tuesday morning — pick up exactly where you left off
claude --continue

# Later — need to switch tasks mid-day
/resume                          # open picker, select "bugfix-login-redirect"

# Switch back to payments
/resume feature-payments-v2

# Before a big risky refactor — export a record
/export payments-context-backup.md

# Context getting full — compact but keep key decisions
/compact keep architecture decisions and API design choices

# Feature complete — start clean for the next task
/clear
```

---

## Parallel Task Workflow

Working on multiple features simultaneously? Give each session its own **git worktree** — a separate checkout on its own branch — so their file edits don't collide:

```bash
# Terminal window 1
claude --worktree feature-auth-oauth

# Terminal window 2
claude --worktree bugfix-report-export

# Terminal window 3
claude --worktree refactor-user-service
```

> The repo needs at least one commit before `--worktree` works.

To switch between parallel sessions, **switch terminal windows** — don't `/resume` a session that's live in another terminal (messages from both would interleave into one transcript).

---

## Quick Reference Card

| What You Want to Do | Command |
|---|---|
| Start a named session | `claude -n "feature-name"` |
| Rename current session | `/rename new-name` |
| Rename from the picker | `/resume` → highlight → `Ctrl+R` |
| Continue last session | `claude -c` or `claude --continue` |
| Browse all sessions | `claude -r` or `claude --resume` |
| Resume specific session | `claude -r "session-name"` |
| Switch sessions mid-chat | `/resume` or `/resume session-name` |
| Find session from PR | `claude --from-pr 142` |
| Resume + send prompt | `claude -c -p "prompt text"` |
| Branch (switch into copy) | `/branch [name]` |
| Fork (copy to background) | `/fork [prompt]` |
| Fork when resuming | `claude -r <id> --fork-session` |
| Parallel isolated session | `claude --worktree <name>` |
| Export conversation | `/export [filename]` |
| Check context usage | `/context` |
| Compact conversation | `/compact [instructions]` |
| Clear and start fresh | `/clear` |

---

## Pro Tips

**Name sessions early.** It's much easier to find `"payment-integration"` than `"explain this function"` three days later.

**One session per task.** Don't pile unrelated work into one session. Use `/clear` between distinct tasks and name each one.

**Preview before resuming.** In the picker, press `Space` to preview a session's content before you open it.

**Rename from the picker.** If you forgot to name a session when starting, press `Ctrl+R` inside the `/resume` picker to rename it retroactively.

**Export before big refactors.** Use `/export` to create a paper trail of reasoning before you make drastic changes.

**Remember the 30-day window.** Transcripts are cleaned up after 30 days by default. Export anything you need to keep, or raise `cleanupPeriodDays` in `settings.json`.

**Watch your context.** Run `/context` regularly during long sessions. Once it fills up, response quality drops — compact early rather than late.

---

*Source: [Manage sessions](https://code.claude.com/docs/en/sessions) — Official Anthropic Documentation*

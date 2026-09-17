# Claude Code — Models Guide

## Which Model to Use & When to Switch

> **Source:** Official model configuration docs ([code.claude.com/docs/en/model-config](https://code.claude.com/docs/en/model-config)).
> **Last verified:** September 2026

## Available Models at a Glance

| Alias | Current Model (Anthropic API) | Context Window | Best For |
|---|---|---|---|
| `fable` | Claude Fable 5.1 | 1M | Hardest, longest-running tasks — ambiguous investigations, architecture, outage debugging |
| `best` | Fable where it's available to you, otherwise the same as `opus` | — | "Give me the most capable model I have" |
| `opus` | Claude Opus 5 | 1M (see Extended Context) | Complex reasoning, deep analysis |
| `sonnet` | Claude Sonnet 5 | 1M on every plan | Daily coding |
| `haiku` | Claude Haiku 4.5 | 200K | Fast, simple, cheap tasks |
| `opus[1m]` / `sonnet[1m]` | 1M-context variants | 1M | Explicit 1M selection (`sonnet[1m]` has no effect on the Anthropic API — Sonnet 5 is always 1M) |
| `opusplan` | Opus in plan mode → Sonnet for execution | — | Plan with Opus, execute with Sonnet |
| `default` | Clears any override → your account's default model | — | Resetting |

Aliases point to the recommended version and update over time. To pin a version, use the full model ID (e.g. `claude-opus-5`) or `ANTHROPIC_DEFAULT_OPUS_MODEL` / `ANTHROPIC_DEFAULT_SONNET_MODEL` / `ANTHROPIC_DEFAULT_HAIKU_MODEL` / `ANTHROPIC_DEFAULT_FABLE_MODEL`.

### Aliases differ by provider

| Provider | `opus` | `sonnet` |
|---|---|---|
| Anthropic API | Opus 5 | Sonnet 5 |
| Claude Platform on AWS | Opus 5 | Sonnet 4.6 |
| Amazon Bedrock, Google Cloud's Agent Platform | Opus 5 | Sonnet 4.5 |
| Microsoft Foundry | Opus 4.6 | Sonnet 4.5 |

If you run Claude Code against a cloud provider, check `/status` rather than assuming the Anthropic API mapping.

### Default model by account

| Account | Default model |
|---|---|
| Max, Team Premium, Enterprise, Anthropic API | Opus 5 |
| Claude Platform on AWS, Bedrock, Google Cloud's Agent Platform | Opus 5 |
| Pro, Team Standard | Sonnet 5 |
| Microsoft Foundry | Sonnet 4.5 |

Fable is never the default — select it explicitly with `/model fable`. On subscription plans, Fable requests can bill usage credits; Claude Code asks for consent first in interactive sessions.

## Full Model IDs (Pinned Versions)

```
claude-fable-5-1
claude-fable-5
claude-opus-5
claude-sonnet-5
claude-haiku-4-5-20251001

# Other models you can pin explicitly:
claude-opus-4-8
claude-opus-4-7
claude-opus-4-6
claude-sonnet-4-6
claude-sonnet-4-5-20250929
claude-opus-4-5-20251101
```

Minimum Claude Code versions: Opus 5 needs v2.1.219+, Sonnet 5 needs v2.1.197+. Run `claude update` if an alias doesn't resolve as expected.

## What Each Model Is Actually Like

### 🟣 Fable 5.1 — The Long-Haul Specialist

The most capable models in Claude Code, suited to work bigger than a single sitting. They sustain long autonomous sessions, investigate before acting, and verify their own work more often.

**What it handles well:**
- Ambiguous root-cause investigations and outage debugging
- Architecture decisions with many trade-offs
- Large, multi-step changes you want done with minimal hand-holding

**When Fable is the right choice:** When the problem is open-ended or long-running, and you'd rather Claude investigate thoroughly than move fast. Skip "remember to test it" reminders — it verifies on its own.

### 🔵 Sonnet 5 — Your Daily Driver

Strong reasoning applied to everyday work — fast enough for real-time collaboration, capable enough that most problems won't outgrow it.

**What it handles well:**
- Writing new features and components
- Fixing standard bugs and refactoring
- Writing tests and documentation
- PR creation and code reviews

**When Sonnet is the right choice:** If you're not sure, start here.

### 🟠 Opus 5 — The Deep Thinker

For complex tasks that need sustained reasoning. Uses more of your plan's limits — reserve it for work that needs it.

**What it handles well:**
- Hard bugs — intermittent, race conditions, concurrency
- Architecture and system design decisions
- Security audits and vulnerability analysis
- Complex multi-file refactors with deep interdependencies
- Understanding unfamiliar or legacy codebases

**When Opus is the right choice:** When Sonnet has tried twice and keeps going off-track.

### 🟢 Haiku 4.5 — The Sprinter

Fast and light on your limits. Still the current Haiku generation. Haiku 4.5 does **not** support effort levels.

**What it handles well:**
- Bulk mechanical edits (copyright headers, renames)
- Simple lookups and short answers
- Formatting passes and boilerplate

**When Haiku is the right choice:** When the task is mechanical and doesn't need judgment.

### 🔄 opusplan — The Hybrid

Uses `opus` during plan mode, then switches to `sonnet` for execution.

```
claude --model opusplan
# or mid-session:
/model opusplan
```

To force 1M context in both phases when your plan doesn't upgrade Opus automatically, use `opusplan[1m]`.

## How to Switch Models — All Methods

**1. Mid-session (most common)**
```
/model sonnet       # switch and save as your default
/model opus
/model haiku
/model fable
/model opusplan
/model default      # back to your account's default
/model              # picker: Enter saves as default, s = this session only,
                    # left/right arrows adjust effort
```
Shortcut: `Option+P` (macOS) / `Alt+P` (Windows/Linux) switches model without clearing your prompt.

**2. At startup (this session only)**
```
claude --model opus
claude --model fable
claude --model claude-opus-5    # pinned version
```

**3. Environment variables**
```
export ANTHROPIC_MODEL="opus"            # forces this model at every launch
export ANTHROPIC_DEFAULT_MODEL="sonnet"  # default for new sessions, unless /model, --model,
                                         # settings, or an org default choose one (v2.1.236+)
```

**4. Settings file**
```jsonc
// .claude/settings.json (project) or ~/.claude/settings.json (user)
{
  "model": "sonnet"
}
```
`model` sets the *initial* selection; users can still switch with `/model`. Admins who need enforcement use `availableModels` + `enforceAvailableModels` in managed settings.

**5. One-shot / headless**
```
claude --model opus -p "Analyse this architecture and suggest improvements"
claude --model haiku -p "Update copyright year to 2026 in all files"
```

## Effort Levels — Independent From Model

Effort controls adaptive reasoning: how much the model thinks on each step.

| Model | Supported levels |
|---|---|
| Fable 5.1, Fable 5 | `low`, `medium`, `high`, `xhigh`, `max` |
| Opus 5, Sonnet 5, Opus 4.8, Opus 4.7 | `low`, `medium`, `high`, `xhigh`, `max` |
| Opus 4.6, Sonnet 4.6 | `low`, `medium`, `high`, `max` |
| Haiku 4.5 | not supported |

| Level | When to use it | Persists |
|---|---|---|
| `low` | Short, scoped, latency-sensitive tasks | Saved when confirmed with `Enter` (press `s` for this session only) |
| `medium` | Cost-sensitive work that can trade some intelligence | Same |
| `high` | **The default on every effort-capable model** except Opus 4.7 | Same |
| `xhigh` | Deeper reasoning at higher token spend; the default on Opus 4.7 | Same |
| `max` | Deepest; diminishing returns and prone to overthinking — test first | **Session only** (unless set via `CLAUDE_CODE_EFFORT_LEVEL`) |
| `ultracode` | Claude Code setting: `xhigh` plus dynamic workflows for substantive tasks | Session only via `/effort` |
| `auto` | Reset to the model default | — |

```
/effort low | medium | high | xhigh | max | ultracode | auto | status
claude --effort xhigh
```

If you pick a level the model doesn't support, Claude Code uses the highest supported level below it (e.g. `xhigh` runs as `high` on Opus 4.6).

## The `ultrathink` Keyword

Include `ultrathink` anywhere in a prompt to ask for deeper reasoning **on that turn only**. Claude Code adds an in-context instruction; the **effort level sent to the API does not change**. "think" / "think hard" are passed through as ordinary words.

```
ultrathink — why is this auth flow failing intermittently?
```

| Method | Scope | Best when |
|---|---|---|
| `ultrathink` in the prompt | One turn | One hard question mid-conversation |
| `/effort xhigh` | Saved default | You want deeper reasoning throughout |
| `/effort max` | Current session | Hardest problems — use sparingly |

## Extended Thinking

- Fable models, Sonnet 5, and Opus 4.7+ always use adaptive reasoning. `MAX_THINKING_TOKENS` (non-zero) and `CLAUDE_CODE_DISABLE_ADAPTIVE_THINKING` only apply to Opus 4.6 / Sonnet 4.6.
- `MAX_THINKING_TOKENS=0` turns thinking off on the Anthropic API — except on Fable, where thinking can't be turned off.
- `Option+T` / `Alt+T` toggles extended thinking for the session (no effect on Fable).

## When to Switch — Decision Guide

**Stay on Sonnet when:** writing features, fixing standard bugs, refactoring, writing tests/docs, creating PRs, or you want fast output without burning limits.

**Switch to Opus when:** a hard or intermittent bug keeps beating Sonnet, you're designing architecture, analysing an unfamiliar codebase, or running a security audit.

**Switch to Fable when:** the task is open-ended and long-running, or you want Claude to investigate and verify with minimal steering.

**Switch to Haiku when:** the work is mechanical (headers, renames, formatting), a quick lookup, or you're conserving limits.

**Switch to opusplan when:** you want a thorough Opus plan and faster Sonnet execution for a large feature or migration.

## Rule of Thumb

> Start with Sonnet. Escalate to Opus (or Fable) only when you genuinely hit a ceiling. Drop to Haiku for anything mechanical.

## Environment Variables for Model Configuration

```bash
export ANTHROPIC_MODEL="sonnet"                         # force a model at launch
export ANTHROPIC_DEFAULT_MODEL="sonnet"                 # default for new sessions (v2.1.236+)
export ANTHROPIC_DEFAULT_OPUS_MODEL="claude-opus-5"     # pin what the aliases resolve to
export ANTHROPIC_DEFAULT_SONNET_MODEL="claude-sonnet-5"
export ANTHROPIC_DEFAULT_HAIKU_MODEL="claude-haiku-4-5-20251001"
export ANTHROPIC_DEFAULT_FABLE_MODEL="claude-fable-5-1"
export CLAUDE_CODE_EFFORT_LEVEL="high"                  # force an effort level
export CLAUDE_CODE_DISABLE_1M_CONTEXT=1                 # treat models as 200K
```

## Checking Your Current Model

```
/status     # version, model, account, connectivity
/model      # picker highlights the active model
/effort status
```

## Extended Context (1M Tokens)

- **Sonnet 5** always runs with a 1M window on the Anthropic API — every plan, no usage credits.
- **Fable 5.1 / Fable 5** and **Opus 4.7+** run with 1M by default on the Anthropic API.
- **Opus with 1M:** included on Max, Team (Standard and Premium seats), and Enterprise; **requires usage credits on Pro**; full access on API pay-as-you-go.
- **Sonnet 4.6 with 1M** requires usage credits on every subscription plan.
- No price premium for tokens beyond 200K. Turn 1M off with `CLAUDE_CODE_DISABLE_1M_CONTEXT=1`.

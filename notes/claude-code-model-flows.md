# Claude Code — Model Routing Flows

> **Sources:** [How Claude Code works](https://code.claude.com/docs/en/how-claude-code-works), [Model configuration](https://code.claude.com/docs/en/model-config), [LLM gateways](https://code.claude.com/docs/en/llm-gateway), [Ollama — Claude Code integration](https://docs.ollama.com/integrations/claude-code)
> **Last verified:** September 2026

## The Two Flows

```
Flow 1:  Terminal → Claude Code → Anthropic API → Fable / Opus / Sonnet / Haiku
Flow 2:  Terminal → Claude Code → Ollama        → local (or Ollama Cloud) model
```

Both are valid. The difference is where inference happens and which model does the reasoning.

---

## Flow 1 — Claude Code → Anthropic API

### How it works

Claude Code is an **agentic harness** around Claude: it provides the tools, context management, and execution environment. The model reasons and decides what to do; Claude Code runs the loop:

```
You type a prompt
  → Claude Code sends the conversation + tool definitions to the API
      (model chosen by /model, --model, or settings)
  → The model streams back text and tool calls
  → Claude Code runs the tools locally (read files, edit, run commands),
    subject to your permission rules
  → Tool results go back in the next request
  → Repeat until the task is done
```

### Model options

| Alias | Model (Anthropic API) | Best for |
|---|---|---|
| `haiku` | Claude Haiku 4.5 | Fast, mechanical tasks |
| `sonnet` | Claude Sonnet 5 | Daily development |
| `opus` | Claude Opus 5 | Complex architecture, hard debugging |
| `fable` | Claude Fable 5.1 | Hardest, longest-running tasks |

Switch mid-session with `/model`, or at launch:

```bash
claude --model sonnet
claude --model claude-opus-5     # pinned ID
```

### Requirements

- Internet connection
- **One** of: a Claude subscription (Pro, Max, Team, Enterprise) signed in with `/login`, **or** an Anthropic Console account / `ANTHROPIC_API_KEY`, **or** your organization's Bedrock / Google Cloud / Foundry setup
- An API key is **not** required when you sign in with a subscription

---

## Flow 2 — Claude Code → Ollama → Local Model

### How it works

Ollama exposes an **Anthropic-compatible API**, so Claude Code can send its requests to Ollama instead of Anthropic by changing the base URL.

**Quickest way** (Ollama sets everything up):

```bash
ollama launch claude
```

**Manual setup:**

```bash
export ANTHROPIC_AUTH_TOKEN=ollama
export ANTHROPIC_API_KEY=""
export ANTHROPIC_BASE_URL=http://localhost:11434

claude --model <model-name>
```

```
You type a prompt
  → Claude Code sends the request to http://localhost:11434 (ANTHROPIC_BASE_URL)
  → Ollama runs the model locally (GPU/CPU)
  → Ollama returns an Anthropic-format response
  → Claude Code runs the tools locally
  → Repeat until done
```

### Making it persistent

Put the variables in `~/.claude/settings.json`:

```json
{
  "env": {
    "ANTHROPIC_BASE_URL": "http://localhost:11434",
    "ANTHROPIC_AUTH_TOKEN": "ollama",
    "ANTHROPIC_API_KEY": ""
  }
}
```

Settings files override shell variables, so if you later switch back to Anthropic, remove these entries (and check with `/status`).

### Choosing a model

- Pick a model **with tool-calling support** — browse [ollama.com/search?c=tools](https://ollama.com/search?c=tools). Without reliable tool calling, Claude Code behaves like a plain chat app: it answers but won't edit files or run commands.
- Set the **context window to 64k tokens or more** (Ollama's recommendation for Claude Code); larger repositories need more.
- Model names change quickly; check Ollama's current recommendations rather than a fixed list.

### Local vs Ollama Cloud

- **Local models** run on your machine — your code doesn't leave it for inference.
- **Ollama Cloud models** (names ending in `:cloud`, or `ANTHROPIC_BASE_URL=https://ollama.com` with an Ollama API key) run on Ollama's servers — your prompts and code **are** sent there.
- Ollama notes that hosted WebSearch and some advanced tool controls aren't fully supported.

### Requirements

- Ollama installed and running
- Enough RAM/VRAM for the model you pick (larger coding models need substantially more)
- No Anthropic account needed
- Internet only to download models (for local models)

---

## Side-by-Side

| | Flow 1 (Anthropic) | Flow 2 (Ollama, local model) |
|---|---|---|
| Model quality | Fable / Opus / Sonnet / Haiku | Depends on the local model |
| Cost | Subscription limits or API pricing | Your hardware and electricity |
| Privacy | Code sent to Anthropic | Stays on your machine (local models only) |
| Speed | Cloud inference | Depends on your hardware |
| Tool calling | Full | Depends on the model |
| Setup | Sign in or API key | Ollama + model download + context settings |

---

## The Key Insight

Claude Code is **not** a model — it's the agentic harness. It handles:

- The agentic loop
- Tool execution (files, shell, git, web, MCP)
- Permissions and hooks
- Context management and compaction
- Session persistence

The model only handles reasoning. Changing the model (with `--model` or `ANTHROPIC_BASE_URL`) changes how well the loop reasons; the harness stays the same.

```
            Claude Code (agentic harness)
   agentic loop · tools · permissions · context · sessions
                        │
               ANTHROPIC_BASE_URL
            ┌───────────┴───────────┐
   api.anthropic.com           localhost:11434
 Fable / Opus / Sonnet / Haiku   Ollama → local model
    (cloud inference)            (local inference)
```

## References

- Ollama — Claude Code integration: https://docs.ollama.com/integrations/claude-code
- How Claude Code works: https://code.claude.com/docs/en/how-claude-code-works
- Model configuration: https://code.claude.com/docs/en/model-config

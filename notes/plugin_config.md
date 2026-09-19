# Plugin + Local Marketplace Setup

*Verified against Claude Code docs, September 2026.*

## 1. Structure your plugin

```
sync-plugin/
├── .claude-plugin/
│   ├── plugin.json       # plugin manifest + hook definitions (only manifests go in here)
│   └── marketplace.json  # marketplace manifest listing this plugin
├── .mcp.json             # MCP servers bundled with the plugin
├── commands/             # slash commands (.md with frontmatter)
├── agents/               # subagents (.md with frontmatter)
├── skills/               # skills (<name>/SKILL.md with frontmatter)
└── hooks/                # hook scripts (.js) referenced from plugin.json — no hooks.json
```

- The manifest file must be named exactly `plugin.json` (not `plugins.json`) — otherwise it isn't read.
- Any paths you set in `plugin.json` must be relative and start with `./`. If you use the default folder names above, you can leave those keys out entirely.

### Hooks — defined inline in `plugin.json`

Instead of a separate `hooks/hooks.json`, hooks are defined under the `hooks` key of `plugin.json`. The value has the same shape as the `hooks` block in `settings.json`:

```json
{
  "name": "sync-plugin",
  "version": "1.0.0",
  "description": "Standard Claude Code setup for internal Synechron Team",
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          { "type": "command", "command": "node \"${CLAUDE_PLUGIN_ROOT}/hooks/bash-guard.js\"" }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          { "type": "command", "command": "node \"${CLAUDE_PLUGIN_ROOT}/hooks/format-fix.js\"" }
        ]
      }
    ],
    "Stop": [
      {
        "hooks": [
          { "type": "command", "command": "node \"${CLAUDE_PLUGIN_ROOT}/hooks/session-log.js\"" }
        ]
      }
    ],
    "Notification": [
      {
        "matcher": "permission_prompt|idle_prompt",
        "hooks": [
          { "type": "command", "command": "node \"${CLAUDE_PLUGIN_ROOT}/hooks/slack-notify.js\"" }
        ]
      }
    ]
  }
}
```

- Reference scripts with `${CLAUDE_PLUGIN_ROOT}`, not `$CLAUDE_PROJECT_DIR`. The plugin is copied to a cache when installed, so project-relative paths won't find its scripts.
- **Quote the path** (`\"${CLAUDE_PLUGIN_ROOT}/...\"`). Without quotes, the command breaks when the plugin lives under a folder with spaces, e.g. `D:\Github - Manish\...`.
- Inside the scripts, keep using `CLAUDE_PROJECT_DIR` for anything that belongs to the project (log files, `backend/`, `frontend/`). Only the script's own location needs `CLAUDE_PLUGIN_ROOT`.
- Don't add a `hooks/hooks.json` with the same hooks. It's loaded by default, so each hook would run twice.
- `slack-notify.js` reads `SLACK_WEBHOOK_URL`, which the plugin doesn't provide. Set it in the project's `settings.json` `env` block or in your environment.
- The same hooks shouldn't also stay in the project's `.claude/settings.json`, for the same reason.
- In `marketplace.json`, point the plugin entry at the plugin root, e.g. `"source": "./"` (paths resolve from the folder that contains `.claude-plugin/`).

## 2. Add MCP servers (optional)

Put a `.mcp.json` at the **plugin root**, next to `.claude-plugin/` — not inside it. Same format as a project `.mcp.json`:

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
      "headers": { "Authorization": "Bearer ${GITHUB_PAT}" }
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres", "${POSTGRES_URL}"]
    }
  }
}
```

**Environment variables each person sets** (never in the file):

| Variable | Used by | Example value |
|---|---|---|
| `GITHUB_PAT` | `github` | `github_pat_…` (fine-grained token) |
| `POSTGRES_URL` | `postgres` | `postgresql://<user>:<password>@localhost:5432/<db>` |

Windows (PowerShell), then open a **new terminal**:

```powershell
setx GITHUB_PAT "github_pat_xxx"
setx POSTGRES_URL "postgresql://<user>:<password>@localhost:5432/<db>"
```

macOS / Linux (add to `~/.zshrc` or `~/.bashrc`):

```bash
export GITHUB_PAT="github_pat_xxx"
export POSTGRES_URL="postgresql://<user>:<password>@localhost:5432/<db>"
```

If a variable isn't set, that server fails to start. Check `/mcp`.

- **No `plugin.json` key needed** when using the default `.mcp.json` location. If you set one: `"mcpServers": "./.mcp.json"` (or define the servers inline under `mcpServers`).
- **Local (stdio) servers shipped in the plugin** must use `${CLAUDE_PLUGIN_ROOT}` so paths resolve wherever the plugin is installed:
  ```json
  "sync-server": {
    "command": "node",
    "args": ["${CLAUDE_PLUGIN_ROOT}/servers/sync-server.js"]
  }
  ```
- **No secrets in the file.** Tokens, passwords and connection strings go in environment variables (`${GITHUB_PAT}`, `${POSTGRES_URL}`), which Claude Code expands from each person's environment. Everyone who installs the plugin gets this file.
- Servers **start automatically** when the plugin is enabled and appear in `/mcp` as `plugin:sync-plugin:<server>`. Each person still authenticates (e.g. Atlassian via `/mcp` → **Authenticate**) with their own account.
- **Avoid duplicates:** if the project's own `.mcp.json` also defines `atlassian` / `github`, you'll get two sets of the same tools. Remove one, or disable one in `/mcp`.

## 3. Validate

Run from anywhere; pass a **directory**, not a file:

```bash
claude plugin validate path/to/sync-plugin
```

- Checks `plugin.json` (including the inline `hooks` block) / `marketplace.json`, and the skill, agent and command frontmatter.
- Add `--strict` to fail on warnings (useful in CI).
- Inside a session: `/plugin validate path/to/sync-plugin`

## 4. Register the folder as a local marketplace (project scope)

In the other project:

```bash
claude plugin marketplace add path/to/sync-plugin --scope project
```

## 5. Install the plugin (project scope)

```bash
claude plugin install sync-plugin@<marketplace-name> --scope project
```

- `<marketplace-name>` is the `"name"` field in `marketplace.json`.
- `--scope project` writes it to `enabledPlugins` in `.claude/settings.json`, so teammates get it too.
- Or inside a session: `/plugin install sync-plugin@<marketplace-name>`

## 6. Check it

```
claude
  /plugin list        # what's installed and enabled
  /reload-plugins     # pick up changes without restarting
  /mcp                # plugin MCP servers show as plugin:sync-plugin:<server>
```

After changing MCP config, restart Claude Code to be safe — `/reload-plugins` may not reconnect servers that are already running.

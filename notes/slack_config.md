# Slack Notifications for Claude Code (Incoming Webhook)

Get a Slack message whenever Claude Code is waiting on you, either for a permission prompt or because it has gone idle waiting for input.

**What you'll set up:**

1. A Slack app with an Incoming Webhook that posts to a channel
2. The webhook URL, stored privately in your Claude Code settings
3. A `Notification` hook that calls `slack-notify.js`
4. A test to confirm it works

---

## Step 1: Create a Slack app

1. Go to <https://api.slack.com/apps>.
2. Click **Create New App** → **From scratch**.
3. Name it `Claude Code`, select your workspace, and click **Create App**.

## Step 2: Enable Incoming Webhooks

1. In the left sidebar, open **Incoming Webhooks** and toggle it **On**.
2. Click **Add New Webhook to Workspace**.
3. Pick a channel (for example `#general`, or create `#claude-notifications`) and click **Allow**.
4. Copy the **Webhook URL**. It looks like this:

   ```text
   https://hooks.slack.com/services/T.../B.../XXXX
   ```

> [!WARNING]
> The webhook URL is a secret. Anyone who has it can post to your channel. Don't paste it into chat, tickets, or any file that gets committed.

## Step 3: Store the URL in a personal settings file

Don't put the URL in `.claude/settings.json`, because that file is committed to Git. Use one of these instead:

| File | Scope |
|---|---|
| `.claude/settings.local.json` | This project only, not meant to be committed |
| `~/.claude/settings.json` | Your user settings, applies to all projects |

Add an `env` block:

```json
{
  "env": {
    "SLACK_WEBHOOK_URL": "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
  }
}
```

If the file already has content (such as `permissions`), add `env` as another top-level key. Don't replace the file.

**Check that the local file really is ignored by Git** before you save the URL in it:

```bash
git check-ignore -v .claude/settings.local.json
```

- It prints a matching `.gitignore` rule: you're safe.
- It prints nothing: the file is **not** ignored. Add `.claude/settings.local.json` to `.gitignore`. If the file is already tracked, also run `git rm --cached .claude/settings.local.json` before you save the URL in it.

## Step 4: Configure the hook

The hook configuration contains no secrets, so it can go in the shared `.claude/settings.json`. Add a `Notification` entry inside the existing `"hooks"` block:

```json
"Notification": [
  {
    "matcher": "permission_prompt|idle_prompt",
    "hooks": [
      {
        "type": "command",
        "command": "node \"$CLAUDE_PROJECT_DIR/.claude/hooks/slack-notify.js\""
      }
    ]
  }
]
```

- `matcher` limits the hook to two events: permission prompts and idle prompts.
- `$CLAUDE_PROJECT_DIR` makes the path work from any working directory. Keep the quotes, because the path may contain spaces.
- `slack-notify.js` must exist at `.claude/hooks/slack-notify.js`. It reads `SLACK_WEBHOOK_URL` and exits quietly when the variable isn't set, so teammates without a webhook aren't affected.

Restart Claude Code after you edit the settings, because hooks are loaded at startup.

## Step 5: Test it

1. Start `claude` in the project.
2. Ask for something that triggers a permission prompt, such as a command covered by an `ask` rule like `npm install`.
3. A message should arrive in your Slack channel.

### Nothing arrived?

First check the webhook itself, outside Claude Code.

**Bash / Git Bash:**

```bash
curl -X POST -H "Content-Type: application/json" -d '{"text":"test"}' "<your webhook URL>"
```

**PowerShell:**

```powershell
Invoke-RestMethod -Method Post -ContentType 'application/json' -Body '{"text":"test"}' -Uri '<your webhook URL>'
```

A reply of `ok` means the webhook works, so the problem is on the Claude Code side:

| Symptom | Check |
|---|---|
| Webhook test fails | Copy the URL again from Slack. It may have been revoked or mistyped. |
| Webhook works, no hook message | Run `/hooks` in Claude Code to confirm the `Notification` hook is loaded, and restart the session after settings changes. |
| Hook loaded, still nothing | Confirm `SLACK_WEBHOOK_URL` is in the `env` block of a settings file Claude Code reads, and that `slack-notify.js` exists at the configured path. |
| Messages for the wrong events | Adjust the `matcher` value. |

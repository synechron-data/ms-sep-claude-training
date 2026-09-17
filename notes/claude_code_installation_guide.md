# Installing Claude Code

*Companion guide for participants — get set up before our next session*

> **Source:** Official setup, authentication, and troubleshooting docs ([code.claude.com/docs/en/setup](https://code.claude.com/docs/en/setup)).
> **Last verified:** September 2026

## What you'll have at the end

- Claude Code installed and signed in
- A working `claude` command in your terminal
- A first session running in a real project
- Fixes for the most common setup errors

Expect 10–15 minutes if everything goes smoothly, 30 minutes if you hit a snag.

## Before you start

| Requirement | Details |
|---|---|
| Operating system | macOS 13.0+, Windows 10 1809+ or Windows Server 2019+, Ubuntu 20.04+, Debian 10+, Alpine Linux 3.19+ |
| Hardware | 4 GB+ RAM, x64 or ARM64 processor |
| Shell | Bash, Zsh, PowerShell, or CMD |
| Internet | Required |
| Account | Claude Pro, Max, Team, or Enterprise, or an Anthropic Console (API) account — or your organization's Bedrock / Google Cloud / Microsoft Foundry setup |

You don't need a GPU. **The free Claude.ai plan does not include Claude Code.**

## Pick an installation method

Choose one — don't install more than one.

| Method | Auto-updates? | Notes |
|---|---|---|
| **Native installer (recommended)** | Yes, in the background | No Node.js needed |
| Homebrew (macOS) | No (opt-in available) | `brew upgrade claude-code` to update |
| WinGet (Windows) | No (opt-in available) | `winget upgrade Anthropic.ClaudeCode` to update |
| apt / dnf / apk (Linux) | No | Signed package repositories; see the setup docs |
| npm | Yes, if the npm global directory is writable | Requires **Node.js 22+**; never use `sudo` |

## macOS / Linux / WSL

**Native installer (recommended):**
```bash
curl -fsSL https://claude.ai/install.sh | bash
```
Close and reopen your terminal afterwards so the new `PATH` is picked up.

**Homebrew (macOS):**
```bash
brew install --cask claude-code          # stable channel
brew install --cask claude-code@latest   # latest channel
```

**npm (any OS):**
```bash
npm install -g @anthropic-ai/claude-code   # Node.js 22 or later, no sudo
```

## Windows

Claude Code runs **natively on Windows** or inside **WSL**. Pick based on where your projects live:

| Option | Needs | Sandboxed command execution | Best for |
|---|---|---|---|
| Native Windows | Nothing extra; Git for Windows optional | Not supported | Windows-native projects and tools |
| WSL 2 | WSL 2 enabled | Supported | Linux toolchains |

### Native Windows

Run one of these — no Administrator rights needed:

```powershell
# PowerShell
irm https://claude.ai/install.ps1 | iex
```
```bat
:: CMD
curl -fsSL https://claude.ai/install.cmd -o install.cmd && install.cmd && del install.cmd
```
```powershell
# WinGet
winget install Anthropic.ClaudeCode
```

**Git for Windows is optional but recommended.** With it, Claude Code uses Git Bash for its Bash tool; without it, Claude runs shell commands through its PowerShell tool.

### WSL

Open your WSL distribution and run the Linux installer above **inside the WSL terminal**. Launch `claude` from WSL, not from PowerShell or CMD. Keep projects in the WSL filesystem (`~/projects/...`) rather than `/mnt/c/...` for speed.

## First-time sign-in

```bash
claude
```

On first launch Claude Code opens your browser to sign in. Pick your Claude subscription or Console account and approve.

- Signed in already and need to switch? Run `/login` inside a session, or `claude auth login` from the shell. `claude auth status` shows who you're signed in as.
- **Headless / CI:** you can set `ANTHROPIC_API_KEY`, but that bills the **API (Console) account at API rates** — it doesn't use your subscription. For CI with a subscription, generate a long-lived token with `claude setup-token` and set it as `CLAUDE_CODE_OAUTH_TOKEN`.

### Behind a corporate proxy?

Point Claude Code at the proxy instead of switching credentials:

```bash
export HTTPS_PROXY=https://proxy.example.com:8080
```

If your proxy inspects TLS, Claude Code trusts your operating system's certificate store by default, so a corporate root certificate installed on the machine usually just works. See the network configuration docs if it doesn't.

## Verify it's working

```bash
claude --version     # prints a version number
claude doctor        # read-only diagnostics: install health, settings errors, and more
```

Inside a session, `/doctor` runs a fuller checkup that can also fix problems.

## Your first session

```bash
cd ~/projects/your-app
claude
```

Inside the session:

```
/init
```

Claude scans the project and creates a starter `CLAUDE.md`. Review it, fix anything wrong, and commit it so your team benefits too.

## Common errors and fixes

| Error | Why it happens | Fix |
|---|---|---|
| `EACCES: permission denied` (npm) | npm writing to system directories | Use the native installer, or set a user-owned npm prefix (below). Never use `sudo` |
| `EBADENGINE` (npm) | Node.js older than 22 | Upgrade Node.js, or use the native installer |
| `command not found: claude` | Install location isn't on `PATH` yet | Close and reopen the terminal; then run `claude doctor` |
| `exec: node: not found` (WSL, npm install) | WSL is using the Windows Node.js | Install Node.js inside WSL (`which node` should not start with `/mnt/c/`), or use the native installer |
| npm reports a platform mismatch (WSL) | WSL picked up the Windows `npm` | `npm config set os linux`, then reinstall — or use the native installer |
| Browser sign-in fails on a corporate network | Proxy or firewall | Set `HTTPS_PROXY` (above) |
| Slow file operations in WSL | Project on a mounted Windows drive | Move it to the WSL filesystem |

### One-time npm permission fix

```bash
mkdir ~/.npm-global
npm config set prefix '~/.npm-global'
echo 'export PATH=~/.npm-global/bin:$PATH' >> ~/.zshrc   # or ~/.bashrc
source ~/.zshrc
```

## Cheat sheet

| Command | What it does |
|---|---|
| `curl -fsSL https://claude.ai/install.sh \| bash` | Native install — macOS, Linux, WSL |
| `irm https://claude.ai/install.ps1 \| iex` | Native install — Windows PowerShell |
| `winget install Anthropic.ClaudeCode` | WinGet install — Windows |
| `brew install --cask claude-code` | Homebrew install — macOS |
| `npm install -g @anthropic-ai/claude-code` | npm install (Node.js 22+) |
| `claude --version` | Print installed version |
| `claude doctor` | Setup diagnostics |
| `claude update` | Update now |
| `claude` | Start a session in the current directory |
| `/init` | Generate a starter `CLAUDE.md` |
| `/login` or `claude auth login` | Sign in / switch account |

## What to do before our next session

1. Install Claude Code with one method above
2. Run `claude --version` — confirm it prints a number
3. Run `claude doctor` — fix anything it flags
4. Run `claude` once to finish signing in
5. *(Optional)* In a real project, run `/init` to create your first `CLAUDE.md`

## Further reading

- [Set up Claude Code](https://code.claude.com/docs/en/setup)
- [Troubleshoot installation](https://code.claude.com/docs/en/troubleshoot-install)
- [Authentication](https://code.claude.com/docs/en/authentication)
- [Network configuration](https://code.claude.com/docs/en/network-config)

*Prepared for training programs — Technizer India*

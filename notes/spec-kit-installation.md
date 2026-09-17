# Installing Spec Kit (`specify` CLI)

*Setup guide for GitHub Spec Kit with Claude Code*

> **Sources:** [Spec Kit installation guide](https://github.github.com/spec-kit/installation.html), [Spec Kit README](https://github.com/github/spec-kit), [uv documentation](https://docs.astral.sh/uv/)
> **Spec Kit release used in this guide:** `v1.0.7`. Check [the releases page](https://github.com/github/spec-kit/releases) and use the latest tag.

## What you'll have at the end

- `uv` installed
- The `specify` CLI installed for your user account
- Spec Kit added to a project, with its `/speckit-*` skills available in Claude Code

## Prerequisites

Spec Kit runs on **Windows, macOS, and Linux**. Windows doesn't need WSL.

| Prerequisite | Why it's needed | Check |
|---|---|---|
| **uv** (Python package and tool manager) | Installs and runs the `specify` CLI | `uv --version` |
| **Python 3.11+** | The `specify` CLI is written in Python | `uv python list --only-installed` or `python --version` |
| **Git** | Needed to install from a `git+https://` URL, and for the optional git extension | `git --version` |
| **Claude Code** | The `claude` integration needs the Claude Code CLI installed | `claude --version` |

---

## Step 1 — Install uv

Pick one method. The standalone installer is recommended.

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
```
Or: `winget install --id=astral-sh.uv -e`

**macOS / Linux / WSL:**
```bash
curl -LsSf https://astral.sh/uv/install.sh | sh
```
Or on macOS: `brew install uv`

The standalone installer puts `uv` and `uvx` in your user's bin folder (`%USERPROFILE%\.local\bin` on Windows, `~/.local/bin` on macOS/Linux) and adds it to your PATH. No admin rights or `sudo` needed.

## Step 2 — Open a new terminal and verify

```bash
uv --version
```

If `uv` isn't recognized, the terminal was opened before your PATH changed. Close it and open a new one. In VS Code or IntelliJ, restart the IDE, because its built-in terminal keeps the old PATH.

## Step 3 — Make sure Python 3.11+ is available

If you don't have Python 3.11 or newer, let uv install one (no admin rights needed):

```bash
uv python install 3.12
```

uv also downloads a suitable Python on its own when it needs one. Installing it up front helps on machines with slow or restricted downloads.

## Step 4 — Install Git if missing

- **Windows:** `winget install --id Git.Git -e`, or the installer from git-scm.com
- **macOS:** `xcode-select --install` or `brew install git`
- **Linux:** your package manager, e.g. `sudo apt install git`

---

## Step 5 — Install the `specify` CLI

The same command works in PowerShell, CMD, and bash. Pin a release tag so everyone on the team gets the same version:

```bash
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git@v1.0.7
```

Verify:

```bash
specify version      # CLI version, Python version, platform
specify check        # confirms Claude Code (and other agent CLIs) can be found
```

If `specify` isn't found, run `uv tool update-shell`, then open a new terminal.

### Other ways to install

| Method | Command | Notes |
|---|---|---|
| From PyPI | `uv tool install specify-cli` | Doesn't need Git |
| pipx | `pipx install specify-cli` | If you already use pipx |
| One-off run, no install | `uvx --from git+https://github.com/github/spec-kit.git@v1.0.7 specify <command>` | Slower; fine for a single command |

### Where it installs — user level

`uv tool install` installs at **user level**: not system-wide, and not inside any project. uv creates a separate virtual environment just for `specify-cli` in your user profile and puts the `specify` command in your user's bin folder.

| | Windows | macOS / Linux |
|---|---|---|
| Tool environment (package and dependencies) | `%APPDATA%\uv\data\tools\specify-cli\` | `~/.local/share/uv/tools/specify-cli/` |
| `specify` command | `%USERPROFILE%\.local\bin\specify.exe` | `~/.local/bin/specify` |

Check the exact paths on your machine:

```bash
uv tool dir          # where tool environments live
uv tool dir --bin    # where the specify command is placed
uv tool list         # installed tools and versions
```

What this means:
- **Available in every project** for your user account, from any folder
- **Per user:** other accounts on the same machine need their own install
- **Isolated** from your other Python packages and project virtual environments
- **No admin rights needed.** Move the folders with the `UV_TOOL_DIR` and `UV_TOOL_BIN_DIR` environment variables if you need to

---

## Step 6 — Add Spec Kit to a project

This is the **project-level** part. Run it from the repository root.

### Existing project

Commit or stash your work first, so the generated files show up as a clean diff:

```bash
cd <your-repo>
git status                                         # should be clean
specify init --here --force --integration claude
```

### New project

```bash
specify init my-project --integration claude
cd my-project
```

### Options

| Option | What it does |
|---|---|
| `--integration claude` | Installs Claude Code skills. Other agents use their own key (e.g. `copilot`, `gemini`) |
| `--here` | Initialize in the current directory instead of creating a new one |
| `--force` | Allow a non-empty directory. Doesn't delete your code, but can overwrite files at paths Spec Kit manages, so start from a clean commit |
| `--script ps\|sh\|py` | Script flavour for the helper scripts. Defaults to PowerShell (`ps`) on Windows and `sh` elsewhere |
| `--ignore-agent-tools` | Skip the check for the Claude Code CLI |

### What gets created

```
.specify/                       Spec Kit templates, scripts, and memory (commit this)
├── memory/constitution.md      Project principles, filled in by /speckit-constitution
├── templates/                  Spec, plan, and tasks templates
└── scripts/                    Helper scripts (.ps1 on Windows, .sh elsewhere)
.claude/skills/speckit-*/       One Claude Code skill per Spec Kit step
```

Commit both folders. Teammates who clone the repo get the `/speckit-*` skills in Claude Code without installing the `specify` CLI themselves; only people who run `specify` commands need the CLI.

### Optional: Git feature branches

Spec Kit tracks the active feature in `.specify/feature.json` and doesn't create Git branches by default. For a numbered branch per feature (e.g. `001-task-priority`):

```bash
specify extension add git
```

---

## Step 7 — Confirm it works in Claude Code

```bash
claude
```

Type `/speckit` and check the skills appear:

| Skill | Purpose |
|---|---|
| `/speckit-constitution` | Set project principles (once per project) |
| `/speckit-specify` | Describe what to build and why |
| `/speckit-clarify` | Resolve ambiguities before planning *(optional)* |
| `/speckit-plan` | Choose the tech stack and design |
| `/speckit-checklist` | Requirements-quality checklist *(optional)* |
| `/speckit-tasks` | Break the plan into ordered tasks |
| `/speckit-analyze` | Check spec, plan, and tasks for consistency *(optional)* |
| `/speckit-implement` | Build it |
| `/speckit-converge` | Check the code against the spec; add tasks for any gaps |

These are Claude Code skills. Run them in Claude's chat, not in the terminal.

---

## Upgrade and uninstall

```bash
specify self upgrade                                  # upgrade the CLI in place
specify integration upgrade claude                    # refresh the project's Claude Code skills
specify extension update                              # update installed extensions
specify init --here --force --integration claude      # fallback refresh of project files
uv tool uninstall specify-cli                         # remove the CLI
```

The upgrade path keeps `.specify/memory/constitution.md`. If you've edited Spec Kit's templates or scripts, the upgrade stops and asks before overwriting them.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `uv` not recognized | Open a new terminal (restart the IDE if using its terminal) |
| `specify` not recognized | `uv tool update-shell`, then open a new terminal |
| Certificate or download errors on a corporate network | Try `uv --native-tls tool install …` to use the OS certificate store. If downloads are blocked, see Spec Kit's [air-gapped install guide](https://github.github.com/spec-kit/install/air-gapped.html) |
| `specify check` can't find Claude Code | Install Claude Code, or pass `--ignore-agent-tools` to `specify init` |
| `/speckit-*` skills don't appear in Claude Code | Check `.claude/skills/speckit-*` exists; if not, re-run `specify init --here --force --integration claude` from the repo root and restart Claude Code |
| Warning about `--ai` | Use `--integration claude` instead |

## Quick reference

```bash
# uv
powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"   # Windows
curl -LsSf https://astral.sh/uv/install.sh | sh                                      # macOS / Linux

# specify CLI (user level)
uv tool install specify-cli --from git+https://github.com/github/spec-kit.git@v1.0.7
specify version
specify check

# project
specify init --here --force --integration claude
specify extension add git            # optional feature branches

# maintenance
specify self upgrade
uv tool uninstall specify-cli
```

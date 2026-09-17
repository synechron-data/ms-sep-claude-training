# Spec Kit — Workflow Steps Explained

*What each Spec Kit step does, what it produces, and what to check before moving on*

> **Sources:** [Spec Kit quickstart](https://github.github.com/spec-kit/quickstart.html), [Agentic SDD reference](https://github.com/github/spec-kit/blob/main/docs/reference/agentic-sdd.md), [Adopting Spec Kit in an existing project](https://github.github.com/spec-kit/guides/existing-projects.html)
> **Setup:** see `spec-kit-installation.md`.

## The Idea: Spec-Driven Development

In prompt-first development you describe a change and Claude writes code straight away. That works for small changes. For a real feature, scope drifts, edge cases get missed, and gaps show up in code review.

**Spec-Driven Development (SDD)** reverses the order:

1. Agree on **what** to build and **why** (the spec)
2. Decide **how** to build it (the plan)
3. Break the plan into **ordered tasks**
4. **Implement** against those tasks
5. **Check** the code against the spec

Each step writes a Markdown file that the next step reads. The files live in your repository, so the spec, plan and tasks can be reviewed and versioned like code.

## How You Run the Steps

- In Claude Code, each step is a **skill** you run in Claude's chat: `/speckit-specify`, `/speckit-plan`, and so on. They are not terminal commands.
- Type your input after the command on the same line. Some steps need input (constitution, specify, plan); others work with none (tasks, analyze, implement, converge).
- **Run one step at a time and review its output before the next one.** The value of Spec Kit comes from catching problems between steps.
- Spec Kit's own docs write the steps as `/speckit.specify` (dotted form). In Claude Code, use the hyphenated form, `/speckit-specify`.

## Two Paths

**Shorter path** — smaller features:

```
/speckit-specify → /speckit-plan → /speckit-tasks → /speckit-implement → /speckit-converge
```

**Full path** — production features, adding three quality gates:

```
/speckit-constitution (once per project)
  → /speckit-specify
  → /speckit-clarify        (quality gate)
  → /speckit-plan
  → /speckit-checklist      (quality gate)
  → /speckit-tasks
  → /speckit-analyze        (quality gate)
  → /speckit-implement
  → /speckit-converge
```

Set the constitution once before either path. Only `/speckit-specify` is strictly required before `/speckit-plan`; clarify, checklist and analyze are optional gates you add when a feature has real ambiguity or risk.

---

## Step 1 — `/speckit-constitution`

**Purpose:** Set the project's guiding principles. Every later step, especially plan and analyze, checks its output against them.

**When:** Once per project, before the first feature. Run it again whenever the team's principles change.

**Input:** The principles as plain text: architecture rules, testing standards, security requirements, compatibility promises, conventions.

**Output:** `.specify/memory/constitution.md`. The command also keeps dependent templates in sync with it.

**What to review:**
- Every rule is **already true** in the codebase or **explicitly agreed** by the team.
- Rules are specific enough to check ("every endpoint enforces resource ownership"), not vague ("write good code").

**Tips:**
- In an existing repository, base it on evidence: the README, architecture decisions, contribution guide and CI configuration.
- **Don't invent standards to fill the template.** Unrealistic rules create noise in every later check.
- **Constitution vs `CLAUDE.md`:** `CLAUDE.md` is guidance Claude reads every session. The constitution is the set of principles Spec Kit's planning and analysis steps *check against*. They can overlap, but they do different jobs.

---

## Step 2 — `/speckit-specify`

**Purpose:** Turn a natural-language description into a structured feature specification.

**When:** At the start of every new feature.

**Input:** What you want and why: user-facing behaviour, goals, constraints that matter to users (e.g. "existing clients must keep working"). **Leave out the tech stack.** Frameworks, databases and libraries belong in the plan.

**Output:**
- A new feature directory, by default `specs/<NNN>-<short-name>/` (e.g. `specs/003-user-auth/`)
- `spec.md` — user stories, functional requirements, acceptance criteria
- `checklists/requirements.md` — a built-in spec-quality checklist
- `.specify/feature.json` — records this as the **active feature**, which later steps work on

**What to review:**
- User stories match what users actually need.
- Acceptance criteria are testable.
- Nothing about implementation has crept in.
- Any `[NEEDS CLARIFICATION: …]` markers (at most three) point at real open questions. Resolve them with `/speckit-clarify` or by re-running `/speckit-specify`.

**Tips:**
- Describe **compatibility boundaries** as well as new behaviour: what must *not* change.
- In an existing codebase, the spec describes **the change you intend**, not everything the system already does. Pick a bounded first feature rather than "document the whole system".

---

## Step 3 — `/speckit-clarify` *(optional quality gate)*

**Purpose:** Find underspecified areas of the spec and resolve them before any design work.

**When:** After specify, before plan, for any feature with meaningful ambiguity. You can run it several times, each time on a different area.

**Input:** Nothing, or a focus area (e.g. error handling, permissions, default behaviour).

**Output:** Up to **five targeted questions**. Your answers are written back into `spec.md`, and the built-in `checklists/requirements.md` may be re-evaluated.

**What to review:** The answers now in `spec.md` read as clear requirements, not as a transcript of Q&A.

**Why it matters:** Planning on top of ambiguity bakes guesses into the design. Answering a question here costs a minute; discovering the gap in code review costs a rewrite. If `/speckit-analyze` later finds requirement gaps, come back to clarify (or specify).

---

## Step 4 — `/speckit-plan`

**Purpose:** Produce the technical design for the spec. **This is where the tech stack goes.**

**When:** After the spec (and clarify, if used) is settled.

**Input:** Your tech stack, architecture and technical constraints. In an existing project, tell it to reuse the current architecture, dependencies and test conventions.

**Output** (in the feature directory):

| File | Contains |
|---|---|
| `plan.md` | The implementation plan and approach |
| `research.md` | Technical research and decisions, with alternatives considered |
| `data-model.md` | Entities, fields, relationships, validation rules |
| `contracts/` | Interface contracts, e.g. API endpoints and payloads |
| `quickstart.md` | How to exercise and validate the feature |

**What to review:**
- The plan references your **real** code (existing entities, services, components) rather than inventing a parallel structure.
- It respects the constitution. The plan step checks against it and flags conflicts.
- Contracts keep any promised backward compatibility.

---

## Step 5 — `/speckit-checklist` *(optional quality gate)*

**Purpose:** Generate a custom checklist that tests **the requirements themselves**, not the code. Think of it as "unit tests for your requirements".

**When:** After plan, before tasks. Run it with no input for a broad pass, or with a focus area.

**Output:** A checklist file in the feature's `checklists/` directory, with items such as "Is behaviour defined for every error case?"

**How it's used:**
- Custom checklists are **reviewer-owned**. A person marks each item `[x]` only after deciding that requirement-quality criterion is met.
- A checked item means "the requirement is good enough", **not** "the work is done".
- If items expose gaps, go back to clarify or specify, then re-check.

**Why it matters:** `/speckit-implement` reads checklist checkboxes as a gate and asks before proceeding when any are unchecked.

---

## Step 6 — `/speckit-tasks`

**Purpose:** Break the design into an actionable, dependency-ordered task list.

**When:** After plan (and checklist, if used).

**Input:** None needed.

**Output:** `tasks.md`, organized into phases:

| Phase | Contains |
|---|---|
| **Setup** | Project scaffolding and configuration |
| **Foundational** | Blocking prerequisites that user stories depend on |
| **One phase per user story** | In priority order; each story's work, including its tests when tests are requested |
| **Polish** | Cross-cutting concerns |

Tasks that can safely run in parallel are marked.

**What to review:**
- Every requirement in `spec.md` has tasks, and every task traces to a requirement.
- Ordering makes sense: nothing depends on work that comes later.
- Tasks are small enough to review individually.

---

## Step 7 — `/speckit-analyze` *(optional quality gate, read-only)*

**Purpose:** A cross-artifact consistency check across `spec.md`, `plan.md` and `tasks.md`.

**When:** After tasks, before implement. You can also run it again after implementation as an extra review.

**Output:** A report of conflicts, gaps and ambiguities, for example a task with no matching requirement, or a plan decision that contradicts the spec. **It never edits files**; it can suggest remediations for you to approve.

**How to act on findings — fix at the source:**

| Problem type | Go back to |
|---|---|
| Missing or unclear requirement | `/speckit-specify` or `/speckit-clarify` |
| Design problem | `/speckit-plan` |
| Task list problem | `/speckit-tasks` (regenerate) |

Then run analyze again until it comes back clean. Fixing artifacts now is far cheaper than fixing code later.

---

## Step 8 — `/speckit-implement`

**Purpose:** Execute the tasks in `tasks.md` and build the feature.

**When:** Once the artifacts are consistent.

**Input:** None to build everything, or a scope, such as a single phase.

**How it works:**
- Runs tasks **in dependency order**, phase by phase, respecting parallel markers.
- **Checklist gate:** before starting, it counts checked and unchecked checklist items and **asks before proceeding** if any are unchecked. It never changes checklist markers itself.

**What to review:** Code **and** artifact changes together, as one change.

**Tips:**
- **Small feature:** run it once.
- **Large feature:** scope each run to a phase (e.g. Setup and Foundational first), validate, then continue. This stops the agent's context from being overwhelmed and keeps each change reviewable.

---

## Step 9 — `/speckit-converge`

**Purpose:** Check that the implementation actually matches the spec, plan and tasks.

**When:** After implement.

**Output:**
- If it finds gaps, it **appends new tasks** to `tasks.md`.
- If everything is covered, it reports **Converged**.

**The loop:**

```
/speckit-implement → /speckit-converge → (new tasks?) → /speckit-implement → /speckit-converge … → Converged
```

Once converged, move on to code review and open a pull request.

---

## Artifact Map

```
.specify/
├── memory/constitution.md        ← Step 1
└── feature.json                  ← active feature (set by Step 2; machine-local, gitignored)

specs/<NNN>-<feature-name>/
├── spec.md                       ← Step 2, refined by Step 3
├── checklists/
│   ├── requirements.md           ← Step 2 (built-in), re-evaluated by Step 3
│   └── <custom>.md               ← Step 5 (reviewer-owned)
├── plan.md                       ← Step 4
├── research.md                   ← Step 4
├── data-model.md                 ← Step 4
├── contracts/                    ← Step 4
├── quickstart.md                 ← Step 4
└── tasks.md                      ← Step 6, extended by Step 9
```

Step 7 (analyze) writes nothing. Step 8 (implement) changes your code and ticks off tasks.

## How Spec Kit Knows Which Feature You're On

- The **active feature** is the directory recorded in `.specify/feature.json`, written by `/speckit-specify`.
- It does **not** follow your Git branch. Checking out a different branch doesn't change the active feature.
- To work on a different feature, update `.specify/feature.json` or set the `SPECIFY_FEATURE_DIRECTORY` environment variable.
- Git branches per feature are optional (`specify extension add git`).

## Keeping Specs Useful After the Feature Ships

Agree as a team how completed feature directories will age:

| Model | How it works |
|---|---|
| **Immutable record** | Each feature directory is a historical record of that change; new changes get new specs |
| **Living contract** | `spec.md` is kept current, and downstream artifacts are regenerated when it changes |
| **Two-way reconciliation** | Discoveries from code, tasks or plans flow back, and the whole artifact set is reconciled |

## When to Use Spec Kit

| Use Spec Kit for | Use a direct prompt for |
|---|---|
| Multi-file features | Bug fixes |
| Changes a reviewer would want a design doc for | Small, well-understood additions |
| Work with real ambiguity or compatibility risk | Changes you can review at a glance |
| Features several people will build or review | One-off experiments |

## Common Mistakes

| Mistake | Instead |
|---|---|
| Putting the tech stack in `/speckit-specify` | Describe what and why; save the stack for `/speckit-plan` |
| Running all steps back to back without reading the output | Review each artifact before the next step |
| Writing aspirational rules into the constitution | Only rules that are already true or agreed |
| Editing code to fix an `/speckit-analyze` finding | Fix the artifact that owns the problem, then re-run analyze |
| Ticking checklist items to get past the implement gate | Tick only after the requirement has been reviewed |
| Running `/speckit-implement` once on a large feature | Scope it phase by phase |
| Stopping after implement | Run `/speckit-converge` until it reports Converged |
| Making the first feature "document the entire existing system" | Start with a bounded, reviewable change |

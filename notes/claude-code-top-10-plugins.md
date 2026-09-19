# Top 10 Most-Downloaded Claude Code Plugins (2026)

> **Note on data:** Install counts come from different snapshots (March–July 2026) and different sources (Anthropic's official directory, GitHub stars, third-party trackers), so figures don't always agree across sources. For a live, daily-updated ranking, see [pluginmarketplace.ai/best-claude-plugins](https://pluginmarketplace.ai/best-claude-plugins).

## 1. Frontend Design
**Type:** Anthropic official
The most-installed plugin in the official directory. Wires Claude into design tokens, screenshots, and layout reasoning so UI output looks intentional rather than generic.
- ~277,000 installs (mid-2026 snapshot) / ~829,000 installs (June 1, 2026 snapshot, source-dependent)

## 2. Superpowers
**Type:** Community (obra/superpowers)
A full plan → spec → test development workflow — effectively a methodology packaged as a plugin.
- ~248k GitHub stars; ~752,000 installs (June 1, 2026)
- Install: `/plugin marketplace add obra/superpowers-marketplace` then `/plugin install superpowers@superpowers-marketplace`

## 3. Context7
**Type:** Community/MCP-backed
Injects live, version-specific library documentation into context instead of relying on stale training data. Popular for fast-moving stacks (Next.js, Tailwind, LangChain, Vercel AI SDK).
- ~348,660 installs (June 1, 2026)

## 4. GitHub
**Type:** Anthropic partner plugin
Repository context, PR workflows, and commit history directly in Claude Code.

## 5. Playwright
**Type:** Anthropic partner plugin
Browser automation and E2E test workflows.

## 6. LSP Language Server Pack
**Type:** Anthropic official
12 language servers (TypeScript, Python, Rust, Go, Java, and more) for IDE-grade code intelligence.

## 7. Claude Mem
**Type:** Community
Persistent memory across sessions — frequently cited among the most popular community plugins.

## 8. Caveman
**Type:** Community
Token-saving terse output mode; measurable reduction in token spend on real work.

## 9. PR Review Toolkit
**Type:** Anthropic official
Automated PR review with confidence scoring across tests, types, regressions, and code quality.

## 10. Chrome DevTools MCP
**Type:** Community/MCP-backed
Real-browser debugging; frequently paired with Frontend Design to verify rendered output.

---

### Quick install reference
```
/plugin marketplace add anthropics/claude-plugins-official
/plugin install <plugin-name>@claude-plugins-official
```
For community plugins, add the specific marketplace first (see each plugin's own install command above).

### Caution
Anthropic's own docs warn that plugins and marketplaces can execute code with your user privileges — treat them like dependencies, especially any that include hooks, shell commands, browser automation, or authenticated MCP servers.

---
name: standup
description: Generate a daily standup update from git history and open files
disable-model-invocation: true
---

You are a senior developer preparing a daily standup update.

Check git log --since="00:00" --oneline and git diff HEAD to understand
what actually changed today. Do not invent work that isn't visible.

Format:
Yesterday: [completed items — specific class/component names]
Today:     [in-progress items based on uncommitted changes or TODOs]
Blockers:  [failing tests, TODO/FIXME comments, incomplete work]

3 bullets max per section. Be factual and brief.
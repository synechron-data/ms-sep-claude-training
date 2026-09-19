---
name: fix-issue
description: Fix a tracked issue with minimal, surgical changes
disable-model-invocation: true
---

You are a careful, experienced backend developer who makes minimal, surgical
changes.

This is a Spring Boot REST API. Controllers stay thin — validation happens
via Bean Validation annotations, business logic lives in @Service classes,
and GlobalExceptionHandler maps exceptions to HTTP responses. Never leak
internal error details to clients.

Look up issue #$ARGUMENTS via the GitHub (or Jira) MCP tool and fix it.

Steps:
1. Understand exactly what the issue describes
2. Identify the minimum set of files that need to change
3. Make only the changes necessary — nothing more
4. Write or update JUnit tests to cover the fix
5. Summarise what changed, which files were affected, and how to verify

Always follow CLAUDE.md. Do not modify application.properties secrets or
push any changes. Be conservative.
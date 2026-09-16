---
name: deploy
description: Deploy the backend and frontend to the staging environment
disable-model-invocation: true
allowed-tools: Bash(mvn:*), Bash(npm:*), Bash(git:*)
---

# Deployment Skill

Deploy steps in exact order:
1. Backend: run `mvn test` — abort if any test fails
2. Backend: run `mvn clean package`
3. Frontend: run `npm test -- --watch=false` — abort if any test fails
4. Frontend: run `npm run build`
5. Tag the release with today's date, push the tag

Never deploy if:
- Any backend or frontend test is failing
- There are uncommitted changes
- The current branch is not staging or main
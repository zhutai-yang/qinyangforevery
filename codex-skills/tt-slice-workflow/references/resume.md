# Slice Resume

Use for `/slice-resume`.

Purpose: recover slice development context after interruption.

Steps:

1. Locate the active slice from branch name, recent changed docs, or user message.
2. Read `implementation-baseline.md`, `acceptance-baseline.md`, and `execution-plan.md`.
3. Inspect commits with `git log --oneline --decorate -n 20`.
4. Check working tree with `git status --short`.
5. Run lightweight verification:

```bash
mvn compile -pl tt-admin/tt-admin-api -am
npm run lint
```

Only run frontend lint if frontend files changed.

Output either a session report or `slice-resume-report.md`:

- Active slice and branch.
- Completed tasks.
- In-progress task.
- Pending tasks.
- Dirty files.
- Compile/lint result.
- Recommended next action.

PASS when context is clear and build/lint is healthy. FAIL when local fixes are required. BLOCKED when the slice cannot be identified.

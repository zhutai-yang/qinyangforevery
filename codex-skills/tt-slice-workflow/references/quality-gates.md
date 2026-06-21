# Quality Gates

Use for `/scope-check`, `/hygiene`, and `/resolve`.

## Shared Base Branch Helper

Resolve base before diff-based checks:

```bash
git rev-parse --verify develop
git rev-parse --verify main
```

Use `develop` if present; otherwise `main`. Then inspect:

```bash
git diff --name-only <base>...HEAD
git diff <base>...HEAD
```

## /scope-check

Purpose: classify changed files as owned, shared, or foreign and produce `scope-check-report.md`.

Derive owned paths from baseline section for owned globs. Default TT module map:

| Module | Path pattern |
|--------|--------------|
| API | `tt-admin/tt-admin-api/**/controller/*` |
| Application | `tt-shared/tt-application/**/service/**` |
| Infrastructure | `tt-shared/tt-infrastructure/**` |
| Admin frontend | `client/admin/views/**` |
| Portal frontend | `client/portal/views/**` |
| Tests | `**/*Test.java` |

Shared append-only surfaces:

| File | Rule |
|------|------|
| `schema.sql` | Table changes require matching migration; do not delete unrelated tables |
| `data.sql` | Append slice seed data only |
| `SecurityConfig.java` | Append permit/authorize rules only |
| `client/*/main.js` | Append routes only |
| `pom.xml` | Append dependencies only |

PASS when `foreign=0` and shared changes comply. FAIL when foreign files or unsafe shared edits exist.

## /hygiene

Purpose: audit changed files for code hygiene and produce `hygiene-report.md`.

Check only files changed against the detected base branch.

Five dimensions:

| Dimension | Check |
|-----------|-------|
| Dead code | Unreferenced methods, large commented-out blocks |
| Naming residue | Legacy names like `backend/`, `TableTennis`, `schema-mysql` |
| TODO/FIXME | Newly added high-priority FIXME or vague TODO |
| Imports/deps | Unused imports; frontend debug `console.log` |
| Config leaks | Hardcoded passwords, JWT secrets, credentials |

Severity:

- Red: FIXME, secrets, BOM/compile-breaking issue.
- Yellow: TODO, debug logs.
- Green: non-blocking cleanup suggestion.

PASS when red findings are zero. FAIL when red findings require local fixes.

## /resolve

Purpose: detect pre-merge conflicts and produce `resolve-report.md`.

Safety preflight:

1. Run `git status --short`.
2. If unrelated dirty work exists, BLOCKED and ask before continuing.
3. Fetch only when network/permissions allow it; otherwise use local base.

Suggested commands:

```bash
git fetch origin main
git merge origin/main --no-commit --no-ff
git diff --name-only --diff-filter=U
```

Conflict handling:

| Type | Handling |
|------|----------|
| Import/format-only | May resolve automatically |
| Same-file logic conflict | Stop for human confirmation or clearly document assumption |
| `schema.sql` | Never silently overwrite; reconcile both migrations and schema |

After resolution, run:

```bash
mvn verify -pl tt-admin/tt-admin-api -am
```

PASS when merge conflict check is clean and verification passes. FAIL when local conflict resolution is needed. BLOCKED when unsafe dirty state or schema conflict requires user choice.

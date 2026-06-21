---
name: tt-slice-workflow
description: |
  TT 乒乓球赛事管理系统的切片开发质量保障工作流。Use this skill when the user invokes
  /slice-bootstrap, /slice-bootstrap-fe, /baseline-review, /slice-plan,
  /slice-resume, /verify, /verify-fe, /scope-check, /hygiene, /resolve,
  /smoke, /handoff, /design-sync, or asks to run the TT slice skill chain.
  It converts the repository's Cursor slash skills into Codex-usable workflows.
metadata:
  short-description: TT 切片开发与验收链
---

# TT Slice Workflow

Use this skill inside the TT repository at `/Users/panzhifeng/pzf-file/AIWORK/赛事记录`.
It is a Codex-ready consolidation of the original `.cursor/skills/*` slash skills.

## Core Protocol

Before changing files, read the relevant slice docs and code. Keep `.cursor/skills` untouched unless the user explicitly asks to edit the legacy Cursor copy.

Every skill run ends with:

```text
result: PASS | FAIL | BLOCKED
next_action: CONTINUE | RETRY | ESCALATE
change_digest: <short summary>
```

Default chain:

```text
Backend:
/slice-bootstrap -> /baseline-review -> /slice-plan -> coding
  -> /verify -> /scope-check -> /hygiene -> /resolve -> /smoke -> /handoff

Frontend:
/slice-bootstrap-fe -> /baseline-review -> /slice-plan -> coding
  -> /verify-fe -> /scope-check -> /hygiene -> /resolve -> /smoke -> /handoff

Design gaps:
/design-sync can be used at any stage.
```

## Repository Anchors

- Tech stack: `乒乓球赛事管理系统（TT）整体规划/a-锚定项/02-技术栈.md`
- Backend slices: `乒乓球赛事管理系统（TT）整体规划/c-切片开发文档（后端）/切片{N}-*/`
- Frontend slices: `乒乓球赛事管理系统（TT）整体规划/d-前端视图设计（前端）/切片UI-*/`
- API module: `tt-admin/tt-admin-api/`
- Schema authority: `tt-admin/tt-admin-api/src/main/resources/db/schema.sql`
- Migration dir: `tt-admin/tt-admin-api/src/main/resources/db/migration/`
- Docker schema snapshot: `docker/mysql/init/01-schema.sql`
- Frontend: `client/admin`, `client/portal`, `client/shared`

## Dispatch

When the user invokes a slash command, load only the matching reference:

- `/slice-bootstrap`, `/slice-bootstrap-fe`: read `references/bootstrap.md`.
- `/baseline-review`: read `references/baseline-review.md`.
- `/slice-plan`: read `references/plan.md`.
- `/slice-resume`: read `references/resume.md`.
- `/verify`, `/verify-fe`: read `references/verify.md`.
- `/scope-check`, `/hygiene`, `/resolve`: read `references/quality-gates.md`.
- `/smoke`: read `references/smoke.md`.
- `/handoff`: read `references/handoff.md`.
- `/design-sync`: read `references/design-sync.md`.

Also read these shared references when needed:

- `references/anti-patterns.md` for common TT mistakes.
- `references/execution-patterns.md` for implementation conventions.

## Base Branch Detection

The original Cursor skills mentioned `develop...HEAD`, but this repository may only have `main`.
Before any diff-based gate, resolve the base branch:

```bash
git rev-parse --verify develop
git rev-parse --verify main
```

Use `develop` if it exists; otherwise use `main`. If neither exists, BLOCKED.

## Validation Commands

Use these commands when the matching area changed:

```bash
mvn clean verify -pl tt-admin/tt-admin-api -am
npm run lint
npm run build:all
```

Run frontend commands from the repository root. The root `package.json` already points to `client/*`.

## Safety

- Do not silently merge or overwrite `schema.sql`; schema changes must include `schema.sql`, a Flyway migration, and the Docker init snapshot when applicable.
- Do not run destructive Git commands. For `/resolve`, inspect working tree cleanliness first and stop on non-trivial conflicts.
- For `/smoke`, start services only when required and report the port, env assumptions, and curl results.

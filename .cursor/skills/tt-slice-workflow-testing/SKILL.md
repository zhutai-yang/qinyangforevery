---
name: tt-slice-workflow-testing
description: |
  Use when testing TT table-tennis tournament workflows, admin-to-portal publishing, backend/frontend alignment, field contract drift, API performance, local stack readiness, self-generated test case checklists, Markdown summaries, HTML evidence reports, screenshots, or browser-previewed test feedback against TT top-level design. Trigger examples: "测一下赛事管理", "验一下成绩录入", "检查官网赛程字段", "跑一轮后台到官网闭环", "/tt-slice-workflow-testing".
---

# TT Slice Workflow Testing

## Overview

Validate TT as a tournament publishing system, not as isolated pages. Treat each workflow as a composition of infrastructure, backend API, frontend admin, public portal, shared domain, evidence, and repair slices. Drive real browser/API behavior when requested, compare implementation against TT top-level design, record controlled bypasses, and produce a repair-oriented report.

## Artifact Location

Do not write test evidence, screenshots, downloaded files, temporary scripts, or repair reports inside the git repository. For this workspace, use a sibling artifact root such as:

```text
/Users/panzhifeng/pzf-file/AIWORK/赛事记录-test-artifacts/<yyyyMMdd>-<workflow-slug>/
```

If another workspace is used, place artifacts in a sibling directory outside the repo root. Before finishing, run `git status --short` and move accidental artifacts out of the repository.

When browser/API testing is executed, or when findings/blockers/field mismatches/performance concerns are found, create two human-readable report artifacts in the artifact root:

- concise Markdown summary: `tt-<workflow-slug>-test-summary.md`
- self-contained HTML evidence report: `tt-<workflow-slug>-test-report.html`

The Markdown summary is text-only. The HTML report may embed or link screenshots. The final chat response should summarize both and point to absolute paths.

## Project Shape

Assume the TT workspace contains these surfaces unless local context proves otherwise:

| Surface | Typical path | Role |
|---|---|---|
| Admin frontend | `client/admin` | Tournament management, CMS, data source configuration |
| Portal frontend | `client/portal` | Public tournament list, schedule, results, player/news pages |
| Shared frontend | `client/shared` | Shared request, layout, components, types |
| Admin API | `tt-admin/tt-admin-api` | `/api/admin/**`, `/api/public/**`, auth, domain services |
| Schema authority | `tt-admin/tt-admin-api/src/main/resources/db/schema.sql` | Authoritative DB shape |
| Top-level design | `乒乓球赛事管理系统（TT）整体规划/` | Business flow, slices, acceptance baselines, frontend views |
| Local stack | `docker-compose.yml`, Maven/npm scripts | MySQL, API, frontend processes |

Use existing repo scripts and TT skills before inventing startup or verification logic.

## Core Rule

Never claim a TT workflow passes from a single surface. For admin changes, verify API persistence and public/portal visibility when the design says the change should publish. For public pages, verify the public API, field projection, privacy/publication rules, empty states, and frontend rendering.

## Natural Request Entry

Accept short, module-level requests. Do not require a long test prompt.

Examples:

- `测一下赛事管理`
- `验一下成绩录入`
- `看下官网赛程`
- `检查选手字段有没有和后端对齐`
- `跑一轮后台发布到官网`
- `测 CMS 首页编排`
- `只做静态测试计划`

When the request names a module but not a test depth, first discover the module, then present a short mode-selection prompt with a recommended bundle. Wait for the user to confirm the test modes before generating the final checklist or executing browser/API tests.

## Module Discovery

For vague module names such as `赛事`, `场馆`, `阶段`, `场次`, `选手`, `成绩`, `官网`, `CMS`, `首页`, `外部数据`, or `字典审计`, discover relevant code and design context before planning execution:

1. Search top-level design and acceptance baselines for the module name and synonyms.
2. Search admin and portal routes, page titles, components, stores, shared request clients, and type definitions.
3. Search backend controllers, DTOs, services, entities, enums, mappers, migrations, and tests.
4. Search `schema.sql` for table/column authority.
5. Build a candidate module map:
   - possible pages
   - possible APIs
   - public/admin surfaces
   - related status or publish transitions
   - related design fields
   - unknowns or ambiguities

If multiple module meanings are found, show the likely candidates and ask the user to choose before running destructive, slow, or browser-driven tests. For static planning, continue with the most likely candidate and mark assumptions.

## Test Modes

| Mode | Name | Use when | Output |
|---|---|---|---|
| A | Quick smoke | User says `测一下` or wants fast confidence | Module map, reachable pages/APIs, obvious blockers |
| B | Field/API alignment | User mentions fields, forms, interface mismatch, design, or page/backend drift | Field matrix, request/response comparison, mismatch issues |
| C | Admin-to-portal workflow | Admin data should appear on portal/public API | Admin-public slice DAG, publication/visibility/status assertions |
| D | Full closed-loop | User asks for complete validation, acceptance, report, root cause, or end-to-end test data | Infra, UI, API, data readiness, performance, root cause, repair report |
| E | Performance focus | User mentions slow, timeout, loading, list performance, submit latency, or API pressure | Timing table, slow endpoints, repeated requests, performance issues |
| F | Static-only plan | Services are not running or user only wants planning | Test plan, field checklist, API map, execution risks, no browser actions |
| G | Visual evidence/archive | User asks for screenshots, visual acceptance, archive, proof, or delivery evidence | Screenshot plan, annotated evidence, HTML report preview |

Default recommendations:

| User request | Recommend |
|---|---|
| `测一下 <模块>` | A + B |
| `<模块> 后台到官网/发布/闭环` | B + C |
| `<模块> 完整/验收/问题报告` | B + C + D |
| `<模块> 字段/接口/页面不一致` | B |
| `<模块> 慢/性能/超时` | A + E |
| `先看看/不启动服务/做计划` | F |
| `验收/归档/截图/证据/交付` | Add G |

Mode recommendation rules:

- Recommend B whenever browser behavior depends on backend fields or API response shape.
- Recommend C whenever admin-created or admin-edited data should appear in portal/public pages.
- Recommend D only when user asks for complete acceptance/closed loop, or safe reversible test data and cleanup are available.
- Recommend E when timing, repeated requests, slow first render, or timeout risk is visible or mentioned.
- Recommend F when services are unavailable and cannot be started within the task.
- Recommend G when user wants previewable evidence, archived acceptance output, annotated screenshots, or issue screenshots in HTML.

Example prompt:

```text
我找到的是赛事管理后台、公开赛事列表/详情接口和官网赛事详情页。建议走 A+B+C：先确认页面/API 可达，再对齐字段，并验证后台变更能在官网显示。
可选：A 冒烟，B 字段/API，C 后台到官网，D 完整闭环，E 性能，F 静态计划，G 可视化证据。
你回复 A+B、B+C+D，或“按推荐来”后我再生成用例并执行。
```

## Slice Model

A slice is the smallest useful verification unit. A workflow is a dependency-ordered group of slices.

| Slice type | Verify |
|---|---|
| Infrastructure | Docker/services, database readiness, ports, API health, frontend availability |
| Backend API | method/path/query/body, response shape, auth, validation, persistence |
| Schema/domain | table fields, enums, state machine, publish rules, audit/dictionary behavior |
| Admin browser | route, form, table, detail page, modal, validation, loading/error state |
| Portal browser | public list/detail/schedule/results/player/news rendering and empty/error states |
| Cross-surface | admin create/edit/delete/publish impact on public API and portal display |
| Evidence | screenshots, DOM state, console errors, network summary, timings, logs when needed |
| Repair | issue record, bypass record, root cause, suggested fix, regression recommendation |

Represent planned workflows as a slice DAG before broad tests:

```yaml
workflow:
  name: admin-event-to-public-portal
  actors:
    - admin-user
    - public-visitor
  slices:
    - infra.local-stack
    - backend.admin-auth
    - frontend.admin-event-create-or-edit
    - backend.event-persistence
    - backend.public-event-projection
    - frontend.portal-event-list
    - frontend.portal-event-detail
    - evidence.html-report
```

## Test Case Checklist

Before browser/API execution, generate a module-specific test case checklist yourself. Treat it as the execution ledger: every case must end with a checked status.

Use this default case shape:

| Column | Meaning | Required |
|---|---|---|
| Case ID | Stable id such as `TC-001`; generate ids in module order | yes |
| Module | Functional area or page/tab | yes |
| Test Point | Specific assertion or UI/API behavior | yes |
| Preconditions | Required state, account, data, page | yes |
| Steps | Human/browser/API path, numbered when useful | yes |
| Expected Result | Concrete pass condition | yes |
| Priority | P0/P1/P2, inferred if needed | yes |
| Status | pass, fail, blocked, not-run, bypass-dependent | yes, update while testing |
| Actual Result | What really happened | yes after execution |
| Evidence | API timing, DOM observation, screenshot, log, or command | yes after execution |
| Issue | Issue id or `-` | yes after execution |

Generate cases from discovered routes, visible UI, API contracts, design fields, and expected negative paths. Include relevant cases for:

- page reachability, menus, list loading, empty states, and permission-gated visibility
- filters, reset behavior, list columns, display formats, pagination, and detail navigation
- create/edit/delete/publish flows when write testing is authorized
- destructive confirmation, cancel-not-delete, disabled buttons, required validation, duplicates, invalid inputs, and boundary dates/scores
- API request/response shape, status code, timing, and frontend rendering alignment
- admin-to-portal visibility when public publication is expected
- cleanup verification for every created test datum

Priority handling:

- P0: core path, data safety, state transition, permission boundary, required fields, destructive confirmation, cleanup, or public publication rule.
- P1: filters, list fields, optional entry points, secondary actions, display formats, and convenience navigation.
- P2: polish, low-risk layout, secondary empty/error copy, and non-critical convenience behavior.

## Test Loop

1. Interpret the natural request and confirm test modes.
   - If user named a module, run module discovery.
   - If user already chose modes, scope the loop to those modes.
   - If user did not choose modes, present a short mode-selection prompt and wait for confirmation.
   - Do not generate the final checklist or execute browser/API tests until modes are confirmed.

2. Load TT design context.
   - Prefer relevant files under `乒乓球赛事管理系统（TT）整体规划/`.
   - For backend slices, read `acceptance-baseline.md`, `implementation-baseline.md`, and `schema.sql`.
   - For frontend slices, read the matching view document under `d-前端视图设计（前端）/`.
   - If design is missing or ambiguous, record a design gap instead of guessing silently.

3. Build the slice DAG.
   - Separate infrastructure, backend, schema/domain, admin browser, portal browser, cross-surface, and evidence slices.
   - Mark dependencies and safe bypass points before execution.

4. Generate the test case checklist.
   - Create concrete cases with ids, module, test point, preconditions, steps, expected result, priority, and initial status.
   - Prefer P0/P1 cases proving core behavior and negative paths before P2 polish.

5. Start and verify the environment.
   - Confirm API/frontend ports and health.
   - Confirm test accounts, seed data, and required services.
   - Skip this step for static-only mode.

6. Drive browser workflows.
   - Use real admin and portal frontends for user-visible checks.
   - Prefer visible controls and user paths.
   - Capture console errors, failed requests, screenshots, and DOM state.
   - Skip browser actions for static-only mode.

7. Compare frontend actions to backend APIs.
   - For each meaningful click/submit/search, capture the emitted API summary.
   - Compare method, path, query, body shape, response shape, timing, and UI rendering.
   - Redact secrets and personal data.

8. Validate field contracts.
   - Compare design fields to frontend labels/controls, request payloads, response payloads, schema/domain mapping, and portal display.
   - Record drift even if the page remains usable.

9. Validate performance.
   - Record timing for core list/detail/search/submit/publish APIs.
   - If no local threshold exists, use: `>500ms` watch, `>1000ms` concern, `>3000ms` serious.
   - Record repeated requests, waterfall blockers, and slow first render when visible.

10. Run the root-cause loop on failures.
    - Reproduce, locate, hypothesize, test, and record.
    - Identify the first boundary where expected data becomes wrong or missing.

11. Ensure data readiness.
    - If the next slice has no usable data, do not stop at "no data".
    - Decide whether to reuse existing data, create reversible test data, or produce a data-preparation plan.

12. Use controlled bypasses when safe.
    - Record the blocking issue and bypass method.
    - Mark downstream conclusions as normal-path or bypass-dependent.

13. Produce report artifacts.
    - Create both Markdown summary and HTML report when browser/API testing was executed or findings were produced.
    - Store both outside the repository.
    - Redact secrets and personal data.

14. Preview the HTML report.
    - Open it in the in-app browser when available.
    - If preview is unavailable, provide a clickable absolute file path and explain why.

## Data Readiness Policy

Closed-loop TT tests often need generated business data: tournament, venue, phase, match, player, score, CMS article, homepage configuration, dictionary item, or external-source record. Treat missing data as a workflow slice.

| Situation | Action |
|---|---|
| Existing data exactly matches flow, role, state, and publish rules | Reuse it and record IDs |
| Existing data is close but wrong state/date/publish flag | Record why it is unsafe; do not claim full closed-loop |
| Data is missing but can be created through normal UI/API with reversible labels | Create it only when write testing is authorized |
| Data requires direct DB edits, irreversible side effects, or production-like secrets | Ask before modifying; otherwise produce a data-preparation plan |
| Data generation path is unknown | Search design, controllers, services, migrations, and tests until path or design gap is identified |

Default mode is read-only until the user explicitly selects write mode or asks to execute the closed loop. When write mode is allowed:

1. Use normal browser or API paths before direct database writes.
2. Prefix created data with a recognizable test marker when the domain allows it, such as `TT_TEST_<date>`.
3. Record every created ID and cleanup hint.
4. Keep created data minimal.
5. Re-query after each state transition before continuing.
6. If async sync/import is involved, poll with a bounded timeout and record timeout as an issue.
7. If a direct DB or service-trigger bypass is needed, mark it bypass-dependent.

## Cross-Surface Verification

For every admin action, verify its API and portal impact when the design requires public visibility.

| Trigger | Must verify |
|---|---|
| Admin creates/edits tournament | Admin UI success, admin API success, persistence, public list/detail visibility when published |
| Admin creates phases/matches | Schedule/detail APIs return correct ordering, dates, statuses, and portal rendering |
| Admin manages players | Player list/detail API and portal player display match design and privacy rules |
| Admin enters scores | Score validation, match status transition, public result display, ranking/summary effects if designed |
| Admin edits CMS/homepage | Article/homepage public API projection and portal display |
| External sync runs | Source config validation, sync log/audit, public dynamic content projection when applicable |
| Dictionary/audit changes | Admin API validation, schema/domain consistency, audit trail visibility if required |

## Field Contract Alignment

Page testing must follow TT top-level design fields. Check every relevant field across:

- top-level design name and business meaning
- frontend label
- frontend control type
- required/optional rule
- type and format
- enum values and display text
- default value
- validation rule
- role/surface visibility
- workflow-state editability
- request payload mapping
- response payload mapping
- `schema.sql` and entity mapping when observable
- portal public visibility or privacy exclusion

Use this matrix in reports:

```markdown
| Field | Top-level design | Admin UI | Admin API | Schema/domain | Public API | Portal UI | Result |
|---|---|---|---|---|---|---|---|
| eventName | required string | present | present | persisted | present | present | pass |
| matchStatus | enum | present | present | persisted | display mapped | present | pass |
```

## API Contract and Performance Evidence

Capture network evidence as redacted summaries, not raw traffic dumps.

```json
{
  "request": {
    "method": "POST",
    "path": "/api/admin/events",
    "auth": "Bearer <redacted:present>",
    "bodyShape": {
      "name": "string",
      "startDate": "string",
      "endDate": "string"
    }
  },
  "response": {
    "status": 200,
    "durationMs": 346,
    "bodyShape": {
      "code": "number",
      "data.id": "number"
    }
  },
  "uiResult": "Admin list shows the event; public API visibility depends on publish state."
}
```

Always redact:

- `Authorization`, `Cookie`, `Set-Cookie`
- passwords, access tokens, refresh tokens, API keys
- phone numbers, emails, personal IDs
- raw business secrets not needed for diagnosis

Never persist raw secrets in files, logs, screenshots, or final answers.

## Root-Cause Loop

A failed workflow is a trace target, not a final result.

1. Reproduce.
2. Locate the break: UI, console, network, API direct call, logs/database/domain state as needed.
3. Form one hypothesis: "I think X is the root cause because Y."
4. Test that hypothesis.
5. If confirmed, record root cause. If rejected, record why and continue.
6. If three hypotheses fail, mark design/architecture uncertainty and escalate instead of guessing.

## Controlled Bypass Rules

Use a bypass only to continue downstream verification after a blocking issue is recorded.

For each bypass, record:

- blocking slice
- exact symptom
- failed evidence
- bypass method
- why the bypass is safe or limited
- which later conclusions depend on the bypass
- which conclusions remain valid on the normal path

Never hide bypass-dependent results inside normal pass results.

## Issue Record Template

Each issue should include: id, severity, workflow, initiating role, affected surface, slice, reproducibility, symptom, expected, actual, UI evidence, API evidence, performance evidence, field alignment, root-cause status, bypass impact, suggested repair, and regression recommendation.

## Markdown Summary Template

```markdown
# TT Workflow Test Summary - <module/workflow>

- Date/time:
- Environment:
- Modes:
- Verdict:
- HTML report:

## What Was Tested
- 

## Results
- Pass:
- Issues:
- Blocked/bypassed:
- Cleanup:

## Key Evidence
- API timings:
- Browser observations:
- Commands:

## Recommended Repairs
- 
```

## HTML Report Requirements

Create a self-contained HTML evidence report. Keep the visual style quiet and operational: readable typography, compact tables, severity badges, clear issue cards, and screenshot evidence where useful. Do not depend on external CSS, JavaScript, fonts, CDNs, or remote images.

Required sections:

- scope, environment, accounts/roles, selected modes, and exact tested dates/times
- overall verdict and completion status
- tested slice DAG and flow completeness
- self-generated test case checklist with checkbox-style status and coverage summary
- field alignment matrix
- API contract and performance table
- browser/UI observations, console/network failures, and visual evidence
- issue cards with severity, symptom, evidence, root-cause status, suggested repair, and regression recommendation
- bypasses and which conclusions depend on them
- commands run and whether they passed

## Regression Recommendations

Choose the smallest layer that proves the behavior:

| Problem type | Preferred regression |
|---|---|
| Field label/control/validation drift | Frontend component/page test or Playwright browser test |
| API request/response drift | Backend controller/application test plus API contract assertion |
| Status machine issue | Domain/application test |
| Admin-to-portal visibility issue | E2E browser/API workflow test |
| Public projection issue | Public API test plus portal rendering assertion |
| Performance regression | API timing benchmark or lightweight performance script |

Follow existing test frameworks and repo scripts. Do not introduce new test frameworks unless the repo lacks a suitable path and the user accepts the addition.

## Common Mistakes

- Do not require long prompts; accept short module requests, discover context, recommend modes, and ask for confirmation.
- Do not treat UI success toasts as workflow success; verify network, API response, persistence, and portal visibility.
- Do not ignore `schema.sql`; use it as schema authority for backend/schema assertions.
- Do not test only admin or only portal when publication is expected; compose a cross-surface workflow.
- Do not store raw tokens, screenshots, or reports in the repo; redact secrets and use the external artifact root.
- Do not propose fixes from symptoms; run the root-cause loop and identify the first incorrect boundary.

## Final Gate

Before claiming completion, verify:

- the tested workflow is tied to TT top-level design
- admin, API, schema/domain, and portal/public surfaces were included when relevant
- field alignment was checked across frontend and backend
- confirmed test modes and recommendation rationale were recorded
- a self-generated checklist was created before execution and every case has a final status
- API contracts and timings were recorded
- failures went through root-cause tracing
- bypasses were explicit and scoped
- Markdown summary and HTML report were generated outside the repository when needed
- the HTML report was opened in the in-app browser when available
- evidence artifacts were not left inside the git repository
- final report includes concrete repair suggestions and regression recommendations

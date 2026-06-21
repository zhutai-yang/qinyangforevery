# Bootstrap Skills

Use for `/slice-bootstrap` and `/slice-bootstrap-fe`.

## /slice-bootstrap

Purpose: derive backend `implementation-baseline.md` and `acceptance-baseline.md`.

Inputs:

- `乒乓球赛事管理系统（TT）整体规划/c-切片开发文档（后端）/切片{N}-*/00-切片概述.md`
- `乒乓球赛事管理系统（TT）整体规划/a-锚定项/02-技术栈.md`
- `乒乓球赛事管理系统（TT）整体规划/e-切片规划与里程碑.md`
- Existing code under `tt-shared/` and `tt-admin/tt-admin-api/`

Steps:

1. Read the slice overview, milestone row, tech stack, existing controllers/services, and schema.
2. Extract scope, API paths/methods, JWT expectations, table changes, service classes, and owned file globs.
3. Write `implementation-baseline.md` in the slice directory.
4. Write `acceptance-baseline.md` with normal, abnormal, permission, schema, and verification AC.
5. If table changes are needed, state the required `schema.sql`, Flyway, and Docker init updates.
6. Mark the milestone as pending/ready only when the baseline is coherent.

Required backend baseline sections:

- Header: slice id, source docs, tech stack anchor, review status.
- API contract: method, path, auth, request, response.
- Data contract: tables, fields, indexes, migrations.
- Implementation map: controller, application service, infrastructure, tests.
- Owned scope: path globs for scope-check.
- Risks and design gaps.

## /slice-bootstrap-fe

Purpose: derive frontend `implementation-baseline.md` and `acceptance-baseline.md`.

Inputs:

- Frontend slice overview or README under `d-前端视图设计（前端）/切片UI-*`
- Tech stack anchor: Vue 2.7, Vite, admin Element UI, portal Vant
- Backend baseline/API contract where available
- Existing `client/admin`, `client/portal`, and `client/shared`

Steps:

1. Locate pages, routes, API dependencies, and target app: admin or portal.
2. Read `client/shared/request.js`, app `api.js`, route registration, and existing page style.
3. Verify frontend paths do not duplicate `/api` if proxy/baseURL already provides it.
4. List UI/API gaps as a table: field, needed by UI, backend availability, resolution option.
5. Write baselines with loading, empty, error, permission, and refresh-after-mutation AC.
6. Add owned frontend path globs.

## Result Rules

- PASS: both baselines are present, testable, and aligned with anchors/code.
- FAIL: baseline has fixable inconsistencies or missing AC.
- BLOCKED: top-level design/API/schema gap needs `/design-sync`.

# Verify Skills

Use for `/verify` and `/verify-fe`.

## /verify

Purpose: review backend implementation against acceptance baseline and produce `verify-report.md`.

Inputs:

- `acceptance-baseline.md`
- `implementation-baseline.md`
- Branch diff against detected base branch
- Backend code and tests

Phase 1 structural review:

| Check | Method |
|-------|--------|
| API contract | Compare paths, params, security with baseline |
| Layering | Controller thin, logic in `tt-application` |
| Schema | PO/Mapper/SQL match `schema.sql` |
| Tests | Relevant `*Test.java` exist and pass |

Phase 2 AC reconciliation:

- For every AC, mark PASS, FAIL, or N/A.
- Add evidence as class name or `file:line`.
- Fix implementation when failures are clearly local and within scope.

Gate command:

```bash
mvn clean verify -pl tt-admin/tt-admin-api -am
```

## /verify-fe

Purpose: review frontend implementation against API contract and AC, produce `verify-fe-report.md`.

Phase 0 architecture gates:

| ID | Check |
|----|-------|
| VF0-01 | Frontend does not rewrite business status locally |
| VF0-02 | Mutations refresh list/detail afterward |
| VF0-03 | Portal 401 does not redirect to `/admin/login` |
| VF0-04 | No frontend multi-API transaction composition |

Phase 1 API alignment:

| ID | Check |
|----|-------|
| VF1-01 | `api.js` paths match backend Controller |
| VF1-02 | HTTP methods match backend |
| VF1-03 | Request fields match DTO |
| VF1-04 | AC-required functionality calls API |

Phase 2 UI/spec gates:

| ID | Check |
|----|-------|
| VF2-01 | Route registered in `main.js` |
| VF2-02 | Dangerous actions have confirmation |
| VF2-03 | Lists have empty/loading states |
| VF2-04 | `npm run lint` passes |

Gate commands:

```bash
npm run lint
npm run build:all
```

## Result Rules

PASS only when AC coverage meets the baseline and required commands pass. FAIL when fixable items remain. Do not proceed to scope-check with FAIL.

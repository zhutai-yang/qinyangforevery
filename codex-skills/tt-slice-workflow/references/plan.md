# Slice Plan

Use for `/slice-plan`.

Purpose: create `execution-plan.md` from reviewed baselines.

Inputs:

- `implementation-baseline.md`
- `acceptance-baseline.md`
- `baseline-review-report.md`
- `references/anti-patterns.md`

Steps:

1. Confirm baseline review is PASS or equivalent.
2. Split work by business closure, not technical layer.
3. For each task, list AC IDs, files, dependency order, and local verification.
4. Add design review checks and risk predictions.
5. Write `execution-plan.md` in the slice directory.

Task sizing:

| Complexity | Suggested task count | Max AC per task |
|------------|----------------------|-----------------|
| Simple | 3-5 | 10 |
| Standard | 5-8 | 12 |

Split any task touching more than 8 files.

Required plan sections:

- Goal.
- Scope.
- Task table.
- Execution order.
- Risks.
- Design review report.

Design review gates:

| ID | Check |
|----|-------|
| DR-1 | API fields exist in DDL/PO/DTO |
| DR-2 | Every API has normal and abnormal AC |
| DR-3 | Business rules have AC coverage |
| DR-4 | No orphan task or orphan AC |
| DR-5 | Existing utilities such as auth ports/request helpers are reused |
| DR-6 | Owned scope matches baseline |

PASS when the plan is executable and mapped to AC. FAIL when plan/baseline needs local correction.

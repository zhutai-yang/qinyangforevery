# Baseline Review

Use for `/baseline-review`.

Purpose: review `implementation-baseline.md` and `acceptance-baseline.md` before planning.

Inputs:

- Slice `implementation-baseline.md`
- Slice `acceptance-baseline.md`
- Slice overview
- Tech stack anchor
- Existing code and `schema.sql`

Produce `baseline-review-report.md` in the slice directory.

## R1 Top-Level Consistency

| ID | Check | Compare against |
|----|-------|-----------------|
| R1-01 | API path/method | Slice overview and existing Controller |
| R1-02 | Tables/fields | `schema.sql` |
| R1-03 | Tech stack | `a-锚定项/02-技术栈.md` |
| R1-04 | Package/path | `com.tt.*`, `tt-admin`, `tt-shared` |
| R1-05 | Frontend stack | Admin Element UI; portal Vant |

Top-level conflict means produce `/design-sync` proposal and BLOCKED.

## R2 Coverage Sufficiency

| ID | Check | Gate |
|----|-------|------|
| R2-01 | Normal and abnormal path | Each API has at least one normal and one abnormal AC |
| R2-02 | Permission/auth | Admin APIs have JWT-related AC |
| R2-03 | Schema changes | AC covers schema and Flyway checks |
| R2-04 | Owned scope | Baseline lists globs for scope-check |

## R3 AC Completeness

| ID | Check | Simple | Standard |
|----|-------|--------|----------|
| R3-01 | AC count | At least 15 | At least 30 |
| R3-02 | Testable AC | No vague "works normally" AC | Same |
| R3-03 | AC mapping | Sample 3 AC; each maps to API/page | Same |

## Output

Report sections:

- Summary and result.
- R1/R2/R3 findings.
- Required fixes or design-sync proposals.
- Updated baseline `review_status` recommendation.

PASS when all three rounds have no blocking item. FAIL when baseline can be corrected locally. BLOCKED when design sync is required.

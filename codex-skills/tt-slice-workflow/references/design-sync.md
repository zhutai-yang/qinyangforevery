# Design Sync

Use for `/design-sync`.

Purpose: produce a design synchronization proposal when implementation/baseline conflicts with top-level TT design.

When to use:

| Scenario | Urgency |
|----------|---------|
| Baseline conflicts with `02-技术栈.md` | Blocking |
| API conflicts with existing public contract | Blocking |
| New need requires top-level definition | Must settle before slice completion |

Output path:

```text
<slice-dir>/design-sync-proposals/DS-{N}-{NN}.md
```

Template:

```markdown
# DS-{N}-{NN}: Title

- **Type**: Design gap / rule conflict / emergent upgrade
- **Target doc**: `a-锚定项/02-技术栈.md` or slice overview
- **Current state**:
- **Suggested change**:
- **Affected slices**:
- **Status**: pending / accepted / rejected
```

After decision:

- Accepted: update top-level document and rerun `/slice-bootstrap`.
- Rejected: converge baseline to existing top-level design.
- Do not leave "phase 1 does not implement" as an unwritten rule; write the complete rule and mark implementation timing with `@since` when needed.

PASS when proposal is produced and linked from the blocking report. BLOCKED until human decision if it affects current implementation.

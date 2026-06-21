# Handoff

Use for `/handoff`.

Purpose: create `handoff-checklist.md` and confirm the slice is ready for review/merge.

Prerequisites:

- `/verify` or `/verify-fe` PASS.
- `/scope-check` PASS.
- `/hygiene` has no red findings.
- `/resolve` PASS or explicitly skipped because base has no conflicts.
- `/smoke` PASS or documented as not applicable with reason.
- Required backend/frontend commands pass.

Checklist content:

```markdown
# Handoff Checklist

- [ ] Slice id:
- [ ] Branch:
- [ ] Change summary:
- [ ] Java changes:
- [ ] SQL/Flyway changes:
- [ ] Frontend changes:
- [ ] Docker/config changes:
- [ ] Verification summary:
- [ ] Smoke summary:
- [ ] Manual validation steps:
- [ ] Known limitations:
- [ ] Follow-up slices:
- [ ] DS proposal status:
```

After merge, update the milestone row in `e-切片规划与里程碑.md` only when requested or clearly part of the task.

Do not hand off when verify FAIL, scope foreign files remain, hygiene red findings exist, or smoke FAIL is unresolved.

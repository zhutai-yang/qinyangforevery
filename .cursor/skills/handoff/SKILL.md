---
name: handoff
description: |
  生成交接清单 handoff-checklist.md，确认可 Review/合并。
  触发词: "/handoff"
---

# handoff（TT 版）

> **协议**: `shared/skill-protocol.md`

## 前置（全部 PASS）

- `/verify` 或 `/verify-fe`
- `/scope-check`
- `/hygiene`（无 🔴）
- `/resolve`（若合并 main）
- `/smoke`
- `mvn verify` + 前端 `npm run lint` / `build:all`

## 产出

`handoff-checklist.md`：

- [ ] 切片编号、分支名
- [ ] 变更清单（Java / SQL / client / docker）
- [ ] Flyway 脚本名（如有）
- [ ] verify / smoke 报告链接或摘要
- [ ] 手动验证 1~3 步
- [ ] 已知限制 / 后续切片
- [ ] DS 提案状态（如有）

## 合并后

`e-切片规划与里程碑.md` 该切片 → ✅

## 禁止

verify FAIL、foreign>0、smoke FAIL 时 handoff

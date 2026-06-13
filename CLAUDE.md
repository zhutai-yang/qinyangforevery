# TT Skills 入口

> 深度说明：[.cursor/SKILLS-GUIDE.md](.cursor/SKILLS-GUIDE.md)

## 质量保障技能链

```
后端:
/slice-bootstrap → /baseline-review → /slice-plan → 编码 → /slice-resume(可选)
  → /verify → /scope-check → /hygiene → /resolve → /smoke → /handoff

前端:
/slice-bootstrap-fe → /baseline-review → /slice-plan → 编码
  → /verify-fe → /scope-check → /hygiene → /resolve → /smoke → /handoff

设计缺口任意阶段: /design-sync
```

## Skills 清单

| 技能 | 触发词 | 职责 |
|------|--------|------|
| slice-bootstrap | `/slice-bootstrap` | 后端 baseline |
| slice-bootstrap-fe | `/slice-bootstrap-fe` | 前端 baseline |
| baseline-review | `/baseline-review` | 基线三轮审查 |
| slice-plan | `/slice-plan` | execution-plan |
| slice-resume | `/slice-resume` | 中断恢复 |
| verify | `/verify` | 后端审查+AC对账 |
| verify-fe | `/verify-fe` | 前端审查+AC对账 |
| scope-check | `/scope-check` | owned/shared/foreign |
| hygiene | `/hygiene` | 代码卫生 |
| resolve | `/resolve` | 预合并冲突 |
| smoke | `/smoke` | PR 前 curl 冒烟 |
| handoff | `/handoff` | 交接清单 |
| design-sync | `/design-sync` | DS 提案 |

## 规则索引

| 规则 | 路径 |
|------|------|
| 工作流 | `.cursor/rules/workflow.mdc` |
| 安全 | `.cursor/rules/security.mdc` |
| Schema | `.cursor/rules/db-schema-governance.mdc` |
| 文档结构 | `.cursor/rules/doc-structure-governance.mdc` |
| 测试策略 | `.cursor/rules/testing-strategy.mdc` |
| 切片协作 | `.cursor/rules/tt-slice-dev-collab.mdc` |

## 共享知识

- `.cursor/skills/shared/skill-protocol.md`
- `.cursor/skills/shared/anti-patterns.md`
- `.cursor/skills/shared/execution-patterns.md`

## 关键路径

| 项 | 路径 |
|----|------|
| 技术栈锚定 | `乒乓球赛事管理系统（TT）整体规划/a-锚定项/02-技术栈.md` |
| API 模块 | `tt-admin/tt-admin-api/` |
| Schema 权威 | `tt-admin/tt-admin-api/src/main/resources/db/schema.sql` |
| 前端 | `client/admin` + `client/portal` + `client/shared` |

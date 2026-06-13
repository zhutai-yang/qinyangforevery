# Skill 统一协议（TT 版）

> 协议引用：IMP `.cursor/skills/shared/skill-protocol.md` 精简适配

## 执行结果格式

每个 Skill 完成后输出：

| 字段 | 取值 |
|------|------|
| result | PASS / FAIL / BLOCKED |
| next_action | CONTINUE / RETRY / ESCALATE |
| change_digest | 本轮变化摘要 |

## 自动继续

- **PASS** → 进入下一 Skill
- **FAIL** 且可修复 → 修复后重试（同一问题最多 3 次）
- **BLOCKED**（设计冲突、依赖未完成）→ 暂停，等人工决策

## TT 技能链

```
/slice-bootstrap [或 /slice-bootstrap-fe]
  → /baseline-review
  → /slice-plan
  → 编码
  → /verify [或 /verify-fe]
  → /scope-check
  → /hygiene
  → /resolve
  → /smoke
  → /handoff
```

## 单 PR 原则

一个切片一个 PR；shared 文件（`schema.sql`、`pom.xml`、路由）修改需在 scope-check 中标注。

## 路径约定

- 规划根：`乒乓球赛事管理系统（TT）整体规划/`
- 后端切片：`c-切片开发文档（后端）/切片{N}-*/`
- 前端切片：`d-前端视图设计（前端）/切片UI-*/`
- 代码：`tt-shared/`、`tt-admin/tt-admin-api/`、`client/`

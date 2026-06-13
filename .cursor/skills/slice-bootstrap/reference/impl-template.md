# implementation-baseline.md 模板（TT）

> 路径：`乒乓球赛事管理系统（TT）整体规划/c-切片开发文档（后端）/切片N-名称/implementation-baseline.md`

```markdown
# 实现基线：切片N-名称
> **配套**: [acceptance-baseline.md](./acceptance-baseline.md)
> **技术栈**: [a-锚定项/02-技术栈.md](../../a-锚定项/02-技术栈.md)

## §一 接口契约
| # | 方法 | 路径 | 认证 | 说明 |
|---|------|------|------|------|

## §二 数据模型
- 涉及表：（`fnd_*` / `evt_*` / `sch_*` …）
- DDL 变更：是/否 → 文件 `schema.sql` + `migration/V*.sql`

## §三 代码落点
| 组件 | 路径 |
|------|------|
| Application | `tt-application/service/...` |
| Controller | `tt-admin-api/.../controller/` |
| 前端（如有） | `client/admin/views/` 或 `client/portal/views/` |

## §四 业务规则
- （简要条目）

## §五 owned 文件范围（供 scope-check）
- 列出本切片允许修改的文件/目录 glob
```

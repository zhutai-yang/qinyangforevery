# TT 开发推进总纲

> 与《乒乓球赛事管理系统-解决方案与分析.md》配套。

## 文档索引

| 目录 | 说明 |
|------|------|
| [a-锚定项/02-技术栈.md](./a-锚定项/02-技术栈.md) | **技术栈唯一权威源** |
| [c-切片开发文档（后端）](./c-切片开发文档（后端）/) | 后端切片 |
| [d-前端视图设计（前端）](./d-前端视图设计（前端）/) | 前端切片 |
| [e-切片规划与里程碑.md](./e-切片规划与里程碑.md) | **切片注册表（权威）** |
| [g-参考文档](./g-参考文档/) | 本地环境等 |

## Skills 工作流

见仓库根 [CLAUDE.md](../CLAUDE.md) 与 [.cursor/SKILLS-GUIDE.md](../.cursor/SKILLS-GUIDE.md)。

```
/slice-bootstrap [或 /slice-bootstrap-fe]
  → /baseline-review → /slice-plan → 编码 → /slice-resume(可选)
  → /verify [或 /verify-fe]
  → /scope-check → /hygiene → /resolve → /smoke → /handoff
设计缺口任意阶段: /design-sync
```

## 代码与文档对应

- 改 API → 更新切片 baseline + Controller/Application 服务
- 改表 → `schema.sql` + `migration/V*.sql` + `.cursor/rules/db-schema-governance.mdc`

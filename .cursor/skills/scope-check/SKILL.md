---
name: scope-check
description: |
  分支边界审计：owned / shared / foreign 分类。
  触发词: "/scope-check", "边界审计"
---

# scope-check（TT 版）

> **前置**: `/verify` 或 `/verify-fe` PASS  
> **产出**: `scope-check-report.md`

## TT 模块映射

从 baseline §三 + §五 推导 **owned**：

| 模块 | 路径模式 |
|------|---------|
| API | `tt-admin/tt-admin-api/**/controller/*` |
| Application | `tt-shared/tt-application/**/service/**` |
| Infrastructure | `tt-shared/tt-infrastructure/**` |
| 前端 admin | `client/admin/views/**` |
| 前端 portal | `client/portal/views/**` |
| 测试 | `**/*Test.java` |

## shared（append-only）

| 文件 | 规则 |
|------|------|
| `schema.sql` | 改表须同步 migration；不删他人表 |
| `data.sql` | 仅追加本切片种子数据 |
| `SecurityConfig.java` | 仅追加 permit/authorize 规则 |
| `client/*/main.js` | 仅追加路由 |
| `pom.xml` | 仅追加依赖 |

## 执行

```bash
git rev-parse --verify develop || git rev-parse --verify main
git diff --name-only develop...HEAD   # 无 develop 则用 main
```

对每个文件分类；shared 检查 diff 是否仅 `+` 新增。

## 判定

- **PASS**: foreign=0 且 shared append-only 合规
- **FAIL**: foreign>0 或 shared 违规修改 → 回滚或拆 PR

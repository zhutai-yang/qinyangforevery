---
name: slice-bootstrap
description: |
  从切片概述与锚定项派生 implementation-baseline.md 与 acceptance-baseline.md。
  触发词: "/slice-bootstrap", "派生基线"
---

# slice-bootstrap（TT 版）

> **协议**: `shared/skill-protocol.md` · **反模式**: `shared/anti-patterns.md`

## 输入

- `乒乓球赛事管理系统（TT）整体规划/c-切片开发文档（后端）/切片{N}-*/00-切片概述.md`
- `a-锚定项/02-技术栈.md`
- `e-切片规划与里程碑.md`
- 现有代码：`tt-shared/`、`tt-admin/tt-admin-api/`

## 产出

同目录：

- `implementation-baseline.md` — [模板](reference/impl-template.md)
- `acceptance-baseline.md` — [模板](reference/acc-template.md)

## 步骤

1. 读概述：定位 / 范围 / 决策 / 锚点
2. 扫描 `tt-application/service/*` 避免重复
3. 列 API（方法+路径+JWT）、表（`schema.sql` 前缀）、服务类
4. 写可测试 AC（正常+异常+权限）
5. 写 §五 owned 文件 glob
6. 里程碑表该切片 → 🟢 待领取

## 改表约束

同步：`schema.sql` + `migration/V*.sql` + `docker/mysql/init/01-schema.sql`（见 `db-schema-governance.mdc`）

## 下游

→ `/baseline-review` → `/slice-plan`

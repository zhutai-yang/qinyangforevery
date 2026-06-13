# 切片开发文档（后端）

> **文档定位**：后端切片开发文档  
> **协作模式**：Skills 驱动（`/slice-bootstrap` → `/baseline-review` → `/slice-plan` → 编码 → `/verify` → `/handoff`）  
> **全局看板**：[`../e-切片规划与里程碑.md`](../e-切片规划与里程碑.md)  
> **前端切片**：[`../d-前端视图设计（前端）/`](../d-前端视图设计（前端）/)

## 核心原则

> 前端仅作为指令发送者与状态观察者；业务逻辑与状态流转必须在后端闭环。

## 目录结构（Skills 体系）

```
c-切片开发文档（后端）/
├── README.md
└── 切片N-xxx/
    ├── 00-切片概述.md
    ├── implementation-baseline.md    # /slice-bootstrap
    ├── acceptance-baseline.md        # /slice-bootstrap
    ├── execution-plan.md             # /slice-plan
    ├── verify-report.md              # /verify
    └── handoff-checklist.md          # /handoff
```

### 命名规范

| 规则 | 示例 |
|------|------|
| 目录名 | `切片01-工程与数据基础` |
| 分支名 | `slice-1-foundation` 或 `slice-01-foundation` |
| 封存 | 已完成切片目录只读，增强用新切片编号 |

## 门禁

| 阶段 | 条件 |
|------|------|
| 基线就绪 | `implementation-baseline.md` + `acceptance-baseline.md` 同 commit |
| 可合并 | `/verify` PASS + `/handoff` 清单完成 |

## 代码落点

| 层 | 路径 |
|----|------|
| 应用服务 | `tt-shared/tt-application/.../service/{domain}/` |
| API | `tt-admin/tt-admin-api/.../controller/` |
| DDL 权威源 | `tt-admin-api/src/main/resources/db/schema.sql` |
| Flyway | `db/migration/V{date}_{seq}__{desc}.sql` |

# 切片01 实现基线（implementation-baseline.md）

> **状态**：✅ 封存（历史切片，Skills 体系建立前已实现）  
> **互引**：[acceptance-baseline.md](./acceptance-baseline.md)

## §一 接口契约

| # | 方法 | 路径 | 认证 |
|---|------|------|------|
| 1 | POST | `/api/admin/auth/login` | 公开 |
| 2 | GET | `/api/admin/auth/me` | JWT |
| 3 | GET | `/api/health` | 公开 |

## §二 数据模型

- `fnd_user`, `fnd_role`, `fnd_user_role`, `sys_login_rate`
- 权威 DDL：`tt-admin-api/src/main/resources/db/schema.sql`

## §三 代码落点

| 组件 | 路径 |
|------|------|
| AuthService | `tt-application/.../auth/AuthService.java` |
| JWT | `tt-infrastructure/.../security/` |
| 种子数据 | `db/data.sql` + `DataInitConfig` |

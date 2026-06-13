# 切片01 验收基线（acceptance-baseline.md）

> **互引**：[implementation-baseline.md](./implementation-baseline.md)

## §一 功能 AC

| ID | 场景 | 预期 |
|----|------|------|
| AC-01-01 | admin/admin123 登录 | 返回 token |
| AC-01-02 | 错误密码 | 401 AUTH_FAILED |
| AC-01-03 | GET /api/health | ok=true |

## §二 部署 AC

| ID | 场景 | 预期 |
|----|------|------|
| AC-01-D01 | init-db.sh | schema+data 执行成功 |
| AC-01-D02 | Flyway 启动 | baseline 不报错 |

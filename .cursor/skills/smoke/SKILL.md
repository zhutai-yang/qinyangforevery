---
name: smoke
description: |
  PR 前核心路径动态验证（真实启动 + curl）。
  触发词: "/smoke", "冒烟测试"
---

# smoke（TT 版）

> **定位**: 静态 verify 的补充；确保服务能启动、API 可调  
> **前置**: resolve PASS（或 main 无冲突时 skip resolve）

## Step 0 前置

| 检查 | 方法 |
|------|------|
| JWT 配置 | `TT_JWT_SECRET` 或 test profile |
| 测试白名单 | 新增 `/api/admin/**` 路径在 SecurityConfig |
| DB | `application-test.yml` 或 docker mysql |

## 路径来源

从 `acceptance-baseline.md` 提取 P0 CRUD 路径（3~8 条）。

## TT 冒烟模板

```bash
# 健康检查
curl -s http://localhost:8096/api/health

# 管理端登录（示例）
curl -s -X POST http://localhost:8096/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"..."}'

# 公开接口
curl -s http://localhost:8096/api/public/events
```

## 启动方式

```bash
mvn spring-boot:run -pl tt-admin/tt-admin-api -am
# 或 docker compose up -d tt-admin-api
```

## 产出

`smoke-report.md`：用例、请求、预期、实际、PASS/FAIL。

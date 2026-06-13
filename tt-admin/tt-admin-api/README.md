# TT Admin API

Spring Boot 可部署模块，默认端口 **8096**。

## 启动

```bash
# 仓库根目录
mvn spring-boot:run -pl tt-admin/tt-admin-api -am
```

## 环境变量（IMP 风格）

| 变量 | 默认 | 说明 |
|------|------|------|
| DB_HOST | 127.0.0.1 | MySQL 主机 |
| DB_PORT | 3306（容器内）/ **3307**（Docker 宿主机映射） | 端口 |
| DB_NAME | tt_event | 库名 |
| DB_USERNAME | root | 用户 |
| DB_PASSWORD | — | 密码 |
| SPRING_PROFILES_ACTIVE | default | Docker 用 `prod` |
| TT_JWT_SECRET | — | 生产必填 |
| TT_SYNC_MODE | direct | `direct` 同步执行 / `queue` RabbitMQ 异步 |
| TT_SYNC_SCHEDULER_ENABLED | true | 是否按 `interval_minutes` 自动抓取 |
| TT_SYNC_SCHEDULER_DELAY_MS | 60000 | 调度扫描间隔（毫秒） |

## 数据库

```bash
bash scripts/init-db.sh
```

| 文件 | 说明 |
|------|------|
| `src/main/resources/db/schema.sql` | 权威 DDL |
| `data.sql` | 初始数据 |
| `migration/V*.sql` | Flyway 增量 |

## Docker

```bash
# 仓库根目录
docker compose up -d --build tt-admin-api
```

Dockerfile：`tt-admin/tt-admin-api/Dockerfile`

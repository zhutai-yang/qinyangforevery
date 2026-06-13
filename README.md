# 乒乓球赛事管理系统

参考 **IMP（标识中台）** 的 Maven 多模块、Flyway、Docker Compose 与 Skills 切片体系。

## 快速开始（对齐 IMP）

```bash
# 一键全栈（等同 IMP 的 docker compose up）
make env    # 首次：生成 .env（IMP 无此步，凭据写在 compose 里）
make up     # mysql + rabbitmq + tt-admin-api + nginx

# 访问
#   官网 http://localhost:8888/
#   管理端 http://localhost:8888/admin/
#   API  http://localhost:8096/api/health
```

默认 Docker 端口已与 IMP（3306/5672/8080）错开，一般无需改 `.env` 即可同机并行。

### 本地开发（前后端分离）

```bash
make infra    # 仅 MySQL + RabbitMQ
make api      # mvn spring-boot:run（需 Node 18：nvm use）
nvm use && npm install --legacy-peer-deps
npm run dev:portal && npm run dev:admin
```

旧写法（等价）：

```bash
bash tt-admin/tt-admin-api/scripts/init-db.sh   # 需本机 mysql 客户端
mvn spring-boot:run -pl tt-admin/tt-admin-api -am
```

## 项目结构

```
pom.xml
tt-shared/                    # common · application · infrastructure
tt-admin/tt-admin-api/        # Spring Boot API (:8096) + db/
client/                       # portal · admin · shared
乒乓球赛事管理系统（TT）整体规划/   # 切片文档（IMP 结构）
.cursor/                        # Skills + Rules
docker-compose.yml
```

## 数据库（IMP 治理链）

```
tt-admin-api/src/main/resources/db/
├── schema.sql          # 权威 DDL
├── data.sql            # 初始数据
├── migration/V*.sql    # Flyway
└── tools/

docker/mysql/init/      # Compose 首次初始化
```

## 部署

| 方式 | 说明 |
|------|------|
| 本地 | `DB_*` 环境变量 + `mvn spring-boot:run` |
| Docker | `docker compose` → mysql + rabbitmq + tt-admin-api + nginx |
| 阿里云 ECS | `make up-ecs`（2G 小机：swap + JVM/MySQL 调优 + 80 端口） |
| GitHub → ECS 自动部署 | push `main` 且 CI 通过后触发 `.github/workflows/deploy.yml` |
| CI | `.github/workflows/ci.yml` |

### 阿里云试用 ECS（3 个月免费机）

```bash
# 1. 安装 Docker 后克隆仓库
git clone <repo> && cd 赛事记录

# 2. 一键部署（含 2G swap、生成 .env、HTTP 80）
make up-ecs

# 3. 控制台安全组：入方向放行 TCP 22、80
# 4. 浏览器访问 http://<公网IP>/ 与 /admin/
```

MySQL / RabbitMQ / API 端口仅绑定 `127.0.0.1`，公网只经 Nginx 80 进入。首次构建约 10–20 分钟；内存不足时 `scripts/ecs-bootstrap.sh` 会创建 swap。

### GitHub Actions 自动部署到 ECS

**流程：** `push main` → CI 测试通过 → SSH 连 ECS → `git pull` + `docker compose up -d --build`

**一次性配置：**

1. ECS 上完成首次部署（`git clone` + `make up-ecs`）
2. ECS 上生成部署密钥：

```bash
cd /root/qinyangforevery
bash scripts/setup-github-deploy-key.sh
```

3. GitHub 仓库 → **Settings** → **Secrets and variables** → **Actions**，添加：

| Secret | 值 |
|--------|-----|
| `ECS_HOST` | ECS 公网 IP |
| `ECS_USER` | `root` |
| `ECS_SSH_KEY` | 脚本输出的私钥全文 |
| `ECS_PORT` | `22`（可选） |
| `ECS_APP_DIR` | `/root/qinyangforevery`（可选） |

4. 之后每次 push 到 `main`，CI 绿了之后会自动部署；也可在 Actions 页手动运行 **CD — Deploy to Aliyun ECS**。

Docker 服务名：`tt-admin-api`（Nginx 反代 `/api/`）

## 文档与 Skills

- 规划总纲：[`乒乓球赛事管理系统（TT）整体规划/e-切片规划与里程碑.md`](乒乓球赛事管理系统（TT）整体规划/e-切片规划与里程碑.md)
- Skills 入口：[`CLAUDE.md`](CLAUDE.md)
- 本地环境：[`g-参考文档/本地开发环境搭建指南.md`](乒乓球赛事管理系统（TT）整体规划/g-参考文档/本地开发环境搭建指南.md)

## 环境变量（`.env.example`）

`DB_HOST` · `DB_NAME` · `DB_USERNAME` · `DB_PASSWORD` · `TT_JWT_SECRET` · `RABBITMQ_*`

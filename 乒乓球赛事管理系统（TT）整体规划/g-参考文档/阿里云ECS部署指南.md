# 阿里云 ECS 部署指南

> **适用场景**：阿里云试用/按量 ECS（2 核 2G）、Docker Compose 全栈部署 TT 项目  
> **更新日期**：2026-06-13  
> **仓库**：https://github.com/zhutai-yang/qinyangforevery

---

## 一、架构概览

```
浏览器
  │  HTTP :80
  ▼
┌─────────────────────────────────────┐
│  阿里云 ECS（Ubuntu 22.04）          │
│  ┌─────────┐    ┌──────────────┐   │
│  │ nginx   │───▶│ tt-admin-api │   │
│  │ (web)   │    │  :8096       │   │
│  └─────────┘    └──────┬───────┘   │
│                        │           │
│  ┌─────────┐    ┌──────▼───────┐   │
│  │ mysql   │    │ rabbitmq     │   │
│  └─────────┘    └──────────────┘   │
│  MySQL/RabbitMQ/API 仅 127.0.0.1    │
└─────────────────────────────────────┘
```

| 组件 | 说明 |
|------|------|
| `web` | Nginx，对外 80，反代 `/api/` |
| `tt-admin-api` | Spring Boot，JVM 限制 512M |
| `mysql` | MySQL 8，小内存调优配置 |
| `rabbitmq` | 应用启动依赖（即使 `direct` 模式也需运行） |

---

## 二、前置条件

| 项 | 要求 |
|----|------|
| ECS 规格 | 建议 2 核 2G 起，系统盘 40G |
| 操作系统 | **Ubuntu 22.04** 或 Alibaba Cloud Linux 3（勿用 Windows） |
| 地域 | 示例：华东 1（杭州） |
| 安全组 | 入方向放行 **TCP 22、80** |
| GitHub | 仓库需可 clone（公开，或 Token/SSH 已配置） |

---

## 三、最终搭建流程（推荐顺序）

### 3.1 购买/启用 ECS

1. 登录 [阿里云 ECS 控制台](https://ecs.console.aliyun.com)
2. 创建或启用实例（试用 3 个月免费机可用）
3. 记录 **公网 IP**（示例：`8.136.148.178`）

### 3.2 更换为 Linux 系统（若初始为 Windows）

1. **停止** 实例
2. **全部操作** → **更换操作系统**
3. 镜像：**Ubuntu 22.04 64位**
4. 安全设置：**自定义密码**，用户 `root`，设置强密码
5. 系统盘 40G，勾选协议 → **立即更换**
6. **启动** 实例，等待「运行中」

### 3.3 配置安全组

**实例** → **网络与安全组** → 点安全组 ID → **入方向** → **手动添加**：

| 授权策略 | 协议 | 访问来源 | 端口 | 说明 |
|----------|------|----------|------|------|
| 允许 | 自定义 TCP | `0.0.0.0/0` | 22 | SSH / Workbench |
| 允许 | 自定义 TCP | `0.0.0.0/0` | 80 | 网站 |

> **常见误填**：把 `22.0.0.0/9` 填进「访问来源」——那是 IP 网段，不是端口 22。  
> 试用阶段可用 `0.0.0.0/0`；上线后可把 22 收窄为 `你的公网IP/32`。

**不要**对公网开放：3306、5672、8096（项目已绑定 127.0.0.1）。

### 3.4 远程连接

1. 实例页 → **远程连接** → **Workbench** → **立即登录**
2. 用户 `root`，输入更换系统时设置的密码

### 3.5 安装 Docker（国内 ECS 必看）

**不要用** `curl get.docker.com`（国内常报 `SSL_connect: Connection reset by peer`）。

```bash
apt update
apt install -y docker.io docker-compose-v2 git
systemctl enable docker
systemctl start docker
docker --version
docker compose version
```

**（推荐）配置 Docker 镜像加速**（拉取 `mysql`、`nginx` 等官方镜像常超时）：

1. 打开 [容器镜像服务 → 镜像加速器](https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors)
2. 复制专属加速地址，写入 ECS：

```bash
sudo tee /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": ["https://<你的ID>.mirror.aliyuncs.com", "https://docker.m.daocloud.io"]
}
EOF
sudo systemctl restart docker
```

3. 预拉 compose 所需基础镜像（`make up-ecs` 也会自动执行）：

```bash
bash scripts/ecs-prefetch-images.sh
```

若仍失败，见 [§八、故障排查](#八故障排查) 中 Docker Hub 相关条目。

### 3.6 拉取代码

仓库已公开时：

```bash
git clone https://github.com/zhutai-yang/qinyangforevery.git
cd qinyangforevery
```

私有仓库见 [§五、Git 相关问题](#五git-与-github-相关问题)。

### 3.7 一键部署

```bash
make up-ecs
```

脚本会自动：

- 创建 2G swap（防 OOM）
- 从 `.env.example` 生成 `.env`，`HTTP_PORT=80`
- 自动生成 `TT_JWT_SECRET`（若仍为默认值）
- `docker compose up -d --build`

首次构建约 **10–20 分钟**。查看进度：

```bash
docker compose ps
docker compose logs -f tt-admin-api
```

### 3.8 访问

| 入口 | URL |
|------|-----|
| 官网 | `http://<公网IP>/` |
| 管理端 | `http://<公网IP>/admin/` |
| 健康检查 | `http://<公网IP>/api/health` |

### 3.9 部署后必做

编辑 `.env`，修改默认密码：

```bash
nano .env
# ADMIN_INITIAL_PASSWORD、DB_PASSWORD、RABBITMQ_PASSWORD、MYSQL_ROOT_PASSWORD
docker compose down && docker compose up -d
```

---

## 四、GitHub Actions 自动部署（可选）

**流程**：`push main` → CI 通过 → SSH 连 ECS → `git pull` + `docker compose up -d --build`

### 4.1 一次性配置

**ECS 上**（完成 §3.7 后）：

```bash
cd /root/qinyangforevery
bash scripts/setup-github-deploy-key.sh
```

复制输出的 **私钥全文**。

**GitHub 上**：仓库 → **Settings** → **Secrets and variables** → **Actions**：

| Secret | 值 |
|--------|-----|
| `ECS_HOST` | ECS 公网 IP |
| `ECS_USER` | `root` |
| `ECS_SSH_KEY` | 私钥全文 |
| `ECS_PORT` | `22`（可选） |
| `ECS_APP_DIR` | `/root/qinyangforevery`（可选） |

### 4.2 触发方式

- 自动：`push` 到 `main` 且 CI 绿了之后触发 `.github/workflows/deploy.yml`
- 手动：Actions → **CD — Deploy to Aliyun ECS** → **Run workflow**

---

## 五、Git 与 GitHub 相关问题

| 现象 | 原因 | 处理 |
|------|------|------|
| `Write access to repository not granted` / 403 | 私有仓库 + Token 无权限 | 改公开，或 Classic Token 勾选 **repo** |
| `github.com/qinyangforevery` 不存在 | URL 少了 `zhutai-yang/` | 正确：`github.com/zhutai-yang/qinyangforevery.git` |
| 只粘贴 URL 到终端报 `No such file` | 缺少 `git clone` 命令 | 必须：`git clone https://...` |
| HTTPS 与 SSH 混用 | `github.com:用户/仓库` 是 SSH 格式 | HTTPS 用 `/`，SSH 用 `:` |
| Fine-grained PAT 仍 403 | 未授权该仓库或缺少 Contents: Read | 换 Classic PAT 或改公开 |

**私有仓库备选：本机 scp 上传**

Mac 本地：

```bash
cd /path/to/赛事记录
tar czf /tmp/qinyangforevery.tar.gz --exclude=node_modules --exclude=target --exclude=.git --exclude=.env .
scp /tmp/qinyangforevery.tar.gz root@<公网IP>:/root/
```

ECS：

```bash
mkdir -p /root/qinyangforevery
tar xzf /root/qinyangforevery.tar.gz -C /root/qinyangforevery
cd /root/qinyangforevery
make up-ecs
```

---

## 六、本次搭建遇到的问题汇总

| # | 问题 | 解决方案 |
|---|------|----------|
| 1 | 初始系统为 **Windows Server** | 停止实例 → 更换操作系统 → **Ubuntu 22.04** |
| 2 | 安全组「访问来源」填成 `22.0.0.0/9` | 应填 `0.0.0.0/0`（来源是 IP，不是端口） |
| 3 | `get.docker.com` 安装 Docker 失败 | 改用 `apt install docker.io docker-compose-v2` |
| 4 | `docker.service does not exist` | Docker 未装上，先完成 #3 |
| 5 | Git clone 403 / 要用户名 | 仓库私有 → 改 **Public** 或配 Token/SSH |
| 6 | clone URL 缺少 `zhutai-yang/` | 完整路径：`zhutai-yang/qinyangforevery` |
| 7 | 把 Token URL 直接粘贴到 bash | 需完整命令：`git clone https://...` |
| 8 | `make: No rule to make target 'up-ecs'` | 未 clone 成功或未 `cd` 进项目目录 |
| 9 | 2G 内存构建 OOM | 项目 `make up-ecs` 已含 swap + JVM/MySQL 调优 |
| 10 | Token 泄露在聊天/截图 | 立即到 GitHub **Revoke**，重新生成 |
| 11 | `docker pull` / build 超时 | 配置阿里云镜像加速 + `ecs-prefetch-images.sh` |

---

## 七、项目内 ECS 相关文件

| 路径 | 作用 |
|------|------|
| `docker-compose.yml` | 全栈编排；MySQL/RabbitMQ/API 绑定 127.0.0.1 |
| `docker/mysql/conf.d/small-instance.cnf` | 2G 小机 MySQL 调优 |
| `tt-admin-api/Dockerfile` | JVM `-Xms256m -Xmx512m` |
| `scripts/ecs-bootstrap.sh` | swap + `.env` 初始化 |
| `scripts/ecs-deploy.sh` | 一键部署入口 |
| `scripts/setup-github-deploy-key.sh` | GitHub Actions SSH 密钥 |
| `.github/workflows/deploy.yml` | 自动部署 workflow |
| `Makefile` | `make up-ecs` |

---

## 八、故障排查

| 现象 | 排查 |
|------|------|
| 页面打不开 | 安全组是否放行 80；`docker compose ps` 是否全 healthy |
| 502 / 504 | API 未就绪：`docker compose logs -f tt-admin-api` |
| Workbench 连不上 | 安全组 22；root 密码是否正确 |
| 构建 killed | `free -h` 看内存；确认 swap 已创建 |
| `git pull` 失败 | 网络；或仓库权限 |

常用命令：

```bash
docker compose ps
docker compose logs -f tt-admin-api
docker compose down && docker compose up -d --build
free -h && swapon --show
```

---

## 九、安全与运维建议

| 项 | 试用阶段 | 正式上线建议 |
|----|----------|--------------|
| SSH 来源 | `0.0.0.0/0` | 改为 `你的IP/32` |
| 密码 | 换掉 `.env` 与 root 默认 | 强密码 + 定期轮换 |
| HTTPS | 可暂用 HTTP | 域名备案 + SSL（443） |
| 备份 | 手动 mysqldump + uploads 卷 | 定时快照 / RDS |
| GitHub Token | 勿提交、勿贴聊天 | 泄露立即 Revoke |

备份示例：

```bash
docker exec tt-mysql mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" tt_event > backup.sql
```

---

## 十、快速命令清单（复制用）

```bash
# === ECS Workbench 一次性部署 ===
apt update && apt install -y docker.io docker-compose-v2 git
systemctl enable docker && systemctl start docker

git clone https://github.com/zhutai-yang/qinyangforevery.git
cd qinyangforevery
make up-ecs

# === 查看状态 ===
docker compose ps
docker compose logs -f tt-admin-api

# === GitHub Actions 密钥（可选）===
bash scripts/setup-github-deploy-key.sh
```

---

## 变更记录

| 日期 | 说明 |
|------|------|
| 2026-06-13 | 初版：试用 ECS 搭建流程、问题汇总、GitHub Actions CD |

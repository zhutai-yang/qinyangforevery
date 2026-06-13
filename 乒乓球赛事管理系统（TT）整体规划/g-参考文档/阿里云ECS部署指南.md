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

复制输出的 **私钥全文**（见下方「私钥粘贴规范」）。

**GitHub 上**：仓库 → **Settings** → **Secrets and variables** → **Actions**：

| Secret | 值 | 常见错误 |
|--------|-----|----------|
| `ECS_HOST` | ECS 公网 IP（如 `8.136.148.178`） | 带了 `http://` 或末尾空格 |
| `ECS_USER` | `root` | 写成 `ubuntu` 等 |
| `ECS_SSH_KEY` | **私钥全文** | 见下方规范 |
| `ECS_PORT` | `22`（可选） | — |
| `ECS_APP_DIR` | `/root/qinyangforevery`（可选） | — |

**私钥粘贴规范（`ECS_SSH_KEY`）**：

```bash
# ECS 上查看完整私钥
cat /root/.ssh/github_actions_deploy
```

- 必须从 `-----BEGIN OPENSSH PRIVATE KEY-----` 到 `-----END OPENSSH PRIVATE KEY-----` **整段复制**
- 粘贴的是 **私钥**，不是 `.pub` 公钥
- 不要少复制 BEGIN/END 行，不要多空行或截断中间内容
- 私钥只存 GitHub Secret，**不要发到聊天/截图**

Secret 名称必须严格为 `ECS_HOST`、`ECS_USER`、`ECS_SSH_KEY`（与 workflow 一致）。

### 4.2 触发方式
- 自动：`push` 到 `main` 且 CI 绿了之后触发 `.github/workflows/deploy.yml`
- 手动：Actions → **CD — Deploy to Aliyun ECS** → **Run workflow**

> 配好 Secret 后需 **重新 Run workflow**；之前失败的 run 不会自动重试。

### 4.3 GitHub Actions 踩坑（已验证）

| 现象 | 原因 | 处理 |
|------|------|------|
| `can't connect without a private SSH key or password` | 未配 Secret，或 `ECS_SSH_KEY` 为空 | 添加三个 Secret 后重新 Run workflow |
| 同上，Secret 已配仍失败 | `ECS_SSH_KEY` **不完整**（缺 `BEGIN` 行或截断） | `cat ~/.ssh/github_actions_deploy` 整段重贴 |
| 误贴公钥 | 复制了 `.pub` 文件 | 必须贴 **无 `.pub` 后缀** 的私钥文件 |
| Secret 名写错 | 如 `SSH_KEY`、`ECS_SSH` | 必须：`ECS_HOST` / `ECS_USER` / `ECS_SSH_KEY` |
| Deploy 在配 Secret 之前已跑 | 旧 run 必然失败 | Actions 页手动 **Run workflow** |
| `目录 ... 不是 git 仓库` | ECS 未 clone 或未 `make up-ecs` | 先完成 §3.7 首次部署 |
| SSH 连不上 | 安全组未开 22；实例已停 | 检查安全组与实例状态 |

**验证 Secret 是否生效的检查顺序**：

1. GitHub Secrets 三个都已填 → Edit 确认 `ECS_SSH_KEY` 含 BEGIN/END
2. ECS：`grep github-actions-deploy ~/.ssh/authorized_keys` 有输出
3. ECS：`ls /root/qinyangforevery/.git` 存在
4. Actions → **Run workflow**（不是重跑旧 commit 前的失败记录也行，但要新触发一次）

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

## 六、踩坑指南（全量）

按部署阶段分类，均为本次试用 ECS 搭建中 **实际遇到过** 的问题。

### 6.1 阿里云控制台

| # | 坑 | 正确做法 |
|---|-----|----------|
| 1 | 试用机默认 **Windows Server** | 停止 → 更换操作系统 → **Ubuntu 22.04** |
| 2 | 安全组「访问来源」填 `22.0.0.0/9` | 来源是 **IP 网段**，不是端口；试用填 `0.0.0.0/0` |
| 3 | 只开 80 没开 22 | Workbench / GitHub Deploy 都需要 **22** |
| 4 | 对公网开了 3306/8096 | 不必开；compose 已绑 `127.0.0.1` |

### 6.2 ECS 环境与 Docker

| # | 坑 | 正确做法 |
|---|-----|----------|
| 5 | `curl get.docker.com` → `SSL_connect: Connection reset` | `apt install docker.io docker-compose-v2` |
| 6 | `docker.service does not exist` | 上一条 Docker 没装上 |
| 7 | `docker pull` / build **i/o timeout** | 配 [镜像加速器](https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors) + `ecs-prefetch-images.sh` |
| 8 | 2G 内存 build 被 **killed** | `make up-ecs` 自动 swap；看 `free -h` |

### 6.3 Git 拉代码

| # | 坑 | 正确做法 |
|---|-----|----------|
| 9 | 私有仓库 **403** / `Write access not granted` | 改 **Public**，或 Classic PAT 勾 `repo` |
| 10 | URL 写成 `github.com/qinyangforevery` | 必须含用户名：`zhutai-yang/qinyangforevery` |
| 11 | 把 clone URL **单独粘贴**到 bash | 要完整命令：`git clone https://...` |
| 12 | HTTPS / SSH 格式混用 | HTTPS 用 `/`；SSH 用 `git@github.com:用户/仓库.git` |
| 13 | Fine-grained PAT 仍 403 | 仓库授权未勾选，或换 Classic PAT |
| 14 | Token 贴到聊天 / 截图 | 立即 **Revoke**，重新生成 |

### 6.4 项目部署

| # | 坑 | 正确做法 |
|---|-----|----------|
| 15 | `make: No rule to make target 'up-ecs'` | clone 失败或未 `cd` 进项目目录 |
| 16 | 页面 502 | API 还在 build，等 `docker compose logs -f tt-admin-api` |
| 17 | `.env` 仍用默认密码 | 部署后改 `ADMIN_INITIAL_PASSWORD` 等 |

### 6.5 GitHub Actions 自动部署

| # | 坑 | 正确做法 |
|---|-----|----------|
| 18 | `can't connect without a private SSH key or password` | 配齐 `ECS_HOST` / `ECS_USER` / `ECS_SSH_KEY` |
| 19 | Secret 有了仍连不上 | **`ECS_SSH_KEY` 不完整**（缺 `-----BEGIN...` 行）→ 整段重贴 |
| 20 | 贴了 `.pub` 公钥 | 必须贴私钥：`cat ~/.ssh/github_actions_deploy` |
| 21 | 配 Secret 前的失败 run | 配好后 Actions 页 **Run workflow** 重新触发 |
| 22 | ECS 无 `/root/qinyangforevery/.git` | 先手动完成首次 `git clone` + `make up-ecs` |

### 6.6 速查：一眼判断问题在哪

```
连不上 ECS Workbench     → 安全组 22、root 密码
git clone 403            → 仓库改 Public
docker 装不上            → 别用 get.docker.com，用 apt
docker pull 超时         → 镜像加速 + prefetch 脚本
make up-ecs 找不到       → 没 clone 成功
网站 502                 → 等 API healthy
Actions 没 SSH key       → 补 Secret
Actions Secret 有仍失败  → 私钥缺 BEGIN/END，整段重贴
```

---

## 七、项目内 ECS 相关文件

| 路径 | 作用 |
|------|------|
| `docker-compose.yml` | 全栈编排；MySQL/RabbitMQ/API 绑定 127.0.0.1 |
| `docker/mysql/conf.d/small-instance.cnf` | 2G 小机 MySQL 调优 |
| `tt-admin-api/Dockerfile` | JVM `-Xms256m -Xmx512m` |
| `scripts/ecs-bootstrap.sh` | swap + `.env` 初始化 |
| `scripts/ecs-deploy.sh` | 一键部署入口 |
| `scripts/ecs-prefetch-images.sh` | 国内 ECS 预拉 Docker Hub 基础镜像 |
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
| `docker pull` 超时 / `i/o timeout` | 配置 `/etc/docker/daemon.json` 镜像加速；`bash scripts/ecs-prefetch-images.sh` |
| Actions `can't connect without a private SSH key` | 补全三个 Secret；见 [§4.3](#43-github-actions-踩坑已验证) |
| Actions Secret 有但仍 SSH 失败 | `ECS_SSH_KEY` 缺 BEGIN/END → 整段重贴私钥 |

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
| `ECS_SSH_KEY` | 整段私钥存 Secret | 缺 BEGIN 行会导致 Deploy 失败 |

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

# 镜像加速（<你的ID> 换为阿里云加速器地址）
sudo tee /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": ["https://<你的ID>.mirror.aliyuncs.com", "https://docker.m.daocloud.io"]
}
EOF
sudo systemctl restart docker

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
| 2026-06-13 | 补充踩坑指南 §6：GitHub Actions SSH 私钥 BEGIN/END、Secret 配置 |

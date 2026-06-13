#!/usr/bin/env bash
# 阿里云 ECS 全栈部署（小内存优化版 compose）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

bash scripts/ecs-bootstrap.sh

if ! command -v docker >/dev/null 2>&1; then
  echo "!! 未检测到 docker，请先安装: https://docs.docker.com/engine/install/"
  exit 1
fi

COMPOSE=(docker compose)
if [[ ! -f .env ]]; then
  echo "!! 缺少 .env，请先运行 bash scripts/ecs-bootstrap.sh"
  exit 1
fi

echo "==> 构建并启动（首次约 10–20 分钟）..."
"${COMPOSE[@]}" up -d --build

echo ""
echo "==> 服务状态"
"${COMPOSE[@]}" ps

echo ""
echo "访问（将 <公网IP> 换成 ECS 公网地址）："
echo "  官网     http://<公网IP>/"
echo "  管理端   http://<公网IP>/admin/"
echo "  健康检查 http://<公网IP>/api/health"
echo ""
echo "查看 API 日志: docker compose logs -f tt-admin-api"

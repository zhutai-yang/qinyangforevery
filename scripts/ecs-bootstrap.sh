#!/usr/bin/env bash
# 阿里云试用 ECS 首次初始化：swap + .env（ECS 默认值）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

SWAP_SIZE="${SWAP_SIZE:-2G}"
SWAP_FILE="${SWAP_FILE:-/swapfile}"

ensure_swap() {
  if swapon --show | grep -q "${SWAP_FILE}"; then
    echo "==> swap 已启用: ${SWAP_FILE}"
    return 0
  fi
  if [[ -f "${SWAP_FILE}" ]]; then
    echo "==> 启用已有 swap 文件: ${SWAP_FILE}"
    sudo swapon "${SWAP_FILE}"
    return 0
  fi
  echo "==> 创建 ${SWAP_SIZE} swap: ${SWAP_FILE}"
  sudo fallocate -l "${SWAP_SIZE}" "${SWAP_FILE}" || sudo dd if=/dev/zero of="${SWAP_FILE}" bs=1M count=2048
  sudo chmod 600 "${SWAP_FILE}"
  sudo mkswap "${SWAP_FILE}"
  sudo swapon "${SWAP_FILE}"
  if ! grep -q "${SWAP_FILE}" /etc/fstab 2>/dev/null; then
    echo "${SWAP_FILE} none swap sw 0 0" | sudo tee -a /etc/fstab >/dev/null
  fi
  free -h
}

ensure_env() {
  if [[ ! -f .env ]]; then
    cp .env.example .env
    echo "==> 已从 .env.example 创建 .env"
  fi
  # ECS 推荐值（仅当仍为 example 默认值时覆盖，避免覆盖用户已改配置）
  if grep -q '^HTTP_PORT=8888$' .env; then
    sed -i.bak 's/^HTTP_PORT=8888$/HTTP_PORT=80/' .env && rm -f .env.bak
    echo "==> HTTP_PORT 已设为 80（ECS 公网访问）"
  fi
  if grep -q 'please-change-to-256-bit-secret-key-for-production-min-32chars!!' .env; then
    if command -v openssl >/dev/null 2>&1; then
      SECRET="$(openssl rand -base64 48 | tr -d '\n' | head -c 48)"
      sed -i.bak "s|^TT_JWT_SECRET=.*|TT_JWT_SECRET=${SECRET}|" .env && rm -f .env.bak
      echo "==> 已自动生成 TT_JWT_SECRET"
    else
      echo "!! 请手动修改 .env 中的 TT_JWT_SECRET、数据库与 RabbitMQ 密码"
    fi
  fi
}

ensure_swap
ensure_env
echo ""
echo "下一步："
echo "  1. 编辑 .env，确认 ADMIN_INITIAL_PASSWORD 与 DB/RabbitMQ 密码"
echo "  2. 阿里云安全组入方向放行 TCP 80（及 SSH 22）"
echo "  3. make up-ecs   或   bash scripts/ecs-deploy.sh"

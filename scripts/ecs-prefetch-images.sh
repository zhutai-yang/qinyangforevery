#!/usr/bin/env bash
# 国内 ECS：经镜像前缀预拉 compose / Dockerfile 所需的 Docker Hub 官方镜像并打回原 tag。
# 用法：bash scripts/ecs-prefetch-images.sh
# 可选：DOCKER_MIRROR_PREFIX=docker.m.daocloud.io/library bash scripts/ecs-prefetch-images.sh
set -euo pipefail

MIRROR_PREFIX="${DOCKER_MIRROR_PREFIX:-docker.m.daocloud.io/library}"

IMAGES=(
  mysql:8.0
  rabbitmq:3-management-alpine
  maven:3.9-eclipse-temurin-17
  eclipse-temurin:17-jre-jammy
  node:18-alpine
  nginx:1.25-alpine
)

pull_one() {
  local name="$1"
  if docker image inspect "$name" >/dev/null 2>&1; then
    echo "  ✓ 已有本地镜像: $name"
    return 0
  fi
  local src="${MIRROR_PREFIX}/${name}"
  echo "  → 拉取 $src"
  if docker pull "$src"; then
    docker tag "$src" "$name"
    echo "  ✓ 已标记为 $name"
    return 0
  fi
  echo "!! 拉取失败: $src"
  return 1
}

echo "==> 预拉镜像（前缀: ${MIRROR_PREFIX}）"
failed=0
for img in "${IMAGES[@]}"; do
  pull_one "$img" || failed=1
done

if [[ "$failed" -ne 0 ]]; then
  echo ""
  echo "!! 部分镜像拉取失败。请配置 Docker 加速器后重试："
  echo "   1. 登录 https://cr.console.aliyun.com/ → 镜像工具 → 镜像加速器"
  echo "   2. 将专属地址写入 /etc/docker/daemon.json 的 registry-mirrors"
  echo "   3. sudo systemctl restart docker"
  echo "   4. 再执行 bash scripts/ecs-prefetch-images.sh"
  exit 1
fi

echo "==> 全部镜像就绪"

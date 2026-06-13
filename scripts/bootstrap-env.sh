#!/usr/bin/env bash
# 生成 .env（docker compose 必需，对齐 IMP 一键启动体验）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [[ -f .env ]]; then
  echo "==> .env 已存在，跳过"
  exit 0
fi
cp .env.example .env
echo "==> 已从 .env.example 创建 .env"

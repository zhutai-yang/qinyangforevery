#!/usr/bin/env bash
# 在 ECS 上运行：生成 GitHub Actions 部署专用 SSH 密钥
# 用法：bash scripts/setup-github-deploy-key.sh
set -euo pipefail

KEY="$HOME/.ssh/github_actions_deploy"
AUTH="$HOME/.ssh/authorized_keys"

mkdir -p "$HOME/.ssh"
chmod 700 "$HOME/.ssh"

if [[ -f "$KEY" ]]; then
  echo "==> 密钥已存在: $KEY"
else
  ssh-keygen -t ed25519 -f "$KEY" -N "" -C "github-actions-deploy"
  echo "==> 已生成: $KEY"
fi

PUB="$(cat "${KEY}.pub")"
if [[ -f "$AUTH" ]] && grep -qF "$PUB" "$AUTH" 2>/dev/null; then
  echo "==> 公钥已在 authorized_keys 中"
else
  cat "${KEY}.pub" >> "$AUTH"
  chmod 600 "$AUTH"
  echo "==> 公钥已写入 authorized_keys"
fi

echo ""
echo "========== 下一步：GitHub 仓库 Secrets =========="
echo "Settings → Secrets and variables → Actions → New repository secret"
echo ""
echo "  ECS_HOST     = ECS 公网 IP（如 8.136.148.178）"
echo "  ECS_USER     = root"
echo "  ECS_SSH_KEY  = 下面私钥全文（含 BEGIN/END 行）"
echo "  ECS_PORT     = 22（可选）"
echo "  ECS_APP_DIR  = /root/qinyangforevery（可选，默认此路径）"
echo ""
echo "========== 私钥（复制到 ECS_SSH_KEY）=========="
cat "$KEY"
echo ""
echo "========== 配置完成后 =========="
echo "push 到 main 且 CI 通过后，Actions 会自动部署。"
echo "也可在 Actions 页手动 Run workflow「CD — Deploy to Aliyun ECS」。"

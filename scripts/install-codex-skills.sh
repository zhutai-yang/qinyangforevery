#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
src="${repo_root}/codex-skills/tt-slice-workflow"
dest="${HOME}/.codex/skills/tt-slice-workflow"

if [[ ! -f "${src}/SKILL.md" ]]; then
  echo "Missing skill source: ${src}/SKILL.md" >&2
  exit 1
fi

mkdir -p "$(dirname "${dest}")"
rm -rf "${dest}"
cp -R "${src}" "${dest}"

echo "Installed TT Codex skill:"
echo "  ${dest}"

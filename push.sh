#!/usr/bin/env bash
# usage: ./push.sh [commit-message]  — stage changes, commit, push current branch
#
# 2026-11-08 补丁：git add -A 前先过「敏感文件闸」。
#   背景：2026-11-07 曾因 add -A 把其它未跟踪文件（当时是 push.sh 自身）一起带进 git，
#   这类事故落在 .env* / 密钥证书上就是泄露。闸门规则：
#     · 未跟踪文件命中 .env*（.env.example 豁免）、密钥/证书、secret 清单 ⇒ 拒绝自动提交；
#     · 已跟踪文件的修改照常提交（不受影响）。
set -euo pipefail

cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo="$(basename "$PWD")"
msg="${1:-chore($repo): sync $(date '+%Y-%m-%d %H:%M')}"

# ---- 敏感文件闸：未跟踪 + 命中黑名单 ⇒ 中止，绝不自动入仓 ----
blocking=$(git ls-files --others --exclude-standard \
  | grep -E '(^|/)(\.env|\.env\.[A-Za-z0-9_]+|\.pem|\.key|\.p12|\.pfx|\.jks|\.pgpass|\.htpasswd|id_rsa|id_ed25519|credentials|\.secret|secret\.ya?ml|\.git-credentials)$' \
  | grep -vE '\.env\.example$' || true)
if [ -n "$blocking" ]; then
  echo "[$repo] ❌ 拦截：检测到敏感/凭据类未跟踪文件，拒绝 git add -A："
  echo "$blocking" | sed 's/^/    /'
  echo "    处理这些文件后重跑；确需提交的请手动 git add 指定文件。"
  exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
  git add -A
  git commit -m "$msg"
else
  echo "[$repo] nothing to commit"
fi

branch="$(git rev-parse --abbrev-ref HEAD)"
if git ls-remote --exit-code --heads origin "$branch" >/dev/null 2>&1; then
  git pull --rebase origin "$branch"
fi
git push origin "$branch"
echo "[$repo] pushed $branch"
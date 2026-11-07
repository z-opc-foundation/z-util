#!/usr/bin/env bash
# usage: ./push.sh [commit-message]  — stage all changes, commit, push current branch
set -euo pipefail

cd "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo="$(basename "$PWD")"
msg="${1:-chore($repo): sync $(date '+%Y-%m-%d %H:%M')}"

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

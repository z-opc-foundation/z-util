#!/bin/sh
#
# gpg-with-passphrase.sh —— maven-gpg-plugin 3.2.8 的 passphrase 注入器。
#
# 为什么需要
# ----------
# maven-gpg-plugin 3.2.8 不读 `-Dgpg.passphrase` 与 `-Dgpg.passphraseFile`，
# 默认走 gpg-agent，headless macOS 上撞 pinentry 时一律失败（`No pinentry`）。
# 这个 wrapper 把 `${CENTRAL_GPG_PASSPHRASE}` 注进 `gpg --passphrase`，强制
# `--batch --pinentry-mode loopback`，让插件签任何文件都无声交付。
#
# 前置
# ----
# `CENTRAL_GPG_PASSPHRASE` 必须在 env 里；本 wrapper 会再尝试从同目录 `.env`
# 自加载（z-util 的 `.env` 是 600 + gitignore 存放处），但仅当变量已
# 经空的时候；不要把它自己 export。
#
# 引用的 GPG binary 走 PATH（`command -v gpg`），找不到就退出；brew 默认装在
# /opt/homebrew/bin/gpg，Intel mac 在 /usr/local/bin/gpg。
#

set -e

# 1. 拿不到 passphrase 先 .env 自加载一次（z-util 仓根 .env）
if [ -z "${CENTRAL_GPG_PASSPHRASE:-}" ]; then
    for candidate in "$PWD/.env" "${maven.multiModuleProjectDirectory:-}/.env"; do
        if [ -f "$candidate" ]; then
            set -a
            # shellcheck disable=SC1090
            . "$candidate"
            set +a
            break
        fi
    done
fi

if [ -z "${CENTRAL_GPG_PASSPHRASE:-}" ]; then
    echo "[gpg-wrap] CENTRAL_GPG_PASSPHRASE not set; source .env (e.g. from z-opc-foundation-lead) before running mvn deploy" >&2
    exit 2
fi

# 2. 锁定 gpg 可执行
GPG_BIN="$(command -v gpg 2>/dev/null || true)"
if [ -z "$GPG_BIN" ] || [ ! -x "$GPG_BIN" ]; then
    echo "[gpg-wrap] no gpg on PATH" >&2
    exit 3
fi

# 3. 转交系统 gpg
exec "$GPG_BIN" \
    --batch \
    --pinentry-mode loopback \
    --passphrase "$CENTRAL_GPG_PASSPHRASE" \
    "$@"

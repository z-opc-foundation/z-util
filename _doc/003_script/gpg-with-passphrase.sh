#!/bin/sh
#
# gpg-with-passphrase.sh —— maven-gpg-plugin 3.2.8 的 passphrase 注入器。
#
# 位置：_doc/003_script/gpg-with-passphrase.sh（仓内 tracked）
# 上游 pom：`<executable>${maven.multiModuleProjectDirectory}/_doc/003_script/gpg-with-passphrase.sh</executable>`
# 与 `_doc/006_release/01-release-to-maven-central.md` 同步描述，避免被「scripts/」根级目录层级误导。
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
# `CENTRAL_GPG_PASSPHRASE` 必须在 env 里；本 wrapper 会再尝试从仓根 `.env`
# 自加载（z-util 的 `.env` 是 600 + gitignore 存放处），仅当变量已经
# 空的时候；不要把它自己 export。
#
# 引用的 GPG binary 走 PATH（`command -v gpg`），找不到就退出；brew 默认装在
# /opt/homebrew/bin/gpg，Intel mac 在 /usr/local/bin/gpg。
#

set -e

# 1. 拿不到 passphrase 先 .env 自加载一次（z-util 仓根 .env）
# 注意：仓内 .env 的键名是 GPG_PASSPHRASE，这里要的是 CENTRAL_GPG_PASSPHRASE，
# 所以加载完必须补一次映射，否则下面第 41 行的判空一定命中。
# maven.multiModuleProjectDirectory 是 maven 注入的环境变量，名字带点，
# 不能写成 ${maven.multiModuleProjectDirectory:-}——dash/bash 都判 bad substitution，
# 只能用 printenv 取。
if [ -z "${CENTRAL_GPG_PASSPHRASE:-}" ]; then
    multi_module_dir="$(printenv maven.multiModuleProjectDirectory 2>/dev/null || true)"
    for candidate in "$PWD/.env" "${multi_module_dir:-}/.env"; do
        if [ -f "$candidate" ]; then
            set -a
            # shellcheck disable=SC1090
            . "$candidate"
            set +a
            break
        fi
    done
    if [ -z "${CENTRAL_GPG_PASSPHRASE:-}" ] && [ -n "${GPG_PASSPHRASE:-}" ]; then
        CENTRAL_GPG_PASSPHRASE="$GPG_PASSPHRASE"
    fi
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

# 3. 剔除调用方传入的 --pinentry-mode（连同它的值）
#    ⚠ 2026-10-30 实测：maven-gpg-plugin 3.2.8 自己会传 `--pinentry-mode error`，
#    而 gpg 对同一选项取**最后一次**出现的值 ⇒ 调用方那个 error 盖掉我们自己的
#    loopback ⇒ gpg 拒绝走 loopback 取口令 ⇒ 报 "No pinentry" / Exit code: 2。
#    本仓的 wrapper 早年是 2026-10-20 从 z-boot 仓照搬，那次漏抄了这段剔除逻辑，
#    于是 1.2.11 实发第一次就死在 gpg:sign（z-boot 仓早在 2026-10-21 补上，z-team 1.0.3 同款）。
#    所以先把入参里的 --pinentry-mode 整组剔掉，再由我们自己以 loopback 追加。
argc=$#
while [ "$argc" -gt 0 ]; do
    _arg="$1"
    shift
    argc=$((argc - 1))
    case "$_arg" in
        --pinentry-mode)
            # 丢掉紧随其后的值
            if [ "$argc" -gt 0 ]; then
                shift
                argc=$((argc - 1))
            fi
            ;;
        --pinentry-mode=*)
            # 形如 --pinentry-mode=error，整条丢掉
            ;;
        *)
            set -- "$@" "$_arg"
            ;;
    esac
done

# 4. 转交系统 gpg（loopback + 口令由本 wrapper 兜住）
exec "$GPG_BIN" \
    --batch \
    --pinentry-mode loopback \
    --passphrase "$CENTRAL_GPG_PASSPHRASE" \
    "$@"

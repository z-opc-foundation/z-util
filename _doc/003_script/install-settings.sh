#!/usr/bin/env bash
#
# install-settings.sh — 把 Maven Central 凭证写入 ~/.m2/settings.xml
#
# 行为：
#   1) 若 ~/.m2/settings.xml 不存在，创建一份只含 <server id="central"> 的最小配置
#   2) 若存在，备份原文件为 settings.xml.bak，然后注入 <server id="central">（不覆盖已有 github server）
#   3) 用 ${env.CENTRAL_USERNAME} / ${env.CENTRAL_TOKEN} 占位，避免明文存密码
#
# 用法：
#   ./install-settings.sh
#
# 后续步骤（脚本会提示）：
#   - 终端执行：export CENTRAL_USERNAME=...
#   - 终端执行：export CENTRAL_TOKEN=...
#   - 在 ~/.zshrc 写一份永久生效
#
set -eo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
log()  { printf "${GREEN}[install-settings]${NC} %s\n" "$*"; }
warn() { printf "${YELLOW}[install-settings]${NC} %s\n" "$*"; }
err()  { printf "${RED}[install-settings]${NC} %s\n" "$*" >&2; }
die()  { err "$*"; exit 1; }

SETTINGS="$HOME/.m2/settings.xml"
BAK="$HOME/.m2/settings.xml.bak.$(date +%Y%m%d%H%M%S)"

mkdir -p "$HOME/.m2"

CENTRAL_BLOCK='    <!-- Maven Central (Sonatype Central Portal) -->
    <server>
        <id>central</id>
        <username>${env.CENTRAL_USERNAME}</username>
        <password>${env.CENTRAL_TOKEN}</password>
    </server>'

if [[ -f "$SETTINGS" ]]; then
    log "已检测到 $SETTINGS，备份到 $BAK"
    cp "$SETTINGS" "$BAK"

    if grep -q '<id>central</id>' "$SETTINGS"; then
        log "<server id=\"central\"> 已存在，跳过注入"
    else
        # 在 </servers> 之前插入 central block
        python3 - "$SETTINGS" "$CENTRAL_BLOCK" <<'PY'
import sys, re
path, block = sys.argv[1], sys.argv[2]
with open(path) as f: c = f.read()
new = c.replace("</servers>", block + "\n</servers>", 1)
if new == c:
    sys.exit("未找到 </servers> 节点，请检查 settings.xml 结构")
with open(path, "w") as f: f.write(new)
PY
        log "已注入 <server id=\"central\">"
    fi
else
    log "未找到 $SETTINGS，创建最小配置"
    cat > "$SETTINGS" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
$CENTRAL_BLOCK
    </servers>
</settings>
EOF
    log "已创建 $SETTINGS"
fi

warn ""
warn "═══════════════════════════════════════════════════════════════"
warn "  下一步：把你的 Central Portal User Token 写到环境变量"
warn "═══════════════════════════════════════════════════════════════"
warn ""
warn "  1) 在当前 shell 临时生效（release-to-central.sh 会读它）："
warn "         export CENTRAL_USERNAME='<User Token Username>'"
warn "         export CENTRAL_TOKEN='<User Token Secret>'"
warn ""
warn "  2) 写入 ~/.zshrc 永久生效："
warn "         echo 'export CENTRAL_USERNAME=\"...\"' >> ~/.zshrc"
warn "         echo 'export CENTRAL_TOKEN=\"...\"' >> ~/.zshrc"
warn "         echo 'export CENTRAL_GPG_PASSPHRASE=\"...\"' >> ~/.zshrc"
warn "         source ~/.zshrc"
warn ""
warn "  3) 验证："
warn "         cat ~/.m2/settings.xml"
warn "         env | grep CENTRAL_"
warn "═══════════════════════════════════════════════════════════════"
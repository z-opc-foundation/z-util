# z-util 发布到 Maven Central 操作手册

> 适用版本：z-util 1.0.x 及以上
> 目标仓库：[Maven Central](https://central.sonatype.com/) 主仓库
> GroupId：`io.github.yuku123`（已从原 `com.zifang` 迁移，无域名验证负担）
> 发布模式：Central Portal 新流程（`central-publishing-maven-plugin` 0.7.0）

---

## ⚡ 一句话总结

```bash
cd /Users/zifang/workplace/ceo_workplace/z-opc-foundation/z-util

# 1) 改版本号：只改根 pom 的 <revision> 一行（全仓仅此一处硬编码）
sed -i '' 's|<revision>1.0.23</revision>|<revision>1.0.24</revision>|' pom.xml
git add pom.xml && git commit -m "chore(release): revision bump 1.0.23 → 1.0.24"
git push origin main

# 2) 备好签名环境（口令变量名对不上是头号坑，见下方「GPG」一节）
export GNUPGHOME="$PWD/.gnupg"
set -a && . ./.env && set +a
export CENTRAL_GPG_PASSPHRASE="$GPG_PASSPHRASE"

# 3) 发（74 模块，约 4~6 分钟出 deploymentId）
mvn -Pcentral deploy -pl '!z-util-all' -DskipTests

# 4) 等 5~17 分钟，用 /publisher/deployments 列表判终态（status?id= 会 500）
# 5) 落地后回 repo1 反查 jar/源码，确认这次要修的东西真的在里面
```

> **2026-10-14 实测校准**：本文早期版本写的 `deploy_maven_center.sh` **在仓内已不存在**，
> KEY_ID、模块数、仓路径也已过期。下面保留的每条命令都是 1.0.23 那次实跑验证过的。
> 再发新版请以本文当前内容为准，不要照搬旧段落里的脚本名。

**完整通用流程、AI 助手避坑指南、GPG 配置、POM 元数据模板**等详见同目录的 [发布指引.md](./02-release-guide.md)。

---

## 📚 目录

- [z-util 项目特定差异](#z-util-项目特定差异)
- [z-util 发布相关的仓库文件](#z-util-发布相关的仓库文件)
- [跳过哪些模块（已配置）](#跳过哪些模块已配置)
- [POM 关键配置](#pom-关键配置)
- [日常发布流](#日常发布流)
- [常见问题（z-util 特定）](#常见问题z-util-特定)
- [🛡️ 安全清单](#-安全清单)
- [附录：发布历史](#附录发布历史)

---

## z-util 项目特定差异

z-util 相比典型 Java 项目有以下特殊点：

| 特点 | 说明 |
|------|------|
| **多模块结构** | 1.0.23 那次实跑 **74 个模块**参与发布（早期文档写的 41 已过期；期间新增了 strategy/sensitive 等模块） |
| **版本管理** | 父 POM 用 `<revision>` property，子模块用 `${revision}`。**全仓只有根 pom 硬编码一次版本号**（`grep -rn "1\.0\.2[0-9]" --include=pom.xml` 实测仅 1 命中），改版本不需要"同步子模块"脚本 |
| **GPG 密钥** | KEY_ID = **`A403FCCFD7EF5DA6`**（yuku123 <1340947819@qq.com>，已上传 keys.openpgp.org）。⚠️ 早期文档写的 `9D064F0E20AC7385` 是**旧密钥**，不是当前这个 |
| **密钥环分家** | ⚠️ `.gnupg/` 里原本**只有公钥环**（无 `private-keys-v1.d/`），私钥实际躺在 `~/.gnupg/private-keys-v1.d/`。两半分家时 gpg 会报 `Sorry, we are in batchmode - can't get input` 而**不是**一句"找不到密钥"，很容易误判。修复见下方 Q&A |
| **跳过模块** | `z-util-zex`（沙盒，已从根 `<modules>` 排除）、`z-util-all`（聚合 pom，deploy 时间戳冲突，用 `-pl '!z-util-all'` 跳过） |
| **凭证隔离** | `.env` + `.gnupg/`（项目本地），全部 `.gitignore` 排除。⚠️ `.env` 是**明文**存 Central token 与 GPG 口令的 |
| **口令我已挪走** | `~/.z-opc-foundation/credentials/maven-central/`、`KEYS.md` **均已不存在**。口令真源只有 `z-util/.env` 的 `GPG_PASSPHRASE` |

---

## z-util 发布相关的仓库文件

```
/Users/zifang/workplace/ceo_workplace/z-opc-foundation/z-util/
├── pom.xml                       # 中央元数据 + central profile（已配）；版本号与发布口径的唯一硬编码点
├── .env                          # Central Portal User Token + GPG passphrase（已 gitignore）
├── .gnupg/                       # GPG 密钥环（已 gitignore）
├── .gitignore                    # 含 .env / .gnupg / central-staging
├── README.md                     # 项目 README（含 Maven 依赖示例）
└── _doc/
    ├── 003_script/gpg-with-passphrase.sh    # maven-gpg-plugin 的 passphrase 注入 wrapper
    └── 006_release/
        ├── 01-release-to-maven-central.md   # 本文件（z-util 特定）
        └── 02-release-guide.md              # 通用发布指引（AI / 人类都能用）
```

> ⚠️ **`deploy_maven_center.sh` 已被删除，仓内不存在。** 早期版本本文所有
> `bash deploy_maven_center.sh publish/verify/gpg-init` 都照此执行即可 —— 请改用本文
> 「一句话总结」和「日常发布流」里的裸 `mvn` 命令。

| 文件 | 作用 | 是否 commit |
|------|------|----------|
| `pom.xml` | 中央元数据 + central profile + 子模块 name | ✅ commit |
| `.env` | Central Token + GPG passphrase | ❌ gitignore |
| `.gnupg/` | GPG 密钥环（公钥 + **私钥**，见「密钥环分家」） | ❌ gitignore |
| `_doc/003_script/gpg-with-passphrase.sh` | maven-gpg-plugin 3.2.8 的 passphrase 注入 wrapper（pom 用 `<executable>` 接到这） | ✅ commit |
| `01-release-to-maven-central.md` | 本手册 | ✅ commit |
| `02-release-guide.md` | 通用发布指引 | ✅ commit |

---

## 跳过哪些模块（已配置）

发版命令里用 `-pl '!z-util-all'` 跳过聚合 pom：

| 模块 | 原因 |
|------|------|
| `z-util-zex` | 沙盒模块，仅做 playground，从根 `<modules>` 排除 |
| `z-util-all` | 聚合 pom（`packaging=pom`），其 `dependencyManagement` 引用其他模块解析后的具体版本号。deploy 时 mvn 找不到这些还在同一次上传里的 jar，导致"Could not find artifact xxx" 失败 |

---

## POM 关键配置

### 根 `pom.xml` 摘要

```xml
<groupId>io.github.yuku123</groupId>
<artifactId>z-util</artifactId>
<version>${revision}</version>
<packaging>pom</packaging>

<name>z-util</name>
<description>面向日常 Java 开发的多模块工具库集合（core/cache/ioc/...）</description>
<url>https://github.com/z-opc-foundation/z-util</url>

<licenses>
    <license>
        <name>MIT License</name>
        <url>https://opensource.org/licenses/MIT</url>
    </license>
</licenses>

<developers>
    <developer>
        <id>yuku123</id>
        <name>yuku123</name>
        <email>1340947819@qq.com</email>
    </developer>
</developers>

<scm>
    <connection>scm:git:git://github.com/z-opc-foundation/z-util.git</connection>
    <url>https://github.com/z-opc-foundation/z-util</url>
</scm>

<revision>1.0.23</revision>  <!-- 发新版就改这一行（全仓唯一硬编码点），不带 -SNAPSHOT -->

<profiles>
    <profile>
        <id>central</id>
        <!-- 包含 javadoc / gpg / central-publishing 插件 -->
    </profile>
    <profile>
        <id>central-dryrun</id>
        <!-- 本地验证 POM 元数据 + javadoc 生成，不签名不上传 -->
    </profile>
</profiles>
```

### ⚠️ `maven-gpg-plugin` 3.2.8 的 passphrase 入口

3.2.8 起 plugin **不读** `-Dgpg.passphrase` 与 `-Dgpg.passphraseFile`，默认走 gpg-agent；
headless macOS 上必撞「No pinentry」。pom 用 `<executable>` 显式接到仓内 wrapper：

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-gpg-plugin</artifactId>
  <version>3.2.8</version>
  <configuration>
    <skip>${central.gpg.skip}</skip>
    <executable>${maven.multiModuleProjectDirectory}/_doc/003_script/gpg-with-passphrase.sh</executable>
  </configuration>
</plugin>
```

wrapper（[`_doc/003_script/gpg-with-passphrase.sh`](../003_script/gpg-with-passphrase.sh)）只把
`${CENTRAL_GPG_PASSPHRASE}` 注进 `gpg --passphrase`，强制 `--batch --pinentry-mode loopback`；
本身不存 secret，passphrase 由调用方按 [[../../008_组织规范/001_凭证管理规范]]（CEE-OPS-K01）走仓根 `.env`（已 `gitignore`）。
**不要再退回 `/tmp/gpg-wrap/gpg` 临时拼 wrapper**——接手的人没法从 git history 看到，下次必撞。

### 子模块 `<name>` 规则

每个发布的子模块必须有 `<name>` 且 **不能等于 `<artifactId>`**：

```xml
<project>
    <parent>
        <artifactId>z-util</artifactId>
        <groupId>io.github.yuku123</groupId>
    </parent>
    <artifactId>z-util-core</artifactId>
    <version>${revision}</version>

    <name>z-util :: core (base utilities: collection/string/io/concurrent/crypto/jwt/limiter/breaker/scheduler)</name>
    <!--  ↑ 必须这么写，"z-util :: " 前缀 + 简短描述 -->
</project>
```

**注意**：4 个模块原本 `<name>` 写成 `z-util-xxx`（等于 artifactId）必须改成 `z-util :: xxx (描述)`。

---

## 日常发布流

### A. 修复 bug 后发补丁版本（如 1.0.23 → 1.0.24）

```bash
cd /Users/zifang/workplace/ceo_workplace/z-opc-foundation/z-util

# 0) 先确认工作树干净、无未推提交 —— 否则发出去的件和仓库对不上
git status -sb
git log --oneline origin/main..HEAD        # 必须为空

# 1) 升版本号。**只改根 pom 这一行**，全仓再无第二处硬编码
#    （早期文档那段"同步子模块"的 python 循环是多余的，实测 grep 仅 1 命中）
sed -i '' 's|<revision>1.0.23</revision>|<revision>1.0.24</revision>|' pom.xml
#    顺手把上一版的发版注释留着，说明这次为什么必须发新版（中央版本不可覆盖）

# 2) commit + push（**不要 commit .env / .gnupg**，两者都已被 gitignore）
git add pom.xml
git status --short                          # 只应有 pom.xml 一行
git commit -m "chore(release): revision bump 1.0.23 → 1.0.24"
git push origin main

# 3) 备签名环境。变量名对不上是头号坑：
#    wrapper 读的是 CENTRAL_GPG_PASSPHRASE，而 z-util/.env 里的键叫 GPG_PASSPHRASE，
#    wrapper 的 .env 自加载因此**不会**把变量填上，必须手工映射一次
export GNUPGHOME="$PWD/.gnupg"
set -a && . ./.env && set +a
export CENTRAL_GPG_PASSPHRASE="$GPG_PASSPHRASE"

# 4) 可选：先用 central-dryrun 验打包，但它**不验签名**，别拿它当签名预演
mvn -Pcentral-dryrun -pl z-util-dsl/z-util-parser/z-util-parser-json -am verify -DskipTests

# 5) 真发。74 模块，约 4~6 分钟出 deploymentId
mvn -Pcentral deploy -pl '!z-util-all' -DskipTests | tee /tmp/zutil-deploy-$(date +%m%d).log
grep -E "BUILD SUCCESS|deploymentId:" /tmp/zutil-deploy-*.log | tail -3

# 6) 等 5~17 分钟转 PUBLISHED。判终态只能认 deployments 列表：
#    /publisher/status 和 /publisher/deployment/list 都返回 500，deployment?id= 返回 404 HTML
#    Portal 凭证在 ~/.m2/settings.xml 的 <id>central</id> 条目（不走 z-util/.env 那份）
#    ⚠️ password 是正则的第 **2** 个捕获组，取 group(1) 会拿到用户名、结果是 401 "Invalid token"
U=$(python3 -c "import re;s=open('$HOME/.m2/settings.xml').read();print(re.search(r'<id>central</id>.*?<username>([^<]*)',s,re.S).group(1))")
P=$(python3 -c "import re;s=open('$HOME/.m2/settings.xml').read();print(re.search(r'<id>central</id>.*?<username>([^<]*)</username>.*?<password>([^<]*)',s,re.S).group(2))")
curl -s -u "$U:$P" "https://central.sonatype.com/api/v1/publisher/deployments" \
  | python3 -c "import sys,json;[print(x['deploymentId'],x['deploymentState']) for x in json.load(sys.stdin)['deployments']]"

# 7) 落地后必须回 repo1 反查，确认这次要修的东西真的进了中央件
curl -s -o /dev/null -w "%{http_code}\n" \
  "https://repo1.maven.org/maven2/io/github/yuku123/z-util-parser-json/1.0.24/z-util-parser-json-1.0.24.jar"
# 源码级反查（比看 jar 可靠，能直接读到改动那几行）：
curl -s -O "https://repo1.maven.org/maven2/.../z-util-parser-json-1.0.24-sources.jar"
unzip -oq z-util-parser-json-1.0.24-sources.jar && grep -n "<你要确认的改动>" com/zifang/util/json/JsonUtil.java
```

> **别在发版后改回 SNAPSHOT**：本仓 `revision` 一直是不带 `-SNAPSHOT` 的稳定号，
> 早期文档「第 5 步改回 SNAPSHOT」的做法已不适用，照做反而会让下次发版找不到匹配的行。

**消费方收口（别忘了）**：z-opc 等消费仓通过自己的 BOM 拿 z-util 版本，
**z-util 发新版不会自动传导**。1.0.23 那次就是：z-opc 经 z-boot-parent:1.2.4
仍解析到 `z-util-parser-json:1.0.19`，修复不生效。要在 z-opc 根 pom 的
`<dependencyManagement>` 里显式钉（本仓自有 DM 优先于 import 进来的 BOM）：

```xml
<dependency>
    <groupId>io.github.yuku123</groupId>
    <artifactId>z-util-parser-json</artifactId>
    <version>1.0.23</version>
</dependency>
<!-- parser-json 的 POM 依赖 dsl-kernel 同版本，dependencyManagement 对传递依赖同样生效，
     只钉 parser-json 会被 BOM 把 dsl-kernel 压回旧版造成运行期版本裂开 ⇒ 必须同笔钉 -->
<dependency>
    <groupId>io.github.yuku123</groupId>
    <artifactId>z-util-dsl-kernel</artifactId>
    <version>1.0.23</version>
</dependency>
```

改完务必实测，别只信 BUILD SUCCESS：

```bash
mvn dependency:tree -Dincludes=io.github.yuku123:z-util-parser-json,io.github.yuku123:z-util-dsl-kernel
# 两格都该是新版本；若仍显示旧版，说明钉版没压过 BOM
```

### B. 用户怎么用 z-util？

```xml
<dependency>
    <groupId>io.github.yuku123</groupId>
    <artifactId>z-util-core</artifactId>
    <version>1.0.23</version>
</dependency>
```

内部项目里**不要指望改个属性就能统一抬版本**：z-opc 的 `util.version` 早已是**死键**
（全仓 0 处引用），z-util 版本实际由 `z-boot-parent` 的 BOM 供。要抬版本得在消费仓
根 pom 的 `<dependencyManagement>` 里显式钉，见上面「消费方收口」。

---

## 常见问题（z-util 特定）

### Q: z-util-all 失败 "Could not find artifact xxx"

**原因**：聚合 pom 的 `dependencyManagement` 引用了 `${revision}` 解析后的具体版本号，deploy 时 mvn 找不到这些 staging 还没同步的 jar。

**解决**：发版命令带 `-pl '!z-util-all'` 跳过聚合 pom。聚合 pom 本来就不一定要发到中央。

### Q: z-util-ioc 总在最后一个模块失败？

**原因**：`z-util-ioc` 依赖 `z-util-proxy` + `z-util-aop`，是 reactor 排序中靠后的模块。中央发布插件在每个模块 deploy 时把所有 staging 文件 zip 成 157MB bundle 上传，**所有模块 deploy 完才触发一次完整 bundle 上传**。看起来像 IOC 失败，实际是**最后阶段的全局 bundle 上传**出问题。

**解决**：
1. `waitUntil=uploaded` + `waitMaxTime=1800` 已配置（不会 5 分钟就放弃）
2. 1.0.23 那次在当前环境下 74 模块**全部 SUCCESS、零 FAILURE 零 SKIPPED**，
   整轮约 4~6 分钟出 deploymentId；末尾那条 `Uploaded bundle successfully` 就是全局上传那一步，
   它成功即代表没有这个问题

### Q: GPG 签名失败 / 报 `Sorry, we are in batchmode - can't get input`？

**这是 1.0.23 那次实际踩到的坑**，报错信息完全看不出真实原因，容易误判成"口令错"。

真实原因：**公钥环与私钥分家在不同目录**。仓内 `.gnupg/` 只有 `pubring.kbx`，
**没有 `private-keys-v1.d/`**；私钥文件实际在 `~/.gnupg/private-keys-v1.d/`。
gpg 只有在同一个 GNUPGHOME 里同时看到公钥与私钥才认得这对密钥。

```bash
# 诊断：分别看两个目录有没有另一半
ls /Users/zifang/workplace/ceo_workplace/z-opc-foundation/z-util/.gnupg/           # 有 pubring.kbx，无 private-keys-v1.d
ls ~/.gnupg/private-keys-v1.d/                                                     # 有 .key 文件

# 修复：把私钥补进仓内 keyring（该目录已被 .gitignore 第 88 行排除，安全）
cd /Users/zifang/workplace/ceo_workplace/z-opc-foundation/z-util
mkdir -p .gnupg/private-keys-v1.d
cp ~/.gnupg/private-keys-v1.d/*.key .gnupg/private-keys-v1.d/
chmod 700 .gnupg .gnupg/private-keys-v1.d && chmod 600 .gnupg/private-keys-v1.d/*.key

# 发版时必须显式指定 homedir，否则 wrapper 里的 gpg 用默认 ~/.gnupg（那里没有公钥环）
export GNUPGHOME="$PWD/.gnupg"
```

验证签名真的通了（**不要只看 BUILD SUCCESS**）：

```bash
# a) 手工签一个探针
echo test > /tmp/sig.txt
gpg --batch --yes --pinentry-mode loopback --passphrase "$CENTRAL_GPG_PASSPHRASE" \
    --local-user A403FCCFD7EF5DA6 --armor --detach-sign --output /tmp/sig.asc /tmp/sig.txt
ls -la /tmp/sig.asc        # 几百字节即成功；0 字节 = 没签成

# b) 让 Maven 真签一遍（verify 阶段签名，但**不会上传**）
mvn -Pcentral -pl z-util-dsl/z-util-parser/z-util-parser-json -am verify -DskipTests
ls z-util-dsl/z-util-parser/z-util-parser-json/target/*.asc
# 期望 4 个：jar / sources / javadoc / pom
```

**口令变量名对不上**：wrapper 读 `CENTRAL_GPG_PASSPHRASE`，而 `z-util/.env` 里的键叫
`GPG_PASSPHRASE`。wrapper 的 `.env` 自加载**填不上**这个变量，必须手工映射：

```bash
set -a && . ./.env && set +a
export CENTRAL_GPG_PASSPHRASE="$GPG_PASSPHRASE"
```

**`central-dryrun` 不验签名**：该 profile 只跑 javadoc + 打包，实测产物里没有 `.asc`。
拿它当签名预演会得到虚假的安全感，签名必须用上面的 `mvn -Pcentral ... verify` 验。

### Q: 怎么判断发版真的成功了？

**只认 `/api/v1/publisher/deployments` 列表。** 实测另两个端点都不好用：

| 端点 | 实测结果 |
|------|---------|
| `/api/v1/publisher/deployments` | ✅ 200，返回 deploymentId + deploymentState |
| `/api/v1/publisher/status?id=...` | ❌ 500 |
| `/api/v1/publisher/deployment/list` | ❌ 500 |
| `/api/v1/publisher/deployment?id=...` | ❌ 404，且返回的是 Next.js 的 HTML 页面（会被 `json.load` 炸掉） |

```bash
curl -s -u "$U:$P" "https://central.sonatype.com/api/v1/publisher/deployments" \
  | python3 -c "import sys,json;[print(x['deploymentId'],x['deploymentState']) for x in json.load(sys.stdin)['deployments']]"
```

**还要再往 repo1 验一层**：Portal 显示 `PUBLISHED` 只代表校验通过，
不等于你能确认这次要修的东西真的在件里。1.0.23 那次就是靠拉 sources jar 反查
`JsonUtil.java` 才确认修复进去了。

### Q: 已经发布的版本发现有问题怎么办？

**Maven Central 不可物理删除**（immutable），只能另发新版。

1. 改代码 → bump revision → 重走「日常发布流 A」
2. 把旧版在 Central Portal 上 `Deprecate`
3. **通知消费方抬版本**——这是最容易漏的一步，见「消费方收口」

### Q: 部署失败后想再发同一版本？

**先到 Central Portal UI 删掉 FAILED 的 deployment**（Drop 按钮），否则会报 "Component already exists"。

或者直接升版本号（如 1.0.23 → 1.0.24），绕开冲突——中央版本不可覆盖，通常后者更省事。

---

## 🛡️ 安全清单

- ⚠️ `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_SECRET` / `GPG_PASSPHRASE` 都是**长期凭证**，**绝不能贴到对话、命令行历史或日志里**
- ⚠️ 本仓库 `.env`（Central token + GPG 口令，**明文**）和 `.gnupg/`（**含私钥**）已被 `.gitignore` 排除。
      提交前例行 `git status --short` + `git diff --cached` 确认暂存区里没有它们
- ⚠️ **不要用 `mvn install` 覆盖已发布版本号的坐标。** 这么干过：本地 m2 的 `1.0.22`
      被换成了自建包，本地行为与 CI/其他机器**静默分叉**，且因为坐标合法、构建照过，
      极难发现。要验证自家改动请用 SNAPSHOT 坐标；手滑了只能删目录后
      `mvn dependency:get -Dartifact=<g>:<a>:<v> -Dtransitive=false` 取回中央原件
- ⚠️ 在浏览器登录 Central Portal 注册 User Token 时，**复制的 Secret 立即存到密码管理器**，不要留在剪贴板
- ⚠️ GPG 公钥虽可公开，但私钥泄露请立即 `gpg --delete-secret-keys A403FCCFD7EF5DA6` 并重新生成
- ⚠️ 手工探私钥时把口令留在 `export` 里的会话，结束后 `unset CENTRAL_GPG_PASSPHRASE`

---

## 附录：发布历史

| 版本 | 日期 | 中央仓库 deployment id | 状态 |
|------|------|----------------------|------|
| 1.0.2 | 2026-08-10（sandbox 试发） | 377654ce-03ac-4dd7-bfbe-1d938d23d2db | PUBLISHED（不完整，仅 metadata） |
| 1.0.3 | 2026-08-10（用户终端真发） | 056e55d3-a424-403f-9639-377a0472c4ce | **PUBLISHED（完整 jar/sources/javadoc + GPG）** |
| 1.0.4 ~ 1.0.21 | — | — | 中间若干次发布，历史未逐笔记录 |
| 1.0.22 | 2026-10-10 | — | PUBLISHED。⚠️ **不含** `JsonUtil.convertValue` 的 `Object.class` 短路（已用 `javap -c` 反汇编确认），消费方别钉它 |
| **1.0.23** | **2026-10-14** | `5d1426da-75b8-44d0-ae87-8cc3f3fdd690` | **PUBLISHED**（74 模块全绿，约 11 分钟转终态）。含 convertValue 修复；strategy / sensitive 两模块随此版**首次**进中央件 |

**1.0.23 那次的完整链路**（可作为下次发版的样板）：
改 `revision` → commit/push → 备 `GNUPGHOME` + `CENTRAL_GPG_PASSPHRASE` →
`mvn -Pcentral deploy -pl '!z-util-all' -DskipTests` → 轮询 `/publisher/deployments` 到 `PUBLISHED` →
回 repo1 拉 sources jar 反查修复 → **通知消费方抬版本**。

---

**详细通用流程与 AI 避坑指南**：见 [发布指引.md](./02-release-guide.md)
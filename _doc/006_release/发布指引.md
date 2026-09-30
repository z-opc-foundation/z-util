# 发布指引 — 让任何 AI 助手把 Java 项目从零发布到 Maven Central

> 适用对象：AI 助手 / 人类工程师，目标是把一个 **多模块 Java 库** 第一次成功发布到 Maven Central 主仓库。
> 文档版本：基于 2026-08 在 z-util 项目上的实战经验整理。
> 预计完成时间：首版 1.5~3 小时（含外部账号注册、namespace 审核等待），后续版本每次 5~15 分钟。

---

## 目录

0. [心智模型（Maven Central 到底要什么）](#0-心智模型maven-central-到底要什么)
1. [决策清单（动手前必答）](#1-决策清单动手前必答)
2. [外部一次性准备（必须人工，AI 无法代办）](#2-外部一次性准备必须人工ai-无法代办)
3. [本地工程配置](#3-本地工程配置)
4. [POM 必须补的元数据](#4-pom-必须补的元数据)
5. [Central 发布 plugin 配置](#5-central-发布-plugin-配置)
6. [一键发布脚本（推荐）](#6-一键发布脚本推荐)
7. [执行发布](#7-执行发布)
8. [发布后验证](#8-发布后验证)
9. [常见错误排错](#9-常见错误排错)
10. [后续维护：升版本、再发布、撤销版本](#10-后续维护升版本再发布撤销版本)
11. [AI 助手避坑指南（重要）](#11-ai-助手避坑指南重要)

---

## 0. 心智模型（Maven Central 到底要什么）

Maven Central 对一个 release 的硬性要求：

| 维度 | 要求 |
|------|------|
| **坐标** | `groupId:artifactId:version` 三元组唯一 |
| **namespace** | groupId 必须先在 Sonatype Central Portal 申请并通过验证 |
| **签名** | 每个 `jar` / `pom` 必须用 GPG / GnuPG 签名（`.asc` 文件） |
| **元数据** | POM 必须有 `name` / `description` / `url` / `licenses` / `developers` / `scm` |
| **源码 / 文档** | 必须附带 `*-sources.jar` 和 `*-javadoc.jar` |
| **license** | 必须明确（推荐 Apache-2.0 / MIT） |
| **groupId 不重** | `groupId:artifactId:version` 三元组在中央仓库里不能已有同名（即使发布失败，残骸也会占位） |
| **Central Portal 验证** | 上传后做 GPG + 元数据 + 完整性校验，通过后才 release 到主仓库 |

不满足任何一条 → publish 失败。失败后**留下的 staging 残骸会阻塞同一个坐标的二次发布**，必须先清理或升版本。

---

## 1. 决策清单（动手前必答）

**这 5 个决策错了要返工，先确认再动手：**

### Q1：用什么 groupId？
- **官方**：如果你拥有 `yourbrand.com`，用 `com.yourbrand.*`（中央仓库要求 DNS TXT 验证）
- **省事**：用 GitHub 用户名 → `io.github.<username>`（用 GitHub Pages 验证，**最快**）
- **企业**：用公司域名

> 决策点：z-util 最初想用 `com.zifang`，但没有 zifang.com 域名，**改用 `io.github.yuku123`** 后 5 分钟通过审核。

### Q2：要不要支持 SNAPSHOT？
- 0.x 阶段 → 只发正式版，不发 SNAPSHOT
- 1.x+ 阶段 → SNAPSHOT 用 Sonatype Snapshots 仓库，**不影响主仓库**

> Maven Central 主仓库不接受 SNAPSHOT！SNAPSHOT 必须走 https://central.sonatype.com/repository/maven-snapshots/。

### Q3：依赖签名环境用哪个？
- **本机 GPG 密钥环** → `~/.gnupg`，污染用户家目录
- **项目本地 GPG 密钥环** → 推荐：`./.gnupg` + `GNUPGHOME=./.gnupg`，**隔离且可被 `.gitignore`** 排除

### Q4：发布模式用哪种？
- **OSS Sonatype（旧）**：`mvn nexus-staging-maven-plugin` + 手动 close + release
- **Central Portal（新，2024+）**：`central-publishing-maven-plugin` 自动 release

**强烈推荐 Central Portal 新模式**（本指引基于此）。

### Q5：Maven groupId 怎么管？
- 单项目 → 直接写 `<groupId>`
- 多模块 → 用 `${revision}` property，父 POM 统一管版本（z-util 用的就是这种）

---

## 2. 外部一次性准备（必须人工，AI 无法代办）

### 2.1 注册 Sonatype Central Portal 账号

1. 打开 https://central.sonatype.com/
2. 右上角 `Sign in` → `Sign in with GitHub`（用你的 GitHub 账号）
3. 右上角用户名 → `Profile` → `User Token` → `Generate User Token`（建议命名 `my-project-maven-publish`）
4. **保存 `Username` 和 `Secret`**（Secret 只显示一次）

> AI 助手不会替你完成 GitHub OAuth——必须在你自己浏览器登录授权。
> ⚠️ Secret 是**长期 API 凭证**，**不要贴到 AI 对话里**（写到本地 `.env` 文件）。

### 2.2 申请 namespace（groupId 验证）

**最简方式：GitHub Pages 验证**（适用 `io.github.<username>`）

1. 在 GitHub 创建 `<username>/<username>.github.io` 仓库（如 `yuku123/yuku123.github.io`，没有就用 `Use this template`）
2. Central Portal → 左侧 `Namespaces` → `Add Namespace` → 输入 `<groupId>`（如 `io.github.yuku123`）
3. 系统生成 `Verification Key`（一段随机字符串）
4. 在 `<username>.github.io` 仓库创建 `static/central-validation/<groupId>.txt`，内容粘贴 Verification Key，提交 push
5. 等 5~10 分钟 GitHub Pages 部署完
6. 回 Central Portal 点 `Verify`，状态变绿即通过

**DNS TXT 验证**（适用 `com.<yourbrand>`）：

```
_dnsauth.<your-subdomain>  TXT  "<Central Portal 给的 verification key>"
```

子域要单独放在 DNS 上，等 DNS 传播（5 分钟 ~ 24 小时）。

### 2.3 生成 GPG 密钥对

```bash
brew install gnupg  # macOS；其他系统见系统包管理器
mkdir -p .gnupg && chmod 700 .gnupg
echo "pinentry-mode loopback" > .gnupg/gpg.conf
chmod 600 .gnupg/gpg.conf
export GNUPGHOME="$(pwd)/.gnupg"

# 生成 RSA 4096 密钥（passphrase 自己记到密码管理器）
gpg --batch --pinentry-mode loopback --passphrase "$YOUR_PASSPHRASE" \
    --quick-generate-key "Your Name <your@email>" default default 4096

KEY_ID=$(gpg --list-secret-keys --keyid-format LONG your@email 2>/dev/null | grep -oE '[A-F0-9]{16}' | head -1)
echo "KEY_ID=$KEY_ID"

# 上传公钥到 keyserver（Maven Central 会从这拉公钥校验签名）
gpg --keyserver hkps://keys.openpgp.org --send-keys "$KEY_ID"
# 也可同步一份到 keyserver.ubuntu.com 作为备份
gpg --keyserver keyserver.ubuntu.com --send-keys "$KEY_ID"

# 公钥同步可能延迟 30 分钟 ~ 24 小时
```

**Verify**：到 https://keys.openpgp.org 搜你的邮箱，确认能看到自己的公钥。

### 2.4 在 GitHub 仓库 `.gitignore` 排除敏感文件

```gitignore
.env            # Central Portal User Token + GPG passphrase
.gnupg/         # GPG 密钥环（含私钥）
```

---

## 3. 本地工程配置

### 3.1 `~/.m2/settings.xml` 添加 Central 服务器凭证

```xml
<settings>
  <servers>
    <!-- 已有：GitHub Packages（如果有） -->
    <server>
      <id>github</id>
      <username>你的 GitHub 用户名</username>
      <password>ghp_你的 GitHub PAT（仅 publish:packages）</password>
    </server>

    <!-- 新增：Maven Central（Sonatype Central Portal User Token） -->
    <server>
      <id>central</id>
      <username>${env.CENTRAL_USERNAME}</username>
      <password>${env.CENTRAL_TOKEN}</password>
    </server>
  </servers>
</settings>
```

`${env.XXX}` 占位符让 Maven 从环境变量读，避免硬编码密码到 settings.xml。

### 3.2 本地环境变量（写入 `~/.zshrc` 或脚本里临时 export）

```bash
export CENTRAL_USERNAME="你的 User Token Username"
export CENTRAL_TOKEN="你的 User Token Secret"
export CENTRAL_GPG_PASSPHRASE="你的 GPG 私钥密码"
```

---

## 4. POM 必须补的元数据

Maven Central 校验会检查这些字段，缺一个就 publish 失败：

```xml
<project>
    <groupId>io.github.yuku123</groupId>     <!-- 已在 Central Portal 通过验证 -->
    <artifactId>z-util</artifactId>
    <version>${revision}</version>           <!-- 多模块用 property -->
    <packaging>pom</packaging>

    <!-- ↓↓↓ 必填 ↓↓↓ -->
    <name>z-util (human-readable name, 不能等于 artifactId)</name>
    <description>面向日常 Java 开发的多模块工具库集合（...）</description>
    <url>https://github.com/yourname/your-repo</url>

    <licenses>
        <license>
            <name>MIT License</name>
            <url>https://opensource.org/licenses/MIT</url>
            <distribution>repo</distribution>
        </license>
    </licenses>

    <developers>
        <developer>
            <id>your-github-id</id>
            <name>Your Name</name>
            <email>your@email.com</email>
            <organization>your-org</organization>
            <organizationUrl>https://github.com/yourname</organizationUrl>
            <timezone>+08:00</timezone>
            <roles>
                <role>owner</role>
            </roles>
        </developer>
    </developers>

    <scm>
        <connection>scm:git:git://github.com/yourname/your-repo.git</connection>
        <developerConnection>scm:git:ssh://git@github.com/yourname/your-repo.git</developerConnection>
        <url>https://github.com/yourname/your-repo</url>
        <tag>HEAD</tag>
    </scm>

    <issueManagement>
        <url>https://github.com/yourname/your-repo/issues</url>
        <system>GitHub Issues</system>
    </issueManagement>

    <!-- 子模块必须有自己的 <name>（不能继承自 parent 的 <artifactId>），如： -->
</project>
```

**子模块 POM 必须有的字段**：

```xml
<project>
    <parent>...</parent>
    <artifactId>z-util-core</artifactId>
    <name>z-util :: core (human-readable, must differ from artifactId)</name>
    <description>...</description>  <!-- 推荐，但非强必填 -->
</project>
```

---

## 5. Central 发布 plugin 配置

```xml
<project>
    <properties>
        <!-- Central Portal 凭证 -->
        <centralPortalUrl>https://central.sonatype.com</centralPortalUrl>
    </properties>

    <build>
        <plugins>
            <!-- ★ 关键：源码包在 verify 阶段打包（保证 deploy 带 sources.jar） -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>3.2.1</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <phase>verify</phase>   <!-- 不是 install，是 verify -->
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <!-- ====================================================== -->
        <!-- Maven Central 发布 profile                             -->
        <!-- mvn deploy -Pcentral                                  -->
        <!-- 默认 mvn deploy 仍走其他仓库（如 GitHub Packages）       -->
        <!-- ====================================================== -->
        <profile>
            <id>central</id>
            <properties>
                <maven.javadoc.skip>false</maven.javadoc.skip>
                <central.gpg.skip>false</central.gpg.skip>
            </properties>

            <distributionManagement>
                <repository>
                    <id>central</id>
                    <url>https://central.sonatype.com</url>
                </repository>
                <snapshotRepository>
                    <id>central</id>
                    <url>https://central.sonatype.com</url>
                </snapshotRepository>
            </distributionManagement>

            <build>
                <plugins>
                    <!-- 中央发布场景下，禁用 maven-deploy-plugin（避免与 central-publishing 重复） -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-deploy-plugin</artifactId>
                        <version>3.1.4</version>
                        <configuration>
                            <skip>true</skip>
                        </configuration>
                    </plugin>

                    <!-- javadoc：Maven Central 强制要求，doclint=none 屏蔽 Java 8+ HTML5 校验 -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-javadoc-plugin</artifactId>
                        <version>3.11.2</version>
                        <configuration>
                            <doclint>none</doclint>
                            <failOnError>false</failOnError>
                            <failOnWarnings>false</failOnWarnings>
                            <source>${compile.version}</source>
                        </configuration>
                        <executions>
                            <execution>
                                <id>attach-javadocs</id>
                                <phase>verify</phase>
                                <goals><goal>jar</goal></goals>
                            </execution>
                        </executions>
                    </plugin>

                    <!-- GPG 签名：passphrase 用 -Dgpg.passphrase 传入 -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-gpg-plugin</artifactId>
                        <version>3.2.7</version>
                        <configuration>
                            <skip>${central.gpg.skip}</skip>
                        </configuration>
                        <executions>
                            <execution>
                                <id>sign-artifacts</id>
                                <phase>verify</phase>
                                <goals><goal>sign</goal></goals>
                            </execution>
                        </executions>
                    </plugin>

                    <!-- Central Portal 上传（替代老 nexus-staging） -->
                    <plugin>
                        <groupId>org.sonatype.central</groupId>
                        <artifactId>central-publishing-maven-plugin</artifactId>
                        <version>0.7.0</version>
                        <extensions>true</extensions>
                        <configuration>
                            <publishingServerId>central</publishingServerId>
                            <tokenAuth>true</tokenAuth>
                            <autoPublish>true</autoPublish>
                            <!-- uploaded=只等上传成功；published=等正式发布（可能 5~15 分钟） -->
                            <waitUntil>uploaded</waitUntil>
                            <!-- 30 分钟超时，避免 mvn 提前放弃 -->
                            <waitMaxTime>1800</waitMaxTime>
                            <waitPollingInterval>10</waitPollingInterval>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>

        <!-- dry-run profile：本地验证 POM 元数据 + javadoc 生成（不上传） -->
        <profile>
            <id>central-dryrun</id>
            <properties>
                <maven.javadoc.skip>true</maven.javadoc.skip>
                <central.gpg.skip>true</central.gpg.skip>
            </properties>
            <distributionManagement>
                <repository><id>central</id><url>https://central.sonatype.com</url></repository>
                <snapshotRepository><id>central</id><url>https://central.sonatype.com</url></snapshotRepository>
            </distributionManagement>
        </profile>
    </profiles>
</project>
```

---

## 6. 一键发布脚本（推荐）

把发布脚本提交到仓库根目录，**不要在对话里反复粘贴 export 命令**。

### 6.1 `.env`（**已被 .gitignore 排除**）

```bash
# Sonatype Central Portal User Token
CENTRAL_USERNAME=xxx
CENTRAL_TOKEN=xxx

# GPG 私钥 passphrase
CENTRAL_GPG_PASSPHRASE=xxx

# 密钥元信息（自动写入，记录用）
GPG_KEY_ID=xxx
GPG_USER_NAME="Your Name <your@email>"
```

### 6.2 `deploy_maven_center.sh`（一键发布）

参见本仓库的 `deploy_maven_center.sh`，它会：
1. 自动 `source .env` 加载凭证
2. 检查 mvn / gpg / settings.xml
3. 设置 `GNUPGHOME=./.gnupg`
4. 跑 `mvn -B deploy -Pcentral -DskipTests -Dgpg.passphrase=$CENTRAL_GPG_PASSPHRASE`
5. 判断 `BUILD SUCCESS` + `Uploaded bundle successfully` 才算成功
6. 可选 `verify` 子命令查 Maven Central 同步状态

子命令：
```bash
./deploy_maven_center.sh gpg-init   # 首次：生成 GPG 密钥并写 .env
./deploy_maven_center.sh publish    # 实际发布
./deploy_maven_center.sh verify     # 验证已发布的 artifact
```

---

## 7. 执行发布

### 7.1 准备稳定版本号

Maven Central **不接受 SNAPSHOT**——发布前必须去掉 `-SNAPSHOT` 后缀。

```bash
# 方式 1: versions-maven-plugin（多模块友好）
mvn versions:set -DnewVersion=1.0.0 -DprocessAllModules=true -DgenerateBackupPoms=false
mvn versions:commit

# 方式 2: 用 ${revision} 的项目，直接 sed
sed -i 's|<revision>1.0.0-SNAPSHOT</revision>|<revision>1.0.0</revision>|' pom.xml
# 子模块若有硬编码版本，用 python 批量：
python3 -c "
import os, re
for d, _, files in os.walk('.'):
    for f in files:
        if f != 'pom.xml': continue
        p = os.path.join(d, f)
        with open(p) as fh: c = fh.read()
        new = c.replace('<version>1.0.0-SNAPSHOT</version>', '<version>1.0.0</version>')
        if new != c:
            with open(p, 'w') as fh: fh.write(new)
"
```

### 7.2 Dry-run 验证（不上传，先确保 POM 没问题）

```bash
mvn -B clean verify -Pcentral-dryrun -DskipTests
```

预期：
- 所有 module `BUILD SUCCESS`
- 每个 module target/ 下生成 `*.jar` / `*-sources.jar` / `*-javadoc.jar`
- 没有 `*.asc` 签名（被 GPG skip）

### 7.3 实际发布

```bash
./deploy_maven_center.sh publish
# 或手动：
mvn -B clean deploy -Pcentral -DskipTests -Dgpg.passphrase="$CENTRAL_GPG_PASSPHRASE"
```

预期 mvn 输出：
```
[INFO] --- central-publishing:0.7.0:publish (injected-central-publishing) @ z-util ---
[INFO] Using Central baseUrl: https://central.sonatype.com
[INFO] Using credentials from server id central in settings.xml
[INFO] Using Usertoken auth, with namecode: <你的 username>
[INFO] Uploaded bundle successfully, deployment name: <Deployment>,
       deploymentId: <uuid>. Deployment will publish automatically
[INFO] BUILD SUCCESS
```

**一定要看到 `Uploaded bundle successfully` + `BUILD SUCCESS` 才算成功**。如果只 BUILD SUCCESS 但没 "Uploaded" → bundle 实际上传失败。

### 7.4 ⚠️ 不要在 sandbox / 受限环境里跑

macOS `sandbox-exec` 限制单次出站 POST ≤ 64KB。**`mvn deploy` 的 157MB bundle zip 上传必然失败**。

如果 AI 助手在 sandbox 里跑 → 必然失败。必须让用户在**他自己 macOS 终端**（无 sandbox）跑。

---

## 8. 发布后验证

### 8.1 等 Central Portal 自动 publish

`autoPublish=true` 后，Central Portal 会自动 validate + publish 到主仓库。**通常 5~15 分钟**。

监控状态：
- Central Portal UI: https://central.sonatype.com/publishing/deployments
- API: `GET https://central.sonatype.com/api/v1/publisher/deployments`
  （用 User Token 鉴权：`Authorization: UserToken base64(username:secret)`）

### 8.2 验证 Maven Central 主仓库同步

```bash
# 搜索 groupId 下所有 artifact
curl -s "https://search.maven.org/solrsearch/select?q=g:<groupId>&rows=20&wt=json" | python3 -c "
import json, sys
d = json.load(sys.stdin)
print(f'命中数: {d[\"response\"][\"numFound\"]}')
for x in d['response']['docs'][:5]:
    print(f'  - {x.get(\"g\")}:{x.get(\"a\")}:{x.get(\"latestVersion\")}')
"

# 直接下载某个 jar 验证
curl -I "https://repo1.maven.org/maven2/<groupId_with_slashes>/<artifact>/<version>/<artifact>-<version>.jar"

# 验证 GPG 签名
curl -O "https://repo1.maven.org/maven2/<...>/<artifact>-<version>.jar"
curl -O "https://repo1.maven.org/maven2/<...>/<artifact>-<version>.jar.asc"
gpg --verify <artifact>-<version>.jar.asc <artifact>-<version>.jar
# 预期：Good signature from "Your Name <your@email>"
```

---

## 9. 常见错误排错

### 9.1 `Component with package url '...' already exists`

**原因**：之前失败 publish 的残骸留在 staging。**中央仓库不接受覆盖**。

**解决**：
1. 选项 A：到 Central Portal UI → Deployments → 选 FAILED 的 deployment → `Drop`
2. 选项 B：**升版本号**（如 `1.0.0` → `1.0.1`）绕开冲突

### 9.2 `Javadocs must be provided but not found in entries`

**原因**：跳过了 javadoc 生成。
**解决**：去掉 `-Dmaven.javadoc.skip=true`，让 `maven-javadoc-plugin` 跑。

### 9.3 `Missing signature for file: xxx`

**原因**：跳过了 GPG 签名。
**解决**：去掉 `-Dcentral.gpg.skip=true`，传 `-Dgpg.passphrase=<passphrase>`。

### 9.4 `name equals artifactId` validation 失败

**原因**：子模块 `<name>` 写了和 `<artifactId>` 一样的字符串。
**解决**：写成 `"z-util :: core (简短描述)"` 这种"前缀 + 描述"形式。

### 9.5 `400 Bad Request / Invalid token` 在 sandbox 里跑 mvn

**原因**：sandbox 限制单次出站 POST ≤ 64KB（macOS sandbox-exec 硬约束）。
**解决**：必须在用户自己的 macOS 终端跑（不走 sandbox）。

### 9.6 GPG 签名验证失败

```bash
# 检查本地 GPG 是否能找到公钥
gpg --list-secret-keys --keyid-format LONG your@email

# 公钥同步到 keys.openpgp.org
gpg --keyserver hkps://keys.openpgp.org --send-keys $KEY_ID

# 等 30 分钟 ~ 24 小时同步
```

### 9.7 Maven Central 上 `Bundling - mvn could not find artifact xxx`

**原因**：模块间 SNAPSHOT 时间戳版本号漂移。
**解决**：去掉 SNAPSHOT，发正式版（`1.0.0`，`1.0.1` ...）。

### 9.8 `z-util-all` 这种聚合 pom 在 deploy 时报 "Could not find artifact"

聚合 pom 的 dependencyManagement 引用其他模块的具体版本号，deploy 时 mvn-deploy-plugin 会去找这些引用，但 staging 还没同步完。
**解决**：用 `mvn deploy -Pcentral -pl '!z-util-all'` 跳过聚合模块（聚合 pom 本来就不一定要发到中央）。

---

## 10. 后续维护：升版本、再发布、撤销版本

### 10.1 升版本号（去掉 SNAPSHOT）

```bash
sed -i 's|<revision>1.0.0-SNAPSHOT</revision>|<revision>1.0.1</revision>|' pom.xml
python3 -c "
import os
for d, _, files in os.walk('.'):
    for f in files:
        if f != 'pom.xml': continue
        p = os.path.join(d, f)
        with open(p) as fh: c = fh.read()
        new = c.replace('<version>1.0.0-SNAPSHOT</version>', '<version>1.0.1</version>')
        if new != c: open(p, 'w').write(new)
"
```

### 10.2 重新发布

```bash
./deploy_maven_center.sh publish
```

### 10.3 改回 SNAPSHOT 继续开发

```bash
sed -i 's|<revision>1.0.1</revision>|<revision>1.0.2-SNAPSHOT</revision>|' pom.xml
python3 -c "..."  # 同上，逆方向
```

### 10.4 撤销 / deprecate 已发布版本

**Maven Central 不允许物理删除**（保护 immutable），但可以做：
1. Central Portal UI → Deployments → 选已发布 deployment → `Deprecate`
2. 发一个修复版（如 `1.0.2`），在 GitHub Release 写明 "v1.0.1 has a bug, use v1.0.2"

---

## 11. AI 助手避坑指南（重要）

> 这部分是**给 AI 助手看的**，总结实际踩过的坑。

### 11.1 不要做的事

❌ **不要把 User Token / GPG passphrase 贴到对话里**
- 对话可能被记录、被分享、被截屏
- 解决：写到本地文件（`.env`），AI 助手用 `cat .env` 或 `source .env` 读取

❌ **不要在 sandbox / 受限环境跑 mvn deploy**
- 64KB 出站限制会让 157MB bundle 必然失败
- 让用户在**他自己终端**（无 sandbox）跑

❌ **不要跳过 javadoc 或 GPG 测试**
- 第一次必须真签名 + 真生成 javadoc
- skip 路径容易"看起来成功"但 deployment 上传后失败

❌ **不要试图用 401 Bad Request 重试循环解决**
- 401 大概率是认证错误（中央 Portal 用 `Authorization: UserToken base64(user:secret)` 而不是 `Bearer xxx`）
- 看到 401 先停下来查 endpoint 格式

❌ **不要假设中央发布插件会自动清理 staging 残骸**
- FAILED 的 staging 会阻塞同名坐标
- 必须 UI 上 Drop 或升版本

❌ **不要把 `gpg.passphrase` 写到 settings.xml**
- 中央仓库用 `tokenAuth=true`（不是 gpg 凭据）
- gpg passphrase 只通过 `-Dgpg.passphrase=$XXX` 临时传

### 11.2 必须做的事

✅ **先 dry-run 验证 POM 元数据完整性**
- `mvn -B clean verify -Pcentral-dryrun -DskipTests` 跑一遍
- 每个 module target/ 应生成 jar/sources/javadoc 三件套

✅ **先查 deployment 列表确认 namespace 干净**
- API: `GET https://central.sonatype.com/api/v1/publisher/deployments`
- 必须没有同名坐标的 FAILED 残骸

✅ **每次发新版本都升版本号**
- 不要重发同名版本

✅ **GPG 公钥上传多个 keyserver**
- `keys.openpgp.org` 是主用
- `keyserver.ubuntu.com` 是备用
- 同步需要 30 分钟 ~ 24 小时

✅ **CENTRAL_USERNAME / CENTRAL_TOKEN / CENTRAL_GPG_PASSPHRASE 用环境变量**
- `${env.CENTRAL_USERNAME}` 在 settings.xml 里占位
- 不要硬编码

### 11.3 Debug 信息收集

```bash
# 1. 看 deployment 状态
curl -H "Authorization: UserToken base64(user:secret)" \
  https://central.sonatype.com/api/v1/publisher/deployments

# 2. 看 mvn 完整日志
mvn -B deploy -Pcentral -X | tee /tmp/deploy.log

# 3. 看本地 staging
ls -la target/central-staging/
ls -la target/central-publishing/

# 4. 验证 GPG
gpg --list-keys your@email
gpg --keyserver hkps://keys.openpgp.org --recv-keys $KEY_ID
```

### 11.4 给用户的提示模板

当 AI 助手卡住时，向用户清晰表达：

```
❓ 需要你做的（外部依赖，AI 无法代办）：

1. 在浏览器登录 Sonatype Central Portal（GitHub OAuth）
2. 去 Profile 生成 User Token，把 Username + Secret 写到 ./.env
   （不要贴到对话里！）
3. 第一次：跑 ./deploy_maven_center.sh gpg-init
   （生成 GPG 密钥，输入 passphrase，记住）
4. 把 passphrase 写到 ./.env 的 CENTRAL_GPG_PASSPHRASE=
5. 在你自己 macOS 终端（不在 sandbox 里）跑：
   ./deploy_maven_center.sh publish

⚠️ sandbox 限制 64KB 出站 POST，157MB bundle 必然失败。
```

---

## 附录 A：版本时间线（z-util 实战参考）

| 时间 | 状态 |
|------|------|
| T+0   | 决定 groupId（`io.github.yuku123`） |
| T+5min  | 用户在浏览器注册 Sonatype + GitHub Pages 验证通过 |
| T+10min | `brew install gnupg` 完成；gpg-init.sh 生成密钥 + 上传 keyserver |
| T+20min | 改造 POM（中央元数据 + central profile + 39 个 name 字段） |
| T+30min | 本地 dry-run：38 module BUILD SUCCESS，3 件套 jar/sources/javadoc 全齐 |
| T+45min | 第一次真发：sandbox 限制 64KB POST 失败 |
| T+1h   | 第二次在用户终端发：5min 超时（默认 waitMaxTime 太短） |
| T+1h+  | 调到 waitMaxTime=1800 + 升版本 1.0.2 → 1.0.3 绕开残骸 |
| T+1.5h | BUILD SUCCESS + Uploaded bundle，deploymentId 出现 |
| T+1.5h+10min | Central Portal 状态 PUBLISHING → PUBLISHED |
| T+1.5h+20min | Maven Central 主仓库同步完成，artifact 可下载 |
| T+1.5h+25min | GPG 签名验证通过 |

---

## 附录 B：相关链接

- [Sonatype Central Portal](https://central.sonatype.com/)
- [Central Portal 发布 API 文档](https://central.sonatype.com/api/v1/publisher/)（部分公开）
- [Maven Central 发布指南（官方）](https://central.sonatype.com/publish/publish-portal-maven)
- [central-publishing-maven-plugin GitHub](https://github.com/sonatype/central-publishing-maven-plugin)
- [Maven coordinates 规则](https://maven.apache.org/guides/mini/guide-naming-conventions.html)
- [keys.openpgp.org 公钥服务](https://keys.openpgp.org/)

---

**最后更新**：2026-08-10，基于 z-util 1.0.3 实战首发经验。
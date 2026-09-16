# z-util 发布到 Maven Central 操作手册

> 适用版本：z-util 1.0.x 及以上
> 目标仓库：[Maven Central](https://central.sonatype.com/) 主仓库
> GroupId：`io.github.yuku123`（已从原 `com.zifang` 迁移，无域名验证负担）
> 发布模式：Central Portal 新流程（`central-publishing-maven-plugin` 0.7.0）

---

## ⚡ 一句话总结

```bash
# 1) 首次：生成 GPG 密钥（只需要做一次）
./deploy_maven_center.sh gpg-init

# 2) 升版本号到稳定版（去掉 -SNAPSHOT）
sed -i 's|<revision>1.0.4-SNAPSHOT</revision>|<revision>1.0.4</revision>|' pom.xml
python3 -c "..."  # 同步子模块

# 3) 在你自己 macOS 终端（**不在 sandbox 里**）跑：
bash deploy_maven_center.sh publish

# 4) 5~15 分钟后验证：
bash deploy_maven_center.sh verify
```

**完整通用流程、AI 助手避坑指南、GPG 配置、POM 元数据模板**等详见同目录的 [发布指引.md](./发布指引.md)。

---

## 📚 目录

- [z-util 项目特定差异](#z-util-项目特定差异)
- [z-util 发布相关的仓库文件](#z-util-发布相关的仓库文件)
- [跳过哪些模块（已配置）](#跳过哪些模块已配置)
- [POM 关键配置](#pom-关键配置)
- [日常发布流](#日常发布流)
- [常见问题（z-util 特定）](#常见问题z-util-特定)

---

## z-util 项目特定差异

z-util 相比典型 Java 项目有以下特殊点：

| 特点 | 说明 |
|------|------|
| **多模块结构** | 41 个 Maven 模块（25 个发布 + 8 个聚合 pom + z-util-zex 沙盒） |
| **版本管理** | 父 POM 用 `<revision>` property，子模块用 `${revision}` |
| **GPG 密钥** | KEY_ID = `9D064F0E20AC7385`（yuku123@qq.com，已上传 keys.openpgp.org） |
| **跳过模块** | `z-util-zex`（沙盒）、`z-util-all`（聚合 pom，deploy 时间戳冲突） |
| **凭证隔离** | `.env` + `.gnupg/`（项目本地），全部 `.gitignore` 排除 |

---

## z-util 发布相关的仓库文件

```
/Users/zifang/workplace/idea_workplace/z-util/
├── 发布指引.md                  # 通用发布指引（AI / 人类都能用）
├── RELEASE_TO_MAVEN_CENTRAL.md  # 本文件（z-util 特定）
├── pom.xml                       # 中央元数据 + central profile（已配）
├── .env                          # Central Portal User Token + GPG passphrase（已 gitignore）
├── .gnupg/                       # GPG 密钥环（已 gitignore）
├── deploy_maven_center.sh        # 一键发布脚本（gpg-init / publish / verify）
├── .gitignore                    # 含 .env / .gnupg / central-staging
└── README.md                     # 项目 README（含 Maven 依赖示例）
```

| 文件 | 作用 | 是否 commit |
|------|------|----------|
| `pom.xml` | 中央元数据 + central profile + 子模块 name | ✅ commit |
| `.env` | Central Token + GPG passphrase | ❌ gitignore |
| `.gnupg/` | GPG 密钥环（含私钥） | ❌ gitignore |
| `deploy_maven_center.sh` | 一键发布脚本 | ✅ commit |
| `RELEASE_TO_MAVEN_CENTRAL.md` | 本手册 | ✅ commit |
| `发布指引.md` | 通用发布指引 | ✅ commit |

---

## 跳过哪些模块（已配置）

`deploy_maven_center.sh` 默认 `-pl '!z-util-all'`：

| 模块 | 原因 |
|------|------|
| `z-util-zex` | 沙盒模块，仅做 playground，从根 `<modules>` 排除 |
| `z-util-all` | 聚合 pom（`packaging=pom`），其 `dependencyManagement` 引用其他模块的 SNAPSHOT 时间戳版本号。deploy 时 mvn 找不到这些 jar，导致"Could not find artifact xxx" 失败 |

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
<url>https://github.com/yuku123/z-util</url>

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
    <connection>scm:git:git://github.com/yuku123/z-util.git</connection>
    <url>https://github.com/yuku123/z-util</url>
</scm>

<revision>1.0.4-SNAPSHOT</revision>  <!-- 升级时改成 1.0.4 / 1.0.5 ... -->

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

### A. 修复 bug 后发补丁版本（如 1.0.3 → 1.0.4）

```bash
cd /Users/zifang/workplace/idea_workplace/z-util

# 1) 升版本号（去掉 -SNAPSHOT）
sed -i 's|<revision>1.0.4-SNAPSHOT</revision>|<revision>1.0.4</revision>|' pom.xml
python3 << 'PY'
import os
for d, _, files in os.walk('.'):
    for f in files:
        if f != 'pom.xml': continue
        p = os.path.join(d, f)
        with open(p) as fh: c = fh.read()
        new = c.replace('<version>1.0.4-SNAPSHOT</version>', '<version>1.0.4</version>')
        if new != c: open(p, 'w').write(new)
PY

# 2) commit（**不要 commit .env / .gnupg**）
git add pom.xml */pom.xml
git commit -m "release: bump version to 1.0.4"
git tag v1.0.4
git push origin main v1.0.4

# 3) 真发（必须在用户自己终端跑，sandbox 不行）
bash deploy_maven_center.sh publish

# 4) 等 5~15 分钟后验证
bash deploy_maven_center.sh verify

# 5) 改回 SNAPSHOT 准备下次开发
sed -i 's|<revision>1.0.4</revision>|<revision>1.0.5-SNAPSHOT</revision>|' pom.xml
python3 -c "..." # 反向同步子模块
git add pom.xml */pom.xml
git commit -m "build: bump to 1.0.5-SNAPSHOT"
```

### B. 用户怎么用 z-util？

```xml
<dependency>
    <groupId>io.github.yuku123</groupId>
    <artifactId>z-util-core</artifactId>
    <version>1.0.3</version>
</dependency>
```

或在 z-opc 等内部项目里 `<util.version>1.0.3</util.version>` + `${util.version}` 引用（z-opc 已切到 1.0.3）。

---

## 常见问题（z-util 特定）

### Q: z-util-all 失败 "Could not find artifact xxx"

**原因**：聚合 pom 的 `dependencyManagement` 引用了 `${revision}` 解析后的具体版本号（如 `1.0.4-SNAPSHOT`），deploy 时 mvn 找不到这些 staging 还没同步的 jar。

**解决**：`deploy_maven_center.sh` 已用 `-pl '!z-util-all'` 跳过聚合 pom。聚合 pom 本来就不一定要发到中央。

### Q: z-util-ioc 总在最后一个模块失败？

**原因**：`z-util-ioc` 依赖 `z-util-proxy` + `z-util-aop`，是 reactor 排序中靠后的模块。中央发布插件在每个模块 deploy 时把所有 staging 文件 zip 成 157MB bundle 上传，**所有模块 deploy 完才触发一次完整 bundle 上传**。看起来像 IOC 失败，实际是**最后阶段的全局 bundle 上传**出问题。

**解决**：
1. **必须在用户自己终端跑**（不受 sandbox 64KB 限制）
2. `waitUntil=uploaded` + `waitMaxTime=1800` 已配置（不会 5 分钟就放弃）

### Q: GPG 找不到公钥，签名验证失败？

`z-util` 用的是 EDDSA 密钥 `9D064F0E20AC7385`。本地 `.gnupg/` 已 gitignore 排除，但已 commit 的密钥 ID 信息在 `.env`。

```bash
# 验证本地密钥
export GNUPGHOME=/Users/zifang/workplace/idea_workplace/z-util/.gnupg
gpg --list-secret-keys --keyid-format LONG 1340947819@qq.com

# 验证公钥已上传到 keyserver
gpg --keyserver hkps://keys.openpgp.org --recv-keys 9D064F0E20AC7385
```

### Q: 已经发布的版本怎么撤回？

**Maven Central 不可物理删除**（保护 immutable）。

操作：
1. Central Portal UI → Deployments → 选已发布的 deployment → `Deprecate`
2. 发修复版（如 1.0.4），在 GitHub Release 写明"v1.0.3 has bug, use v1.0.4"

### Q: 部署失败后想再发同一版本？

**先到 Central Portal UI 删掉 FAILED 的 deployment**（Drep 按钮），否则会报 "Component already exists"。

或者直接升版本号（如 1.0.3 → 1.0.4），绕开冲突。

---

## 🛡️ 安全清单

- ⚠️ `CENTRAL_TOKEN` / `CENTRAL_GPG_PASSPHRASE` 都是**长期凭证**，**绝不能贴到对话里**
- ⚠️ 本仓库 `.env` 和 `.gnupg/` 已被 `.gitignore` 排除，但**还是会被本地工具读取**，请妥善保管
- ⚠️ 在浏览器登录 Central Portal 注册 User Token 时，**复制的 Secret 立即存到密码管理器**，不要留在剪贴板
- ⚠️ GPG 公钥虽可公开，但私钥泄露请立即 `gpg --delete-secret-keys KEY_ID` 并重新生成

---

## 附录：发布历史

| 版本 | 日期 | 中央仓库 deployment id | 状态 |
|------|------|----------------------|------|
| 1.0.2 | 2026-08-10（sandbox 试发） | 377654ce-03ac-4dd7-bfbe-1d938d23d2db | PUBLISHED（不完整，仅 metadata） |
| 1.0.3 | 2026-08-10（用户终端真发） | 056e55d3-a424-403f-9639-377a0472c4ce | **PUBLISHED（完整 jar/sources/javadoc + GPG）** |

---

**详细通用流程与 AI 避坑指南**：见 [发布指引.md](./发布指引.md)
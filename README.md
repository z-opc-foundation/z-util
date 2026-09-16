# z-util

> 面向日常 Java 开发的多模块工具库集合 —— 按需引用、按需升级。

[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuku123/z-util-all.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.yuku123/z-util-all)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)
[![JDK](https://img.shields.io/badge/JDK-8%2B-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue.svg)](https://maven.apache.org/)
[![Modules](https://img.shields.io/badge/modules-30%2B-purple.svg)](#模块清单)

`z-util` 是一组 **Maven 多模块** 的 Java 工具库，当前稳定版 **`1.0.9`**，由统一的父 POM 管理版本与依赖。

- **整体引入**：使用 `z-util-all` 一次性获取所有能力
- **按需引入**：单独引入某个子模块，最小化依赖体积

---

## 目录

- [模块清单](#模块清单)
- [快速开始](#快速开始)
- [架构设计](#架构设计)
- [构建与测试](#构建与测试)
- [发布与分发](#发布与分发)
- [贡献](#贡献)
- [常见问题](#常见问题)

---

## 模块清单

### 基础核心

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-core](#z-util-core) | 核心工具类（字符串、集合、IO、安全、反射、并发等） | `com.zifang.util.core.*` |
| [z-util-cache](#z-util-cache) | 缓存实现（本地缓存、分布式缓存） | `com.zifang.util.cache.*` |
| [z-util-validation](#z-util-validation) | 参数校验工具 | `com.zifang.util.validation.*` |

### 容器与 AOP

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-ioc](#z-util-ioc) | 轻量级 IoC 容器（自研 Spring 替代） | `com.zifang.util.ioc.*` |
| [z-util-aop](#z-util-aop) | AOP 切面支持 | `com.zifang.util.aop.*` |
| [z-util-proxy](#z-util-proxy) | 动态代理与字节码操作 | `com.zifang.util.proxy.*` |

### 数据与解析

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-jdbc](#z-util-jdbc) | 极简 ORM 框架（自研 MyBatis 替代） | `com.zifang.util.db.*` |
| [z-util-dsl](#z-util-dsl) | 自研 DSL 解析框架 | `com.zifang.util.dsl.*` |
| [z-util-parser](#z-util-parser) | 多格式解析器（JSON/XML/YAML/CSV/TOML） | `com.zifang.util.parser.*` |
| [z-util-expr](#z-util-expr) | 表达式引擎（EL/Groovy/JS/Lua/SQL） | `com.zifang.util.expr.*` |

### 数学与机器学习

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-math](#z-util-math) | 数学计算与数据分析（对标 NumPy/Pandas） | `com.zifang.util.numpy.*` |
| [z-util-ml](#z-util-ml) | 机器学习工具集（神经网络、树模型、聚类等） | `com.zifang.util.ml.*` |

### 网络与通信

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-http](#z-util-http) | HTTP 客户端与 Netty 网络编程 | `com.zifang.util.http.*` |
| [z-util-workflow](#z-util-workflow) | 工作流引擎 | `com.zifang.util.workflow.*` |

### 办公与媒体

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-office](#z-util-office) | Office 文档处理（Excel/PDF/Word） | `com.zifang.util.office.*` |
| [z-util-media](#z-util-media) | 图像处理与验证码生成 | `com.zifang.util.media.*` |
| [z-util-visualization](#z-util-visualization) | 可视化图表（Swing） | `com.zifang.util.visuallization.*` |

### 运维与监控

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-monitor](#z-util-monitor) | JVM/线程池/OS/网络监控 | `com.zifang.util.monitor.*` |
| [z-util-devops](#z-util-devops) | 运维工具（Docker/Git/Nexus/GitHub API） | `com.zifang.util.devops.*` |

### 工具与扩展

| 模块 | 说明 | 包路径 |
|------|------|--------|
| [z-util-distribute](#z-util-distribute) | 分布式 ID 生成（Snowflake/NanoId/UUID v7） | `com.zifang.util.distributes.*` |
| [z-util-source](#z-util-source) | 字节码分析与源码解析 | `com.zifang.util.source.*` |
| [z-util-cli](#z-util-cli) | 命令行解析（对标 Apache Commons CLI） | `com.zifang.util.cli.*` |
| [z-util-ch](#z-util-ch) | 中文工具（拼音/身份证/金额） | `com.zifang.util.ch.*` |

### 练习场（不发布）

| 模块 | 说明 |
|------|------|
| [z-util-zex](#z-util-zex) | 个人练习与学习样例 |

---

## 快速开始

### Maven 依赖

```xml
<!-- 引入全部模块 -->
<dependency>
    <groupId>io.github.yuku123</groupId>
    <artifactId>z-util-all</artifactId>
    <version>1.0.9</version>
</dependency>

<!-- 或按需引入单个模块 -->
<dependency>
    <groupId>io.github.yuku123</groupId>
    <artifactId>z-util-core</artifactId>
    <version>1.0.9</version>
</dependency>
```

### Gradle 依赖

```gradle
// 引入全部模块
implementation 'io.github.yuku123:z-util-all:1.0.9'

// 或按需引入单个模块
implementation 'io.github.yuku123:z-util-core:1.0.9'
```

### 验证安装

```bash
# 从 Maven Central 下载验证
curl -I https://repo1.maven.org/maven2/io/github/yuku123/z-util-core/1.0.9/z-util-core-1.0.9.jar
```

---

## 模块详解

### z-util-core

核心工具类库，提供日常开发最常用的能力：

- **字符串**：`StringUtil`（判空、截取、格式化）
- **集合**：`CollectionUtil`、`Venn`、`Tuples`
- **IO**：`FileUtil`、`ZipUtil`、`JarUtil`
- **安全**：`core.security.*`（Base64/MD5/RSA/DES/JWT）
- **反射**：`ReflectUtil`、`BeanUtil`
- **并发**：`NameThreadFactory`（自研线程工厂）
- **设计模式**：`pattern.*`（责任链、命令、工厂、状态机等）
- **日志**：`Logs` 工具类

### z-util-jdbc

自研极简 ORM 框架，对标 MyBatis / Spring Data：

```java
// 注解方式
@Select("SELECT * FROM user WHERE id = :id")
User findById(@Param("id") long id);

// SQL 构造
String sql = new SqlBuilder().select("*").from("user").where("id = ?", id).build();
```

**核心能力**：
- 数据源管理：`DataSourceContext` / `DataSourceManager`
- 注解驱动：`@Select` / `@Insert` / `@Update` / `@Delete`
- 事务支持：`@Transactional` + `TransactionManager`
- 代码生成：`JpaStratege` / `MybaitsStratige`

### z-util-parser

基于 ANTLR 的多格式解析器：

| 格式 | 模块 | 说明 |
|------|------|------|
| JSON | z-util-parser-json | JSON 解析与生成 |
| XML | z-util-parser-xml | XML 解析与绑定 |
| YAML | z-util-parser-yaml | YAML 解析 |
| CSV | z-util-parser-csv | CSV 解析 |
| TOML | z-util-parser-toml | TOML 解析 |
| INI | z-util-parser-ini | INI 配置解析 |
| Proto | z-util-parser-proto | Protocol Buffers |

### z-util-math

对标 NumPy/Pandas 的 Java 数学计算库：

```java
// 多维数组
NdArray arr = Numpy.zeros(3, 4);
NdArray result = arr.dot(other);  // 矩阵乘法

// 数据分析
DataFrame df = Pandas.readCsv("data.csv");
Series mean = df.mean();
```

**核心能力**：
- `NdArray`：多维数组运算
- `DataFrame`：二维表格数据处理
- `Linalg`：线性代数（SVD/QR/Cholesky 分解）
- 统计、插值、窗口函数

### z-util-ml

机器学习工具集（教学/练手性质）：

- **神经网络**：`Sequential` / `Dense` / `Conv2d` / `LSTM` / `Transformer`
- **经典网络**：`NeuralNetwork` / `Layer` / `Neuron`
- **损失函数**：`MSELoss` / `CrossEntropyLoss` / `BinaryCrossEntropyLoss`
- **优化器**：`SGD` / `Adam` / `Adagrad`
- **树模型**：`DecisionTree` / `RandomForest` / `XGBoost`
- **聚类**：`KMeans` / `DBSCAN` / `GMM`
- **降维**：`PCA` / `tSNE` / `UMAP`

### z-util-office

Office 文档处理：

```java
// Excel
List<Map<String, Object>> data = ExcelUtils.readExcel(file);

// PDF
PdfConverter.toImage(pdfFile, outputDir);
String text = PdtExtractor.extract(pdfFile);
```

### z-util-distribute

分布式 ID 生成：

```java
// Snowflake
SnowflakeIdWorker id = new SnowflakeIdWorker(1L, 1L);
long next = id.nextId();

// NanoId
String nanoId = NanoId.fast();  // URL 友好的短 ID

// UUID v7（时间有序）
UuidV7 uuid = UuidV7.fast();
```

### z-util-devops

运维工具：

- **Docker**：`DockerClient` / `DockerCommandClient`
- **Git**：`GitClient` / `JGitExecutor`
- **GitHub API**：`GithubApiWrapper`（repo/pr/issue/release/action）
- **Nexus**：`NexusComponentManager`

### z-util-zex

个人练习场，集中放置学习样例：

- **bust**：《码出高效》等书章节样例
- **sort**：各种排序算法实现
- **leetcode**：LeetCode 题解
- **guava**：Guava 组件实战
- **interview**：面试手写代码

> 代码不一定经过严格测试，主要用作「自留地」与「面试复盘」。

---

## 架构设计

### 模块依赖图

```
                       z-util-core
                            │
   ┌──────────┬─────────────┼──────────────┬────────────┐
   │          │             │              │            │
z-util-cache  z-util-proxy  z-util-validation  z-util-parser  ...
               │   │
        ┌──────┴───┴──────┐
        │   z-util-ioc    │  (proxy + aop)
        │   z-util-aop    │
        └─────────────────┘

z-util-ml / z-util-math / z-util-workflow / z-util-http
                            │
                       z-util-core
```

### 设计原则

1. **能不引就不引**：优先自研实现，减少第三方依赖
2. **统一版本管理**：所有版本号在父 POM `dependencyManagement` 管理
3. **低耦合高内聚**：每个模块专注于一个领域

### 自研替代表

| 领域 | 自研实现 | 替代的第三方 |
|------|----------|--------------|
| 字符串/判空 | `StringUtil` / `Assert` | commons-lang3 |
| 集合/元组 | `CollectionUtil` / `Tuples` | Guava Collections |
| 文件/IO | `FileUtil` / `ZipUtil` | commons-io |
| 安全/加密 | `core.security.*` | commons-codec |
| Bean 拷贝 | `BeanUtil` / `ReflectUtil` | commons-beanutils |
| XML | `XmlUtil` | dom4j |
| 对象池 | `ObjectPool` | commons-pool2 |
| JWT | `core.security.jwt.*` | nimbus-jose-jwt |

---

## 构建与测试

### 环境要求

- JDK 8+
- Maven 3.6+

### 常用命令

```bash
# 完整构建
mvn clean install

# 构建单个模块（含依赖）
mvn clean install -pl z-util-core -am

# 跳过测试
mvn clean install -DskipTests

# 运行特定模块测试
mvn test -pl z-util-cache

# 依赖分析
mvn dependency:analyze
```

---

## 发布与分发

### Maven Central（推荐）

```bash
# 1) 升版本号
mvn versions:set -DnewVersion=1.0.9
mvn versions:commit

# 2) 发布
./deploy_maven_center.sh publish

# 3) 验证（5~15 分钟后）
./deploy_maven_center.sh verify

# 4) 改回 SNAPSHOT
mvn versions:set -DnewVersion=1.0.10-SNAPSHOT
mvn versions:commit
```

### GitHub Packages（备选）

```bash
mvn clean deploy
```

> 详细发布流程见 [发布指引.md](./发布指引.md)

---

## 贡献

欢迎以任何形式参与贡献：

- **提 Issue**：报告 Bug / 提出功能建议
- **发 PR**：修复 Bug / 添加新功能
- **完善示例**：各模块的 README 与测试用例

### PR 流程

1. Fork 仓库
2. 新建分支：`git checkout -b feature/your-feature`
3. 提交改动：`git commit -m "feat: 描述你的改动"`
4. Push 到 fork：`git push origin feature/your-feature`
5. 创建 Pull Request

---

## 常见问题

### 构建相关

1. **构建失败**
   - 确认 JDK 8+、Maven 3.6+
   - 清理本地缓存：`rm -rf ~/.m2/repository/io/github/yuku123`
   - 重新构建：`mvn clean install`

2. **测试超时**
   - 部分测试依赖网络/数据库
   - 跳过测试：`mvn install -DskipTests`

3. **依赖冲突**
   - 父 POM 已统一管理版本，子模块不写 `<version>`

### 发布相关

4. **GPG 签名失败**
   - 公钥需上传：`gpg --keyserver hkps://keys.openpgp.org --send-keys $KEY_ID`
   - 同步需 30 分钟 ~ 24 小时

5. **Central Portal 401**
   - 使用 User Token（非 Bearer）
   - 确认 `settings.xml` 中 `<server><id>central</id>` 配置正确

---

## 项目状态

| 维度 | 状态 |
|------|------|
| **最新发布** | `1.0.9`（Maven Central） |
| **开发线** | `1.0.10-SNAPSHOT` |
| **JDK** | 8+ |
| **Maven** | 3.6+ |
| **License** | MIT |
| **维护者** | yuku123 |

---

## 致谢

本项目的实现参考了以下开源项目：

| 项目 | 用途 |
|------|------|
| [ANTLR](https://www.antlr.org/) | 多格式解析器语法框架 |
| [Apache POI / PDFBox](https://poi.apache.org/) | Office 文档处理 |
| [OkHttp](https://square.github.io/okhttp/) | HTTP 客户端 |
| [Netty](https://netty.io/) | 网络编程 |
| [Guava](https://github.com/google/guava) | 集合/缓存设计参考 |
| [JGit](https://www.eclipse.org/jgit/) | Git 操作 |

---

## 许可证

本项目采用 **MIT License** — 详见 [LICENSE](./LICENSE) 文件。


## 文档目录

本项目文档统一收口在 `_doc/` 下:

- [`_doc/001_arch/`](_doc/001_arch/) — 架构文档 (项目总览 / 模块结构 / 接口清单 / DB schema / 前端 / 能力 / roadmap):
  - [`RELEASE_TO_MAVEN_CENTRAL.md`](_doc/001_arch/RELEASE_TO_MAVEN_CENTRAL.md)
  - [`pivot.md`](_doc/001_arch/pivot.md)
  - [`resource.md`](_doc/001_arch/resource.md)
  - [`target.md`](_doc/001_arch/target.md)
  - [`wire-format.md`](_doc/001_arch/wire-format.md)
  - [`发布指引.md`](_doc/001_arch/发布指引.md)

- [`_doc/003_script/`](_doc/003_script/) — 运维脚本:
  - [`11.1.sh`](_doc/003_script/11.1.sh)
  - [`11.3.sh`](_doc/003_script/11.3.sh)
  - [`11.4.sh`](_doc/003_script/11.4.sh)
  - [`11.7.sh`](_doc/003_script/11.7.sh)
  - [`11.8.sh`](_doc/003_script/11.8.sh)
  - [`12.1.sh`](_doc/003_script/12.1.sh)
  - [`12.4.sh`](_doc/003_script/12.4.sh)
  - [`12.7.sh`](_doc/003_script/12.7.sh)
  - [`13.1.sh`](_doc/003_script/13.1.sh)
  - [`deploy_maven_center.sh`](_doc/003_script/deploy_maven_center.sh)
  - [`install-settings.sh`](_doc/003_script/install-settings.sh)
  - [`loopDir.sh`](_doc/003_script/loopDir.sh)

- [`_doc/004_skill/`](_doc/004_skill/) — AI skill 定义:
  - [`CLAUDE.md`](_doc/004_skill/CLAUDE.md)

各文档详细说明见各子目录。

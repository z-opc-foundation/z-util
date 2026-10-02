# z-util

> 一人公司基座的底层 Java 工具库 —— 45 个模块 · Java 8 口径 · 按需引用、按需升级。

[![Maven Central](https://img.shields.io/maven-central/v/io.github.yuku123/z-util-core.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.yuku123/z-util-core)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/compile%20target-JDK%208-orange.svg)](https://adoptium.net/)
[![Modules](https://img.shields.io/badge/modules-45-purple.svg)](#-模块清单)

`z-util` 是 z-opc-foundation 的**基础库**：组织内其余仓（z-boot starter 家族、z-opc、z-ctc、z-mq、z-gw…）
要用到的字符串/集合/IO/加密/JWT、JSON 与多格式解析、表达式与对象整形 DSL、数据源与动态查询、缓存、
IoC/AOP/代理、二进制序列化、监控、Office、图像、数学与 ML，全部收在这里。

它解决的具体问题是**同一件小事在全站被写 N 遍**。收编之后只有一份口径：
分页只剩 `core.meta.page.PageResult`，JWT 只剩 `core.jwt.Jwt`（z-ctc 的 `JwtUtil` 已改成门面委托它），
分布式 ID 只剩 `distributes.sequence.*`，"库里取数 → 内存 SQL → 对象整形"这条链只剩
`z-util-jdbc` + `z-util-expr-sql` + `z-util-expr-obj`。

---

## 📋 基本信息

| 字段 | 值 |
|------|-----|
| **仓库** | `z-util`（org: z-opc-foundation 的基础库；本仓 remote 仍是个人仓 `github.com:yuku123/z-util.git`） |
| **Maven 坐标** | 根聚合 `io.github.yuku123:z-util:${revision}`（`packaging=pom`）；子件 `io.github.yuku123:z-util-<module>:${revision}` |
| **当前版本** | `1.0.14`（根 POM `<properties><revision>`，CI-friendly versions + flatten-maven-plugin `resolveCiFriendliesOnly`） |
| **父项目** | `io.github.yuku123:z-boot-parent:1.0.21`（`<relativePath/>` 留空，parent 在 repo1 不在磁盘；2026-09 消费者轮从 1.0.19 抬上来） |
| **Maven Central** | 已发布：实测 `1.0.14` 下 **46 个构件的 `.pom` 全部 HTTP 200**（根 `z-util` + 45 个 reactor 模块）；唯一 404 是 `z-util-zex`（不进默认 reactor、不发布） |
| **模块数** | 根 POM `<modules>` 25 个条目；展开 `parser`(8) / `expr`(6) / `serialize`(6) 三个聚合件后共 **45 个模块 POM = 41 个 jar + 4 个 pom** |
| **运行口径** | Java 8（`compile.version=8`，各模块 `maven.compiler.source/target=8`；产物可直接被 Spring Boot 2.7 系消费） |
| **测试运行 JDK** | 全量 `mvn test` 需 JDK 9+，组织口径用 **JDK 17**（原因见「🧪 测试」） |
| **默认端口** | 无（这是库，不监听端口）。`z-util-http` 的 `AllPathHttpServer` 基于 JDK `com.sun.net.httpserver`，端口由调用方传入 |
| **最近更新** | 2026-09-30 |

---

## 🧩 模块清单

下表**逐条对齐**根 `pom.xml` 的 `<modules>`（22 条，按 POM 内声明顺序），职责取自各模块 POM 的 `<name>`。
`z-util-dsl` / `z-util-serialize` / `z-util-devops` 是聚合 POM；`z-util-dsl` 家族收编了 `z-util-dsl-kernel`（原 `z-util-dsl` jar）+ `z-util-expr` + `z-util-parser`，子模块单列在下面子表。

| 模块（`<module>` 顺序） | 类型 | 职责 | 关键入口（实测存在的类） |
|------|------|------|------|
| `z-util-core` | jar | 基础工具：集合/字符串/IO/并发/加密/JWT/限流/熔断/调度 | `StringUtil` `CollectionUtil` `BeanUtil` `ReflectUtil` `FileUtil` `ZipUtil` `JarUtil` `XmlUtil` `Assert` `StopWatch` `EventBus`；`jwt.Jwt`/`Claims`/`HmacSha256`；`encrypt.AesUtil`/`RsaUtil`/`MD5Utils`/`Base64Utils`/`HMAC`/`ApiSignUtil`/`Base62`；`ratelimit.SlidingWindowRateLimiter`/`TokenBucketRateLimiter`；`resilience.CircuitBreaker`/`Bulkhead`/`TimeLimiter`；`meta.page.PageResult`；`lang.concurrency.NameThreadFactory`；`pattern.*`（chain/command/composite/factory/state/pool/event/spi） |
| `z-util-office` | jar | 基于 POI / PDFBox 的 Word / Excel / PDF / PPT 工具 + 模板引擎 + 链式门面（详见模块 README） | `excel.ExcelUtils`（`readFirstSheet` / `readAllSheets` / `writeCell` / `writeRow`）、`ExcelTemplate`；`word.U` + `WordTemplate` + `WordExtractor`；`pdf.PdfUtil` / `PdfOperator` / `PdfExtractor`；`ppt.PptUtils` + `PptTemplate`；`core.RoundTripAssert` / `core.OfficeFormat` / `core.OfficePipeline`（2026-10-02 新增 builder：open→读→改→saveAs/toBytes） |
| `z-util-media` | jar | 图像处理、验证码、GIF、二维码 | `CaptchaUtil`、`GifEncoder`/`GifBuilder`、`ColorUtil`、`graph/QRCode`（encoder + decoder） |
| `z-util-dsl` | **pom 聚合** | 语言处理家族：`z-util-dsl-kernel`（自研词法/语法/AST + **运行时动态加载 `.g4`**）+ `z-util-expr`（6 引擎）+ `z-util-parser`（8 格式） | kernel：`g4.DynamicLexer`、`DynamicParser`、`G4FileParser`、`ASTFactory`、`ASTNode`、`token.Lexer` |
| `z-util-wf` | **pom 聚合** | 内存内工作流家族：`z-util-wf-kernel`（DAG 模型 + BPMN 解析 + runtime + persistence + interfaces）+ `z-util-wf-executor`（`z-util-wf-executor-java` + `z-util-wf-py`） | kernel：`conponents.WorkFlowApplication` / `WorkFlowApplicationContext` / `Task`；`config.WorkflowNode` / `Connector` / `Engine`；`bpmn.Bpmn` / `BpmnDiagram` / `BpmnProcess` / `BpmnModelConverter` / `BpmnXmlParser`；`engine.runtime.WorkflowRuntimeEngine` / `GatewayEvaluator` / `ExecutionResult`；`engine.interfaces.EngineFactory`（自注册，无 reactor cycle）；`persistence.FileWorkflowPersistencePlugin`；`json.WorkflowConfigurationSerializer` |
| `z-util-visualization` | jar | Swing 图表与算法可视化（包名历史拼写 `visuallization`） | `chart.*`、`swing.AlgoFrame`/`AlgoVisHelper`/`AlgoVisualizer`、`robot.*` |
| `z-util-jdbc` | jar | 数据源注册 + 多方言动态查询 + 内存 SQL + 极简 ORM | `context.DataSourceRegistry`/`DatasourceContextManager`/`PoolSpec`；`dialect.Dialects`/`MySqlDialect`/`PostgresDialect`/`H2Dialect`；`query.Query`/`Criteria`/`QueryCompiler`/`SqlTemplate`/`DynamicQuery`；`memory.InMemoryTables`；`respository.CrudRepository`；`@Select`/`@Insert`/`@Update`/`@Delete` + `@Transactional`/`TransactionManager`；`plugin.MyBatisPageInterceptor`；`generater.JpaStratege`/`MybaitsStratige` |
| `z-util-http` | jar | 注解式 HTTP 客户端 + curl 解析 + 简易 HTTP 服务 + SSE | `client.HttpExecutor`/`HttpClientFactory`/`HttpRequestInvocationHandler`（OkHttp）；`server.AllPathHttpServer`/`HttpServerBuilder`；`parser.curl.CurlParser`/`CurlBuilder`；`sse.SseParser`/`SseFrame`/`SseEvent` |
| `z-util-math` | jar | NumPy/Pandas 风格数据结构 | `numpy.Numpy`（`zeros` 等）/`NdArray`/`Linalg`/`random`；`pandas.Pandas`（`DataFrame(...)`/`Series(...)` 工厂）/`DataFrame`/`Series`/`matrix.Linalg`/`io.CSVReader`/`CSVWriter`/`interpolate`/`discretize` |
| `z-util-ml` | jar | 机器学习（教学/练手性质，98 个主源文件） | `nn.Sequential`/`Dense`/`Conv2d`/`LSTM`/`TransformerEncoder`；`loss.CrossEntropyLoss`、`nnet.MSELoss`；`optim.SGD`/`Adam`/`Adagrad`；`tree.DecisionTree`/`RandomForest`/`XGBoost`；`clustering.KMeans`/`DBSCAN`/`GMM`；`decomposition.PCA`/`tSNE`/`UMAP`；`rl.QLearning`；`ga`/`ensemble`/`association`/`anomaly` |
| `z-util-monitor` | jar | JVM / 线程池 / OS / 网络监控 + 指标导出 | `JvmMonitor`/`OsMonitor`/`NetMonitor`/`ThreadMonitor`、`ExecutorManager`/`FixedMonitorableExecutor`、`MetricsRegistry`/`MetricsCollector`/`MetricsSnapshot`、`HtmlExporter`/`JsonExporter`、`AlarmService`/`LogAlarmService`/`ThreadPoolOvertimeAlarmPolicy` |
| `z-util-devops` | **pom 聚合** | DevOps 三件：Git (JGit/Shell) + GitHub API、Docker 客户端、Nexus REST | 见下表 |
| `z-util-bc` | jar | 字节码 + 源码工具集（字节码模型/ASM 编织/内存编译；2026-10-02 收编 z-util-source 为源码面，compiler 孤岛死码随迁删除） | `bytecode.*` class 模型、`compile.CFJavaCompiler`/`MapClassLoader`、`weave.*`；`source.parser.SourceCodeParser`（javaparser）、`source.define.ByteCodeParser`/`ByteCodeGenerator`（契约）+ `parser.ByteCodeParserImpl`/`generator.ByteCodeGeneratorImpl`、`source.generator.JavaSourceGenerator` + `diff.ClassInfoDiffer`、`source.generator.info.ClassInfo`/`FieldInfo`/`MethodInfo`、`source.analyser.AnalysisContext` |
| `z-util-distribute` | jar | 分布式 ID：Snowflake / Segment / NanoId / UUID v7 | `sequence.SnowflakeIdWorker`（`(workerId, datacenterId)` → `nextId()`）、`Sequence`、`SegmentIdGenerator`、`NanoId`（`new NanoId(size, alphabet).next()`）、`UuidV7.next()`/`fromMillis`/`toUuid`、`SystemClock` |
| `z-util-proxy` | jar | JDK/CGLIB 动态代理 + 自研 class 文件字节码模型 | `CglibProxyFactory`/`CglibInterceptor`、`a.model.*`（`AbstractConstantPool`/`AttributeFactory`/`AccessFlagConvertor`）、`ByteCodeResolver`、内置反编译 demo（`a.decompile.*`） |
| `z-util-ch` | jar | 中文工具：拼音/身份证/金额/星期 | `PinyinGeneratorUtil`、`IdcardUtil`、`MoneyUtil`、`NumberChineseUtil`、`WeekUtil` |
| `z-util-cli` | jar | POSIX/GNU/Basic/Default 命令行解析 + OptionGroup + HelpFormatter | `CLI`、`CommandLine`、`CommandLineParser`、`BasicParser`/`GnuParser`/`DefaultParser`、`help.HelpFormatter` |
| `z-util-pattern` | **pom 聚合** | GoF + 行为模式 + 校验原语聚合件（`z-util-pattern-chain`/`-command`/`-composite`/`-event`/`-factory`/`-ioc`/`-memento`/`-pool`/`-register`/`-spi`/`-state`/`-stream`/`-strategy`/`-template`/`-visitor`/`-builder`/`-cache`/`-validation` 等子件各自成坐标） | validation 面：`annotation.NotNull`/`Length`/`Pattern`/`Range`/`Email` 与同名 `*Validator`（2026-10-02 自 `z-util-validation` 降级收编，零内部依赖） |
| `z-util-cache` | jar | **进程内**缓存：TTL + LRU / W-TinyLFU + builder + 装饰器 | `MemoryCache`、`WTinyLfuCache`、`BoundedCache`/`LruNode`、`LoadingCache`/`LoadingMemoryCache`/`CacheLoader`、`CacheBuilder`、`CacheManager`、`MeteredCache`、`TransactionalCache`、`CountMinSketch`、`Expiry`、`RemovalListener`（无 Redis/远端后端） |
| `z-util-ioc` | jar | 轻量 IoC 容器（Guice 形接口 + JSR-330 注解）+ AOP 集成 | `Injector`/`Module`/`Scopes`、`binder.Binder`/`DefaultBinder`/`ConstantBindingBuilder`、`context.ClassPathApplicationContext`、`core.DefaultBeanRegistry`/`BeanDefinition`、`inject.*`、`aop.AopModule`/`AopProxyPostProcessor`/`ClassMatcher` |
| `z-util-aop` | jar | `@Advise` / `Intercept` 拦截器 + `ProxyFactory`（零内部依赖） | `Advise`、`Intercept`、`ProxyFactory` |
| `z-util-serialize` | **pom 聚合** | 跨语言、schema-first、零拷贝编解码（6 个子模块） | 见下表 |
| `z-util-all` | **pom（umbrella）** | 只含 `<dependencyManagement>`，**没有任何 `<dependencies>`、不产出 jar** | 版本表覆盖 **20 个**模块（不是"全部模块"） |
| `z-util-zex` | jar（仅 `-Psandbox`） | 个人练习场：`bust`（《码出高效》章节）/`sort`/`leetcode`/`guava`/`interview`/`bytecode`/`disrupt` | 不在默认 reactor，Maven Central 实测 404 |

### `z-util-parser` 的 8 个子模块（`z-util-dsl` 子件，ANTLR `.g4` + 自研 `z-util-dsl-kernel` 动态解析）

| 子模块 | 内容 |
|--------|------|
| `z-util-parser-json` | `JSONParser` + `JsonObject`/`JsonArray` + `JsonUtil` + `BeautifyJsonUtils` + 序列化注解/`serializer.*`（依赖 `z-util-dsl-kernel`） |
| `z-util-parser-xml` | `XDocument`/`XElement`/`XAttribute` 模型 + XPath 查询 + 注解绑定（`AnnotationIntrospector`） |
| `z-util-parser-yaml` | `YamlG4Parser` + `SimpleYamlParser` + `SnakeYamlBackend` 门面 |
| `z-util-parser-csv` | `CsvReader`/`CsvWriter`/`CsvG4Parser` + `CsvCharsetDetector` |
| `z-util-parser-toml` | `TomlParser`/`TomlG4Parser`/`TomlDocument` |
| `z-util-parser-ini` | `IniParser`/`IniG4Parser`/`IniSection` |
| `z-util-parser-properties` | `PropertiesParser`/`PropertiesG4Parser`/`PropertiesModel` |
| `z-util-parser-proto` | `ProtoParser`/`ProtoG4Parser` + `ProtoMessage`/`ProtoEnum`/`ProtoRpc`/`ProtoService` |

### `z-util-expr` 的 6 个子模块（`z-util-dsl` 子件；每种语言一个入口，彼此不共用 SPI）

| 子模块 | 入口 | 用途 |
|--------|------|------|
| `z-util-expr-el` | `ElEvaluator` | 自研 EL，`${a}` / 算术 / 比较 / 三元 |
| `z-util-expr-js` | `ExpressionEngine` | 自研 JS 引擎雏形（lexer + instruction + PlayScript 参考） |
| `z-util-expr-groovy` | `GroovyExecutor` | Groovy 脚本扩展点 |
| `z-util-expr-lua` | `LuaExecutor` | Lua 脚本扩展点 |
| `z-util-expr-sql` | `VirtualTableEngine` | 内存 SQL：把行数据当表查，join / group by / 聚合 |
| `z-util-expr-obj` | `ObjEngine` | 对象整形：二维表 → 任意高维结构 |

OBJ 是"取数链"的最后一段，算子实测 15 个：`from` `select` `where` `order` `limit` `one` `group` `fold`
`map` `keyBy` `tree` `pivot` `unpivot` `get` `set`。它不带语法解析器 —— 程序形状就是 JSON
（保序 Map/List 树），任何给出这种树的解析器解析出来即可执行：

```java
// 库里取原始数据 → 内存 SQL 出二维表 → OBJ 抬成渲染要的结构
InMemoryTables mem = new InMemoryTables(dynamicQuery)
        .load("t_order", Query.select().from("t_order"))
        .index("t_order", "user_id");
Object doc = mem.shape(spec);          // spec 就是 JSON 树；new ObjEngine(source).shape(spec) 等价
```

### `z-util-devops` 的 3 个子模块（2026-10-01 由单 jar 拆分；三包实测零互耦，按需引用）

| 子模块 | 内容 |
|--------|------|
| `z-util-devops-git` | `git.operations.GitClient` + `jgit.JGitExecutor`/`shell.ShellExecutor`；`git.github.GithubApiWrapper` 与 `Repository`/`PullRequest`/`Issue`/`Release`/`Action`/`Organization`/`User` ApiWrapper（依赖 core + parser-json + jgit + github-api） |
| `z-util-devops-docker` | `docker.DockerClient`/`DockerCommandClient` + `docker.dto.*`（依赖 core + parser-json；不带 okhttp） |
| `z-util-devops-nexus` | `nexus.NexusComponentManager`（依赖 parser-json + okhttp） |

原 `z-util-devops` 聚合 jar 的消费者注意：它现在是 **pom 聚合件，不再产出 jar**——请按需改引上面三个子件（与 `z-util-parser` 家族同一用法）。

### `z-util-serialize` 的 6 个子模块

| 子模块 | 内容 |
|--------|------|
| `z-util-serialize-schema` | 纯注解：`@ZMessage` `@ZField` `@ZEnum` `@ZMap` `@ZOneof` + `FieldType`（零运行时依赖） |
| `z-util-serialize-core` | 线格式与编解码：`ZSerializer`/`ZDeserializer`/`FieldIndex`/`Header`/`IndexBuilder`/`Codec`/`CodecConfig`/`CodecRegistry` |
| `z-util-serialize-codegen` | 注解处理器 `ZSerializeProcessor`：生成零反射的 Serializer/Deserializer（模块自身编译禁用注解处理，见 commit `4921635d`） |
| `z-util-serialize-formats` | 互通转码器 `JsonTranscoder`（Jackson） |
| `z-util-serialize-benchmarks` | 与 Java 原生 / Kryo / Protobuf / Avro / JSON 对比（`ZSerializeVsKryoBench`） |
| `z-util-serialize-it` | 跨模块集成测试（`SimpleUser`） |

压缩实现目前只有 `GzipCompressor`（+ `AesGcmEncryptor`）；[`_doc/001_arch/wire-format.md`](_doc/001_arch/wire-format.md)
里写的 zstd / lz4 仍是设计目标，代码里还没有对应类。

---

## 🏗️ 项目结构

```
z-util/
├── pom.xml                # 根聚合 POM：继承 z-boot-parent:1.0.21，<revision> 统一版本，DM 覆盖 43 个自家构件
├── z-util-core/           # 基础库（23 个包：lang/io/jwt/encrypt/pattern/ratelimit/resilience/meta/schedule/...）
├── z-util-{aop,ioc,proxy,cache,validation,bc}/          # 容器、切面、代理、缓存、校验、字节码+源码工具
├── z-util-parser/         # 聚合 POM → 8 个格式子模块
├── z-util-expr/           # 聚合 POM → 6 个表达式子模块（含 -sql / -obj）
├── z-util-dsl/            # 自研 lexer/parser/AST，运行时加载 .g4
├── z-util-jdbc/           # 数据源/方言/动态查询/内存表/ORM/分页插件/代码生成
├── z-util-http/           # OkHttp 客户端 + curl 解析 + JDK http server + SSE
├── z-util-serialize/      # 聚合 POM → 6 个编解码子模块
├── z-util-{math,ml,workflow,office,media,visualization,monitor,devops,distribute,ch,cli}/
├── z-util-all/            # 只有 <dependencyManagement> 的 umbrella pom（不产出 jar）
├── z-util-zex/            # 练习场：只在 -Psandbox 下构建，不发布
├── _doc/                  # 文档，见文末「文档目录」
├── .github/dependabot.yml # Maven + GitHub Actions 每周一自动升依赖（分组 apache-commons/jackson/log4j/spring/testing）
└── LICENSE                # MIT
```

内部依赖（各模块 POM 实测，不是画好看的图）：`aop` 与 `dsl` 是零内部依赖的叶子；
`parser-json → dsl`；`core → parser-json, aop`；`cache/ioc → core, aop`（ioc 另加 proxy）；
`proxy → core`；`expr-obj → expr-el, core`；`jdbc → core, proxy, expr-sql, expr-obj, distribute`。
编译顺序由 Maven 保证，跨层引用（例如 `expr-*` 引 `jdbc`）会形成环，不要加。

---

## 🔧 技术栈

| 层级 | 技术（版本取自根 POM `<properties>` 与各模块 POM 实测） |
|------|------|
| 语言 / 运行时 | Java 8（`compile.version=8`） |
| 父链 | `z-boot-parent:1.0.21` → `z-boot-dependencies`（第三方地板）+ `z-boot-fleet`（兄弟仓权威表）；本仓只保留**量出分歧**的格 |
| 本仓覆盖的第三方版本 | `log4j 2.26.1`（地板 2.25.4）、`slf4j 2.0.16`（地板经 spring-boot 供 1.7.36）、`junit 5.11.4`（地板 junit-bom 5.9.3） |
| 测试 | JUnit Jupiter 5.11.4 + `junit-vintage-engine`（跑历史 JUnit4 用例）；surefire 3.5.4 仅在 `z-util-proxy` 覆写 |
| JSON | 自研 `z-util-parser-json`（`.g4` + 自研 DSL 解析器，非 Jackson）；Jackson 2.18.9 作 databind/jsr310 辅助 |
| 解析 | ANTLR 4.13.2（runtime）、jsoup 1.23.1 与 Guava 33.6.0-jre **只在根 DM 里管着，没有任何模块引用** |
| HTTP | OkHttp（client）、JDK `com.sun.net.httpserver`（server）；`netty-all` 在 `z-util-http/pom.xml` 声明但**全仓无一处 `io.netty` 代码** |
| 数据 | `z-util-jdbc`：Druid 1.2.24 + mysql-connector-j 8.2.0 + `javax.persistence-api` 2.2 + MyBatis 3.5.16（`<optional>`，只服务分页插件兼容层）+ H2 2.2.224（`test` scope） |
| Office | POI 5.5.1（+ `poi-ooxml-full`）、PDFBox 3.0.8 |
| 字节码 | ASM 9.7（`z-util-proxy` 内字面钉）、cglib 3.3.0、javassist 3.32.0-GA、javaparser 3.28.2、jol-core 0.17 |
| 运维 | github-api 1.330（根 DM）、JGit 5.13.4 + gitlab4j-api 5.2.0（`z-util-devops` 内字面钉）、Docker CLI/HTTP、Nexus REST |
| 模块内字面版本钉（现状） | 实测仍有 15 处非 `${revision}` 的第三方字面 `<version>`：pinyin4j 2.5.1（ch）、luaj-jse 3.0.1（expr-lua）、snakeyaml 2.2（parser-yaml）、mysql-connector-j 8.2.0 / javax.persistence-api 2.2 / mybatis 3.5.16 / h2 2.2.224（jdbc）、asm 9.7（proxy）、jgit 5.13.4 / gitlab4j-api 5.2.0（devops）、kryo 5.5.0 / jmh 1.37 ×2（serialize-benchmarks）、junit 4.13.2（serialize-it）；expr-groovy 走 `${groovy.version}` |
| Java EE 口径 | `javax.inject 1`、`javax.annotation 1.3.2`、`javax.servlet-api 3.1.0`、`validation 2.0.1.Final`、`javax.mail 1.6.2`、JAXB 2.3.x |
| 构建 | Maven + flatten-maven-plugin（CI-friendly `${revision}`）；`.env` / `.gnupg` 已被 `.gitignore` 排除 |

---

## 🚀 快速开始

### 引入某个模块（推荐：只拿你要的那一件）

```xml
<dependency>
    <groupId>io.github.yuku123</groupId>
    <artifactId>z-util-core</artifactId>
    <version>1.0.14</version>
</dependency>
```

### 用 `z-util-all` 统一版本（注意：它不是"一把梭全拿"）

`z-util-all` 的 packaging 是 `pom`，内部**只有 `<dependencyManagement>`**、没有 `<dependencies>`，
所以把它当普通依赖引入**不会带来任何 jar**。正确用法是 import 成 BOM，再声明需要的模块：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.yuku123</groupId>
            <artifactId>z-util-all</artifactId>
            <version>1.0.14</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.yuku123</groupId>
        <artifactId>z-util-jdbc</artifactId>   <!-- 版本由上面的 BOM 提供 -->
    </dependency>
</dependencies>
```

它的版本表实测覆盖 20 个模块（core/monitor/parser-json/math/proxy/office/media/expr-js/workflow/
visualization/jdbc/http/ml/devops/source/distribute/ch/parser-xml/dsl/cli）。
`z-util-cache`（已降 `z-util-pattern-cache`）、`z-util-ioc`、`z-util-aop`、`z-util-validation`（已降 `z-util-pattern-validation`）与各 `serialize-*`、其余 `parser-*` /
`expr-*` 子模块**不在** `z-util-all` 里 —— 引这些要么显式写版本，要么直接用根 `z-util` POM 的 DM（那里覆盖 43 个自家构件）。

### 本地构建

```bash
mvn clean install -DskipTests        # 编译 + 安装到本地仓库
mvn clean install -pl z-util-core -am -DskipTests   # 单模块（含其上游）
```

构建需要能解析到 `io.github.yuku123:z-boot-parent:1.0.21`（在 repo1，`<relativePath/>` 留空）。
第三方版本主要由父链（`z-boot-dependencies` 地板 + `z-boot-fleet` 兄弟仓权威表）供给；
但**模块 POM 里实测还留着 15 处字面版本钉**（清单见「🔧 技术栈」），新增依赖时优先交给父链，
别再往模块里添新的一格。

### 常用入口

| 想做的事 | 入口类（`io.github.yuku123:z-util-*`） |
|----------|------|
| 判空/截串/集合运算 | `com.zifang.util.core.lang.StringUtil` / `CollectionUtil` / `Assert` |
| JWT 签发与校验 | `com.zifang.util.core.jwt.Jwt`（+ `Claims` / `HmacSha256`） |
| 分页返回体 | `com.zifang.util.core.meta.page.PageResult` / `PageRequest`（全站唯一口径） |
| 限流 / 熔断 | `core.ratelimit.SlidingWindowRateLimiter`、`core.resilience.CircuitBreaker` |
| JSON 读写 | `com.zifang.util.json.JsonUtil`（`toJson` / `parseObject` / `fromJson` / `getObject` / `getBoolean` …） |
| 取数 → 内存 SQL → 对象整形 | `com.zifang.util.db.*` + `com.zifang.util.expr.sql.engine.VirtualTableEngine` + `com.zifang.util.expr.obj.ObjEngine` |
| 进程内缓存 | `com.zifang.util.cache.CacheBuilder` → `LoadingCache` / `WTinyLfuCache` |
| 分布式 ID | `com.zifang.util.distributes.sequence.SnowflakeIdWorker` / `UuidV7` / `NanoId` |
| 二进制序列化 | `io.zifu.z.serialize.core.ZSerializer` / `ZDeserializer` + `@ZMessage` / `@ZField` |
| 声明式 HTTP 客户端 | `com.zifang.util.http.client.HttpExecutor` / `HttpRequestInvocationHandler` |
| 指标与导出 | `com.zifang.util.monitor.JvmMonitor` / `MetricsRegistry` / `JsonExporter` |

本仓**不是**服务，没有 `@RequestMapping`、没有 `application.yml`、没有 Dockerfile/compose/k8s 资产，
所以这里不列 API 路径表与部署章节。

---

## 🧪 测试

```bash
mvn test                     # 全量：请在 JDK 17 上跑（见下）
mvn test -pl z-util-cache -am
mvn test -Dtest=JsonUtilTest -pl z-util-parser/z-util-parser-json
```

- **全量 `mvn test` 不能跑在 JDK 8 上。** `z-util-proxy/pom.xml` 的 surefire `<argLine>` 写死了
  `--add-opens java.base/java.lang=ALL-UNNAMED` 与 `--add-opens java.base/java.lang.reflect=ALL-UNNAMED`
  （它的测试要对 JDK 内部类 `setAccessible`）。这两个参数是 JDK 9+ 才认识的，**JDK 8 的 JVM 会直接拒绝启动 forked 进程**，
  于是从根跑的 `mvn test` 必然在该模块断掉。库本身的编译/运行口径仍是 Java 8 —— 两件事不要混。
- 规模：主源 1468 个 `.java`、测试 748 个 `.java`；`z-util-core`(236)、`z-util-http`(129)、`z-util-ml`(76)、
  `z-util-proxy`(47) 占了大头。`@Disabled` 数量为 0。
- 部分用例碰外部世界，离线或无凭据环境会红：`z-util-http`（真实出网）、`z-util-devops`（GitHub API / docker 命令）、
  `z-util-jdbc`（MySQL 连接串，另有一批走 H2）。这类失败不代表代码坏了，验证时可以先按模块跑：
  `mvn test -pl z-util-core,z-util-parser/z-util-parser-json,z-util-expr/z-util-expr-obj`。
- 判"测过"要以 `mvn clean ...` 为准：增量构建会复用旧 `target/`，绿色不一定可信。

---

## 📦 发布到 Maven Central

```bash
bash _doc/003_script/deploy_maven_center.sh gpg-init    # 首次：生成 GPG 密钥环并写本地 .env
bash _doc/003_script/deploy_maven_center.sh publish     # mvn -B deploy -Pcentral -pl '!z-util-all' -DskipTests
bash _doc/003_script/deploy_maven_center.sh verify      # 回查中央是否可拉
bash _doc/003_script/install-settings.sh               # 把 server id=central 写进 ~/.m2/settings.xml
```

- 凭据只从**根目录 `.env`**（已 gitignore）读取，涉及的环境变量名：`CENTRAL_USERNAME`、`CENTRAL_TOKEN`
  （Central Portal User Token，不是 Bearer）、`CENTRAL_GPG_PASSPHRASE`、`GPG_KEY_ID`。README 与文档里**不写任何值**。
  GPG 密钥环用 `GNUPGHOME=./.gnupg`，不污染 `~/.gnupg`。
- 升版本是改根 POM 的 `<properties><revision>`（当前 `1.0.14`），**不要**用 `mvn versions:set` ——
  CI-friendly 版本 + flatten 插件下 `versions:set` 会写出一堆 `${revision}` 之外的脏值。
- `central` profile 追加 javadoc（`doclint=none`）、GPG 签名，并把 default-deploy 交给 `central-publishing-maven-plugin`
  （`maven-deploy-plugin` 在该 profile 下设 `skip=true`，避免重复 deploy）。
- 脚本默认带 `-pl '!z-util-all'`，但实测 `z-util-all:1.0.14` 的 pom 在 repo1 可读（且是它唯一的已发布版本，
  `1.0.13` 为 404）—— 说明 1.0.14 这一批是由组织的批量发布通道把聚合件也一起推上去的，脚本那行排除参数只描述它自己那条路径。
- `z-util-zex` 不在默认 reactor（只有 `-Psandbox` 才构建），实测中央 404：这是设计如此，它永远不该发布。

完整流程与避坑清单见 [`_doc/006_release/RELEASE_TO_MAVEN_CENTRAL.md`](_doc/006_release/RELEASE_TO_MAVEN_CENTRAL.md)
与 [`_doc/006_release/发布指引.md`](_doc/006_release/发布指引.md)。

---

## ⚠️ 实测坑（都还在代码里，不是历史故事）

1. **`JsonUtil` 把整数 `1` 读成 `false`。** `JsonUtil.getObject(json, "flag", Boolean.class)` / `fromJson(...)`
   走的是私有 `toBoolean(Object)`：非 `Boolean` 时退化成 `Boolean.parseBoolean(String.valueOf(v))`，
   于是 `{"flag":1}` → `false`（而不是 `true`、也不是异常）。
   另一路 `getBoolean(json, key)` 用的是 `toBooleanOrNull(...)`，只认 `Boolean` 与 `"true"/"false"` 字符串，
   遇到数字返回 `null`。写迁移/接第三方报文时，把 0/1 当布尔用一定要先归一化。
2. **字符串里的裸换行会让整份 JSON 作废。** `z-util-parser-json` 的
   `JsonLexer.g4` 里 `StringLiteral: '"' ~["\\\r\n]* ('\\' . ~["\\\r\n]*)* '"'` —— 字符串字面量内部禁止裸 `\r`/`\n`
   （token 之间的换行由 `EOL` 走 hidden channel，pretty JSON 没问题）。所以**一个未转义的换行会让整份文档解析失败**，
   整帧一起丢。产出侧要相信 `JsonUtil.toJson`：它会把 `\n`/`\t`/`\r` 转义（`JsonUtilTest` 有断言）。
   消费不可信来源时用 `parseObjectQuietly` / `parseToMap`（失败返回 null / 空 Map，不抛）。
3. **`z-util-all` 不聚合 jar**，只发版本号 —— 见「快速开始」，这是最容易踩的一条。
4. **`z-util-cache` 没有分布式后端**，只有进程内实现（W-TinyLFU / LRU / Loading / Transactional / Metered）。
   需要跨节点一致请另找 z-cache。
5. **`netty-all` 与 `guava`/`jsoup` 是"账面依赖"**：前者在 `z-util-http` 的 POM 里声明但代码零引用，
   后两者只在根 DM 里管版本、没有模块使用。别据此推断本仓有 Netty 网关或 Guava 依赖。

---

## 🤝 贡献

- 新增模块：在根 POM `<modules>` 与 `<dependencyManagement>` 各加一条，子 POM 的 `<parent>` 指向 `z-util`（`${revision}`），
  第三方版本交给父链，别在模块里写字面版本。
- 收编优先于新写：全站的重复实现（ID、分页、JWT、限流…）都往这里收，收一份就删一份调用方的旧码。
- 提交前跑 `mvn clean install`（JDK 17），PR 只需描述动机与影响面。
- 文档只放 `_doc/`，根目录只留 `README.md`（见 `002_项目文档收口规范`）。

---

## 📄 License

MIT，见根目录 [`LICENSE`](LICENSE)；根 POM `<licenses>` 同样声明 MIT License。

_Maintained by the z-opc-foundation organization._

---

## 文档目录

本项目文档统一收口在 `_doc/` 下：

- [`_doc/001_arch/`](_doc/001_arch/) — 架构与发布文档：
  - [`wire-format.md`](_doc/001_arch/wire-format.md) — Z-Serialize 线格式规范（header/varint/schemaId/压缩加密；zstd、lz4 尚未落地为代码）
  - [`pivot.md`](_doc/001_arch/pivot.md) — `engine.service.pivot` 的 `pivot` / `unpivot` 参数说明
  - [`resource.md`](_doc/001_arch/resource.md) — `engine.service.resourceHandler`：本地文件 / MySQL / 集群 / HDFS 读写与表映射
  - [`target.md`](_doc/001_arch/target.md) — `engine.service.target`：churn 的 target 生成
  - [`RELEASE_TO_MAVEN_CENTRAL.md`](_doc/006_release/RELEASE_TO_MAVEN_CENTRAL.md) — 本仓发布到 Central 的操作手册
  - [`发布指引.md`](_doc/006_release/发布指引.md) — 通用（任何多模块 Java 仓）Central 发布指引 + AI 避坑清单
- `_doc/002_deploy/` — 目前为空目录（本仓是库，没有部署资产；空目录未被 git 跟踪，clone 后可能不存在）
- [`_doc/003_script/`](_doc/003_script/) — 脚本：
  - [`deploy_maven_center.sh`](_doc/003_script/deploy_maven_center.sh) — 发布入口（`gpg-init` / `publish` / `verify` / `readme` / `help`；须在仓库根目录上下文运行，凭据取 `.env`）
  - [`install-settings.sh`](_doc/003_script/install-settings.sh) — 把 `<server id="central">` 写进 `~/.m2/settings.xml`
  - `11.1.sh` / `11.3.sh` / `11.4.sh` / `11.7.sh` / `11.8.sh` / `12.1.sh` / `12.4.sh` / `12.7.sh` / `13.1.sh` / [`loopDir.sh`](_doc/003_script/loopDir.sh) — Shell 学习笔记示例（多命令、echo、变量、算术、退出码、if、数值比较、case、循环、递归遍历目录），**不是运维脚本**，与构建无关
- `_doc/004_skill/` — AI skill 定义：
  - [`CLAUDE.md`](CLAUDE.md) — 给 AI 协作的构建/模块约定（其版本号段落已过时，以本 README 与 POM 为准）

各文档详细说明见各子目录。

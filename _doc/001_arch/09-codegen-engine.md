# 09 · 编译期代码生成引擎（z-util-codegen）

2026-11-19 落地。目标：在 z-util 里复刻两套业界已验证的编译期代码生成体验——
**Lombok 性质**（往已有类里注入成员）与 **MapStruct 性质**（生成转换器实现类），
且不引第三方依赖、不复刻它们的全部面。

## 1. 为什么难：APT 的先天限制

标准 `javax.annotation.processing` 只允许**新增**文件（`Filer.createSourceFile` / `createClassFile`），
不允许修改已有类型——这是 JSR 269 的明文约束。

Lombok 的做法是绕过这层：在注解处理轮次里直接改 javac 内部 AST
（`com.sun.tools.javac.tree.TreeMaker` 造节点，塞进 `JCClassDecl.defs`）。
本引擎走的就是这条路，区别在于 Lombok 用自家私有注入通道，我们用**标准 APT 入口 + javac 内部 API 注入**，
把越界面收窄到一个 javac 开关（见 §4）。

## 2. 两种产物、两条通道

| | Lombok 性质（bean） | MapStruct 性质（mapper） |
|---|---|---|
| 产物 | 成员注入到**注解所在的类** | 全新 `{Interface}Impl` 类 |
| 通道 | `AstMemberEmitter`：javac AST 注入 | `FilerSourceEmitter`：`Filer.createSourceFile` |
| 触发 | `@ZData` `@ZGetter` 等 13 个注解 | `@ZMapper` |
| 后续轮次可见性 | 同轮编译立即可用（AST 已改） | 下一轮编译才可见（需要源码落盘） |

### 2.1 内核（z-util-codegen-core）

- `AbstractCodegenProcessor`：APT 骨架。`supportedAnnotationTypes` 取各 handler
  `supportedAnnotations()` 的并集；每轮把命中元素按注解分发给 `CodeGenHandler`（SPI 或子类覆写）。
- `ModelBuilder`：把 `TypeElement` 摊平成中性 `TypeModel`/`FieldModel`/`MethodModel`
  （含注解值解包：数组/嵌套注解/枚举常量/Class 字面量）。handler 面向这个模型写，不碰 javac 类型。
- `Expr` / `Stmt` / `TypeRef`：密封接口表达式/语句模型（`binary`、`call`、`ifThen`、`ret`…），
  是 handler 与 javac 之间的隔离层。
- `AstMemberEmitter`：唯一的 javac 内部 API 触点。把 `Expr`/`Stmt` 翻成 `JCTree`：
  `maker.MethodDef/VarDef/Block`、`Exec` 包语句、`Select` 组方法引用（`this.getClass()` 必须走
  `Select(this, "getClass")`，直接拼 Ident 会变成构造器调用——实测踩过）。
  自带去重：类里已有同名同参方法/构造器/字段则跳过，尊重手写覆盖。
- `FilerSourceEmitter`：普通 `JavaSource` 拼行 + 排序 import，走标准 Filer。

### 2.2 Lombok 性质集（z-util-codegen-bean）

7 个 handler：AccessorHandler（`@ZGetter`/`@ZSetter`，类级或字段级，boolean 字段支持 `is` 前缀）、
ToStringHandler、EqualsHashCodeHandler（`getClass()` 严格相等语义 + `Objects.equals/hash`）、
ConstructorHandler（`@ZAllArgsConstructor`/`@ZNoArgsConstructor`/`@ZRequiredArgsConstructor`）、
BuilderHandler（**偏离**：生成顶层 `XxxBuilder` 类 + 注入静态 `builder()` 工厂，不是 Lombok 的内部类——
AST 注入一个完整嵌套类的复杂度不值得）、LoggerHandler（`@ZSlf4j`，slf4j-api 是唯一运行时依赖）、
DataHandler（组合展开为以上全部）。

### 2.3 MapStruct 性质集（z-util-codegen-mapper）

`@ZMapper` 标在接口上，生成同包 `{Interface}Impl`：
同名字段自动映射、源为 null 输出 null、`@ZMapping` 显式指定 source/target/ignore/
`expression = "java(src.getName().toUpperCase())"`（原样拼进生成代码，`src` 是源参数名）/constant、
嵌套映射（同轮扫描其它 `@ZMapper` 生成 `private final {Impl} field = new {Impl}()` 委托）、
`@ZMapperConfig` + `@ZMapper(unmappedTargetPolicy)` 的 ERROR/WARN/IGNORE 未映射报告。

## 3. 模块与依赖

```
z-util-codegen-annotations   纯注解，SOURCE retention，零依赖
z-util-codegen-core          内核；仅依赖 jdk.compiler 内部 API，零 z-util-* 依赖
z-util-codegen-bean          → annotations + core + slf4j-api
z-util-codegen-mapper        → annotations + core
z-util-codegen-it            test 载体，src/main 无源码；不发布（excludeArtifacts）
```

发布口径与 serialize-it 同款：DM 集合 = 已发布集合，四个 jar 进根 DM 与 `z-util-all` BOM，
codegen-it 由 central profile `excludeArtifacts` 退出发布。

## 4. 运行前提（消费者必须知道的一条）

AST 注入触碰 `com.sun.tools.javac.{tree,util,code,processing}` 内部包，需要两件事：

1. 编译参数（maven-compiler-plugin `compilerArgs`）加 4 条
   `--add-exports jdk.compiler/com.sun.tools.javac.xxx=ALL-UNNAMED`；
2. **`-XDaccessInternalAPI`**——javac 的隐藏开关。设了它，`JavacProcessingEnvironment`
   会把整个 jdk.compiler 模块 open 给处理器 classloader，运行期不再需要任何
   `--add-opens`/`--add-exports` JVM 参数。JDK 17 与 23 均实测存在且有效。

`z-util-codegen-it/pom.xml` 是可拷贝的完整配置样例（`annotationProcessorPaths` + 上述参数）。
这是对"纯标准 APT"的显式偏离：代价是绑死 javac（JDK 换 ECJ/Groovy 编译器不可用），
换来的是 Lombok 级体验。升级 JDK 大版本时 `JCTree.Tag`/`TypeTag` 枚举需回归验证。

## 5. 测试证据

`z-util-codegen-it`：`BeanFeatureTest` 3 例（data 全家桶 + `@ZNonNull` NPE、builder）、
`MapperFeatureTest` 4 例（同名映射、null 透传、嵌套映射、显式规则），test 域真实跑注解处理器，
2026-11-19 全绿（7/7，BUILD SUCCESS）。

注意增量编译陷阱：改了处理器之后必须 `mvn clean` 掉 IT 模块再测——
增量编译发现注解处理器 jar 未变时会跳过重跑，留下旧版本生成物。

## 6. 不做什么

- 不做 Lombok 的 `val`/`var`、`@SneakyThrows`、`@Delegate`、实验性 AST 改写类型的那部分；
- 不做 MapStruct 的 `@AfterMapping`、多源参数、qualified by、Spring/CDI 组件模型——
  生成物永远是 `new` 出来的无状态实现类；
- 不做 IDE 插件。AST 注入对 javac 透明，但对 IDE 的增量解析不一定透明
  （Lombok 为此维护了全套 IDE 插件，我们不背这个成本；建议 IDE 侧开启注解处理即可）。

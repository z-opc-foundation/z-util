# z-util-strategy — 决策引擎（决策包 / 灰度 / 试运行）

> 落库日期：2026-10-13
> 定位：风控 / 反欺诈 / 内容审核场景的决策能力。回答"给定事实，该给什么结论、依据是什么、这条规则上线前跑得怎么样"。

## 0. 术语澄清（先看这段，否则会拿着自造词去跟人对齐）

**「决策包」是本仓自定义术语，没有行业标准出处。** 2026-10-13 联网核实的各家叫法：

| 厂商 | 实际用词 | 语义侧重 |
|---|---|---|
| 阿里云决策引擎 | **策略** | 事件 + 条件规则（左变量/操作符/右变量）+ 逻辑表达式 + 输出（标签/评分/决策）+ 中间变量 |
| 蚂蚁 AlphaRisk | 智能风控引擎 / 风险大脑 | 公开资料未出现"决策包" |
| 腾讯天御 | 智能风控中台 / RCE 引擎 | 讲"场景/能力"，不讲打包单元 |
| 社区通用 | 规则包 / 策略集 | 一组可发布、可回滚的决策资产 |

概念是成立的（阿里云那条"策略"确实是一个带版本、有生效延迟、能整体回滚的资产单元），
**词是自造的**。内核叫 `DecisionPackage`，文档标注自定义术语，对外沟通说"策略包"。

## 1. 与既有引擎的划界

| 组件 | 回答的问题 | 有无状态 |
|---|---|---|
| `z-util-dsl/z-util-expr-*` | 一个表达式求值成什么值 | 无 |
| `z-wf` | 流程走到哪一步、token 怎么流转、任务派给谁（原 `z-camuda` 已于 2026-11-17 退役归档，由 z-wf-admin 兼容层承接）| 有（`ZWF_*` 9 表）|
| **`z-util-strategy`** | 这一步该判什么、判成什么、这条规则靠不靠谱 | 无（单次调用内） |

**z-wf 决定"要不要走风控这一步"，z-util-strategy 决定"这一步判什么"。**
网关条件求值与本引擎共用 `z-util-expr-*`，不允许出现第二套表达式实现。

## 2. 三个核心概念的关系

这是整个设计里最容易做错的地方 —— **灰度和试运行语义完全不同，不能混成一套代码路径**：

| | 试运行 / TRIAL | 灰度 / CANARY | 对照组 / CONTROL |
|---|---|---|---|
| 流量 | **100%** 复制同一批请求给新包 | **切 1%/5%** 真实用户 | 命中的用户里随机放行一小部分 |
| 是否生效 | **只记录不生效** | **真生效** | **故意不拦** |
| 产出 | 新旧输出差异比对 | 业务指标看板 | 误杀率 |
| 副作用 | 无 | 有（误杀 / 转化损失） | 有（刻意放过一小撮欺诈） |

**统一模型**：引擎同时持有 `current` 与 `candidate` 两个决策包，`candidate` 有三种取值：

```
candidate == null                              → 只有 current，永远 ACTIVE
candidate.runMode == TRIAL                     → current 出决策；candidate 全流量执行，
                                                 结果进 DecisionResult.shadow()，不生效
candidate.runMode == ACTIVE 且命中流量分片      → 用 candidate 出决策，trace 标 hitGray
```

于是"灰度"和"试运行"不是两套代码，是同一个 candidate 槽位的两种模式。

对照组的意义：全量拦会误杀正常用户，误杀率无法从日志反推。
随机放行 1% 命中用户、事后人工复核这批人的实际欺诈率，就得到了误杀率的估计值。
（[Sardine: How to Release Fraud Rules Safely](https://www.sardine.ai/blog/fraud-rules)）

## 3. 结构

```
com.zifang.util.strategy
├── DecisionPackage        决策包：不可变快照 + 版本号 + 运行模式
├── DecisionEngine         引擎：current/candidate 两个 volatile 槽 + 评估入口
├── RunMode                DRAFT / TRIAL / ACTIVE
├── DecisionResult         结论：decision + shadow + trace + 版本 + 分片标记
├── DecisionOutcome        PASS / SCORE / REVIEW / MANUAL / REJECT
├── StrategyContext        事实集
├── DecisionTrace/TraceStep 可解释轨迹
├── TrafficPolicy          灰度分流：稳定哈希 + 放量比例
├── ControlPolicy          对照组：命中用户随机放行比例
├── DecisionMetrics        指标出口（接口）+ InMemoryDecisionMetrics
└── rule/
    ├── Rule               规则：id / priority / enabled / condition / effect
    ├── Condition          对象化条件表达，toDsl() 编译成 DSL 交给 dsl 求值
    ├── Conditions         条件工厂（字段、比较、逻辑组合）
    ├── Effect             规则效果：给结论 / 加减分 / 打标签
    ├── MatchMode          FIRST_MATCH / COLLECT_ALL / THRESHOLD
    ├── MatchSpec          编排参数
    ├── HitRecord          命中记录
    └── RuleEvaluator      规则求值，产出 HitRecord 序列
```

## 4. 关键设计决策

### 4.1 规则 = 对象化条件表达，内部编译成 DSL 执行

规则条件不是字符串，是一棵 Java 对象树：

```java
Conditions.and(
        Conditions.gt("amount", 10000),
        Conditions.in("country", "CN", "US"),
        Conditions.or(Conditions.eq("vipLevel", 3), Conditions.like("deviceId", "LD%"))
)
```

内部由编译器翻译成 DSL 文本（`amount > 10000 AND (country = 'CN' OR country = 'US')`），
交给 `z-util-expr-sql` 的 `ExpressionEvaluator` 求值。理由：

- 业务方用 Java builder 写条件，有类型检查、IDE 补全、不会写出语法错误的字符串
- 执行路径仍走仓内已有的 DSL 栈，不引第二套表达式引擎
- **编译结果缓存在规则对象里**，热路径只求值不重新 parse

### 4.2 决策包是不可变快照，切换是换引用

`DecisionPackage` 全部字段 final，构建后只读。引擎用 `AtomicReference` 持有
current / candidate，**切换 = 换引用 = 原子**，回滚 = 换回上一个快照引用。
灰度期间想改规则必须产生新快照（`version` 递增），不允许就地改 ——
就地改会让"正在跑的灰度"和"你以为在跑的灰度"不是同一份配置。

### 4.3 分流单元是稳定哈希，不是请求 ID

```
bucket = Math.abs(hashKey.hashCode()) % 100
命中灰度 ⟺ bucket < trafficPolicy.percent
```

同一个 hashKey（通常是用户 ID）恒定落同一桶，比例 0→5→20 放量时用户**不会来回跳**。
用请求 ID 分流会让同一用户随机进出，指标没法归因。内核只管哈希算法，
hashKey 的类型（字符串/数字）由应用层给。

### 4.4 条件求值失败必须显式选 fail-open 还是 fail-closed

风控漏判和误判的代价不对称，而且随场景反转：反欺诈宁可误杀，反洗钱宁可放过。
不设全局默认值之外的隐式行为，`MatchSpec` 上显式声明 `onError`。

### 4.5 多规则结论合并取严

`REJECT > MANUAL > REVIEW > SCORE > PASS`，由 `DecisionOutcome.worse()` 统一承担，规则求值时逐条取更严重者。

曾经设计过一个可插拔的 `MergePolicy` 接口（可切"取严 / 取宽"），最终删掉了：
"取宽"只有灰度双跑合并这一个真实用法，而那条路径本轮并不实现 ——
留一个只有一种调用方式的抽象，比不留更糟（调用方会以为换个实现就能改语义）。
真正的"取宽"场景（提额、加白名单）直接写一条产出 PASS 的规则即可，不需要内核支持。

## 5. 决策包的两种写法（Java API + JSON/YAML）

- Java：`DecisionPackage.builder()...build()`
- JSON/YAML：交给 `DecisionPackageLoader`，产出同一个 `DecisionPackage`

条件支持两种形态，JSON 里可混用：

```yaml
name: 大额异常交易
version: 3
runMode: TRIAL
match:
  mode: COLLECT_ALL
rules:
  - id: R001
    priority: 10
    condition: "amount > 10000 AND country <> 'CN'"   # DSL 文本
    effect: { outcome: REVIEW, score: 60, tag: big_amount }
  - id: R002
    priority: 20
    condition:                                          # 结构化（阿里云那套左变量/操作符/右变量）
      and:
        - { field: vipLevel, op: "<=", value: 2 }
        - { field: deviceId, op: MATCHES, value: "LD[0-9]{4}" }
    effect: { outcome: REJECT, score: 40 }
```

> YAML 载入走 `z-util-parser-yaml`（带 SnakeYAML）。`z-util-strategy` 本身**不依赖**它 ——
> SnakeYAML 有 CVE 前科，不该让一个决策内核把它拖给所有消费方。
> YAML 用法：`DecisionPackageLoader.load(YamlUtil.parse(text))`（YAML 是 JSON 超集，落到同一个 Map 入口）。

## 6. 内核做 / 应用层不做

**内核**：决策包快照与版本、原子切换与回滚、三态运行模式、稳定哈希分流、结论合并策略、
双结果（decision + shadow）、对照组抽样与指标计数、trace、规则 DSL 编译与执行。

**应用层**：放量阶梯的调度与定时晋级、指标看板与告警、审批流与发布单、规则可视化编辑器、
命中事件落库与推送、对照组样本的事后人工复核。

判据很简单：内核只做**单次调用内、无存储、无网络**能完成的事。
任何需要定时器、数据库、消息队列的概念，都不进这个纯库。

## 7. 开源对照

| 项目 | 决策包 | 版本 | 灰度 | 试运行 |
|---|---|---|---|---|
| Camunda DMN | DRD（带 version） | 强（版本化部署） | 应用层 | 决策表手工测试 |
| Drools | kjar + `kmodule.xml` | 靠 Maven GAV | 无 | 无 |
| Easy Rules | 无 | 无 | 无 | 无 |

**决策包版本化可以抄 DMN，但"灰度 + 试运行 + 对照组"全行业都得自己造** ——
这正是本模块存在的理由。

## 8. 本轮交付范围

**已实现**：DecisionPackage / RunMode / DecisionEngine / TrafficPolicy / ControlPolicy /
ControlPolicy / DecisionResult + DecisionOutcome + StrategyContext + DecisionTrace /
rule 包全套（Rule、Condition、Conditions、Effect、MatchMode、MatchSpec、HitRecord、RuleEvaluator）/
DecisionMetrics + InMemoryDecisionMetrics / DecisionPackageLoader（Map + JSON）。

**有意不做**：`DecisionTable`（决策表要先有稳定表格 DSL，否则写出来必推翻）、
集合型事实的成员判定（需要注册 UDF，会污染全局 `SqlFunctionRegistry`）、
规则持久化与热更新源（应用层职责）。

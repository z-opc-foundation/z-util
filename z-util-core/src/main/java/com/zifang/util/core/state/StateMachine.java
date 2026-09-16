package com.zifang.util.core.state;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 业务级状态机（支持 statechart 层级）。
 * <p>
 * 相较于 flat 状态机，本实现提供以下能力，覆盖更复杂的业务流程：
 * <ul>
 *   <li><b>层级状态（composite）</b>：一个状态可以拥有子状态机，子状态机的初始子态作为父态激活后的实际当前态</li>
 *   <li><b>外部 / 本地转移</b>：转移的 from/to 可以是任一层级的状态，引擎会沿父链向上找匹配（继承），
 *       并通过 LCA（最近公共祖先）计算退出与进入路径</li>
 *   <li><b>Entry / Exit 动作</b>：状态本身可挂 entry/exit 动作，在状态进出时触发</li>
 *   <li><b>History 状态（H）</b>：声明为 history 的状态，会记住父 composite 上次激活的子态，
 *       下次进入时恢复到该子态</li>
 *   <li><b>动作在 LCA 上触发</b>：转移动作的 from 参数是 LCA，符合 UML statechart 语义</li>
 *   <li><b>守卫 / 监听器 / 失败快速开关</b>：与简单版一致</li>
 *   <li><b>线程安全</b>：{@link #fire} 加锁，多业务线程并发触发安全</li>
 * </ul>
 *
 * <h2>用法示例</h2>
 * <pre>{@code
 * enum OrderState {
 *     CREATED, PAID,                  // 顶层
 *     PENDING_INVOICE, INVOICED,     // PAID 的子态
 *     SHIPPED, DONE, CANCELLED        // 顶层
 * }
 * enum OrderEvent { PAY, ISSUE_INVOICE, CONFIRM, SHIP, COMPLETE, CANCEL }
 *
 * StateMachine<OrderState, OrderEvent, OrderCtx> sm = StateMachine
 *     .<OrderState, OrderEvent, OrderCtx>builder()
 *     .initial(OrderState.CREATED)
 *
 *     // 顶层转移
 *     .from(OrderState.CREATED).on(OrderEvent.PAY).to(OrderState.PAID)
 *     .from(OrderState.CREATED).on(OrderEvent.CANCEL).to(OrderState.CANCELLED)
 *
 *     // PAID 是个 composite：进入 PAID 会自动下钻到 PENDING_INVOICE
 *     .composite(OrderState.PAID, OrderState.PENDING_INVOICE, sub -> sub
 *         .from(OrderState.PENDING_INVOICE).on(OrderEvent.ISSUE_INVOICE)
 *             .action((from, ctx) -> ctx.invoiceIssued = true)
 *             .to(OrderState.INVOICED)
 *     )
 *
 *     // 外部转移：从 INVOICED（PAID 的子态）出去，到顶层 SHIPPED
 *     .from(OrderState.INVOICED).on(OrderEvent.CONFIRM).to(OrderState.SHIPPED)
 *
 *     // 外部转移：PAID 的任一子态都能被取消
 *     .from(OrderState.PENDING_INVOICE).on(OrderEvent.CANCEL).to(OrderState.CANCELLED)
 *     .from(OrderState.INVOICED).on(OrderEvent.CANCEL).to(OrderState.CANCELLED)
 *
 *     // 顶层转移
 *     .from(OrderState.SHIPPED).on(OrderEvent.COMPLETE).to(OrderState.DONE)
 *
 *     .build();
 *
 * sm.fire(OrderEvent.PAY, ctx);    // CREATED -> (exit CREATED) -> PAID -> (enter PAID, descend) -> PENDING_INVOICE
 * sm.getCurrentState();             // PENDING_INVOICE
 * }</pre>
 *
 * @param <S> 状态类型（推荐 enum）
 * @param <E> 事件类型
 * @param <C> 业务上下文类型
 */
public class StateMachine<S, E, C> {

    private final S initialState;
    private final Map<S, Map<E, Transition<S, E, C>>> transitionTable;
    // 不可变配置（在 build 时由 builder 一次性填充）。copy 构造器复用引用。
    private Map<S, S> parentMap = new HashMap<>();            // 子态 -> 父态（顶层为 null）
    private Map<S, S> compositeInitialMap = new HashMap<>();  // composite -> 初始子态
    private Map<S, BiConsumer<S, C>> entryActions = new HashMap<>();
    private Map<S, BiConsumer<S, C>> exitActions = new HashMap<>();
    private Set<S> historyStates = new HashSet<>();
    // 每实例可变状态
    private Map<S, S> lastActiveSubstate = new HashMap<>();   // composite -> 上次激活的子态（H 用）
    // 不可变配置续
    private Map<S, S> historyParentMap = new HashMap<>();     // H 状态 -> 其父 composite
    private Map<S, HistoryType> historyTypes = new HashMap<>(); // H 状态 -> 恢复类型
    private Set<S> finalStates = new HashSet<>();             // 终态集合

    // 监听器需在 copy 构造器里被覆盖（每实例独立），非 final
    private CopyOnWriteArrayList<StateListener<S, E, C>> listeners = new CopyOnWriteArrayList<>();

    private volatile S currentState;

    private StateMachine(S initialState,
                         Map<S, Map<E, Transition<S, E, C>>> transitionTable) {
        this.initialState = Objects.requireNonNull(initialState, "initialState");
        this.transitionTable = transitionTable;
        // 注意：此时 parentMap / compositeInitialMap 等还没填充，初始下钻放到 build() 里完成
        this.currentState = initialState;
    }

    /**
     * 基于"模板"（已构建完成的实例）复制出一个新的实例：共享不可变配置（转移表/层级结构），
     * 但拥有独立的 currentState / lastActiveSubstate / listeners。
     * <p>
     * 主要供 {@link StateMachineFactory} 使用，业务上一个独立对象（如一个订单）对应一个实例。
     */
    private StateMachine(StateMachine<S, E, C> template) {
        this.initialState = template.initialState;
        this.transitionTable = template.transitionTable;
        this.parentMap = template.parentMap;
        this.compositeInitialMap = template.compositeInitialMap;
        this.entryActions = template.entryActions;
        this.exitActions = template.exitActions;
        this.historyStates = template.historyStates;
        this.historyParentMap = template.historyParentMap;
        this.historyTypes = template.historyTypes;
        this.finalStates = template.finalStates;
        // 独立可变状态
        this.lastActiveSubstate = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.currentState = descendIntoInitial(this.initialState, null);
    }

    private static String escape(String s) {
        return s.replace("\"", "\\\"");
    }

    /**
     * builder方法。
     *
     * @return static <S, E, C> StateMachineBuilder<S, E, C>类型返回值
     */
    public static <S, E, C> StateMachineBuilder<S, E, C> builder() {
        return new StateMachineBuilder<>();
    }

    /**
     * 复制一个独立实例（共享配置、独立状态）。等价于 {@code template.newInstance()}。
     */
    public StateMachine<S, E, C> newInstance() {
        return new StateMachine<>(this);
    }

    /**
     * 导出 Graphviz DOT 格式的状态图字符串，可粘贴到 <a href="https://dreampuf.github.io/GraphvizOnline/">在线渲染</a>
     * 或用 {@code dot -Tpng} 生成本地图片。
     * <p>
     * 形状约定：
     * <ul>
     *   <li>普通状态：椭圆</li>
     *   <li>composite：双圈（doublecircle）</li>
     *   <li>history（H）：八角形（octagon）</li>
     *   <li>终态（final）：粗体</li>
     * </ul>
     * 转移标签：{@code <event> [guard] / action}，internal 转移标 {@code (int)}。
     */
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph StateMachine {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=ellipse, fontname=\"Helvetica\"];\n");
        sb.append("  edge [fontname=\"Helvetica\", fontsize=10];\n\n");

        // 初始状态：带个入口空心节点指向它
        sb.append("  __start__ [shape=point, width=0, height=0];\n");
        sb.append("  __start__ -> ").append(dotId(initialState)).append(";\n\n");

        // 节点
        for (S s : allStates()) {
            String shape;
            if (historyStates.contains(s)) {
                shape = "octagon";
            } else if (compositeInitialMap.containsKey(s)) {
                shape = "doublecircle";
            } else if (finalStates.contains(s)) {
                shape = "ellipse";
            } else {
                shape = "ellipse";
            }
            sb.append("  ").append(dotId(s));
            sb.append(" [label=\"").append(escape(s.toString())).append("\"");
            if (finalStates.contains(s)) sb.append(", style=bold");
            sb.append(", shape=").append(shape);
            sb.append("];\n");
        }
        sb.append("\n");

        // 转移边
        for (S from : allStates()) {
            Map<E, Transition<S, E, C>> m = transitionTable.get(from);
            if (m == null) continue;
            for (Map.Entry<E, Transition<S, E, C>> e : m.entrySet()) {
                E event = e.getKey();
                Transition<S, E, C> t = e.getValue();
                String label = escape(String.valueOf(event));
                if (t.isInternal()) {
                    label += " (internal)";
                } else if (t.hasBranches()) {
                    label += " [choice]";
                }
                sb.append("  ").append(dotId(from)).append(" -> ").append(dotId(t.getTo()));
                sb.append(" [label=\"").append(label).append("\"];\n");
            }
        }
        // composite 初始进入边（initial -> initialChild）
        for (Map.Entry<S, S> e : compositeInitialMap.entrySet()) {
            sb.append("  ").append(dotId(e.getKey())).append(" -> ")
                    .append(dotId(e.getValue())).append(" [label=\"(initial)\", style=dashed];\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String dotId(S state) {
        // DOT 节点 ID 不能含空格或特殊字符
        return "\"" + state.toString().replace("\"", "\\\"") + "\"";
    }

    private java.util.Set<S> allStates() {
        java.util.Set<S> all = new java.util.LinkedHashSet<>();
        all.add(initialState);
        all.addAll(transitionTable.keySet());
        for (Map<E, Transition<S, E, C>> m : transitionTable.values()) {
            for (Transition<S, E, C> t : m.values()) {
                all.add(t.getTo());
                if (t.hasBranches()) {
                    for (Transition.Branch<S, C> b : t.getBranches()) {
                        all.add(b.target);
                    }
                }
            }
        }
        all.addAll(parentMap.keySet());
        all.addAll(compositeInitialMap.keySet());
        all.addAll(compositeInitialMap.values());
        all.addAll(historyStates);
        all.addAll(historyParentMap.keySet());
        all.addAll(finalStates);
        return all;
    }

    /**
     * 构造完成后由 build() 调用：从初始状态沿 composite 下钻到真正激活的叶子，并跑 entry actions。
     */
    private void descendOnInit() {
        this.currentState = descendIntoInitial(this.initialState, null);
    }

    // ==================== 公共 API ====================

    /**
     * getCurrentState方法。
     *
     * @return S类型返回值
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * getInitialState方法。
     *
     * @return S类型返回值
     */
    public S getInitialState() {
        return initialState;
    }

    /**
     * 重置回初始状态（不触发 entry/exit）。
     */
    public synchronized void reset() {
        this.currentState = initialState;
    }

    /**
     * 强制设到目标状态（跳过守卫/动作/监听器）。用于持久化恢复。
     */
    public synchronized void forceState(S state) {
        Objects.requireNonNull(state, "state");
        this.currentState = state;
    }

    /**
     * addListener方法。
     * * @param listener StateListenerS,类型参数
     */
    public void addListener(StateListener<S, E, C> listener) {
        if (listener != null) listeners.add(listener);
    }

    /**
     * removeListener方法。
     * * @param listener StateListenerS,类型参数
     */
    public void removeListener(StateListener<S, E, C> listener) {
        listeners.remove(listener);
    }

    /**
     * 触发一个事件，找不到匹配或守卫拒绝时抛 {@link StateMachineException}。
     */
    public synchronized S fire(E event, C context) {
        return fire(event, context, true);
    }

    /**
     * 触发一个事件。
     *
     * @param failFast true 表示无匹配转移或守卫失败抛异常；false 表示静默忽略并保留当前态
     */
    public synchronized S fire(E event, C context, boolean failFast) {
        S from = currentState;
        // 终态：拒绝所有事件
        if (finalStates.contains(from)) {
            String reason = "State machine is completed at final state '" + from
                    + "'; event '" + event + "' is not accepted";
            notifyRefused(from, event, context, reason);
            if (failFast) {
                throw new StateMachineException(reason);
            }
            return from;
        }
        Transition<S, E, C> t = resolveTransition(from, event);
        if (t == null) {
            String reason = "No transition from state '" + from + "' on event '" + event + "'";
            notifyRefused(from, event, context, reason);
            if (failFast) {
                throw new StateMachineException(reason);
            }
            return from;
        }
        if (!t.isAllowed(context)) {
            String reason = "Guard rejected transition from '" + from + "' on event '" + event + "'";
            notifyRefused(from, event, context, reason);
            if (failFast) {
                throw new StateMachineException(reason);
            }
            return from;
        }

        S transitionFrom = t.getFrom();
        // choice 转移：根据 context 解析出真实目标
        S to = t.resolveTarget(context);
        S lca = findLCA(from, to);

        // 内部转移（自循环）：不触发 exit/enter，只跑 action
        if (t.isInternal()) {
            try {
                t.executeAction(transitionFrom, context);
            } catch (RuntimeException ex) {
                notifyError(from, event, context, ex);
                throw new StateMachineException("Action failed during internal transition from '"
                        + from + "' on event '" + event + "'", ex);
            }
            notifyEnd(from, from, event, context);
            return from;
        }

        // 进入转移的"begin"通知（在 exit 之前，转移尚未发生）
        notifyBegin(transitionFrom, to, event, context);

        // 1. 退出：从 currentState 向上退出，直到 (但不包括) lca
        //    如果 lca 为 null（顶层状态间转换），exit 链就是 from 自身
        List<S> exitChain = lca != null
                ? collectExitChain(from, lca)
                : Collections.singletonList(from);
        for (S s : exitChain) {
            if (compositeInitialMap.containsKey(s)) {
                lastActiveSubstate.put(s, currentState);
            }
            BiConsumer<S, C> exit = exitActions.get(s);
            if (exit != null) {
                exit.accept(s, context);
            }
        }

        // 2. 转移动作：传入 LCA；跨顶层转移时 LCA 为 null（UML 语义）
        try {
            t.executeAction(lca, context);
        } catch (RuntimeException ex) {
            notifyError(from, event, context, ex);
            throw new StateMachineException("Action failed during transition from '" + from
                    + "' on event '" + event + "'", ex);
        }

        // 3. 切换 currentState 到 target
        currentState = to;

        // 4. 进入：从 lca 向下到 target。lca 为 null 时直接 enter target
        if (lca != null) {
            List<S> enterChain = collectEnterChain(lca, to);
            for (S s : enterChain) {
                BiConsumer<S, C> entry = entryActions.get(s);
                if (entry != null) {
                    entry.accept(s, context);
                }
            }
        }
        BiConsumer<S, C> targetEntry = entryActions.get(to);
        if (targetEntry != null) {
            targetEntry.accept(to, context);
        }

        // 5. 如果 target 是 composite，下钻到其 initial 子态
        S resolved = descendIntoInitial(to, context);

        currentState = resolved;
        // 6. 记录每个祖先 composite 的 last active substate（H 恢复用）
        S ancestor = resolved;
        while (true) {
            S parent = parentMap.get(ancestor);
            if (parent == null) break;
            if (compositeInitialMap.containsKey(parent)) {
                lastActiveSubstate.put(parent, ancestor);
            }
            ancestor = parent;
        }
        notifyEnd(from, resolved, event, context);
        // 如果 resolved 是终态，通知状态机完成
        if (finalStates.contains(resolved)) {
            notifyComplete(resolved, context);
        }
        return resolved;
    }

    /**
     * 在不修改状态的前提下试探事件是否允许触发。
     */
    public S accept(E event, C context) {
        Transition<S, E, C> t = resolveTransition(currentState, event);
        if (t != null && t.isAllowed(context)) {
            return resolveTargetAfterEntry(t.resolveTarget(context));
        }
        return null;
    }

    /**
     * 查询在指定状态、给定事件，是否存在注册转移（不评估守卫）。
     */
    public boolean hasTransition(S state, E event) {
        return resolveTransition(state, event) != null;
    }

    /**
     * 判断某状态是否为 composite（有子状态）。
     */
    public boolean isComposite(S state) {
        return compositeInitialMap.containsKey(state);
    }

    /**
     * 判断某状态是否为 history（H）状态。
     */
    public boolean isHistory(S state) {
        return historyStates.contains(state);
    }

    /**
     * 判断是否为终态（进入后状态机视为完成，所有事件都会被拒收）。
     */
    public boolean isFinal(S state) {
        return finalStates.contains(state);
    }

    /**
     * 判断当前状态机是否已完成（已进入终态）。
     */
    public boolean isCompleted() {
        return finalStates.contains(currentState);
    }

    /**
     * 取得当前状态机的可序列化快照（用于持久化）。
     * <p>
     * 包含 {@code currentState} 与每个 composite 的 {@code lastActiveSubstate}（H 恢复用）。
     */
    public StateMachineSnapshot<S> getSnapshot() {
        return new StateMachineSnapshot<>(currentState, new HashMap<>(lastActiveSubstate));
    }

    /**
     * 从快照恢复运行时状态。会在 fire 之外被调用，因此需要自行加锁。
     */
    public synchronized void restoreFromSnapshot(StateMachineSnapshot<S> snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        this.currentState = snapshot.getCurrentState();
        this.lastActiveSubstate.clear();
        this.lastActiveSubstate.putAll(snapshot.getLastActiveSubstate());
    }

    /**
     * 取得指定状态的所有祖先（从自身到根）。
     */
    public List<S> ancestorsOf(S state) {
        List<S> ancestors = new ArrayList<>();
        S s = state;
        while (s != null) {
            ancestors.add(s);
            s = parentMap.get(s);
        }
        return ancestors;
    }

    // ==================== 内部 ====================

    /**
     * 沿父链向上查找第一个匹配 (state, event) 的转移。
     */
    private Transition<S, E, C> resolveTransition(S from, E event) {
        S s = from;
        while (s != null) {
            Map<E, Transition<S, E, C>> row = transitionTable.get(s);
            if (row != null) {
                Transition<S, E, C> t = row.get(event);
                if (t != null) return t;
            }
            s = parentMap.get(s);
        }
        return null;
    }

    /**
     * 找两个状态在 parentMap 中的最近公共祖先。
     */
    private S findLCA(S a, S b) {
        Set<S> ancestorsA = new HashSet<>();
        S s = a;
        while (s != null) {
            ancestorsA.add(s);
            s = parentMap.get(s);
        }
        s = b;
        while (s != null) {
            if (ancestorsA.contains(s)) return s;
            s = parentMap.get(s);
        }
        return null;
    }

    /**
     * 从 currentState 向上到（不包括）lca，组成退出链。返回 [currentState, ..., lca.child]
     */
    private List<S> collectExitChain(S from, S lca) {
        List<S> chain = new ArrayList<>();
        S s = from;
        while (s != null && !s.equals(lca)) {
            chain.add(s);
            s = parentMap.get(s);
        }
        return chain;
    }

    /**
     * 从 lca 向下到 to，组成进入链（包含 lca 的下一层和 to 自身）。
     * 注意 lca 自身不在此链中（lca 已经在场，无需重新进入）。
     */
    private List<S> collectEnterChain(S lca, S to) {
        List<S> toAncestors = ancestorsOf(to);
        int lcaIdx = toAncestors.indexOf(lca);
        if (lcaIdx < 0) {
            return Collections.emptyList();
        }
        // 取 toAncestors[0..lcaIdx)（不包含 lca）
        List<S> chain = new ArrayList<>(toAncestors.subList(0, lcaIdx));
        Collections.reverse(chain);
        return chain;
    }

    /**
     * 如果 state 是 composite，递归下钻到其 initial 子态（也包括 history 状态）。
     */
    private S descendIntoInitial(S state, C context) {
        S s = state;
        // 防死循环：visited 集合
        java.util.Set<S> visited = new java.util.HashSet<>();
        // 是否处于 history 恢复中（H 触发后的下钻用 lastActive；普通下钻只用 initial）
        boolean restoring = false;
        while (visited.add(s)) {
            if (historyStates.contains(s)) {
                // History 状态：恢复到其父 composite 上次激活的子态
                S parent = historyParentMap.get(s);
                S last = parent != null ? lastActiveSubstate.get(parent) : null;
                HistoryType type = historyTypes.getOrDefault(s, HistoryType.DEEP);
                if (last != null) {
                    if (type == HistoryType.SHALLOW) {
                        // 浅历史：只回到 parent 的直接子态
                        while (parentMap.get(last) != null && parentMap.get(last) != parent) {
                            last = parentMap.get(last);
                        }
                        restoring = false; // 浅历史：从直接子态开始按 initial 下钻
                    } else {
                        restoring = true;  // 深历史：递归按 lastActive 下钻
                    }
                    s = last;
                } else if (parent != null) {
                    restoring = false;
                    s = parent;
                } else {
                    // 没有父（孤立 H），保持 H
                    break;
                }
                continue;
            }
            // 普通 composite：深历史恢复中用 lastActive，否则用 initial
            S next;
            if (restoring && lastActiveSubstate.containsKey(s)) {
                next = lastActiveSubstate.get(s);
            } else {
                next = compositeInitialMap.get(s);
            }
            if (next == null) break;
            BiConsumer<S, C> entry = entryActions.get(next);
            if (entry != null) entry.accept(next, context);
            s = next;
        }
        return s;
    }

    /**
     * 用于 accept()：解析一个 transition 的最终目标态（处理 composite 下钻，不触发 entry）。
     */
    private S resolveTargetAfterEntry(S target) {
        S s = target;
        while (compositeInitialMap.containsKey(s) && !historyStates.contains(s)) {
            s = compositeInitialMap.get(s);
        }
        return s;
    }

    private void notifyBegin(S from, S to, E event, C context) {
        for (StateListener<S, E, C> l : listeners) {
            try {
                l.onTransitionBegin(from, to, event, context);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void notifyEnd(S from, S to, E event, C context) {
        for (StateListener<S, E, C> l : listeners) {
            try {
                l.onAction(from, to, event, context);
                l.onTransitionEnd(from, to, event, context);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void notifyRefused(S state, E event, C context, String reason) {
        for (StateListener<S, E, C> l : listeners) {
            try {
                l.onTransitionRefused(state, event, context, reason);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void notifyError(S state, E event, C context, Throwable error) {
        for (StateListener<S, E, C> l : listeners) {
            try {
                l.onError(state, event, context, error);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private void notifyComplete(S finalState, C context) {
        for (StateListener<S, E, C> l : listeners) {
            try {
                l.onStateMachineComplete(finalState, context);
            } catch (RuntimeException ignored) {
            }
        }
    }

    // ==================== Builder ====================

    public static class StateMachineBuilder<S, E, C> {

        private final Map<S, Map<E, Transition<S, E, C>>> table = new HashMap<>();
        private final java.util.List<StateListener<S, E, C>> listeners = new java.util.ArrayList<>();
        // 层级结构相关字段（与 StateMachine 同步，在 build() 时拷贝过去）
        private final Map<S, S> parentMap = new HashMap<>();
        private final Map<S, S> compositeInitialMap = new HashMap<>();
        private final Map<S, BiConsumer<S, C>> entryActions = new HashMap<>();
        private final Map<S, BiConsumer<S, C>> exitActions = new HashMap<>();
        private final Set<S> historyStates = new HashSet<>();
        // H 状态 -> 其父 composite 映射
        private final Map<S, S> historyParentMap = new HashMap<>();
        // H 状态 -> 恢复类型
        private final Map<S, HistoryType> historyTypes = new HashMap<>();
        // 终态集合
        private final Set<S> finalStates = new HashSet<>();
        private S initialState;
        // 最近一次 composite 声明的 state（用于把后续 state(s).history() 关联到这个 composite）
        private S lastComposite = null;

        private StateMachineBuilder() {
        }

        /**
         * initial方法。
         * * @param state S类型参数
         *
         * @return StateMachineBuilder<S, E, C>类型返回值
         */
        public StateMachineBuilder<S, E, C> initial(S state) {
            this.initialState = state;
            return this;
        }

        /**
         * 启动 from 配置，链上下一步是 {@link #on(Object)}。
         */
        public FromStateStep<S, E, C> from(S state) {
            return new FromStateStep<>(this, state);
        }

        /**
         * listener方法。
         * * @param listener StateListenerS,类型参数
         *
         * @return StateMachineBuilder<S, E, C>类型返回值
         */
        public StateMachineBuilder<S, E, C> listener(StateListener<S, E, C> listener) {
            if (listener != null) listeners.add(listener);
            return this;
        }

        /**
         * 声明一个状态（可用于挂 entry/exit 动作或标记 composite）。
         * 用法：
         * <pre>{@code
         * .state(OrderState.PAID).composite(OrderState.PENDING_INVOICE, sub -> ...)
         * .state(OrderState.SHIPPED).entry(ctx -> log.info("shipped")).exit(ctx -> log.info("leaving shipped"))
         * }</pre>
         */
        public StateConfigStep<S, E, C> state(S state) {
            return new StateConfigStep<>(this, state);
        }

        /**
         * 便捷方法：声明 state 为 composite，并指定初始子态与子状态机配置。
         */
        public StateMachineBuilder<S, E, C> composite(S state, S initialChild,
                                                      java.util.function.Consumer<SubMachineBuilder<S, E, C>> subConfig) {
            SubMachineBuilder<S, E, C> sub = new SubMachineBuilder<>(this, state, initialChild);
            this.lastComposite = state;
            subConfig.accept(sub);
            // 消费完子机 lambda 后，自动把 composite 关系注册到 root
            sub.end();
            return this;
        }

        /**
         * 声明一个 history（H）状态 — 属于其父 composite，激活时恢复到父 composite 上次激活的子态。
         */
        public StateMachineBuilder<S, E, C> history(S state) {
            this.historyStates.add(state);
            // H 状态视为 composite（拥有 default 子态），以便下钻
            // 实际恢复逻辑在 descendIntoInitial 中处理
            return this;
        }

        /**
         * 声明一个 history（H）状态，并指定恢复类型（{@link HistoryType#DEEP 深} 或 {@link HistoryType#SHALLOW 浅}）。
         */
        public StateMachineBuilder<S, E, C> history(S state, HistoryType type) {
            this.historyStates.add(state);
            this.historyTypes.put(state, type);
            return this;
        }

        /**
         * 声明一个终态（end / final state）。状态机进入此状态后，
         * 后续事件全部被拒收，{@link StateMachine#isCompleted()} 返回 true。
         */
        public StateMachineBuilder<S, E, C> end(S state) {
            this.finalStates.add(state);
            return this;
        }

        StateMachineBuilder<S, E, C> register(S from, E event, S to,
                                              Predicate<C> guard,
                                              BiConsumer<S, C> action) {
            table.computeIfAbsent(from, k -> new HashMap<>())
                    .put(event, new Transition<>(from, event, to, guard, action));
            return this;
        }

        /**
         * 注册一条内部转移（自循环，不触发 from 的 exit/to 的 enter）。
         * 等价于 {@code register(from, event, from, guard, action, internal=true)}。
         */
        StateMachineBuilder<S, E, C> registerInternal(S from, E event,
                                                      Predicate<C> guard,
                                                      BiConsumer<S, C> action) {
            table.computeIfAbsent(from, k -> new HashMap<>())
                    .put(event, new Transition<>(from, event, from, guard, action, true));
            return this;
        }

        /**
         * 注册一条 choice（条件分支）转移。branches 按顺序匹配首个 guard 命中的 target，
         * 否则用 defaultTarget。
         */
        StateMachineBuilder<S, E, C> registerChoice(S from, E event, S defaultTarget,
                                                    java.util.List<Transition.Branch<S, C>> branches,
                                                    Predicate<C> guard,
                                                    BiConsumer<S, C> action) {
            table.computeIfAbsent(from, k -> new HashMap<>())
                    .put(event, new Transition<S, E, C>(from, event, defaultTarget, branches, guard, action, false));
            return this;
        }

        void registerParent(S child, S parent) {
            this.parentMap.put(child, parent);
        }

        void registerComposite(S composite, S initialChild) {
            this.compositeInitialMap.put(composite, initialChild);
        }

        void registerEntry(S state, BiConsumer<S, C> action) {
            if (action != null) this.entryActions.put(state, action);
        }

        void registerExit(S state, BiConsumer<S, C> action) {
            if (action != null) this.exitActions.put(state, action);
        }

        void registerHistory(S state) {
            this.historyStates.add(state);
            this.historyTypes.put(state, HistoryType.DEEP);
            if (lastComposite != null) {
                this.historyParentMap.put(state, lastComposite);
                this.parentMap.put(state, lastComposite);
            }
            // 消费完 lastComposite 后清掉，避免污染后续 state 声明
            this.lastComposite = null;
        }

        void registerHistory(S state, HistoryType type) {
            this.historyStates.add(state);
            this.historyTypes.put(state, type);
            if (lastComposite != null) {
                this.historyParentMap.put(state, lastComposite);
                this.parentMap.put(state, lastComposite);
            }
            this.lastComposite = null;
        }

        void registerFinal(S state) {
            this.finalStates.add(state);
        }

        /**
         * build方法。
         *
         * @return StateMachine<S, E, C>类型返回值
         */
        public StateMachine<S, E, C> build() {
            if (initialState == null) {
                throw new StateMachineException("Initial state is required");
            }
            // 复制为不可变视图
            Map<S, Map<E, Transition<S, E, C>>> copy = new HashMap<>();
            for (Map.Entry<S, Map<E, Transition<S, E, C>>> e : table.entrySet()) {
                copy.put(e.getKey(), new HashMap<>(e.getValue()));
            }
            StateMachine<S, E, C> sm = new StateMachine<>(initialState, copy);
            sm.parentMap.putAll(this.parentMap);
            sm.compositeInitialMap.putAll(this.compositeInitialMap);
            sm.entryActions.putAll(this.entryActions);
            sm.exitActions.putAll(this.exitActions);
            sm.historyStates.addAll(this.historyStates);
            sm.historyParentMap.putAll(this.historyParentMap);
            sm.historyTypes.putAll(this.historyTypes);
            sm.finalStates.addAll(this.finalStates);
            for (StateListener<S, E, C> l : listeners) {
                if (l != null) sm.listeners.add(l);
            }
            // 初始下钻（在所有 map 都就绪后）
            sm.descendOnInit();
            return sm;
        }

        /**
         * 构建一个 {@link StateMachineFactory}：后续可用 {@code factory.create()} 批量
         * 创建独立实例（共享配置、独占状态）。
         */
        public StateMachineFactory<S, E, C> buildFactory() {
            return new StateMachineFactory<>(build());
        }
    }

    /**
     * from 配置入口。必须链 {@link OnEventStep#on(Object)}。
     */
    public static class FromStateStep<S, E, C> {

        private final StateMachineBuilder<S, E, C> parent;
        private final S from;
        private final S inComposite; // 非 null 表示这条 from 在 sub 上下文内（应把 from 注册为该 composite 的子态）

        FromStateStep(StateMachineBuilder<S, E, C> parent, S from) {
            this(parent, from, null);
        }

        FromStateStep(StateMachineBuilder<S, E, C> parent, S from, S inComposite) {
            this.parent = parent;
            this.from = from;
            this.inComposite = inComposite;
            if (inComposite != null) {
                parent.registerParent(from, inComposite);
            }
        }

        /**
         * on方法。
         * * @param event E类型参数
         *
         * @return OnEventStep<S, E, C>类型返回值
         */
        public OnEventStep<S, E, C> on(E event) {
            return new OnEventStep<>(parent, from, event, inComposite);
        }
    }

    public static class OnEventStep<S, E, C> {

        private final StateMachineBuilder<S, E, C> parent;
        private final S from;
        private final E event;
        private final S inComposite;
        private Predicate<C> guard;
        private BiConsumer<S, C> action;

        OnEventStep(StateMachineBuilder<S, E, C> parent, S from, E event) {
            this(parent, from, event, null);
        }

        OnEventStep(StateMachineBuilder<S, E, C> parent, S from, E event, S inComposite) {
            this.parent = parent;
            this.from = from;
            this.event = event;
            this.inComposite = inComposite;
        }

        /**
         * guard方法。
         * * @param guard PredicateC类型参数
         *
         * @return OnEventStep<S, E, C>类型返回值
         */
        public OnEventStep<S, E, C> guard(Predicate<C> guard) {
            Predicate<C> existing = this.guard;
            this.guard = existing == null ? guard : existing.and(guard);
            return this;
        }

        /**
         * action方法。
         * * @param action BiConsumerS,类型参数
         *
         * @return OnEventStep<S, E, C>类型返回值
         */
        public OnEventStep<S, E, C> action(BiConsumer<S, C> action) {
            this.action = action;
            return this;
        }

        /**
         * 结束 on().to() 链。
         * <p>
         * 如果本步在 composite 子机构建上下文内（{@code composite(...)} 的 lambda 内），
         * 则把 to 自动注册为该 composite 的子态。
         */
        public StateMachineBuilder<S, E, C> to(S to) {
            parent.register(from, event, to, guard, action);
            if (inComposite != null) {
                parent.registerParent(to, inComposite);
            }
            return parent;
        }

        /**
         * 声明一条内部转移（自循环）：事件触发后，状态保持不变，不调用 from 的 exit/to 的 enter，
         * 仅执行 action。等价于 {@code .to(from)}，但语义更清晰、避免误用。
         * <p>
         * 示例：
         * <pre>{@code
         *   .from(STATE_BUSY).on(Event.TICK).internal()
         *       .action((from, ctx) -> log.info("tick"));
         * }</pre>
         */
        public StateMachineBuilder<S, E, C> internal() {
            parent.registerInternal(from, event, guard, action);
            return parent;
        }

        /**
         * 开启 choice（条件分支）配置：本次转移不再固定到单一目标，而是按
         * {@link ChoiceStep#when} 注册的顺序匹配 guard，第一个通过的作为目标；
         * 都没命中则用 {@link ChoiceStep#otherwise} 给的默认目标。
         * <p>
         * 示例：
         * <pre>{@code
         *   .from(CHECK).on(CHECK_EVENT).choice()
         *       .when(ctx -> ctx.amount > 1000, HIGH)
         *       .when(ctx -> ctx.amount > 0,    LOW)
         *       .otherwise(FAIL)
         * }</pre>
         */
        public ChoiceStep<S, E, C> choice() {
            return new ChoiceStep<>(parent, from, event, guard, action, inComposite);
        }
    }

    /**
     * Choice 配置步骤：依次 {@link #when} 注册条件分支，{@link #otherwise} 给默认，最后 {@link #end} 收尾。
     */
    public static class ChoiceStep<S, E, C> {
        private final StateMachineBuilder<S, E, C> root;
        private final S from;
        private final E event;
        private final Predicate<C> guard;
        private final BiConsumer<S, C> action;
        private final S inComposite;
        private final java.util.List<Transition.Branch<S, C>> branches = new java.util.ArrayList<>();
        private S defaultTarget;

        ChoiceStep(StateMachineBuilder<S, E, C> root, S from, E event,
                   Predicate<C> guard, BiConsumer<S, C> action, S inComposite) {
            this.root = root;
            this.from = from;
            this.event = event;
            this.guard = guard;
            this.action = action;
            this.inComposite = inComposite;
        }

        /**
         * 添加一个条件分支：guard 通过时跳到 target。
         */
        public ChoiceStep<S, E, C> when(Predicate<C> branchGuard, S target) {
            branches.add(new Transition.Branch<>(branchGuard, target));
            if (inComposite != null) {
                root.registerParent(target, inComposite);
            }
            return this;
        }

        /**
         * 设置默认目标（所有分支 guard 都不命中时使用）。通常对应"错误/兜底"路径。
         */
        public ChoiceStep<S, E, C> otherwise(S target) {
            this.defaultTarget = target;
            if (inComposite != null) {
                root.registerParent(target, inComposite);
            }
            return this;
        }

        /**
         * 收尾注册到 builder。必须在 {@link #otherwise} 之后调用。
         */
        public StateMachineBuilder<S, E, C> end() {
            if (defaultTarget == null) {
                throw new StateMachineException(
                        "Choice transition from '" + from + "' on '" + event
                                + "' must call otherwise(target) before end()");
            }
            root.registerChoice(from, event, defaultTarget, branches, guard, action);
            return root;
        }
    }

    /**
     * state 配置入口。提供 entry/exit 动作与 composite 声明。
     */
    public static class StateConfigStep<S, E, C> {

        private final StateMachineBuilder<S, E, C> parent;
        private final S state;

        StateConfigStep(StateMachineBuilder<S, E, C> parent, S state) {
            this.parent = parent;
            this.state = state;
        }

        /**
         * entry方法。
         * * @param action BiConsumerS,类型参数
         *
         * @return StateMachineBuilder<S, E, C>类型返回值
         */
        public StateMachineBuilder<S, E, C> entry(BiConsumer<S, C> action) {
            parent.registerEntry(state, action);
            return parent;
        }

        /**
         * exit方法。
         * * @param action BiConsumerS,类型参数
         *
         * @return StateMachineBuilder<S, E, C>类型返回值
         */
        public StateMachineBuilder<S, E, C> exit(BiConsumer<S, C> action) {
            parent.registerExit(state, action);
            return parent;
        }

        /**
         * history方法。
         *
         * @return StateMachineBuilder<S, E, C>类型返回值
         */
        public StateMachineBuilder<S, E, C> history() {
            parent.registerHistory(state);
            return parent;
        }

        /**
         * 同 {@link #history()}，但可指定恢复类型。
         */
        public StateMachineBuilder<S, E, C> history(HistoryType type) {
            parent.registerHistory(state, type);
            return parent;
        }

        /**
         * 把当前 state 声明为终态。状态机进入此状态后，所有事件被拒收。
         */
        public StateMachineBuilder<S, E, C> end() {
            parent.registerFinal(state);
            return parent;
        }

        /**
         * 链式回到 transfer 配置入口（{@link #on(Object)} 之后）。
         */
        public FromStateStep<S, E, C> from(S from) {
            return new FromStateStep<>(parent, from);
        }

        /**
         * 把 state 声明为 composite，指定初始子态并通过 sub 配置子状态机。
         */
        public StateMachineBuilder<S, E, C> composite(S initialChild,
                                                      java.util.function.Consumer<SubMachineBuilder<S, E, C>> subConfig) {
            SubMachineBuilder<S, E, C> sub = new SubMachineBuilder<>(parent, state, initialChild);
            subConfig.accept(sub);
            return parent;
        }
    }

    /**
     * 子状态机构建器：在 composite 配置中，用于声明哪些 state 是当前 composite 的子态、配置子态内部转移。
     * <p>
     * 调用 {@link #child(S)} 显式声明哪些 state 属于本 composite（可选 — 引擎也能从 transfer 推断），
     * 然后通过 {@link #from(S)} 像顶层一样声明子态间的转移。
     */
    public static class SubMachineBuilder<S, E, C> {

        private final StateMachineBuilder<S, E, C> root;
        private final S composite;
        private final S initialChild;

        SubMachineBuilder(StateMachineBuilder<S, E, C> root, S composite, S initialChild) {
            this.root = root;
            this.composite = composite;
            this.initialChild = initialChild;
        }

        /**
         * child方法。
         * * @param state S类型参数
         *
         * @return SubMachineBuilder<S, E, C>类型返回值
         */
        public SubMachineBuilder<S, E, C> child(S state) {
            root.registerParent(state, composite);
            return this;
        }

        /**
         * 在子机里再嵌套一个 composite（grand-composite）。在子机 lambda 内调用时
         * 等价于 {@code root.composite(state, initialChild, sub -> ...)}。
         */
        public SubMachineBuilder<S, E, C> composite(S state, S initialChild,
                                                    java.util.function.Consumer<SubMachineBuilder<S, E, C>> subConfig) {
            SubMachineBuilder<S, E, C> sub = new SubMachineBuilder<>(root, state, initialChild);
            subConfig.accept(sub);
            sub.end();
            return this;
        }

        /**
         * from方法。
         * * @param state S类型参数
         *
         * @return FromStateStep<S, E, C>类型返回值
         */
        public FromStateStep<S, E, C> from(S state) {
            return new FromStateStep<>(root, state, composite);
        }

        /**
         * 显式结束子机构建（通常不必调用，链式 API 会自动回到 root）。
         */
        public StateMachineBuilder<S, E, C> end() {
            // 标记 composite 本身 — 在 end 时一次性注册 composite 关系
            root.registerComposite(composite, initialChild);
            return root;
        }
    }
}

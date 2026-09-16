package com.zifang.util.core.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 状态机中的"转移"定义。
 * <p>
 * 一条转移描述了在 {@code from} 状态下、收到 {@code event} 时，如何切换到 {@code to} 状态：
 * <ul>
 *   <li>{@code guard}：守卫条件。上下文通过时才能触发，否则保持原状态并触发 {@link StateListener#onTransitionRefused}</li>
 *   <li>{@code action}：转移执行期间的动作（可选）。在状态切换为 to 之前执行；抛错时转移中止并触发 {@link StateListener#onError}</li>
 *   <li>{@code internal}：内部转移（自循环），不触发 from 的 exit/to 的 enter。{@code to == from}</li>
 *   <li>{@code branches}：choice（条件分支）候选列表。fire 时按顺序匹配第一个 guard 命中的 target；都没命中则用 {@code defaultTarget}</li>
 * </ul>
 * 转移由 {@link StateMachineBuilder} 构造，无需用户直接 new。
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public class Transition<S, E, C> {

    private final S from;
    private final E event;
    private final S defaultTarget;
    private final List<Branch<S, C>> branches;
    private final Predicate<C> guard;
    private final BiConsumer<S, C> action;
    private final boolean internal;

    Transition(S from, E event, S to, Predicate<C> guard, BiConsumer<S, C> action) {
        this(from, event, to, null, guard, action, false);
    }

    Transition(S from, E event, S to, Predicate<C> guard, BiConsumer<S, C> action, boolean internal) {
        this(from, event, to, null, guard, action, internal);
    }

    Transition(S from, E event, S defaultTarget, List<Branch<S, C>> branches,
               Predicate<C> guard, BiConsumer<S, C> action, boolean internal) {
        this.from = Objects.requireNonNull(from, "from");
        this.event = Objects.requireNonNull(event, "event");
        this.defaultTarget = Objects.requireNonNull(defaultTarget, "defaultTarget");
        this.branches = branches == null ? null : Collections.unmodifiableList(new ArrayList<>(branches));
        this.guard = guard;
        this.action = action;
        this.internal = internal;
        if (internal && !from.equals(defaultTarget)) {
            throw new IllegalArgumentException(
                    "Internal transition must have from == target, but got from=" + from + " to=" + defaultTarget);
        }
    }

    /**
     * getFrom方法。
     *
     * @return S类型返回值
     */
    public S getFrom() {
        return from;
    }

    /**
     * getEvent方法。
     *
     * @return E类型返回值
     */
    public E getEvent() {
        return event;
    }

    /**
     * getTo方法。
     *
     * @return S类型返回值
     */
    public S getTo() {
        return defaultTarget;
    }

    /**
     * isInternal方法。
     *
     * @return boolean类型返回值
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * hasBranches方法。
     *
     * @return boolean类型返回值
     */
    public boolean hasBranches() {
        return branches != null && !branches.isEmpty();
    }

    /**
     * getBranches方法。
     *
     * @return List<Branch<S, C>>类型返回值
     */
    public List<Branch<S, C>> getBranches() {
        return branches;
    }

    /**
     * 根据 context 解析本次转移的真实目标。普通转移直接返回 defaultTarget；
     * choice 转移按 branches 顺序匹配第一个 guard 通过的 target，否则返回 defaultTarget。
     */
    public S resolveTarget(C context) {
        if (!hasBranches()) {
            return defaultTarget;
        }
        for (Branch<S, C> b : branches) {
            if (b.guard == null || b.guard.test(context)) {
                return b.target;
            }
        }
        return defaultTarget;
    }

    /**
     * 判断在当前上下文是否允许触发本转移。无 guard 时永远返回 true。
     */
    public boolean isAllowed(C context) {
        return guard == null || guard.test(context);
    }

    /**
     * 执行转移关联的动作。无 action 时直接返回。
     */
    public void executeAction(S fromState, C context) {
        if (action != null) {
            action.accept(fromState, context);
        }
    }

    /**
     * Choice 转移的单个分支：guard 满足时跳到 target。
     */
    public static class Branch<S, C> {
        public final Predicate<C> guard;
        public final S target;

        /**
         * Branch方法。
         * * @param guard PredicateC类型参数
         *
         * @param target S类型参数
         */
        public Branch(Predicate<C> guard, S target) {
            this.guard = guard;
            this.target = Objects.requireNonNull(target, "target");
        }
    }
}

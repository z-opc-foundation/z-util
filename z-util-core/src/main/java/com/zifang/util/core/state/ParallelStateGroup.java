package com.zifang.util.core.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 轻量级"并行 region"组：将 N 个独立 {@link StateMachine} 实例聚合成一个并行执行单元。
 * <p>
 * 每次 fire 时把同一事件 fan-out 到所有 region（每个 region 独立决策是否响应），
 * 收集每个 region 的转移结果。整体完成状态 = 所有 region 都完成。
 * <p>
 * 适用场景：业务对象有多条独立的状态子流（如订单 = 支付子流 + 物流子流 + 售后子流），
 * 每条子流有自己独立的状态机，事件来了同时尝试处理。
 * <p>
 * 与 UML statechart 中"orthogonal regions"的区别：本实现不维护统一的 currentState 列表，
 * 而是保持 N 个独立 currentState，并通过 fan-out 协调。如需真正 UML 风格的正交 region，
 * 需扩展 StateMachine 本身（不在本类范围内）。
 *
 * @param <S> 状态类型（所有 region 共享同一枚举/类型，每个 region 只使用其中一部分状态）
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public class ParallelStateGroup<S, E, C> {

    private final List<StateMachine<S, E, C>> regions = new ArrayList<>();
    private final List<String> regionNames = new ArrayList<>();

    ParallelStateGroup(List<StateMachine<S, E, C>> regions, List<String> regionNames) {
        this.regions.addAll(Objects.requireNonNull(regions, "regions"));
        this.regionNames.addAll(regionNames);
        if (this.regions.size() != this.regionNames.size()) {
            throw new IllegalArgumentException("regions and names must have the same size");
        }
    }

    /**
     * builder方法。
     *
     * @return static <S, E, C> Builder<S, E, C>类型返回值
     */
    public static <S, E, C> Builder<S, E, C> builder() {
        return new Builder<>();
    }

    /**
     * 把同一事件 fan-out 到所有 region（每个 region 独立尝试响应）。
     * 返回每个 region 的新 currentState（按 regionNames 顺序对齐）。
     */
    public synchronized List<S> fire(E event, C context, boolean failFast) {
        List<S> results = new ArrayList<>(regions.size());
        for (StateMachine<S, E, C> r : regions) {
            results.add(r.fire(event, context, failFast));
        }
        return results;
    }

    /**
     * fire方法。
     * * @param event E类型参数
     *
     * @param context C类型参数
     * @return synchronized List<S>类型返回值
     */
    public synchronized List<S> fire(E event, C context) {
        return fire(event, context, false);
    }

    /**
     * 取得每个 region 的当前状态。
     */
    public synchronized List<S> getCurrentStates() {
        List<S> out = new ArrayList<>(regions.size());
        for (StateMachine<S, E, C> r : regions) {
            out.add(r.getCurrentState());
        }
        return out;
    }

    /**
     * 取得指定 region 的当前状态。
     */
    public synchronized S getCurrentState(int regionIndex) {
        return regions.get(regionIndex).getCurrentState();
    }

    /**
     * 取得 region 数量。
     */
    public int regionCount() {
        return regions.size();
    }

    /**
     * 是否所有 region 都已完成。
     */
    public synchronized boolean isAllCompleted() {
        for (StateMachine<S, E, C> r : regions) {
            if (!r.isCompleted()) return false;
        }
        return true;
    }

    /**
     * 给指定 region 加监听器。
     */
    public void addListener(int regionIndex, StateListener<S, E, C> listener) {
        regions.get(regionIndex).addListener(listener);
    }

    /**
     * getRegionNames方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getRegionNames() {
        return Collections.unmodifiableList(regionNames);
    }

    /**
     * ParallelStateGroup builder。每个 region 是一个独立构建的 {@link StateMachine}。
     */
    public static class Builder<S, E, C> {
        private final List<String> names = new ArrayList<>();
        private final List<StateMachine<S, E, C>> machines = new ArrayList<>();

        /**
         * region方法。
         * * @param name String类型参数
         *
         * @param machine StateMachineS,类型参数
         * @return Builder<S, E, C>类型返回值
         */
        public Builder<S, E, C> region(String name, StateMachine<S, E, C> machine) {
            names.add(name);
            machines.add(machine);
            return this;
        }

        /**
         * build方法。
         *
         * @return ParallelStateGroup<S, E, C>类型返回值
         */
        public ParallelStateGroup<S, E, C> build() {
            return new ParallelStateGroup<>(new ArrayList<>(machines), new ArrayList<>(names));
        }
    }
}

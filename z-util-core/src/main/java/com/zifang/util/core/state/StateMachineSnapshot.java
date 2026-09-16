package com.zifang.util.core.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 状态机快照：可序列化的"实例状态"集合，用于持久化（DB / Redis / 文件等）。
 * <p>
 * 包含：
 * <ul>
 *   <li>{@code currentState}：当前激活状态（含历史 H 恢复后下钻到的最终叶子）</li>
 *   <li>{@code lastActiveSubstate}：每个 composite 的最近一次激活子态（H 恢复用）</li>
 * </ul>
 * 注意：本快照不包含事件 / guard / action / listener / 转移表 — 这些是"配置"，由 builder 单独管理。
 *
 * @param <S> 状态类型
 */
public class StateMachineSnapshot<S> {

    private final S currentState;
    private final Map<S, S> lastActiveSubstate;

    /**
     * StateMachineSnapshot方法。
     * * @param currentState S类型参数
     *
     * @param lastActiveSubstate MapS,类型参数
     */
    public StateMachineSnapshot(S currentState, Map<S, S> lastActiveSubstate) {
        this.currentState = currentState;
        this.lastActiveSubstate = lastActiveSubstate == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(lastActiveSubstate));
    }

    /**
     * getCurrentState方法。
     *
     * @return S类型返回值
     */
    public S getCurrentState() {
        return currentState;
    }

    /**
     * getLastActiveSubstate方法。
     *
     * @return Map<S, S>类型返回值
     */
    public Map<S, S> getLastActiveSubstate() {
        return lastActiveSubstate;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "StateMachineSnapshot{currentState=" + currentState
                + ", lastActiveSubstate=" + lastActiveSubstate + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateMachineSnapshot)) return false;
        StateMachineSnapshot<?> that = (StateMachineSnapshot<?>) o;
        return Objects.equals(currentState, that.currentState)
                && Objects.equals(lastActiveSubstate, that.lastActiveSubstate);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(currentState, lastActiveSubstate);
    }
}

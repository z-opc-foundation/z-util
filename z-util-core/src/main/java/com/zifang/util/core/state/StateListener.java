package com.zifang.util.core.state;

/**
 * 状态机状态变化监听器。
 * <p>
 * 监听器按以下顺序触发：
 * <ol>
 *   <li>{@link #onTransitionBegin} — 转移开始（含守卫通过后）</li>
 *   <li>{@link #onAction} — 动作执行（动作未注册时不触发）</li>
 *   <li>{@link #onTransitionEnd} — 转移完成</li>
 *   <li>{@link #onTransitionRefused} — 转移被拒绝（无匹配转移或守卫失败）</li>
 *   <li>{@link #onError} — 转移过程中抛错</li>
 * </ol>
 *
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public interface StateListener<S, E, C> {

    /**
     * 转移开始（守卫通过、即将执行动作/切换状态时）回调。
     *
     * @param from    源状态
     * @param to      目标状态
     * @param event   触发事件
     * @param context 业务上下文
     */
    default void onTransitionBegin(S from, S to, E event, C context) {
    }

    /**
     * 动作执行回调（仅当转移配置了 action 时触发）。
     *
     * @param from    源状态
     * @param to      目标状态
     * @param event   触发事件
     * @param context 业务上下文
     */
    default void onAction(S from, S to, E event, C context) {
    }

    /**
     * 转移完成回调。
     *
     * @param from    源状态
     * @param to      目标状态
     * @param event   触发事件
     * @param context 业务上下文
     */
    default void onTransitionEnd(S from, S to, E event, C context) {
    }

    /**
     * 转移被拒绝回调。
     *
     * @param state   当前状态
     * @param event   触发事件
     * @param context 业务上下文
     * @param reason  拒绝原因
     */
    default void onTransitionRefused(S state, E event, C context, String reason) {
    }

    /**
     * 转移过程中抛错回调。
     *
     * @param state   当前状态
     * @param event   触发事件
     * @param context 业务上下文
     * @param error   异常
     */
    default void onError(S state, E event, C context, Throwable error) {
    }

    /**
     * 状态机进入终态回调（仅当 to 是终态时触发一次）。
     *
     * @param finalState 进入的终态
     * @param context    业务上下文
     */
    default void onStateMachineComplete(S finalState, C context) {
    }
}

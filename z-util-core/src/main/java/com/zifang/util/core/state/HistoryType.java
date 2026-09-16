package com.zifang.util.core.state;

/**
 * History（H）状态恢复策略。
 * <ul>
 *   <li>{@link #DEEP}（默认）：恢复到该 composite 上次激活的最终叶子状态（递归下钻）</li>
 *   <li>{@link #SHALLOW}：只恢复到该 composite 的直接子状态（不深入），再按正常初始下钻</li>
 * </ul>
 */
public enum HistoryType {
    DEEP,
    SHALLOW
}

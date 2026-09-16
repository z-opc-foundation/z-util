package com.zifang.util.core.schedule;

/**
 * 失火策略（任务到期未执行时的处理方式）。
 */
public enum MisfirePolicy {
    /** 默认：尽力赶上，错过的执行立刻补一次。 */
    SMART_POLICY,
    /** 忽略错过的执行。 */
    IGNORE_MISFIRES,
    /** 错过的执行都补一遍。 */
    FIRE_AND_PROCEED,
    /** 错过的执行都不补，直接跳到下一次。 */
    DO_NOTHING
}

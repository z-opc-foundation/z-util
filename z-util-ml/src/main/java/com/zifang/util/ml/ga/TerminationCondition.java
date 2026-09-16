package com.zifang.util.ml.ga;

/**
 * 终止条件接口 - 定义算法何时停止
 */
public interface TerminationCondition {

    /**
     * 检查是否满足终止条件
     *
     * @param generation 当前代数
     * @param population 当前种群
     * @return true表示应该终止
     */
    boolean isTerminated(int generation, Population population);
}
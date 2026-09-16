package com.zifang.util.ml.ga;

/**
 * 适应度评估器接口
 * <p>
 * 定义遗传算法中个体适应度评估的标准接口。
 * 适应度函数用于衡量个体对问题的解决程度，
 * 是遗传算法选择操作的核心依据。
 *
 * <p>主要职责：
 * <ul>
 *   <li>评估个体的问题解决能力</li>
 *   <li>将个体的基因型转换为适应度值</li>
 *   <li>为选择算子提供选择依据</li>
 * </ul>
 *
 * <p>设计原则：
 * <ul>
 *   <li>适应度值通常为非负数</li>
 *   <li>适应度值越大表示个体越优秀</li>
 *   <li>适应度评估应该高效，以便在大种群中快速计算</li>
 * </ul>
 *
 * @author zifang
 * @see Individual
 * @see GeneticAlgorithmEngine
 * @see SelectionOperator
 */
public interface FitnessFunction {

    /**
     * 计算个体的适应度
     *
     * @param individual 要评估的个体
     * @return 适应度值，非负数，越大表示越优秀
     */
    double evaluate(Individual individual);
}
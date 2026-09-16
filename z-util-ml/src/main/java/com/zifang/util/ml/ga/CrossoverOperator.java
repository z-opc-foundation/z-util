package com.zifang.util.ml.ga;

/**
 * 交叉算子接口
 * <p>
 * 定义遗传算法中交叉操作的标准接口。
 * 交叉操作模拟生物界的基因重组，通过组合两个父代的基因信息
 * 产生新的后代，是遗传算法探索解空间的主要机制。
 *
 * <p>主要职责：
 * <ul>
 *   <li>从种群中选择两个父代个体</li>
 *   <li>按照某种策略交换基因片段</li>
 *   <li>产生一至两个子代个体</li>
 * </ul>
 *
 * <p>常见交叉策略：
 * <ul>
 *   <li>单点交叉：在基因串上随机选择一个切点进行交换</li>
 *   <li>两点交叉：选择两个切点，交换中间片段</li>
 *   <li>均匀交叉：每个基因位独立决定来自哪个父代</li>
 *   <li>算术交叉：针对实数编码，计算加权和</li>
 * </ul>
 *
 * @author zifang
 * @see Individual
 * @see MutationOperator
 * @see GeneticAlgorithmEngine
 */
public interface CrossoverOperator {

    /**
     * 对两个父代进行交叉操作，产生两个子代
     *
     * @param parent1 第一个父代个体
     * @param parent2 第二个父代个体
     * @return 包含两个子代个体的数组
     */
    Individual[] crossover(Individual parent1, Individual parent2);
}
package com.zifang.util.ml.ga;

/**
 * 变异算子接口
 * <p>
 * 定义遗传算法中变异操作的标准接口。
 * 变异操作模拟生物界的基因突变，通过随机改变个体的基因信息
 * 来引入新的遗传物质，是遗传算法避免局部最优的重要机制。
 *
 * <p>主要职责：
 * <ul>
 *   <li>以一定概率对个体的基因进行随机修改</li>
 *   <li>增加种群的多样性</li>
 *   <li>帮助算法跳出局部最优解</li>
 * </ul>
 *
 * <p>常见变异策略：
 * <ul>
 *   <li>位翻转变异：针对二进制编码，0变1或1变0</li>
 *   <li>高斯变异：针对实数编码，添加高斯噪声</li>
 *   <li>均匀变异：随机选择基因位，用均匀分布的值替换</li>
 *   <li>边界变异：用参数的上下界替换基因值</li>
 * </ul>
 *
 * @author zifang
 * @see Individual
 * @see CrossoverOperator
 * @see GeneticAlgorithmEngine
 */
public interface MutationOperator {

    /**
     * 对个体进行变异操作
     *
     * @param individual   要变异的个体
     * @param mutationRate 变异概率，通常在0.01到0.1之间
     */
    void mutate(Individual individual, double mutationRate);
}
package com.zifang.util.ml.ga;

import java.util.Random;

/**
 * 单点交叉算子
 */
public class SinglePointCrossover implements CrossoverOperator {

    private final Random random = new Random();

    /**
     * 对两个父代进行单点交叉，产生两个子代
     *
     * @param parent1 第一个父代
     * @param parent2 第二个父代
     * @return 包含两个子代的数组
     */
    @Override
    /**
     * crossover方法。
     *      * @param parent1 Individual类型参数
     * @param parent2 Individual类型参数
     * @return Individual[]类型返回值
     */
    public Individual[] crossover(Individual parent1, Individual parent2) {
        Individual[] result = new Individual[2];
        result[0] = parent1.copy();
        result[1] = parent2.copy();

        int crossoverPoint = random.nextInt(parent1.length());

        for (int i = crossoverPoint; i < parent1.length(); i++) {
            Object temp = result[0].getGene(i);
            result[0].setGene(i, result[1].getGene(i));
            result[1].setGene(i, temp);
        }

        return result;
    }
}
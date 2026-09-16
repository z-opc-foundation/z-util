package com.zifang.util.ml.ga;

import java.util.Random;

/**
 * 二进制翻转变异算子
 */
public class BinaryMutation implements MutationOperator {

    private final Random random = new Random();

    /**
     * 对二进制基因型个体进行翻转变异
     *
     * @param individual   要变异的个体，必须是BinaryGenotype类型
     * @param mutationRate 变异率，范围[0, 1]
     */
    @Override
    /**
     * mutate方法。
     *      * @param individual Individual类型参数
     * @param mutationRate double类型参数
     */
    public void mutate(Individual individual, double mutationRate) {
        for (int i = 0; i < individual.length(); i++) {
            if (random.nextDouble() < mutationRate) {
                if (individual instanceof BinaryGenotype) {
                    BinaryGenotype bg = (BinaryGenotype) individual;
                    int currentGene = bg.getGene(i);
                    bg.setGene(i, currentGene == 1 ? 0 : 1);
                }
            }
        }
    }
}
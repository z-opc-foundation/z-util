package com.zifang.util.ml.ga;

import java.util.Objects;

/**
 * 个体 - 代表种群中的单个成员
 */
public class Individual {

    private final Object[] chromosome;
    private double fitness = -1;

    /**
     * 构造一个具有指定染色体长度的个体
     *
     * @param length 染色体长度
     */
    public Individual(int length) {
        this.chromosome = new Object[length];
    }

    /**
     * 构造一个具有给定染色体的个体
     *
     * @param chromosome 染色体数组，不能为null
     */
    public Individual(Object[] chromosome) {
        this.chromosome = Objects.requireNonNull(chromosome);
    }

    /**
     * 获取染色体的长度
     *
     * @return 染色体长度
     */
    public int length() {
        return chromosome.length;
    }

    /**
     * 获取指定位置的基因
     *
     * @param index 基因索引
     * @return 基因值
     */
    public Object getGene(int index) {
        return chromosome[index];
    }

    /**
     * 设置指定位置的基因
     *
     * @param index 基因索引
     * @param gene  基因值
     */
    public void setGene(int index, Object gene) {
        chromosome[index] = gene;
    }

    /**
     * 创建当前个体的深拷贝
     *
     * @return 个体的副本
     */
    public Individual copy() {
        Individual result = new Individual(this.chromosome.length);
        for (int i = 0; i < chromosome.length; i++) {
            result.setGene(i, this.chromosome[i]);
        }
        result.fitness = this.fitness;
        return result;
    }

    /**
     * 随机初始化个体的基因
     *
     * @throws UnsupportedOperationException 子类未实现此方法时抛出
     */
    public void randomize() {
        throw new UnsupportedOperationException("Subclass must implement randomize()");
    }

    /**
     * 获取个体的适应度值
     *
     * @return 适应度值
     */
    public double getFitness() {
        return fitness;
    }

    /**
     * 设置个体的适应度值
     *
     * @param fitness 适应度值
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object gene : chromosome) {
            sb.append(gene);
        }
        return sb.toString();
    }
}
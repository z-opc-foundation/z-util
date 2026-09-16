package com.zifang.util.ml.ga;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * 种群
 */
public class Population {

    private final List<Individual> individuals;
    private final Random random;
    private double totalFitness = -1;

    /**
     * 构造一个空的种群
     */
    public Population() {
        this.individuals = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * 构造一个具有初始容量的种群
     *
     * @param size 初始容量
     */
    public Population(int size) {
        this.individuals = new ArrayList<>(size);
        this.random = new Random();
    }

    /**
     * 向种群中添加一个个体
     *
     * @param individual 要添加的个体
     */
    public void addIndividual(Individual individual) {
        individuals.add(individual);
    }

    /**
     * 获取指定索引位置的个体
     *
     * @param index 索引位置
     * @return 个体对象
     */
    public Individual getIndividual(int index) {
        return individuals.get(index);
    }

    /**
     * 设置指定索引位置的个体
     *
     * @param index      索引位置
     * @param individual 要设置的个体
     * @return 被替换的旧个体
     */
    public Individual setIndividual(int index, Individual individual) {
        return individuals.set(index, individual);
    }

    /**
     * 获取种群中个体的数量
     *
     * @return 个体数量
     */
    public int size() {
        return individuals.size();
    }

    /**
     * 获取种群中所有个体的副本
     *
     * @return 包含所有个体的列表
     */
    public List<Individual> getIndividuals() {
        return new ArrayList<>(individuals);
    }

    /**
     * 获取种群中适应度最高的个体
     *
     * @return 适应度最高的个体
     */
    public Individual getFittest() {
        return individuals.stream()
                .max(Comparator.comparingDouble(Individual::getFitness))
                .orElse(individuals.get(0));
    }

    /**
     * 获取种群中适应度第n高的个体（已按适应度降序排序）
     *
     * @param offset 偏移量，从0开始
     * @return 排序后对应偏移量的个体
     */
    public Individual getFittest(int offset) {
        individuals.sort(Comparator.comparingDouble(Individual::getFitness).reversed());
        if (offset < 0 || offset >= individuals.size()) {
            offset = 0;
        }
        return individuals.get(offset);
    }

    /**
     * 获取种群的总适应度
     *
     * @return 总适应度值
     */
    public double getTotalFitness() {
        return totalFitness;
    }

    /**
     * 设置种群的总适应度
     *
     * @param totalFitness 总适应度值
     */
    public void setTotalFitness(double totalFitness) {
        this.totalFitness = totalFitness;
    }

    /**
     * 计算种群中所有个体的总适应度
     *
     * @return 总适应度值
     */
    public double calculateTotalFitness() {
        this.totalFitness = individuals.stream()
                .mapToDouble(Individual::getFitness)
                .sum();
        return this.totalFitness;
    }

    /**
     * 使用轮盘赌算法从种群中选择一个个体
     *
     * @return 被选中的个体
     */
    public Individual rouletteSelect() {
        if (totalFitness <= 0) {
            return individuals.get(random.nextInt(individuals.size()));
        }
        double point = random.nextDouble() * totalFitness;
        double accumulated = 0;
        for (Individual individual : individuals) {
            accumulated += individual.getFitness();
            if (accumulated >= point) {
                return individual;
            }
        }
        return individuals.get(individuals.size() - 1);
    }

    /**
     * 随机打乱种群中个体的顺序
     */
    public void shuffle() {
        for (int i = individuals.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Individual temp = individuals.get(i);
            individuals.set(i, individuals.get(j));
            individuals.set(j, temp);
        }
    }
}
package com.zifang.util.visuallization.chart;

import com.zifang.util.ml.ga.Individual;
import com.zifang.util.ml.ga.Population;

import java.util.ArrayList;
import java.util.List;

/**
 * 遗传算法可视化器
 * 用于可视化遗传算法进化过程中的适应度变化和最优染色体
 */
public class GAVisualizer {

    private final LineChart fitnessChart;
    private final BarChart chromosomeChart;
    private final List<Double> bestFitnessHistory;
    private final List<Double> avgFitnessHistory;
    private int generation = 0;

    /**
     * 创建遗传算法可视化器
     */
    public GAVisualizer() {
        this.fitnessChart = new LineChart("GA - Fitness Evolution", 600, 400);
        this.chromosomeChart = new BarChart("GA - Best Chromosome", 600, 400);
        this.bestFitnessHistory = new ArrayList<>();
        this.avgFitnessHistory = new ArrayList<>();
    }

    /**
     * 更新可视化数据
     *
     * @param population 当前种群
     */
    public void update(Population population) {
        Individual fittest = population.getFittest();

        bestFitnessHistory.add(fittest.getFitness());
        avgFitnessHistory.add(population.getTotalFitness() / population.size());

        updateFitnessChart();
        updateChromosomeChart(fittest);

        generation++;
    }

    /**
     * 获取适应度曲线图组件
     *
     * @return 适应度曲线图实例
     */
    public LineChart getFitnessChart() {
        return fitnessChart;
    }

    /**
     * 获取染色体条形图组件
     *
     * @return 染色体条形图实例
     */
    public BarChart getChromosomeChart() {
        return chromosomeChart;
    }

    /**
     * 获取当前代数
     *
     * @return 当前进化代数
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * 获取最优适应度历史记录
     *
     * @return 最优适应度列表副本
     */
    public List<Double> getBestFitnessHistory() {
        return new ArrayList<>(bestFitnessHistory);
    }

    /**
     * 更新适应度图表
     */
    private void updateFitnessChart() {
        fitnessChart.clear();
        fitnessChart.addSeries("Best Fitness", new ArrayList<>(bestFitnessHistory));
        fitnessChart.addSeries("Avg Fitness", new ArrayList<>(avgFitnessHistory));
        fitnessChart.setAxisLabels("Generation", "Fitness");
        fitnessChart.render();
    }

    /**
     * 更新染色体图表
     *
     * @param individual 最优个体
     */
    private void updateChromosomeChart(Individual individual) {
        chromosomeChart.clear();
        List<Double> geneValues = new ArrayList<>();
        for (int i = 0; i < individual.length(); i++) {
            Object gene = individual.getGene(i);
            if (gene instanceof Number) {
                geneValues.add(((Number) gene).doubleValue());
            } else {
                geneValues.add(gene.toString().equals("1") ? 1.0 : 0.0);
            }
        }
        chromosomeChart.addSeries("Genes", geneValues);
        chromosomeChart.setAxisLabels("Gene Index", "Value");
        chromosomeChart.render();
    }
}

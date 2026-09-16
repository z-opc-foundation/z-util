package com.zifang.util.ml.ga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 遗传算法集成测试 - 使用GA解决OneMax问题
 */

/**
 * GeneticAlgorithmIntegrationTest类。
 */
public class GeneticAlgorithmIntegrationTest {

    @Test
    void testSolveOneMaxProblem() {
        int chromosomeLength = 20;
        int populationSize = 100;
        int maxGenerations = 500;

        GeneticAlgorithmEngine engine = GeneticAlgorithmEngine.builder()
                .populationSize(populationSize)
                .mutationRate(0.02)
                .crossoverRate(0.9)
                .elitismCount(5)
                .individualFactory(() -> new BinaryGenotype(chromosomeLength))
                .fitnessFunction(ind -> {
                    int count = 0;
                    for (int i = 0; i < ind.length(); i++) {
                        if (ind.getGene(i).equals(1)) count++;
                    }
                    return (double) count / ind.length();
                })
                .crossoverOperator(new SinglePointCrossover())
                .mutationOperator(new BinaryMutation())
                .terminationCondition(new TargetFitnessTermination(0.999))
                .build();

        Population result = engine.evolve(maxGenerations);

        Individual fittest = result.getFittest();
        assertTrue(fittest.getFitness() > 0.9, "Should find near-optimal solution");

        System.out.println("OneMax Problem Result:");
        System.out.println("Best chromosome: " + fittest);
        System.out.println("Best fitness: " + fittest.getFitness());
    }

    @Test
    void testCustomIndividualEvolution() {
        // 自定义基因型：整数基因，范围0-9
        int chromosomeLength = 5;
        int populationSize = 50;

        GeneticAlgorithmEngine engine = GeneticAlgorithmEngine.builder()
                .populationSize(populationSize)
                .mutationRate(0.1)
                .crossoverRate(0.8)
                .elitismCount(2)
                .individualFactory(() -> new Individual(chromosomeLength) {
                    @Override
                    /**
                     * randomize方法。
                     */
                    public void randomize() {
                        for (int i = 0; i < length(); i++) {
                            setGene(i, (int) (Math.random() * 10));
                        }
                    }
                })
                .fitnessFunction(ind -> {
                    int sum = 0;
                    for (int i = 0; i < ind.length(); i++) {
                        sum += (int) ind.getGene(i);
                    }
                    return sum;
                })
                .crossoverOperator((p1, p2) -> {
                    Individual[] result = new Individual[2];
                    result[0] = p1.copy();
                    result[1] = p2.copy();

                    int crossoverPoint = (int) (Math.random() * p1.length());
                    for (int i = crossoverPoint; i < p1.length(); i++) {
                        Object temp = result[0].getGene(i);
                        result[0].setGene(i, result[1].getGene(i));
                        result[1].setGene(i, temp);
                    }
                    return result;
                })
                .mutationOperator((ind, rate) -> {
                    for (int i = 0; i < ind.length(); i++) {
                        if (Math.random() < rate) {
                            ind.setGene(i, (int) (Math.random() * 10));
                        }
                    }
                })
                .terminationCondition((gen, pop) -> gen >= 100)
                .build();

        Population result = engine.evolve(100);

        System.out.println("Custom Individual Evolution Result:");
        System.out.println("Best genes: ");
        for (int i = 0; i < result.getFittest().length(); i++) {
            System.out.print(result.getFittest().getGene(i) + " ");
        }
        System.out.println("\nBest fitness: " + result.getFittest().getFitness());
    }
}
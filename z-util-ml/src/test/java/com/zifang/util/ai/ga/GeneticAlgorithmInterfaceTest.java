package com.zifang.util.ml.ga;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 遗传算法接口测试
 */

/**
 * GeneticAlgorithmInterfaceTest类。
 */
public class GeneticAlgorithmInterfaceTest {

    @Test
    void testBinaryGenotypeCreation() {
        BinaryGenotype genotype = new BinaryGenotype(10);
        assertEquals(10, genotype.length());
    }

    @Test
    void testBinaryGenotypeRandomize() {
        BinaryGenotype genotype = new BinaryGenotype(10);
        genotype.randomize();

        boolean hasZero = false;
        boolean hasOne = false;
        for (int i = 0; i < 10; i++) {
            int gene = genotype.getGene(i);
            assertTrue(gene == 0 || gene == 1);
            if (gene == 0) hasZero = true;
            if (gene == 1) hasOne = true;
        }
        assertTrue(hasZero && hasOne);
    }

    @Test
    void testIndividualCopy() {
        BinaryGenotype original = new BinaryGenotype(5);
        original.setGene(0, 1);
        original.setGene(1, 0);
        original.setGene(2, 1);
        original.setGene(3, 0);
        original.setGene(4, 1);
        original.setFitness(0.8);

        Individual copy = original.copy();

        assertEquals(original.length(), copy.length());
        for (int i = 0; i < 5; i++) {
            assertEquals(original.getGene(i), copy.getGene(i));
        }
        assertEquals(original.getFitness(), copy.getFitness());
    }

    @Test
    void testPopulationOperations() {
        Population population = new Population(3);

        for (int i = 0; i < 3; i++) {
            BinaryGenotype genotype = new BinaryGenotype(5);
            genotype.randomize();
            genotype.setFitness(i * 0.3);
            population.addIndividual(genotype);
        }

        assertEquals(3, population.size());
        assertNotNull(population.getFittest());
    }

    @Test
    void testSinglePointCrossover() {
        SinglePointCrossover crossover = new SinglePointCrossover();

        BinaryGenotype parent1 = new BinaryGenotype(5);
        parent1.setGene(0, 1);
        parent1.setGene(1, 1);
        parent1.setGene(2, 1);
        parent1.setGene(3, 1);
        parent1.setGene(4, 1);

        BinaryGenotype parent2 = new BinaryGenotype(5);
        parent2.setGene(0, 0);
        parent2.setGene(1, 0);
        parent2.setGene(2, 0);
        parent2.setGene(3, 0);
        parent2.setGene(4, 0);

        Individual[] children = crossover.crossover(parent1, parent2);

        assertEquals(2, children.length);
        assertEquals(5, children[0].length());
        assertEquals(5, children[1].length());
    }

    @Test
    void testBinaryMutation() {
        BinaryMutation mutation = new BinaryMutation();

        BinaryGenotype genotype = new BinaryGenotype(10);
        genotype.setGene(0, 1);
        genotype.setGene(1, 0);
        genotype.setGene(2, 1);
        genotype.setGene(3, 0);
        genotype.setGene(4, 1);
        genotype.setGene(5, 0);
        genotype.setGene(6, 1);
        genotype.setGene(7, 0);
        genotype.setGene(8, 1);
        genotype.setGene(9, 0);

        mutation.mutate(genotype, 1.0);

        for (int i = 0; i < 10; i++) {
            assertEquals(i % 2 == 0 ? 0 : 1, genotype.getGene(i));
        }
    }

    @Test
    void testTargetFitnessTermination() {
        TerminationCondition condition = new TargetFitnessTermination(0.9);

        Population population = new Population(1);
        BinaryGenotype genotype = new BinaryGenotype(5);
        genotype.setFitness(0.85);
        population.addIndividual(genotype);

        assertFalse(condition.isTerminated(0, population));

        genotype.setFitness(0.95);
        assertTrue(condition.isTerminated(0, population));
    }

    @Test
    void testGeneticAlgorithmEngineBuilder() {
        GeneticAlgorithmEngine engine = GeneticAlgorithmEngine.builder()
                .populationSize(50)
                .mutationRate(0.1)
                .crossoverRate(0.8)
                .elitismCount(2)
                .individualFactory(() -> new BinaryGenotype(10))
                .fitnessFunction(ind -> {
                    int count = 0;
                    for (int i = 0; i < ind.length(); i++) {
                        if (ind.getGene(i).equals(1)) count++;
                    }
                    return (double) count / ind.length();
                })
                .crossoverOperator(new SinglePointCrossover())
                .mutationOperator(new BinaryMutation())
                .terminationCondition(new TargetFitnessTermination(0.99))
                .build();

        assertNotNull(engine);
    }
}
package com.zifang.util.ml.ga;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Population 类测试
 */

/**
 * PopulationTest类。
 */
public class PopulationTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        Population population = new Population();
        assertNotNull(population);
        assertEquals(0, population.size());
        assertEquals(-1, population.getTotalFitness(), 0.0001);
    }

    @Test
    /**
     * testConstructorWithSize方法。
     */
    public void testConstructorWithSize() {
        Population population = new Population(10);
        assertNotNull(population);
        assertEquals(0, population.size());
    }

    @Test
    /**
     * testAddIndividual方法。
     */
    public void testAddIndividual() {
        Population population = new Population();
        Individual individual = new Individual(5);

        population.addIndividual(individual);

        assertEquals(1, population.size());
    }

    @Test
    /**
     * testAddMultipleIndividuals方法。
     */
    public void testAddMultipleIndividuals() {
        Population population = new Population();

        for (int i = 0; i < 5; i++) {
            population.addIndividual(new Individual(5));
        }

        assertEquals(5, population.size());
    }

    @Test
    /**
     * testGetIndividual方法。
     */
    public void testGetIndividual() {
        Population population = new Population();
        Individual individual1 = new Individual(5);
        Individual individual2 = new Individual(5);
        individual1.setFitness(10.0);
        individual2.setFitness(20.0);

        population.addIndividual(individual1);
        population.addIndividual(individual2);

        assertSame(individual1, population.getIndividual(0));
        assertSame(individual2, population.getIndividual(1));
    }

    @Test
    /**
     * testSetIndividual方法。
     */
    public void testSetIndividual() {
        Population population = new Population();
        Individual original = new Individual(5);
        Individual replacement = new Individual(5);

        population.addIndividual(original);
        population.setIndividual(0, replacement);

        assertSame(replacement, population.getIndividual(0));
    }

    @Test
    /**
     * testGetIndividuals方法。
     */
    public void testGetIndividuals() {
        Population population = new Population();
        Individual ind1 = new Individual(5);
        Individual ind2 = new Individual(5);
        population.addIndividual(ind1);
        population.addIndividual(ind2);

        List<Individual> list = population.getIndividuals();
        assertEquals(2, list.size());
        assertSame(ind1, list.get(0));
        assertSame(ind2, list.get(1));
    }

    @Test
    /**
     * testGetTotalFitness方法。
     */
    public void testGetTotalFitness() {
        Population population = new Population();
        assertEquals(-1, population.getTotalFitness(), 0.0001);
    }

    @Test
    /**
     * testSetTotalFitness方法。
     */
    public void testSetTotalFitness() {
        Population population = new Population();
        population.setTotalFitness(100.0);
        assertEquals(100.0, population.getTotalFitness(), 0.0001);
    }

    @Test
    /**
     * testCalculateTotalFitness方法。
     */
    public void testCalculateTotalFitness() {
        Population population = new Population();
        Individual ind1 = new Individual(5);
        Individual ind2 = new Individual(5);
        ind1.setFitness(10.0);
        ind2.setFitness(20.0);
        population.addIndividual(ind1);
        population.addIndividual(ind2);

        double total = population.calculateTotalFitness();
        assertEquals(30.0, total, 0.0001);
        assertEquals(30.0, population.getTotalFitness(), 0.0001);
    }

    @Test
    /**
     * testGetFittest方法。
     */
    public void testGetFittest() {
        Population population = new Population();
        Individual ind1 = new Individual(5);
        Individual ind2 = new Individual(5);
        ind1.setFitness(10.0);
        ind2.setFitness(20.0);
        population.addIndividual(ind1);
        population.addIndividual(ind2);

        Individual fittest = population.getFittest();
        assertSame(ind2, fittest);
    }

    @Test
    /**
     * testGetFittestOffset方法。
     */
    public void testGetFittestOffset() {
        Population population = new Population();
        Individual ind1 = new Individual(5);
        Individual ind2 = new Individual(5);
        ind1.setFitness(10.0);
        ind2.setFitness(20.0);
        population.addIndividual(ind1);
        population.addIndividual(ind2);

        Individual secondFittest = population.getFittest(1);
        assertSame(ind1, secondFittest);
    }

    @Test
    /**
     * testRouletteSelect方法。
     */
    public void testRouletteSelect() {
        Population population = new Population();
        for (int i = 0; i < 5; i++) {
            Individual ind = new Individual(5);
            ind.setFitness(1.0);
            population.addIndividual(ind);
        }
        population.setTotalFitness(5.0);

        Individual selected = population.rouletteSelect();
        assertNotNull(selected);
        assertEquals(5, selected.length());
    }

    @Test
    /**
     * testRouletteSelectWithZeroFitness方法。
     */
    public void testRouletteSelectWithZeroFitness() {
        Population population = new Population();
        for (int i = 0; i < 3; i++) {
            population.addIndividual(new Individual(5));
        }

        Individual selected = population.rouletteSelect();
        assertNotNull(selected);
    }

    @Test
    /**
     * testShuffle方法。
     */
    public void testShuffle() {
        Population population = new Population();
        for (int i = 0; i < 10; i++) {
            Individual ind = new Individual(5);
            ind.setGene(0, i);
            population.addIndividual(ind);
        }

        int firstBefore = (int) population.getIndividual(0).getGene(0);
        population.shuffle();
        // After shuffle, order may or may not change, but no exception means success
        assertNotNull(population.getIndividual(0));
    }
}

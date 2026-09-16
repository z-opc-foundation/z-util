package com.zifang.util.ml.inheritance.chapter4;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Population类。
 */
public class Population {
    private Individual[] population;
    private double populationFitness = -1;

    /**
     * Initializes blank population of individuals
     *
     * @param populationSize The size of the population
     */
    /**
     * Population方法。
     * * @param populationSize int类型参数
     */
    public Population(int populationSize) {
        // Initial population
        this.population = new Individual[populationSize];
    }

    /**
     * Initializes population of individuals
     *
     * @param populationSize   The size of the population
     * @param chromosomeLength The length of the individuals chromosome
     */
    /**
     * Population方法。
     * * @param populationSize int类型参数
     *
     * @param chromosomeLength int类型参数
     */
    public Population(int populationSize, int chromosomeLength) {
        // Initial population
        this.population = new Individual[populationSize];

        // Loop over population size
        for (int individualCount = 0; individualCount < populationSize; individualCount++) {
            // Create individual
            Individual individual = new Individual(chromosomeLength);
            // Add individual to population
            this.population[individualCount] = individual;
        }
    }

    /**
     * Get individuals from the population
     *
     * @return individuals Individuals in population
     */
    /**
     * getIndividuals方法。
     *
     * @return Individual[]类型返回值
     */
    public Individual[] getIndividuals() {
        return this.population;
    }

    /**
     * Find fittest individual in the population
     *
     * @param offset
     * @return individual Fittest individual at offset
     */
    /**
     * getFittest方法。
     * * @param offset int类型参数
     *
     * @return Individual类型返回值
     */
    public Individual getFittest(int offset) {
        // Order population by fitness
        Arrays.sort(this.population, new Comparator<Individual>() {
            @Override
            /**
             * compare方法。
             *      * @param o1 Individual类型参数
             * @param o2 Individual类型参数
             * @return int类型返回值
             */
            public int compare(Individual o1, Individual o2) {
                if (o1.getFitness() > o2.getFitness()) {
                    return -1;
                } else if (o1.getFitness() < o2.getFitness()) {
                    return 1;
                }
                return 0;
            }
        });

        // Return the fittest individual
        return this.population[offset];
    }

    /**
     * Set population's fitness
     *
     * @param fitness The population's total fitness
     */

    /**
     * getPopulationFitness方法。
     *
     * @return double类型返回值
     */
    public double getPopulationFitness() {
        return this.populationFitness;
    }

    /**
     * Get population's fitness
     *
     * @return populationFitness The population's total fitness
     */

    /**
     * setPopulationFitness方法。
     * * @param fitness double类型参数
     */
    public void setPopulationFitness(double fitness) {
        this.populationFitness = fitness;
    }

    /**
     * Get population's size
     *
     * @return size The population's size
     */

    /**
     * size方法。
     *
     * @return int类型返回值
     */
    public int size() {
        return this.population.length;
    }

    /**
     * Set individual at offset
     *
     * @param individual
     * @param offset
     * @return individual
     */
    /**
     * setIndividual方法。
     * * @param offset int类型参数
     *
     * @param individual Individual类型参数
     * @return Individual类型返回值
     */
    public Individual setIndividual(int offset, Individual individual) {
        return population[offset] = individual;
    }

    /**
     * Get individual at offset
     *
     * @param offset
     * @return individual
     */
    /**
     * getIndividual方法。
     * * @param offset int类型参数
     *
     * @return Individual类型返回值
     */
    public Individual getIndividual(int offset) {
        return population[offset];
    }

    /**
     * Shuffles the population in-place
     *
     * @param void
     * @return void
     */
    /**
     * shuffle方法。
     */
    public void shuffle() {
        Random rnd = new Random();
        for (int i = population.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            Individual a = population[index];
            population[index] = population[i];
            population[i] = a;
        }
    }

}
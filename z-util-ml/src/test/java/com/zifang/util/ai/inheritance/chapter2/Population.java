package com.zifang.util.ml.inheritance.chapter2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;


/**
 * A population is an abstraction of a collection of individuals. The population
 * class is generally used to perform group-level operations on its individuals,
 * such as finding the strongest individuals, collecting stats on the population
 * as a whole, and selecting individuals to mutate or crossover.
 *
 * @author zifang
 */

/**
 * Population类。
 */
public class Population {
    private Individual[] population;
    private double populationFitness = -1;

    /**
     * Initializes blank population of individuals
     *
     * @param populationSize The number of individuals in the population
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
     * @param populationSize   The number of individuals in the population
     * @param chromosomeLength The size of each individual's chromosome
     */
    /**
     * Population方法。
     * * @param populationSize int类型参数
     *
     * @param chromosomeLength int类型参数
     */
    public Population(int populationSize, int chromosomeLength) {
        // Initialize the population as an array of individuals
        this.population = new Individual[populationSize];

        // Create each individual in turn
        for (int individualCount = 0; individualCount < populationSize; individualCount++) {
            // Create an individual, initializing its chromosome to the given
            // length
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
     * Find an individual in the population by its fitness
     * <p>
     * This method lets you select an individual in order of its fitness. This
     * can be used to find the single strongest individual (eg, if you're
     * testing for a solution), but it can also be used to find weak individuals
     * (if you're looking to cull the population) or some of the strongest
     * individuals (if you're using "elitism").
     *
     * @param offset The offset of the individual you want, sorted by fitness. 0 is
     *               the strongest, population.length - 1 is the weakest.
     * @return individual Individual at offset
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
     * Set population's group fitness
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
     * Get population's group fitness
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
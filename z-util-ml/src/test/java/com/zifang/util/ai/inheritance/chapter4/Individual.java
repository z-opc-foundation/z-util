package com.zifang.util.ml.inheritance.chapter4;

/**
 * Individual类。
 */
public class Individual {

    /**
     * In this case, the chromosome is an array of integers rather than a string.
     */
    private int[] chromosome;
    private double fitness = -1;

    /**
     * Initializes individual with specific chromosome
     *
     * @param chromosome The chromosome to give individual
     */
    /**
     * Individual方法。
     * * @param chromosome int[]类型参数
     */
    public Individual(int[] chromosome) {
        // Create individualchromosome
        this.chromosome = chromosome;
    }

    /**
     * Initializes random individual
     *
     * @param chromosomeLength The length of the individuals chromosome
     */
    /**
     * Individual方法。
     * * @param chromosomeLength int类型参数
     */
    public Individual(int chromosomeLength) {
        // Create random individual
        int[] individual;
        individual = new int[chromosomeLength];

        /**
         * In this case, we can no longer simply pick 0s and 1s -- we need to
         * use every city index available. We also don't need to randomize or
         * shuffle this chromosome, as crossover and mutation will ultimately
         * take care of that for us.
         */
        for (int gene = 0; gene < chromosomeLength; gene++) {
            individual[gene] = gene;
        }

        this.chromosome = individual;
    }

    /**
     * Gets individual's chromosome
     *
     * @return The individual's chromosome
     */
    /**
     * getChromosome方法。
     *
     * @return int[]类型返回值
     */
    public int[] getChromosome() {
        return this.chromosome;
    }

    /**
     * Gets individual's chromosome length
     *
     * @return The individual's chromosome length
     */
    /**
     * getChromosomeLength方法。
     *
     * @return int类型返回值
     */
    public int getChromosomeLength() {
        return this.chromosome.length;
    }

    /**
     * Set gene at offset
     *
     * @param gene
     * @param offset
     */
    /**
     * setGene方法。
     * * @param offset int类型参数
     *
     * @param gene int类型参数
     */
    public void setGene(int offset, int gene) {
        this.chromosome[offset] = gene;
    }

    /**
     * Get gene at offset
     *
     * @param offset
     * @return gene
     */
    /**
     * getGene方法。
     * * @param offset int类型参数
     *
     * @return int类型返回值
     */
    public int getGene(int offset) {
        return this.chromosome[offset];
    }

    /**
     * Store individual's fitness
     *
     * @param fitness The individuals fitness
     */

    /**
     * getFitness方法。
     *
     * @return double类型返回值
     */
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Gets individual's fitness
     *
     * @return The individual's fitness
     */

    /**
     * setFitness方法。
     * * @param fitness double类型参数
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * toString方法。
     *
     * @return String类型返回值
     */
    public String toString() {
        String output = "";
        for (int gene = 0; gene < this.chromosome.length; gene++) {
            output += this.chromosome[gene] + ",";
        }
        return output;
    }

    /**
     * Search for a specific integer gene in this individual.
     * <p>
     * For instance, in a Traveling Salesman Problem where cities are encoded as
     * integers with the range, say, 0-99, this method will check to see if the
     * city "42" exists.
     *
     * @param gene
     * @return
     */
    /**
     * containsGene方法。
     * * @param gene int类型参数
     *
     * @return boolean类型返回值
     */
    public boolean containsGene(int gene) {
        for (int i = 0; i < this.chromosome.length; i++) {
            if (this.chromosome[i] == gene) {
                return true;
            }
        }
        return false;
    }


}

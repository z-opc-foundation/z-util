package com.zifang.util.ml.inheritance.chapter4;

/**
 * The main Evaluation class for the TSP. It's pretty simple -- given an
 * Individual (ie, a chromosome) and a list of canonical cities, calculate the
 * total distance required to travel to the cities in the specified order. The
 * result returned by getDistance() is used by GeneticAlgorithm.calcFitness.
 *
 * @author zifang
 */

/**
 * Route类。
 */
public class Route {
    private City[] route;
    private double distance = 0;

    /**
     * Initialize Route
     *
     * @param individual A GA individual
     * @param cities     The cities referenced
     */
    /**
     * Route方法。
     * * @param individual Individual类型参数
     *
     * @param cities City[]类型参数
     */
    public Route(Individual individual, City[] cities) {
        // Get individual's chromosome
        int[] chromosome = individual.getChromosome();
        // Create route
        this.route = new City[cities.length];
        for (int geneIndex = 0; geneIndex < chromosome.length; geneIndex++) {
            this.route[geneIndex] = cities[chromosome[geneIndex]];
        }
    }

    /**
     * Get route distance
     *
     * @return distance The route's distance
     */
    /**
     * getDistance方法。
     *
     * @return double类型返回值
     */
    public double getDistance() {
        if (this.distance > 0) {
            return this.distance;
        }

        // Loop over cities in route and calculate route distance
        double totalDistance = 0;
        for (int cityIndex = 0; cityIndex + 1 < this.route.length; cityIndex++) {
            totalDistance += this.route[cityIndex].distanceFrom(this.route[cityIndex + 1]);
        }

        totalDistance += this.route[this.route.length - 1].distanceFrom(this.route[0]);
        this.distance = totalDistance;

        return totalDistance;
    }
}
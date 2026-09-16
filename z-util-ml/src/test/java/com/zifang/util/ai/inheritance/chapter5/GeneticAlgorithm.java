package com.zifang.util.ai.inheritance.chapter5;


/**
 * GeneticAlgorithm类。
 */
public class GeneticAlgorithm {

    protected int tournamentSize;
    private int populationSize;
    private double mutationRate;
    private double crossoverRate;
    private int elitismCount;

    /**
     * GeneticAlgorithm方法。
     * * @param populationSize int类型参数
     *
     * @param mutationRate   double类型参数
     * @param crossoverRate  double类型参数
     * @param elitismCount   int类型参数
     * @param tournamentSize int类型参数
     */
    public GeneticAlgorithm(int populationSize, double mutationRate, double crossoverRate, int elitismCount,
                            int tournamentSize) {

        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.elitismCount = elitismCount;
        this.tournamentSize = tournamentSize;
    }

    /**
     * Initialize population
     *
     * @param chromosomeLength The length of the individuals chromosome
     * @return population The initial population generated
     */
    /**
     * initPopulation方法。
     * * @param timetable Timetable类型参数
     *
     * @return Population类型返回值
     */
    public Population initPopulation(Timetable timetable) {
        // Initialize population
        Population population = new Population(this.populationSize, timetable);
        return population;
    }

    /**
     * Check if population has met termination condition
     *
     * @param generationsCount Number of generations passed
     * @param maxGenerations   Number of generations to terminate after
     * @return boolean True if termination condition met, otherwise, false
     */
    /**
     * isTerminationConditionMet方法。
     * * @param generationsCount int类型参数
     *
     * @param maxGenerations int类型参数
     * @return boolean类型返回值
     */
    public boolean isTerminationConditionMet(int generationsCount, int maxGenerations) {
        return (generationsCount > maxGenerations);
    }

    /**
     * Check if population has met termination condition
     *
     * @param population
     * @return boolean True if termination condition met, otherwise, false
     */
    /**
     * isTerminationConditionMet方法。
     * * @param population Population类型参数
     *
     * @return boolean类型返回值
     */
    public boolean isTerminationConditionMet(Population population) {
        return population.getFittest(0).getFitness() == 1.0;
    }

    /**
     * Calculate individual's fitness value
     *
     * @param individual
     * @param timetable
     * @return fitness
     */
    /**
     * calcFitness方法。
     * * @param individual Individual类型参数
     *
     * @param timetable Timetable类型参数
     * @return double类型返回值
     */
    public double calcFitness(Individual individual, Timetable timetable) {

        // Create new timetable object to use -- cloned from an existing timetable
        Timetable threadTimetable = new Timetable(timetable);
        threadTimetable.createClasses(individual);

        // Calculate fitness
        int clashes = threadTimetable.calcClashes();
        double fitness = 1 / (double) (clashes + 1);

        individual.setFitness(fitness);

        return fitness;
    }

    /**
     * Evaluate population
     *
     * @param population
     * @param timetable
     */
    /**
     * evalPopulation方法。
     * * @param population Population类型参数
     *
     * @param timetable Timetable类型参数
     */
    public void evalPopulation(Population population, Timetable timetable) {
        double populationFitness = 0;

        // Loop over population evaluating individuals and summing population
        // fitness
        for (Individual individual : population.getIndividuals()) {
            populationFitness += this.calcFitness(individual, timetable);
        }

        population.setPopulationFitness(populationFitness);
    }

    /**
     * Selects parent for crossover using tournament selection
     * <p>
     * Tournament selection works by choosing N random individuals, and then
     * choosing the best of those.
     *
     * @param population
     * @return The individual selected as a parent
     */
    /**
     * selectParent方法。
     * * @param population Population类型参数
     *
     * @return Individual类型返回值
     */
    public Individual selectParent(Population population) {
        // Create tournament
        Population tournament = new Population(this.tournamentSize);

        // Add random individuals to the tournament
        population.shuffle();
        for (int i = 0; i < this.tournamentSize; i++) {
            Individual tournamentIndividual = population.getIndividual(i);
            tournament.setIndividual(i, tournamentIndividual);
        }

        // Return the best
        return tournament.getFittest(0);
    }


    /**
     * Apply mutation to population
     *
     * @param population
     * @param timetable
     * @return The mutated population
     */
    /**
     * mutatePopulation方法。
     * * @param population Population类型参数
     *
     * @param timetable Timetable类型参数
     * @return Population类型返回值
     */
    public Population mutatePopulation(Population population, Timetable timetable) {
        // Initialize new population
        Population newPopulation = new Population(this.populationSize);

        // Loop over current population by fitness
        for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
            Individual individual = population.getFittest(populationIndex);

            // Create random individual to swap genes with
            Individual randomIndividual = new Individual(timetable);

            // Loop over individual's genes
            for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                // Skip mutation if this is an elite individual
                if (populationIndex > this.elitismCount) {
                    // Does this gene need mutation?
                    if (this.mutationRate > Math.random()) {
                        // Swap for new gene
                        individual.setGene(geneIndex, randomIndividual.getGene(geneIndex));
                    }
                }
            }

            // Add individual to population
            newPopulation.setIndividual(populationIndex, individual);
        }

        // Return mutated population
        return newPopulation;
    }

    /**
     * Apply crossover to population
     *
     * @param population The population to apply crossover to
     * @return The new population
     */
    /**
     * crossoverPopulation方法。
     * * @param population Population类型参数
     *
     * @return Population类型返回值
     */
    public Population crossoverPopulation(Population population) {
        // Create new population
        Population newPopulation = new Population(population.size());

        // Loop over current population by fitness
        for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
            Individual parent1 = population.getFittest(populationIndex);

            // Apply crossover to this individual?
            if (this.crossoverRate > Math.random() && populationIndex >= this.elitismCount) {
                // Initialize offspring
                Individual offspring = new Individual(parent1.getChromosomeLength());

                // Find second parent
                Individual parent2 = selectParent(population);

                // Loop over genome
                for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
                    // Use half of parent1's genes and half of parent2's genes
                    if (0.5 > Math.random()) {
                        offspring.setGene(geneIndex, parent1.getGene(geneIndex));
                    } else {
                        offspring.setGene(geneIndex, parent2.getGene(geneIndex));
                    }
                }

                // Add offspring to new population
                newPopulation.setIndividual(populationIndex, offspring);
            } else {
                // Add individual to new population without applying crossover
                newPopulation.setIndividual(populationIndex, parent1);
            }
        }

        return newPopulation;
    }


}

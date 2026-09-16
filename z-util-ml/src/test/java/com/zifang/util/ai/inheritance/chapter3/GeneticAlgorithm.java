package com.zifang.util.ml.inheritance.chapter3;

/**
 * Please see chapter2/GeneticAlgorithm for additional comments.
 * <p>
 * This GeneticAlgorithm class is designed to solve the
 * "Robot Controller in a Maze" problem, and is necessarily a little different
 * from the chapter2/GeneticAlgorithm class.
 * <p>
 * This class introduces the concepts of tournament selection and single-point
 * crossover. Additionally, the calcFitness method is vastly different from the
 * AllOnesGA fitness method; in this case we actually have to evaluate how good
 * the robot is at navigating a maze!
 *
 * @author zifang
 */

/**
 * GeneticAlgorithm类。
 */
public class GeneticAlgorithm {

    /**
     * A new property we've introduced is the size of the population used for
     * tournament selection in crossover.
     */
    protected int tournamentSize;
    /**
     * See chapter2/GeneticAlgorithm for a description of these properties.
     */
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
     * * @param chromosomeLength int类型参数
     *
     * @return Population类型返回值
     */
    public Population initPopulation(int chromosomeLength) {
        // Initialize population
        Population population = new Population(this.populationSize, chromosomeLength);
        return population;
    }

    /**
     * Calculate fitness for an individual.
     * <p>
     * This fitness calculation is a little more involved than chapter2's. In
     * this case we initialize a new Robot class, and evaluate its performance
     * in the given maze.
     *
     * @param individual the individual to evaluate
     * @return double The fitness value for individual
     */
    /**
     * calcFitness方法。
     * * @param individual Individual类型参数
     *
     * @param maze Maze类型参数
     * @return double类型返回值
     */
    public double calcFitness(Individual individual, Maze maze) {
        // Get individual's chromosome
        int[] chromosome = individual.getChromosome();

        // Get fitness
        Robot robot = new Robot(chromosome, maze, 100);
        robot.run();
        int fitness = maze.scoreRoute(robot.getRoute());

        // Store fitness
        individual.setFitness(fitness);

        return fitness;
    }

    /**
     * Evaluate the whole population
     * <p>
     * Essentially, loop over the individuals in the population, calculate the
     * fitness for each, and then calculate the entire population's fitness. The
     * population's fitness may or may not be important, but what is important
     * here is making sure that each individual gets evaluated.
     * <p>
     * The difference between this method and the one in chapter2 is that this
     * method requires the maze itself as a parameter; unlike the All Ones
     * problem in chapter2, we can't determine a fitness just by looking at the
     * chromosome -- we need to evaluate each member against the maze.
     *
     * @param population the population to evaluate
     * @param maze       the maze to evaluate each individual against.
     */
    /**
     * evalPopulation方法。
     * * @param population Population类型参数
     *
     * @param maze Maze类型参数
     */
    public void evalPopulation(Population population, Maze maze) {
        double populationFitness = 0;

        // Loop over population evaluating individuals and suming population
        // fitness
        for (Individual individual : population.getIndividuals()) {
            populationFitness += this.calcFitness(individual, maze);
        }

        population.setPopulationFitness(populationFitness);
    }

    /**
     * Check if population has met termination condition
     * <p>
     * We don't actually know what a perfect solution looks like for the robot
     * controller problem, so the only constraint we can give to the genetic
     * algorithm is an upper bound on the number of generations.
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
     * <p>
     * This method is the same as chapter2's version.
     *
     * @param population The population to apply mutation to
     * @return The mutated population
     */
    /**
     * mutatePopulation方法。
     * * @param population Population类型参数
     *
     * @return Population类型返回值
     */
    public Population mutatePopulation(Population population) {
        // Initialize new population
        Population newPopulation = new Population(this.populationSize);

        // Loop over current population by fitness
        for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
            Individual individual = population.getFittest(populationIndex);

            // Loop over individual's genes
            for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                // Skip mutation if this is an elite individual
                if (populationIndex >= this.elitismCount) {
                    // Does this gene need mutation?
                    if (this.mutationRate > Math.random()) {
                        // Get new gene
                        int newGene = 1;
                        if (individual.getGene(geneIndex) == 1) {
                            newGene = 0;
                        }
                        // Mutate gene
                        individual.setGene(geneIndex, newGene);
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
     * Crossover population using single point crossover
     * <p>
     * Single-point crossover differs from the crossover used in chapter2.
     * Chapter2's version simply selects genes at random from each parent, but
     * in this case we want to select a contiguous region of the chromosome from
     * each parent.
     * <p>
     * For instance, chapter2's version would look like this:
     * <p>
     * Parent1: AAAAAAAAAA
     * Parent2: BBBBBBBBBB
     * Child  : AABBAABABA
     * <p>
     * This version, however, might look like this:
     * <p>
     * Parent1: AAAAAAAAAA
     * Parent2: BBBBBBBBBB
     * Child  : AAAABBBBBB
     *
     * @param population Population to crossover
     * @return Population The new population
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
                Individual parent2 = this.selectParent(population);

                // Get random swap point
                int swapPoint = (int) (Math.random() * (parent1.getChromosomeLength() + 1));

                // Loop over genome
                for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
                    // Use half of parent1's genes and half of parent2's genes
                    if (geneIndex < swapPoint) {
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

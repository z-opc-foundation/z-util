package com.zifang.util.ml.ga;

import java.util.Random;

/**
 * 遗传算法引擎
 */
public class GeneticAlgorithmEngine {

    private final int populationSize;
    private final double mutationRate;
    private final double crossoverRate;
    private final int elitismCount;

    private final IndividualFactory individualFactory;
    private final FitnessFunction fitnessFunction;
    private final CrossoverOperator crossoverOperator;
    private final MutationOperator mutationOperator;
    private final SelectionOperator selectionOperator;
    private final TerminationCondition terminationCondition;

    private final Random random;

    private GeneticAlgorithmEngine(Builder builder) {
        this.populationSize = builder.populationSize;
        this.mutationRate = builder.mutationRate;
        this.crossoverRate = builder.crossoverRate;
        this.elitismCount = builder.elitismCount;
        this.individualFactory = builder.individualFactory;
        this.fitnessFunction = builder.fitnessFunction;
        this.crossoverOperator = builder.crossoverOperator;
        this.mutationOperator = builder.mutationOperator;
        this.selectionOperator = builder.selectionOperator;
        this.terminationCondition = builder.terminationCondition;
        this.random = new Random();
    }

    /**
     * 获取遗传算法引擎的构建器
     *
     * @return Builder实例，用于配置和构建GeneticAlgorithmEngine
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 执行遗传算法进化过程
     *
     * @param maxGenerations 最大迭代代数
     * @return 最终的种群对象
     */
    @SuppressWarnings("unchecked")
    /**
     * evolve方法。
     *      * @param maxGenerations int类型参数
     * @return Population类型返回值
     */
    public Population evolve(int maxGenerations) {
        Population population = initializePopulation();

        for (int generation = 0; generation < maxGenerations; generation++) {
            evaluatePopulation(population);

            if (terminationCondition.isTerminated(generation, population)) {
                break;
            }

            population = nextGeneration(population);
        }

        evaluatePopulation(population);
        return population;
    }

    private Population initializePopulation() {
        Population population = new Population(populationSize);
        for (int i = 0; i < populationSize; i++) {
            Individual individual = individualFactory.create();
            individual.randomize();
            population.addIndividual(individual);
        }
        return population;
    }

    private void evaluatePopulation(Population population) {
        for (Individual individual : population.getIndividuals()) {
            double fitness = fitnessFunction.evaluate(individual);
            individual.setFitness(fitness);
        }
        population.calculateTotalFitness();
    }

    private Population nextGeneration(Population population) {
        Population newPopulation = new Population(populationSize);

        // 保留精英
        for (int i = 0; i < elitismCount && i < population.size(); i++) {
            newPopulation.addIndividual(population.getFittest(i).copy());
        }

        // 产生新个体
        while (newPopulation.size() < populationSize) {
            Individual parent1 = selectionOperator.select(population);
            Individual parent2 = selectionOperator.select(population);

            Individual offspring;
            if (random.nextDouble() < crossoverRate) {
                Individual[] children = crossoverOperator.crossover(parent1, parent2);
                offspring = children[0];
            } else {
                offspring = parent1.copy();
            }

            if (random.nextDouble() < mutationRate) {
                mutationOperator.mutate(offspring, mutationRate);
            }

            newPopulation.addIndividual(offspring);
        }

        return newPopulation;
    }

    /**
     * 用于创建新个体的工厂接口
     */
    @FunctionalInterface
/**
 * IndividualFactory接口。
 */
    public interface IndividualFactory {
        Individual create();
    }

    /**
     * 用于构建GeneticAlgorithmEngine的配置类
     */
    public static class Builder {
        private int populationSize = 100;
        private double mutationRate = 0.01;
        private double crossoverRate = 0.9;
        private int elitismCount = 2;
        private IndividualFactory individualFactory;
        private FitnessFunction fitnessFunction;
        private CrossoverOperator crossoverOperator;
        private MutationOperator mutationOperator;
        private SelectionOperator selectionOperator;
        private TerminationCondition terminationCondition;

        /**
         * 设置种群大小
         *
         * @param populationSize 种群大小，必须大于0
         * @return this Builder实例
         */
        public Builder populationSize(int populationSize) {
            this.populationSize = populationSize;
            return this;
        }

        /**
         * 设置变异率
         *
         * @param mutationRate 变异率，范围[0, 1]
         * @return this Builder实例
         */
        public Builder mutationRate(double mutationRate) {
            this.mutationRate = mutationRate;
            return this;
        }

        /**
         * 设置交叉率
         *
         * @param crossoverRate 交叉率，范围[0, 1]
         * @return this Builder实例
         */
        public Builder crossoverRate(double crossoverRate) {
            this.crossoverRate = crossoverRate;
            return this;
        }

        /**
         * 设置精英个数
         *
         * @param elitismCount 精英个数，用于保留最优个体到下一代
         * @return this Builder实例
         */
        public Builder elitismCount(int elitismCount) {
            this.elitismCount = elitismCount;
            return this;
        }

        /**
         * 设置个体工厂
         *
         * @param individualFactory 用于创建新个体的工厂
         * @return this Builder实例
         */
        public Builder individualFactory(IndividualFactory individualFactory) {
            this.individualFactory = individualFactory;
            return this;
        }

        /**
         * 设置适应度函数
         *
         * @param fitnessFunction 用于评估个体适应度的函数
         * @return this Builder实例
         */
        public Builder fitnessFunction(FitnessFunction fitnessFunction) {
            this.fitnessFunction = fitnessFunction;
            return this;
        }

        /**
         * 设置交叉算子
         *
         * @param crossoverOperator 用于产生子代的交叉操作
         * @return this Builder实例
         */
        public Builder crossoverOperator(CrossoverOperator crossoverOperator) {
            this.crossoverOperator = crossoverOperator;
            return this;
        }

        /**
         * 设置变异算子
         *
         * @param mutationOperator 用于对个体进行变异的操作
         * @return this Builder实例
         */
        public Builder mutationOperator(MutationOperator mutationOperator) {
            this.mutationOperator = mutationOperator;
            return this;
        }

        /**
         * 设置选择算子
         *
         * @param selectionOperator 用于从种群中选择个体的操作
         * @return this Builder实例
         */
        public Builder selectionOperator(SelectionOperator selectionOperator) {
            this.selectionOperator = selectionOperator;
            return this;
        }

        /**
         * 设置终止条件
         *
         * @param terminationCondition 判断算法何时停止的条件
         * @return this Builder实例
         */
        public Builder terminationCondition(TerminationCondition terminationCondition) {
            this.terminationCondition = terminationCondition;
            return this;
        }

        /**
         * 构建GeneticAlgorithmEngine实例
         *
         * @return 构建好的遗传算法引擎实例
         * @throws IllegalStateException 如果缺少必需的组件
         */
        public GeneticAlgorithmEngine build() {
            if (individualFactory == null) {
                throw new IllegalStateException("individualFactory is required");
            }
            if (fitnessFunction == null) {
                throw new IllegalStateException("fitnessFunction is required");
            }
            if (crossoverOperator == null) {
                throw new IllegalStateException("crossoverOperator is required");
            }
            if (mutationOperator == null) {
                throw new IllegalStateException("mutationOperator is required");
            }
            if (selectionOperator == null) {
                selectionOperator = pop -> pop.rouletteSelect();
            }
            if (terminationCondition == null) {
                terminationCondition = (gen, pop) -> pop.getFittest().getFitness() >= 0.999;
            }
            return new GeneticAlgorithmEngine(this);
        }
    }
}
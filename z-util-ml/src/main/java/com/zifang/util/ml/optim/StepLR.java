package com.zifang.util.ml.optim;

/**
 * Step learning rate scheduler.
 * <p>
 * Decays the learning rate by a factor of gamma every step_size epochs.
 * <p>
 * Formula:
 * lr = base_lr * gamma^(epoch / step_size)
 */
public class StepLR implements LrScheduler {

    private final Optimizer optimizer;
    private final double baseLearningRate;
    private final int stepSize;
    private final double gamma;
    private int epoch;

    /**
     * StepLR方法。
     * * @param optimizer Optimizer类型参数
     *
     * @param stepSize int类型参数
     * @param gamma    double类型参数
     */
    public StepLR(Optimizer optimizer, int stepSize, double gamma) {
        this(optimizer, stepSize, gamma, 0);
    }

    /**
     * StepLR方法。
     * * @param optimizer Optimizer类型参数
     *
     * @param stepSize  int类型参数
     * @param gamma     double类型参数
     * @param lastEpoch int类型参数
     */
    public StepLR(Optimizer optimizer, int stepSize, double gamma, int lastEpoch) {
        this.optimizer = optimizer;
        this.baseLearningRate = optimizer.getLearningRate();
        this.stepSize = stepSize;
        this.gamma = gamma;
        this.epoch = lastEpoch;
    }

    @Override
    /**
     * step方法。
     */
    public void step() {
        epoch++;
        double newLr = baseLearningRate * Math.pow(gamma, epoch / stepSize);
        optimizer.setLearningRate(newLr);
    }

    @Override
    /**
     * getLastLR方法。
     * @return double类型返回值
     */
    public double getLastLR() {
        return optimizer.getLearningRate();
    }

    /**
     * Get the current epoch.
     */
    public int getEpoch() {
        return epoch;
    }

    /**
     * Get the step size.
     */
    public int getStepSize() {
        return stepSize;
    }

    /**
     * Get the gamma factor.
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * Get the base learning rate.
     */
    public double getBaseLearningRate() {
        return baseLearningRate;
    }
}

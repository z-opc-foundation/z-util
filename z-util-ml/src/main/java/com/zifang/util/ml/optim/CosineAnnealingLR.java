package com.zifang.util.ml.optim;

/**
 * Cosine annealing learning rate scheduler with warm restarts.
 * <p>
 * The learning rate decreases following a cosine function from the base
 * learning rate to 0, with optional warm restarts.
 * <p>
 * Formula (without warm restarts):
 * eta_min = 0
 * lr = eta_min + (base_lr - eta_min) * (1 + cos(epoch * pi / T_max)) / 2
 * <p>
 * Formula (with warm restarts):
 * lr = eta_min + (base_lr - eta_min) * (1 + cos((epoch - last_restart) * pi / T_i)) / 2
 * <p>
 * where T_i is the period for the current restart.
 */
public class CosineAnnealingLR implements LrScheduler {

    private final Optimizer optimizer;
    private final double baseLearningRate;
    private final double etaMin;
    private final int tMax;
    private final int tMult;
    private final int warmup_epochs;
    private final double warmup_start_lr;

    private int epoch;
    private int lastRestart;
    private int currentPeriod;

    /**
     * CosineAnnealingLR方法。
     * * @param optimizer Optimizer类型参数
     *
     * @param tMax int类型参数
     */
    public CosineAnnealingLR(Optimizer optimizer, int tMax) {
        this(optimizer, tMax, 0.0, 1, 0, 0.0);
    }

    /**
     * CosineAnnealingLR方法。
     * * @param optimizer Optimizer类型参数
     *
     * @param tMax   int类型参数
     * @param etaMin double类型参数
     */
    public CosineAnnealingLR(Optimizer optimizer, int tMax, double etaMin) {
        this(optimizer, tMax, etaMin, 1, 0, 0.0);
    }

    /**
     * CosineAnnealingLR方法。
     * * @param optimizer Optimizer类型参数
     *
     * @param tMax            int类型参数
     * @param etaMin          double类型参数
     * @param tMult           int类型参数
     * @param warmup_epochs   int类型参数
     * @param warmup_start_lr double类型参数
     */
    public CosineAnnealingLR(Optimizer optimizer, int tMax, double etaMin, int tMult, int warmup_epochs, double warmup_start_lr) {
        this.optimizer = optimizer;
        this.baseLearningRate = optimizer.getLearningRate();
        this.etaMin = etaMin;
        this.tMax = tMax;
        this.tMult = tMult;
        this.warmup_epochs = warmup_epochs;
        this.warmup_start_lr = warmup_start_lr;
        this.epoch = 0;
        this.lastRestart = 0;
        this.currentPeriod = tMax;
    }

    @Override
    /**
     * step方法。
     */
    public void step() {
        epoch++;

        double newLr;

        if (warmup_epochs > 0 && epoch <= warmup_epochs) {
            // Warmup period: linearly interpolate from warmup_start_lr to baseLearningRate
            double progress = (double) epoch / warmup_epochs;
            newLr = warmup_start_lr + (baseLearningRate - warmup_start_lr) * progress;
        } else {
            // Cosine annealing period
            int adjustedEpoch = epoch;
            if (warmup_epochs > 0) {
                adjustedEpoch = epoch - warmup_epochs;
            }

            int periodStart = lastRestart;
            int periodLength = currentPeriod;

            if (adjustedEpoch >= periodStart + periodLength) {
                // New restart
                lastRestart = adjustedEpoch;
                currentPeriod = periodLength * tMult;
            }

            int epochInPeriod = adjustedEpoch - lastRestart;
            double cosValue = Math.cos(epochInPeriod * Math.PI / currentPeriod);
            newLr = etaMin + (baseLearningRate - etaMin) * (1.0 + cosValue) / 2.0;
        }

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
     * Get the maximum number of epochs (period).
     */
    public int gettMax() {
        return tMax;
    }

    /**
     * Get the minimum learning rate.
     */
    public double getEtaMin() {
        return etaMin;
    }

    /**
     * Get the period multiplier for warm restarts.
     */
    public int gettMult() {
        return tMult;
    }

    /**
     * Get the base learning rate.
     */
    public double getBaseLearningRate() {
        return baseLearningRate;
    }
}

package com.zifang.util.ml.optim;

/**
 * Reduce learning rate on plateau scheduler.
 * <p>
 * Monitors a metric and reduces the learning rate when the metric
 * stops improving (plateau detection).
 * <p>
 * The learning rate is reduced by a factor of gamma when the metric
 * has stopped improving for a certain number of epochs (patience).
 * <p>
 * Options:
 * - mode: 'min' (metric should decrease) or 'max' (metric should increase)
 * - factor: factor to reduce learning rate (new_lr = lr * factor)
 * - patience: number of epochs with no improvement to wait before reducing
 * - threshold: threshold to consider metric as improved
 * - min_delta: minimum change to qualify as improvement
 */
public class ReduceLROnPlateau implements LrScheduler {

    private final Optimizer optimizer;
    private final double baseLearningRate;
    private final Mode mode;
    private final double factor;
    private final int patience;
    private final double threshold;
    private final double minDelta;
    private final boolean verbose;
    private int epoch;
    private double bestMetric;
    private int numBadEpochs;
    private boolean inCooldown;
    private int cooldownCounter;
    private int patienceCounter;

    /**
     * ReduceLROnPlateau方法。
     * * @param optimizer Optimizer类型参数
     */
    public ReduceLROnPlateau(Optimizer optimizer) {
        this(optimizer, Mode.MIN, 0.1, 10, 1e-4, 1e-8, true);
    }

    /**
     * ReduceLROnPlateau方法。
     * * @param optimizer Optimizer类型参数
     *
     * @param mode      Mode类型参数
     * @param factor    double类型参数
     * @param patience  int类型参数
     * @param threshold double类型参数
     * @param minDelta  double类型参数
     * @param verbose   boolean类型参数
     */
    public ReduceLROnPlateau(Optimizer optimizer, Mode mode, double factor, int patience,
                             double threshold, double minDelta, boolean verbose) {
        this.optimizer = optimizer;
        this.baseLearningRate = optimizer.getLearningRate();
        this.mode = mode;
        this.factor = factor;
        this.patience = patience;
        this.threshold = threshold;
        this.minDelta = minDelta;
        this.verbose = verbose;
        this.epoch = 0;
        this.bestMetric = Double.MAX_VALUE;
        this.numBadEpochs = 0;
        this.inCooldown = false;
        this.cooldownCounter = 0;
        this.patienceCounter = 0;
    }

    /**
     * ReduceLROnPlateau方法。
     * * @param optimizer Optimizer类型参数
     *
     * @param mode      String类型参数
     * @param factor    double类型参数
     * @param patience  int类型参数
     * @param threshold double类型参数
     * @param minDelta  double类型参数
     * @param verbose   boolean类型参数
     */
    public ReduceLROnPlateau(Optimizer optimizer, String mode, double factor, int patience,
                             double threshold, double minDelta, boolean verbose) {
        this(optimizer,
                "max".equalsIgnoreCase(mode) ? Mode.MAX : Mode.MIN,
                factor, patience, threshold, minDelta, verbose);
    }

    @Override
    /**
     * step方法。
     *      * @param metric double类型参数
     */
    public void step(double metric) {
        epoch++;

        boolean isBetter;
        if (mode == Mode.MIN) {
            isBetter = metric < bestMetric - minDelta;
        } else {
            isBetter = metric > bestMetric + minDelta;
        }

        if (isBetter) {
            if (mode == Mode.MIN) {
                bestMetric = metric;
            } else {
                bestMetric = metric;
            }
            numBadEpochs = 0;
            if (verbose) {
                System.out.println("Epoch " + epoch + ": metric improved to " + metric);
            }
        } else {
            numBadEpochs++;
            if (verbose) {
                System.out.println("Epoch " + epoch + ": metric did not improve (bad epochs: " + numBadEpochs + ")");
            }
        }

        if (inCooldown) {
            if (numBadEpochs >= cooldownCounter) {
                inCooldown = false;
                cooldownCounter = 0;
            }
        }

        if (numBadEpochs >= patience && !inCooldown) {
            double oldLr = optimizer.getLearningRate();
            double newLr = oldLr * factor;

            if (newLr < threshold) {
                newLr = threshold;
            }

            if (newLr != oldLr) {
                optimizer.setLearningRate(newLr);
                if (verbose) {
                    System.out.println("Epoch " + epoch + ": reducing learning rate from " + oldLr + " to " + newLr);
                }
            }

            inCooldown = true;
            cooldownCounter = patience;
            numBadEpochs = 0;
        }
    }

    @Override
    /**
     * step方法。
     */
    public void step() {
        // Default implementation does nothing
        // Must call step(double metric) with actual metric
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
     * Get the best metric value seen so far.
     */
    public double getBestMetric() {
        return bestMetric;
    }

    /**
     * Get the number of bad epochs.
     */
    public int getNumBadEpochs() {
        return numBadEpochs;
    }

    /**
     * Check if currently in cooldown period.
     */
    public boolean isInCooldown() {
        return inCooldown;
    }

    /**
     * Get the factor for reducing learning rate.
     */
    public double getFactor() {
        return factor;
    }

    /**
     * Get the patience.
     */
    public int getPatience() {
        return patience;
    }

    /**
     * Get the mode (MIN or MAX).
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Reset the scheduler state.
     */
    public void reset() {
        epoch = 0;
        bestMetric = (mode == Mode.MIN) ? Double.MAX_VALUE : Double.NEGATIVE_INFINITY;
        numBadEpochs = 0;
        inCooldown = false;
        cooldownCounter = 0;
    }

    /**
     * Mode枚举。
     */
    public enum Mode {
        MIN,
        MAX
    }
}

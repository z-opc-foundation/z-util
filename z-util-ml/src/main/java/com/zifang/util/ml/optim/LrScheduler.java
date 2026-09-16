package com.zifang.util.ml.optim;

/**
 * Learning rate scheduler interface.
 * Schedulers adjust the learning rate during training.
 */
public interface LrScheduler {

    /**
     * Get the current learning rate.
     *
     * @return Current learning rate
     */
    double getLastLR();

    /**
     * Step the scheduler to update the learning rate.
     * This should be called after each epoch or batch.
     */
    void step();

    /**
     * Step the scheduler with a metric value.
     * Used for schedulers like ReduceLROnPlateau.
     *
     * @param metric The metric value to use for scheduling
     */
    default void step(double metric) {
        step();
    }

    /**
     * Get the current learning rate from the attached optimizer.
     *
     * @return Current learning rate
     */
    default double getLearningRate() {
        return getLastLR();
    }
}

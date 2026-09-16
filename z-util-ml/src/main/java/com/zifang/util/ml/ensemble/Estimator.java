package com.zifang.util.ml.ensemble;

import com.zifang.util.numpy.NdArray;

/**
 * Interface for estimators used in ensemble methods.
 * Defines the common interface for base estimators in Stacking and Voting.
 */
public interface Estimator {
    /**
     * Fit the estimator on training data.
     *
     * @param X Training features
     * @param y Training labels (can be int[] for classification or double[] for regression)
     */
    void fit(NdArray X, Object y);

    /**
     * Predict using the fitted estimator.
     *
     * @param X Features to predict
     * @return Predictions (int[] for classification, double[] for regression)
     */
    Object predict(NdArray X);

    /**
     * Predict class probabilities.
     *
     * @param X Features to predict
     * @return Probability array of shape (n_samples, n_classes)
     */
    NdArray predictProba(NdArray X);
}

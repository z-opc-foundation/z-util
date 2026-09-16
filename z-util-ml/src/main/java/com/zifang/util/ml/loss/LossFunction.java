package com.zifang.util.ml.loss;

import com.zifang.util.numpy.NdArray;

/**
 * Loss function interface for machine learning.
 * <p>
 * Defines the basic operations required for loss functions
 * to evaluate predictions against targets.
 */
public interface LossFunction {

    /**
     * Compute the loss value.
     *
     * @param predictions Predicted values (output from model)
     * @param targets     Ground truth values
     * @return Loss value (lower is better)
     */
    NdArray compute(NdArray predictions, NdArray targets);

    /**
     * Compute the gradient of the loss with respect to predictions.
     *
     * @param predictions Predicted values
     * @param targets     Ground truth values
     * @return Gradient with same shape as predictions
     */
    NdArray gradient(NdArray predictions, NdArray targets);
}

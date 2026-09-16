package com.zifang.util.ml.loss;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Binary Cross Entropy loss function.
 * <p>
 * BCE = -(1/n) * Σ(target[i] * log(pred[i]) + (1 - target[i]) * log(1 - pred[i]))
 * <p>
 * Used for binary classification problems.
 */
public class BinaryCrossEntropyLoss implements LossFunction {

    private static final double EPSILON = 1e-7;

    /**
     * Compute binary cross entropy loss.
     *
     * @param predictions Predicted probabilities (values between 0 and 1)
     * @param targets     Target values (0 or 1)
     * @return Scalar loss value
     */
    @Override
    /**
     * compute方法。
     *      * @param predictions NdArray类型参数
     * @param targets NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray compute(NdArray predictions, NdArray targets) {
        validateInputs(predictions, targets);

        double[] predData = toDoubleArray(predictions);
        double[] targetData = toDoubleArray(targets);

        double sum = 0;
        for (int i = 0; i < predictions.size(); i++) {
            double pred = Math.max(EPSILON, Math.min(1 - EPSILON, predData[i]));
            sum += targetData[i] * Math.log(pred) + (1 - targetData[i]) * Math.log(1 - pred);
        }

        double loss = -sum / predictions.size();
        return NdArray.array(new double[]{loss}, DType.FLOAT64);
    }

    /**
     * Compute gradient of binary cross entropy loss with respect to predictions.
     * <p>
     * gradient[i] = (pred[i] - target[i]) / (pred[i] * (1 - pred[i]))
     *
     * @param predictions Predicted probabilities
     * @param targets     Target values
     * @return Gradient array with same shape as predictions
     */
    @Override
    /**
     * gradient方法。
     *      * @param predictions NdArray类型参数
     * @param targets NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray gradient(NdArray predictions, NdArray targets) {
        validateInputs(predictions, targets);

        int size = predictions.size();
        double[] result = new double[size];
        double scale = 1.0 / size;

        double[] predData = toDoubleArray(predictions);
        double[] targetData = toDoubleArray(targets);

        for (int i = 0; i < size; i++) {
            double pred = Math.max(EPSILON, Math.min(1 - EPSILON, predData[i]));
            result[i] = (pred - targetData[i]) / (pred * (1 - pred)) * scale;
        }

        return NdArray.array(result, DType.FLOAT64);
    }

    private void validateInputs(NdArray predictions, NdArray targets) {
        if (!predictions.getShape().equals(targets.getShape())) {
            throw new IllegalArgumentException(
                    "Predictions and targets must have the same shape: " +
                            predictions.getShape() + " vs " + targets.getShape());
        }
    }

    private double[] toDoubleArray(NdArray arr) {
        Object data = arr.getData();
        int size = arr.size();

        if (data instanceof double[]) {
            return (double[]) data;
        } else if (data instanceof float[]) {
            float[] fData = (float[]) data;
            double[] result = new double[size];
            for (int i = 0; i < size; i++) {
                result[i] = fData[i];
            }
            return result;
        } else if (data instanceof int[]) {
            int[] iData = (int[]) data;
            double[] result = new double[size];
            for (int i = 0; i < size; i++) {
                result[i] = iData[i];
            }
            return result;
        } else {
            double[] result = new double[size];
            for (int i = 0; i < size; i++) {
                result[i] = ((Number) arr.get(i)).doubleValue();
            }
            return result;
        }
    }
}

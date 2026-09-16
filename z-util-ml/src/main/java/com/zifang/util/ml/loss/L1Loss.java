package com.zifang.util.ml.loss;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * L1 Loss (Mean Absolute Error) loss function.
 * <p>
 * L1 = (1/n) * Σ|predictions[i] - targets[i]|
 */
public class L1Loss implements LossFunction {

    /**
     * Compute mean absolute error.
     *
     * @param predictions Predicted values
     * @param targets     Target values
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

        double sum = 0;
        double[] predData = toDoubleArray(predictions);
        double[] targetData = toDoubleArray(targets);

        for (int i = 0; i < predictions.size(); i++) {
            sum += Math.abs(predData[i] - targetData[i]);
        }

        double mean = sum / predictions.size();
        return NdArray.array(new double[]{mean}, DType.FLOAT64);
    }

    /**
     * Compute gradient of L1 loss with respect to predictions.
     * <p>
     * gradient[i] = sign(predictions[i] - targets[i]) / n
     *
     * @param predictions Predicted values
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
            double diff = predData[i] - targetData[i];
            result[i] = (diff > 0 ? 1 : (diff < 0 ? -1 : 0)) * scale;
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

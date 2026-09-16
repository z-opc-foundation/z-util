package com.zifang.util.ml.loss;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Mean Squared Error loss function.
 * <p>
 * MSE = (1/n) * Σ(predictions[i] - targets[i])²
 * <p>
 * Ported from nnet to use NdArray.
 */
public class MSELoss implements LossFunction {

    /**
     * Compute mean squared error.
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

        NdArray diff = elementwiseSubtract(predictions, targets);
        NdArray squared = elementwiseMultiply(diff, diff);

        double sum = sumAll(squared);
        double mean = sum / predictions.size();

        return NdArray.array(new double[]{mean}, DType.FLOAT64);
    }

    /**
     * Compute gradient of MSE with respect to predictions.
     * <p>
     * gradient[i] = 2 * (predictions[i] - targets[i]) / n
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

        NdArray diff = elementwiseSubtract(predictions, targets);
        double scale = 2.0 / predictions.size();

        return elementwiseScale(diff, scale);
    }

    private void validateInputs(NdArray predictions, NdArray targets) {
        if (!predictions.getShape().equals(targets.getShape())) {
            throw new IllegalArgumentException(
                    "Predictions and targets must have the same shape: " +
                            predictions.getShape() + " vs " + targets.getShape());
        }
    }

    private NdArray elementwiseSubtract(NdArray a, NdArray b) {
        int size = a.size();
        double[] result = new double[size];

        double[] aData = toDoubleArray(a);
        double[] bData = toDoubleArray(b);

        for (int i = 0; i < size; i++) {
            result[i] = aData[i] - bData[i];
        }

        return NdArray.array(result, DType.FLOAT64).reshape(a.getShape().getDimensions());
    }

    private NdArray elementwiseMultiply(NdArray a, NdArray b) {
        int size = a.size();
        double[] result = new double[size];

        double[] aData = toDoubleArray(a);
        double[] bData = toDoubleArray(b);

        for (int i = 0; i < size; i++) {
            result[i] = aData[i] * bData[i];
        }

        return NdArray.array(result, DType.FLOAT64).reshape(a.getShape().getDimensions());
    }

    private NdArray elementwiseScale(NdArray a, double scale) {
        int size = a.size();
        double[] result = new double[size];

        double[] aData = toDoubleArray(a);

        for (int i = 0; i < size; i++) {
            result[i] = aData[i] * scale;
        }

        return NdArray.array(result, DType.FLOAT64).reshape(a.getShape().getDimensions());
    }

    private double sumAll(NdArray a) {
        double sum = 0;
        double[] data = toDoubleArray(a);
        for (int i = 0; i < a.size(); i++) {
            sum += data[i];
        }
        return sum;
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

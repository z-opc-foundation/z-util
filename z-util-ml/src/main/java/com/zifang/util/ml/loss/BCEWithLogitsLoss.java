package com.zifang.util.ml.loss;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Binary Cross Entropy with Logits loss.
 * <p>
 * This combines sigmoid activation with BCE loss for numerical stability.
 * BCEWithLogits = -(1/n) * Σ(target[i] * log(sigmoid(pred)[i]) + (1 - target[i]) * log(1 - sigmoid(pred)[i]))
 * <p>
 * Computations are done on logits (before sigmoid) for better numerical stability.
 */
public class BCEWithLogitsLoss implements LossFunction {

    private static final double EPSILON = 1e-7;

    /**
     * Compute BCE with logits loss.
     *
     * @param logits  Raw logits (before sigmoid)
     * @param targets Target values (0 or 1)
     * @return Scalar loss value
     */
    @Override
    /**
     * compute方法。
     *      * @param logits NdArray类型参数
     * @param targets NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray compute(NdArray logits, NdArray targets) {
        validateInputs(logits, targets);

        double[] logitData = toDoubleArray(logits);
        double[] targetData = toDoubleArray(targets);

        double sum = 0;
        for (int i = 0; i < logits.size(); i++) {
            double sig = sigmoid(logitData[i]);
            sig = Math.max(EPSILON, Math.min(1 - EPSILON, sig));
            sum += targetData[i] * Math.log(sig) + (1 - targetData[i]) * Math.log(1 - sig);
        }

        double loss = -sum / logits.size();
        return NdArray.array(new double[]{loss}, DType.FLOAT64);
    }

    /**
     * Compute gradient of BCE with logits loss with respect to logits.
     * <p>
     * gradient[i] = (sigmoid(pred[i]) - target[i]) / (sigmoid(pred[i]) * (1 - sigmoid(pred[i])))
     * <p>
     * Which simplifies to: gradient[i] = sigmoid(pred[i]) - target[i]
     * when computing on logits directly.
     *
     * @param logits  Raw logits
     * @param targets Target values
     * @return Gradient with same shape as logits
     */
    @Override
    /**
     * gradient方法。
     *      * @param logits NdArray类型参数
     * @param targets NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray gradient(NdArray logits, NdArray targets) {
        validateInputs(logits, targets);

        int size = logits.size();
        double[] result = new double[size];
        double scale = 1.0 / size;

        double[] logitData = toDoubleArray(logits);
        double[] targetData = toDoubleArray(targets);

        for (int i = 0; i < size; i++) {
            double sig = sigmoid(logitData[i]);
            result[i] = (sig - targetData[i]) * scale;
        }

        return NdArray.array(result, DType.FLOAT64);
    }

    /**
     * Sigmoid activation function.
     * <p>
     * sigmoid(x) = 1 / (1 + exp(-x))
     */
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private void validateInputs(NdArray logits, NdArray targets) {
        if (!logits.getShape().equals(targets.getShape())) {
            throw new IllegalArgumentException(
                    "Logits and targets must have the same shape: " +
                            logits.getShape() + " vs " + targets.getShape());
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

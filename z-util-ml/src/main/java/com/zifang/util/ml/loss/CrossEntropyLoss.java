package com.zifang.util.ml.loss;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Cross Entropy Loss combining LogSoftmax and NLL.
 * <p>
 * This loss is commonly used for multi-class classification.
 * It computes: -Σ(target[i] * log(softmax(pred)[i]))
 * <p>
 * The log_softmax is computed internally for numerical stability:
 * log_softmax(x)[i] = x[i] - log(Σexp(x[j]))
 */
public class CrossEntropyLoss implements LossFunction {

    /**
     * Compute cross entropy loss.
     *
     * @param predictions Predicted logits (before softmax)
     * @param targets     Target labels (one-hot encoded or class indices)
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

        NdArray logProbs = logSoftmax(predictions);
        double[] logProbData = toDoubleArray(logProbs);
        double[] targetData = toDoubleArray(targets);

        double sum = 0;
        for (int i = 0; i < predictions.size(); i++) {
            sum += targetData[i] * logProbData[i];
        }

        double loss = -sum / predictions.size();
        return NdArray.array(new double[]{loss}, DType.FLOAT64);
    }

    /**
     * Compute gradient of cross entropy loss with respect to predictions.
     * <p>
     * gradient = softmax(predictions) - targets
     *
     * @param predictions Predicted logits
     * @param targets     Target labels
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

        NdArray softmaxProbs = softmax(predictions);
        double[] result = elementwiseSubtract(toDoubleArray(softmaxProbs), toDoubleArray(targets));
        double scale = 1.0 / predictions.size();

        for (int i = 0; i < result.length; i++) {
            result[i] *= scale;
        }

        return NdArray.array(result, DType.FLOAT64).reshape(predictions.getShape().getDimensions());
    }

    /**
     * Compute log softmax in a numerically stable way.
     * <p>
     * log_softmax(x)[i] = x[i] - max(x) - log(Σexp(x[j] - max(x)))
     */
    private NdArray logSoftmax(NdArray predictions) {
        double[] predData = toDoubleArray(predictions);
        int size = predData.length;

        // Find max for numerical stability
        double maxVal = predData[0];
        for (int i = 1; i < size; i++) {
            if (predData[i] > maxVal) {
                maxVal = predData[i];
            }
        }

        // Compute sum of exp(x - max)
        double sumExp = 0;
        for (int i = 0; i < size; i++) {
            sumExp += Math.exp(predData[i] - maxVal);
        }

        double logSumExp = Math.log(sumExp);

        // Compute log_softmax
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = predData[i] - maxVal - logSumExp;
        }

        return NdArray.array(result, DType.FLOAT64);
    }

    /**
     * Compute softmax.
     * <p>
     * softmax(x)[i] = exp(x[i]) / Σexp(x[j])
     */
    private NdArray softmax(NdArray predictions) {
        double[] predData = toDoubleArray(predictions);
        int size = predData.length;

        // Find max for numerical stability
        double maxVal = predData[0];
        for (int i = 1; i < size; i++) {
            if (predData[i] > maxVal) {
                maxVal = predData[i];
            }
        }

        // Compute sum of exp(x - max)
        double sumExp = 0;
        for (int i = 0; i < size; i++) {
            sumExp += Math.exp(predData[i] - maxVal);
        }

        // Compute softmax
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = Math.exp(predData[i] - maxVal) / sumExp;
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

    private double[] elementwiseSubtract(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }
}

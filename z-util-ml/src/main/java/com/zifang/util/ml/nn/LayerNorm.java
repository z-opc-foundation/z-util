package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Layer Normalization layer
 * Normalizes inputs across the last N dimensions
 * Input: (*, normalized_shape)
 * Output: same shape as input
 */
public class LayerNorm extends Module {

    private final int normalizedShape;
    private final float eps;
    private NdArray weight;  // Scale parameter
    private NdArray bias;    // Shift parameter
    private NdArray savedMean;
    private NdArray savedInput;

    /**
     * LayerNorm方法。
     * * @param normalizedShape int类型参数
     */
    public LayerNorm(int normalizedShape) {
        this(normalizedShape, 1e-5f);
    }

    /**
     * LayerNorm方法。
     * * @param normalizedShape int类型参数
     *
     * @param eps float类型参数
     */
    public LayerNorm(int normalizedShape, float eps) {
        this.normalizedShape = normalizedShape;
        this.eps = eps;

        weight = NdArray.ones(new Shape(normalizedShape), DType.FLOAT32);
        bias = NdArray.zeros(new Shape(normalizedShape), DType.FLOAT32);

        registerParameter("weight", weight);
        registerParameter("bias", bias);
    }

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        savedInput = input.copy();

        int ndim = input.ndim();
        int featureSize = normalizedShape;

        // Calculate the size of the normalized part
        int normalizedElements = featureSize;
        int totalSize = input.size();
        int batchSize = totalSize / normalizedElements;

        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        Object wData = weight.getData();
        Object bData = bias.getData();

        Object meanData, varData;

        if (training) {
            float[] meanArr = new float[batchSize];
            float[] varArr = new float[batchSize];

            // Compute mean and variance for each batch
            for (int b = 0; b < batchSize; b++) {
                float sum = 0.0f;
                for (int i = 0; i < featureSize; i++) {
                    int idx = b * featureSize + i;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    sum += val;
                }
                meanArr[b] = sum / featureSize;

                sum = 0.0f;
                for (int i = 0; i < featureSize; i++) {
                    int idx = b * featureSize + i;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    float diff = val - meanArr[b];
                    sum += diff * diff;
                }
                varArr[b] = sum / featureSize;
            }

            savedMean = NdArray.array(meanArr, DType.FLOAT32);

            // Normalize, scale, and shift
            for (int b = 0; b < batchSize; b++) {
                float mean = meanArr[b];
                float std = (float) Math.sqrt(varArr[b] + eps);

                for (int i = 0; i < featureSize; i++) {
                    int idx = b * featureSize + i;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    float normalized = (val - mean) / std;
                    float w = ((Number) com.zifang.util.numpy.Array.get(wData, i)).floatValue();
                    float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, i)).floatValue();
                    com.zifang.util.numpy.Array.set(outData, idx, w * normalized + bVal);
                }
            }
        } else {
            // Inference mode
            for (int b = 0; b < batchSize; b++) {
                float sum = 0.0f;
                for (int i = 0; i < featureSize; i++) {
                    int idx = b * featureSize + i;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    sum += val;
                }
                float mean = sum / featureSize;

                sum = 0.0f;
                for (int i = 0; i < featureSize; i++) {
                    int idx = b * featureSize + i;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    float diff = val - mean;
                    sum += diff * diff;
                }
                float std = (float) Math.sqrt(sum / featureSize + eps);

                for (int i = 0; i < featureSize; i++) {
                    int idx = b * featureSize + i;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    float normalized = (val - mean) / std;
                    float w = ((Number) com.zifang.util.numpy.Array.get(wData, i)).floatValue();
                    float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, i)).floatValue();
                    com.zifang.util.numpy.Array.set(outData, idx, w * normalized + bVal);
                }
            }
        }

        return output;
    }

    @Override
    /**
     * backward方法。
     *      * @param gradOutput NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray backward(NdArray gradOutput) {
        int totalSize = gradOutput.size();
        int batchSize = totalSize / normalizedShape;

        Object gOutData = gradOutput.getData();
        Object inData = savedInput.getData();
        Object gInData = NdArray.zeros(gradOutput.getShape(), DType.FLOAT32).getData();
        Object wData = weight.getData();

        float[] meanArr = new float[savedMean.size()];
        Object mData = savedMean.getData();
        for (int i = 0; i < meanArr.length; i++) {
            meanArr[i] = ((Number) com.zifang.util.numpy.Array.get(mData, i)).floatValue();
        }

        // Gradient w.r.t. weight and bias
        float[] wGrad = new float[normalizedShape];
        float[] bGrad = new float[normalizedShape];

        for (int b = 0; b < batchSize; b++) {
            float mean = meanArr[b];

            // First compute normalized input
            float sumVar = 0.0f;
            for (int i = 0; i < normalizedShape; i++) {
                int idx = b * normalizedShape + i;
                float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                sumVar += (val - mean) * (val - mean);
            }
            float var = sumVar / normalizedShape;
            float std = (float) Math.sqrt(var + eps);

            for (int i = 0; i < normalizedShape; i++) {
                int idx = b * normalizedShape + i;
                float x = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                float xNorm = (x - mean) / std;
                float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, idx)).floatValue();
                float w = ((Number) com.zifang.util.numpy.Array.get(wData, i)).floatValue();

                wGrad[i] += gOut * xNorm;
                bGrad[i] += gOut;
            }
        }

        // Gradient w.r.t. input
        NdArray gradInput = NdArray.zeros(savedInput.getShape(), DType.FLOAT32);
        Object giData = gradInput.getData();

        for (int b = 0; b < batchSize; b++) {
            float mean = meanArr[b];

            float sumVar = 0.0f;
            for (int i = 0; i < normalizedShape; i++) {
                int idx = b * normalizedShape + i;
                float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                sumVar += (val - mean) * (val - mean);
            }
            float var = sumVar / normalizedShape;
            float std = (float) Math.sqrt(var + eps);
            float std3 = std * std * std;

            // Sum over output gradients weighted by gamma
            float[] weightedSum = new float[normalizedShape];
            for (int i = 0; i < normalizedShape; i++) {
                int idx = b * normalizedShape + i;
                float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, idx)).floatValue();
                float w = ((Number) com.zifang.util.numpy.Array.get(wData, i)).floatValue();
                weightedSum[i] = gOut * w;
            }

            float dMean = 0.0f;
            for (int i = 0; i < normalizedShape; i++) {
                dMean += weightedSum[i];
            }
            dMean /= std;

            float dVar = 0.0f;
            for (int i = 0; i < normalizedShape; i++) {
                int idx = b * normalizedShape + i;
                float x = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                dVar += weightedSum[i] * (x - mean);
            }
            dVar *= -0.5f * Math.pow(std, -3);

            for (int i = 0; i < normalizedShape; i++) {
                int idx = b * normalizedShape + i;
                float x = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                float xCentered = x - mean;

                float part1 = weightedSum[i] / std;
                float part2 = dVar * 2 * xCentered / normalizedShape;
                float part3 = dMean / normalizedShape;

                com.zifang.util.numpy.Array.set(giData, idx, part1 + part2 + part3);
            }
        }

        return gradInput;
    }

    /**
     * getWeight方法。
     *
     * @return NdArray类型返回值
     */
    public NdArray getWeight() {
        return weight;
    }

    /**
     * getBias方法。
     *
     * @return NdArray类型返回值
     */
    public NdArray getBias() {
        return bias;
    }
}

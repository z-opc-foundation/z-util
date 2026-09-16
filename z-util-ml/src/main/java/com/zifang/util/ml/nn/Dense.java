package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Dense (fully connected) layer: y = Wx + b
 */
public class Dense extends Module {

    private final int inputFeatures;
    private final int outputFeatures;
    private NdArray weight;  // Shape: (outputFeatures, inputFeatures)
    private NdArray bias;    // Shape: (outputFeatures,)
    private NdArray savedInput;  // For backward pass
    private NdArray savedOutput; // For backward pass

    /**
     * Dense方法。
     * * @param inputFeatures int类型参数
     *
     * @param outputFeatures int类型参数
     */
    public Dense(int inputFeatures, int outputFeatures) {
        this.inputFeatures = inputFeatures;
        this.outputFeatures = outputFeatures;

        // Initialize weights with Xavier uniform initialization
        weight = NdArray.zeros(new Shape(outputFeatures, inputFeatures), DType.FLOAT32);
        initXavierUniform(weight, inputFeatures, outputFeatures);
        registerParameter("weight", weight);

        // Initialize bias to zeros
        bias = NdArray.zeros(new Shape(outputFeatures), DType.FLOAT32);
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

        int batchSize;
        if (input.ndim() == 1) {
            // (inputFeatures,)
            batchSize = 1;
        } else if (input.ndim() == 2) {
            // (batchSize, inputFeatures)
            batchSize = input.getShape().get(0);
        } else {
            throw new IllegalArgumentException("Input must be 1D or 2D, got " + input.ndim() + "D");
        }

        Shape inputShape = input.getShape();
        int rows, cols;

        if (input.ndim() == 1) {
            rows = 1;
            cols = inputFeatures;
        } else {
            rows = inputShape.get(0);
            cols = inputShape.get(1);
        }

        // Compute output = input @ W.T + bias
        // input: (batch, inputFeatures) @ W: (outputFeatures, inputFeatures) -> (batch, outputFeatures)
        NdArray output;
        if (input.ndim() == 1) {
            output = NdArray.zeros(new Shape(outputFeatures), DType.FLOAT32);
        } else {
            output = NdArray.zeros(new Shape(batchSize, outputFeatures), DType.FLOAT32);
        }

        Object wData = weight.getData();
        Object bData = bias.getData();
        Object outData = output.getData();
        Object inData = input.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int outIdx = 0; outIdx < outputFeatures; outIdx++) {
                float sum = 0.0f;
                for (int inIdx = 0; inIdx < inputFeatures; inIdx++) {
                    float w = ((Number) com.zifang.util.numpy.Array.get(wData, outIdx * inputFeatures + inIdx)).floatValue();
                    float x;
                    if (input.ndim() == 1) {
                        x = ((Number) com.zifang.util.numpy.Array.get(inData, inIdx)).floatValue();
                    } else {
                        x = ((Number) com.zifang.util.numpy.Array.get(inData, b * inputFeatures + inIdx)).floatValue();
                    }
                    sum += w * x;
                }
                float biasVal = ((Number) com.zifang.util.numpy.Array.get(bData, outIdx)).floatValue();
                float outVal = sum + biasVal;

                if (output.ndim() == 1) {
                    com.zifang.util.numpy.Array.set(outData, outIdx, outVal);
                } else {
                    com.zifang.util.numpy.Array.set(outData, b * outputFeatures + outIdx, outVal);
                }
            }
        }

        savedOutput = output.copy();
        return output;
    }

    @Override
    /**
     * backward方法。
     *      * @param gradOutput NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray backward(NdArray gradOutput) {
        // gradOutput: (batchSize, outputFeatures) or (outputFeatures,)
        Object gOutData = gradOutput.getData();
        int batchSize = gradOutput.ndim() == 1 ? 1 : gradOutput.getShape().get(0);

        // Gradient with respect to weights: dL/dW = gradOutput.T @ input
        Object wGradData = NdArray.zeros(weight.getShape(), DType.FLOAT32).getData();

        // Gradient with respect to bias: dL/db = sum(gradOutput, axis=0)
        Object bGradData = NdArray.zeros(bias.getShape(), DType.FLOAT32).getData();

        for (int outIdx = 0; outIdx < outputFeatures; outIdx++) {
            float bGradSum = 0.0f;
            for (int b = 0; b < batchSize; b++) {
                float gOut;
                if (gradOutput.ndim() == 1) {
                    gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, outIdx)).floatValue();
                } else {
                    gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, b * outputFeatures + outIdx)).floatValue();
                }
                bGradSum += gOut;
            }
            com.zifang.util.numpy.Array.set(bGradData, outIdx, bGradSum);
        }

        // Update weights gradient
        Object inData = savedInput.getData();
        for (int outIdx = 0; outIdx < outputFeatures; outIdx++) {
            for (int inIdx = 0; inIdx < inputFeatures; inIdx++) {
                float wGrad = 0.0f;
                for (int b = 0; b < batchSize; b++) {
                    float gOut;
                    if (gradOutput.ndim() == 1) {
                        gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, outIdx)).floatValue();
                    } else {
                        gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, b * outputFeatures + outIdx)).floatValue();
                    }
                    float x;
                    if (savedInput.ndim() == 1) {
                        x = ((Number) com.zifang.util.numpy.Array.get(inData, inIdx)).floatValue();
                    } else {
                        x = ((Number) com.zifang.util.numpy.Array.get(inData, b * inputFeatures + inIdx)).floatValue();
                    }
                    wGrad += gOut * x;
                }
                com.zifang.util.numpy.Array.set(wGradData, outIdx * inputFeatures + inIdx, wGrad);
            }
        }

        // Gradient w.r.t. input: dL/dx = gradOutput @ W
        NdArray gradInput;
        if (savedInput.ndim() == 1) {
            gradInput = NdArray.zeros(new Shape(inputFeatures), DType.FLOAT32);
        } else {
            gradInput = NdArray.zeros(new Shape(batchSize, inputFeatures), DType.FLOAT32);
        }
        Object gInData = gradInput.getData();
        Object wData = weight.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int inIdx = 0; inIdx < inputFeatures; inIdx++) {
                float sum = 0.0f;
                for (int outIdx = 0; outIdx < outputFeatures; outIdx++) {
                    float gOut;
                    if (gradOutput.ndim() == 1) {
                        gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, outIdx)).floatValue();
                    } else {
                        gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, b * outputFeatures + outIdx)).floatValue();
                    }
                    float w = ((Number) com.zifang.util.numpy.Array.get(wData, outIdx * inputFeatures + inIdx)).floatValue();
                    sum += gOut * w;
                }
                if (gradInput.ndim() == 1) {
                    com.zifang.util.numpy.Array.set(gInData, inIdx, sum);
                } else {
                    com.zifang.util.numpy.Array.set(gInData, b * inputFeatures + inIdx, sum);
                }
            }
        }

        return gradInput;
    }

    /**
     * Returns the weight tensor
     */
    public NdArray getWeight() {
        return weight;
    }

    /**
     * Returns the bias tensor
     */
    public NdArray getBias() {
        return bias;
    }
}

package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * 2D Convolution layer: applies conv2d over input tensor
 * Input: (batchSize, inChannels, height, width)
 * Output: (batchSize, outChannels, outHeight, outWidth)
 */
public class Conv2d extends Module {

    private final int inChannels;
    private final int outChannels;
    private final int kernelSize;
    private final int stride;
    private final int padding;
    private final int dilation;

    private NdArray weight;  // Shape: (outChannels, inChannels, kernelSize, kernelSize)
    private NdArray bias;    // Shape: (outChannels,)

    private NdArray savedInput;
    private int[] outputShape;

    /**
     * Conv2d方法。
     * * @param inChannels int类型参数
     *
     * @param outChannels int类型参数
     * @param kernelSize  int类型参数
     */
    public Conv2d(int inChannels, int outChannels, int kernelSize) {
        this(inChannels, outChannels, kernelSize, 1, kernelSize / 2);
    }

    /**
     * Conv2d方法。
     * * @param inChannels int类型参数
     *
     * @param outChannels int类型参数
     * @param kernelSize  int类型参数
     * @param stride      int类型参数
     * @param padding     int类型参数
     */
    public Conv2d(int inChannels, int outChannels, int kernelSize, int stride, int padding) {
        this(inChannels, outChannels, kernelSize, stride, padding, 1);
    }

    /**
     * Conv2d方法。
     * * @param inChannels int类型参数
     *
     * @param outChannels int类型参数
     * @param kernelSize  int类型参数
     * @param stride      int类型参数
     * @param padding     int类型参数
     * @param dilation    int类型参数
     */
    public Conv2d(int inChannels, int outChannels, int kernelSize, int stride, int padding, int dilation) {
        this.inChannels = inChannels;
        this.outChannels = outChannels;
        this.kernelSize = kernelSize;
        this.stride = stride;
        this.padding = padding;
        this.dilation = dilation;

        // Initialize weights with Kaiming initialization
        int fanIn = inChannels * kernelSize * kernelSize;
        weight = NdArray.zeros(new Shape(outChannels, inChannels, kernelSize, kernelSize), DType.FLOAT32);
        initKaimingNormal(weight, fanIn);
        registerParameter("weight", weight);

        // Initialize bias to zeros
        bias = NdArray.zeros(new Shape(outChannels), DType.FLOAT32);
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

        if (input.ndim() != 4) {
            throw new IllegalArgumentException("Input must be 4D: (batchSize, channels, height, width)");
        }

        int batchSize = input.getShape().get(0);
        int inH = input.getShape().get(2);
        int inW = input.getShape().get(3);

        // Calculate output dimensions
        int effectiveKernelSize = (kernelSize - 1) * dilation + 1;
        int outH = (inH + 2 * padding - effectiveKernelSize) / stride + 1;
        int outW = (inW + 2 * padding - effectiveKernelSize) / stride + 1;

        outputShape = new int[]{batchSize, outChannels, outH, outW};
        NdArray output = NdArray.zeros(new Shape(outputShape), DType.FLOAT32);

        Object inData = input.getData();
        Object wData = weight.getData();
        Object bData = bias.getData();
        Object outData = output.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int outC = 0; outC < outChannels; outC++) {
                for (int outY = 0; outY < outH; outY++) {
                    for (int outX = 0; outX < outW; outX++) {
                        float sum = 0.0f;

                        for (int inC = 0; inC < inChannels; inC++) {
                            for (int kY = 0; kY < kernelSize; kY++) {
                                for (int kX = 0; kX < kernelSize; kX++) {
                                    int inY = outY * stride + kY * dilation - padding;
                                    int inX = outX * stride + kX * dilation - padding;

                                    if (inY >= 0 && inY < inH && inX >= 0 && inX < inW) {
                                        int inIdx = ((b * inChannels + inC) * inH + inY) * inW + inX;
                                        int wIdx = ((outC * inChannels + inC) * kernelSize + kY) * kernelSize + kX;

                                        float x = ((Number) com.zifang.util.numpy.Array.get(inData, inIdx)).floatValue();
                                        float w = ((Number) com.zifang.util.numpy.Array.get(wData, wIdx)).floatValue();
                                        sum += x * w;
                                    }
                                }
                            }
                        }

                        float biasVal = ((Number) com.zifang.util.numpy.Array.get(bData, outC)).floatValue();
                        float outVal = sum + biasVal;

                        int outIdx = ((b * outChannels + outC) * outH + outY) * outW + outX;
                        com.zifang.util.numpy.Array.set(outData, outIdx, outVal);
                    }
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
        Object gOutData = gradOutput.getData();
        int batchSize = gradOutput.getShape().get(0);
        int outH = gradOutput.getShape().get(2);
        int outW = gradOutput.getShape().get(3);

        Object inData = savedInput.getData();
        int inH = savedInput.getShape().get(2);
        int inW = savedInput.getShape().get(3);

        // Gradient w.r.t. input
        NdArray gradInput = NdArray.zeros(savedInput.getShape(), DType.FLOAT32);
        Object gInData = gradInput.getData();

        Object wData = weight.getData();

        // Compute gradient w.r.t. weights and bias
        Object wGradData = NdArray.zeros(weight.getShape(), DType.FLOAT32).getData();
        Object bGradData = NdArray.zeros(bias.getShape(), DType.FLOAT32).getData();

        for (int b = 0; b < batchSize; b++) {
            for (int outC = 0; outC < outChannels; outC++) {
                // Bias gradient
                for (int outY = 0; outY < outH; outY++) {
                    for (int outX = 0; outX < outW; outX++) {
                        int outIdx = ((b * outChannels + outC) * outH + outY) * outW + outX;
                        float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, outIdx)).floatValue();
                        float bGrad = ((Number) com.zifang.util.numpy.Array.get(bGradData, outC)).floatValue();
                        com.zifang.util.numpy.Array.set(bGradData, outC, bGrad + gOut);
                    }
                }

                for (int inC = 0; inC < inChannels; inC++) {
                    for (int kY = 0; kY < kernelSize; kY++) {
                        for (int kX = 0; kX < kernelSize; kX++) {
                            float wGrad = 0.0f;

                            for (int outY = 0; outY < outH; outY++) {
                                for (int outX = 0; outX < outW; outX++) {
                                    int inY = outY * stride + kY * dilation - padding;
                                    int inX = outX * stride + kX * dilation - padding;

                                    if (inY >= 0 && inY < inH && inX >= 0 && inX < inW) {
                                        int outIdx = ((b * outChannels + outC) * outH + outY) * outW + outX;
                                        int inIdx = ((b * inChannels + inC) * inH + inY) * inW + inX;

                                        float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, outIdx)).floatValue();
                                        float x = ((Number) com.zifang.util.numpy.Array.get(inData, inIdx)).floatValue();
                                        wGrad += gOut * x;
                                    }
                                }
                            }

                            int wGradIdx = ((outC * inChannels + inC) * kernelSize + kY) * kernelSize + kX;
                            float existingWGrad = ((Number) com.zifang.util.numpy.Array.get(wGradData, wGradIdx)).floatValue();
                            com.zifang.util.numpy.Array.set(wGradData, wGradIdx, existingWGrad + wGrad);
                        }
                    }
                }
            }
        }

        // Gradient w.r.t. input
        for (int b = 0; b < batchSize; b++) {
            for (int inC = 0; inC < inChannels; inC++) {
                for (int inY = 0; inY < inH; inY++) {
                    for (int inX = 0; inX < inW; inX++) {
                        float sum = 0.0f;

                        for (int outC = 0; outC < outChannels; outC++) {
                            for (int kY = 0; kY < kernelSize; kY++) {
                                for (int kX = 0; kX < kernelSize; kX++) {
                                    int outY = (inY + padding - kY * dilation) / stride;
                                    int outX = (inX + padding - kX * dilation) / stride;

                                    if (outY >= 0 && outY < outH && outX >= 0 && outX < outW
                                            && (inY + padding - kY * dilation) % stride == 0
                                            && (inX + padding - kX * dilation) % stride == 0) {

                                        int outIdx = ((b * outChannels + outC) * outH + outY) * outW + outX;
                                        int wIdx = ((outC * inChannels + inC) * kernelSize + kY) * kernelSize + kX;

                                        float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, outIdx)).floatValue();
                                        float w = ((Number) com.zifang.util.numpy.Array.get(wData, wIdx)).floatValue();
                                        sum += gOut * w;
                                    }
                                }
                            }
                        }

                        int inIdx = ((b * inChannels + inC) * inH + inY) * inW + inX;
                        com.zifang.util.numpy.Array.set(gInData, inIdx, sum);
                    }
                }
            }
        }

        return gradInput;
    }

    /**
     * getInChannels方法。
     *
     * @return int类型返回值
     */
    public int getInChannels() {
        return inChannels;
    }

    /**
     * getOutChannels方法。
     *
     * @return int类型返回值
     */
    public int getOutChannels() {
        return outChannels;
    }

    /**
     * getKernelSize方法。
     *
     * @return int类型返回值
     */
    public int getKernelSize() {
        return kernelSize;
    }

    /**
     * getStride方法。
     *
     * @return int类型返回值
     */
    public int getStride() {
        return stride;
    }

    /**
     * getPadding方法。
     *
     * @return int类型返回值
     */
    public int getPadding() {
        return padding;
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

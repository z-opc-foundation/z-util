package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * 2D Max Pooling layer
 * Input: (batchSize, channels, height, width)
 * Output: (batchSize, channels, outHeight, outWidth)
 */
public class MaxPool2d extends Module {

    private final int kernelSize;
    private final int stride;
    private NdArray savedIndices;  // For backward pass
    private int[] outputShape;
    private int[] inputShape;  // Save input shape for backward pass

    /**
     * MaxPool2d方法。
     * * @param kernelSize int类型参数
     */
    public MaxPool2d(int kernelSize) {
        this(kernelSize, kernelSize);
    }

    /**
     * MaxPool2d方法。
     * * @param kernelSize int类型参数
     *
     * @param stride int类型参数
     */
    public MaxPool2d(int kernelSize, int stride) {
        this.kernelSize = kernelSize;
        this.stride = stride;
    }

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        if (input.ndim() != 4) {
            throw new IllegalArgumentException("Input must be 4D: (batchSize, channels, height, width)");
        }

        int batchSize = input.getShape().get(0);
        int channels = input.getShape().get(1);
        int inH = input.getShape().get(2);
        int inW = input.getShape().get(3);

        int outH = (inH - kernelSize) / stride + 1;
        int outW = (inW - kernelSize) / stride + 1;

        outputShape = new int[]{batchSize, channels, outH, outW};
        inputShape = new int[]{batchSize, channels, inH, inW};
        NdArray output = NdArray.zeros(new Shape(outputShape), DType.FLOAT32);
        savedIndices = NdArray.zeros(new Shape(outputShape), DType.INT32);

        Object inData = input.getData();
        Object outData = output.getData();
        Object idxData = savedIndices.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int c = 0; c < channels; c++) {
                for (int outY = 0; outY < outH; outY++) {
                    for (int outX = 0; outX < outW; outX++) {
                        float maxVal = Float.NEGATIVE_INFINITY;
                        int maxIdx = -1;

                        for (int kY = 0; kY < kernelSize; kY++) {
                            for (int kX = 0; kX < kernelSize; kX++) {
                                int inY = outY * stride + kY;
                                int inX = outX * stride + kX;

                                int inIdx = ((b * channels + c) * inH + inY) * inW + inX;
                                float val = ((Number) com.zifang.util.numpy.Array.get(inData, inIdx)).floatValue();

                                if (val > maxVal) {
                                    maxVal = val;
                                    maxIdx = inIdx;
                                }
                            }
                        }

                        int outIdx = ((b * channels + c) * outH + outY) * outW + outX;
                        com.zifang.util.numpy.Array.set(outData, outIdx, maxVal);
                        com.zifang.util.numpy.Array.set(idxData, outIdx, maxIdx);
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
        int batchSize = gradOutput.getShape().get(0);
        int channels = gradOutput.getShape().get(1);
        int outH = gradOutput.getShape().get(2);
        int outW = gradOutput.getShape().get(3);

        // Use saved input shape, not savedIndices shape
        int inH = inputShape[2];
        int inW = inputShape[3];

        NdArray gradInput = NdArray.zeros(new Shape(batchSize, channels, inH, inW), DType.FLOAT32);
        Object gInData = gradInput.getData();
        Object gOutData = gradOutput.getData();
        Object idxData = savedIndices.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int c = 0; c < channels; c++) {
                for (int outY = 0; outY < outH; outY++) {
                    for (int outX = 0; outX < outW; outX++) {
                        int outIdx = ((b * channels + c) * outH + outY) * outW + outX;
                        float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, outIdx)).floatValue();
                        int maxIdx = ((Number) com.zifang.util.numpy.Array.get(idxData, outIdx)).intValue();

                        float existing = ((Number) com.zifang.util.numpy.Array.get(gInData, maxIdx)).floatValue();
                        com.zifang.util.numpy.Array.set(gInData, maxIdx, existing + gOut);
                    }
                }
            }
        }

        return gradInput;
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
}

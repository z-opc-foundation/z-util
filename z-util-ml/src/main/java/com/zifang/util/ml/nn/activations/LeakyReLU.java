package com.zifang.util.ml.nn.activations;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Leaky ReLU activation
 * f(x) = x if x > 0, else negative_slope * x
 * f'(x) = 1 if x > 0, else negative_slope
 */
public class LeakyReLU extends com.zifang.util.ml.nn.Module {

    private final float negativeSlope;
    private NdArray savedInput;

    /**
     * LeakyReLU方法。
     */
    public LeakyReLU() {
        this(0.01f);
    }

    /**
     * LeakyReLU方法。
     * * @param negativeSlope float类型参数
     */
    public LeakyReLU(float negativeSlope) {
        this.negativeSlope = negativeSlope;
    }

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        savedInput = input.copy();

        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            com.zifang.util.numpy.Array.set(outData, i, val > 0 ? val : negativeSlope * val);
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
        NdArray gradInput = NdArray.zeros(gradOutput.getShape(), DType.FLOAT32);
        Object gInData = gradInput.getData();
        Object gOutData = gradOutput.getData();
        Object inData = savedInput.getData();
        int size = gradOutput.size();

        for (int i = 0; i < size; i++) {
            float x = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, i)).floatValue();
            float slope = x > 0 ? 1.0f : negativeSlope;
            com.zifang.util.numpy.Array.set(gInData, i, gOut * slope);
        }

        return gradInput;
    }

    /**
     * getNegativeSlope方法。
     *
     * @return float类型返回值
     */
    public float getNegativeSlope() {
        return negativeSlope;
    }
}

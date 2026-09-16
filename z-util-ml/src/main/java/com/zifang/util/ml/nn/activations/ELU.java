package com.zifang.util.ml.nn.activations;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * ELU (Exponential Linear Unit) activation
 * f(x) = x if x > 0, else alpha * (exp(x) - 1)
 * f'(x) = 1 if x > 0, else alpha * exp(x)
 */
public class ELU extends com.zifang.util.ml.nn.Module {

    private final float alpha;
    private NdArray savedInput;

    /**
     * ELU方法。
     */
    public ELU() {
        this(1.0f);
    }

    /**
     * ELU方法。
     * * @param alpha float类型参数
     */
    public ELU(float alpha) {
        this.alpha = alpha;
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
            float result = val > 0 ? val : alpha * ((float) Math.exp(val) - 1.0f);
            com.zifang.util.numpy.Array.set(outData, i, result);
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
            float slope = x > 0 ? 1.0f : alpha * (float) Math.exp(x);
            com.zifang.util.numpy.Array.set(gInData, i, gOut * slope);
        }

        return gradInput;
    }

    /**
     * getAlpha方法。
     *
     * @return float类型返回值
     */
    public float getAlpha() {
        return alpha;
    }
}

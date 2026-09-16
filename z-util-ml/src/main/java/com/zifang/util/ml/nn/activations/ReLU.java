package com.zifang.util.ml.nn.activations;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * ReLU (Rectified Linear Unit) activation
 * f(x) = max(0, x)
 * f'(x) = 1 if x > 0, else 0
 */
public class ReLU extends com.zifang.util.ml.nn.Module {

    private NdArray savedInput;

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
            com.zifang.util.numpy.Array.set(outData, i, Math.max(0, val));
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
            // dL/dx = dL/dy * f'(x) where f'(x) = 1 if x > 0, else 0
            com.zifang.util.numpy.Array.set(gInData, i, x > 0 ? gOut : 0);
        }

        return gradInput;
    }
}

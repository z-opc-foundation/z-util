package com.zifang.util.ml.nn.activations;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Swish activation function
 * f(x) = x * sigmoid(x)
 * f'(x) = sigmoid(x) + x * sigmoid(x) * (1 - sigmoid(x))
 * = sigmoid(x) + x * sigmoid(x) * (1 - sigmoid(x))
 * = sigmoid(x) * (1 + x * (1 - sigmoid(x)))
 */
public class Swish extends com.zifang.util.ml.nn.Module {

    private NdArray savedInput;
    private NdArray savedSigmoid;

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        savedInput = input.copy();

        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        NdArray sigmoid = NdArray.zeros(input.getShape(), DType.FLOAT32);

        Object inData = input.getData();
        Object outData = output.getData();
        Object sigData = sigmoid.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float x = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            float sig = (float) (1.0 / (1.0 + Math.exp(-x)));
            float result = x * sig;
            com.zifang.util.numpy.Array.set(outData, i, result);
            com.zifang.util.numpy.Array.set(sigData, i, sig);
        }

        savedSigmoid = sigmoid;
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
        Object sigData = savedSigmoid.getData();
        int size = gradOutput.size();

        for (int i = 0; i < size; i++) {
            float x = ((Number) com.zifang.util.numpy.Array.get(savedInput.getData(), i)).floatValue();
            float sig = ((Number) com.zifang.util.numpy.Array.get(sigData, i)).floatValue();
            float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, i)).floatValue();

            // f'(x) = sigmoid(x) + x * sigmoid(x) * (1 - sigmoid(x))
            float dsig = sig * (1.0f - sig);
            float dx = sig + x * dsig;

            com.zifang.util.numpy.Array.set(gInData, i, gOut * dx);
        }

        return gradInput;
    }
}

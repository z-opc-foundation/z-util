package com.zifang.util.ml.nn.activations;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * GELU (Gaussian Error Linear Unit) activation
 * f(x) = x * Phi(x) where Phi is the CDF of standard normal
 * Approximation: 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3)))
 * f'(x) = Phi(x) + x * phi(x) where phi is the PDF of standard normal
 */
public class GELU extends com.zifang.util.ml.nn.Module {

    private final boolean approximate;
    private NdArray savedInput;

    /**
     * GELU方法。
     */
    public GELU() {
        this(true);
    }

    /**
     * GELU方法。
     * * @param approximate boolean类型参数
     */
    public GELU(boolean approximate) {
        this.approximate = approximate;
    }

    // Standard normal PDF
    private static float normalPdf(float x) {
        return (float) (Math.exp(-0.5f * x * x) / Math.sqrt(2 * Math.PI));
    }

    // Standard normal CDF (approximation)
    private static float normalCdf(float x) {
        float t = (float) (1.0 / (1.0 + 0.2316419 * Math.abs(x)));
        float poly = t * (0.319381530f + t * (-0.356563782f + t * (1.781477937f + t * (-1.821255978f + t * 1.330274429f))));
        float p = 0.5f * (1.0f + (float) Math.signum(x) * poly);
        return p;
    }

    // Tanh approximation for GELU
    private static float tanhApprox(float x) {
        float t = (float) Math.tanh(x);
        return t;
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

        if (approximate) {
            // Use approximation: 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3)))
            float sqrt2OverPi = (float) Math.sqrt(2.0 / Math.PI);
            float coeff = 0.044715f;

            for (int i = 0; i < size; i++) {
                float x = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
                float xCubed = x * x * x;
                float inner = sqrt2OverPi * (x + coeff * xCubed);
                float result = 0.5f * x * (1.0f + tanhApprox(inner));
                com.zifang.util.numpy.Array.set(outData, i, result);
            }
        } else {
            // Use exact: x * Phi(x)
            for (int i = 0; i < size; i++) {
                float x = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
                float result = x * normalCdf(x);
                com.zifang.util.numpy.Array.set(outData, i, result);
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
        NdArray gradInput = NdArray.zeros(gradOutput.getShape(), DType.FLOAT32);
        Object gInData = gradInput.getData();
        Object gOutData = gradOutput.getData();
        Object inData = savedInput.getData();
        int size = gradOutput.size();

        if (approximate) {
            // Derivative of approximate GELU
            float sqrt2OverPi = (float) Math.sqrt(2.0 / Math.PI);
            float coeff = 0.044715f;

            for (int i = 0; i < size; i++) {
                float x = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
                float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, i)).floatValue();

                float xCubed = x * x * x;
                float inner = sqrt2OverPi * (x + coeff * xCubed);
                float tanhVal = tanhApprox(inner);

                float dx = 0.5f * (1.0f + tanhVal) +
                        0.5f * x * (1.0f - tanhVal * tanhVal) *
                                sqrt2OverPi * (1.0f + 3.0f * coeff * x * x);

                com.zifang.util.numpy.Array.set(gInData, i, gOut * dx);
            }
        } else {
            // Derivative of exact GELU: Phi(x) + x * phi(x)
            for (int i = 0; i < size; i++) {
                float x = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
                float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, i)).floatValue();

                float cdf = normalCdf(x);
                float pdf = normalPdf(x);
                float dx = cdf + x * pdf;

                com.zifang.util.numpy.Array.set(gInData, i, gOut * dx);
            }
        }

        return gradInput;
    }

    /**
     * isApproximate方法。
     *
     * @return boolean类型返回值
     */
    public boolean isApproximate() {
        return approximate;
    }
}

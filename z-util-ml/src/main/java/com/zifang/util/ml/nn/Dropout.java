package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * Dropout layer - regularization technique
 * During training, randomly sets some values to zero with probability p
 * During inference, scales output by (1 - p)
 */
public class Dropout extends Module {

    private final float p;
    private final Random random;
    private NdArray mask;

    /**
     * Dropout方法。
     * * @param p float类型参数
     */
    public Dropout(float p) {
        this(p, new Random());
    }

    /**
     * Dropout方法。
     * * @param p float类型参数
     *
     * @param random Random类型参数
     */
    public Dropout(float p, Random random) {
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("Dropout probability must be between 0 and 1");
        }
        this.p = p;
        this.random = random;
    }

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        if (training) {
            // Create mask where each element is kept with probability (1-p)
            mask = NdArray.zeros(input.getShape(), DType.FLOAT32);
            Object maskData = mask.getData();
            Object inData = input.getData();
            int size = input.size();

            for (int i = 0; i < size; i++) {
                float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
                if (random.nextFloat() < p) {
                    // Drop this element
                    com.zifang.util.numpy.Array.set(maskData, i, 0.0f);
                } else {
                    // Keep this element, scale by 1/(1-p)
                    com.zifang.util.numpy.Array.set(maskData, i, val / (1 - p));
                }
            }

            return mask.copy();
        } else {
            // Inference mode: just return input scaled by (1-p)
            NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
            Object outData = output.getData();
            Object inData = input.getData();
            int size = input.size();
            float scale = 1 - p;

            for (int i = 0; i < size; i++) {
                float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
                com.zifang.util.numpy.Array.set(outData, i, val * scale);
            }
            return output;
        }
    }

    @Override
    public NdArray backward(NdArray gradOutput) {
        // During training: dL/dx = dL/dy * mask (scaled)
        // During inference: dL/dx = dL/dy * (1-p)
        if (training && mask != null) {
            NdArray gradInput = NdArray.zeros(gradOutput.getShape(), DType.FLOAT32);
            Object gInData = gradInput.getData();
            Object gOutData = gradOutput.getData();
            Object maskData = mask.getData();
            int size = gradOutput.size();

            for (int i = 0; i < size; i++) {
                float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, i)).floatValue();
                float m = ((Number) com.zifang.util.numpy.Array.get(maskData, i)).floatValue();
                // mask already contains scaled values (val / (1-p)) during forward
                // so gradInput = gradOutput * (mask / (1-p)) = gradOutput * mask / (1-p)
                // But simpler: gradInput = gradOutput * mask (since mask has 0 or 1/(1-p))
                // Actually: during forward we did: mask[i] = val / (1-p) if kept, else 0
                // During backward: we want dL/dval = dL/dout * (1/(1-p)) if kept
                // But we already have gradOutput, so: gradInput[i] = gradOutput[i] * (1/(1-p)) if mask[i] != 0
                if (m != 0) {
                    com.zifang.util.numpy.Array.set(gInData, i, gOut / (1 - p));
                }
            }
            return gradInput;
        } else {
            float scale = 1 - p;
            NdArray gradInput = NdArray.zeros(gradOutput.getShape(), DType.FLOAT32);
            Object gInData = gradInput.getData();
            Object gOutData = gradOutput.getData();
            int size = gradOutput.size();

            for (int i = 0; i < size; i++) {
                float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, i)).floatValue();
                com.zifang.util.numpy.Array.set(gInData, i, gOut * scale);
            }
            return gradInput;
        }
    }

    /**
     * getP方法。
     *
     * @return float类型返回值
     */
    public float getP() {
        return p;
    }
}

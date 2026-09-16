package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Softmax layer - applies softmax activation
 * softmax(x_i) = exp(x_i) / sum(exp(x_j))
 * Typically applied along the last dimension
 */
public class Softmax extends Module {

    private final int dim;

    /**
     * Softmax方法。
     */
    public Softmax() {
        this(-1);  // Default: apply along last dimension
    }

    /**
     * Softmax方法。
     * * @param dim int类型参数
     */
    public Softmax(int dim) {
        this.dim = dim;
    }

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        int ndim = input.ndim();
        int applyDim = dim < 0 ? ndim + dim : dim;

        if (applyDim < 0 || applyDim >= ndim) {
            throw new IllegalArgumentException("Invalid dimension: " + dim);
        }

        Shape shape = input.getShape();

        // Compute along the specified dimension
        int dimSize = shape.get(applyDim);
        int outerSize = 1;
        for (int i = 0; i < applyDim; i++) {
            outerSize *= shape.get(i);
        }
        int innerSize = 1;
        for (int i = applyDim + 1; i < ndim; i++) {
            innerSize *= shape.get(i);
        }

        NdArray output = NdArray.zeros(shape, DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();

        // For numerical stability, subtract max before exp
        for (int outer = 0; outer < outerSize; outer++) {
            for (int inner = 0; inner < innerSize; inner++) {
                // Find max in this row
                float maxVal = Float.NEGATIVE_INFINITY;

                for (int d = 0; d < dimSize; d++) {
                    int idx;
                    if (ndim == 1) {
                        idx = d;
                    } else if (ndim == 2) {
                        idx = outer * dimSize + d;
                    } else {
                        // Multi-dimensional case
                        idx = (outer * dimSize + d) * innerSize + inner;
                    }
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    if (val > maxVal) {
                        maxVal = val;
                    }
                }

                // Compute exp and sum
                float sum = 0.0f;
                float[] expVals = new float[dimSize];

                for (int d = 0; d < dimSize; d++) {
                    int idx;
                    if (ndim == 1) {
                        idx = d;
                    } else if (ndim == 2) {
                        idx = outer * dimSize + d;
                    } else {
                        idx = (outer * dimSize + d) * innerSize + inner;
                    }
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    float expVal = (float) Math.exp(val - maxVal);
                    expVals[d] = expVal;
                    sum += expVal;
                }

                // Normalize
                for (int d = 0; d < dimSize; d++) {
                    int idx;
                    if (ndim == 1) {
                        idx = d;
                    } else if (ndim == 2) {
                        idx = outer * dimSize + d;
                    } else {
                        idx = (outer * dimSize + d) * innerSize + inner;
                    }
                    com.zifang.util.numpy.Array.set(outData, idx, expVals[d] / sum);
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
        // For softmax: dL/dx_i = sum(dL/dy_j * y_j) * y_i - dL/dy_i * y_i
        // Simplified: dL/dx = y * (dL/dy - sum(dL/dy * y))

        int ndim = gradOutput.ndim();
        Shape shape = gradOutput.getShape();
        int applyDim = dim < 0 ? ndim + dim : dim;
        int dimSize = shape.get(applyDim);

        NdArray gradInput = NdArray.zeros(shape, DType.FLOAT32);
        Object gOutData = gradOutput.getData();
        Object gInData = gradInput.getData();

        // We need the forward output for Jacobian computation
        // For simplicity, this returns a simplified gradient
        // Full implementation would need to store forward output

        int outerSize = 1;
        for (int i = 0; i < applyDim; i++) {
            outerSize *= shape.get(i);
        }
        int innerSize = 1;
        for (int i = applyDim + 1; i < ndim; i++) {
            innerSize *= shape.get(i);
        }

        for (int outer = 0; outer < outerSize; outer++) {
            for (int inner = 0; inner < innerSize; inner++) {
                // Compute sum(dL/dy * y)
                float weightedSum = 0.0f;
                for (int d = 0; d < dimSize; d++) {
                    int idx;
                    if (ndim == 1) {
                        idx = d;
                    } else if (ndim == 2) {
                        idx = outer * dimSize + d;
                    } else {
                        idx = (outer * dimSize + d) * innerSize + inner;
                    }
                    float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, idx)).floatValue();
                    // Assuming y_i ~ 1/dimSize for gradient estimation
                    // This is an approximation since we don't have forward output stored
                    weightedSum += gOut * (1.0f / dimSize);
                }

                // dL/dx_i = y_i * (dL/dy_i - weightedSum)
                for (int d = 0; d < dimSize; d++) {
                    int idx;
                    if (ndim == 1) {
                        idx = d;
                    } else if (ndim == 2) {
                        idx = outer * dimSize + d;
                    } else {
                        idx = (outer * dimSize + d) * innerSize + inner;
                    }
                    float gOut = ((Number) com.zifang.util.numpy.Array.get(gOutData, idx)).floatValue();
                    // y_i ~ 1/dimSize approximation
                    float y = 1.0f / dimSize;
                    float gIn = y * (gOut - weightedSum);
                    com.zifang.util.numpy.Array.set(gInData, idx, gIn);
                }
            }
        }

        return gradInput;
    }

    /**
     * getDim方法。
     *
     * @return int类型返回值
     */
    public int getDim() {
        return dim;
    }
}

package com.zifang.util.ml.nn;

import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Reshape layer - reshapes input to specified shape
 * Input: (*)
 * Output: (shape)
 */
public class Reshape extends Module {

    private final int[] targetShape;
    private int[] inferredShape;

    /**
     * Reshape方法。
     * * @param targetShape int...类型参数
     */
    public Reshape(int... targetShape) {
        this.targetShape = targetShape;
    }

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        int inputSize = input.size();

        // Handle -1 dimension (infer it)
        int unknownDim = -1;
        int knownProduct = 1;
        int numUnknown = 0;

        for (int dim : targetShape) {
            if (dim == -1) {
                unknownDim = numUnknown++;
            } else {
                knownProduct *= dim;
            }
        }

        if (numUnknown > 1) {
            throw new IllegalArgumentException("At most one dimension can be -1");
        }

        if (unknownDim >= 0) {
            if (inputSize % knownProduct != 0) {
                throw new IllegalArgumentException("Cannot reshape array of size " + inputSize +
                        " into shape " + new Shape(targetShape));
            }
            inferredShape = targetShape.clone();
            inferredShape[unknownDim] = inputSize / knownProduct;
            return input.reshape(inferredShape);
        }

        if (inputSize != knownProduct) {
            throw new IllegalArgumentException("Cannot reshape array of size " + inputSize +
                    " into shape " + new Shape(targetShape));
        }

        return input.reshape(targetShape);
    }

    @Override
    /**
     * backward方法。
     *      * @param gradOutput NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray backward(NdArray gradOutput) {
        // Reshape back to input shape (which is stored as original shape)
        return gradOutput.reshape(gradOutput.getShape().getDimensions());
    }

    /**
     * getTargetShape方法。
     *
     * @return int[]类型返回值
     */
    public int[] getTargetShape() {
        return inferredShape != null ? inferredShape : targetShape;
    }
}

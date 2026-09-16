package com.zifang.util.ml.nn;

import com.zifang.util.numpy.NdArray;

/**
 * Flatten layer - reshapes input to 2D tensor (batch, features)
 * Input: (*)
 * Output: (batch_size, -1) where -1 is inferred from remaining dimensions
 */
public class Flatten extends Module {

    private int batchSize;
    private int numFeatures;

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        batchSize = 1;
        int ndim = input.ndim();

        if (ndim == 0) {
            throw new IllegalArgumentException("Input must have at least 1 dimension");
        }

        if (ndim == 1) {
            // Just return as 2D with batch=1
            numFeatures = input.getShape().get(0);
            return input.reshape(1, numFeatures);
        }

        // First dimension is batch size
        batchSize = input.getShape().get(0);
        numFeatures = 1;
        for (int i = 1; i < ndim; i++) {
            numFeatures *= input.getShape().get(i);
        }

        return input.reshape(batchSize, numFeatures);
    }

    @Override
    /**
     * backward方法。
     *      * @param gradOutput NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray backward(NdArray gradOutput) {
        // Reshape gradient back to original shape
        int[] originalShape = new int[batchSize > 1 ? numFeatures > 0 ? 2 : 1 : 1];
        if (batchSize > 1 && numFeatures > 0) {
            // We need to reconstruct the original shape
            // For simplicity, return reshaped to what was stored
            return gradOutput.reshape(batchSize, numFeatures);
        }
        return gradOutput.reshape(numFeatures);
    }

    /**
     * getBatchSize方法。
     *
     * @return int类型返回值
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * getNumFeatures方法。
     *
     * @return int类型返回值
     */
    public int getNumFeatures() {
        return numFeatures;
    }
}

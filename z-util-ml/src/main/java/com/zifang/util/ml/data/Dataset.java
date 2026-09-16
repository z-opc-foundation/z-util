package com.zifang.util.ml.data;

import com.zifang.util.numpy.NdArray;

/**
 * Dataset interface for machine learning data handling.
 * <p>
 * Represents a dataset that can be iterated over to get samples.
 */
public interface Dataset {

    /**
     * Get the number of samples in the dataset.
     *
     * @return Number of samples
     */
    int size();

    /**
     * Get a sample from the dataset.
     *
     * @param index Sample index
     * @return Array of NdArrays representing the sample (e.g., [features, labels])
     */
    NdArray[] get(int index);

    /**
     * Get the shape of features.
     *
     * @return Shape of a single feature tensor
     */
    int[] getFeatureShape();

    /**
     * Get the shape of labels.
     *
     * @return Shape of a single label tensor
     */
    int[] getLabelShape();
}

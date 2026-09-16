package com.zifang.util.ml.data;

import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Slice;

import java.util.ArrayList;
import java.util.List;

/**
 * Dataset wrapper around a list of NdArray tensors.
 * <p>
 * Each sample consists of one or more tensors (e.g., features and labels).
 * This provides a convenient way to create datasets from existing NdArray data.
 */
public class TensorDataset implements Dataset {

    private final List<NdArray[]> samples;
    private final int[] featureShape;
    private final int[] labelShape;

    /**
     * Create a TensorDataset from features and labels.
     *
     * @param features Features tensor of shape (n_samples, ...)
     * @param labels   Labels tensor of shape (n_samples, ...)
     */
    public TensorDataset(NdArray features, NdArray labels) {
        this.samples = new ArrayList<>();
        int[] fullFeatureShape = features.getShape().getDimensions();
        int[] fullLabelShape = labels.getShape().getDimensions();
        this.featureShape = computePerSampleShape(fullFeatureShape);
        this.labelShape = computePerSampleShape(fullLabelShape);

        int nSamples = features.getShape().get(0);
        for (int i = 0; i < nSamples; i++) {
            NdArray[] sample = new NdArray[2];
            sample[0] = features.slice(Slice.slice(i, i + 1));
            sample[1] = labels.slice(Slice.slice(i, i + 1));
            samples.add(sample);
        }
    }

    /**
     * Create a TensorDataset from a list of (features, labels) pairs.
     *
     * @param data List of NdArray pairs where each pair is [features, labels]
     */
    public TensorDataset(List<NdArray[]> data) {
        this.samples = new ArrayList<>(data);

        if (!data.isEmpty()) {
            NdArray[] first = data.get(0);
            int[] featDims = first[0].getShape().getDimensions();
            int[] labelDims = first[1].getShape().getDimensions();
            this.featureShape = computePerSampleShape(featDims);
            this.labelShape = computePerSampleShape(labelDims);
        } else {
            this.featureShape = new int[]{};
            this.labelShape = new int[]{};
        }
    }

    /**
     * Get the number of samples in the dataset.
     *
     * @return Number of samples
     */
    @Override
    /**
     * size方法。
     * @return int类型返回值
     */
    public int size() {
        return samples.size();
    }

    /**
     * Get a sample from the dataset.
     *
     * @param index Sample index
     * @return Array of NdArrays representing [features, labels]
     */
    @Override
    /**
     * get方法。
     *      * @param index int类型参数
     * @return NdArray[]类型返回值
     */
    public NdArray[] get(int index) {
        if (index < 0 || index >= samples.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + samples.size());
        }
        return samples.get(index);
    }

    /**
     * Compute per-sample shape by removing the batch dimension (first element).
     * If shape is (nSamples, ...), returns (...).
     * If shape is just (nSamples,) or (), returns empty int[].
     */
    private int[] computePerSampleShape(int[] shape) {
        if (shape.length > 1) {
            int[] perSample = new int[shape.length - 1];
            System.arraycopy(shape, 1, perSample, 0, shape.length - 1);
            return perSample;
        }
        return new int[]{};
    }

    /**
     * Get the shape of features.
     *
     * @return Shape of a single feature tensor
     */
    @Override
    /**
     * getFeatureShape方法。
     * @return int[]类型返回值
     */
    public int[] getFeatureShape() {
        return featureShape;
    }

    /**
     * Get the shape of labels.
     *
     * @return Shape of a single label tensor
     */
    @Override
    /**
     * getLabelShape方法。
     * @return int[]类型返回值
     */
    public int[] getLabelShape() {
        return labelShape;
    }

    /**
     * Get all samples as a list.
     *
     * @return List of all samples
     */
    public List<NdArray[]> getAllSamples() {
        return new ArrayList<>(samples);
    }
}

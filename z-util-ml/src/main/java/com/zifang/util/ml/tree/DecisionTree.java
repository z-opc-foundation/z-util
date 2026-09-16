package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * CART Decision Tree for Classification
 * Uses Gini impurity for split selection
 * Supports numerical features with threshold-based splits
 */
public class DecisionTree {
    private int maxDepth;
    private int minSamplesSplit;
    private int minSamplesLeaf;
    private TreeNode root;
    private int nClasses;
    private Random random;

    /**
     * DecisionTree方法。
     * * @param maxDepth int类型参数
     *
     * @param minSamplesSplit int类型参数
     * @param minSamplesLeaf  int类型参数
     */
    public DecisionTree(int maxDepth, int minSamplesSplit, int minSamplesLeaf) {
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.minSamplesLeaf = minSamplesLeaf;
        this.random = new Random();
    }

    /**
     * fit方法。
     * * @param X NdArray类型参数
     *
     * @param y int[]类型参数
     */
    public void fit(NdArray X, int[] y) {
        this.nClasses = findMaxValue(y) + 1;
        double[][] Xdata = toDouble2D(X);
        this.root = buildTree(Xdata, y, 0);
    }

    /**
     * predict方法。
     * * @param X NdArray类型参数
     *
     * @return int[]类型返回值
     */
    public int[] predict(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int[] predictions = new int[Xdata.length];
        for (int i = 0; i < Xdata.length; i++) {
            predictions[i] = predictSingle(Xdata[i], root);
        }
        return predictions;
    }

    /**
     * predictProba方法。
     * * @param X NdArray类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray predictProba(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        double[][] proba = new double[Xdata.length][nClasses];
        for (int i = 0; i < Xdata.length; i++) {
            double[] dist = predictProbaSingle(Xdata[i], root);
            proba[i] = dist;
        }
        return createNdArray(proba, Xdata.length, nClasses);
    }

    private double[] predictProbaSingle(double[] x, TreeNode node) {
        if (node.isLeaf) {
            return node.classDistribution.clone();
        }
        if (x[node.featureIndex] <= node.threshold) {
            return predictProbaSingle(x, node.left);
        } else {
            return predictProbaSingle(x, node.right);
        }
    }

    private int predictSingle(double[] x, TreeNode node) {
        if (node.isLeaf) {
            return node.predictedClass;
        }
        if (x[node.featureIndex] <= node.threshold) {
            return predictSingle(x, node.left);
        } else {
            return predictSingle(x, node.right);
        }
    }

    private TreeNode buildTree(double[][] X, int[] y, int depth) {
        int nSamples = X.length;
        int nFeatures = X[0].length;

        TreeNode node = new TreeNode();

        // Check stopping conditions
        if (depth >= maxDepth || nSamples < minSamplesSplit || nSamples < 2 * minSamplesLeaf) {
            node.isLeaf = true;
            node.predictedClass = findMostCommonClass(y);
            node.classDistribution = computeClassDistribution(y);
            return node;
        }

        // Find best split
        double[] gini;
        int bestFeature = -1;
        double bestThreshold = 0;
        double bestGini = Double.MAX_VALUE;

        // Try each feature
        for (int featureIdx = 0; featureIdx < nFeatures; featureIdx++) {
            // Get unique values and sort
            double[] values = new double[nSamples];
            for (int i = 0; i < nSamples; i++) {
                values[i] = X[i][featureIdx];
            }

            // Try splits at midpoints between consecutive unique sorted values
            double[] sortedValues = values.clone();
            java.util.Arrays.sort(sortedValues);

            for (int i = 0; i < nSamples - 1; i++) {
                double threshold = (sortedValues[i] + sortedValues[i + 1]) / 2.0;

                // Skip if threshold doesn't create valid split
                int[] leftIndices = new int[nSamples];
                int[] rightIndices = new int[nSamples];
                int leftCount = 0, rightCount = 0;

                for (int j = 0; j < nSamples; j++) {
                    if (X[j][featureIdx] <= threshold) {
                        leftIndices[leftCount++] = j;
                    } else {
                        rightIndices[rightCount++] = j;
                    }
                }

                if (leftCount < minSamplesLeaf || rightCount < minSamplesLeaf) {
                    continue;
                }

                // Compute Gini impurity for this split
                double giniLeft = computeGini(y, leftIndices, leftCount);
                double giniRight = computeGini(y, rightIndices, rightCount);
                double weightedGini = (leftCount * giniLeft + rightCount * giniRight) / nSamples;

                if (weightedGini < bestGini) {
                    bestGini = weightedGini;
                    bestFeature = featureIdx;
                    bestThreshold = threshold;
                }
            }
        }

        // If no valid split found, make leaf
        if (bestFeature == -1) {
            node.isLeaf = true;
            node.predictedClass = findMostCommonClass(y);
            node.classDistribution = computeClassDistribution(y);
            return node;
        }

        // Partition data
        int[] leftIndices = new int[nSamples];
        int[] rightIndices = new int[nSamples];
        int leftCount = 0, rightCount = 0;

        for (int i = 0; i < nSamples; i++) {
            if (X[i][bestFeature] <= bestThreshold) {
                leftIndices[leftCount++] = i;
            } else {
                rightIndices[rightCount++] = i;
            }
        }

        // Create child nodes
        double[][] XLeft = new double[leftCount][nFeatures];
        int[] yLeft = new int[leftCount];
        for (int i = 0; i < leftCount; i++) {
            XLeft[i] = X[leftIndices[i]];
            yLeft[i] = y[leftIndices[i]];
        }

        double[][] XRight = new double[rightCount][nFeatures];
        int[] yRight = new int[rightCount];
        for (int i = 0; i < rightCount; i++) {
            XRight[i] = X[rightIndices[i]];
            yRight[i] = y[rightIndices[i]];
        }

        node.featureIndex = bestFeature;
        node.threshold = bestThreshold;
        node.left = buildTree(XLeft, yLeft, depth + 1);
        node.right = buildTree(XRight, yRight, depth + 1);

        return node;
    }

    private double computeGini(int[] y, int[] indices, int count) {
        if (count == 0) return 0.0;

        int[] classCounts = new int[nClasses];
        for (int i = 0; i < count; i++) {
            classCounts[y[indices[i]]]++;
        }

        double gini = 1.0;
        for (int c = 0; c < nClasses; c++) {
            double p = (double) classCounts[c] / count;
            gini -= p * p;
        }
        return gini;
    }

    private double[] computeClassDistribution(int[] y) {
        double[] dist = new double[nClasses];
        for (int label : y) {
            dist[label]++;
        }
        for (int i = 0; i < nClasses; i++) {
            dist[i] /= y.length;
        }
        return dist;
    }

    private int findMostCommonClass(int[] y) {
        int[] counts = new int[nClasses];
        for (int label : y) {
            counts[label]++;
        }
        int maxCount = 0;
        int maxClass = 0;
        for (int i = 0; i < nClasses; i++) {
            if (counts[i] > maxCount) {
                maxCount = counts[i];
                maxClass = i;
            }
        }
        return maxClass;
    }

    private int findMaxValue(int[] arr) {
        int max = arr[0];
        for (int val : arr) {
            if (val > max) max = val;
        }
        return max;
    }

    private double[][] toDouble2D(NdArray arr) {
        Object data = arr.getData();
        int nRows = arr.getShape().get(0);
        int nCols = arr.getShape().get(1);

        double[][] result = new double[nRows][nCols];

        if (data instanceof double[][]) {
            double[][] d2 = (double[][]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = d2[i][j];
                }
            }
        } else if (data instanceof double[]) {
            double[] d1 = (double[]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = d1[i * nCols + j];
                }
            }
        } else if (data instanceof float[][]) {
            float[][] f2 = (float[][]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = f2[i][j];
                }
            }
        } else if (data instanceof float[]) {
            float[] f1 = (float[]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = f1[i * nCols + j];
                }
            }
        } else {
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = ((Number) arr.get(i, j)).doubleValue();
                }
            }
        }

        return result;
    }

    private NdArray createNdArray(double[][] data, int rows, int cols) {
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = data[i][j];
            }
        }
        return NdArray.array(flat, DType.FLOAT64).reshape(rows, cols);
    }

    /**
     * getRoot方法。
     *
     * @return TreeNode类型返回值
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * getNClasses方法。
     *
     * @return int类型返回值
     */
    public int getNClasses() {
        return nClasses;
    }

    /**
     * Inner class representing a node in the decision tree
     */
    public static class TreeNode {
        public boolean isLeaf;
        public int predictedClass;
        public TreeNode left;
        public TreeNode right;
        public int featureIndex;
        public double threshold;
        public double[] classDistribution;

        /**
         * TreeNode方法。
         */
        public TreeNode() {
            this.isLeaf = false;
            this.left = null;
            this.right = null;
            this.featureIndex = -1;
            this.threshold = 0.0;
            this.classDistribution = null;
        }
    }
}

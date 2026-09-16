package com.zifang.util.ml.anomaly;

import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * Isolation Forest - anomaly detection using ensemble of random trees.
 * <p>
 * Algorithm:
 * 1. Build nEstimators isolation trees (random binary trees)
 * 2. Each tree: randomly select maxSamples from X, recursively split on random feature/threshold until single point or maxDepth
 * 3. Anomaly score for point x: s(x, n) = 2^(-E[h(x)] / c(n)) where h(x) is path length, c(n) is average path length of unsuccessful search in BST
 * 4. Points with s > threshold are anomalies
 */
public class IsolationForest {

    private int nEstimators;
    private int maxSamples;
    private double contamination;
    private double threshold;
    private IsolationTree[] trees;
    private Random random;
    private int maxDepth;

    /**
     * Create a new IsolationForest.
     *
     * @param nEstimators   Number of isolation trees in the forest
     * @param maxSamples    Maximum samples to use for building each tree
     * @param contamination Expected proportion of outliers (0.0 to 0.5)
     */
    public IsolationForest(int nEstimators, int maxSamples, double contamination) {
        this.nEstimators = nEstimators;
        this.maxSamples = maxSamples;
        this.contamination = contamination;
        this.random = new Random();
        this.maxDepth = (int) Math.ceil(Math.log(maxSamples) / Math.log(2));
        this.trees = new IsolationTree[nEstimators];
    }

    /**
     * Fit the IsolationForest to the training data.
     */
    public void fit(NdArray X) {
        int nSamples = X.getShape().get(0);
        int nFeatures = X.getShape().get(1);
        double[][] Xdata = toDouble2D(X);

        // Adjust maxSamples if necessary
        int actualMaxSamples = Math.min(maxSamples, nSamples);

        // Calculate threshold based on contamination
        // For anomaly score, lower score = more normal, higher score = more anomalous
        // We use the quantile approach: threshold = 1 - contamination
        this.threshold = calculateThreshold(actualMaxSamples);

        // Build isolation trees
        for (int i = 0; i < nEstimators; i++) {
            trees[i] = buildIsolationTree(Xdata, nSamples, nFeatures, actualMaxSamples);
        }
    }

    /**
     * Calculate the anomaly score threshold based on contamination.
     */
    private double calculateThreshold(int nSamples) {
        // c(n) is the average path length of unsuccessful search in BST
        double c = calculateAveragePathLength(nSamples);
        // The threshold for anomaly score s = 2^(-1/c) corresponds to expected contamination
        return Math.pow(2, -1.0 / c) * (1.0 + 0.01);
    }

    /**
     * Build a single isolation tree.
     */
    private IsolationTree buildIsolationTree(double[][] X, int nSamples, int nFeatures, int maxSamples) {
        // Randomly select samples for this tree
        int[] sampleIndices = new int[maxSamples];
        for (int i = 0; i < maxSamples; i++) {
            sampleIndices[i] = random.nextInt(nSamples);
        }

        // Create tree with estimated max nodes
        int maxNodes = maxSamples * 2;
        IsolationTree tree = new IsolationTree(maxNodes);

        // Build tree recursively
        tree.rootIndex = buildTreeRecursive(X, sampleIndices, 0, maxSamples, nFeatures, tree);

        return tree;
    }

    /**
     * Recursively build the isolation tree.
     */
    private int buildTreeRecursive(double[][] X, int[] indices, int depth, int size, int nFeatures, IsolationTree tree) {
        if (size <= 1 || depth >= maxDepth) {
            int nodeIdx = tree.addNode();
            if (nodeIdx >= 0) {
                tree.nodes[nodeIdx].isLeaf = true;
                tree.nodes[nodeIdx].size = size;
            }
            return nodeIdx;
        }

        // Choose random feature
        int featureIdx = random.nextInt(nFeatures);

        // Find min and max for this feature among samples
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MAX_VALUE;
        double[] featureValues = new double[size];

        for (int i = 0; i < size; i++) {
            double val = X[indices[i]][featureIdx];
            featureValues[i] = val;
            if (val < minVal) minVal = val;
            if (val > maxVal) maxVal = val;
        }

        // If min equals max, make this a leaf
        if (minVal >= maxVal) {
            int nodeIdx = tree.addNode();
            if (nodeIdx >= 0) {
                tree.nodes[nodeIdx].isLeaf = true;
                tree.nodes[nodeIdx].size = size;
            }
            return nodeIdx;
        }

        // Choose random threshold between min and max
        double threshold = minVal + random.nextDouble() * (maxVal - minVal);

        // Partition samples
        int leftSize = 0;
        int rightSize = 0;
        int[] leftIndices = new int[size];
        int[] rightIndices = new int[size];

        for (int i = 0; i < size; i++) {
            if (X[indices[i]][featureIdx] <= threshold) {
                leftIndices[leftSize++] = indices[i];
            } else {
                rightIndices[rightSize++] = indices[i];
            }
        }

        // Create node
        int nodeIdx = tree.addNode();
        if (nodeIdx < 0) {
            return -1;
        }

        tree.nodes[nodeIdx].featureIndex = featureIdx;
        tree.nodes[nodeIdx].threshold = threshold;
        tree.nodes[nodeIdx].size = size;
        tree.nodes[nodeIdx].isLeaf = false;

        // Build children recursively
        tree.nodes[nodeIdx].leftIndex = buildTreeRecursive(X, leftIndices, depth + 1, leftSize, nFeatures, tree);
        tree.nodes[nodeIdx].rightIndex = buildTreeRecursive(X, rightIndices, depth + 1, rightSize, nFeatures, tree);

        return nodeIdx;
    }

    /**
     * Calculate the average path length of unsuccessful search in BST.
     * c(n) = 2 * (ln(n-1) + gamma) - 2 * (n-1)/n where gamma ≈ 0.5772156649
     */
    private double calculateAveragePathLength(int n) {
        if (n <= 1) {
            return 0;
        }
        double gamma = 0.5772156649;
        return 2.0 * (Math.log(n - 1) + gamma) - 2.0 * (n - 1) / n;
    }

    /**
     * Calculate anomaly score for a single point.
     */
    private double scorePoint(double[] point, int nSamples) {
        double[] pathLengths = new double[nEstimators];

        for (int i = 0; i < nEstimators; i++) {
            pathLengths[i] = trees[i].pathLength(point, trees[i].rootIndex, null, null, 0);
        }

        // Calculate average path length
        double avgPathLength = 0;
        for (int i = 0; i < nEstimators; i++) {
            avgPathLength += pathLengths[i];
        }
        avgPathLength /= nEstimators;

        // Calculate anomaly score: s(x, n) = 2^(-E[h(x)] / c(n))
        double c = calculateAveragePathLength(nSamples);
        double score = Math.pow(2, -avgPathLength / c);

        return score;
    }

    /**
     * Predict anomalies for input data.
     *
     * @param X Input data (nSamples x nFeatures)
     * @return Array of predictions: 1 for normal, -1 for anomaly
     */
    public int[] predict(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;

        double[] scores = score(X);
        int[] predictions = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            predictions[i] = scores[i] > threshold ? -1 : 1;
        }

        return predictions;
    }

    /**
     * Calculate anomaly scores for input data.
     *
     * @param X Input data (nSamples x nFeatures)
     * @return Array of anomaly scores (higher = more anomalous)
     */
    public double[] score(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;
        double[] scores = new double[nSamples];

        int trainingSize = maxSamples;
        for (int i = 0; i < nSamples; i++) {
            scores[i] = scorePoint(Xdata[i], trainingSize);
        }

        return scores;
    }

    /**
     * Convert NdArray to 2D double array.
     */
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

    /**
     * Inner class representing an isolation tree node
     */
    private static class IsolationTreeNode {
        public boolean isLeaf;
        public int leftIndex;
        public int rightIndex;
        public int featureIndex;
        public double threshold;
        public int size;

        /**
         * IsolationTreeNode方法。
         */
        public IsolationTreeNode() {
            this.isLeaf = false;
            this.leftIndex = -1;
            this.rightIndex = -1;
            this.featureIndex = -1;
            this.threshold = 0.0;
            this.size = 0;
        }
    }

    /**
     * Inner class representing an isolation tree
     */
    private static class IsolationTree {
        public IsolationTreeNode[] nodes;
        public int rootIndex;
        public int nodeCount;

        /**
         * IsolationTree方法。
         * * @param maxNodes int类型参数
         */
        public IsolationTree(int maxNodes) {
            this.nodes = new IsolationTreeNode[maxNodes];
            this.rootIndex = -1;
            this.nodeCount = 0;
        }

        /**
         * addNode方法。
         *
         * @return int类型返回值
         */
        public int addNode() {
            if (nodeCount >= nodes.length) {
                return -1;
            }
            nodes[nodeCount] = new IsolationTreeNode();
            return nodeCount++;
        }

        /**
         * pathLength方法。
         * * @param point double[]类型参数
         *
         * @param nodeIndex     int类型参数
         * @param featureValues double[]类型参数
         * @param sampleIndices int[]类型参数
         * @param currentSize   int类型参数
         * @return int类型返回值
         */
        public int pathLength(double[] point, int nodeIndex, double[] featureValues, int[] sampleIndices, int currentSize) {
            if (nodeIndex < 0 || nodeIndex >= nodeCount || nodes[nodeIndex] == null) {
                return 0;
            }

            IsolationTreeNode node = nodes[nodeIndex];

            if (node.isLeaf || node.size <= 1) {
                return node.size <= 1 ? 0 : 1;
            }

            int featureIdx = node.featureIndex;
            double threshold = node.threshold;
            double pointVal = point[featureIdx];

            if (pointVal <= threshold) {
                return 1 + pathLength(point, node.leftIndex, featureValues, sampleIndices, currentSize);
            } else {
                return 1 + pathLength(point, node.rightIndex, featureValues, sampleIndices, currentSize);
            }
        }
    }
}

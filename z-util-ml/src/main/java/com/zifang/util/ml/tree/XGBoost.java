package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * Gradient Boosting (simplified XGBoost-style)
 * Uses gradient descent for boosting with second-order gradient information
 */
public class XGBoost {
    private int nEstimators;
    private double learningRate;
    private int maxDepth;
    private int minChildWeight;
    private double subsample;
    private double colsampleBytree;
    private double lambda;
    private double alpha;
    private RegressionTree[] trees;
    private double[] weights;
    private int nClasses;
    private int nFeatures;
    private Random random;

    /**
     * XGBoost方法。
     * * @param nEstimators int类型参数
     *
     * @param learningRate    double类型参数
     * @param maxDepth        int类型参数
     * @param minChildWeight  int类型参数
     * @param subsample       double类型参数
     * @param colsampleBytree double类型参数
     * @param lambda          double类型参数
     * @param alpha           double类型参数
     */
    public XGBoost(int nEstimators, double learningRate, int maxDepth, int minChildWeight,
                   double subsample, double colsampleBytree, double lambda, double alpha) {
        this.nEstimators = nEstimators;
        this.learningRate = learningRate;
        this.maxDepth = maxDepth;
        this.minChildWeight = minChildWeight;
        this.subsample = subsample;
        this.colsampleBytree = colsampleBytree;
        this.lambda = lambda;
        this.alpha = alpha;
        this.random = new Random();
    }

    /**
     * fit方法。
     * * @param X NdArray类型参数
     *
     * @param y int[]类型参数
     */
    public void fit(NdArray X, int[] y) {
        int nSamples = X.getShape().get(0);
        this.nFeatures = X.getShape().get(1);

        // Find number of classes
        this.nClasses = findMaxValue(y) + 1;

        double[][] Xdata = toDouble2D(X);

        // Convert labels to one-hot encoding for multi-class
        double[][] yOneHot = new double[nSamples][nClasses];
        for (int i = 0; i < nSamples; i++) {
            yOneHot[i][y[i]] = 1.0;
        }

        this.trees = new RegressionTree[nEstimators * nClasses];
        this.weights = new double[nClasses];

        // Initialize predictions to zero
        double[][] f = new double[nSamples][nClasses];
        for (int c = 0; c < nClasses; c++) {
            weights[c] = 0.0;
        }

        // Train each class as binary classification problem
        for (int c = 0; c < nClasses; c++) {
            double[] yBinary = new double[nSamples];
            for (int i = 0; i < nSamples; i++) {
                yBinary[i] = yOneHot[i][c];
            }

            for (int iter = 0; iter < nEstimators; iter++) {
                // Compute gradients (pseudo-residuals)
                double[] gradients = new double[nSamples];
                double[] hessians = new double[nSamples];

                for (int i = 0; i < nSamples; i++) {
                    double pred = sigmoid(f[i][c]);
                    double label = yBinary[i];
                    gradients[i] = pred - label;  // First derivative
                    hessians[i] = pred * (1 - pred) + lambda;  // Second derivative with L2 reg
                }

                // Subsample data
                int sampleSize = (int) (nSamples * subsample);
                int[] sampleIndices = subsampleIndices(nSamples, sampleSize);

                double[][] XSample = new double[sampleSize][nFeatures];
                double[] ySample = new double[sampleSize];
                double[] gradSample = new double[sampleSize];
                double[] hessSample = new double[sampleSize];

                for (int i = 0; i < sampleSize; i++) {
                    XSample[i] = Xdata[sampleIndices[i]];
                    ySample[i] = yBinary[sampleIndices[i]];
                    gradSample[i] = gradients[sampleIndices[i]];
                    hessSample[i] = hessians[sampleIndices[i]];
                }

                // Select random features
                int nSelectedFeatures = Math.max(1, (int) (nFeatures * colsampleBytree));
                int[] featureIndices = selectRandomFeatures(nFeatures, nSelectedFeatures);

                // Fit regression tree on scaled gradients
                double[] scaledGradients = new double[sampleSize];
                for (int i = 0; i < sampleSize; i++) {
                    scaledGradients[i] = gradSample[i] / (hessSample[i] + 1e-6);
                }

                RegressionTree tree = new RegressionTree(maxDepth, minChildWeight, nFeatures);
                tree.fit(XSample, scaledGradients, featureIndices);

                // Update predictions
                double[] treePredictions = tree.predict(Xdata);
                for (int i = 0; i < nSamples; i++) {
                    f[i][c] += learningRate * treePredictions[i];
                }

                trees[c * nEstimators + iter] = tree;
            }
        }
    }

    /**
     * predict方法。
     * * @param X NdArray类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray predict(NdArray X) {
        int nSamples = X.getShape().get(0);
        double[][] Xdata = toDouble2D(X);

        double[][] logits = new double[nSamples][nClasses];

        for (int c = 0; c < nClasses; c++) {
            for (int iter = 0; iter < nEstimators; iter++) {
                RegressionTree tree = trees[c * nEstimators + iter];
                if (tree != null) {
                    double[] predictions = tree.predict(Xdata);
                    for (int i = 0; i < nSamples; i++) {
                        logits[i][c] += learningRate * predictions[i];
                    }
                }
            }
        }

        // Return raw logits as NdArray
        return createNdArray(logits, nSamples, nClasses);
    }

    /**
     * predictClass方法。
     * * @param X NdArray类型参数
     *
     * @return int[]类型返回值
     */
    public int[] predictClass(NdArray X) {
        int nSamples = X.getShape().get(0);
        NdArray logits = predict(X);
        double[][] logitsData = toDouble2D(logits);

        int[] predictions = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            int bestClass = 0;
            double maxLogit = logitsData[i][0];
            for (int c = 1; c < nClasses; c++) {
                if (logitsData[i][c] > maxLogit) {
                    maxLogit = logitsData[i][c];
                    bestClass = c;
                }
            }
            predictions[i] = bestClass;
        }
        return predictions;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-Math.max(-500, Math.min(500, x))));
    }

    private int[] subsampleIndices(int n, int size) {
        int[] indices = new int[size];
        for (int i = 0; i < size; i++) {
            indices[i] = random.nextInt(n);
        }
        return indices;
    }

    private int[] selectRandomFeatures(int nFeatures, int nSelect) {
        int[] indices = new int[nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            indices[i] = i;
        }
        // Fisher-Yates shuffle
        for (int i = nFeatures - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;
        }
        int[] result = new int[nSelect];
        for (int i = 0; i < nSelect; i++) {
            result[i] = indices[i];
        }
        return result;
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
     * Regression tree for XGBoost base learner
     */
    public static class RegressionTree {
        private int maxDepth;
        private int minChildWeight;
        private int nFeatures;
        private TreeNode root;
        private Random random;

        /**
         * RegressionTree方法。
         * * @param maxDepth int类型参数
         *
         * @param minChildWeight int类型参数
         * @param nFeatures      int类型参数
         */
        public RegressionTree(int maxDepth, int minChildWeight, int nFeatures) {
            this.maxDepth = maxDepth;
            this.minChildWeight = minChildWeight;
            this.nFeatures = nFeatures;
            this.random = new Random();
        }

        /**
         * fit方法。
         * * @param X double[][]类型参数
         *
         * @param y              double[]类型参数
         * @param featureIndices int[]类型参数
         */
        public void fit(double[][] X, double[] y, int[] featureIndices) {
            this.root = buildTree(X, y, featureIndices, 0);
        }

        /**
         * predict方法。
         * * @param X double[][]类型参数
         *
         * @return double[]类型返回值
         */
        public double[] predict(double[][] X) {
            double[] predictions = new double[X.length];
            for (int i = 0; i < X.length; i++) {
                predictions[i] = predictSingle(X[i], root);
            }
            return predictions;
        }

        private double predictSingle(double[] x, TreeNode node) {
            if (node.isLeaf) {
                return node.value;
            }
            if (x[node.featureIndex] <= node.threshold) {
                return predictSingle(x, node.left);
            } else {
                return predictSingle(x, node.right);
            }
        }

        private TreeNode buildTree(double[][] X, double[] y, int[] featureIndices, int depth) {
            int nSamples = X.length;

            TreeNode node = new TreeNode();

            if (depth >= maxDepth || nSamples < minChildWeight) {
                node.isLeaf = true;
                node.value = computeMean(y);
                return node;
            }

            // Find best split using variance reduction
            double bestGain = 0;
            int bestFeature = -1;
            double bestThreshold = 0;
            double bestLeftVariance = 0, bestRightVariance = 0;
            double currentVariance = computeVariance(y);

            for (int featureIdx : featureIndices) {
                double[] values = new double[nSamples];
                for (int i = 0; i < nSamples; i++) {
                    values[i] = X[i][featureIdx];
                }

                double[] sortedValues = values.clone();
                java.util.Arrays.sort(sortedValues);

                for (int i = 0; i < nSamples - 1; i++) {
                    double threshold = (sortedValues[i] + sortedValues[i + 1]) / 2.0;

                    double[] yLeft = new double[nSamples];
                    double[] yRight = new double[nSamples];
                    int leftCount = 0, rightCount = 0;

                    for (int j = 0; j < nSamples; j++) {
                        if (X[j][featureIdx] <= threshold) {
                            yLeft[leftCount++] = y[j];
                        } else {
                            yRight[rightCount++] = y[j];
                        }
                    }

                    if (leftCount < minChildWeight || rightCount < minChildWeight) {
                        continue;
                    }

                    double leftVariance = computeVariance(yLeft, leftCount);
                    double rightVariance = computeVariance(yRight, rightCount);
                    double gain = currentVariance - (leftCount * leftVariance + rightCount * rightVariance) / nSamples;

                    if (gain > bestGain) {
                        bestGain = gain;
                        bestFeature = featureIdx;
                        bestThreshold = threshold;
                        bestLeftVariance = leftVariance;
                        bestRightVariance = rightVariance;
                    }
                }
            }

            if (bestFeature == -1) {
                node.isLeaf = true;
                node.value = computeMean(y);
                return node;
            }

            node.featureIndex = bestFeature;
            node.threshold = bestThreshold;

            double[][] XLeft = new double[nSamples][nFeatures];
            double[][] XRight = new double[nSamples][nFeatures];
            double[] yLeft = new double[nSamples];
            double[] yRight = new double[nSamples];
            int leftCount = 0, rightCount = 0;

            for (int i = 0; i < nSamples; i++) {
                if (X[i][bestFeature] <= bestThreshold) {
                    XLeft[leftCount] = X[i];
                    yLeft[leftCount++] = y[i];
                } else {
                    XRight[rightCount] = X[i];
                    yRight[rightCount++] = y[i];
                }
            }

            node.left = buildTree(XLeft, yLeft, featureIndices, depth + 1);
            node.right = buildTree(XRight, yRight, featureIndices, depth + 1);

            return node;
        }

        private double computeMean(double[] y) {
            double sum = 0;
            for (double v : y) {
                sum += v;
            }
            return sum / y.length;
        }

        private double computeMean(double[] y, int count) {
            double sum = 0;
            for (int i = 0; i < count; i++) {
                sum += y[i];
            }
            return sum / count;
        }

        private double computeVariance(double[] y) {
            if (y.length == 0) return 0;
            double mean = computeMean(y);
            double sumSq = 0;
            for (double v : y) {
                sumSq += (v - mean) * (v - mean);
            }
            return sumSq / y.length;
        }

        private double computeVariance(double[] y, int count) {
            if (count == 0) return 0;
            double mean = computeMean(y, count);
            double sumSq = 0;
            for (int i = 0; i < count; i++) {
                sumSq += (y[i] - mean) * (y[i] - mean);
            }
            return sumSq / count;
        }

        public static class TreeNode {
            public boolean isLeaf;
            public double value;
            public TreeNode left;
            public TreeNode right;
            public int featureIndex;
            public double threshold;

            /**
             * TreeNode方法。
             */
            public TreeNode() {
                this.isLeaf = false;
                this.value = 0.0;
                this.left = null;
                this.right = null;
                this.featureIndex = -1;
                this.threshold = 0.0;
            }
        }
    }
}

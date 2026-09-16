package com.zifang.util.ml.ensemble;

import com.zifang.util.ml.tree.DecisionTree;
import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bagging (Bootstrap Aggregating) ensemble classifier.
 * Creates multiple DecisionTree estimators, each trained on a bootstrap sample,
 * and aggregates predictions via majority vote (classification) or averaging (regression).
 */
public class Bagging {
    private int nEstimators;
    private int maxDepth;
    private int minSamplesSplit;
    private List<DecisionTree> estimators;
    private Random random;
    private int nClasses;
    private boolean isRegression;

    /**
     * Create a Bagging ensemble.
     *
     * @param nEstimators     Number of base estimators
     * @param maxDepth        Maximum depth of each DecisionTree
     * @param minSamplesSplit Minimum samples required to split a node
     */
    public Bagging(int nEstimators, int maxDepth, int minSamplesSplit) {
        this.nEstimators = nEstimators;
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.estimators = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Fit the Bagging ensemble on training data.
     * Creates bootstrap samples and trains a DecisionTree on each.
     *
     * @param X Training features
     * @param y Training labels
     */
    public void fit(NdArray X, int[] y) {
        int nSamples = X.getShape().get(0);
        int nFeatures = X.getShape().get(1);

        // Determine if regression (continuous labels) or classification
        this.isRegression = isRegressionLabels(y);
        this.nClasses = findMaxValue(y) + 1;

        double[][] Xdata = toDouble2D(X);

        // Build each tree with bootstrap sample
        for (int i = 0; i < nEstimators; i++) {
            // Create bootstrap sample
            int[] bootstrapIndices = bootstrapSample(nSamples);

            double[][] XBootstrap = new double[nSamples][nFeatures];
            int[] yBootstrap = new int[nSamples];

            for (int j = 0; j < nSamples; j++) {
                int origIdx = bootstrapIndices[j];
                XBootstrap[j] = Xdata[origIdx];
                yBootstrap[j] = y[origIdx];
            }

            // Create and train tree
            DecisionTree tree = new DecisionTree(maxDepth, minSamplesSplit, 1);
            NdArray XbootstrapNd = createNdArray(XBootstrap, nSamples, nFeatures);
            tree.fit(XbootstrapNd, yBootstrap);
            estimators.add(tree);
        }
    }

    /**
     * Predict class labels using majority vote.
     *
     * @param X Features to predict
     * @return Predicted class labels
     */
    public int[] predict(NdArray X) {
        int nSamples = X.getShape().get(0);
        int[][] predictions = new int[nEstimators][nSamples];

        // Get predictions from all estimators
        for (int i = 0; i < nEstimators; i++) {
            predictions[i] = estimators.get(i).predict(X);
        }

        // Majority vote
        int[] finalPredictions = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            int[] votes = new int[nClasses];
            for (int j = 0; j < nEstimators; j++) {
                votes[predictions[j][i]]++;
            }

            int maxVote = 0;
            int bestClass = 0;
            for (int c = 0; c < nClasses; c++) {
                if (votes[c] > maxVote) {
                    maxVote = votes[c];
                    bestClass = c;
                }
            }
            finalPredictions[i] = bestClass;
        }

        return finalPredictions;
    }

    /**
     * Predict class probabilities by averaging probabilities from all estimators.
     *
     * @param X Features to predict
     * @return Probability array of shape (n_samples, n_classes)
     */
    public NdArray predictProba(NdArray X) {
        int nSamples = X.getShape().get(0);
        int nClasses = this.nClasses;
        double[][] avgProba = new double[nSamples][nClasses];

        // Accumulate probabilities from all estimators
        for (int i = 0; i < nEstimators; i++) {
            NdArray treeProba = estimators.get(i).predictProba(X);
            double[][] probaData = toDouble2D(treeProba);
            for (int j = 0; j < nSamples; j++) {
                for (int c = 0; c < nClasses; c++) {
                    avgProba[j][c] += probaData[j][c];
                }
            }
        }

        // Average
        for (int i = 0; i < nSamples; i++) {
            for (int c = 0; c < nClasses; c++) {
                avgProba[i][c] /= nEstimators;
            }
        }

        return createNdArray(avgProba, nSamples, nClasses);
    }

    /**
     * Create a bootstrap sample of indices (sampling with replacement).
     */
    private int[] bootstrapSample(int nSamples) {
        int[] indices = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            indices[i] = random.nextInt(nSamples);
        }
        return indices;
    }

    private boolean isRegressionLabels(int[] y) {
        // If labels span a wide range beyond typical class indices, consider regression
        int max = findMaxValue(y);
        int min = findMinValue(y);
        return (max - min) > nEstimators * 2;
    }

    private int findMinValue(int[] arr) {
        int min = arr[0];
        for (int val : arr) {
            if (val < min) min = val;
        }
        return min;
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
     * getNEstimators方法。
     *
     * @return int类型返回值
     */
    public int getNEstimators() {
        return nEstimators;
    }

    /**
     * getEstimators方法。
     *
     * @return List<DecisionTree>类型返回值
     */
    public List<DecisionTree> getEstimators() {
        return estimators;
    }
}

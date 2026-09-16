package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * Random Forest ensemble classifier
 * Uses bootstrap sampling and random feature selection
 * Aggregates predictions via majority vote
 */
public class RandomForest {
    private int nEstimators;
    private int maxDepth;
    private int minSamplesSplit;
    private int maxFeatures;
    private DecisionTree[] trees;
    private Random random;

    /**
     * RandomForest方法。
     * * @param nEstimators int类型参数
     *
     * @param maxDepth        int类型参数
     * @param minSamplesSplit int类型参数
     * @param maxFeatures     int类型参数
     */
    public RandomForest(int nEstimators, int maxDepth, int minSamplesSplit, int maxFeatures) {
        this.nEstimators = nEstimators;
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.maxFeatures = maxFeatures;
        this.trees = new DecisionTree[nEstimators];
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
        int nFeatures = X.getShape().get(1);

        // Calculate maxFeatures if not specified (sqrt for classification)
        if (this.maxFeatures <= 0) {
            this.maxFeatures = (int) Math.sqrt(nFeatures);
        }
        if (this.maxFeatures > nFeatures) {
            this.maxFeatures = nFeatures;
        }

        double[][] Xdata = toDouble2D(X);

        // Build each tree with bootstrap sample
        for (int i = 0; i < nEstimators; i++) {
            // Create bootstrap sample
            int[] bootstrapIndices = bootstrapSample(nSamples);
            int[] featureIndices = randomFeatureIndices(nFeatures);

            double[][] XBootstrap = new double[nSamples][maxFeatures];
            int[] yBootstrap = new int[nSamples];

            for (int j = 0; j < nSamples; j++) {
                int origIdx = bootstrapIndices[j];
                yBootstrap[j] = y[origIdx];
                for (int k = 0; k < maxFeatures; k++) {
                    XBootstrap[j][k] = Xdata[origIdx][featureIndices[k]];
                }
            }

            // Create and train tree
            DecisionTree tree = new DecisionTree(maxDepth, minSamplesSplit, 1);
            NdArray XbootstrapNd = createNdArray(XBootstrap, nSamples, maxFeatures);
            tree.fit(XbootstrapNd, yBootstrap);
            trees[i] = tree;
        }
    }

    /**
     * predict方法。
     * * @param X NdArray类型参数
     *
     * @return int[]类型返回值
     */
    public int[] predict(NdArray X) {
        int nSamples = X.getShape().get(0);
        int[][] predictions = new int[nEstimators][nSamples];

        // Get predictions from all trees
        for (int i = 0; i < nEstimators; i++) {
            predictions[i] = trees[i].predict(X);
        }

        // Majority vote
        int[] finalPredictions = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            int[] votes = new int[trees[0].getNClasses()];
            for (int j = 0; j < nEstimators; j++) {
                votes[predictions[j][i]]++;
            }

            int maxVote = 0;
            int bestClass = 0;
            for (int c = 0; c < votes.length; c++) {
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
     * predictProba方法。
     * * @param X NdArray类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray predictProba(NdArray X) {
        int nSamples = X.getShape().get(0);
        int nClasses = trees[0].getNClasses();
        double[][] avgProba = new double[nSamples][nClasses];

        // Accumulate probabilities from all trees
        for (int i = 0; i < nEstimators; i++) {
            NdArray treeProba = trees[i].predictProba(X);
            double[][] probaData = toDouble2D(treeProba);
            for (int j = 0; j < nSamples; j++) {
                for (int c = 0; c < nClasses; c++) {
                    avgProba[j][c] += probaData[j][c];
                }
            }
        }

        // Average and normalize
        for (int i = 0; i < nSamples; i++) {
            double sum = 0;
            for (int c = 0; c < nClasses; c++) {
                sum += avgProba[i][c];
            }
            if (sum > 0) {
                for (int c = 0; c < nClasses; c++) {
                    avgProba[i][c] /= sum;
                }
            }
        }

        return createNdArray(avgProba, nSamples, nClasses);
    }

    private int[] bootstrapSample(int nSamples) {
        int[] indices = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            indices[i] = random.nextInt(nSamples);
        }
        return indices;
    }

    private int[] randomFeatureIndices(int nFeatures) {
        int[] shuffled = new int[nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            shuffled[i] = i;
        }

        // Fisher-Yates shuffle
        for (int i = nFeatures - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = shuffled[i];
            shuffled[i] = shuffled[j];
            shuffled[j] = temp;
        }

        int[] result = new int[maxFeatures];
        for (int i = 0; i < maxFeatures; i++) {
            result[i] = shuffled[i];
        }
        return result;
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
}

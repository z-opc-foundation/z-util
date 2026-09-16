package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * AdaBoost (Adaptive Boosting)
 * Uses decision stumps (depth-1 trees) as base learners
 * Reweights samples at each round based on misclassification
 * Final prediction via weighted majority vote
 */
public class AdaBoost {
    private int nEstimators;
    private double learningRate;
    private DecisionStump[] stumps;
    private double[] alphas;
    private int nClasses;
    private Random random;

    /**
     * AdaBoost方法。
     * * @param nEstimators int类型参数
     *
     * @param learningRate double类型参数
     */
    public AdaBoost(int nEstimators, double learningRate) {
        this.nEstimators = nEstimators;
        this.learningRate = learningRate;
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

        this.nClasses = findMaxValue(y) + 1;
        this.stumps = new DecisionStump[nEstimators];
        this.alphas = new double[nEstimators];

        double[][] Xdata = toDouble2D(X);

        // Initialize sample weights uniformly
        double[] weights = new double[nSamples];
        for (int i = 0; i < nSamples; i++) {
            weights[i] = 1.0 / nSamples;
        }

        for (int t = 0; t < nEstimators; t++) {
            // Find best decision stump
            DecisionStump stump = findBestStump(Xdata, y, weights, nFeatures);
            stumps[t] = stump;

            // Compute weighted error
            double error = 0;
            for (int i = 0; i < nSamples; i++) {
                int predicted = stump.predict(Xdata[i]);
                if (predicted != y[i]) {
                    error += weights[i];
                }
            }

            // Avoid division by zero
            error = Math.max(1e-10, Math.min(1 - 1e-10, error));

            // Compute stump weight (alpha)
            alphas[t] = learningRate * 0.5 * Math.log((1 - error) / error);

            // Update sample weights
            double weightSum = 0;
            for (int i = 0; i < nSamples; i++) {
                int predicted = stump.predict(Xdata[i]);
                double indicator = (predicted == y[i]) ? 0 : 1;
                weights[i] *= Math.exp(alphas[t] * (2 * indicator - 1));
                weightSum += weights[i];
            }

            // Normalize weights
            for (int i = 0; i < nSamples; i++) {
                weights[i] /= weightSum;
            }
        }
    }

    /**
     * predict方法。
     * * @param X NdArray类型参数
     *
     * @return int[]类型返回值
     */
    public int[] predict(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;
        int[] predictions = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            predictions[i] = predictSingle(Xdata[i]);
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
        int nSamples = Xdata.length;
        double[][] proba = new double[nSamples][nClasses];

        // Accumulate weighted votes
        for (int i = 0; i < nSamples; i++) {
            double[] classWeights = new double[nClasses];
            for (int t = 0; t < nEstimators; t++) {
                int predicted = stumps[t].predict(Xdata[i]);
                // Use exp(-alpha) weighting
                double weight = Math.exp(-alphas[t]);
                classWeights[predicted] += weight;
            }

            // Normalize to get probabilities
            double sum = 0;
            for (int c = 0; c < nClasses; c++) {
                sum += classWeights[c];
            }
            if (sum > 0) {
                for (int c = 0; c < nClasses; c++) {
                    proba[i][c] = classWeights[c] / sum;
                }
            } else {
                // Uniform if sum is zero
                for (int c = 0; c < nClasses; c++) {
                    proba[i][c] = 1.0 / nClasses;
                }
            }
        }

        return createNdArray(proba, nSamples, nClasses);
    }

    private int predictSingle(double[] x) {
        double[] classWeights = new double[nClasses];

        for (int t = 0; t < nEstimators; t++) {
            int predicted = stumps[t].predict(x);
            // Use exp(-alpha) weighting
            double weight = Math.exp(-alphas[t]);
            classWeights[predicted] += weight;
        }

        // Return class with highest weighted vote
        int bestClass = 0;
        double maxWeight = classWeights[0];
        for (int c = 1; c < nClasses; c++) {
            if (classWeights[c] > maxWeight) {
                maxWeight = classWeights[c];
                bestClass = c;
            }
        }
        return bestClass;
    }

    private DecisionStump findBestStump(double[][] X, int[] y, double[] weights, int nFeatures) {
        int nSamples = X.length;
        DecisionStump bestStump = new DecisionStump(nClasses);

        double bestError = Double.MAX_VALUE;

        for (int featureIdx = 0; featureIdx < nFeatures; featureIdx++) {
            // Get values for this feature
            double[] values = new double[nSamples];
            for (int i = 0; i < nSamples; i++) {
                values[i] = X[i][featureIdx];
            }

            double[] sortedValues = values.clone();
            java.util.Arrays.sort(sortedValues);

            // Try thresholds at midpoints
            for (int i = 0; i < nSamples - 1; i++) {
                double threshold = (sortedValues[i] + sortedValues[i + 1]) / 2.0;

                // Compute error for each side prediction assignment
                for (int leftClass = 0; leftClass < nClasses; leftClass++) {
                    for (int rightClass = 0; rightClass < nClasses; rightClass++) {
                        if (leftClass == rightClass) continue;

                        DecisionStump stump = new DecisionStump(nClasses);
                        stump.featureIndex = featureIdx;
                        stump.threshold = threshold;
                        stump.predictions[0] = leftClass;
                        stump.predictions[1] = rightClass;

                        double error = 0;
                        for (int j = 0; j < nSamples; j++) {
                            int predicted = stump.predict(X[j]);
                            if (predicted != y[j]) {
                                error += weights[j];
                            }
                        }

                        if (error < bestError) {
                            bestError = error;
                            bestStump = stump;
                        }
                    }
                }
            }
        }

        return bestStump;
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
     * Decision stump (depth-1 tree) for AdaBoost
     */
    public static class DecisionStump {
        public int featureIndex;
        public double threshold;
        public int[] predictions;
        public int nClasses;

        /**
         * DecisionStump方法。
         * * @param nClasses int类型参数
         */
        public DecisionStump(int nClasses) {
            this.nClasses = nClasses;
            this.predictions = new int[2];  // Prediction for left and right
        }

        /**
         * predict方法。
         * * @param x double[]类型参数
         *
         * @return int类型返回值
         */
        public int predict(double[] x) {
            if (x[featureIndex] <= threshold) {
                return predictions[0];
            } else {
                return predictions[1];
            }
        }

        /**
         * predictIndex方法。
         * * @param x double[]类型参数
         *
         * @return int类型返回值
         */
        public int predictIndex(double[] x) {
            if (x[featureIndex] <= threshold) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}

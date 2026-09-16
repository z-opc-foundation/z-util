package com.zifang.util.ml.linear;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Basic Perceptron classifier.
 * Uses online learning (stochastic gradient descent).
 * Convergence only guaranteed for linearly separable data.
 */
public class Perceptron {

    private double learningRate;
    private int nIterations;
    private double[] weights;
    private double bias;

    /**
     * Create a new Perceptron classifier.
     *
     * @param learningRate Learning rate for weight updates
     * @param nIterations  Number of passes over the training data
     */
    public Perceptron(double learningRate, int nIterations) {
        this.learningRate = learningRate;
        this.nIterations = nIterations;
    }

    /**
     * Add bias term to feature matrix.
     */
    private NdArray addBias(NdArray X) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);
        double[] result = new double[n * (d + 1)];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                Object val = X.get(i, j);
                result[i * (d + 1) + j] = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
            }
            result[i * (d + 1) + d] = 1.0;
        }
        return NdArray.create(result, DType.FLOAT64, new Shape(n, d + 1));
    }

    /**
     * Convert NdArray row to double[].
     */
    private double[] rowToDoubleArray(NdArray X, int row) {
        int cols = X.getShape().get(1);
        double[] result = new double[cols];
        for (int j = 0; j < cols; j++) {
            Object val = X.get(row, j);
            result[j] = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
        }
        return result;
    }

    /**
     * Fit the perceptron to training data using online learning.
     * Updates weights for each misclassified sample.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @param y Target labels (binary: 0 or 1)
     */
    public void fit(NdArray X, int[] y) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);

        // Add bias term
        NdArray Xb = addBias(X);
        int d1 = d + 1;

        // Initialize weights to zeros
        this.weights = new double[d1];
        this.bias = 0.0;

        // Online learning: iterate through samples and update for misclassifications
        for (int iter = 0; iter < nIterations; iter++) {
            for (int i = 0; i < n; i++) {
                double[] xi = rowToDoubleArray(Xb, i);
                int yi = y[i];

                // Compute prediction: 1 if activation >= 0, else 0
                double activation = 0;
                for (int j = 0; j < d1; j++) {
                    activation += weights[j] * xi[j];
                }
                int prediction = activation >= 0 ? 1 : 0;

                // Update weights for misclassified samples
                if (prediction != yi) {
                    int update = yi - prediction; // +1 or -1
                    for (int j = 0; j < d1; j++) {
                        weights[j] += learningRate * update * xi[j];
                    }
                }
            }
        }
    }

    /**
     * Predict class labels for samples.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @return Array of predicted class labels (0 or 1)
     */
    public int[] predict(NdArray X) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);
        int[] predictions = new int[n];

        // X is n x d, weights is d+1 (including bias)
        // We need to add bias column for prediction too
        double[][] Xb = new double[n][d + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                Object val = X.get(i, j);
                Xb[i][j] = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
            }
            Xb[i][d] = 1.0; // bias term
        }

        for (int i = 0; i < n; i++) {
            double activation = 0;
            for (int j = 0; j < d + 1; j++) {
                activation += weights[j] * Xb[i][j];
            }
            predictions[i] = activation >= 0 ? 1 : 0;
        }
        return predictions;
    }

    /**
     * Calculate accuracy score on given data.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @param y True labels
     * @return Accuracy (fraction of correctly classified samples)
     */
    public double score(NdArray X, int[] y) {
        int[] predictions = predict(X);
        int correct = 0;
        for (int i = 0; i < y.length; i++) {
            if (predictions[i] == y[i]) {
                correct++;
            }
        }
        return (double) correct / y.length;
    }
}

package com.zifang.util.ml.linear;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Logistic Regression classifier using binary cross-entropy loss with L2 regularization.
 * Uses gradient descent optimization.
 */
public class LogisticRegression {

    private final double learningRate;
    private final int nIterations;
    private final double lambda;
    private double[] weights;
    private double bias;

    /**
     * Create a new Logistic Regression classifier.
     *
     * @param learningRate Learning rate for gradient descent
     * @param nIterations  Number of iterations for gradient descent
     * @param lambda       L2 regularization parameter
     */
    public LogisticRegression(double learningRate, int nIterations, double lambda) {
        this.learningRate = learningRate;
        this.nIterations = nIterations;
        this.lambda = lambda;
    }

    /**
     * Sigmoid function: σ(z) = 1 / (1 + exp(-z))
     */
    private double sigmoid(double z) {
        if (z > 20) return 0.999999;
        if (z < -20) return 0.000001;
        return 1.0 / (1.0 + Math.exp(-z));
    }

    /**
     * Add bias term to feature matrix (column of ones).
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
     * Convert NdArray to double[][].
     */
    private double[][] toDouble2D(NdArray arr) {
        int rows = arr.getShape().get(0);
        int cols = arr.getShape().get(1);
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Object val = arr.get(i, j);
                result[i][j] = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
            }
        }
        return result;
    }

    /**
     * Fit the model to training data.
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

        // Initialize weights
        this.weights = new double[d1];
        this.bias = 0.0;

        // Gradient descent
        for (int iter = 0; iter < nIterations; iter++) {
            // Compute predictions
            double[] predictions = new double[n];
            for (int i = 0; i < n; i++) {
                double z = 0;
                for (int j = 0; j < d1; j++) {
                    Object val = Xb.get(i, j);
                    double x = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
                    z += x * weights[j];
                }
                predictions[i] = sigmoid(z);
            }

            // Compute gradient
            double[] grad = new double[d1];
            for (int j = 0; j < d1; j++) {
                double sum = 0;
                for (int i = 0; i < n; i++) {
                    Object val = Xb.get(i, j);
                    double x = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
                    double pred = predictions[i];
                    sum += x * (pred - y[i]);
                }
                grad[j] = sum / n;
                if (j < d) {
                    grad[j] += lambda * weights[j] / n; // L2 regularization
                }
            }

            // Update weights (including bias at weights[d])
            for (int j = 0; j < d1; j++) {
                weights[j] -= learningRate * grad[j];
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

        for (int i = 0; i < n; i++) {
            double z = 0;
            for (int j = 0; j < d; j++) {
                Object val = X.get(i, j);
                double x = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
                z += x * weights[j];
            }
            z += weights[d];  // bias is stored in weights[d]
            predictions[i] = sigmoid(z) >= 0.5 ? 1 : 0;
        }
        return predictions;
    }

    /**
     * Predict class probabilities for samples.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @return NdArray of shape [n_samples, 2] with probabilities for each class
     */
    public NdArray predictProba(NdArray X) {
        int n = X.getShape().get(0);
        double[] proba = new double[n * 2];

        for (int i = 0; i < n; i++) {
            double z = 0;
            for (int j = 0; j < weights.length - 1; j++) {
                Object val = X.get(i, j);
                double x = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
                z += x * weights[j];
            }
            z += weights[weights.length - 1];  // bias is stored in weights[d]
            double p = sigmoid(z);
            proba[i * 2] = 1.0 - p;
            proba[i * 2 + 1] = p;
        }

        return NdArray.create(proba, DType.FLOAT64, new Shape(n, 2));
    }
}

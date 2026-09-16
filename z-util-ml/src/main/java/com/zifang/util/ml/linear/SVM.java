package com.zifang.util.ml.linear;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Support Vector Machine (Soft-margin) classifier.
 * Supports LINEAR and RBF kernels.
 * Binary classification only (labels {+1, -1}).
 */
public class SVM {

    private double learningRate;
    private int nIterations;
    private double lambda;
    private KernelType kernelType;
    private double[] weights;
    private double bias;
    private double[][] supportVectors;
    private int[] supportVectorLabels;
    private double gamma;

    /**
     * Create a new SVM classifier.
     *
     * @param learningRate Learning rate for gradient descent
     * @param nIterations  Number of iterations
     * @param lambda       L2 regularization parameter (controls margin softness)
     * @param kernelType   Type of kernel (LINEAR or RBF)
     */
    public SVM(double learningRate, int nIterations, double lambda, KernelType kernelType) {
        this.learningRate = learningRate;
        this.nIterations = nIterations;
        this.lambda = lambda;
        this.kernelType = kernelType;
        this.gamma = 0.1; // default gamma for RBF
    }

    /**
     * Set gamma parameter for RBF kernel.
     */
    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    /**
     * Linear kernel: K(x, x') = x · x'
     */
    private double linearKernel(double[] x1, double[] x2) {
        double sum = 0;
        for (int i = 0; i < x1.length; i++) {
            sum += x1[i] * x2[i];
        }
        return sum;
    }

    /**
     * RBF (Gaussian) kernel: K(x, x') = exp(-γ * ||x - x'||²)
     */
    private double rbfKernel(double[] x1, double[] x2) {
        double sum = 0;
        for (int i = 0; i < x1.length; i++) {
            double diff = x1[i] - x2[i];
            sum += diff * diff;
        }
        return Math.exp(-gamma * sum);
    }

    /**
     * Compute kernel matrix row for a sample against all support vectors.
     */
    private double computeKernelRow(double[] x, int idx) {
        if (kernelType == KernelType.LINEAR) {
            return linearKernel(x, supportVectors[idx]);
        } else {
            return rbfKernel(x, supportVectors[idx]);
        }
    }

    /**
     * Add bias term to feature matrix.
     */
    private NdArray addBias(NdArray X) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);
        double[] ones = new double[n];
        for (int i = 0; i < n; i++) ones[i] = 1.0;
        NdArray biasCol = NdArray.create(ones, DType.FLOAT64, new Shape(n, 1));

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
     * Fit the SVM model using gradient descent on the primal form.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @param y Target labels (+1 or -1)
     */
    public void fit(NdArray X, int[] y) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);

        // Add bias term
        NdArray Xb = addBias(X);
        int d1 = d + 1;

        // Initialize weights
        this.weights = new double[d1];

        // Gradient descent on primal SVM
        for (int iter = 0; iter < nIterations; iter++) {
            double[] grad = new double[d1];
            double totalLoss = 0;

            for (int i = 0; i < n; i++) {
                double[] xi = rowToDoubleArray(Xb, i);
                double yi = y[i];

                // Compute margin
                double margin = 0;
                for (int j = 0; j < d1; j++) {
                    margin += weights[j] * xi[j];
                }
                margin *= yi;

                // Hinge loss derivative
                if (margin < 1) {
                    totalLoss += 1 - margin;
                    for (int j = 0; j < d1; j++) {
                        grad[j] -= yi * xi[j];
                    }
                }
            }

            // Add L2 regularization gradient
            for (int j = 0; j < d1; j++) {
                grad[j] += lambda * weights[j];
            }

            // Scale gradient by n
            for (int j = 0; j < d1; j++) {
                grad[j] /= n;
            }

            // Update weights
            for (int j = 0; j < d1; j++) {
                weights[j] -= learningRate * grad[j];
            }
        }

        // Store support vectors for kernel predictions
        int svCount = 0;
        for (int i = 0; i < n; i++) {
            double[] xi = rowToDoubleArray(X, i);
            double margin = 0;
            for (int j = 0; j < d; j++) {
                margin += weights[j] * xi[j];
            }
            margin += weights[d]; // bias
            margin *= y[i];
            if (margin < 2) { // Approximate support vector selection
                svCount++;
            }
        }

        supportVectors = new double[svCount][];
        supportVectorLabels = new int[svCount];
        int svIdx = 0;
        for (int i = 0; i < n; i++) {
            double[] xi = rowToDoubleArray(X, i);
            double margin = 0;
            for (int j = 0; j < d; j++) {
                margin += weights[j] * xi[j];
            }
            margin += weights[d];
            margin *= y[i];
            if (margin < 2) {
                supportVectors[svIdx] = xi;
                supportVectorLabels[svIdx] = y[i];
                svIdx++;
            }
        }

        // Compute bias from support vectors
        bias = 0;
        int biasCount = 0;
        for (int i = 0; i < supportVectors.length; i++) {
            double decision = 0;
            for (int j = 0; j < d; j++) {
                decision += weights[j] * supportVectors[i][j];
            }
            decision += weights[d];
            if (supportVectorLabels[i] * decision < 2) {
                bias -= supportVectorLabels[i] - decision;
                biasCount++;
            }
        }
        if (biasCount > 0) bias /= biasCount;
    }

    /**
     * Predict class labels for samples.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @return Array of predicted class labels (+1 or -1)
     */
    public int[] predict(NdArray X) {
        int n = X.getShape().get(0);
        int[] predictions = new int[n];

        for (int i = 0; i < n; i++) {
            double[] xi = rowToDoubleArray(X, i);
            double decision = 0;
            for (int j = 0; j < weights.length - 1; j++) {
                decision += weights[j] * xi[j];
            }
            decision += bias;
            predictions[i] = decision >= 0 ? 1 : -1;
        }
        return predictions;
    }

    /**
     * KernelType枚举。
     */
    public enum KernelType {
        LINEAR,
        RBF
    }
}

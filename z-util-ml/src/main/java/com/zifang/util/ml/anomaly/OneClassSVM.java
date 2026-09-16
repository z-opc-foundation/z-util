package com.zifang.util.ml.anomaly;

import com.zifang.util.numpy.NdArray;

import java.util.Arrays;
import java.util.Random;

/**
 * One-Class SVM for outlier detection.
 * <p>
 * Algorithm:
 * 1. Map data to feature space via kernel
 * 2. Find hyperplane that separates data from origin with maximum margin
 * 3. Decision function: f(x) = sign(w · φ(x) - ρ)
 * 4. Points with f(x) < 0 are anomalies
 * 5. Use SGD for dual optimization with one-class objective
 */
public class OneClassSVM {

    private double nu;
    private String kernel;
    private double gamma;
    private int maxIterations;
    private double learningRate;

    private double[] weights;
    private double rho;
    private int nFeatures;
    private int trainingSize;
    private double[][] supportVectors;
    private double[] dualCoefficients;
    private int nSupportVectors;

    /**
     * Create a new One-Class SVM.
     *
     * @param nu     Upper bound on fraction of outliers (e.g. 0.1)
     * @param kernel Kernel type: "RBF" or "LINEAR"
     * @param gamma  Kernel parameter for RBF (ignored for LINEAR)
     */
    public OneClassSVM(double nu, String kernel, double gamma) {
        this.nu = nu;
        this.kernel = kernel.toUpperCase();
        this.gamma = gamma;
        this.maxIterations = 1000;
        this.learningRate = 0.01;
    }

    /**
     * Fit the One-Class SVM to the training data.
     */
    public void fit(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        this.nFeatures = Xdata[0].length;
        this.trainingSize = Xdata.length;

        // Initialize weights randomly
        Random random = new Random();
        this.weights = new double[nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            weights[i] = (random.nextDouble() - 0.5) * 0.1;
        }
        this.rho = 0.0;

        // SGD optimization for dual formulation
        // Minimize: (1/2) * sum(alpha_i * alpha_j * K(x_i, x_j)) - sum(alpha_i)
        // Subject to: 0 <= alpha_i <= 1/n, sum(alpha_i) = 1

        double[] alpha = new double[trainingSize];
        double[] gradient = new double[trainingSize];

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Compute gradient of objective function
            // Gradient: sum_j(alpha_j * K(x_i, x_j)) - 1

            // For SGD, we approximate the gradient
            int batchSize = Math.min(32, trainingSize);
            int[] batchIndices = new int[batchSize];
            for (int i = 0; i < batchSize; i++) {
                batchIndices[i] = random.nextInt(trainingSize);
            }

            // Compute kernel matrix for batch
            double[][] Kbatch = new double[batchSize][trainingSize];
            for (int i = 0; i < batchSize; i++) {
                for (int j = 0; j < trainingSize; j++) {
                    Kbatch[i][j] = kernelFunction(Xdata[batchIndices[i]], Xdata[j]);
                }
            }

            // Update alpha for batch
            for (int i = 0; i < batchSize; i++) {
                int idx = batchIndices[i];

                // Compute gradient approximation
                double gradSum = 0.0;
                for (int j = 0; j < trainingSize; j++) {
                    gradSum += alpha[j] * Kbatch[i][j];
                }
                gradient[idx] = gradSum - 1.0;

                // SGD update with projection onto [0, 1/n]
                alpha[idx] -= learningRate * gradient[idx];
                alpha[idx] = Math.max(0.0, Math.min(1.0 / trainingSize, alpha[idx]));
            }
        }

        // Project alpha to satisfy sum(alpha) = 1
        double alphaSum = Arrays.stream(alpha).sum();
        if (alphaSum > 0) {
            for (int i = 0; i < trainingSize; i++) {
                alpha[i] /= alphaSum;
            }
        }

        // Find support vectors (alpha > small threshold)
        this.nSupportVectors = 0;
        for (int i = 0; i < trainingSize; i++) {
            if (alpha[i] > 1e-7) {
                nSupportVectors++;
            }
        }

        this.supportVectors = new double[nSupportVectors][nFeatures];
        this.dualCoefficients = new double[nSupportVectors];

        int svIndex = 0;
        for (int i = 0; i < trainingSize; i++) {
            if (alpha[i] > 1e-7) {
                this.supportVectors[svIndex] = Xdata[i].clone();
                this.dualCoefficients[svIndex] = alpha[i];
                svIndex++;
            }
        }

        // Compute rho (offset) from support vectors
        computeRho();
    }

    /**
     * Compute rho (offset) from support vectors.
     */
    private void computeRho() {
        // rho = sum_i(alpha_i * K(sv_i, sv_j)) for any j where alpha_j > 0
        // We use the mean of such computations as a robust estimate
        double rhoSum = 0.0;
        int count = 0;

        for (int i = 0; i < nSupportVectors; i++) {
            double sum = 0.0;
            for (int j = 0; j < nSupportVectors; j++) {
                sum += dualCoefficients[j] * kernelFunction(supportVectors[i], supportVectors[j]);
            }
            rhoSum += sum;
            count++;
        }

        this.rho = count > 0 ? rhoSum / count : 0.0;
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
     * Compute kernel function between two points.
     */
    private double kernelFunction(double[] x1, double[] x2) {
        if ("RBF".equals(kernel)) {
            return rbfKernel(x1, x2);
        } else {
            return linearKernel(x1, x2);
        }
    }

    /**
     * Compute decision function value for a single point.
     * f(x) = sum_i(alpha_i * K(sv_i, x)) - rho
     */
    private double decisionFunctionSingle(double[] x) {
        if (nSupportVectors == 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (int i = 0; i < nSupportVectors; i++) {
            sum += dualCoefficients[i] * kernelFunction(supportVectors[i], x);
        }

        return sum - rho;
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
            predictions[i] = scores[i] >= 0 ? 1 : -1;
        }

        return predictions;
    }

    /**
     * Calculate decision function values for input data.
     *
     * @param X Input data (nSamples x nFeatures)
     * @return Array of decision function values (positive = normal, negative = anomaly)
     */
    public double[] score(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;
        double[] scores = new double[nSamples];

        for (int i = 0; i < nSamples; i++) {
            scores[i] = decisionFunctionSingle(Xdata[i]);
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
}

package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * Gaussian Mixture Model (GMM) clustering using EM algorithm.
 * <p>
 * Algorithm: EM for GMM
 * - E-step: compute posterior probability for each component
 * - M-step: update weights, means, covariances (diagonal covariance)
 * <p>
 * Uses NdArray for matrix operations where possible.
 */
public class GMM {
    private int nComponents;
    private int maxIterations;
    private double tolerance;

    private double[] weights;       // Component weights [k]
    private double[][] means;       // Component means [k x n_features]
    private double[][] variances;   // Component variances (diagonal) [k x n_features]
    private double[][] responsibilities; // Soft assignments [n_samples x k]

    private double[][] Xdata;
    private int nSamples;
    private int nFeatures;

    /**
     * GMM方法。
     * * @param nComponents int类型参数
     *
     * @param maxIterations int类型参数
     * @param tolerance     double类型参数
     */
    public GMM(int nComponents, int maxIterations, double tolerance) {
        this.nComponents = nComponents;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    /**
     * Fit the GMM to the data.
     *
     * @param X NdArray of shape [n_samples, n_features]
     */
    public void fit(NdArray X) {
        this.Xdata = toDouble2D(X);
        this.nSamples = Xdata.length;
        this.nFeatures = Xdata[0].length;

        // Initialize parameters
        initializeParameters();

        // EM iterations
        for (int iter = 0; iter < maxIterations; iter++) {
            // E-step: compute responsibilities
            double[][] newResponsibilities = eStep();

            // Compute change for convergence check
            double change = computeMaxChange(responsibilities, newResponsibilities);

            responsibilities = newResponsibilities;

            // M-step: update parameters
            mStep();

            if (change < tolerance) {
                break;
            }
        }
    }

    /**
     * Hard cluster assignment - each point assigned to most likely component.
     *
     * @param X NdArray of shape [n_samples, n_features]
     * @return cluster labels array
     */
    public int[] predict(NdArray X) {
        double[][] Xtest = toDouble2D(X);
        int n = Xtest.length;
        int[] predictions = new int[n];

        double[][] probs = computeLogLikelihood(Xtest);

        for (int i = 0; i < n; i++) {
            int best = 0;
            double maxProb = probs[i][0];
            for (int k = 1; k < nComponents; k++) {
                if (probs[i][k] > maxProb) {
                    maxProb = probs[i][k];
                    best = k;
                }
            }
            predictions[i] = best;
        }

        return predictions;
    }

    /**
     * Soft assignment - probability of each point belonging to each component.
     *
     * @param X NdArray of shape [n_samples, n_features]
     * @return NdArray of shape [n_samples x n_components] with probabilities
     */
    public NdArray predictProba(NdArray X) {
        double[][] Xtest = toDouble2D(X);
        double[][] probs = computeLogLikelihood(Xtest);

        // Convert from log-likelihood to probabilities
        // Apply log-sum-exp trick for numerical stability
        double[][] result = new double[nSamples][nComponents];

        for (int i = 0; i < nSamples; i++) {
            // Find max for numerical stability
            double maxLog = probs[i][0];
            for (int k = 1; k < nComponents; k++) {
                if (probs[i][k] > maxLog) {
                    maxLog = probs[i][k];
                }
            }

            // Compute softmax
            double sum = 0.0;
            for (int k = 0; k < nComponents; k++) {
                probs[i][k] = Math.exp(probs[i][k] - maxLog);
                sum += probs[i][k];
            }

            for (int k = 0; k < nComponents; k++) {
                result[i][k] = probs[i][k] / sum;
            }
        }

        return NdArray.array(flatten2D(result), DType.FLOAT64).reshape(nSamples, nComponents);
    }

    /**
     * Fit and return cluster labels.
     *
     * @param X NdArray of shape [n_samples, n_features]
     * @return cluster labels array
     */
    public int[] fitPredict(NdArray X) {
        fit(X);
        return predict(X);
    }

    /**
     * E-step: compute responsibilities (posterior probabilities).
     */
    private double[][] eStep() {
        double[][] r = new double[nSamples][nComponents];

        // Compute log-likelihood for each sample and component
        double[][] logLikelihood = computeLogLikelihood(Xdata);

        // Convert to responsibilities using log-sum-exp for numerical stability
        for (int i = 0; i < nSamples; i++) {
            // Find max log-likelihood for numerical stability
            double maxLog = logLikelihood[i][0];
            for (int k = 1; k < nComponents; k++) {
                if (logLikelihood[i][k] > maxLog) {
                    maxLog = logLikelihood[i][k];
                }
            }

            // Compute exp and sum
            double sum = 0.0;
            for (int k = 0; k < nComponents; k++) {
                logLikelihood[i][k] = Math.exp(logLikelihood[i][k] - maxLog);
                sum += logLikelihood[i][k];
            }

            // Normalize to get responsibilities
            for (int k = 0; k < nComponents; k++) {
                r[i][k] = logLikelihood[i][k] / sum;
            }
        }

        return r;
    }

    /**
     * M-step: update weights, means, and variances.
     */
    private void mStep() {
        // Compute effective number of points assigned to each component
        double[] Nk = new double[nComponents];
        for (int k = 0; k < nComponents; k++) {
            Nk[k] = 0.0;
            for (int i = 0; i < nSamples; i++) {
                Nk[k] += responsibilities[i][k];
            }
        }

        // Update weights
        double total = 0.0;
        for (int k = 0; k < nComponents; k++) {
            weights[k] = Nk[k] / nSamples;
            total += weights[k];
        }
        // Normalize weights
        for (int k = 0; k < nComponents; k++) {
            weights[k] /= total;
        }

        // Update means
        for (int k = 0; k < nComponents; k++) {
            if (Nk[k] > 0) {
                for (int j = 0; j < nFeatures; j++) {
                    means[k][j] = 0.0;
                    for (int i = 0; i < nSamples; i++) {
                        means[k][j] += responsibilities[i][k] * Xdata[i][j];
                    }
                    means[k][j] /= Nk[k];
                }
            }
        }

        // Update variances (diagonal covariance)
        for (int k = 0; k < nComponents; k++) {
            if (Nk[k] > 0) {
                for (int j = 0; j < nFeatures; j++) {
                    variances[k][j] = 0.0;
                    for (int i = 0; i < nSamples; i++) {
                        double diff = Xdata[i][j] - means[k][j];
                        variances[k][j] += responsibilities[i][k] * diff * diff;
                    }
                    variances[k][j] /= Nk[k];

                    // Add small regularization to prevent zero variance
                    if (variances[k][j] < 1e-6) {
                        variances[k][j] = 1e-6;
                    }
                }
            }
        }
    }

    /**
     * Compute log-likelihood for each sample under each component.
     */
    private double[][] computeLogLikelihood(double[][] X) {
        double[][] logLikelihood = new double[X.length][nComponents];

        // Precompute constant part: -0.5 * n_features * log(2*pi)
        double constPart = -0.5 * nFeatures * Math.log(2 * Math.PI);

        for (int i = 0; i < X.length; i++) {
            for (int k = 0; k < nComponents; k++) {
                // Log weight
                double logWeight = Math.log(weights[k] + 1e-300);

                // Log likelihood under Gaussian
                double logProb = constPart;
                for (int j = 0; j < nFeatures; j++) {
                    double diff = X[i][j] - means[k][j];
                    logProb -= 0.5 * Math.log(variances[k][j] + 1e-300);
                    logProb -= 0.5 * diff * diff / (variances[k][j] + 1e-300);
                }

                logLikelihood[i][k] = logWeight + logProb;
            }
        }

        return logLikelihood;
    }

    /**
     * Initialize GMM parameters using k-means for better initial placement.
     */
    private void initializeParameters() {
        Random rand = new Random();

        // Use k-means++ style initialization
        this.means = new double[nComponents][nFeatures];
        this.variances = new double[nComponents][nFeatures];
        this.weights = new double[nComponents];
        this.responsibilities = new double[nSamples][nComponents];

        // Pick first mean randomly
        int firstIdx = rand.nextInt(nSamples);
        for (int j = 0; j < nFeatures; j++) {
            means[0][j] = Xdata[firstIdx][j];
        }

        // Pick remaining means with probability proportional to distance squared
        double[] distances = new double[nSamples];
        for (int k = 1; k < nComponents; k++) {
            double totalDist = 0.0;
            for (int i = 0; i < nSamples; i++) {
                distances[i] = minDistanceSquared(Xdata[i], means, k);
                totalDist += distances[i];
            }

            double threshold = rand.nextDouble() * totalDist;
            double cumulative = 0.0;
            for (int i = 0; i < nSamples; i++) {
                cumulative += distances[i];
                if (cumulative >= threshold) {
                    for (int j = 0; j < nFeatures; j++) {
                        means[k][j] = Xdata[i][j];
                    }
                    break;
                }
            }
        }

        // Initialize variances to overall data variance
        double[] dataMean = new double[nFeatures];
        for (int j = 0; j < nFeatures; j++) {
            dataMean[j] = 0.0;
            for (int i = 0; i < nSamples; i++) {
                dataMean[j] += Xdata[i][j];
            }
            dataMean[j] /= nSamples;
        }

        double[] dataVar = new double[nFeatures];
        for (int j = 0; j < nFeatures; j++) {
            dataVar[j] = 0.0;
            for (int i = 0; i < nSamples; i++) {
                double diff = Xdata[i][j] - dataMean[j];
                dataVar[j] += diff * diff;
            }
            dataVar[j] /= nSamples;
            if (dataVar[j] < 1e-6) {
                dataVar[j] = 1e-6;
            }
        }

        for (int k = 0; k < nComponents; k++) {
            for (int j = 0; j < nFeatures; j++) {
                variances[k][j] = dataVar[j];
            }
            weights[k] = 1.0 / nComponents;
        }
    }

    /**
     * Find minimum squared distance from a point to existing means.
     */
    private double minDistanceSquared(double[] point, double[][] means, int nMeans) {
        double minDist = Double.MAX_VALUE;
        for (int m = 0; m < nMeans; m++) {
            double dist = 0.0;
            for (int j = 0; j < nFeatures; j++) {
                double diff = point[j] - means[m][j];
                dist += diff * diff;
            }
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    /**
     * Compute maximum change between two responsibility matrices.
     */
    private double computeMaxChange(double[][] r1, double[][] r2) {
        double maxChange = 0.0;
        for (int i = 0; i < nSamples; i++) {
            for (int k = 0; k < nComponents; k++) {
                double change = Math.abs(r1[i][k] - r2[i][k]);
                if (change > maxChange) {
                    maxChange = change;
                }
            }
        }
        return maxChange;
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
            // Generic fallback
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = ((Number) arr.get(i, j)).doubleValue();
                }
            }
        }

        return result;
    }

    /**
     * Flatten 2D double array to 1D for NdArray creation.
     */
    private double[] flatten2D(double[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = arr[i][j];
            }
        }
        return flat;
    }
}

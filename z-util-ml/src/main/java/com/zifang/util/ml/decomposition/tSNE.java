package com.zifang.util.ml.decomposition;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * t-Distributed Stochastic Neighbor Embedding (t-SNE)
 * <p>
 * A nonlinear dimensionality reduction technique that is particularly well-suited
 * for embedding high-dimensional data into a low-dimensional (2D or 3D) space
 * for visualization.
 * <p>
 * Algorithm steps:
 * 1. Compute pairwise distances in high-dim space
 * 2. Convert to conditional probabilities P(j|i) using perplexity
 * 3. Symmetrize: P = (P_ij + P_ji) / (2n)
 * 4. Initialize low-dim embeddings randomly (standard normal * 0.0001)
 * 5. Compute q_ij (Student-t distribution with df=1)
 * 6. Gradient descent: dL/dy_i = 4 * sum_j (p_ij - q_ij) * (y_i - y_j) * (1 + ||y_i - y_j||^2)^(-1)
 * 7. Early exaggeration: multiply P by 4 for first 100 iterations
 */
public class tSNE {
    private int nComponents;
    private int perplexity;
    private int nIterations;
    private double learningRate;

    // Precomputed values
    private double[][] P;  // Joint probabilities
    private double[][] Y;  // Low-dimensional embedding

    /**
     * Constructor with parameters
     *
     * @param nComponents  Number of dimensions for embedding (default 2)
     * @param perplexity   Perplexity parameter (default 30)
     * @param nIterations  Number of iterations (default 1000)
     * @param learningRate Learning rate for gradient descent (default 200)
     */
    public tSNE(int nComponents, int perplexity, int nIterations, double learningRate) {
        this.nComponents = nComponents;
        this.perplexity = perplexity;
        this.nIterations = nIterations;
        this.learningRate = learningRate;
    }

    /**
     * Constructor with default parameters
     */
    public tSNE() {
        this(2, 30, 1000, 200.0);
    }

    /**
     * Fit and transform data to low-dimensional embedding
     *
     * @param X High-dimensional data of shape [n_samples, n_features]
     * @return Low-dimensional embedding of shape [n_samples, n_components]
     */
    public NdArray fitTransform(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;
        int nFeatures = Xdata[0].length;

        // Step 1 & 2 & 3: Compute pairwise distances and convert to probabilities
        this.P = computeGaussianPerplexity(Xdata, nSamples, nFeatures);

        // Step 4: Initialize low-dimensional embedding randomly
        // Using standard normal * 0.0001 as suggested in the paper
        this.Y = new double[nSamples][nComponents];
        java.util.Random random = new java.util.Random(42);
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nComponents; j++) {
                Y[i][j] = random.nextGaussian() * 0.0001;
            }
        }

        // Step 5, 6, 7: Gradient descent optimization
        double momentum = 0.8;
        double[][] gains = new double[nSamples][nComponents];
        double[][] velocities = new double[nSamples][nComponents];

        // Initialize gains
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nComponents; j++) {
                gains[i][j] = 1.0;
            }
        }

        int earlyExaggerationIter = 100;
        double earlyExaggeration = 4.0;

        for (int iter = 0; iter < nIterations; iter++) {
            // Compute q_ij (Student-t distribution with df=1)
            double[][] Q = computeStudentT(Y, nSamples);

            // Compute gradient
            double[][] gradients = computeGradient(P, Q, Y, nSamples);

            // Apply early exaggeration
            boolean usingExaggeration = iter < earlyExaggerationIter;
            double mult = usingExaggeration ? earlyExaggeration : 1.0;

            // Update embedding with momentum and gains
            for (int i = 0; i < nSamples; i++) {
                for (int j = 0; j < nComponents; j++) {
                    double grad = gradients[i][j] * mult;

                    // Update gains (AdaGrad-like)
                    if (grad * velocities[i][j] > 0) {
                        gains[i][j] += 0.2;
                    } else {
                        gains[i][j] *= 0.8;
                    }
                    gains[i][j] = Math.max(gains[i][j], 0.01);

                    // Update velocity with momentum
                    double lr = usingExaggeration ? learningRate * 0.25 : learningRate;
                    velocities[i][j] = momentum * velocities[i][j] - lr * gains[i][j] * grad;
                    Y[i][j] += velocities[i][j];
                }
            }

            // Center embedding
            double[] mean = new double[nComponents];
            for (int i = 0; i < nSamples; i++) {
                for (int j = 0; j < nComponents; j++) {
                    mean[j] += Y[i][j];
                }
            }
            for (int j = 0; j < nComponents; j++) {
                mean[j] /= nSamples;
            }
            for (int i = 0; i < nSamples; i++) {
                for (int j = 0; j < nComponents; j++) {
                    Y[i][j] -= mean[j];
                }
            }
        }

        // Return final embedding
        return NdArray.create(flatten2D(Y), DType.FLOAT64, new Shape(nSamples, nComponents));
    }

    /**
     * Compute Gaussian perplexity for each point
     */
    private double[][] computeGaussianPerplexity(double[][] X, int nSamples, int nFeatures) {
        double[][] P = new double[nSamples][nSamples];

        // Compute pairwise squared distances
        double[][] dists = new double[nSamples][nSamples];
        for (int i = 0; i < nSamples; i++) {
            for (int j = i + 1; j < nSamples; j++) {
                double sum = 0.0;
                for (int k = 0; k < nFeatures; k++) {
                    double diff = X[i][k] - X[j][k];
                    sum += diff * diff;
                }
                dists[i][j] = sum;
                dists[j][i] = sum;
            }
        }

        // For each point i, find sigma such that perplexity matches desired value
        for (int i = 0; i < nSamples; i++) {
            double targetPerplexity = this.perplexity;

            // Binary search for sigma
            double sigmaMin = 1e-10;
            double sigmaMax = 1e10;
            double sigma = 1.0;

            for (int searchIter = 0; searchIter < 50; searchIter++) {
                sigma = Math.sqrt(sigmaMin * sigmaMax); // geometric mean

                // Compute conditional probabilities
                double sum = 0.0;
                double entropy = 0.0;

                for (int j = 0; j < nSamples; j++) {
                    if (i != j) {
                        double p = Math.exp(-dists[i][j] / (2 * sigma * sigma));
                        P[i][j] = p;
                        sum += p;
                    }
                }

                // Normalize
                for (int j = 0; j < nSamples; j++) {
                    if (i != j) {
                        P[i][j] /= sum;
                        if (P[i][j] > 1e-10) {
                            entropy -= P[i][j] * Math.log(P[i][j]);
                        }
                    }
                }

                double perplexity = Math.exp(entropy);

                if (perplexity > targetPerplexity) {
                    sigmaMax = sigma;
                } else {
                    sigmaMin = sigma;
                }

                if (Math.abs(perplexity - targetPerplexity) < 1e-5) {
                    break;
                }
            }
        }

        // Symmetrize: P = (P_ij + P_ji) / (2n)
        double[][] Pjoint = new double[nSamples][nSamples];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nSamples; j++) {
                Pjoint[i][j] = (P[i][j] + P[j][i]) / (2.0 * nSamples);
            }
        }

        return Pjoint;
    }

    /**
     * Compute Student-t distribution probabilities q_ij
     */
    private double[][] computeStudentT(double[][] Y, int nSamples) {
        double[][] Q = new double[nSamples][nSamples];
        double sum = 0.0;

        for (int i = 0; i < nSamples; i++) {
            for (int j = i + 1; j < nSamples; j++) {
                double distSq = 0.0;
                for (int k = 0; k < nComponents; k++) {
                    double diff = Y[i][k] - Y[j][k];
                    distSq += diff * diff;
                }
                double q = 1.0 / (1.0 + distSq);  // Student-t with df=1
                Q[i][j] = q;
                Q[j][i] = q;
                sum += 2.0 * q;
            }
        }

        // Normalize
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nSamples; j++) {
                Q[i][j] /= sum;
            }
        }

        return Q;
    }

    /**
     * Compute gradient of KL divergence
     * dL/dy_i = 4 * sum_j (p_ij - q_ij) * (y_i - y_j) * (1 + ||y_i - y_j||^2)^(-1)
     */
    private double[][] computeGradient(double[][] P, double[][] Q, double[][] Y, int nSamples) {
        double[][] gradients = new double[nSamples][nComponents];

        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nSamples; j++) {
                if (i == j) continue;

                double distSq = 0.0;
                for (int k = 0; k < nComponents; k++) {
                    double diff = Y[i][k] - Y[j][k];
                    distSq += diff * diff;
                }

                double mult = 4.0 * (P[i][j] - Q[i][j]) * (1.0 / (1.0 + distSq));

                for (int k = 0; k < nComponents; k++) {
                    gradients[i][k] += mult * (Y[i][k] - Y[j][k]);
                }
            }
        }

        return gradients;
    }

    /**
     * Convert NdArray to 2D double array
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
     * Flatten 2D double array to 1D for NdArray creation
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

package com.zifang.util.ml.decomposition;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Uniform Manifold Approximation and Projection (UMAP)
 * <p>
 * A dimensionality reduction technique that can be used for visualisation
 * and general unsupervised learning tasks.
 * <p>
 * Algorithm steps:
 * 1. Compute fuzzy simplicial set (k-NN graph with membership/simplicial probabilities)
 * 2. Use smooth k-NN distances for local metric learning
 * 3. Compute cross-entropy between local fuzzy sets
 * 4. Initialize embedding (use Laplacian Eigenmaps initialization or spectral layout)
 * 5. Optimize embedding via stochastic gradient descent
 * 6. Binary cross-entropy loss with repulsion term
 * 7. Use Student-t (like t-SNE) in low-dim space for final embedding
 * <p>
 * Simplified implementation using fuzzy set approach with binary cross-entropy optimization.
 */
public class UMAP {
    private int nComponents;
    private int nNeighbors;
    private double minDist;
    private double learningRate;
    private int nIterations;

    /**
     * Constructor with parameters
     *
     * @param nComponents  Number of dimensions for embedding (default 2)
     * @param nNeighbors   Number of neighbors for k-NN graph (default 15)
     * @param minDist      Minimum distance between points in embedding (default 0.1)
     * @param learningRate Learning rate (default 1.0)
     * @param nIterations  Number of iterations (default 200)
     */
    public UMAP(int nComponents, int nNeighbors, double minDist, double learningRate, int nIterations) {
        this.nComponents = nComponents;
        this.nNeighbors = nNeighbors;
        this.minDist = minDist;
        this.learningRate = learningRate;
        this.nIterations = nIterations;
    }

    /**
     * Constructor with default parameters
     */
    public UMAP() {
        this(2, 15, 0.1, 1.0, 200);
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

        // Step 1: Compute k-NN graph and fuzzy simplicial set
        double[][] knnDistances = new double[nSamples][nNeighbors];
        int[][] knnIndices = new int[nSamples][nNeighbors];
        computeKNN(Xdata, nSamples, nFeatures, knnIndices, knnDistances);

        // Step 2: Compute fuzzy set membership probabilities
        double[][] A = computeFuzzySimplicialSet(Xdata, knnIndices, knnDistances, nSamples);

        // Step 3: Compute initialization using spectral layout
        double[][] Y = initializeEmbedding(A, nSamples, nComponents);

        // Step 4: Optimize embedding via SGD
        Y = optimizeEmbedding(Y, A, nSamples);

        // Return final embedding
        return NdArray.create(flatten2D(Y), DType.FLOAT64, new Shape(nSamples, nComponents));
    }

    /**
     * Compute k-nearest neighbors
     */
    private void computeKNN(double[][] X, int nSamples, int nFeatures,
                            int[][] knnIndices, double[][] knnDistances) {
        java.util.Random random = new java.util.Random(42);

        for (int i = 0; i < nSamples; i++) {
            // Use random projection for approximate k-NN in high dimensions
            // or compute exact distances if nSamples is small
            double[] distances = new double[nSamples];
            for (int j = 0; j < nSamples; j++) {
                if (i == j) {
                    distances[j] = Double.MAX_VALUE;
                } else {
                    distances[j] = euclideanDistanceSquared(X[i], X[j]);
                }
            }

            // Find k nearest neighbors using partial sort
            Integer[] indices = new Integer[nSamples];
            for (int j = 0; j < nSamples; j++) indices[j] = j;
            java.util.Arrays.sort(indices, (a, b) -> Double.compare(distances[a], distances[b]));

            for (int j = 0; j < nNeighbors; j++) {
                knnIndices[i][j] = indices[j];
                knnDistances[i][j] = distances[indices[j]];
            }
        }
    }

    /**
     * Compute fuzzy simplicial set membership probabilities
     */
    private double[][] computeFuzzySimplicialSet(double[][] X, int[][] knnIndices,
                                                 double[][] knnDistances, int nSamples) {
        double[][] A = new double[nSamples][nSamples];
        double[][] sigmas = new double[nSamples][1];
        double[][] rhos = new double[nSamples][1];

        // Compute sigma (bandwidth) for each point based on k-NN distances
        for (int i = 0; i < nSamples; i++) {
            // Find rho (distance to nearest neighbor)
            double rho = Math.sqrt(knnDistances[i][0]);
            rhos[i][0] = rho;

            // Compute sigma using the formula: sum_i exp(-(d(i,j) - rho) / sigma) = k
            // Simplified: use the k-NN distance as sigma
            double sigma = Math.sqrt(knnDistances[i][nNeighbors - 1]) + 1e-10;
            sigmas[i][0] = sigma;

            // Compute membership strength to each neighbor
            for (int j = 0; j < nNeighbors; j++) {
                int neighbor = knnIndices[i][j];
                double dist = Math.sqrt(knnDistances[i][j]);
                double a_ij = Math.exp(-(dist - rho) / sigma);
                A[i][neighbor] = Math.max(A[i][neighbor], a_ij);
                A[neighbor][i] = Math.max(A[neighbor][i], a_ij);
            }
        }

        // Symmetrize and normalize
        for (int i = 0; i < nSamples; i++) {
            for (int j = i + 1; j < nSamples; j++) {
                double aij = (A[i][j] + A[j][i]) / 2.0;
                A[i][j] = aij;
                A[j][i] = aij;
            }
        }

        return A;
    }

    /**
     * Initialize embedding using spectral layout
     */
    private double[][] initializeEmbedding(double[][] A, int nSamples, int nComponents) {
        // Simplified initialization: use spectral embedding via eigendecomposition of Laplacian
        // L = I - D^(-1/2) A D^(-1/2) where D is diagonal degree matrix

        // Compute degree matrix
        double[] D = new double[nSamples];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nSamples; j++) {
                D[i] += A[i][j];
            }
        }

        // Compute normalized Laplacian
        double[][] L = new double[nSamples][nSamples];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nSamples; j++) {
                if (i == j) {
                    L[i][j] = 1.0;
                } else if (D[i] > 0 && D[j] > 0) {
                    L[i][j] = -A[i][j] / Math.sqrt(D[i] * D[j]);
                }
            }
        }

        // For simplicity, use random initialization with spectral-like spreading
        double[][] Y = new double[nSamples][nComponents];
        java.util.Random random = new java.util.Random(42);

        // Try to use first few eigenvectors approximately
        // Simplified: use random projection scaled by degree
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nComponents; j++) {
                Y[i][j] = (random.nextGaussian() * 0.1) / Math.sqrt(D[i] + 1.0);
            }
        }

        return Y;
    }

    /**
     * Optimize embedding using binary cross-entropy loss with repulsion
     */
    private double[][] optimizeEmbedding(double[][] Y, double[][] A, int nSamples) {
        double[][] YGrad = new double[nSamples][nComponents];
        double learningRate = this.learningRate;

        java.util.Random random = new java.util.Random(42);

        for (int iter = 0; iter < nIterations; iter++) {
            // Decay learning rate
            double lr = learningRate * Math.exp(-iter * 0.05);

            // Sample edges for stochastic gradient
            int nEdges = Math.min(10 * nNeighbors * nSamples / 2, nSamples * nSamples / 2);

            for (int e = 0; e < nEdges; e++) {
                // Sample a random edge (i, j)
                int i = random.nextInt(nSamples);
                int j = random.nextInt(nSamples);
                if (i == j) continue;

                // Compute distance in embedding
                double distSq = 0.0;
                for (int k = 0; k < nComponents; k++) {
                    double diff = Y[i][k] - Y[j][k];
                    distSq += diff * diff;
                }
                double dist = Math.sqrt(distSq);

                // Compute q_ij using Student-t (UMAP uses different formulation)
                // q_ij = (1 + dist^2)^(-1)
                double q_ij = 1.0 / (1.0 + distSq);

                // Binary cross-entropy gradient
                // d/dy_i = sum_j [(p_ij - q_ij) * (1 + dist^2)^(-1) * 2 * (y_i - y_j)]
                // where p_ij is the high-dimensional probability
                double p_ij = A[i][j];

                // Repulsion and attraction terms
                double a = 1.0;
                double b = 1.0;
                double repulsionCoeff = -2.0 * a * b * q_ij * q_ij / (1.0 + distSq);
                double attractionCoeff = 2.0 * (p_ij - q_ij) * q_ij;

                for (int k = 0; k < nComponents; k++) {
                    double yi_yj = Y[i][k] - Y[j][k];
                    YGrad[i][k] += lr * (attractionCoeff + repulsionCoeff) * yi_yj;
                }
            }

            // Apply gradients
            for (int i = 0; i < nSamples; i++) {
                for (int k = 0; k < nComponents; k++) {
                    Y[i][k] -= YGrad[i][k];
                }
            }

            // Apply min_dist constraint (push apart if too close)
            applyMinDist(Y, nSamples);

            // Clear gradients
            for (int i = 0; i < nSamples; i++) {
                for (int k = 0; k < nComponents; k++) {
                    YGrad[i][k] = 0.0;
                }
            }
        }

        return Y;
    }

    /**
     * Apply min_dist constraint to prevent points from getting too close
     */
    private void applyMinDist(double[][] Y, int nSamples) {
        java.util.Random random = new java.util.Random(42);

        for (int i = 0; i < nSamples; i++) {
            // Add small repulsion if points are too close
            for (int j = i + 1; j < nSamples; j++) {
                double distSq = 0.0;
                for (int k = 0; k < nComponents; k++) {
                    double diff = Y[i][k] - Y[j][k];
                    distSq += diff * diff;
                }

                if (distSq < minDist * minDist) {
                    double push = (minDist - Math.sqrt(distSq)) * 0.1;
                    for (int k = 0; k < nComponents; k++) {
                        Y[i][k] += (random.nextDouble() - 0.5) * push;
                        Y[j][k] += (random.nextDouble() - 0.5) * push;
                    }
                }
            }
        }
    }

    /**
     * Compute squared Euclidean distance between two points
     */
    private double euclideanDistanceSquared(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return sum;
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

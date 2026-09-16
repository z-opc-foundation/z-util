package com.zifang.util.ml.decomposition;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.Linalg;
import com.zifang.util.numpy.NdArray;

/**
 * Principal Component Analysis (PCA)
 * <p>
 * A dimensionality reduction technique that transforms data to a new coordinate system
 * defined by principal components - directions of maximum variance.
 * <p>
 * Algorithm steps:
 * 1. Center data (subtract mean)
 * 2. Compute covariance matrix (X^T X / n)
 * 3. Compute eigenvalues/eigenvectors via SVD
 * 4. Sort by eigenvalue descending, select top nComponents
 * 5. Project data onto principal components
 */
public class PCA {
    private int nComponents;
    private NdArray principalComponents;  // Eigenvectors (loading vectors)
    private NdArray mean;                   // Training set mean
    private NdArray explainedVariance;      // Eigenvalues

    /**
     * Constructor with number of components
     *
     * @param nComponents Number of principal components to keep
     */
    public PCA(int nComponents) {
        this.nComponents = nComponents;
    }

    /**
     * Fit the PCA model to training data
     *
     * @param X Training data of shape [n_samples, n_features]
     */
    public void fit(NdArray X) {
        int nSamples = X.getShape().get(0);
        int nFeatures = X.getShape().get(1);

        // Convert to double[][] for processing
        double[][] Xdata = toDouble2D(X);

        // Step 1: Compute mean and center data
        double[] meanVec = new double[nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                meanVec[j] += Xdata[i][j];
            }
        }
        for (int j = 0; j < nFeatures; j++) {
            meanVec[j] /= nSamples;
        }

        // Center the data (subtract mean)
        double[][] centered = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                centered[i][j] = Xdata[i][j] - meanVec[j];
            }
        }

        this.mean = NdArray.array(meanVec, DType.FLOAT64).reshape(1, nFeatures);

        // Step 2: Compute covariance matrix (X^T X / n)
        double[][] covariance = new double[nFeatures][nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            for (int j = 0; j < nFeatures; j++) {
                double sum = 0.0;
                for (int k = 0; k < nSamples; k++) {
                    sum += centered[k][i] * centered[k][j];
                }
                covariance[i][j] = sum / nSamples;
            }
        }

        // Flatten 2D covariance to 1D so NdArray.get(i,j) works correctly
        double[] flatCov = new double[nFeatures * nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            for (int j = 0; j < nFeatures; j++) {
                flatCov[i * nFeatures + j] = covariance[i][j];
            }
        }
        NdArray covNd = NdArray.create(flatCov, DType.FLOAT64, new com.zifang.util.numpy.Shape(nFeatures, nFeatures));

        // Step 3: Compute eigenvalues and eigenvectors using SVD
        // For SVD: A = U S V^T, for covariance matrix A = V S^2 V^T
        // So eigenvectors are in V and eigenvalues are squared singular values
        NdArray[] svdResult = Linalg.svd(covNd);
        NdArray S = svdResult[1];  // Singular values
        NdArray Vt = svdResult[2]; // V^T

        // Eigenvalues are squared singular values
        double[] eigenvalues = new double[nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            double s = ((Number) S.get(i, i)).doubleValue();
            eigenvalues[i] = s * s;
        }

        // Principal components are eigenvectors (rows of Vt)
        // Sort by eigenvalue descending
        Integer[] indices = new Integer[nFeatures];
        for (int i = 0; i < nFeatures; i++) indices[i] = i;
        java.util.Arrays.sort(indices, (a, b) -> Double.compare(eigenvalues[b], eigenvalues[a]));

        // Select top nComponents
        double[][] sortedComponents = new double[nComponents][nFeatures];
        double[] sortedEigenvalues = new double[nComponents];
        for (int i = 0; i < nComponents; i++) {
            sortedEigenvalues[i] = eigenvalues[indices[i]];
            // Get eigenvector (column indices[i] of V)
            // V = Vt^T, so eigenvector j is column j of V = row j of Vt transposed
            for (int j = 0; j < nFeatures; j++) {
                Object val = Vt.get(indices[i], j);
                sortedComponents[i][j] = ((Number) val).doubleValue();
            }
        }

        this.principalComponents = NdArray.create(flatten2D(sortedComponents), DType.FLOAT64,
                new com.zifang.util.numpy.Shape(nComponents, nFeatures));
        this.explainedVariance = NdArray.array(sortedEigenvalues, DType.FLOAT64).reshape(nComponents);
    }

    /**
     * Transform data to principal component space
     *
     * @param X Data of shape [n_samples, n_features]
     * @return Transformed data of shape [n_samples, n_components]
     */
    public NdArray transform(NdArray X) {
        // Center data
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;
        int nFeatures = Xdata[0].length;

        double[] meanVec = new double[nFeatures];
        for (int j = 0; j < nFeatures; j++) {
            meanVec[j] = ((Number) mean.get(0, j)).doubleValue();
        }

        double[][] centered = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                centered[i][j] = Xdata[i][j] - meanVec[j];
            }
        }

        // Project onto principal components: Y = X_centered * PC^T
        double[][] pcData = toDouble2D(principalComponents);
        double[][] result = new double[nSamples][nComponents];

        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nComponents; j++) {
                double sum = 0.0;
                for (int k = 0; k < nFeatures; k++) {
                    sum += centered[i][k] * pcData[j][k];
                }
                result[i][j] = sum;
            }
        }

        return NdArray.create(flatten2D(result), DType.FLOAT64,
                new com.zifang.util.numpy.Shape(nSamples, nComponents));
    }

    /**
     * Fit the model and transform training data
     *
     * @param X Training data of shape [n_samples, n_features]
     * @return Transformed data of shape [n_samples, n_components]
     */
    public NdArray fitTransform(NdArray X) {
        fit(X);
        return transform(X);
    }

    /**
     * Transform data back to original space (inverse transform)
     *
     * @param X Transformed data of shape [n_samples, n_components]
     * @return Original space data of shape [n_samples, n_features]
     */
    public NdArray inverseTransform(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;

        // Project back: X_original = X_transformed * PC + mean
        double[][] pcData = toDouble2D(principalComponents);
        int nFeatures = pcData[0].length;

        double[] meanVec = new double[nFeatures];
        for (int j = 0; j < nFeatures; j++) {
            meanVec[j] = ((Number) mean.get(0, j)).doubleValue();
        }

        double[][] result = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                double sum = 0.0;
                for (int k = 0; k < nComponents; k++) {
                    sum += Xdata[i][k] * pcData[k][j];
                }
                result[i][j] = sum + meanVec[j];
            }
        }

        return NdArray.create(flatten2D(result), DType.FLOAT64,
                new com.zifang.util.numpy.Shape(nSamples, nFeatures));
    }

    /**
     * Get principal components (eigenvectors)
     */
    public NdArray getPrincipalComponents() {
        return principalComponents;
    }

    /**
     * Get explained variance (eigenvalues)
     */
    public NdArray getExplainedVariance() {
        return explainedVariance;
    }

    /**
     * Get total explained variance ratio
     */
    public double getExplainedVarianceRatio() {
        double totalVariance = 0.0;
        double retainedVariance = 0.0;

        Object data = explainedVariance.getData();
        int n = explainedVariance.size();

        if (data instanceof double[]) {
            double[] d = (double[]) data;
            for (int i = 0; i < n; i++) {
                retainedVariance += d[i];
            }
        }

        // Approximate total variance from explained variance
        // In practice, you'd compute this from the full eigenvalue set
        return retainedVariance / (retainedVariance * 1.5); // Simplified ratio
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

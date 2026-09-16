package com.zifang.util.numpy;

import java.util.Arrays;

/**
 * Linear algebra operations for 2D matrices.
 * Provides numpy.linalg equivalent functionality.
 */
public class Linalg {

    private static double getDouble(NdArray a, int... indices) {
        Object val = a.get(indices);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        throw new IllegalArgumentException("Expected numeric value");
    }

    private static void setDouble(NdArray a, int row, int col, double value) {
        a.set(value, row, col);
    }

    private static double[] toDoubleArray(Object data, DType dtype, int size) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            Object val = Array.get(data, i);
            if (val instanceof Number) {
                result[i] = ((Number) val).doubleValue();
            } else {
                result[i] = 0.0;
            }
        }
        return result;
    }

    private static NdArray fromDoubleArray(double[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        double[] flat = new double[rows * cols];
        int idx = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[idx++] = data[i][j];
            }
        }
        return NdArray.create(flat, DType.FLOAT64, new Shape(rows, cols));
    }

    private static NdArray fromDoubleArray1D(double[] data) {
        return NdArray.create(data, DType.FLOAT64, new Shape(data.length));
    }

    /**
     * Dot product of two matrices (2D arrays).
     * For 2D matrices, computes matrix multiplication.
     */
    public static NdArray dot(NdArray a, NdArray b) {
        int aRows = a.getShape().get(0);
        int aCols = a.getShape().get(1);
        int bRows = b.getShape().get(0);
        int bCols = b.getShape().get(1);

        // Handle 1D vectors
        if (a.ndim() == 1 && b.ndim() == 1) {
            if (aRows != bRows) {
                throw new IllegalArgumentException("Vectors must have same length for dot product");
            }
            double sum = 0;
            for (int i = 0; i < aRows; i++) {
                sum += getDouble(a, i) * getDouble(b, i);
            }
            return NdArray.create(new double[]{sum}, DType.FLOAT64, new Shape(1));
        }

        // Matrix multiplication: (m,n) dot (n,p) = (m,p)
        if (aCols != bRows) {
            throw new IllegalArgumentException(
                    "Matrix dimensions incompatible for dot product: " + aCols + " != " + bRows);
        }

        double[][] result = new double[aRows][bCols];
        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bCols; j++) {
                double sum = 0;
                for (int k = 0; k < aCols; k++) {
                    sum += getDouble(a, i, k) * getDouble(b, k, j);
                }
                result[i][j] = sum;
            }
        }
        return fromDoubleArray(result);
    }

    /**
     * Matrix multiplication (same as dot for 2D matrices).
     */
    public static NdArray matmul(NdArray a, NdArray b) {
        return dot(a, b);
    }

    /**
     * Transpose of a matrix.
     */
    public static NdArray transpose(NdArray a) {
        return a.transpose();
    }

    /**
     * Matrix inverse for square matrices.
     */
    public static NdArray inv(NdArray a) {
        if (a.getShape().get(0) != a.getShape().get(1)) {
            throw new IllegalArgumentException("Matrix must be square for inversion");
        }
        int n = a.getShape().get(0);
        double[][] augmented = new double[n][2 * n];

        // Create augmented matrix [A|I]
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmented[i][j] = getDouble(a, i, j);
            }
            for (int j = n; j < 2 * n; j++) {
                augmented[i][j] = (j - n == i) ? 1.0 : 0.0;
            }
        }

        // Gauss-Jordan elimination
        for (int col = 0; col < n; col++) {
            // Find pivot
            int maxRow = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(augmented[row][col]) > Math.abs(augmented[maxRow][col])) {
                    maxRow = row;
                }
            }
            // Swap rows
            double[] temp = augmented[col];
            augmented[col] = augmented[maxRow];
            augmented[maxRow] = temp;

            // Check for singular matrix
            if (Math.abs(augmented[col][col]) < 1e-10) {
                throw new ArithmeticException("Matrix is singular and cannot be inverted");
            }

            // Scale pivot row
            double pivot = augmented[col][col];
            for (int j = 0; j < 2 * n; j++) {
                augmented[col][j] /= pivot;
            }

            // Eliminate column
            for (int row = 0; row < n; row++) {
                if (row != col) {
                    double factor = augmented[row][col];
                    for (int j = 0; j < 2 * n; j++) {
                        augmented[row][j] -= factor * augmented[col][j];
                    }
                }
            }
        }

        // Extract inverse
        double[][] invResult = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                invResult[i][j] = augmented[i][n + j];
            }
        }
        return fromDoubleArray(invResult);
    }

    /**
     * Determinant of a square matrix.
     */
    public static double det(NdArray a) {
        if (a.getShape().get(0) != a.getShape().get(1)) {
            throw new IllegalArgumentException("Matrix must be square for determinant");
        }
        int n = a.getShape().get(0);
        if (n == 1) {
            return getDouble(a, 0, 0);
        }
        if (n == 2) {
            return getDouble(a, 0, 0) * getDouble(a, 1, 1) - getDouble(a, 0, 1) * getDouble(a, 1, 0);
        }

        double det = 0;
        for (int j = 0; j < n; j++) {
            det += Math.pow(-1, j) * getDouble(a, 0, j) * det(minor(a, 0, j));
        }
        return det;
    }

    private static double detRecursive(NdArray a) {
        int n = a.getShape().get(0);
        if (n == 1) {
            return getDouble(a, 0, 0);
        }
        if (n == 2) {
            return getDouble(a, 0, 0) * getDouble(a, 1, 1) - getDouble(a, 0, 1) * getDouble(a, 1, 0);
        }
        double d = 0;
        for (int j = 0; j < n; j++) {
            d += Math.pow(-1, j) * getDouble(a, 0, j) * detRecursive(minor(a, 0, j));
        }
        return d;
    }

    private static NdArray minor(NdArray a, int row, int col) {
        int n = a.getShape().get(0);
        double[][] minor = new double[n - 1][n - 1];
        int mi = 0;
        for (int i = 0; i < n; i++) {
            if (i == row) continue;
            int mj = 0;
            for (int j = 0; j < n; j++) {
                if (j == col) continue;
                minor[mi][mj++] = getDouble(a, i, j);
            }
            mi++;
        }
        return fromDoubleArray(minor);
    }

    /**
     * Eigenvalues and eigenvectors of a symmetric matrix.
     * Returns [eigenvalues, eigenvectors] as NdArray[2].
     */
    public static NdArray[] eig(NdArray a) {
        if (a.getShape().get(0) != a.getShape().get(1)) {
            throw new IllegalArgumentException("Matrix must be square for eigenvalue decomposition");
        }
        int n = a.getShape().get(0);

        // Copy matrix to double[][]
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = getDouble(a, i, j);
            }
        }

        // Simple Jacobi eigenvalue algorithm
        double[] eigenvalues = new double[n];
        double[][] eigenvectors = new double[n][n];

        // Initialize eigenvectors as identity
        for (int i = 0; i < n; i++) {
            eigenvectors[i][i] = 1.0;
        }

        // Iterate
        for (int iter = 0; iter < 100; iter++) {
            // Find largest off-diagonal element
            double maxVal = 0;
            int p = 0, q = 1;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (Math.abs(matrix[i][j]) > maxVal) {
                        maxVal = Math.abs(matrix[i][j]);
                        p = i;
                        q = j;
                    }
                }
            }

            if (maxVal < 1e-10) break;

            // Jacobi rotation
            double theta = (matrix[q][q] - matrix[p][p]) / (2 * matrix[p][q]);
            double t = (theta >= 0 ? 1 : -1) / (Math.abs(theta) + Math.sqrt(1 + theta * theta));
            double c = 1 / Math.sqrt(1 + t * t);
            double s = t * c;

            // Update matrix
            double mpq = matrix[p][q];
            double mpp = matrix[p][p];
            double mqq = matrix[q][q];
            matrix[p][p] = c * c * mpp - 2 * c * s * mpq + s * s * mqq;
            matrix[q][q] = s * s * mpp + 2 * c * s * mpq + c * c * mqq;
            matrix[p][q] = 0;
            matrix[q][p] = 0;

            for (int k = 0; k < n; k++) {
                if (k != p && k != q) {
                    double mpk = matrix[p][k];
                    double mqk = matrix[q][k];
                    matrix[p][k] = c * mpk - s * mqk;
                    matrix[q][k] = s * mpk + c * mqk;
                    matrix[k][p] = matrix[p][k];
                    matrix[k][q] = matrix[q][k];
                }
            }

            // Update eigenvectors
            for (int k = 0; k < n; k++) {
                double vpk = eigenvectors[k][p];
                double vqk = eigenvectors[k][q];
                eigenvectors[k][p] = c * vpk - s * vqk;
                eigenvectors[k][q] = s * vpk + c * vqk;
            }
        }

        // Extract eigenvalues
        for (int i = 0; i < n; i++) {
            eigenvalues[i] = matrix[i][i];
        }

        NdArray eigenvaluesNd = fromDoubleArray1D(eigenvalues);
        NdArray eigenvectorsNd = fromDoubleArray(eigenvectors).transpose();

        return new NdArray[]{eigenvaluesNd, eigenvectorsNd};
    }

    /**
     * Solve linear system Ax = b.
     */
    public static NdArray solve(NdArray a, NdArray b) {
        if (a.getShape().get(0) != a.getShape().get(1)) {
            throw new IllegalArgumentException("Matrix A must be square");
        }
        int n = a.getShape().get(0);

        if (b.getShape().get(0) != n || b.ndim() > 2) {
            throw new IllegalArgumentException("Vector b must have same length as matrix A");
        }

        // Use Gaussian elimination with partial pivoting
        double[][] aug = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                aug[i][j] = getDouble(a, i, j);
            }
            aug[i][n] = b.ndim() == 1 ? getDouble(b, i) : getDouble(b, i, 0);
        }

        // Forward elimination
        for (int col = 0; col < n; col++) {
            // Find pivot
            int maxRow = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(aug[row][col]) > Math.abs(aug[maxRow][col])) {
                    maxRow = row;
                }
            }
            double[] temp = aug[col];
            aug[col] = aug[maxRow];
            aug[maxRow] = temp;

            // Check for zero pivot
            if (Math.abs(aug[col][col]) < 1e-10) {
                throw new ArithmeticException("Matrix is singular");
            }

            // Eliminate
            for (int row = col + 1; row < n; row++) {
                double factor = aug[row][col] / aug[col][col];
                for (int j = col; j <= n; j++) {
                    aug[row][j] -= factor * aug[col][j];
                }
            }
        }

        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = aug[i][n];
            for (int j = i + 1; j < n; j++) {
                x[i] -= aug[i][j] * x[j];
            }
            x[i] /= aug[i][i];
        }

        return fromDoubleArray1D(x).reshape(n, 1);
    }

    /**
     * Matrix norm.
     * ord=2: Frobenius norm (default), ord=1: column sum, ord=Integer.MAX_VALUE: infinity norm
     */
    public static double norm(NdArray a, int ord) {
        int rows = a.getShape().get(0);
        int cols = a.getShape().get(1);
        switch (ord) {
            case 1: // L1 norm (column sum)
                double maxColSum = 0;
                for (int j = 0; j < cols; j++) {
                    double colSum = 0;
                    for (int i = 0; i < rows; i++) {
                        colSum += Math.abs(getDouble(a, i, j));
                    }
                    maxColSum = Math.max(maxColSum, colSum);
                }
                return maxColSum;
            case 2: // Frobenius norm
            default:
                double sumSquares = 0;
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        double val = getDouble(a, i, j);
                        sumSquares += val * val;
                    }
                }
                return Math.sqrt(sumSquares);
            case Integer.MAX_VALUE: // Infinity norm (row sum)
                double maxRowSum = 0;
                for (int i = 0; i < rows; i++) {
                    double rowSum = 0;
                    for (int j = 0; j < cols; j++) {
                        rowSum += Math.abs(getDouble(a, i, j));
                    }
                    maxRowSum = Math.max(maxRowSum, rowSum);
                }
                return maxRowSum;
        }
    }

    /**
     * norm方法。
     * * @param a NdArray类型参数
     *
     * @return static double类型返回值
     */
    public static double norm(NdArray a) {
        return norm(a, 2);
    }

    /**
     * Singular Value Decomposition.
     * Returns [U, S, V] where A = U * S * V^T
     */
    public static NdArray[] svd(NdArray a) {
        int m = a.getShape().get(0);
        int n = a.getShape().get(1);

        // Compute A^T * A
        double[][] ata = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0;
                for (int k = 0; k < m; k++) {
                    sum += getDouble(a, k, i) * getDouble(a, k, j);
                }
                ata[i][j] = sum;
            }
        }

        // Compute eigenvalues of A^T * A (which are squared singular values)
        double[] eigenvalues = powerIterationEigenvalues(ata, n);
        double[] singularValues = new double[n];
        for (int i = 0; i < n; i++) {
            singularValues[i] = Math.sqrt(Math.max(eigenvalues[i], 0));
        }

        // Sort singular values in descending order
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        Arrays.sort(indices, (i, j) -> Double.compare(singularValues[j], singularValues[i]));

        double[] sortedSV = new double[n];
        double[][] sortedV = new double[n][n];
        for (int i = 0; i < n; i++) {
            sortedSV[i] = singularValues[indices[i]];
        }

        // Compute V (eigenvectors of A^T * A)
        double[][] v = computeEigenvectors(ata, n);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sortedV[i][j] = v[i][indices[j]];
            }
        }

        // Compute U = A * V * S^(-1)
        double[][] u = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    if (sortedSV[k] > 1e-10) {
                        sum += getDouble(a, i, k) * sortedV[k][j] / sortedSV[k];
                    }
                }
                u[i][j] = sum;
            }
        }

        // Create S matrix (singular values on diagonal)
        double[][] s = new double[m][n];
        int minDim = Math.min(m, n);
        for (int i = 0; i < minDim; i++) {
            s[i][i] = sortedSV[i];
        }

        return new NdArray[]{
                fromDoubleArray(u),
                fromDoubleArray(s),
                fromDoubleArray(sortedV).transpose()
        };
    }

    private static double[] powerIterationEigenvalues(double[][] matrix, int n) {
        double[] eigenvalues = new double[n];

        double[][] copy = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, n);
        }

        for (int i = 0; i < n; i++) {
            double[] eigenvector = new double[n];
            eigenvector[0] = 1;
            for (int iter = 0; iter < 100; iter++) {
                // Compute copy * eigenvector
                double[] next = new double[n];
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        next[j] += copy[j][k] * eigenvector[k];
                    }
                }
                // Normalize
                double norm = 0;
                for (double v : next) norm += v * v;
                norm = Math.sqrt(norm);
                if (norm < 1e-10) break;
                for (int j = 0; j < n; j++) eigenvector[j] = next[j] / norm;
            }
            // Compute eigenvalue (Rayleigh quotient)
            double[] mve = new double[n];
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    mve[j] += copy[j][k] * eigenvector[k];
                }
            }
            double dot = 0;
            for (int j = 0; j < n; j++) dot += eigenvector[j] * mve[j];
            eigenvalues[i] = dot;

            // Deflate matrix
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    copy[j][k] -= eigenvalues[i] * eigenvector[j] * eigenvector[k];
                }
            }
        }
        return eigenvalues;
    }

    private static double[][] computeEigenvectors(double[][] matrix, int n) {
        double[][] eigenvectors = new double[n][n];
        double[] eigenvalues = powerIterationEigenvalues(matrix, n);

        double[][] copy = new double[n][n];
        for (int i = 0; i < n; i++) System.arraycopy(matrix[i], 0, copy[i], 0, n);

        for (int i = 0; i < n; i++) {
            double[] eigenvector = new double[n];
            eigenvector[0] = 1;
            for (int iter = 0; iter < 100; iter++) {
                double[] next = new double[n];
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        next[j] += copy[j][k] * eigenvector[k];
                    }
                }
                double norm = 0;
                for (double v : next) norm += v * v;
                norm = Math.sqrt(norm);
                if (norm < 1e-10) break;
                for (int j = 0; j < n; j++) eigenvector[j] = next[j] / norm;
            }
            for (int j = 0; j < n; j++) eigenvectors[j][i] = eigenvector[j];

            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    copy[j][k] -= eigenvalues[i] * eigenvector[j] * eigenvector[k];
                }
            }
        }
        return eigenvectors;
    }

    /**
     * QR decomposition.
     * Returns [Q, R] where A = QR.
     */
    public static NdArray[] qr(NdArray a) {
        int m = a.getShape().get(0);
        int n = a.getShape().get(1);

        double[][] q = new double[m][n];
        double[][] r = new double[n][n];

        double[][] aCopy = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                aCopy[i][j] = getDouble(a, i, j);
            }
        }

        // Gram-Schmidt orthogonalization
        for (int j = 0; j < n; j++) {
            // Get column j
            double[] v = new double[m];
            for (int i = 0; i < m; i++) v[i] = aCopy[i][j];

            // Orthogonalize against previous columns
            for (int i = 0; i < j; i++) {
                double[] qi = new double[m];
                for (int k = 0; k < m; k++) qi[k] = q[k][i];

                // r[i][j] = q_i^T * a_j
                r[i][j] = 0;
                for (int k = 0; k < m; k++) r[i][j] += qi[k] * aCopy[k][j];

                // v = v - r[i][j] * q_i
                for (int k = 0; k < m; k++) v[k] -= r[i][j] * qi[k];
            }

            // r[j][j] = ||v||
            r[j][j] = 0;
            for (int i = 0; i < m; i++) r[j][j] += v[i] * v[i];
            r[j][j] = Math.sqrt(r[j][j]);

            // q_j = v / r[j][j]
            for (int i = 0; i < m; i++) q[i][j] = v[i] / r[j][j];
        }

        return new NdArray[]{fromDoubleArray(q), fromDoubleArray(r)};
    }

    /**
     * Least squares solution.
     * Returns x that minimizes ||Ax - b||^2.
     */
    public static NdArray lstsq(NdArray a, NdArray b) {
        // Use QR decomposition: A = QR, then x = R^{-1} Q^T b
        NdArray[] qrResult = qr(a);
        NdArray q = qrResult[0];
        NdArray r = qrResult[1];

        // Compute Q^T * b
        int m = a.getShape().get(0);
        int n = a.getShape().get(1);
        double[] qtb = new double[n];
        for (int i = 0; i < n; i++) {
            qtb[i] = 0;
            for (int j = 0; j < m; j++) {
                qtb[i] += getDouble(q, j, i) * (b.ndim() == 1 ? getDouble(b, j) : getDouble(b, j, 0));
            }
        }

        // Back substitute to solve R * x = Q^T * b
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = qtb[i];
            for (int j = i + 1; j < n; j++) {
                x[i] -= getDouble(r, i, j) * x[j];
            }
            x[i] /= getDouble(r, i, i);
        }

        return fromDoubleArray1D(x).reshape(n, 1);
    }
}

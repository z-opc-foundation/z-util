package com.zifang.util.ml.linear;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.Linalg;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

/**
 * Linear Regression using Ordinary Least Squares with optional Ridge regularization.
 * Supports multiple targets (y can be [n x m]).
 */
public class LinearRegression {

    private double lambda;
    private double[] weights;
    private double bias;
    private int nTargets;

    /**
     * Create a new Linear Regression model.
     *
     * @param lambda Ridge regularization parameter (0 for plain OLS)
     */
    public LinearRegression(double lambda) {
        this.lambda = lambda;
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
     * Convert 1D double array to column vector NdArray.
     */
    private NdArray toColumnVector(double[] arr) {
        return NdArray.create(arr, DType.FLOAT64, new Shape(arr.length, 1));
    }

    /**
     * Convert 2D double array to NdArray.
     */
    private NdArray toNdArray(double[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        double[] flat = new double[rows * cols];
        int idx = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[idx++] = arr[i][j];
            }
        }
        return NdArray.create(flat, DType.FLOAT64, new Shape(rows, cols));
    }

    /**
     * Fit the model using normal equations: w = (XᵀX + λI)⁻¹Xᵀy
     * Supports multiple targets.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @param y Target values of shape [n_samples] or [n_samples, n_targets]
     */
    public void fit(NdArray X, double[] y) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);
        this.nTargets = 1;

        // Convert y to column vector
        NdArray yVec = toColumnVector(y);

        fitMultiTarget(X, yVec);
    }

    /**
     * Fit the model with multi-target y.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @param y Target values of shape [n_samples, n_targets]
     */
    public void fit(NdArray X, NdArray y) {
        this.nTargets = y.getShape().get(1);
        fitMultiTarget(X, y);
    }

    private void fitMultiTarget(NdArray X, NdArray y) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);

        // Add bias term
        NdArray Xb = addBias(X);
        int d1 = d + 1;

        // Compute XᵀX + λI (with regularization on all but bias term)
        NdArray XtX = Linalg.dot(Xb.transpose(), Xb);

        // Add ridge regularization: λ on diagonal except for bias term (last one)
        double[] XtXFlat = new double[d1 * d1];
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d1; j++) {
                Object val = XtX.get(i, j);
                XtXFlat[i * d1 + j] = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
            }
        }

        // Apply regularization to all parameters except bias
        for (int i = 0; i < d; i++) {
            XtXFlat[i * d1 + i] += lambda;
        }

        NdArray XtXReg = NdArray.create(XtXFlat, DType.FLOAT64, new Shape(d1, d1));

        // Compute (XᵀX + λI)⁻¹
        NdArray XtXInv = Linalg.inv(XtXReg);

        // Compute Xᵀy
        NdArray Xty = Linalg.dot(Xb.transpose(), y);

        // Compute weights: w = (XᵀX + λI)⁻¹Xᵀy
        NdArray w = Linalg.dot(XtXInv, Xty);

        // Extract weights and bias
        this.weights = new double[d];
        this.bias = 0;

        for (int i = 0; i < d; i++) {
            Object val = w.get(i, 0);
            weights[i] = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
        }
        Object biasVal = w.get(d, 0);
        bias = biasVal instanceof Number ? ((Number) biasVal).doubleValue() : 0.0;
    }

    /**
     * Predict target values for samples.
     *
     * @param X Feature matrix of shape [n_samples, n_features]
     * @return NdArray of predictions of shape [n_samples] or [n_samples, n_targets]
     */
    public NdArray predict(NdArray X) {
        int n = X.getShape().get(0);
        int d = X.getShape().get(1);

        double[][] predictions;
        if (nTargets == 1) {
            predictions = new double[n][1];
        } else {
            predictions = new double[n][nTargets];
        }

        for (int i = 0; i < n; i++) {
            for (int t = 0; t < nTargets; t++) {
                double sum = bias;
                for (int j = 0; j < d; j++) {
                    Object val = X.get(i, j);
                    double x = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
                    sum += x * weights[j];
                }
                predictions[i][t] = sum;
            }
        }

        if (nTargets == 1) {
            double[] flatPred = new double[n];
            for (int i = 0; i < n; i++) {
                flatPred[i] = predictions[i][0];
            }
            return NdArray.create(flatPred, DType.FLOAT64, new Shape(n));
        } else {
            return toNdArray(predictions);
        }
    }
}

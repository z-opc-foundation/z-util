package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * K-Means clustering algorithm using Lloyd's method.
 * <p>
 * Algorithm:
 * 1. Initialize centroids (random from data, k-means++ preferred)
 * 2. Assign each point to nearest centroid (Euclidean distance)
 * 3. Update centroids as mean of assigned points
 * 4. Repeat until convergence or maxIterations
 */
public class KMeans {
    private int k;
    private int maxIterations;
    private double tolerance;
    private NdArray centroids;
    private int[] labels;

    /**
     * KMeans方法。
     * * @param k int类型参数
     *
     * @param maxIterations int类型参数
     * @param tolerance     double类型参数
     */
    public KMeans(int k, int maxIterations, double tolerance) {
        this.k = k;
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
    }

    /**
     * fit方法。
     * * @param X NdArray类型参数
     */
    public void fit(NdArray X) {
        int nSamples = X.getShape().get(0);
        int nFeatures = X.getShape().get(1);
        double[][] Xdata = toDouble2D(X);

        // Initialize centroids using k-means++ method
        this.centroids = initializeCentroids(Xdata, nSamples, nFeatures);
        double[][] centroidsData = toDouble2D(centroids);

        this.labels = new int[nSamples];

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Assign each point to nearest centroid
            int[] newLabels = new int[nSamples];
            for (int i = 0; i < nSamples; i++) {
                newLabels[i] = findNearestCentroid(Xdata[i], centroidsData);
            }

            // Check for convergence
            boolean converged = true;
            for (int i = 0; i < nSamples; i++) {
                if (newLabels[i] != labels[i]) {
                    converged = false;
                    break;
                }
            }
            this.labels = newLabels;

            if (converged) {
                break;
            }

            // Update centroids
            double[][] newCentroids = new double[k][nFeatures];
            int[] counts = new int[k];

            for (int i = 0; i < nSamples; i++) {
                int cluster = labels[i];
                for (int j = 0; j < nFeatures; j++) {
                    newCentroids[cluster][j] += Xdata[i][j];
                }
                counts[cluster]++;
            }

            // Compute mean and check convergence
            double maxChange = 0.0;
            for (int c = 0; c < k; c++) {
                if (counts[c] > 0) {
                    for (int j = 0; j < nFeatures; j++) {
                        newCentroids[c][j] /= counts[c];
                    }
                } else {
                    // Reinitialize empty cluster with random data point
                    int randomIdx = (int) (Math.random() * nSamples);
                    for (int j = 0; j < nFeatures; j++) {
                        newCentroids[c][j] = Xdata[randomIdx][j];
                    }
                }
            }

            // Calculate max centroid change for convergence check
            for (int c = 0; c < k; c++) {
                double change = euclideanDistance(centroidsData[c], newCentroids[c]);
                if (change > maxChange) {
                    maxChange = change;
                }
            }

            centroidsData = newCentroids;

            if (maxChange < tolerance) {
                break;
            }
        }

        // Store final centroids
        this.centroids = NdArray.array(flatten2D(centroidsData), DType.FLOAT64);
        this.centroids = this.centroids.reshape(k, nFeatures);
    }

    /**
     * predict方法。
     * * @param X NdArray类型参数
     *
     * @return int[]类型返回值
     */
    public int[] predict(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        double[][] centroidsData = toDouble2D(this.centroids);
        int nSamples = Xdata.length;
        int[] predictions = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            predictions[i] = findNearestCentroid(Xdata[i], centroidsData);
        }

        return predictions;
    }

    /**
     * fitPredict方法。
     * * @param X NdArray类型参数
     *
     * @return int[]类型返回值
     */
    public int[] fitPredict(NdArray X) {
        fit(X);
        return labels;
    }

    /**
     * Initialize centroids using k-means++ method for better initial placement.
     */
    private NdArray initializeCentroids(double[][] X, int nSamples, int nFeatures) {
        double[][] centroids = new double[k][nFeatures];

        // Choose first centroid randomly
        int firstIdx = (int) (Math.random() * nSamples);
        for (int j = 0; j < nFeatures; j++) {
            centroids[0][j] = X[firstIdx][j];
        }

        // Choose remaining centroids with probability proportional to distance squared
        double[] distances = new double[nSamples];
        double totalDistance;

        for (int c = 1; c < k; c++) {
            totalDistance = 0.0;
            for (int i = 0; i < nSamples; i++) {
                distances[i] = findMinDistanceSquared(X[i], centroids, c);
                totalDistance += distances[i];
            }

            // Choose next centroid with probability proportional to distance squared
            double threshold = Math.random() * totalDistance;
            double cumulative = 0.0;
            for (int i = 0; i < nSamples; i++) {
                cumulative += distances[i];
                if (cumulative >= threshold) {
                    for (int j = 0; j < nFeatures; j++) {
                        centroids[c][j] = X[i][j];
                    }
                    break;
                }
            }
        }

        return NdArray.array(flatten2D(centroids), DType.FLOAT64).reshape(k, nFeatures);
    }

    /**
     * Find the minimum squared distance from a point to existing centroids.
     */
    private double findMinDistanceSquared(double[] point, double[][] centroids, int nCentroids) {
        double minDist = Double.MAX_VALUE;
        for (int c = 0; c < nCentroids; c++) {
            double dist = euclideanDistanceSquared(point, centroids[c]);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    /**
     * Find index of nearest centroid for a given point.
     */
    private int findNearestCentroid(double[] point, double[][] centroids) {
        int nearest = 0;
        double minDist = euclideanDistance(point, centroids[0]);

        for (int c = 1; c < centroids.length; c++) {
            double dist = euclideanDistance(point, centroids[c]);
            if (dist < minDist) {
                minDist = dist;
                nearest = c;
            }
        }

        return nearest;
    }

    /**
     * Compute Euclidean distance between two points.
     */
    private double euclideanDistance(double[] a, double[] b) {
        return Math.sqrt(euclideanDistanceSquared(a, b));
    }

    /**
     * Compute squared Euclidean distance between two points.
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

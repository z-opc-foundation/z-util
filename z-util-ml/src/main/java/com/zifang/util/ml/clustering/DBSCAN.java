package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.NdArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DBSCAN (Density-Based Spatial Clustering of Applications with Noise) algorithm.
 * <p>
 * Algorithm:
 * - Core point: has >= minPts within eps radius
 * - Border point: within eps of a core point but not core
 * - Noise: neither core nor border
 * - Expand cluster from core points via density-reachability
 */
public class DBSCAN {
    private double eps;
    private int minPts;
    private int[] labels;
    private double[][] centroids;

    /**
     * DBSCAN方法。
     * * @param eps double类型参数
     *
     * @param minPts int类型参数
     */
    public DBSCAN(double eps, int minPts) {
        this.eps = eps;
        this.minPts = minPts;
    }

    /**
     * Fit DBSCAN to the data and return cluster labels.
     * Labels: -1 = noise, 0, 1, 2, ... = cluster IDs
     *
     * @param X NdArray of shape [n_samples, n_features]
     * @return cluster labels array
     */
    public int[] fit(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;

        this.labels = new int[nSamples];
        Arrays.fill(this.labels, -1); // Initialize all as noise

        int clusterId = 0;
        boolean[] visited = new boolean[nSamples];

        for (int i = 0; i < nSamples; i++) {
            if (visited[i]) {
                continue;
            }
            visited[i] = true;

            // Find neighbors within eps radius
            List<Integer> neighbors = regionQuery(Xdata, i);

            if (neighbors.size() < minPts) {
                // Not a core point, will be labeled as noise or border
                // We keep it as -1 for now, may be reassigned later
                continue;
            }

            // Expand cluster from core point
            expandCluster(Xdata, i, neighbors, clusterId, visited);
            clusterId++;
        }

        // Compute centroids for each cluster
        computeCentroids(Xdata, clusterId);

        return this.labels;
    }

    /**
     * Assign new points to nearest cluster based on fitted model.
     * Noise points (label -1) are assigned to nearest cluster or remain noise.
     *
     * @param X NdArray of shape [n_samples, n_features]
     * @return cluster labels array
     */
    public int[] predict(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;
        int[] predictions = new int[nSamples];

        // If not yet fitted, fit first
        if (this.labels == null) {
            fit(X);
            return this.labels;
        }

        // Count number of clusters
        int maxCluster = -1;
        for (int label : this.labels) {
            if (label > maxCluster) {
                maxCluster = label;
            }
        }

        if (maxCluster < 0) {
            // All noise, return noise labels
            return this.labels.clone();
        }

        // Assign each new point to nearest cluster centroid
        for (int i = 0; i < nSamples; i++) {
            predictions[i] = findNearestCluster(Xdata[i]);
        }

        return predictions;
    }

    /**
     * Expand cluster from a core point using density-reachability.
     */
    private void expandCluster(double[][] X, int pointIdx, List<Integer> neighbors,
                               int clusterId, boolean[] visited) {
        this.labels[pointIdx] = clusterId;

        List<Integer> seeds = new ArrayList<>(neighbors);
        int seedIndex = 0;

        while (seedIndex < seeds.size()) {
            int currentIdx = seeds.get(seedIndex);

            if (!visited[currentIdx]) {
                visited[currentIdx] = true;
                List<Integer> currentNeighbors = regionQuery(X, currentIdx);

                if (currentNeighbors.size() >= minPts) {
                    // It's a core point, add its neighbors to seeds
                    for (int neighbor : currentNeighbors) {
                        if (!seeds.contains(neighbor)) {
                            seeds.add(neighbor);
                        }
                    }
                }
            }

            // Assign to cluster if not already assigned
            if (this.labels[currentIdx] == -1) {
                this.labels[currentIdx] = clusterId;
            }

            seedIndex++;
        }
    }

    /**
     * Find all points within eps radius of a point (excluding the point itself).
     */
    private List<Integer> regionQuery(double[][] X, int pointIdx) {
        List<Integer> neighbors = new ArrayList<>();
        double[] point = X[pointIdx];

        for (int i = 0; i < X.length; i++) {
            if (i != pointIdx) {
                double dist = euclideanDistance(point, X[i]);
                if (dist <= eps) {
                    neighbors.add(i);
                }
            }
        }

        return neighbors;
    }

    /**
     * Compute centroids for each cluster after fitting.
     */
    private void computeCentroids(double[][] X, int nClusters) {
        this.centroids = new double[nClusters][];

        for (int c = 0; c < nClusters; c++) {
            List<double[]> clusterPoints = new ArrayList<>();

            for (int i = 0; i < X.length; i++) {
                if (this.labels[i] == c) {
                    clusterPoints.add(X[i]);
                }
            }

            if (clusterPoints.size() > 0) {
                int nFeatures = X[0].length;
                double[] centroid = new double[nFeatures];

                for (double[] point : clusterPoints) {
                    for (int j = 0; j < nFeatures; j++) {
                        centroid[j] += point[j];
                    }
                }

                for (int j = 0; j < nFeatures; j++) {
                    centroid[j] /= clusterPoints.size();
                }

                this.centroids[c] = centroid;
            }
        }
    }

    /**
     * Find nearest cluster centroid for a point.
     */
    private int findNearestCluster(double[] point) {
        int nearest = -1;
        double minDist = Double.MAX_VALUE;

        for (int c = 0; c < centroids.length; c++) {
            if (centroids[c] != null) {
                double dist = euclideanDistance(point, centroids[c]);
                if (dist < minDist) {
                    minDist = dist;
                    nearest = c;
                }
            }
        }

        return nearest;
    }

    /**
     * Compute Euclidean distance between two points.
     */
    private double euclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
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
}

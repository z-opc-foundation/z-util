package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Agglomerative hierarchical clustering algorithm.
 * <p>
 * Algorithm:
 * - Compute distance matrix
 * - Iteratively merge two closest clusters based on linkage criterion
 * - SINGLE: min distance between points
 * - COMPLETE: max distance
 * - AVERAGE: avg distance
 * - WARD: minimize variance increase
 */
public class HierarchicalClustering {
    private int nClusters;
    private Linkage linkage;
    private int[] labels;
    private NdArray dendrogram; // Merge history [n-1 x 2] stored as [merge_idx1, merge_idx2, distance]
    private double[][] Xdata;
    private int nSamples;
    private int nFeatures;

    /**
     * HierarchicalClustering方法。
     * * @param nClusters int类型参数
     *
     * @param linkage Linkage类型参数
     */
    public HierarchicalClustering(int nClusters, Linkage linkage) {
        this.nClusters = nClusters;
        this.linkage = linkage;
    }

    /**
     * Fit the hierarchical clustering algorithm and return cluster labels.
     *
     * @param X NdArray of shape [n_samples, n_features]
     * @return cluster labels array
     */
    public int[] fit(NdArray X) {
        this.Xdata = toDouble2D(X);
        this.nSamples = Xdata.length;
        this.nFeatures = Xdata[0].length;

        // Initialize each point as its own cluster
        int[] clusterIds = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            clusterIds[i] = i;
        }

        // Track cluster sizes for WARD linkage
        int[] clusterSizes = new int[nSamples];
        Arrays.fill(clusterSizes, 1);

        // Track cluster sums for WARD linkage
        double[][] clusterSums = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            clusterSums[i] = Xdata[i].clone();
        }

        // Compute initial distance matrix
        double[][] distMatrix = computeDistanceMatrix(Xdata);

        // Priority queue for finding closest pairs
        // Entry: [distance, cluster1, cluster2]
        PriorityQueue<double[]> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(a[0], b[0])
        );

        // Track active clusters (true if still in consideration)
        boolean[] active = new boolean[nSamples];
        Arrays.fill(active, true);

        // Number of current clusters
        int currentClusters = nSamples;

        // dendrogram data: for each merge, store [idx1, idx2, distance]
        // idx1 and idx2 are the original point indices being merged
        double[][] mergeHistory = new double[nSamples - 1][3];

        // Find initial closest pairs
        for (int i = 0; i < nSamples; i++) {
            for (int j = i + 1; j < nSamples; j++) {
                pq.offer(new double[]{distMatrix[i][j], i, j});
            }
        }

        int mergeCount = 0;

        while (currentClusters > nClusters && mergeCount < nSamples - 1) {
            // Find closest pair of active clusters
            double[] closest = null;
            while (!pq.isEmpty()) {
                closest = pq.poll();
                int c1 = (int) closest[1];
                int c2 = (int) closest[2];

                // Check if both clusters are still active
                if (active[c1] && active[c2]) {
                    break;
                }
                closest = null;
            }

            if (closest == null) {
                break; // No more valid pairs
            }

            int cluster1 = (int) closest[1];
            int cluster2 = (int) closest[2];
            double minDist = closest[0];

            // Store merge information
            mergeHistory[mergeCount][0] = cluster1;
            mergeHistory[mergeCount][1] = cluster2;
            mergeHistory[mergeCount][2] = minDist;

            // Mark smaller cluster as inactive
            active[Math.min(cluster1, cluster2)] = false;

            // Create new cluster with the larger index
            int newCluster = Math.max(cluster1, cluster2);

            // Update cluster sums and sizes for WARD
            if (linkage == Linkage.WARD) {
                int oldSize1 = clusterSizes[cluster1];
                int oldSize2 = clusterSizes[cluster2];
                int newSize = oldSize1 + oldSize2;

                for (int j = 0; j < nFeatures; j++) {
                    clusterSums[newCluster][j] = clusterSums[cluster1][j] + clusterSums[cluster2][j];
                }
                clusterSizes[newCluster] = newSize;
            }

            // Update distances from new cluster to all others
            for (int i = 0; i < nSamples; i++) {
                if (active[i] && i != newCluster) {
                    double newDist;

                    switch (linkage) {
                        case SINGLE:
                            newDist = Math.min(distMatrix[cluster1][i], distMatrix[cluster2][i]);
                            break;
                        case COMPLETE:
                            newDist = Math.max(distMatrix[cluster1][i], distMatrix[cluster2][i]);
                            break;
                        case AVERAGE:
                            // Weighted average based on cluster sizes
                            if (linkage == Linkage.AVERAGE) {
                                double w1 = clusterSizes[cluster1];
                                double w2 = clusterSizes[cluster2];
                                newDist = (distMatrix[cluster1][i] * w1 + distMatrix[cluster2][i] * w2) / (w1 + w2);
                            } else {
                                newDist = minDist;
                            }
                            break;
                        case WARD:
                            // WARD linkage: minimize variance increase
                            // Use squared distances and centroids
                            double[] centroid1 = getCentroid(cluster1, clusterSums, clusterSizes);
                            double[] centroid2 = getCentroid(cluster2, clusterSums, clusterSizes);
                            double[] centroidNew = getCentroid(newCluster, clusterSums, clusterSizes);

                            double dist1 = euclideanDistance(Xdata[i], centroid1);
                            double dist2 = euclideanDistance(Xdata[i], centroid2);
                            double distNew = euclideanDistance(Xdata[i], centroidNew);

                            double varIncrease = clusterSizes[newCluster] * distNew * distNew
                                    - clusterSizes[cluster1] * dist1 * dist1
                                    - clusterSizes[cluster2] * dist2 * dist2;
                            newDist = Math.sqrt(Math.max(0, varIncrease));
                            break;
                        default:
                            newDist = minDist;
                    }

                    distMatrix[newCluster][i] = newDist;
                    distMatrix[i][newCluster] = newDist;
                }
            }

            // Add new distances to priority queue
            for (int i = 0; i < nSamples; i++) {
                if (active[i] && i != newCluster) {
                    pq.offer(new double[]{distMatrix[newCluster][i], newCluster, i});
                }
            }

            currentClusters--;
            mergeCount++;
        }

        // Assign final labels based on remaining active clusters
        int[] remainingClusters = new int[nSamples];
        int label = 0;
        for (int i = 0; i < nSamples; i++) {
            if (active[i]) {
                remainingClusters[i] = label++;
            }
        }

        // Assign labels to all points based on final cluster membership
        this.labels = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            // Find which final cluster this point belongs to
            int finalCluster = findFinalCluster(i, mergeHistory, active);
            this.labels[i] = remainingClusters[finalCluster];
        }

        // Store dendrogram
        this.dendrogram = NdArray.array(flatten2D(mergeHistory), DType.FLOAT64).reshape(nSamples - 1, 3);

        return this.labels;
    }

    /**
     * Get dendrogram (merge history).
     * Returns array of shape [n-1 x 3] where each row is [idx1, idx2, distance].
     *
     * @return NdArray containing merge history
     */
    public NdArray getDendrogram() {
        return this.dendrogram;
    }

    /**
     * Find which final cluster a point belongs to based on merge history.
     */
    private int findFinalCluster(int pointIdx, double[][] mergeHistory, boolean[] active) {
        int current = pointIdx;

        // Follow the merges until we reach a final cluster
        for (int m = 0; m < mergeHistory.length; m++) {
            if (mergeHistory[m][0] == -1 && mergeHistory[m][1] == -1) {
                continue; // Already processed
            }

            int idx1 = (int) mergeHistory[m][0];
            int idx2 = (int) mergeHistory[m][1];

            if (current == idx1 || current == idx2) {
                // Point was involved in this merge
                current = Math.max(idx1, idx2);
            }
        }

        return current;
    }

    /**
     * Compute centroid of a cluster.
     */
    private double[] getCentroid(int clusterIdx, double[][] clusterSums, int[] clusterSizes) {
        if (clusterSizes[clusterIdx] == 0) {
            return new double[nFeatures];
        }
        double[] centroid = new double[nFeatures];
        for (int j = 0; j < nFeatures; j++) {
            centroid[j] = clusterSums[clusterIdx][j] / clusterSizes[clusterIdx];
        }
        return centroid;
    }

    /**
     * Compute pairwise distance matrix.
     */
    private double[][] computeDistanceMatrix(double[][] X) {
        int n = X.length;
        double[][] dist = new double[n][n];

        for (int i = 0; i < n; i++) {
            dist[i][i] = 0.0;
            for (int j = i + 1; j < n; j++) {
                double d = euclideanDistance(X[i], X[j]);
                dist[i][j] = d;
                dist[j][i] = d;
            }
        }

        return dist;
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

    /**
     * Linkage枚举。
     */
    public enum Linkage {SINGLE, COMPLETE, AVERAGE, WARD}
}

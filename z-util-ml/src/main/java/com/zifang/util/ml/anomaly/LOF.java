package com.zifang.util.ml.anomaly;

import com.zifang.util.numpy.NdArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Local Outlier Factor (LOF) for anomaly detection.
 * <p>
 * Algorithm:
 * 1. For each point p, find k nearest neighbors (k = nNeighbors)
 * 2. Compute reachability distance: rd(p, o) = max(k-distance(o), d(p, o))
 * 3. Compute local reachability density: lrd(p) = 1 / (sum_rd(p, o) / k)
 * 4. LOF(p) = sum(lrd(o) / lrd(p)) / k for all neighbors o
 * 5. LOF > 1 means more dense than neighbors (outlier), LOF ≈ 1 normal
 */
public class LOF {

    private int nNeighbors;
    private double contamination;
    private double threshold;
    private double[][] trainingData;
    private int nSamples;
    private int nFeatures;

    /**
     * Create a new LOF detector.
     *
     * @param nNeighbors    Number of neighbors (k)
     * @param contamination Expected proportion of outliers (0.0 to 0.5)
     */
    public LOF(int nNeighbors, double contamination) {
        this.nNeighbors = nNeighbors;
        this.contamination = contamination;
        this.threshold = 1.0;
    }

    /**
     * Fit the LOF to the training data.
     */
    public void fit(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        this.nSamples = Xdata.length;
        this.nFeatures = Xdata[0].length;
        this.trainingData = Xdata;

        // Calculate LOF scores for training data to determine threshold
        double[] trainScores = scoreLOF(Xdata);

        // Sort scores and find threshold at contamination quantile
        double[] sortedScores = trainScores.clone();
        Arrays.sort(sortedScores);
        int thresholdIndex = (int) Math.floor((1.0 - contamination) * nSamples);
        thresholdIndex = Math.max(0, Math.min(thresholdIndex, nSamples - 1));
        this.threshold = sortedScores[thresholdIndex];
    }

    /**
     * Calculate LOF scores for training data.
     */
    private double[] scoreLOF(double[][] X) {
        double[] scores = new double[X.length];

        for (int i = 0; i < X.length; i++) {
            scores[i] = computeLOFSingle(X, i, X);
        }

        return scores;
    }

    /**
     * Compute LOF for a single point.
     */
    private double computeLOFSingle(double[][] X, int pointIndex, double[][] allData) {
        // Find k nearest neighbors
        int[] neighbors = findKNearestNeighbors(X, pointIndex, nNeighbors, allData);

        // Compute k-distance for each neighbor
        double[] kDistances = new double[neighbors.length];
        for (int i = 0; i < neighbors.length; i++) {
            kDistances[i] = distance(X[pointIndex], X[neighbors[i]]);
        }
        double kDistance = Arrays.stream(kDistances).max().orElse(0.0);

        // Compute reachability distance to each neighbor
        double[] reachDist = new double[neighbors.length];
        for (int i = 0; i < neighbors.length; i++) {
            double d = distance(X[pointIndex], X[neighbors[i]]);
            reachDist[i] = Math.max(kDistance, d);
        }

        // Compute local reachability density (lrd)
        double sumReachDist = Arrays.stream(reachDist).sum();
        double lrd = nNeighbors / sumReachDist;

        // Compute LOF
        double lrdSum = 0.0;
        for (int i = 0; i < neighbors.length; i++) {
            // Compute lrd for neighbor
            int[] neighborNeighbors = findKNearestNeighbors(X, neighbors[i], nNeighbors, allData);
            double[] neighborKDists = new double[neighborNeighbors.length];
            for (int j = 0; j < neighborNeighbors.length; j++) {
                neighborKDists[j] = distance(X[neighbors[i]], X[neighborNeighbors[j]]);
            }
            double neighborKDist = Arrays.stream(neighborKDists).max().orElse(0.0);

            double[] neighborReachDist = new double[neighborNeighbors.length];
            for (int j = 0; j < neighborNeighbors.length; j++) {
                double d = distance(X[neighbors[i]], X[neighborNeighbors[j]]);
                neighborReachDist[j] = Math.max(neighborKDist, d);
            }
            double neighborLrd = neighborNeighbors.length / Arrays.stream(neighborReachDist).sum();

            lrdSum += neighborLrd / lrd;
        }

        return lrdSum / nNeighbors;
    }

    /**
     * Find k nearest neighbors using brute force.
     */
    private int[] findKNearestNeighbors(double[][] X, int pointIndex, int k, double[][] allData) {
        // Compute distances to all points
        List<NeighborDistance> distances = new ArrayList<>();
        for (int i = 0; i < allData.length; i++) {
            if (i != pointIndex) {
                double d = distance(X[pointIndex], allData[i]);
                distances.add(new NeighborDistance(i, d));
            }
        }

        // Sort by distance
        distances.sort((a, b) -> Double.compare(a.distance, b.distance));

        // Return k nearest
        int size = Math.min(k, distances.size());
        int[] neighbors = new int[size];
        for (int i = 0; i < size; i++) {
            neighbors[i] = distances.get(i).index;
        }

        return neighbors;
    }

    /**
     * Compute Euclidean distance between two points.
     */
    private double distance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
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
            predictions[i] = scores[i] > threshold ? -1 : 1;
        }

        return predictions;
    }

    /**
     * Calculate LOF scores for input data.
     *
     * @param X Input data (nSamples x nFeatures)
     * @return Array of LOF scores (higher = more anomalous)
     */
    public double[] score(NdArray X) {
        double[][] Xdata = toDouble2D(X);
        int nSamples = Xdata.length;
        double[] scores = new double[nSamples];

        // Combine with training data for nearest neighbor search
        double[][] combined = new double[nSamples + this.nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            combined[i] = Xdata[i];
        }
        for (int i = 0; i < this.nSamples; i++) {
            combined[nSamples + i] = trainingData[i];
        }

        for (int i = 0; i < nSamples; i++) {
            scores[i] = computeLOFSingle(combined, i, combined);
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

    /**
     * Helper class for neighbor distance pairs.
     */
    private static class NeighborDistance {
        int index;
        double distance;

        NeighborDistance(int index, double distance) {
            this.index = index;
            this.distance = distance;
        }
    }
}

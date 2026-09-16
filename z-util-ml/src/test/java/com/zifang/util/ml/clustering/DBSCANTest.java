package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DBSCANTest类。
 */
public class DBSCANTest {

    private Random random = new Random(42);

    private NdArray createNdArray(double[][] data, int rows, int cols) {
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = data[i][j];
            }
        }
        return NdArray.array(flat, DType.FLOAT32).reshape(rows, cols);
    }

    /**
     * Generate data with a dense cluster and some noise points.
     */
    private NdArray generateClusteredDataWithNoise() {
        double[][] data = new double[50][2];

        // Dense cluster centered at (0, 0) - 30 points
        for (int i = 0; i < 30; i++) {
            data[i][0] = random.nextGaussian() * 0.3;
            data[i][1] = random.nextGaussian() * 0.3;
        }

        // Noise points scattered far away
        for (int i = 30; i < 45; i++) {
            data[i][0] = random.nextDouble() * 20 - 10;
            data[i][1] = random.nextDouble() * 20 - 10;
        }

        // Another small cluster at (10, 10) - 5 points
        for (int i = 45; i < 50; i++) {
            data[i][0] = random.nextGaussian() * 0.3 + 10;
            data[i][1] = random.nextGaussian() * 0.3 + 10;
        }

        return createNdArray(data, 50, 2);
    }

    @Test
    /**
     * testDBSCANClustering方法。
     */
    public void testDBSCANClustering() {
        NdArray X = generateClusteredDataWithNoise();

        DBSCAN dbscan = new DBSCAN(1.0, 3);
        int[] labels = dbscan.fit(X);

        assertEquals(50, labels.length);

        // Should have at least one cluster (the dense one)
        int clusterCount = 0;
        for (int label : labels) {
            if (label >= 0) {
                clusterCount = Math.max(clusterCount, label + 1);
            }
        }
        assertTrue(clusterCount >= 1, "Should identify at least one cluster");
    }

    @Test
    /**
     * testDBSCANNoise方法。
     */
    public void testDBSCANNoise() {
        NdArray X = generateClusteredDataWithNoise();

        DBSCAN dbscan = new DBSCAN(0.5, 3);  // Small eps will cause noise detection
        int[] labels = dbscan.fit(X);

        assertEquals(50, labels.length);

        // Verify noise points have label -1
        boolean hasNoise = false;
        for (int label : labels) {
            if (label == -1) {
                hasNoise = true;
                break;
            }
        }
        assertTrue(hasNoise, "Should detect some noise points with small eps");
    }

    @Test
    /**
     * testDBSCANPredict方法。
     */
    public void testDBSCANPredict() {
        double[][] trainData = new double[30][2];
        for (int i = 0; i < 30; i++) {
            trainData[i][0] = random.nextGaussian() * 0.5;
            trainData[i][1] = random.nextGaussian() * 0.5;
        }
        NdArray Xtrain = createNdArray(trainData, 30, 2);

        DBSCAN dbscan = new DBSCAN(1.5, 3);
        dbscan.fit(Xtrain);

        // Test data including points near and far from cluster
        double[][] testData = new double[5][2];
        testData[0] = new double[]{0.0, 0.0};  // Near cluster center
        testData[1] = new double[]{0.5, 0.5};
        testData[2] = new double[]{20.0, 20.0}; // Far away - noise
        testData[3] = new double[]{-20.0, -20.0};
        testData[4] = new double[]{0.1, 0.1};

        NdArray Xtest = createNdArray(testData, 5, 2);
        int[] predictions = dbscan.predict(Xtest);

        assertEquals(5, predictions.length);
    }

    @Test
    /**
     * testDBSCANDenseRegion方法。
     */
    public void testDBSCANDenseRegion() {
        // Create data with varying densities
        double[][] data = new double[60][2];

        // Dense cluster at origin - 40 points
        for (int i = 0; i < 40; i++) {
            data[i][0] = random.nextGaussian() * 0.2;
            data[i][1] = random.nextGaussian() * 0.2;
        }

        // Sparse cluster at (5, 5) - 20 points
        for (int i = 40; i < 60; i++) {
            data[i][0] = random.nextGaussian() * 1.0 + 5.0;
            data[i][1] = random.nextGaussian() * 1.0 + 5.0;
        }

        NdArray X = createNdArray(data, 60, 2);

        // With proper eps, should identify both clusters
        DBSCAN dbscan = new DBSCAN(2.0, 3);
        int[] labels = dbscan.fit(X);

        assertEquals(60, labels.length);

        // Both clusters should be identified
        int maxLabel = -1;
        for (int label : labels) {
            if (label > maxLabel) {
                maxLabel = label;
            }
        }
        assertTrue(maxLabel >= 1, "Should identify at least 2 clusters");
    }
}

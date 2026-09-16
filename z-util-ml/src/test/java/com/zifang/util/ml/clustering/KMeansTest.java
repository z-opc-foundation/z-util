package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * KMeansTest类。
 */
public class KMeansTest {

    private Random random = new Random(42);

    /**
     * Generate synthetic 2D data with 3 distinct clusters.
     */
    private NdArray generateClusteredData(int nSamplesPerCluster) {
        double[][] data = new double[nSamplesPerCluster * 3][2];

        // Cluster 1: centered at (0, 0)
        for (int i = 0; i < nSamplesPerCluster; i++) {
            data[i][0] = random.nextGaussian() * 0.5 + 0.0;
            data[i][1] = random.nextGaussian() * 0.5 + 0.0;
        }

        // Cluster 2: centered at (5, 5)
        for (int i = 0; i < nSamplesPerCluster; i++) {
            data[nSamplesPerCluster + i][0] = random.nextGaussian() * 0.5 + 5.0;
            data[nSamplesPerCluster + i][1] = random.nextGaussian() * 0.5 + 5.0;
        }

        // Cluster 3: centered at (0, 5)
        for (int i = 0; i < nSamplesPerCluster; i++) {
            data[2 * nSamplesPerCluster + i][0] = random.nextGaussian() * 0.5 + 0.0;
            data[2 * nSamplesPerCluster + i][1] = random.nextGaussian() * 0.5 + 5.0;
        }

        return createNdArray(data, 3 * nSamplesPerCluster, 2);
    }

    private NdArray createNdArray(double[][] data, int rows, int cols) {
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = data[i][j];
            }
        }
        return NdArray.array(flat, DType.FLOAT32).reshape(rows, cols);
    }

    @Test
    /**
     * testKMeansFit方法。
     */
    public void testKMeansFit() {
        int nSamplesPerCluster = 30;
        NdArray X = generateClusteredData(nSamplesPerCluster);

        KMeans kmeans = new KMeans(3, 100, 1e-4);
        int[] labels = kmeans.fitPredict(X);

        assertEquals(3 * nSamplesPerCluster, labels.length);

        // Count points in each cluster
        int[] clusterCounts = new int[3];
        for (int label : labels) {
            clusterCounts[label]++;
        }

        // Each cluster should have a reasonable number of points
        for (int count : clusterCounts) {
            assertTrue(count > 0, "Each cluster should have at least one point");
            assertTrue(count <= 3 * nSamplesPerCluster, "Cluster count should not exceed total");
        }
    }

    @Test
    /**
     * testKMeansPredict方法。
     */
    public void testKMeansPredict() {
        int nSamplesPerCluster = 25;
        NdArray Xtrain = generateClusteredData(nSamplesPerCluster);

        KMeans kmeans = new KMeans(3, 100, 1e-4);
        kmeans.fit(Xtrain);

        // Generate test data near cluster centers
        double[][] testData = new double[6][2];
        testData[0] = new double[]{0.1, 0.1};  // Near cluster 1
        testData[1] = new double[]{-0.1, -0.1};
        testData[2] = new double[]{5.1, 5.1};  // Near cluster 2
        testData[3] = new double[]{4.9, 4.9};
        testData[4] = new double[]{0.1, 5.1};  // Near cluster 3
        testData[5] = new double[]{-0.1, 4.9};

        NdArray Xtest = createNdArray(testData, 6, 2);
        int[] predictions = kmeans.predict(Xtest);

        assertEquals(6, predictions.length);

        // Points near same cluster center should have same label
        assertEquals(predictions[0], predictions[1], "Points near cluster 1 center should have same label");
        assertEquals(predictions[2], predictions[3], "Points near cluster 2 center should have same label");
        assertEquals(predictions[4], predictions[5], "Points near cluster 3 center should have same label");
    }

    @Test
    /**
     * testKMeansConvergence方法。
     */
    public void testKMeansConvergence() {
        int nSamplesPerCluster = 20;
        NdArray X = generateClusteredData(nSamplesPerCluster);

        // Should terminate without error
        KMeans kmeans = new KMeans(3, 50, 1e-6);
        int[] labels = kmeans.fitPredict(X);

        assertEquals(60, labels.length);

        // Verify centroids are accessible
        int[] predictions = kmeans.predict(X);
        assertEquals(60, predictions.length);
    }

    @Test
    /**
     * testKMeansEmptyCluster方法。
     */
    public void testKMeansEmptyCluster() {
        // Create data where one cluster might be empty due to initialization
        double[][] data = new double[30][2];
        // All points in a tight cluster
        for (int i = 0; i < 30; i++) {
            data[i][0] = random.nextGaussian() * 0.1;
            data[i][1] = random.nextGaussian() * 0.1;
        }

        NdArray X = createNdArray(data, 30, 2);

        // k=5 but only 30 points, some clusters will be empty initially
        KMeans kmeans = new KMeans(5, 100, 1e-4);
        int[] labels = kmeans.fitPredict(X);

        assertEquals(30, labels.length);

        // All points should still have valid labels (0-4)
        for (int label : labels) {
            assertTrue(label >= 0 && label < 5, "Labels should be between 0 and 4");
        }
    }
}

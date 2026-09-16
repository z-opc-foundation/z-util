package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GMMTest类。
 */
public class GMMTest {

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

    private NdArray generateGMMData(int nSamples) {
        double[][] data = new double[nSamples][2];

        // Component 1: centered at (0, 0)
        for (int i = 0; i < nSamples / 3; i++) {
            data[i][0] = random.nextGaussian() * 0.5;
            data[i][1] = random.nextGaussian() * 0.5;
        }

        // Component 2: centered at (5, 5)
        for (int i = nSamples / 3; i < 2 * nSamples / 3; i++) {
            data[i][0] = random.nextGaussian() * 0.5 + 5.0;
            data[i][1] = random.nextGaussian() * 0.5 + 5.0;
        }

        // Component 3: centered at (0, 5)
        for (int i = 2 * nSamples / 3; i < nSamples; i++) {
            data[i][0] = random.nextGaussian() * 0.5;
            data[i][1] = random.nextGaussian() * 0.5 + 5.0;
        }

        return createNdArray(data, nSamples, 2);
    }

    @Test
    /**
     * testGMMFit方法。
     */
    public void testGMMFit() {
        int nSamples = 90;
        NdArray X = generateGMMData(nSamples);

        GMM gmm = new GMM(3, 100, 1e-4);
        int[] labels = gmm.fitPredict(X);

        assertEquals(nSamples, labels.length);

        // All labels should be valid (0, 1, or 2)
        for (int label : labels) {
            assertTrue(label >= 0 && label < 3, "Labels should be 0, 1, or 2");
        }

        // Each cluster should have some points
        int[] clusterCounts = new int[3];
        for (int label : labels) {
            clusterCounts[label]++;
        }
        for (int count : clusterCounts) {
            assertTrue(count > 0, "Each cluster should have at least one point");
        }
    }

    @Test
    /**
     * testGMMProba方法。
     */
    public void testGMMProba() {
        int nSamples = 60;
        NdArray X = generateGMMData(nSamples);

        GMM gmm = new GMM(3, 100, 1e-4);
        gmm.fit(X);

        NdArray proba = gmm.predictProba(X);

        assertEquals(nSamples, proba.getShape().get(0));
        assertEquals(3, proba.getShape().get(1));

        // Probabilities should sum to 1 for each sample
        for (int i = 0; i < nSamples; i++) {
            double sum = 0;
            for (int j = 0; j < 3; j++) {
                double p = ((Number) proba.get(i, j)).doubleValue();
                assertTrue(p >= 0 && p <= 1, "Probabilities should be between 0 and 1");
                sum += p;
            }
            assertEquals(1.0, sum, 1e-6, "Probabilities should sum to 1");
        }
    }

    @Test
    /**
     * testGMMConvergence方法。
     */
    public void testGMMConvergence() {
        int nSamples = 45;
        NdArray X = generateGMMData(nSamples);

        // Should terminate without error
        GMM gmm = new GMM(3, 50, 1e-6);
        int[] labels = gmm.fitPredict(X);

        assertEquals(nSamples, labels.length);

        // Predictions should work
        NdArray proba = gmm.predictProba(X);
        assertEquals(nSamples, proba.getShape().get(0));
        assertEquals(3, proba.getShape().get(1));
    }
}

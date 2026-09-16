package com.zifang.util.ml.decomposition;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PCATest类。
 */
public class PCATest {

    private Random random = new Random(42);

    private NdArray createNdArray(double[][] data, int rows, int cols) {
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = data[i][j];
            }
        }
        return NdArray.array(flat, DType.FLOAT64).reshape(rows, cols);
    }

    @Test
    /**
     * testPCAVariance方法。
     */
    public void testPCAVariance() {
        int nSamples = 100;
        int nFeatures = 5;

        double[][] data = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                data[i][j] = random.nextGaussian();
            }
        }

        NdArray X = createNdArray(data, nSamples, nFeatures);

        PCA pca = new PCA(3);
        pca.fit(X);

        NdArray explainedVariance = pca.getExplainedVariance();

        assertNotNull(explainedVariance);
        assertEquals(3, explainedVariance.getShape().get(0));

        // Variance values should be non-negative
        double totalVariance = 0;
        for (int i = 0; i < 3; i++) {
            double var = ((Number) explainedVariance.get(i)).doubleValue();
            assertTrue(var >= 0, "Explained variance should be non-negative");
            totalVariance += var;
        }

        // Total variance retained should be positive
        assertTrue(totalVariance > 0, "Total explained variance should be positive");
    }

    @Test
    /**
     * testPCAProjection方法。
     */
    public void testPCAProjection() {
        int nSamples = 50;
        int nFeatures = 4;
        int nComponents = 2;

        double[][] data = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                data[i][j] = random.nextGaussian() * (j + 1);
            }
        }

        NdArray X = createNdArray(data, nSamples, nFeatures);

        PCA pca = new PCA(nComponents);
        NdArray transformed = pca.fitTransform(X);

        assertEquals(nSamples, transformed.getShape().get(0));
        assertEquals(nComponents, transformed.getShape().get(1));
    }

    @Test
    /**
     * testPCAReconstruction方法。
     */
    public void testPCAReconstruction() {
        int nSamples = 30;
        int nFeatures = 3;

        double[][] data = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                data[i][j] = random.nextGaussian();
            }
        }

        NdArray X = createNdArray(data, nSamples, nFeatures);

        PCA pca = new PCA(3);  // Keep all components
        pca.fit(X);

        NdArray transformed = pca.transform(X);
        NdArray reconstructed = pca.inverseTransform(transformed);

        assertEquals(nSamples, reconstructed.getShape().get(0));
        assertEquals(nFeatures, reconstructed.getShape().get(1));

        // With all components, reconstruction should be close to original
        double maxError = 0;
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                double orig = ((Number) X.get(i, j)).doubleValue();
                double recon = ((Number) reconstructed.get(i, j)).doubleValue();
                double error = Math.abs(orig - recon);
                maxError = Math.max(maxError, error);
            }
        }

        // With full components, error should be very small
        assertTrue(maxError < 1e-10, "Reconstruction error with full PCA should be near zero");
    }
}

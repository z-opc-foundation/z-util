package com.zifang.util.ml.decomposition;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * tSNETest类。
 */
public class tSNETest {

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

    @Test
    /**
     * testTSNEOutputShape方法。
     */
    public void testTSNEOutputShape() {
        int nSamples = 30;
        int nFeatures = 4;
        int nComponents = 2;

        double[][] data = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                data[i][j] = random.nextGaussian();
            }
        }

        NdArray X = createNdArray(data, nSamples, nFeatures);

        tSNE tsne = new tSNE(nComponents, 10, 100, 100.0);
        NdArray embedding = tsne.fitTransform(X);

        assertEquals(nSamples, embedding.getShape().get(0));
        assertEquals(nComponents, embedding.getShape().get(1));
    }

    @Test
    /**
     * testTSNEConvergence方法。
     */
    public void testTSNEConvergence() {
        int nSamples = 25;
        int nFeatures = 3;

        // Create a simple cluster
        double[][] data = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                data[i][j] = random.nextGaussian() * 0.5;
            }
        }

        NdArray X = createNdArray(data, nSamples, nFeatures);

        // Should complete without error
        tSNE tsne = new tSNE(2, 10, 50, 50.0);
        NdArray embedding = tsne.fitTransform(X);

        assertNotNull(embedding);
        assertEquals(nSamples, embedding.getShape().get(0));
        assertEquals(2, embedding.getShape().get(1));
    }

    @Test
    /**
     * testTSNEPerplexity方法。
     */
    public void testTSNEPerplexity() {
        int nSamples = 40;
        int nFeatures = 5;

        double[][] data = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                data[i][j] = random.nextGaussian();
            }
        }

        NdArray X = createNdArray(data, nSamples, nFeatures);

        // Test with different perplexity values
        int[] perplexities = {5, 15, 30};

        for (int perp : perplexities) {
            tSNE tsne = new tSNE(2, perp, 50, 50.0);
            NdArray embedding = tsne.fitTransform(X);

            assertEquals(nSamples, embedding.getShape().get(0));
            assertEquals(2, embedding.getShape().get(1));
        }
    }
}

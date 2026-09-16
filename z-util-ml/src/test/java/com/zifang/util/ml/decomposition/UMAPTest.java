package com.zifang.util.ml.decomposition;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UMAPTest类。
 */
public class UMAPTest {

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
     * testUMAPOutputShape方法。
     */
    public void testUMAPOutputShape() {
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

        UMAP umap = new UMAP(nComponents, 10, 0.1, 1.0, 50);
        NdArray embedding = umap.fitTransform(X);

        assertEquals(nSamples, embedding.getShape().get(0));
        assertEquals(nComponents, embedding.getShape().get(1));
    }

    @Test
    /**
     * testUMAPNeighbors方法。
     */
    public void testUMAPNeighbors() {
        int nSamples = 40;
        int nFeatures = 5;

        double[][] data = new double[nSamples][nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                data[i][j] = random.nextGaussian();
            }
        }

        NdArray X = createNdArray(data, nSamples, nFeatures);

        // Test with different n_neighbors values
        int[] neighborValues = {5, 15, 25};

        for (int nNeighbors : neighborValues) {
            UMAP umap = new UMAP(2, nNeighbors, 0.1, 1.0, 50);
            NdArray embedding = umap.fitTransform(X);

            assertEquals(nSamples, embedding.getShape().get(0));
            assertEquals(2, embedding.getShape().get(1));
        }
    }
}

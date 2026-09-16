package com.zifang.util.ml.sequence;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRFTest类。
 */
public class CRFTest {

    private static double[] flatten(double[][] arr) {
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

    @Test
    /**
     * testCRFFit方法。
     */
    public void testCRFFit() {
        CRF crf = new CRF(3, 5, 0.1, 0.01);

        // Create simple feature matrix: 4 time steps, 5 features
        double[][] featuresData = {
                {1.0, 0.0, 0.5, 0.2, 0.1},
                {0.5, 1.0, 0.3, 0.4, 0.2},
                {0.2, 0.3, 1.0, 0.1, 0.5},
                {0.1, 0.4, 0.2, 1.0, 0.3}
        };

        NdArray features = NdArray.array(flatten(featuresData), DType.FLOAT32).reshape(4, 5);
        int[] labels = new int[]{0, 1, 2, 1};

        // Fit should not throw
        crf.fit(features, labels, 10);

        // Score should return a value
        double scoreVal = crf.score(features, labels);
        assertTrue(Double.isFinite(scoreVal));
    }

    @Test
    /**
     * testCRFPredict方法。
     */
    public void testCRFPredict() {
        CRF crf = new CRF(2, 4, 0.1, 0.01);

        double[][] featuresData = {
                {1.0, 0.5, 0.2, 0.1},
                {0.3, 0.8, 0.1, 0.4},
                {0.2, 0.3, 0.7, 0.2}
        };

        NdArray features = NdArray.array(flatten(featuresData), DType.FLOAT32).reshape(3, 4);

        int[] predictions = crf.predict(features);

        assertNotNull(predictions);
        assertEquals(3, predictions.length);

        // All predictions should be valid tags (0 or 1)
        for (int pred : predictions) {
            assertTrue(pred >= 0 && pred < 2);
        }
    }
}

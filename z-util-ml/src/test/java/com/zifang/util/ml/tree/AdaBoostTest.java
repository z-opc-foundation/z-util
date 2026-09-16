package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AdaBoostTest类。
 */
public class AdaBoostTest {

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

    private NdArray generateClassificationData(int nSamples) {
        double[][] data = new double[nSamples][2];
        for (int i = 0; i < nSamples; i++) {
            double x = random.nextDouble() * 10 - 5;
            double y = random.nextDouble() * 10 - 5;
            data[i][0] = x;
            data[i][1] = y;
        }
        return createNdArray(data, nSamples, 2);
    }

    private int[] generateLabels(int nSamples) {
        int[] labels = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            double x = random.nextDouble() * 10 - 5;
            double y = random.nextDouble() * 10 - 5;
            labels[i] = (x + y > 0) ? 1 : 0;
        }
        return labels;
    }

    @Test
    /**
     * testAdaBoostFit方法。
     */
    public void testAdaBoostFit() {
        int nSamples = 80;
        NdArray X = generateClassificationData(nSamples);
        int[] y = generateLabels(nSamples);

        // Test with different number of estimators
        AdaBoost ab5 = new AdaBoost(5, 1.0);
        ab5.fit(X, y);

        AdaBoost ab20 = new AdaBoost(20, 1.0);
        ab20.fit(X, y);

        int[] pred5 = ab5.predict(X);
        int[] pred20 = ab20.predict(X);

        assertEquals(nSamples, pred5.length);
        assertEquals(nSamples, pred20.length);

        // More estimators should generally help
        int correct5 = 0, correct20 = 0;
        for (int i = 0; i < nSamples; i++) {
            if (pred5[i] == y[i]) correct5++;
            if (pred20[i] == y[i]) correct20++;
        }

        // Both should achieve some accuracy
        assertTrue(correct5 > 0, "Should have some correct predictions with 5 estimators");
        assertTrue(correct20 > 0, "Should have some correct predictions with 20 estimators");
    }

    @Test
    /**
     * testAdaBoostWeights方法。
     */
    public void testAdaBoostWeights() {
        int nSamples = 60;
        NdArray X = generateClassificationData(nSamples);
        int[] y = generateLabels(nSamples);

        AdaBoost ab = new AdaBoost(10, 1.0);
        ab.fit(X, y);

        // Just verify it completes without error
        int[] predictions = ab.predict(X);
        assertEquals(nSamples, predictions.length);
    }

    @Test
    /**
     * testAdaBoostPredict方法。
     */
    public void testAdaBoostPredict() {
        int nSamples = 50;
        NdArray X = generateClassificationData(nSamples);
        int[] y = generateLabels(nSamples);

        AdaBoost ab = new AdaBoost(10, 1.0);
        ab.fit(X, y);

        int[] predictions = ab.predict(X);

        assertEquals(nSamples, predictions.length);

        // All predictions should be valid labels (0 or 1)
        for (int pred : predictions) {
            assertTrue(pred == 0 || pred == 1, "Predictions should be 0 or 1");
        }

        // Verify predictions match training labels for some samples
        int correct = 0;
        for (int i = 0; i < nSamples; i++) {
            if (predictions[i] == y[i]) {
                correct++;
            }
        }
        assertTrue(correct > 0, "Should have some correct predictions");
    }
}

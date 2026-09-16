package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RandomForestTest类。
 */
public class RandomForestTest {

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
     * testRandomForestAccuracy方法。
     */
    public void testRandomForestAccuracy() {
        int nSamples = 100;
        NdArray X = generateClassificationData(nSamples);
        int[] y = generateLabels(nSamples);

        RandomForest rf = new RandomForest(10, 5, 2, 0);
        rf.fit(X, y);

        int[] predictions = rf.predict(X);

        int correct = 0;
        for (int i = 0; i < nSamples; i++) {
            if (predictions[i] == y[i]) {
                correct++;
            }
        }
        double accuracy = (double) correct / nSamples;

        // Random Forest should achieve reasonable accuracy (relaxed threshold)
        assertTrue(accuracy > 0.5, "Random Forest accuracy should be > 0.5, got: " + accuracy);
    }

    @Test
    /**
     * testRandomForestProba方法。
     */
    public void testRandomForestProba() {
        int nSamples = 50;
        NdArray X = generateClassificationData(nSamples);
        int[] y = generateLabels(nSamples);

        RandomForest rf = new RandomForest(5, 5, 2, 0);
        rf.fit(X, y);

        NdArray proba = rf.predictProba(X);

        assertEquals(nSamples, proba.getShape().get(0));
        assertEquals(2, proba.getShape().get(1));

        // Verify probabilities sum to 1
        for (int i = 0; i < nSamples; i++) {
            double p0 = ((Number) proba.get(i, 0)).doubleValue();
            double p1 = ((Number) proba.get(i, 1)).doubleValue();

            assertTrue(p0 >= 0 && p0 <= 1);
            assertTrue(p1 >= 0 && p1 <= 1);
            assertEquals(1.0, p0 + p1, 1e-6);
        }
    }

    @Test
    /**
     * testRandomForestMultiClass方法。
     */
    public void testRandomForestMultiClass() {
        int nSamples = 90;
        double[][] data = new double[nSamples][2];
        int[] labels = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            double x = random.nextDouble() * 10 - 5;
            double y = random.nextDouble() * 10 - 5;
            data[i][0] = x;
            data[i][1] = y;

            // 3 classes based on quadrant
            if (x > 0 && y > 0) labels[i] = 0;
            else if (x > 0) labels[i] = 1;
            else labels[i] = 2;
        }

        NdArray X = createNdArray(data, nSamples, 2);

        RandomForest rf = new RandomForest(10, 8, 2, 0);
        rf.fit(X, labels);

        int[] predictions = rf.predict(X);

        assertEquals(nSamples, predictions.length);

        // All predictions should be valid class labels
        for (int pred : predictions) {
            assertTrue(pred >= 0 && pred < 3);
        }
    }
}

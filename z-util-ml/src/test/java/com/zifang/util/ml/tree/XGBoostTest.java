package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * XGBoostTest类。
 */
public class XGBoostTest {

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
     * testXGBoostFit方法。
     */
    public void testXGBoostFit() {
        int nSamples = 80;
        NdArray X = generateClassificationData(nSamples);
        int[] y = generateLabels(nSamples);

        XGBoost xgb = new XGBoost(10, 0.1, 3, 1, 0.8, 0.8, 1.0, 0.0);
        xgb.fit(X, y);

        int[] predictions = xgb.predictClass(X);

        assertEquals(nSamples, predictions.length);

        // Verify predictions are valid
        for (int pred : predictions) {
            assertTrue(pred == 0 || pred == 1);
        }
    }

    @Test
    /**
     * testXGBoostRegularization方法。
     */
    public void testXGBoostRegularization() {
        int nSamples = 60;
        NdArray X = generateClassificationData(nSamples);
        int[] y = generateLabels(nSamples);

        // Test with L1 and L2 regularization
        XGBoost xgb1 = new XGBoost(5, 0.1, 3, 1, 0.8, 0.8, 1.0, 0.0);
        xgb1.fit(X, y);

        XGBoost xgb2 = new XGBoost(5, 0.1, 3, 1, 0.8, 0.8, 0.0, 1.0);
        xgb2.fit(X, y);

        XGBoost xgb3 = new XGBoost(5, 0.1, 3, 1, 0.8, 0.8, 1.0, 1.0);
        xgb3.fit(X, y);

        // All should complete without error
        int[] pred1 = xgb1.predictClass(X);
        int[] pred2 = xgb2.predictClass(X);
        int[] pred3 = xgb3.predictClass(X);

        assertEquals(nSamples, pred1.length);
        assertEquals(nSamples, pred2.length);
        assertEquals(nSamples, pred3.length);
    }

    @Test
    /**
     * testXGBoostMultiClass方法。
     */
    public void testXGBoostMultiClass() {
        int nSamples = 90;
        double[][] data = new double[nSamples][2];
        int[] labels = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            double x = random.nextDouble() * 10 - 5;
            double y = random.nextDouble() * 10 - 5;
            data[i][0] = x;
            data[i][1] = y;

            // 3 classes
            if (x > 0 && y > 0) labels[i] = 0;
            else if (x > 0) labels[i] = 1;
            else labels[i] = 2;
        }

        NdArray X = createNdArray(data, nSamples, 2);

        XGBoost xgb = new XGBoost(10, 0.1, 4, 1, 0.8, 0.8, 1.0, 0.0);
        xgb.fit(X, labels);

        int[] predictions = xgb.predictClass(X);

        assertEquals(nSamples, predictions.length);

        // All predictions should be valid class labels
        for (int pred : predictions) {
            assertTrue(pred >= 0 && pred < 3);
        }
    }
}

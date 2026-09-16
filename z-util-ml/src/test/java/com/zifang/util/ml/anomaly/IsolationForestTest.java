package com.zifang.util.ml.anomaly;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IsolationForestTest类。
 */
public class IsolationForestTest {

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
     * testIsolationForestFit方法。
     */
    public void testIsolationForestFit() {
        IsolationForest isoForest = new IsolationForest(10, 8, 0.1);

        // Create simple training data (100 samples, 2 features)
        double[][] data = new double[100][2];
        for (int i = 0; i < 100; i++) {
            data[i][0] = Math.random() * 2 - 1; // range [-1, 1]
            data[i][1] = Math.random() * 2 - 1;
        }

        NdArray X = NdArray.array(flatten(data), DType.FLOAT32).reshape(100, 2);

        // Fit should not throw
        isoForest.fit(X);
    }

    @Test
    /**
     * testIsolationForestPredict方法。
     */
    public void testIsolationForestPredict() {
        IsolationForest isoForest = new IsolationForest(10, 8, 0.1);

        double[][] trainData = new double[50][2];
        for (int i = 0; i < 50; i++) {
            trainData[i][0] = Math.random() * 2 - 1;
            trainData[i][1] = Math.random() * 2 - 1;
        }

        NdArray Xtrain = NdArray.array(flatten(trainData), DType.FLOAT32).reshape(50, 2);
        isoForest.fit(Xtrain);

        // Create test data
        double[][] testData = new double[10][2];
        for (int i = 0; i < 10; i++) {
            testData[i][0] = Math.random() * 2 - 1;
            testData[i][1] = Math.random() * 2 - 1;
        }

        NdArray Xtest = NdArray.array(flatten(testData), DType.FLOAT32).reshape(10, 2);
        int[] predictions = isoForest.predict(Xtest);

        assertNotNull(predictions);
        assertEquals(10, predictions.length);

        // All predictions should be 1 (normal) or -1 (anomaly)
        for (int pred : predictions) {
            assertTrue(pred == 1 || pred == -1, "Predictions should be 1 or -1");
        }
    }

    @Test
    /**
     * testIsolationForestScores方法。
     */
    public void testIsolationForestScores() {
        IsolationForest isoForest = new IsolationForest(10, 8, 0.1);

        double[][] data = new double[50][2];
        for (int i = 0; i < 50; i++) {
            data[i][0] = Math.random() * 2 - 1;
            data[i][1] = Math.random() * 2 - 1;
        }

        NdArray X = NdArray.array(flatten(data), DType.FLOAT32).reshape(50, 2);
        isoForest.fit(X);

        double[] scores = isoForest.score(X);

        assertNotNull(scores);
        assertEquals(50, scores.length);

        // All scores should be between 0 and 1
        for (double score : scores) {
            assertTrue(score >= 0 && score <= 1, "Scores should be between 0 and 1");
        }
    }
}

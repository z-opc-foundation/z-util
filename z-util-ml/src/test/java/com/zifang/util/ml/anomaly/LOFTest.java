package com.zifang.util.ml.anomaly;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LOFTest类。
 */
public class LOFTest {

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
     * testLOFFit方法。
     */
    public void testLOFFit() {
        LOF lof = new LOF(5, 0.1);

        // Create simple training data (50 samples, 2 features)
        double[][] data = new double[50][2];
        for (int i = 0; i < 50; i++) {
            data[i][0] = Math.random() * 2 - 1;
            data[i][1] = Math.random() * 2 - 1;
        }

        NdArray X = NdArray.array(flatten(data), DType.FLOAT32).reshape(50, 2);

        // Fit should not throw
        lof.fit(X);
    }

    @Test
    /**
     * testLOFPredict方法。
     */
    public void testLOFPredict() {
        LOF lof = new LOF(5, 0.1);

        double[][] trainData = new double[50][2];
        for (int i = 0; i < 50; i++) {
            trainData[i][0] = Math.random() * 2 - 1;
            trainData[i][1] = Math.random() * 2 - 1;
        }

        NdArray Xtrain = NdArray.array(flatten(trainData), DType.FLOAT32).reshape(50, 2);
        lof.fit(Xtrain);

        // Create test data
        double[][] testData = new double[10][2];
        for (int i = 0; i < 10; i++) {
            testData[i][0] = Math.random() * 2 - 1;
            testData[i][1] = Math.random() * 2 - 1;
        }

        NdArray Xtest = NdArray.array(flatten(testData), DType.FLOAT32).reshape(10, 2);
        int[] predictions = lof.predict(Xtest);

        assertNotNull(predictions);
        assertEquals(10, predictions.length);

        // All predictions should be 1 (normal) or -1 (anomaly)
        for (int pred : predictions) {
            assertTrue(pred == 1 || pred == -1, "Predictions should be 1 or -1");
        }
    }

    @Test
    /**
     * testLOFScores方法。
     */
    public void testLOFScores() {
        LOF lof = new LOF(5, 0.1);

        double[][] data = new double[50][2];
        for (int i = 0; i < 50; i++) {
            data[i][0] = Math.random() * 2 - 1;
            data[i][1] = Math.random() * 2 - 1;
        }

        NdArray X = NdArray.array(flatten(data), DType.FLOAT32).reshape(50, 2);
        lof.fit(X);

        double[] scores = lof.score(X);

        assertNotNull(scores);
        assertEquals(50, scores.length);

        // LOF scores should be non-negative
        for (double score : scores) {
            assertTrue(score >= 0, "LOF scores should be non-negative");
        }
    }
}

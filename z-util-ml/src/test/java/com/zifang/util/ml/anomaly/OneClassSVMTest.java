package com.zifang.util.ml.anomaly;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OneClassSVMTest类。
 */
public class OneClassSVMTest {

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
     * testOneClassSVMFit方法。
     */
    public void testOneClassSVMFit() {
        OneClassSVM ocsvm = new OneClassSVM(0.1, "RBF", 0.5);

        // Create simple training data (50 samples, 2 features)
        double[][] data = new double[50][2];
        for (int i = 0; i < 50; i++) {
            data[i][0] = Math.random() * 2 - 1;
            data[i][1] = Math.random() * 2 - 1;
        }

        NdArray X = NdArray.array(flatten(data), DType.FLOAT32).reshape(50, 2);

        // Fit should not throw
        ocsvm.fit(X);
    }

    @Test
    /**
     * testOneClassSVMPredict方法。
     */
    public void testOneClassSVMPredict() {
        OneClassSVM ocsvm = new OneClassSVM(0.1, "RBF", 0.5);

        double[][] trainData = new double[50][2];
        for (int i = 0; i < 50; i++) {
            trainData[i][0] = Math.random() * 2 - 1;
            trainData[i][1] = Math.random() * 2 - 1;
        }

        NdArray Xtrain = NdArray.array(flatten(trainData), DType.FLOAT32).reshape(50, 2);
        ocsvm.fit(Xtrain);

        // Create test data
        double[][] testData = new double[10][2];
        for (int i = 0; i < 10; i++) {
            testData[i][0] = Math.random() * 2 - 1;
            testData[i][1] = Math.random() * 2 - 1;
        }

        NdArray Xtest = NdArray.array(flatten(testData), DType.FLOAT32).reshape(10, 2);
        int[] predictions = ocsvm.predict(Xtest);

        assertNotNull(predictions);
        assertEquals(10, predictions.length);

        // All predictions should be 1 (normal) or -1 (anomaly)
        for (int pred : predictions) {
            assertTrue(pred == 1 || pred == -1, "Predictions should be 1 or -1");
        }
    }

    @Test
    /**
     * testOneClassSVMLinearKernel方法。
     */
    public void testOneClassSVMLinearKernel() {
        OneClassSVM ocsvm = new OneClassSVM(0.1, "LINEAR", 0.5);

        double[][] data = new double[30][3];
        for (int i = 0; i < 30; i++) {
            data[i][0] = Math.random() * 2 - 1;
            data[i][1] = Math.random() * 2 - 1;
            data[i][2] = Math.random() * 2 - 1;
        }

        NdArray X = NdArray.array(flatten(data), DType.FLOAT32).reshape(30, 3);
        ocsvm.fit(X);

        int[] predictions = ocsvm.predict(X);

        assertNotNull(predictions);
        assertEquals(30, predictions.length);
    }
}

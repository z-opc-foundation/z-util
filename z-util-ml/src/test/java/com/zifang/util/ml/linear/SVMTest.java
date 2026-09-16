package com.zifang.util.ml.linear;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SVMTest类。
 */
public class SVMTest {

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

    private DataWithLabels generateLinearlySeparableDataWithLabels(int nSamples, Random rnd) {
        double[][] data = new double[nSamples][2];
        int[] labels = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            double x = rnd.nextDouble() * 10 - 5;
            double y = rnd.nextDouble() * 10 - 5;
            data[i][0] = x;
            data[i][1] = y;
            labels[i] = (x > 0) ? 1 : -1;
        }
        return new DataWithLabels(createNdArray(data, nSamples, 2), labels);
    }

    @Test
    /**
     * testSVMLinear方法。
     */
    public void testSVMLinear() {
        int nSamples = 100;
        DataWithLabels dwl = generateLinearlySeparableDataWithLabels(nSamples, new Random(42));
        NdArray X = dwl.X;
        int[] y = dwl.y;

        SVM svm = new SVM(0.01, 1000, 0.1, SVM.KernelType.LINEAR);
        svm.fit(X, y);

        int[] predictions = svm.predict(X);

        assertEquals(nSamples, predictions.length);

        // All predictions should be +1 or -1
        for (int pred : predictions) {
            assertTrue(pred == 1 || pred == -1, "Predictions should be +1 or -1");
        }

        // Calculate accuracy
        int correct = 0;
        for (int i = 0; i < nSamples; i++) {
            if (predictions[i] == y[i]) {
                correct++;
            }
        }
        double accuracy = (double) correct / nSamples;
        assertTrue(accuracy > 0.85, "Linear SVM accuracy should be > 0.85, got: " + accuracy);
    }

    @Test
    /**
     * testSVMRBF方法。
     */
    public void testSVMRBF() {
        // Create circular data where linear SVM would struggle
        int nSamples = 60;
        double[][] data = new double[nSamples][2];
        int[] labels = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * 2;

            if (i < nSamples / 2) {
                // Inner circle - label -1
                data[i][0] = radius * Math.cos(angle);
                data[i][1] = radius * Math.sin(angle);
                labels[i] = -1;
            } else {
                // Outer circle - label +1
                data[i][0] = (radius + 3) * Math.cos(angle);
                data[i][1] = (radius + 3) * Math.sin(angle);
                labels[i] = 1;
            }
        }

        NdArray X = createNdArray(data, nSamples, 2);

        SVM svm = new SVM(0.01, 500, 0.1, SVM.KernelType.RBF);
        svm.setGamma(0.1);
        svm.fit(X, labels);

        int[] predictions = svm.predict(X);

        assertEquals(nSamples, predictions.length);

        // All predictions should be +1 or -1
        for (int pred : predictions) {
            assertTrue(pred == 1 || pred == -1);
        }
    }

    @Test
    /**
     * testSVMPredict方法。
     */
    public void testSVMPredict() {
        int nSamples = 40;
        DataWithLabels dwl = generateLinearlySeparableDataWithLabels(nSamples, new Random(42));
        NdArray X = dwl.X;
        int[] y = dwl.y;

        SVM svm = new SVM(0.01, 500, 0.1, SVM.KernelType.LINEAR);
        svm.fit(X, y);

        int[] predictions = svm.predict(X);

        assertEquals(nSamples, predictions.length);

        for (int pred : predictions) {
            assertTrue(pred == 1 || pred == -1, "Predictions should be +1 or -1");
        }
    }

    /**
     * Generate linearly separable data AND labels together (two classes separated by x > 0).
     */
    private static class DataWithLabels {
        NdArray X;
        int[] y;

        DataWithLabels(NdArray X, int[] y) {
            this.X = X;
            this.y = y;
        }
    }
}

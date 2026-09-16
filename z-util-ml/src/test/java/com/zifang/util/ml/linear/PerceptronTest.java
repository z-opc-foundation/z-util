package com.zifang.util.ml.linear;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PerceptronTest类。
 */
public class PerceptronTest {

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
            labels[i] = (x > 0) ? 1 : 0;
        }
        return new DataWithLabels(createNdArray(data, nSamples, 2), labels);
    }

    @Test
    /**
     * testPerceptronConvergence方法。
     */
    public void testPerceptronConvergence() {
        int nSamples = 80;
        DataWithLabels dwl = generateLinearlySeparableDataWithLabels(nSamples, new Random(42));
        NdArray X = dwl.X;
        int[] y = dwl.y;

        Perceptron perceptron = new Perceptron(0.1, 100);
        perceptron.fit(X, y);

        int[] predictions = perceptron.predict(X);

        assertEquals(nSamples, predictions.length);

        // For linearly separable data, perceptron should achieve good accuracy
        int correct = 0;
        for (int i = 0; i < nSamples; i++) {
            if (predictions[i] == y[i]) {
                correct++;
            }
        }
        double accuracy = (double) correct / nSamples;
        assertTrue(accuracy > 0.8, "Perceptron should converge on linearly separable data");
    }

    @Test
    /**
     * testPerceptronPredict方法。
     */
    public void testPerceptronPredict() {
        int nSamples = 50;
        DataWithLabels dwl = generateLinearlySeparableDataWithLabels(nSamples, new Random(42));
        NdArray X = dwl.X;
        int[] y = dwl.y;

        Perceptron perceptron = new Perceptron(0.1, 50);
        perceptron.fit(X, y);

        int[] predictions = perceptron.predict(X);

        assertEquals(nSamples, predictions.length);

        // All predictions should be 0 or 1
        for (int pred : predictions) {
            assertTrue(pred == 0 || pred == 1);
        }
    }

    @Test
    /**
     * testPerceptronScore方法。
     */
    public void testPerceptronScore() {
        int nSamples = 60;
        DataWithLabels dwl = generateLinearlySeparableDataWithLabels(nSamples, new Random(42));
        NdArray X = dwl.X;
        int[] y = dwl.y;

        Perceptron perceptron = new Perceptron(0.1, 100);
        perceptron.fit(X, y);

        double score = perceptron.score(X, y);

        assertTrue(score >= 0.0 && score <= 1.0, "Score should be between 0 and 1, got: " + score);
        assertTrue(score > 0.5, "Score should be better than random for linearly separable data");
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

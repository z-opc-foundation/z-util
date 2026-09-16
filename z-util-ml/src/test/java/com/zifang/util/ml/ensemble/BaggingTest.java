package com.zifang.util.ml.ensemble;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Bagging ensemble classifier.
 */
class BaggingTest {

    @Test
    void testBaggingFit() {
        // Create synthetic training data: 20 samples, 2 features, 3 classes
        int nSamples = 20;
        int nFeatures = 2;
        int nClasses = 3;

        // Generate random feature data
        double[] flatX = new double[nSamples * nFeatures];
        Random rand = new Random(42);
        for (int i = 0; i < flatX.length; i++) {
            flatX[i] = rand.nextDouble() * 10;
        }
        NdArray X = NdArray.array(flatX, DType.FLOAT32).reshape(nSamples, nFeatures);

        // Generate random labels (class indices 0, 1, 2)
        int[] y = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            y[i] = rand.nextInt(nClasses);
        }

        // Create and fit Bagging
        Bagging bagging = new Bagging(5, 5, 2);
        bagging.fit(X, y);

        // Verify estimators were created
        assertEquals(5, bagging.getNEstimators());
        assertEquals(5, bagging.getEstimators().size());
    }

    @Test
    void testBaggingAccuracy() {
        // Create simple linearly separable data
        int nSamples = 30;
        int nFeatures = 2;

        // Generate data for 2 classes
        double[] flatX = new double[nSamples * nFeatures];
        int[] y = new int[nSamples];

        Random rand = new Random(123);

        // Class 0: clustered around (0, 0)
        for (int i = 0; i < nSamples / 2; i++) {
            flatX[i * nFeatures] = rand.nextGaussian() * 0.5;
            flatX[i * nFeatures + 1] = rand.nextGaussian() * 0.5;
            y[i] = 0;
        }

        // Class 1: clustered around (5, 5)
        for (int i = nSamples / 2; i < nSamples; i++) {
            flatX[i * nFeatures] = 5 + rand.nextGaussian() * 0.5;
            flatX[i * nFeatures + 1] = 5 + rand.nextGaussian() * 0.5;
            y[i] = 1;
        }

        NdArray X = NdArray.array(flatX, DType.FLOAT32).reshape(nSamples, nFeatures);

        // Fit Bagging
        Bagging bagging = new Bagging(10, 10, 2);
        bagging.fit(X, y);

        // Predict
        int[] predictions = bagging.predict(X);

        // Check predictions have correct size
        assertEquals(nSamples, predictions.length);

        // For simple separable data, accuracy should be reasonable
        int correct = 0;
        for (int i = 0; i < nSamples; i++) {
            if (predictions[i] == y[i]) {
                correct++;
            }
        }
        double accuracy = (double) correct / nSamples;
        assertTrue(accuracy > 0.5, "Accuracy should be better than random: " + accuracy);
    }

    @Test
    void testBaggingPredictProba() {
        // Create synthetic data
        int nSamples = 20;
        int nFeatures = 2;
        int nClasses = 2;

        double[] flatX = new double[nSamples * nFeatures];
        Random rand = new Random(42);
        for (int i = 0; i < flatX.length; i++) {
            flatX[i] = rand.nextDouble();
        }
        NdArray X = NdArray.array(flatX, DType.FLOAT32).reshape(nSamples, nFeatures);

        int[] y = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            y[i] = rand.nextInt(nClasses);
        }

        Bagging bagging = new Bagging(5, 5, 2);
        bagging.fit(X, y);

        // Get probability predictions
        NdArray proba = bagging.predictProba(X);

        // Verify probabilities shape
        assertEquals(nSamples, proba.getShape().get(0));
        assertEquals(nClasses, proba.getShape().get(1));

        // Verify probabilities sum to approximately 1
        Object data = proba.getData();
        for (int i = 0; i < nSamples; i++) {
            float sum = 0;
            for (int c = 0; c < nClasses; c++) {
                float val = ((Number) com.zifang.util.numpy.Array.get(data, i * nClasses + c)).floatValue();
                sum += val;
            }
            assertTrue(Math.abs(sum - 1.0f) < 0.01f, "Probabilities should sum to 1");
        }
    }
}
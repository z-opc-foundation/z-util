package com.zifang.util.ml.linear;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LinearRegressionTest类。
 */
public class LinearRegressionTest {

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

    @Test
    /**
     * testLinearRegressionFit方法。
     */
    public void testLinearRegressionFit() {
        int nSamples = 100;
        double trueSlope = 2.0;
        double trueIntercept = 1.0;

        double[][] Xdata = new double[nSamples][1];
        double[] y = new double[nSamples];

        for (int i = 0; i < nSamples; i++) {
            double x = random.nextDouble() * 10;
            double noise = random.nextGaussian() * 0.5;
            Xdata[i][0] = x;
            y[i] = trueSlope * x + trueIntercept + noise;
        }

        NdArray X = createNdArray(Xdata, nSamples, 1);

        LinearRegression lr = new LinearRegression(0.0);
        lr.fit(X, y);

        NdArray predictions = lr.predict(X);

        assertEquals(nSamples, predictions.getShape().get(0));

        // Check predictions are reasonable (should be close to actual y values)
        double totalError = 0;
        for (int i = 0; i < nSamples; i++) {
            double pred = ((Number) predictions.get(i)).doubleValue();
            totalError += Math.abs(pred - y[i]);
        }
        double avgError = totalError / nSamples;
        assertTrue(avgError < 2.0, "Average prediction error should be < 2.0");
    }

    @Test
    /**
     * testLinearRegressionRidge方法。
     */
    public void testLinearRegressionRidge() {
        int nSamples = 50;

        double[][] Xdata = new double[nSamples][2];
        double[] y = new double[nSamples];

        for (int i = 0; i < nSamples; i++) {
            double x1 = random.nextDouble() * 10;
            double x2 = random.nextDouble() * 10;
            Xdata[i][0] = x1;
            Xdata[i][1] = x2;
            y[i] = 2 * x1 + 3 * x2 + 1;
        }

        NdArray X = createNdArray(Xdata, nSamples, 2);

        // With regularization
        LinearRegression lrReg = new LinearRegression(1.0);
        lrReg.fit(X, y);

        // Without regularization
        LinearRegression lrNoReg = new LinearRegression(0.0);
        lrNoReg.fit(X, y);

        NdArray predReg = lrReg.predict(X);
        NdArray predNoReg = lrNoReg.predict(X);

        assertEquals(nSamples, predReg.getShape().get(0));
        assertEquals(nSamples, predNoReg.getShape().get(0));

        // Both should give reasonable predictions
        for (int i = 0; i < nSamples; i++) {
            double pReg = ((Number) predReg.get(i)).doubleValue();
            double pNoReg = ((Number) predNoReg.get(i)).doubleValue();
            assertTrue(pReg > 0, "Regularized prediction should be positive");
            assertTrue(pNoReg > 0, "Non-regularized prediction should be positive");
        }
    }

    @Test
    /**
     * testLinearRegressionMultiTarget方法。
     */
    public void testLinearRegressionMultiTarget() {
        int nSamples = 40;
        int nTargets = 3;

        double[][] Xdata = new double[nSamples][2];
        double[][] ydata = new double[nSamples][nTargets];

        for (int i = 0; i < nSamples; i++) {
            double x1 = random.nextDouble() * 5;
            double x2 = random.nextDouble() * 5;
            Xdata[i][0] = x1;
            Xdata[i][1] = x2;

            ydata[i][0] = 2 * x1 + 1;
            ydata[i][1] = 3 * x2 + 2;
            ydata[i][2] = x1 + x2 + 3;
        }

        NdArray X = createNdArray(Xdata, nSamples, 2);
        NdArray y = createNdArray(ydata, nSamples, nTargets);

        LinearRegression lr = new LinearRegression(0.0);
        lr.fit(X, y);

        NdArray predictions = lr.predict(X);

        assertEquals(nSamples, predictions.getShape().get(0));
        assertEquals(nTargets, predictions.getShape().get(1));

        // Verify predictions are reasonable
        for (int i = 0; i < nSamples; i++) {
            for (int t = 0; t < nTargets; t++) {
                double pred = ((Number) predictions.get(i, t)).doubleValue();
                // Each prediction should be positive for our synthetic data
                assertTrue(pred > -10, "Prediction should be reasonable");
            }
        }
    }
}

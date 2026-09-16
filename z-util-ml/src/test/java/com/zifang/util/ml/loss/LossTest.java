package com.zifang.util.ml.loss;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for loss functions: MSELoss and CrossEntropyLoss.
 */
class LossTest {

    @Test
    void testMSELoss() {
        MSELoss mseLoss = new MSELoss();

        int size = 4;

        // Predictions: [1, 2, 3, 4]
        double[] predValues = {1.0, 2.0, 3.0, 4.0};
        NdArray predictions = NdArray.array(predValues, DType.FLOAT64).reshape(1, size);

        // Targets: [1, 2, 3, 4] (same as predictions -> MSE should be 0)
        double[] targetValues = {1.0, 2.0, 3.0, 4.0};
        NdArray targets = NdArray.array(targetValues, DType.FLOAT64).reshape(1, size);

        // Compute loss
        NdArray loss = mseLoss.compute(predictions, targets);

        // Loss should be approximately 0
        Object lossData = loss.getData();
        double lossValue = (double) com.zifang.util.numpy.Array.get(lossData, 0);
        assertEquals(0.0, lossValue, 0.001);
    }

    @Test
    void testMSELossNonZero() {
        MSELoss mseLoss = new MSELoss();

        int size = 4;

        // Predictions: [1, 2, 3, 4]
        double[] predValues = {1.0, 2.0, 3.0, 4.0};
        NdArray predictions = NdArray.array(predValues, DType.FLOAT64).reshape(1, size);

        // Targets: [2, 3, 4, 5] (each off by 1)
        double[] targetValues = {2.0, 3.0, 4.0, 5.0};
        NdArray targets = NdArray.array(targetValues, DType.FLOAT64).reshape(1, size);

        // Compute loss
        NdArray loss = mseLoss.compute(predictions, targets);

        // MSE = (1 + 1 + 1 + 1) / 4 = 1
        Object lossData = loss.getData();
        double lossValue = (double) com.zifang.util.numpy.Array.get(lossData, 0);
        assertEquals(1.0, lossValue, 0.001);
    }

    @Test
    void testMSELossGradient() {
        MSELoss mseLoss = new MSELoss();

        int size = 4;

        // Predictions
        double[] predValues = {1.0, 2.0, 3.0, 4.0};
        NdArray predictions = NdArray.array(predValues, DType.FLOAT64).reshape(1, size);

        // Targets
        double[] targetValues = {2.0, 3.0, 4.0, 5.0};
        NdArray targets = NdArray.array(targetValues, DType.FLOAT64).reshape(1, size);

        // Compute gradient
        NdArray gradient = mseLoss.gradient(predictions, targets);

        // Gradient = 2 * (pred - target) / n = 2 * (-1, -1, -1, -1) / 4 = (-0.5, -0.5, -0.5, -0.5)
        Object gradData = gradient.getData();
        for (int i = 0; i < size; i++) {
            double gradValue = (double) com.zifang.util.numpy.Array.get(gradData, i);
            assertEquals(-0.5, gradValue, 0.001);
        }
    }

    @Test
    void testCrossEntropyLoss() {
        CrossEntropyLoss celLoss = new CrossEntropyLoss();

        int batchSize = 2;
        int numClasses = 3;

        // Predictions (logits before softmax)
        // For class 0: [2.0, 1.0, 0.5]
        // For class 1: [0.5, 2.0, 1.0]
        double[] predValues = {2.0, 1.0, 0.5, 0.5, 2.0, 1.0};
        NdArray predictions = NdArray.array(predValues, DType.FLOAT64).reshape(batchSize, numClasses);

        // Targets (one-hot encoded)
        // Class 0 for sample 0, Class 1 for sample 1
        double[] targetValues = {1.0, 0.0, 0.0, 0.0, 1.0, 0.0};
        NdArray targets = NdArray.array(targetValues, DType.FLOAT64).reshape(batchSize, numClasses);

        // Compute loss
        NdArray loss = celLoss.compute(predictions, targets);

        // Loss should be positive
        Object lossData = loss.getData();
        double lossValue = (double) com.zifang.util.numpy.Array.get(lossData, 0);
        assertTrue(lossValue > 0, "CrossEntropyLoss should be positive");
    }

    @Test
    void testCrossEntropyLossGradient() {
        CrossEntropyLoss celLoss = new CrossEntropyLoss();

        int batchSize = 2;
        int numClasses = 3;

        // Predictions (logits)
        double[] predValues = {2.0, 1.0, 0.5, 0.5, 2.0, 1.0};
        NdArray predictions = NdArray.array(predValues, DType.FLOAT64).reshape(batchSize, numClasses);

        // Targets (one-hot)
        double[] targetValues = {1.0, 0.0, 0.0, 0.0, 1.0, 0.0};
        NdArray targets = NdArray.array(targetValues, DType.FLOAT64).reshape(batchSize, numClasses);

        // Compute gradient
        NdArray gradient = celLoss.gradient(predictions, targets);

        // Gradient shape should match predictions
        assertEquals(batchSize, gradient.getShape().get(0));
        assertEquals(numClasses, gradient.getShape().get(1));
    }

    @Test
    void testLossShape() {
        MSELoss mseLoss = new MSELoss();
        CrossEntropyLoss celLoss = new CrossEntropyLoss();

        int batchSize = 4;
        int numClasses = 3;
        int features = 8;

        // MSE predictions and targets
        double[] msePred = new double[batchSize * features];
        double[] mseTarget = new double[batchSize * features];
        Random rand = new Random(42);
        for (int i = 0; i < msePred.length; i++) {
            msePred[i] = rand.nextDouble();
            mseTarget[i] = rand.nextDouble();
        }

        NdArray msePredictions = NdArray.array(msePred, DType.FLOAT64).reshape(batchSize, features);
        NdArray mseTargets = NdArray.array(mseTarget, DType.FLOAT64).reshape(batchSize, features);

        NdArray mseLossOutput = mseLoss.compute(msePredictions, mseTargets);
        NdArray mseGradient = mseLoss.gradient(msePredictions, mseTargets);

        // Loss is scalar
        assertEquals(1, mseLossOutput.getShape().get(0));

        // Gradient has same shape as predictions
        assertEquals(batchSize, mseGradient.getShape().get(0));
        assertEquals(features, mseGradient.getShape().get(1));

        // CE predictions and targets
        double[] cePred = new double[batchSize * numClasses];
        double[] ceTarget = new double[batchSize * numClasses];
        for (int i = 0; i < cePred.length; i++) {
            cePred[i] = rand.nextDouble();
            ceTarget[i] = (i % numClasses == 0) ? 1.0 : 0.0;
        }

        NdArray cePredictions = NdArray.array(cePred, DType.FLOAT64).reshape(batchSize, numClasses);
        NdArray ceTargets = NdArray.array(ceTarget, DType.FLOAT64).reshape(batchSize, numClasses);

        NdArray ceLossOutput = celLoss.compute(cePredictions, ceTargets);
        NdArray ceGradient = celLoss.gradient(cePredictions, ceTargets);

        // Loss is scalar
        assertEquals(1, ceLossOutput.getShape().get(0));

        // Gradient has same shape as predictions
        assertEquals(batchSize, ceGradient.getShape().get(0));
        assertEquals(numClasses, ceGradient.getShape().get(1));
    }
}

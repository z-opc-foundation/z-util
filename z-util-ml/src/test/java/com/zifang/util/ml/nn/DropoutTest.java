package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Dropout layer.
 */
class DropoutTest {

    @Test
    void testDropoutTrainEval() {
        int batchSize = 4;
        int features = 16;

        Dropout dropout = new Dropout(0.5f);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, features), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Train mode - should apply dropout
        dropout.train();
        assertTrue(dropout.isTraining());

        NdArray trainOutput = dropout.forward(input);

        // In train mode, some values should be zeroed or scaled
        // We can't test exact values due to randomness, but we can verify
        // that not all values are the same as input (dropout was applied)
        Object trainData = trainOutput.getData();
        boolean dropoutApplied = false;
        for (int i = 0; i < input.size(); i++) {
            double inVal = ((Number) com.zifang.util.numpy.Array.get(data, i)).doubleValue();
            double outVal = ((Number) com.zifang.util.numpy.Array.get(trainData, i)).doubleValue();
            // In train mode with p=0.5, values should either be 0 or scaled by 2
            if (inVal != outVal) {
                dropoutApplied = true;
                break;
            }
        }
        // With high probability, dropout should be applied
        assertTrue(dropoutApplied);

        // Eval mode - should not apply dropout (just scale)
        dropout.eval();
        assertFalse(dropout.isTraining());

        NdArray evalOutput = dropout.forward(input);

        // In eval mode, output should be input scaled by (1-p)
        Object evalData = evalOutput.getData();
        for (int i = 0; i < input.size(); i++) {
            double inVal = ((Number) com.zifang.util.numpy.Array.get(data, i)).doubleValue();
            double outVal = ((Number) com.zifang.util.numpy.Array.get(evalData, i)).doubleValue();
            // In eval mode with p=0.5, output = input * 0.5
            assertEquals(inVal * 0.5f, outVal, 0.001);
        }
    }

    @Test
    void testDropoutForwardShape() {
        int batchSize = 4;
        int features = 16;

        Dropout dropout = new Dropout(0.5f);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, features), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = dropout.forward(input);

        // Output shape should match input shape
        assertEquals(input.getShape().get(0), output.getShape().get(0));
        assertEquals(input.getShape().get(1), output.getShape().get(1));
    }

    @Test
    void testDropoutBackward() {
        int batchSize = 4;
        int features = 16;

        Dropout dropout = new Dropout(0.5f);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, features), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Train mode
        dropout.train();

        // Forward pass
        NdArray output = dropout.forward(input);

        // Create upstream gradient
        NdArray gradOutput = NdArray.zeros(new Shape(batchSize, features), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = dropout.backward(gradOutput);

        // Gradient shape should match input shape
        assertEquals(input.getShape().get(0), gradInput.getShape().get(0));
        assertEquals(input.getShape().get(1), gradInput.getShape().get(1));
    }

    @Test
    void testDropoutZeroProbability() {
        int batchSize = 4;
        int features = 16;

        // p = 0 means no dropout
        Dropout dropout = new Dropout(0.0f);
        dropout.train();

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, features), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = dropout.forward(input);

        // With p=0, output should equal input scaled by 1 (no change)
        Object outData = output.getData();
        for (int i = 0; i < input.size(); i++) {
            double inVal = ((Number) com.zifang.util.numpy.Array.get(data, i)).doubleValue();
            double outVal = ((Number) com.zifang.util.numpy.Array.get(outData, i)).doubleValue();
            assertEquals(inVal, outVal, 0.001);
        }
    }
}
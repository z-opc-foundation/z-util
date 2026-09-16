package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LSTM layer.
 */
class LSTMTest {

    @Test
    void testLSTMForwardShape() {
        int inputSize = 8;
        int hiddenSize = 16;
        int numLayers = 2;
        int seqLen = 4;
        int batchSize = 2;

        LSTM lstm = new LSTM(inputSize, hiddenSize, numLayers);

        // Create input: [seqLen, batch, inputSize]
        NdArray input = NdArray.zeros(new Shape(seqLen, batchSize, inputSize), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = lstm.forward(input);

        // Output shape: [seqLen, batch, hiddenSize]
        assertEquals(seqLen, output.getShape().get(0));
        assertEquals(batchSize, output.getShape().get(1));
        assertEquals(hiddenSize, output.getShape().get(2));
    }

    @Test
    void testLSTMForwardSingleLayer() {
        int inputSize = 4;
        int hiddenSize = 8;
        int numLayers = 1;
        int seqLen = 3;
        int batchSize = 1;

        LSTM lstm = new LSTM(inputSize, hiddenSize, numLayers);

        // Create input
        NdArray input = NdArray.zeros(new Shape(seqLen, batchSize, inputSize), DType.FLOAT32);
        Random rand = new Random(123);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = lstm.forward(input);

        // Verify output shape
        assertEquals(seqLen, output.getShape().get(0));
        assertEquals(batchSize, output.getShape().get(1));
        assertEquals(hiddenSize, output.getShape().get(2));

        // Verify output values are in valid range (tanh output is in [-1, 1])
        Object outData = output.getData();
        for (int i = 0; i < output.size(); i++) {
            double val = ((Number) com.zifang.util.numpy.Array.get(outData, i)).doubleValue();
            assertTrue(val >= -1.0f && val <= 1.0f, "LSTM output should be in [-1, 1]");
        }
    }

    @Test
    void testLSTMBackward() {
        int inputSize = 4;
        int hiddenSize = 8;
        int numLayers = 1;
        int seqLen = 3;
        int batchSize = 2;

        LSTM lstm = new LSTM(inputSize, hiddenSize, numLayers);

        // Create input
        NdArray input = NdArray.zeros(new Shape(seqLen, batchSize, inputSize), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = lstm.forward(input);

        // Create upstream gradient
        NdArray gradOutput = NdArray.zeros(new Shape(seqLen, batchSize, hiddenSize), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = lstm.backward(gradOutput);

        // Gradient shape should match input shape
        assertEquals(seqLen, gradInput.getShape().get(0));
        assertEquals(batchSize, gradInput.getShape().get(1));
        assertEquals(inputSize, gradInput.getShape().get(2));
    }

    @Test
    void testLSTMParameters() {
        int inputSize = 4;
        int hiddenSize = 8;
        int numLayers = 2;

        LSTM lstm = new LSTM(inputSize, hiddenSize, numLayers);

        // Verify parameters are registered
        assertFalse(lstm.parameters().isEmpty());

        // For 2-layer LSTM: each layer has 4 gates (i, f, o, g)
        // Each gate has: weight_ii, weight_hi, bias_ii (and similar for f, o, g)
        // So for 2 layers, we expect many parameters
        assertTrue(lstm.parameters().size() >= numLayers * 12); // 4 gates * 3 params each per layer
    }
}
package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Dense (fully connected) layer.
 */
class DenseTest {

    @Test
    void testDenseForward() {
        int inputFeatures = 8;
        int outputFeatures = 16;
        int batchSize = 4;

        Dense dense = new Dense(inputFeatures, outputFeatures);

        // Create input: (batchSize, inputFeatures)
        NdArray input = NdArray.zeros(new Shape(batchSize, inputFeatures), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = dense.forward(input);

        // Verify output shape
        assertEquals(batchSize, output.getShape().get(0));
        assertEquals(outputFeatures, output.getShape().get(1));
    }

    @Test
    void testDenseWeights() {
        int inputFeatures = 8;
        int outputFeatures = 16;

        Dense dense = new Dense(inputFeatures, outputFeatures);

        // Verify weight shape
        NdArray weight = dense.getWeight();
        assertEquals(outputFeatures, weight.getShape().get(0));
        assertEquals(inputFeatures, weight.getShape().get(1));

        // Verify bias shape
        NdArray bias = dense.getBias();
        assertEquals(outputFeatures, bias.getShape().get(0));

        // Verify weights are registered as parameters
        assertFalse(dense.parameters().isEmpty());
        assertTrue(dense.parameters().size() >= 2); // weight and bias
    }

    @Test
    void testDenseForward1D() {
        int inputFeatures = 8;
        int outputFeatures = 16;

        Dense dense = new Dense(inputFeatures, outputFeatures);

        // Create 1D input
        NdArray input = NdArray.zeros(new Shape(inputFeatures), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = dense.forward(input);

        // Verify output shape: should be (outputFeatures,)
        assertEquals(outputFeatures, output.getShape().get(0));
    }

    @Test
    void testDenseBackward() {
        int inputFeatures = 8;
        int outputFeatures = 16;
        int batchSize = 4;

        Dense dense = new Dense(inputFeatures, outputFeatures);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, inputFeatures), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = dense.forward(input);

        // Create upstream gradient
        NdArray gradOutput = NdArray.zeros(new Shape(batchSize, outputFeatures), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = dense.backward(gradOutput);

        // Verify gradient shape matches input
        assertEquals(batchSize, gradInput.getShape().get(0));
        assertEquals(inputFeatures, gradInput.getShape().get(1));
    }
}
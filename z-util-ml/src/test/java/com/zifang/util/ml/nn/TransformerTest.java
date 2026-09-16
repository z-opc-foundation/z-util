package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TransformerEncoder layer.
 */
class TransformerTest {

    @Test
    void testTransformerForwardShape() {
        int dModel = 16;
        int nhead = 4;
        int dimFeedforward = 32;
        int numLayers = 2;
        int seqLen = 4;
        int batchSize = 2;

        TransformerEncoder transformer = new TransformerEncoder(dModel, nhead, dimFeedforward, numLayers, 0.0);

        // Create input: [seqLen, batch, dModel]
        NdArray input = NdArray.zeros(new Shape(seqLen, batchSize, dModel), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = transformer.forward(input);

        // Output shape: [seqLen, batch, dModel]
        assertEquals(seqLen, output.getShape().get(0));
        assertEquals(batchSize, output.getShape().get(1));
        assertEquals(dModel, output.getShape().get(2));
    }

    @Test
    void testTransformerForwardSingleLayer() {
        int dModel = 8;
        int nhead = 2;
        int dimFeedforward = 16;
        int numLayers = 1;
        int seqLen = 3;
        int batchSize = 1;

        TransformerEncoder transformer = new TransformerEncoder(dModel, nhead, dimFeedforward, numLayers, 0.0);

        // Create input
        NdArray input = NdArray.zeros(new Shape(seqLen, batchSize, dModel), DType.FLOAT32);
        Random rand = new Random(123);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = transformer.forward(input);

        // Verify output shape
        assertEquals(seqLen, output.getShape().get(0));
        assertEquals(batchSize, output.getShape().get(1));
        assertEquals(dModel, output.getShape().get(2));
    }

    @Test
    void testTransformerBackward() {
        int dModel = 8;
        int nhead = 2;
        int dimFeedforward = 16;
        int numLayers = 1;
        int seqLen = 3;
        int batchSize = 2;

        TransformerEncoder transformer = new TransformerEncoder(dModel, nhead, dimFeedforward, numLayers, 0.0);

        // Create input
        NdArray input = NdArray.zeros(new Shape(seqLen, batchSize, dModel), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Forward pass
        NdArray output = transformer.forward(input);

        // Create upstream gradient
        NdArray gradOutput = NdArray.zeros(new Shape(seqLen, batchSize, dModel), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = transformer.backward(gradOutput);

        // Gradient shape should match input shape
        assertEquals(seqLen, gradInput.getShape().get(0));
        assertEquals(batchSize, gradInput.getShape().get(1));
        assertEquals(dModel, gradInput.getShape().get(2));
    }

    @Test
    void testTransformerParameters() {
        int dModel = 8;
        int nhead = 2;
        int dimFeedforward = 16;
        int numLayers = 2;

        TransformerEncoder transformer = new TransformerEncoder(dModel, nhead, dimFeedforward, numLayers, 0.0);

        // Verify parameters are registered
        assertFalse(transformer.parameters().isEmpty());

        // For each layer: Wq, Wk, Wv, Wo (4) + ffLinear1 (weight+bias = 2) + ffLinear2 (weight+bias = 2)
        // + gamma1, beta1, gamma2, beta2 (4)
        // Total per layer: 12 parameters
        assertTrue(transformer.parameters().size() >= numLayers * 12);
    }
}
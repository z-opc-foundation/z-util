package com.zifang.util.ml.nn;

import com.zifang.util.ml.nn.activations.ReLU;
import com.zifang.util.ml.nn.activations.Tanh;
import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for neural network modules (Sequential container).
 */
class ModuleTest {

    @Test
    void testSequentialForward() {
        // Create a simple sequential network: Dense -> ReLU -> Dense
        Dense dense1 = new Dense(8, 16);
        ReLU relu = new ReLU();
        Dense dense2 = new Dense(16, 8);

        Sequential seq = new Sequential(dense1, relu, dense2);

        // Create input: batch size 2, 8 features
        NdArray input = NdArray.zeros(new Shape(2, 8), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = seq.forward(input);

        // Verify output shape: (batch=2, features=8)
        assertEquals(2, output.getShape().get(0));
        assertEquals(8, output.getShape().get(1));
    }

    @Test
    void testSequentialBackward() {
        // Create a simple sequential network
        Dense dense1 = new Dense(8, 16);
        ReLU relu = new ReLU();
        Dense dense2 = new Dense(16, 8);

        Sequential seq = new Sequential(dense1, relu, dense2);

        // Create input
        NdArray input = NdArray.zeros(new Shape(2, 8), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = seq.forward(input);

        // Create upstream gradient (same shape as output)
        NdArray gradOutput = NdArray.zeros(new Shape(2, 8), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = seq.backward(gradOutput);

        // Verify gradient shape matches input shape
        assertEquals(input.getShape().get(0), gradInput.getShape().get(0));
        assertEquals(input.getShape().get(1), gradInput.getShape().get(1));
    }

    @Test
    void testSequentialTrainEval() {
        // Create a simple sequential network with dropout
        Dense dense = new Dense(8, 16);
        Dropout dropout = new Dropout(0.5f);
        Sequential seq = new Sequential(dense, dropout);

        // Create input
        NdArray input = NdArray.zeros(new Shape(2, 8), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Test train mode
        seq.train();
        assertTrue(seq.isTraining());
        NdArray trainOutput = seq.forward(input);

        // Test eval mode
        seq.eval();
        assertFalse(seq.isTraining());
        NdArray evalOutput = seq.forward(input);

        // Output shapes should be the same
        assertEquals(trainOutput.getShape().get(0), evalOutput.getShape().get(0));
        assertEquals(trainOutput.getShape().get(1), evalOutput.getShape().get(1));
    }

    @Test
    void testSequentialSize() {
        Dense dense1 = new Dense(8, 16);
        ReLU relu = new ReLU();
        Dense dense2 = new Dense(16, 8);
        Tanh tanh = new Tanh();

        Sequential seq = new Sequential(dense1, relu, dense2, tanh);

        assertEquals(4, seq.size());
        assertSame(dense1, seq.get(0));
        assertSame(relu, seq.get(1));
        assertSame(dense2, seq.get(2));
        assertSame(tanh, seq.get(3));
    }
}
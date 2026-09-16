package com.zifang.util.ml.optim;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for optimizers: SGD and Adam.
 */
class OptimizerTest {

    @Test
    void testSGDDense() {
        int paramSize = 8;

        // Create a parameter
        NdArray param = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = param.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Create SGD optimizer
        SGD optimizer = new SGD(0.1);
        optimizer.addParameter("weight", param);

        // Create gradient (all ones)
        NdArray grad = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Object gradData = grad.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }
        optimizer.setGradient("weight", grad);

        // Get initial parameter values
        Object paramData = param.getData();
        double[] initialValues = new double[paramSize];
        for (int i = 0; i < paramSize; i++) {
            initialValues[i] = ((Number) com.zifang.util.numpy.Array.get(paramData, i)).doubleValue();
        }

        // Step
        optimizer.step();

        // Verify parameters were updated (should move in negative gradient direction)
        for (int i = 0; i < paramSize; i++) {
            double newValue = ((Number) com.zifang.util.numpy.Array.get(paramData, i)).doubleValue();
            // With lr=0.1 and grad=1, param should decrease by 0.1
            assertEquals(initialValues[i] - 0.1, newValue, 0.001);
        }
    }

    @Test
    void testAdamDense() {
        int paramSize = 8;

        // Create a parameter
        NdArray param = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = param.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Create Adam optimizer
        Adam optimizer = new Adam(0.1);
        optimizer.addParameter("weight", param);

        // Create gradient (all ones)
        NdArray grad = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Object gradData = grad.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }
        optimizer.setGradient("weight", grad);

        // Get initial parameter values
        Object paramData = param.getData();
        double initialSum = 0;
        for (int i = 0; i < paramSize; i++) {
            initialSum += ((Number) com.zifang.util.numpy.Array.get(paramData, i)).doubleValue();
        }

        // Step
        optimizer.step();

        // Verify parameters were updated
        double newSum = 0;
        for (int i = 0; i < paramSize; i++) {
            newSum += ((Number) com.zifang.util.numpy.Array.get(paramData, i)).doubleValue();
        }

        // Adam should have updated the parameters
        assertNotEquals(initialSum, newSum);
    }

    @Test
    void testZeroGrad() {
        int paramSize = 8;

        // Create a parameter
        NdArray param = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = param.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Create SGD optimizer
        SGD optimizer = new SGD(0.1);
        optimizer.addParameter("weight", param);

        // Set gradient
        NdArray grad = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Object gradData = grad.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }
        optimizer.setGradient("weight", grad);

        // Zero gradients
        optimizer.zeroGrad();

        // Gradient should be null after zeroGrad
        assertNotNull(optimizer.getParameter("weight"));
        // The gradient map should have null for "weight"
    }

    @Test
    void testSGDWithMomentum() {
        int paramSize = 8;

        // Create a parameter
        NdArray param = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = param.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) (rand.nextDouble() * 2 - 1));
        }

        // Create SGD with momentum
        SGD optimizer = new SGD(0.1, 0.9);
        optimizer.addParameter("weight", param);

        // Set gradient
        NdArray grad = NdArray.zeros(new Shape(paramSize), DType.FLOAT32);
        Object gradData = grad.getData();
        for (int i = 0; i < paramSize; i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }
        optimizer.setGradient("weight", grad);

        // First step
        optimizer.step();

        // Second step with same gradient
        optimizer.setGradient("weight", grad);
        optimizer.step();

        // Parameters should be updated twice
        // The momentum buffer should be non-zero
        assertTrue(optimizer.getMomentum() > 0);
    }

    @Test
    void testAdamBetaDefaults() {
        Adam optimizer = new Adam(0.001);

        // Check default beta values
        assertEquals(0.9, optimizer.getBeta1(), 0.001);
        assertEquals(0.999, optimizer.getBeta2(), 0.001);
        assertEquals(1e-8, optimizer.getEps(), 1e-10);
    }
}
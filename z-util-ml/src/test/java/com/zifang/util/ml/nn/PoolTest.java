package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for MaxPool2d and AvgPool2d layers.
 */
class PoolTest {

    @Test
    void testMaxPool2dShape() {
        int kernelSize = 2;
        int stride = 2;
        int batchSize = 2;
        int channels = 3;
        int height = 8;
        int width = 8;

        MaxPool2d maxPool = new MaxPool2d(kernelSize, stride);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, channels, height, width), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = maxPool.forward(input);

        // outputH = (8 - 2) / 2 + 1 = 4
        // outputW = (8 - 2) / 2 + 1 = 4
        assertEquals(batchSize, output.getShape().get(0));
        assertEquals(channels, output.getShape().get(1));
        assertEquals(4, output.getShape().get(2));
        assertEquals(4, output.getShape().get(3));
    }

    @Test
    void testMaxPool2dForward() {
        int kernelSize = 2;
        int stride = 2;
        int batchSize = 1;
        int channels = 1;
        int height = 4;
        int width = 4;

        MaxPool2d maxPool = new MaxPool2d(kernelSize, stride);

        // Create input with known values
        // [[1, 2, 3, 4],
        //  [5, 6, 7, 8],
        //  [9, 10, 11, 12],
        //  [13, 14, 15, 16]]
        double[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        NdArray input = NdArray.array(values, DType.FLOAT32).reshape(batchSize, channels, height, width);

        // Forward pass
        NdArray output = maxPool.forward(input);

        // First window [1,2,5,6] should have max of 6
        // Get output value
        Object outData = output.getData();
        double firstMax = ((Number) com.zifang.util.numpy.Array.get(outData, 0)).doubleValue();

        assertEquals(6.0, firstMax, 0.001);
    }

    @Test
    void testMaxPool2dBackward() {
        int kernelSize = 2;
        int stride = 2;
        int batchSize = 1;
        int channels = 1;
        int height = 4;
        int width = 4;

        MaxPool2d maxPool = new MaxPool2d(kernelSize, stride);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, channels, height, width), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = maxPool.forward(input);

        // Create upstream gradient
        NdArray gradOutput = NdArray.zeros(output.getShape(), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = maxPool.backward(gradOutput);

        // Gradient shape should match input shape
        assertEquals(input.getShape().get(0), gradInput.getShape().get(0));
        assertEquals(input.getShape().get(1), gradInput.getShape().get(1));
        assertEquals(input.getShape().get(2), gradInput.getShape().get(2));
        assertEquals(input.getShape().get(3), gradInput.getShape().get(3));
    }

    @Test
    void testAvgPool2dShape() {
        int kernelSize = 2;
        int stride = 2;
        int batchSize = 2;
        int channels = 3;
        int height = 8;
        int width = 8;

        AvgPool2d avgPool = new AvgPool2d(kernelSize, stride);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, channels, height, width), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = avgPool.forward(input);

        // outputH = (8 - 2) / 2 + 1 = 4
        // outputW = (8 - 2) / 2 + 1 = 4
        assertEquals(batchSize, output.getShape().get(0));
        assertEquals(channels, output.getShape().get(1));
        assertEquals(4, output.getShape().get(2));
        assertEquals(4, output.getShape().get(3));
    }

    @Test
    void testAvgPool2dForward() {
        int kernelSize = 2;
        int stride = 2;
        int batchSize = 1;
        int channels = 1;
        int height = 4;
        int width = 4;

        AvgPool2d avgPool = new AvgPool2d(kernelSize, stride);

        // Create input with all ones
        NdArray input = NdArray.zeros(new Shape(batchSize, channels, height, width), DType.FLOAT32);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, 4.0); // All values = 4
        }

        // Forward pass
        NdArray output = avgPool.forward(input);

        // All outputs should be 4 (average of 4s is 4)
        Object outData = output.getData();
        for (int i = 0; i < output.size(); i++) {
            double val = ((Number) com.zifang.util.numpy.Array.get(outData, i)).doubleValue();
            assertEquals(4.0, val, 0.001);
        }
    }

    @Test
    void testAvgPool2dBackward() {
        int kernelSize = 2;
        int stride = 2;
        int batchSize = 1;
        int channels = 1;
        int height = 4;
        int width = 4;

        AvgPool2d avgPool = new AvgPool2d(kernelSize, stride);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, channels, height, width), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = avgPool.forward(input);

        // Create upstream gradient
        NdArray gradOutput = NdArray.zeros(output.getShape(), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = avgPool.backward(gradOutput);

        // Gradient shape should match input shape
        assertEquals(input.getShape().get(0), gradInput.getShape().get(0));
        assertEquals(input.getShape().get(1), gradInput.getShape().get(1));
        assertEquals(input.getShape().get(2), gradInput.getShape().get(2));
        assertEquals(input.getShape().get(3), gradInput.getShape().get(3));
    }
}
package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Conv2d layer.
 */
class Conv2dTest {

    @Test
    void testConv2dOutputShape() {
        int inChannels = 3;
        int outChannels = 8;
        int kernelSize = 3;
        int batchSize = 2;
        int height = 8;
        int width = 8;

        Conv2d conv = new Conv2d(inChannels, outChannels, kernelSize);

        // Create input: (batchSize, channels, height, width)
        NdArray input = NdArray.zeros(new Shape(batchSize, inChannels, height, width), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = conv.forward(input);

        // With default padding = kernelSize/2 = 1, stride = 1
        // outputH = (8 + 2*1 - 3) / 1 + 1 = 8
        // outputW = (8 + 2*1 - 3) / 1 + 1 = 8
        assertEquals(batchSize, output.getShape().get(0));
        assertEquals(outChannels, output.getShape().get(1));
        assertEquals(8, output.getShape().get(2));
        assertEquals(8, output.getShape().get(3));
    }

    @Test
    void testConv2dForward() {
        int inChannels = 1;
        int outChannels = 4;
        int kernelSize = 3;
        int stride = 1;
        int padding = 1;
        int batchSize = 1;
        int height = 4;
        int width = 4;

        Conv2d conv = new Conv2d(inChannels, outChannels, kernelSize, stride, padding);

        // Create input with known values
        NdArray input = NdArray.zeros(new Shape(batchSize, inChannels, height, width), DType.FLOAT32);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, 1.0);
        }

        // Forward pass
        NdArray output = conv.forward(input);

        // Verify output is not all zeros (convolution was applied)
        Object outData = output.getData();
        boolean hasNonZero = false;
        for (int i = 0; i < output.size(); i++) {
            double val = ((Number) com.zifang.util.numpy.Array.get(outData, i)).doubleValue();
            if (val != 0) {
                hasNonZero = true;
                break;
            }
        }
        assertTrue(hasNonZero, "Conv2d output should have non-zero values");

        // Verify weights are initialized
        NdArray weight = conv.getWeight();
        assertEquals(outChannels, weight.getShape().get(0));
        assertEquals(inChannels, weight.getShape().get(1));
        assertEquals(kernelSize, weight.getShape().get(2));
        assertEquals(kernelSize, weight.getShape().get(3));
    }

    @Test
    void testConv2dWithStride() {
        int inChannels = 1;
        int outChannels = 2;
        int kernelSize = 2;
        int stride = 2;
        int padding = 0;
        int batchSize = 1;
        int height = 4;
        int width = 4;

        Conv2d conv = new Conv2d(inChannels, outChannels, kernelSize, stride, padding);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, inChannels, height, width), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = conv.forward(input);

        // outputH = (4 - 2) / 2 + 1 = 2
        // outputW = (4 - 2) / 2 + 1 = 2
        assertEquals(2, output.getShape().get(2));
        assertEquals(2, output.getShape().get(3));
    }

    @Test
    void testConv2dBackward() {
        int inChannels = 1;
        int outChannels = 2;
        int kernelSize = 3;
        int batchSize = 1;
        int height = 4;
        int width = 4;

        Conv2d conv = new Conv2d(inChannels, outChannels, kernelSize);

        // Create input
        NdArray input = NdArray.zeros(new Shape(batchSize, inChannels, height, width), DType.FLOAT32);
        Random rand = new Random(42);
        Object data = input.getData();
        for (int i = 0; i < input.size(); i++) {
            com.zifang.util.numpy.Array.set(data, i, (float) rand.nextDouble());
        }

        // Forward pass
        NdArray output = conv.forward(input);

        // Create upstream gradient
        NdArray gradOutput = NdArray.zeros(output.getShape(), DType.FLOAT32);
        Object gradData = gradOutput.getData();
        for (int i = 0; i < gradOutput.size(); i++) {
            com.zifang.util.numpy.Array.set(gradData, i, 1.0);
        }

        // Backward pass
        NdArray gradInput = conv.backward(gradOutput);

        // Gradient shape should match input shape
        assertEquals(input.getShape().get(0), gradInput.getShape().get(0));
        assertEquals(input.getShape().get(1), gradInput.getShape().get(1));
        assertEquals(input.getShape().get(2), gradInput.getShape().get(2));
        assertEquals(input.getShape().get(3), gradInput.getShape().get(3));
    }
}
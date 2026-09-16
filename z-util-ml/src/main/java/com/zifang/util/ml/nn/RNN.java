package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple RNN (Recurrent Neural Network) cell implementation.
 * h_t = tanh(W_ih @ x_t + W_hh @ h_{t-1} + b_h)
 * <p>
 * Input shape: [seqLen, batch, inputSize]
 * Output shape: [seqLen, batch, hiddenSize]
 */
public class RNN extends Module {

    private int inputSize;
    private int hiddenSize;
    private int numLayers;

    private NdArray[] weightIh;  // Input to hidden weights
    private NdArray[] weightHh;  // Hidden to hidden weights
    private NdArray[] biasH;     // Hidden biases

    private List<NdArray> savedHiddenStates;  // For BPTT
    private List<NdArray> savedInputs;         // For BPTT
    private List<NdArray> savedOutputs;        // For BPTT

    /**
     * RNN方法。
     * * @param inputSize int类型参数
     *
     * @param hiddenSize int类型参数
     * @param numLayers  int类型参数
     */
    public RNN(int inputSize, int hiddenSize, int numLayers) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.numLayers = numLayers;

        weightIh = new NdArray[numLayers];
        weightHh = new NdArray[numLayers];
        biasH = new NdArray[numLayers];

        for (int layer = 0; layer < numLayers; layer++) {
            // Weight for input: [hiddenSize, inputSize] for first layer, [hiddenSize, hiddenSize] for others
            int inFeatures = (layer == 0) ? inputSize : hiddenSize;
            weightIh[layer] = NdArray.zeros(new Shape(hiddenSize, inFeatures), DType.FLOAT32);
            initXavierUniform(weightIh[layer], inFeatures, hiddenSize);
            registerParameter("weight_ih_l" + layer, weightIh[layer]);

            // Weight for hidden: [hiddenSize, hiddenSize]
            weightHh[layer] = NdArray.zeros(new Shape(hiddenSize, hiddenSize), DType.FLOAT32);
            initXavierUniform(weightHh[layer], hiddenSize, hiddenSize);
            registerParameter("weight_hh_l" + layer, weightHh[layer]);

            // Bias: [hiddenSize]
            biasH[layer] = NdArray.zeros(new Shape(hiddenSize), DType.FLOAT32);
            registerParameter("bias_h_l" + layer, biasH[layer]);
        }
    }

    @Override
    /**
     * forward方法。
     *      * @param input NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray input) {
        // Input shape: [seqLen, batch, inputSize]
        int seqLen = input.getShape().get(0);
        int batchSize = input.getShape().get(1);

        savedHiddenStates = new ArrayList<>();
        savedInputs = new ArrayList<>();
        savedOutputs = new ArrayList<>();

        // Initialize hidden state to zeros
        NdArray[] h_prev = new NdArray[numLayers];
        for (int layer = 0; layer < numLayers; layer++) {
            h_prev[layer] = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
        }

        NdArray[] allOutputs = new NdArray[seqLen];

        for (int t = 0; t < seqLen; t++) {
            // Get input at time t: [batch, inputSize]
            NdArray x_t = sliceInput(input, t, batchSize);

            savedInputs.add(x_t.copy());

            NdArray[] h_current = new NdArray[numLayers];
            NdArray currentInput = x_t;

            for (int layer = 0; layer < numLayers; layer++) {
                savedHiddenStates.add(h_prev[layer].copy());

                // Compute: h_t = tanh(W_ih @ x + W_hh @ h_{t-1} + b_h)
                int inFeatures = (layer == 0) ? inputSize : hiddenSize;

                // matmul(x, W_ih.T): [batch, hiddenSize]
                NdArray wx = matmul(currentInput, weightIh[layer].transpose());

                // matmul(h_prev, W_hh.T): [batch, hiddenSize]
                NdArray wh = matmul(h_prev[layer], weightHh[layer].transpose());

                // Add bias
                NdArray preact = add(wx, wh);
                preact = add(preact, biasH[layer]);

                // tanh activation
                h_current[layer] = tanh(preact);

                currentInput = h_current[layer];
            }

            // Output is the hidden state of the last layer
            allOutputs[t] = h_current[numLayers - 1].copy();
            savedOutputs.add(allOutputs[t].copy());
            h_prev = h_current;
        }

        // Stack outputs: [seqLen, batch, hiddenSize]
        NdArray output = stack(allOutputs, 0);
        return output;
    }

    @Override
    /**
     * backward方法。
     *      * @param gradOutput NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray backward(NdArray gradOutput) {
        // gradOutput: [seqLen, batch, hiddenSize]
        int seqLen = gradOutput.getShape().get(0);
        int batchSize = gradOutput.getShape().get(1);

        // Initialize gradient of input
        NdArray gradInput = NdArray.zeros(new Shape(seqLen, batchSize, inputSize), DType.FLOAT32);

        // Gradient for each layer's weights
        NdArray[][] gradWeightIh = new NdArray[numLayers][seqLen];
        NdArray[][] gradWeightHh = new NdArray[numLayers][seqLen];
        NdArray[][] gradBiasH = new NdArray[numLayers][seqLen];

        for (int layer = 0; layer < numLayers; layer++) {
            for (int t = 0; t < seqLen; t++) {
                gradWeightIh[layer][t] = NdArray.zeros(weightIh[layer].getShape(), DType.FLOAT32);
                gradWeightHh[layer][t] = NdArray.zeros(weightHh[layer].getShape(), DType.FLOAT32);
                gradBiasH[layer][t] = NdArray.zeros(biasH[layer].getShape(), DType.FLOAT32);
            }
        }

        // Backward pass through time (BPTT)
        NdArray[] dh_next = new NdArray[numLayers];
        for (int layer = 0; layer < numLayers; layer++) {
            dh_next[layer] = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
        }

        for (int t = seqLen - 1; t >= 0; t--) {
            // Get gradient from output at time t
            NdArray gradOut_t = sliceTimeStep(gradOutput, t, batchSize);

            for (int layer = numLayers - 1; layer >= 0; layer--) {
                // Gradient w.r.t. hidden state at t for this layer
                NdArray dh = add(gradOut_t.copy(), dh_next[layer]);

                // Get saved hidden state at t-1 (or zeros if t=0)
                NdArray h_prev;
                if (t > 0 && savedHiddenStates.size() > (t - 1) * numLayers + layer) {
                    h_prev = savedHiddenStates.get((t - 1) * numLayers + layer);
                } else {
                    h_prev = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
                }

                // Get saved input
                NdArray x_t = savedInputs.get(t);

                // Gradient w.r.t. pre-activation (through tanh)
                // tanh'(x) = 1 - tanh^2(x)
                NdArray savedOutput = savedOutputs.get(t);
                NdArray dtanh = tanhDerivative(savedOutput);
                NdArray dpreact = elementwiseMul(dh, dtanh);

                // Gradient w.r.t. weights
                NdArray x_layer = (layer == 0) ? x_t : (t > 0 ? savedInputs.get(Math.max(0, t - 1)) : x_t);

                gradWeightIh[layer][t] = matmul(dpreact.transpose(), x_layer);
                gradWeightHh[layer][t] = matmul(dpreact.transpose(), h_prev);

                // Gradient w.r.t. bias
                gradBiasH[layer][t] = sum(dpreact, 0);

                // Gradient w.r.t. input (for passing to previous layer)
                NdArray din = matmul(dpreact, weightIh[layer]);

                // Gradient w.r.t. previous hidden state (for next time step)
                dh_next[layer] = matmul(dpreact, weightHh[layer]);

                // Store gradient for input (only for first layer)
                if (layer == 0) {
                    gradInput = setSliceTimeStep(gradInput, din, t);
                }
            }
        }

        return gradInput;
    }

    // Slice input at time t: returns [batch, inputSize]
    private NdArray sliceInput(NdArray input, int t, int batchSize) {
        NdArray result = NdArray.zeros(new Shape(batchSize, inputSize), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = result.getData();

        int seqLen = input.getShape().get(0);
        int totalCols = input.getShape().get(2);

        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < inputSize; i++) {
                int inIdx = (t * batchSize + b) * totalCols + i;
                int outIdx = b * inputSize + i;
                Object val = com.zifang.util.numpy.Array.get(inData, inIdx);
                com.zifang.util.numpy.Array.set(outData, outIdx, val);
            }
        }
        return result;
    }

    // Slice gradOutput at time t: returns [batch, hiddenSize]
    private NdArray sliceTimeStep(NdArray gradOutput, int t, int batchSize) {
        NdArray result = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
        Object inData = gradOutput.getData();
        Object outData = result.getData();

        int seqLen = gradOutput.getShape().get(0);
        int totalCols = gradOutput.getShape().get(2);

        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < hiddenSize; i++) {
                int inIdx = (t * batchSize + b) * totalCols + i;
                int outIdx = b * hiddenSize + i;
                Object val = com.zifang.util.numpy.Array.get(inData, inIdx);
                com.zifang.util.numpy.Array.set(outData, outIdx, val);
            }
        }
        return result;
    }

    // Set gradient at time t
    private NdArray setSliceTimeStep(NdArray array, NdArray value, int t) {
        int batchSize = array.getShape().get(1);
        int features = array.getShape().get(2);

        NdArray result = array.copy();
        Object outData = result.getData();
        Object inData = value.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < features; i++) {
                int outIdx = (t * batchSize + b) * features + i;
                int inIdx = b * features + i;
                Object val = com.zifang.util.numpy.Array.get(inData, inIdx);
                com.zifang.util.numpy.Array.set(outData, outIdx, val);
            }
        }
        return result;
    }

    // Tanh derivative: tanh'(x) = 1 - tanh^2(x)
    private NdArray tanhDerivative(NdArray input) {
        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            float th = (float) Math.tanh(val);
            com.zifang.util.numpy.Array.set(outData, i, 1.0f - th * th);
        }
        return output;
    }

    // Helper: matrix multiplication (batch, m, k) @ (k, n) -> (batch, m, n)
    private NdArray matmul(NdArray a, NdArray b) {
        if (a.ndim() == 2 && b.ndim() == 2) {
            int m = a.getShape().get(0);
            int k = a.getShape().get(1);
            int n = b.getShape().get(1);

            NdArray result = NdArray.zeros(new Shape(m, n), DType.FLOAT32);
            Object aData = a.getData();
            Object bData = b.getData();
            Object cData = result.getData();

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    float sum = 0.0f;
                    for (int l = 0; l < k; l++) {
                        float aVal = ((Number) com.zifang.util.numpy.Array.get(aData, i * k + l)).floatValue();
                        float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, l * n + j)).floatValue();
                        sum += aVal * bVal;
                    }
                    com.zifang.util.numpy.Array.set(cData, i * n + j, sum);
                }
            }
            return result;
        }
        throw new IllegalArgumentException("matmul only supports 2D matrices");
    }

    // Helper: element-wise tanh
    private NdArray tanh(NdArray input) {
        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            com.zifang.util.numpy.Array.set(outData, i, (float) Math.tanh(val));
        }
        return output;
    }

    // Helper: element-wise multiplication
    private NdArray elementwiseMul(NdArray a, NdArray b) {
        if (!a.getShape().equals(b.getShape())) {
            throw new IllegalArgumentException("Shape mismatch: " + a.getShape() + " vs " + b.getShape());
        }

        NdArray result = NdArray.zeros(a.getShape(), DType.FLOAT32);
        Object aData = a.getData();
        Object bData = b.getData();
        Object cData = result.getData();
        int size = a.size();

        for (int i = 0; i < size; i++) {
            float aVal = ((Number) com.zifang.util.numpy.Array.get(aData, i)).floatValue();
            float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, i)).floatValue();
            com.zifang.util.numpy.Array.set(cData, i, aVal * bVal);
        }
        return result;
    }

    // Helper: element-wise addition with broadcasting
    private NdArray add(NdArray a, NdArray b) {
        Shape shapeA = a.getShape();
        Shape shapeB = b.getShape();

        if (shapeA.equals(shapeB)) {
            NdArray result = NdArray.zeros(shapeA, DType.FLOAT32);
            Object aData = a.getData();
            Object bData = b.getData();
            Object cData = result.getData();
            int size = a.size();

            for (int i = 0; i < size; i++) {
                float aVal = ((Number) com.zifang.util.numpy.Array.get(aData, i)).floatValue();
                float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, i)).floatValue();
                com.zifang.util.numpy.Array.set(cData, i, aVal + bVal);
            }
            return result;
        }

        // Bias broadcasting: [batch, hiddenSize] + [hiddenSize] -> [batch, hiddenSize]
        if (a.ndim() == 2 && b.ndim() == 1 && shapeA.get(1) == shapeB.get(0)) {
            int batchSize = shapeA.get(0);
            int hiddenSize = shapeA.get(1);

            NdArray result = NdArray.zeros(shapeA, DType.FLOAT32);
            Object aData = a.getData();
            Object bData = b.getData();
            Object cData = result.getData();

            for (int i = 0; i < batchSize; i++) {
                for (int j = 0; j < hiddenSize; j++) {
                    float aVal = ((Number) com.zifang.util.numpy.Array.get(aData, i * hiddenSize + j)).floatValue();
                    float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, j)).floatValue();
                    com.zifang.util.numpy.Array.set(cData, i * hiddenSize + j, aVal + bVal);
                }
            }
            return result;
        }

        throw new IllegalArgumentException("Cannot add arrays with shapes " + shapeA + " and " + shapeB);
    }

    // Helper: sum over axis
    private NdArray sum(NdArray input, int axis) {
        if (axis == 0) {
            int rows = input.getShape().get(0);
            int cols = input.getShape().get(1);
            NdArray result = NdArray.zeros(new Shape(cols), DType.FLOAT32);
            Object inData = input.getData();
            Object outData = result.getData();

            for (int j = 0; j < cols; j++) {
                float sum = 0.0f;
                for (int i = 0; i < rows; i++) {
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, i * cols + j)).floatValue();
                    sum += val;
                }
                com.zifang.util.numpy.Array.set(outData, j, sum);
            }
            return result;
        }
        throw new IllegalArgumentException("Sum axis " + axis + " not implemented");
    }

    // Helper: stack arrays along axis
    private NdArray stack(NdArray[] arrays, int axis) {
        if (arrays.length == 0) throw new IllegalArgumentException("Empty array");

        int seqLen = arrays.length;
        int batchSize = arrays[0].getShape().get(0);
        int hiddenSize = arrays[0].getShape().get(1);

        NdArray result = NdArray.zeros(new Shape(seqLen, batchSize, hiddenSize), DType.FLOAT32);
        Object outData = result.getData();

        for (int t = 0; t < seqLen; t++) {
            Object inData = arrays[t].getData();
            for (int i = 0; i < batchSize * hiddenSize; i++) {
                com.zifang.util.numpy.Array.set(outData, t * batchSize * hiddenSize + i,
                        com.zifang.util.numpy.Array.get(inData, i));
            }
        }
        return result;
    }
}

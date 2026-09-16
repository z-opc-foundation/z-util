package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * LSTM (Long Short-Term Memory) implementation.
 * <p>
 * Gates:
 * - i (input gate): i = σ(W_ii @ x + b_ii + W_hi @ h + b_hi)
 * - f (forget gate): f = σ(W_if @ x + b_if + W_hf @ h + b_hf)
 * - o (output gate): o = σ(W_io @ x + b_io + W_ho @ h + b_ho)
 * - g (cell candidate): g = tanh(W_ig @ x + b_ig + W_hg @ h + b_hg)
 * <p>
 * Update:
 * - c = f * c + i * g (cell state)
 * - h = o * tanh(c) (hidden state)
 * <p>
 * Input shape: [seqLen, batch, inputSize]
 * Output shape: [seqLen, batch, hiddenSize]
 */
public class LSTM extends Module {

    private int inputSize;
    private int hiddenSize;
    private int numLayers;

    // Input gate weights and biases
    private NdArray[] weightIi;  // Input to input gate
    private NdArray[] weightHi;  // Hidden to input gate
    private NdArray[] biasIi;

    // Forget gate weights and biases
    private NdArray[] weightIf;
    private NdArray[] weightHf;
    private NdArray[] biasIf;

    // Output gate weights and biases
    private NdArray[] weightIo;
    private NdArray[] weightHo;
    private NdArray[] biasIo;

    // Cell candidate weights and biases
    private NdArray[] weightIg;
    private NdArray[] weightHg;
    private NdArray[] biasIg;

    // Saved states for backward pass
    private List<NdArray> savedHiddenStates;
    private List<NdArray> savedCellStates;
    private List<NdArray> savedInputs;
    private List<NdArray[]> savedGates;  // For each time step: [i, f, o, g]

    /**
     * LSTM方法。
     * * @param inputSize int类型参数
     *
     * @param hiddenSize int类型参数
     * @param numLayers  int类型参数
     */
    public LSTM(int inputSize, int hiddenSize, int numLayers) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.numLayers = numLayers;

        // Initialize gate parameters
        weightIi = new NdArray[numLayers];
        weightHi = new NdArray[numLayers];
        biasIi = new NdArray[numLayers];

        weightIf = new NdArray[numLayers];
        weightHf = new NdArray[numLayers];
        biasIf = new NdArray[numLayers];

        weightIo = new NdArray[numLayers];
        weightHo = new NdArray[numLayers];
        biasIo = new NdArray[numLayers];

        weightIg = new NdArray[numLayers];
        weightHg = new NdArray[numLayers];
        biasIg = new NdArray[numLayers];

        for (int layer = 0; layer < numLayers; layer++) {
            int inFeatures = (layer == 0) ? inputSize : hiddenSize;

            // Input gate
            weightIi[layer] = NdArray.zeros(new Shape(hiddenSize, inFeatures), DType.FLOAT32);
            initXavierUniform(weightIi[layer], inFeatures, hiddenSize);
            registerParameter("weight_ii_l" + layer, weightIi[layer]);

            weightHi[layer] = NdArray.zeros(new Shape(hiddenSize, hiddenSize), DType.FLOAT32);
            initXavierUniform(weightHi[layer], hiddenSize, hiddenSize);
            registerParameter("weight_hi_l" + layer, weightHi[layer]);

            biasIi[layer] = NdArray.zeros(new Shape(hiddenSize), DType.FLOAT32);
            registerParameter("bias_ii_l" + layer, biasIi[layer]);

            // Forget gate
            weightIf[layer] = NdArray.zeros(new Shape(hiddenSize, inFeatures), DType.FLOAT32);
            initXavierUniform(weightIf[layer], inFeatures, hiddenSize);
            registerParameter("weight_if_l" + layer, weightIf[layer]);

            weightHf[layer] = NdArray.zeros(new Shape(hiddenSize, hiddenSize), DType.FLOAT32);
            initXavierUniform(weightHf[layer], hiddenSize, hiddenSize);
            registerParameter("weight_hf_l" + layer, weightHf[layer]);

            biasIf[layer] = NdArray.zeros(new Shape(hiddenSize), DType.FLOAT32);
            registerParameter("bias_if_l" + layer, biasIf[layer]);

            // Output gate
            weightIo[layer] = NdArray.zeros(new Shape(hiddenSize, inFeatures), DType.FLOAT32);
            initXavierUniform(weightIo[layer], inFeatures, hiddenSize);
            registerParameter("weight_io_l" + layer, weightIo[layer]);

            weightHo[layer] = NdArray.zeros(new Shape(hiddenSize, hiddenSize), DType.FLOAT32);
            initXavierUniform(weightHo[layer], hiddenSize, hiddenSize);
            registerParameter("weight_ho_l" + layer, weightHo[layer]);

            biasIo[layer] = NdArray.zeros(new Shape(hiddenSize), DType.FLOAT32);
            registerParameter("bias_io_l" + layer, biasIo[layer]);

            // Cell candidate
            weightIg[layer] = NdArray.zeros(new Shape(hiddenSize, inFeatures), DType.FLOAT32);
            initXavierUniform(weightIg[layer], inFeatures, hiddenSize);
            registerParameter("weight_ig_l" + layer, weightIg[layer]);

            weightHg[layer] = NdArray.zeros(new Shape(hiddenSize, hiddenSize), DType.FLOAT32);
            initXavierUniform(weightHg[layer], hiddenSize, hiddenSize);
            registerParameter("weight_hg_l" + layer, weightHg[layer]);

            biasIg[layer] = NdArray.zeros(new Shape(hiddenSize), DType.FLOAT32);
            registerParameter("bias_ig_l" + layer, biasIg[layer]);
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
        savedCellStates = new ArrayList<>();
        savedInputs = new ArrayList<>();
        savedGates = new ArrayList<>();

        // Initialize hidden state and cell state to zeros
        NdArray[] h_prev = new NdArray[numLayers];
        NdArray[] c_prev = new NdArray[numLayers];
        for (int layer = 0; layer < numLayers; layer++) {
            h_prev[layer] = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
            c_prev[layer] = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
        }

        NdArray[] allOutputs = new NdArray[seqLen];

        for (int t = 0; t < seqLen; t++) {
            // Get input at time t
            NdArray x_t = sliceInput(input, t, batchSize);

            savedInputs.add(x_t.copy());
            NdArray[] layerHidden = new NdArray[numLayers];
            NdArray[] layerCell = new NdArray[numLayers];

            NdArray currentInput = x_t;

            for (int layer = 0; layer < numLayers; layer++) {
                // Compute gates
                // i = σ(W_ii @ x + W_hi @ h + b_i)
                NdArray wx_i = matmul(currentInput, weightIi[layer].transpose());
                NdArray wh_i = matmul(h_prev[layer], weightHi[layer].transpose());
                NdArray pre_i = add(wx_i, wh_i);
                pre_i = add(pre_i, biasIi[layer]);
                NdArray i = sigmoid(pre_i);

                // f = σ(W_if @ x + W_hf @ h + b_f)
                NdArray wx_f = matmul(currentInput, weightIf[layer].transpose());
                NdArray wh_f = matmul(h_prev[layer], weightHf[layer].transpose());
                NdArray pre_f = add(wx_f, wh_f);
                pre_f = add(pre_f, biasIf[layer]);
                NdArray f = sigmoid(pre_f);

                // o = σ(W_io @ x + W_ho @ h + b_o)
                NdArray wx_o = matmul(currentInput, weightIo[layer].transpose());
                NdArray wh_o = matmul(h_prev[layer], weightHo[layer].transpose());
                NdArray pre_o = add(wx_o, wh_o);
                pre_o = add(pre_o, biasIo[layer]);
                NdArray o = sigmoid(pre_o);

                // g = tanh(W_ig @ x + W_hg @ h + b_g)
                NdArray wx_g = matmul(currentInput, weightIg[layer].transpose());
                NdArray wh_g = matmul(h_prev[layer], weightHg[layer].transpose());
                NdArray pre_g = add(wx_g, wh_g);
                pre_g = add(pre_g, biasIg[layer]);
                NdArray g = tanh(pre_g);

                // c = f * c_prev + i * g
                NdArray c = add(elementwiseMul(f, c_prev[layer]), elementwiseMul(i, g));

                // h = o * tanh(c)
                NdArray h = elementwiseMul(o, tanh(c));

                // Save gates and states
                savedGates.add(new NdArray[]{i, f, o, g});
                savedHiddenStates.add(h_prev[layer].copy());
                savedCellStates.add(c_prev[layer].copy());

                layerHidden[layer] = h;
                layerCell[layer] = c;

                currentInput = h;
            }

            // Output is the hidden state of the last layer
            allOutputs[t] = layerHidden[numLayers - 1].copy();
            h_prev = layerHidden;
            c_prev = layerCell;
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

        // Gradient for hidden and cell states at each time step
        NdArray[][] gradH = new NdArray[numLayers][seqLen + 1];
        NdArray[][] gradC = new NdArray[numLayers][seqLen + 1];
        for (int layer = 0; layer < numLayers; layer++) {
            for (int t = 0; t <= seqLen; t++) {
                gradH[layer][t] = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
                gradC[layer][t] = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
            }
        }

        // Backward pass through time
        for (int t = seqLen - 1; t >= 0; t--) {
            // Get gradient from output at time t
            NdArray gradOut_t = sliceTimeStep(gradOutput, t, batchSize);

            for (int layer = numLayers - 1; layer >= 0; layer--) {
                // Add gradient from output
                NdArray dh = add(gradOut_t.copy(), gradH[layer][t + 1]);

                // Get saved gates
                int gateIdx = t * numLayers + layer;
                if (gateIdx < savedGates.size()) {
                    NdArray[] gates = savedGates.get(gateIdx);
                    NdArray i = gates[0];
                    NdArray f = gates[1];
                    NdArray o = gates[2];
                    NdArray g = gates[3];

                    // Get saved cell state
                    NdArray c_prev_saved = (t > 0) ? savedCellStates.get((t - 1) * numLayers + layer)
                            : NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);

                    // Gradient through h = o * tanh(c)
                    NdArray tanh_c = tanh(c_prev_saved);
                    NdArray do_grad = elementwiseMul(dh, tanh_c);
                    NdArray pre_o_grad = elementwiseMul(do_grad, sigmoidDerivative(o));

                    // Gradient through c = f * c_prev + i * g
                    NdArray dc = elementwiseMul(dh, elementwiseMul(o, tanhDerivative(tanh_c)));
                    dc = add(dc, gradC[layer][t + 1]);

                    NdArray df = elementwiseMul(dc, c_prev_saved);
                    NdArray di = elementwiseMul(dc, g);
                    NdArray dg = elementwiseMul(dc, i);

                    NdArray pre_f_grad = elementwiseMul(df, sigmoidDerivative(f));
                    NdArray pre_i_grad = elementwiseMul(di, sigmoidDerivative(i));
                    NdArray pre_g_grad = elementwiseMul(dg, tanhDerivative(g));

                    // Gradient for next time step
                    gradH[layer][t] = matmul(pre_i_grad, weightHi[layer]);
                    gradH[layer][t] = add(gradH[layer][t], matmul(pre_f_grad, weightHf[layer]));
                    gradH[layer][t] = add(gradH[layer][t], matmul(pre_o_grad, weightHo[layer]));
                    gradH[layer][t] = add(gradH[layer][t], matmul(pre_g_grad, weightHg[layer]));

                    gradC[layer][t] = elementwiseMul(dc, f);

                    // Input for first layer
                    if (layer == 0) {
                        gradInput = setSliceTimeStep(gradInput, savedInputs.get(t), t);
                    }
                }
            }
        }

        return gradInput;
    }

    // Slice input at time t
    private NdArray sliceInput(NdArray input, int t, int batchSize) {
        NdArray result = NdArray.zeros(new Shape(batchSize, inputSize), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = result.getData();

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

    // Slice time step from gradOutput
    private NdArray sliceTimeStep(NdArray gradOutput, int t, int batchSize) {
        NdArray result = NdArray.zeros(new Shape(batchSize, hiddenSize), DType.FLOAT32);
        Object inData = gradOutput.getData();
        Object outData = result.getData();

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

    // Sigmoid function
    private NdArray sigmoid(NdArray input) {
        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            float sig = (float) (1.0 / (1.0 + Math.exp(-val)));
            com.zifang.util.numpy.Array.set(outData, i, sig);
        }
        return output;
    }

    // Sigmoid derivative
    private NdArray sigmoidDerivative(NdArray input) {
        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            float sig = (float) (1.0 / (1.0 + Math.exp(-val)));
            com.zifang.util.numpy.Array.set(outData, i, sig * (1 - sig));
        }
        return output;
    }

    // Tanh derivative
    private NdArray tanhDerivative(NdArray input) {
        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            float th = (float) Math.tanh(val);
            com.zifang.util.numpy.Array.set(outData, i, 1 - th * th);
        }
        return output;
    }

    // Tanh function
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

    // Element-wise multiplication
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

    // Matrix multiplication (2D)
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

    // Addition with broadcasting for bias
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

        // Bias broadcasting
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

    // Stack arrays
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

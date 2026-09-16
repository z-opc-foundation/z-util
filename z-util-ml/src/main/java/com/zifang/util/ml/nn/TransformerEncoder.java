package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified Transformer Encoder implementation.
 * <p>
 * Architecture:
 * - Multi-head self-attention
 * - Feed-forward network
 * - Add & Norm after each sublayer
 * - Positional encoding
 * <p>
 * Input shape: [seqLen, batch, dModel]
 * Output shape: [seqLen, batch, dModel]
 */
public class TransformerEncoder extends Module {

    private int dModel;
    private int nhead;
    private int dimFeedforward;
    private int numLayers;
    private double dropout;

    // Multi-head attention parameters
    private NdArray[] Wq;  // Query weights per layer
    private NdArray[] Wk;  // Key weights per layer
    private NdArray[] Wv;  // Value weights per layer
    private NdArray[] Wo;  // Output weights per layer

    // Feed-forward network parameters
    private NdArray[] ffLinear1Weight;  // First linear layer weights
    private NdArray[] ffLinear1Bias;
    private NdArray[] ffLinear2Weight;  // Second linear layer weights
    private NdArray[] ffLinear2Bias;

    // Layer norm parameters
    private NdArray[] gamma1;  // Scale for attention norm
    private NdArray[] beta1;   // Shift for attention norm
    private NdArray[] gamma2;  // Scale for FFN norm
    private NdArray[] beta2;    // Shift for FFN norm

    private int dK;  // Dimension per head

    // Saved states for backward pass
    private List<NdArray> savedAttentionInputs;
    private List<NdArray> savedAttentionOutputs;
    private List<NdArray> savedFFNInputs;

    /**
     * TransformerEncoder方法。
     * * @param dModel int类型参数
     *
     * @param nhead          int类型参数
     * @param dimFeedforward int类型参数
     * @param numLayers      int类型参数
     * @param dropout        double类型参数
     */
    public TransformerEncoder(int dModel, int nhead, int dimFeedforward, int numLayers, double dropout) {
        this.dModel = dModel;
        this.nhead = nhead;
        this.dimFeedforward = dimFeedforward;
        this.numLayers = numLayers;
        this.dropout = dropout;
        this.dK = dModel / nhead;

        // Initialize parameters for each layer
        Wq = new NdArray[numLayers];
        Wk = new NdArray[numLayers];
        Wv = new NdArray[numLayers];
        Wo = new NdArray[numLayers];

        ffLinear1Weight = new NdArray[numLayers];
        ffLinear1Bias = new NdArray[numLayers];
        ffLinear2Weight = new NdArray[numLayers];
        ffLinear2Bias = new NdArray[numLayers];

        gamma1 = new NdArray[numLayers];
        beta1 = new NdArray[numLayers];
        gamma2 = new NdArray[numLayers];
        beta2 = new NdArray[numLayers];

        for (int layer = 0; layer < numLayers; layer++) {
            // Q, K, V projection weights: [dModel, dK * nhead]
            Wq[layer] = NdArray.zeros(new Shape(dModel, dK * nhead), DType.FLOAT32);
            initXavierUniform(Wq[layer], dModel, dK * nhead);
            registerParameter("Wq_" + layer, Wq[layer]);

            Wk[layer] = NdArray.zeros(new Shape(dModel, dK * nhead), DType.FLOAT32);
            initXavierUniform(Wk[layer], dModel, dK * nhead);
            registerParameter("Wk_" + layer, Wk[layer]);

            Wv[layer] = NdArray.zeros(new Shape(dModel, dK * nhead), DType.FLOAT32);
            initXavierUniform(Wv[layer], dModel, dK * nhead);
            registerParameter("Wv_" + layer, Wv[layer]);

            // Output projection: [dK * nhead, dModel]
            Wo[layer] = NdArray.zeros(new Shape(dK * nhead, dModel), DType.FLOAT32);
            initXavierUniform(Wo[layer], dK * nhead, dModel);
            registerParameter("Wo_" + layer, Wo[layer]);

            // FFN weights - standard matrix multiplication: W @ x where W is (out_features, in_features)
            // Linear(dModel, dimFeedforward): W1 maps dModel -> dimFeedforward, so W1 shape (dModel, dimFeedforward)
            ffLinear1Weight[layer] = NdArray.zeros(new Shape(dModel, dimFeedforward), DType.FLOAT32);
            initXavierUniform(ffLinear1Weight[layer], dModel, dimFeedforward);
            registerParameter("ff1_w_" + layer, ffLinear1Weight[layer]);

            ffLinear1Bias[layer] = NdArray.zeros(new Shape(dimFeedforward), DType.FLOAT32);
            registerParameter("ff1_b_" + layer, ffLinear1Bias[layer]);

            // Linear(dimFeedforward, dModel): W2 maps dimFeedforward -> dModel, so W2 shape (dimFeedforward, dModel)
            ffLinear2Weight[layer] = NdArray.zeros(new Shape(dimFeedforward, dModel), DType.FLOAT32);
            initXavierUniform(ffLinear2Weight[layer], dimFeedforward, dModel);
            registerParameter("ff2_w_" + layer, ffLinear2Weight[layer]);

            ffLinear2Bias[layer] = NdArray.zeros(new Shape(dModel), DType.FLOAT32);
            registerParameter("ff2_b_" + layer, ffLinear2Bias[layer]);

            // Layer norm parameters
            gamma1[layer] = NdArray.ones(new Shape(dModel), DType.FLOAT32);
            registerParameter("gamma1_" + layer, gamma1[layer]);

            beta1[layer] = NdArray.zeros(new Shape(dModel), DType.FLOAT32);
            registerParameter("beta1_" + layer, beta1[layer]);

            gamma2[layer] = NdArray.ones(new Shape(dModel), DType.FLOAT32);
            registerParameter("gamma2_" + layer, gamma2[layer]);

            beta2[layer] = NdArray.zeros(new Shape(dModel), DType.FLOAT32);
            registerParameter("beta2_" + layer, beta2[layer]);
        }
    }

    @Override
    /**
     * forward方法。
     *      * @param src NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray src) {
        // Input shape: [seqLen, batch, dModel]
        int seqLen = src.getShape().get(0);
        int batchSize = src.getShape().get(1);

        savedAttentionInputs = new ArrayList<>();
        savedAttentionOutputs = new ArrayList<>();
        savedFFNInputs = new ArrayList<>();

        // Add positional encoding
        NdArray x = addPositionalEncoding(src);

        for (int layer = 0; layer < numLayers; layer++) {
            // Multi-head self-attention sublayer
            NdArray Q = matmul(x, Wq[layer]);
            NdArray K = matmul(x, Wk[layer]);
            NdArray V = matmul(x, Wv[layer]);

            // Save for backward pass
            if (layer == 0) {
                savedAttentionInputs.add(Q.copy());
                savedAttentionInputs.add(K.copy());
                savedAttentionInputs.add(V.copy());
            }

            // Reshape for multi-head attention: [seqLen, batch, nhead, dK]
            Q = reshapeToMultiHead(Q, seqLen, batchSize);
            K = reshapeToMultiHead(K, seqLen, batchSize);
            V = reshapeToMultiHead(V, seqLen, batchSize);

            // Scaled dot-product attention for each head and concatenate
            NdArray attention = scaledDotProductAttention(Q, K, V);

            // Save attention output
            if (layer == 0) {
                savedAttentionOutputs.add(attention.copy());
            }

            // Output projection
            NdArray concatOutput = matmul(attention, Wo[layer]);

            // Add & Norm (attention)
            NdArray attentionNorm = layerNorm(add(concatOutput, x), gamma1[layer], beta1[layer]);

            // Feed-forward sublayer
            NdArray ffnOutput = feedForward(attentionNorm, layer);

            // Save FFN input
            if (layer == 0) {
                savedFFNInputs.add(attentionNorm.copy());
            }

            // Add & Norm (FFN)
            x = layerNorm(add(ffnOutput, attentionNorm), gamma2[layer], beta2[layer]);
        }

        return x;
    }

    @Override
    /**
     * backward方法。
     *      * @param gradOutput NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray backward(NdArray gradOutput) {
        // Simplified backward pass
        int seqLen = gradOutput.getShape().get(0);
        int batchSize = gradOutput.getShape().get(1);

        NdArray gradInput = NdArray.zeros(new Shape(seqLen, batchSize, dModel), DType.FLOAT32);
        Object gradOutData = gradOutput.getData();
        Object gradInData = gradInput.getData();
        int size = gradOutput.size();

        for (int i = 0; i < size; i++) {
            Object val = com.zifang.util.numpy.Array.get(gradOutData, i);
            com.zifang.util.numpy.Array.set(gradInData, i, val);
        }

        return gradInput;
    }

    // Reshape to multi-head format
    private NdArray reshapeToMultiHead(NdArray input, int seqLen, int batchSize) {
        // Input: [seqLen, batch, dK * nhead]
        // Output: [seqLen, batch, nhead, dK]
        NdArray output = NdArray.zeros(new Shape(seqLen, batchSize, nhead, dK), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();

        for (int i = 0; i < seqLen; i++) {
            for (int b = 0; b < batchSize; b++) {
                for (int h = 0; h < nhead; h++) {
                    for (int k = 0; k < dK; k++) {
                        int inIdx = (i * batchSize + b) * dK * nhead + h * dK + k;
                        int outIdx = ((i * batchSize + b) * nhead + h) * dK + k;
                        float val = ((Number) com.zifang.util.numpy.Array.get(inData, inIdx)).floatValue();
                        com.zifang.util.numpy.Array.set(outData, outIdx, val);
                    }
                }
            }
        }
        return output;
    }

    // Scaled dot-product attention
    private NdArray scaledDotProductAttention(NdArray Q, NdArray K, NdArray V) {
        // Q, K, V: [seqLen, batch, nhead, dK]
        int seqLen = Q.getShape().get(0);
        int batchSize = Q.getShape().get(1);

        // Compute QK^T / sqrt(dK)
        // Result: [seqLen, seqLen, batch, nhead]
        NdArray scores = NdArray.zeros(new Shape(seqLen, seqLen, batchSize, nhead), DType.FLOAT32);

        Object qData = Q.getData();
        Object kData = K.getData();
        Object sData = scores.getData();

        for (int i = 0; i < seqLen; i++) {
            for (int j = 0; j < seqLen; j++) {
                for (int b = 0; b < batchSize; b++) {
                    for (int h = 0; h < nhead; h++) {
                        float sum = 0.0f;
                        for (int k = 0; k < dK; k++) {
                            int qIdx = ((i * batchSize + b) * nhead + h) * dK + k;
                            int kIdx = ((j * batchSize + b) * nhead + h) * dK + k;
                            float qVal = ((Number) com.zifang.util.numpy.Array.get(qData, qIdx)).floatValue();
                            float kVal = ((Number) com.zifang.util.numpy.Array.get(kData, kIdx)).floatValue();
                            sum += qVal * kVal;
                        }
                        float scaledScore = sum / (float) Math.sqrt(dK);
                        int sIdx = ((i * seqLen + j) * batchSize + b) * nhead + h;
                        com.zifang.util.numpy.Array.set(sData, sIdx, scaledScore);
                    }
                }
            }
        }

        // Softmax over key dimension (j)
        NdArray attention = softmax(scores);

        // Multiply by V: [seqLen, seqLen, batch, nhead] @ [seqLen, batch, nhead, dK] -> [seqLen, batch, nhead, dK]
        NdArray output = NdArray.zeros(new Shape(seqLen, batchSize, nhead, dK), DType.FLOAT32);
        Object aData = attention.getData();
        Object vData = V.getData();
        Object outData = output.getData();

        for (int i = 0; i < seqLen; i++) {
            for (int b = 0; b < batchSize; b++) {
                for (int h = 0; h < nhead; h++) {
                    for (int k = 0; k < dK; k++) {
                        float sum = 0.0f;
                        for (int j = 0; j < seqLen; j++) {
                            int aIdx = ((i * seqLen + j) * batchSize + b) * nhead + h;
                            int vIdx = ((j * batchSize + b) * nhead + h) * dK + k;
                            float aVal = ((Number) com.zifang.util.numpy.Array.get(aData, aIdx)).floatValue();
                            float vVal = ((Number) com.zifang.util.numpy.Array.get(vData, vIdx)).floatValue();
                            sum += aVal * vVal;
                        }
                        int outIdx = ((i * batchSize + b) * nhead + h) * dK + k;
                        com.zifang.util.numpy.Array.set(outData, outIdx, sum);
                    }
                }
            }
        }

        // Reshape back: [seqLen, batch, nhead, dK] -> [seqLen, batch, dK * nhead]
        NdArray result = NdArray.zeros(new Shape(seqLen, batchSize, dK * nhead), DType.FLOAT32);
        Object resData = result.getData();

        for (int i = 0; i < seqLen; i++) {
            for (int b = 0; b < batchSize; b++) {
                for (int h = 0; h < nhead; h++) {
                    for (int k = 0; k < dK; k++) {
                        int srcIdx = ((i * batchSize + b) * nhead + h) * dK + k;
                        int dstIdx = (i * batchSize + b) * dK * nhead + h * dK + k;
                        float val = ((Number) com.zifang.util.numpy.Array.get(outData, srcIdx)).floatValue();
                        com.zifang.util.numpy.Array.set(resData, dstIdx, val);
                    }
                }
            }
        }

        return result;
    }

    // Softmax over the key dimension (axis 1, which is the seqLen dimension)
    private NdArray softmax(NdArray input) {
        int seqLen = input.getShape().get(0);
        int keyDim = input.getShape().get(1);
        int batchSize = input.getShape().get(2);
        int nhead = input.getShape().get(3);

        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();

        for (int i = 0; i < seqLen; i++) {
            for (int b = 0; b < batchSize; b++) {
                for (int h = 0; h < nhead; h++) {
                    // Find max
                    float maxVal = Float.NEGATIVE_INFINITY;
                    for (int j = 0; j < keyDim; j++) {
                        int idx = ((i * keyDim + j) * batchSize + b) * nhead + h;
                        float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                        if (val > maxVal) maxVal = val;
                    }

                    // Compute exp and sum
                    float sum = 0.0f;
                    float[] expVals = new float[keyDim];
                    for (int j = 0; j < keyDim; j++) {
                        int idx = ((i * keyDim + j) * batchSize + b) * nhead + h;
                        float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                        float expVal = (float) Math.exp(val - maxVal);
                        expVals[j] = expVal;
                        sum += expVal;
                    }

                    // Normalize
                    for (int j = 0; j < keyDim; j++) {
                        int idx = ((i * keyDim + j) * batchSize + b) * nhead + h;
                        com.zifang.util.numpy.Array.set(outData, idx, expVals[j] / sum);
                    }
                }
            }
        }

        return output;
    }

    // Feed-forward network: Linear(ReLU(Linear(x)))
    private NdArray feedForward(NdArray input, int layer) {
        // First linear + ReLU
        // weight is already (dimFeedforward, dModel), no transpose needed
        NdArray hidden = relu(add(matmul(input, ffLinear1Weight[layer]), ffLinear1Bias[layer]));

        // Second linear
        // weight is already (dModel, dimFeedforward), no transpose needed
        NdArray output = add(matmul(hidden, ffLinear2Weight[layer]), ffLinear2Bias[layer]);

        return output;
    }

    // ReLU activation
    private NdArray relu(NdArray input) {
        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        int size = input.size();

        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            com.zifang.util.numpy.Array.set(outData, i, Math.max(0, val));
        }
        return output;
    }

    // Layer normalization
    private NdArray layerNorm(NdArray input, NdArray gamma, NdArray beta) {
        Shape shape = input.getShape();
        int seqLen = shape.get(0);
        int batchSize = shape.get(1);
        int dModel = shape.get(2);

        NdArray output = NdArray.zeros(shape, DType.FLOAT32);
        Object inData = input.getData();
        Object outData = output.getData();
        Object gammaData = gamma.getData();
        Object betaData = beta.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < seqLen; i++) {
                // Compute mean
                float mean = 0.0f;
                for (int j = 0; j < dModel; j++) {
                    int idx = (i * batchSize + b) * dModel + j;
                    mean += ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                }
                mean /= dModel;

                // Compute variance
                float variance = 0.0f;
                for (int j = 0; j < dModel; j++) {
                    int idx = (i * batchSize + b) * dModel + j;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    variance += (val - mean) * (val - mean);
                }
                variance /= dModel;

                // Normalize and scale
                float std = (float) Math.sqrt(variance + 1e-8);
                for (int j = 0; j < dModel; j++) {
                    int idx = (i * batchSize + b) * dModel + j;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    float normalized = (val - mean) / std;
                    float g = ((Number) com.zifang.util.numpy.Array.get(gammaData, j)).floatValue();
                    float b_val = ((Number) com.zifang.util.numpy.Array.get(betaData, j)).floatValue();
                    com.zifang.util.numpy.Array.set(outData, idx, g * normalized + b_val);
                }
            }
        }

        return output;
    }

    // Add positional encoding
    private NdArray addPositionalEncoding(NdArray input) {
        int seqLen = input.getShape().get(0);
        int batchSize = input.getShape().get(1);

        NdArray output = input.copy();
        Object inData = input.getData();
        Object outData = output.getData();

        for (int pos = 0; pos < seqLen; pos++) {
            for (int i = 0; i < dModel; i++) {
                float angle = pos / (float) Math.pow(10000, (2 * (i / 2)) / dModel);
                float pe = (i % 2 == 0) ? (float) Math.sin(angle) : (float) Math.cos(angle);

                for (int b = 0; b < batchSize; b++) {
                    int idx = (pos * batchSize + b) * dModel + i;
                    float val = ((Number) com.zifang.util.numpy.Array.get(inData, idx)).floatValue();
                    com.zifang.util.numpy.Array.set(outData, idx, val + pe);
                }
            }
        }

        return output;
    }

    // Matrix multiplication (supports 2D and 3D batched)
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
        } else if (a.ndim() == 3 && b.ndim() == 2) {
            // 3D input [seqLen, batch, dModel] @ 2D weight [dModel, outDim]
            // Treat as batched matmul: flatten first two dims, multiply, reshape back
            int seqLen = a.getShape().get(0);
            int batch = a.getShape().get(1);
            int k = a.getShape().get(2);
            int n = b.getShape().get(1);

            int m = seqLen * batch;

            // Flatten a to [m, k]
            NdArray aFlat = NdArray.zeros(new Shape(m, k), DType.FLOAT32);
            Object aData = a.getData();
            Object aFlatData = aFlat.getData();
            for (int i = 0; i < seqLen; i++) {
                for (int j = 0; j < batch; j++) {
                    for (int l = 0; l < k; l++) {
                        int srcIdx = (i * batch + j) * k + l;
                        int dstIdx = (i * batch + j) * k + l;
                        float val = ((Number) com.zifang.util.numpy.Array.get(aData, srcIdx)).floatValue();
                        com.zifang.util.numpy.Array.set(aFlatData, dstIdx, val);
                    }
                }
            }

            // Multiply: [m, k] @ [k, n] -> [m, n]
            NdArray resultFlat = NdArray.zeros(new Shape(m, n), DType.FLOAT32);
            Object bData = b.getData();
            Object cFlatData = resultFlat.getData();

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    float sum = 0.0f;
                    for (int l = 0; l < k; l++) {
                        float aVal = ((Number) com.zifang.util.numpy.Array.get(aFlatData, i * k + l)).floatValue();
                        float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, l * n + j)).floatValue();
                        sum += aVal * bVal;
                    }
                    com.zifang.util.numpy.Array.set(cFlatData, i * n + j, sum);
                }
            }

            // Reshape back to [seqLen, batch, n]
            NdArray result = NdArray.zeros(new Shape(seqLen, batch, n), DType.FLOAT32);
            Object resultData = result.getData();
            Object resFlatData = resultFlat.getData();
            for (int i = 0; i < seqLen; i++) {
                for (int j = 0; j < batch; j++) {
                    for (int l = 0; l < n; l++) {
                        int srcIdx = (i * batch + j) * n + l;
                        int dstIdx = (i * batch + j) * n + l;
                        float val = ((Number) com.zifang.util.numpy.Array.get(resFlatData, srcIdx)).floatValue();
                        com.zifang.util.numpy.Array.set(resultData, dstIdx, val);
                    }
                }
            }
            return result;
        }
        throw new IllegalArgumentException("matmul only supports 2D or 3D@2D matrices, got a.ndim=" + a.ndim() + ", b.ndim=" + b.ndim());
    }

    // Addition
    private NdArray add(NdArray a, NdArray b) {
        Shape shapeA = a.getShape();
        Shape shapeB = b.getShape();

        if (shapeA.equals(shapeB)) {
            return addNoBroadcast(a, b);
        }

        // Broadcasting: b's trailing dimsB dimensions are broadcast across a's last dimsB dimensions
        int dimsA = shapeA.ndim();
        int dimsB = shapeB.ndim();

        // Verify broadcast compatibility: for trailing dims, each dimA must equal dimB or be 1, or dimB must equal dimA
        for (int i = 0; i < dimsB; i++) {
            int dimA = shapeA.get(dimsA - dimsB + i);
            int dimB = shapeB.get(i);
            // Valid if: dimB==1 OR dimA==dimB OR dimA==1
            boolean valid = (dimB == 1) || (dimA == dimB) || (dimA == 1);
            if (!valid) {
                throw new IllegalArgumentException("Cannot broadcast shapes " + shapeA + " and " + shapeB);
            }
        }

        NdArray result = NdArray.zeros(shapeA, DType.FLOAT32);
        Object aData = a.getData();
        Object bData = b.getData();
        Object cData = result.getData();
        int size = result.size();
        int bSize = b.size();

        for (int i = 0; i < size; i++) {
            float aVal = ((Number) com.zifang.util.numpy.Array.get(aData, i)).floatValue();
            int bIdx = (bSize == 1) ? 0 : i % bSize;
            float bVal = ((Number) com.zifang.util.numpy.Array.get(bData, bIdx)).floatValue();
            com.zifang.util.numpy.Array.set(cData, i, aVal + bVal);
        }
        return result;
    }

    private NdArray addNoBroadcast(NdArray a, NdArray b) {
        Shape shapeA = a.getShape();
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
}

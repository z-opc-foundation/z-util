package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

import java.util.Random;

/**
 * VAE (Variational Autoencoder) implementation.
 * <p>
 * Architecture:
 * - Encoder: x -> [mu, logVar] (mean and log-variance of latent Gaussian)
 * - Reparameterization: z = mu + std * ε where ε ~ N(0,1)
 * - Decoder: z -> reconstruction
 * <p>
 * Loss: BCE(reconstruction, x) + KL(mu, logVar)
 * where KL = -0.5 * sum(1 + log(σ²) - μ² - σ²)
 */
public class VAE extends Module {

    private Module encoder;
    private Module decoder;
    private int latentDim;
    private int inputDim;

    private Random random;

    private NdArray savedMu;
    private NdArray savedLogVar;
    private NdArray savedZ;
    private NdArray savedReconstruction;

    /**
     * VAE方法。
     * * @param inputDim int类型参数
     *
     * @param latentDim  int类型参数
     * @param hiddenDims int[]类型参数
     */
    public VAE(int inputDim, int latentDim, int[] hiddenDims) {
        this.inputDim = inputDim;
        this.latentDim = latentDim;
        this.random = new Random();

        buildEncoder(inputDim, hiddenDims, latentDim);
        buildDecoder(latentDim, hiddenDims, inputDim);
    }

    private void buildEncoder(int inputDim, int[] hiddenDims, int latentDim) {
        com.zifang.util.ml.nn.Module[] encoderLayers = new com.zifang.util.ml.nn.Module[hiddenDims.length + 1];

        encoderLayers[0] = new Dense(inputDim, hiddenDims[0]);
        for (int i = 1; i < hiddenDims.length; i++) {
            encoderLayers[i] = new com.zifang.util.ml.nn.activations.ReLU();
            encoderLayers[i + 1] = new Dense(hiddenDims[i - 1], hiddenDims[i]);
        }

        int lastHidden = hiddenDims[hiddenDims.length - 1];
        encoderLayers[hiddenDims.length] = new Dense(lastHidden, 2 * latentDim);

        encoder = new Sequential(encoderLayers);
    }

    private void buildDecoder(int latentDim, int[] hiddenDims, int outputDim) {
        int[] decoderHiddenDims = new int[hiddenDims.length];
        for (int i = 0; i < hiddenDims.length; i++) {
            decoderHiddenDims[i] = hiddenDims[hiddenDims.length - 1 - i];
        }

        com.zifang.util.ml.nn.Module[] decoderLayers = new com.zifang.util.ml.nn.Module[decoderHiddenDims.length * 2 + 1];

        decoderLayers[0] = new Dense(latentDim, decoderHiddenDims[0]);
        for (int i = 1; i < decoderHiddenDims.length; i++) {
            decoderLayers[i * 2] = new com.zifang.util.ml.nn.activations.ReLU();
            decoderLayers[i * 2 + 1] = new Dense(decoderHiddenDims[i - 1], decoderHiddenDims[i]);
        }

        decoderLayers[decoderLayers.length - 1] = new Dense(decoderHiddenDims[decoderHiddenDims.length - 1], outputDim);

        decoder = new Sequential(decoderLayers);
    }

    @Override
    /**
     * forward方法。
     *      * @param x NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray forward(NdArray x) {
        NdArray encoderOutput = encoder.forward(x);

        int batchSize = encoderOutput.getShape().get(0);

        savedMu = NdArray.zeros(new Shape(batchSize, latentDim), DType.FLOAT32);
        savedLogVar = NdArray.zeros(new Shape(batchSize, latentDim), DType.FLOAT32);

        Object encData = encoderOutput.getData();
        Object muData = savedMu.getData();
        Object logVarData = savedLogVar.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < latentDim; i++) {
                float muVal = ((Number) com.zifang.util.numpy.Array.get(encData, b * 2 * latentDim + i)).floatValue();
                float logVarVal = ((Number) com.zifang.util.numpy.Array.get(encData, b * 2 * latentDim + latentDim + i)).floatValue();
                com.zifang.util.numpy.Array.set(muData, b * latentDim + i, muVal);
                com.zifang.util.numpy.Array.set(logVarData, b * latentDim + i, logVarVal);
            }
        }

        savedZ = sample(savedMu, savedLogVar);
        savedReconstruction = decoder.forward(savedZ);

        NdArray result = concatenate(new NdArray[]{savedReconstruction, savedMu, savedLogVar}, 1);
        return result;
    }

    @Override
    /**
     * backward方法。
     *      * @param gradOutput NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray backward(NdArray gradOutput) {
        int batchSize = gradOutput.getShape().get(0);

        NdArray gradRecon = slice(gradOutput, 0, batchSize, 0, inputDim);

        NdArray gradZ = decoder.backward(gradRecon);

        NdArray gradInput = NdArray.zeros(new Shape(batchSize, inputDim), DType.FLOAT32);

        gradInput = encoder.backward(gradInput);

        return gradInput;
    }

    /**
     * sample方法。
     * * @param mu NdArray类型参数
     *
     * @param logVar NdArray类型参数
     * @return NdArray类型返回值
     */
    public NdArray sample(NdArray mu, NdArray logVar) {
        int batchSize = mu.getShape().get(0);

        NdArray z = NdArray.zeros(new Shape(batchSize, latentDim), DType.FLOAT32);
        Object muData = mu.getData();
        Object logVarData = logVar.getData();
        Object zData = z.getData();

        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < latentDim; i++) {
                float muVal = ((Number) com.zifang.util.numpy.Array.get(muData, b * latentDim + i)).floatValue();
                float logVarVal = ((Number) com.zifang.util.numpy.Array.get(logVarData, b * latentDim + i)).floatValue();
                float std = (float) Math.sqrt(Math.exp(logVarVal));

                double u1 = random.nextDouble();
                double u2 = random.nextDouble();
                double epsilon = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);

                float zVal = muVal + std * (float) epsilon;
                com.zifang.util.numpy.Array.set(zData, b * latentDim + i, zVal);
            }
        }

        return z;
    }

    /**
     * decode方法。
     * * @param z NdArray类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray decode(NdArray z) {
        return decoder.forward(z);
    }

    /**
     * computeLoss方法。
     * * @param x NdArray类型参数
     *
     * @param reconstruction NdArray类型参数
     * @param mu             NdArray类型参数
     * @param logVar         NdArray类型参数
     * @return double类型返回值
     */
    public double computeLoss(NdArray x, NdArray reconstruction, NdArray mu, NdArray logVar) {
        double bceLoss = computeBCELoss(x, reconstruction);
        double klLoss = computeKLLoss(mu, logVar);
        return bceLoss + klLoss;
    }

    private double computeBCELoss(NdArray x, NdArray reconstruction) {
        int size = x.size();
        Object xData = x.getData();
        Object reconData = reconstruction.getData();

        double loss = 0.0;
        for (int i = 0; i < size; i++) {
            float xVal = ((Number) com.zifang.util.numpy.Array.get(xData, i)).floatValue();
            float reconVal = ((Number) com.zifang.util.numpy.Array.get(reconData, i)).floatValue();

            reconVal = clamp(reconVal, 1e-7f, 1 - 1e-7f);
            loss += -xVal * Math.log(reconVal) - (1 - xVal) * Math.log(1 - reconVal);
        }

        return loss / size;
    }

    private double computeKLLoss(NdArray mu, NdArray logVar) {
        int batchSize = mu.getShape().get(0);
        Object muData = mu.getData();
        Object logVarData = logVar.getData();

        double klLoss = 0.0;
        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < latentDim; i++) {
                float muVal = ((Number) com.zifang.util.numpy.Array.get(muData, b * latentDim + i)).floatValue();
                float logVarVal = ((Number) com.zifang.util.numpy.Array.get(logVarData, b * latentDim + i)).floatValue();
                float var = (float) Math.exp(logVarVal);

                klLoss += 0.5 * (logVarVal - muVal * muVal - var + 1);
            }
        }

        return klLoss / batchSize;
    }

    /**
     * getEncoder方法。
     *
     * @return Module类型返回值
     */
    public Module getEncoder() {
        return encoder;
    }

    /**
     * getDecoder方法。
     *
     * @return Module类型返回值
     */
    public Module getDecoder() {
        return decoder;
    }

    /**
     * getLatentDim方法。
     *
     * @return int类型返回值
     */
    public int getLatentDim() {
        return latentDim;
    }

    private NdArray concatenate(NdArray[] arrays, int axis) {
        if (arrays.length == 0) throw new IllegalArgumentException("Empty array");

        if (axis == 1) {
            int batchSize = arrays[0].getShape().get(0);
            int totalFeatures = 0;
            for (NdArray arr : arrays) {
                totalFeatures += arr.getShape().get(1);
            }

            NdArray result = NdArray.zeros(new Shape(batchSize, totalFeatures), DType.FLOAT32);
            Object outData = result.getData();

            int offset = 0;
            for (NdArray arr : arrays) {
                Object inData = arr.getData();
                for (int b = 0; b < batchSize; b++) {
                    for (int i = 0; i < arr.getShape().get(1); i++) {
                        int outIdx = b * totalFeatures + offset + i;
                        int inIdx = b * arr.getShape().get(1) + i;
                        com.zifang.util.numpy.Array.set(outData, outIdx,
                                com.zifang.util.numpy.Array.get(inData, inIdx));
                    }
                }
                offset += arr.getShape().get(1);
            }
            return result;
        }

        throw new IllegalArgumentException("Unsupported axis: " + axis);
    }

    private NdArray slice(NdArray input, int rowStart, int rowEnd, int colStart, int colEnd) {
        int batchSize = rowEnd - rowStart;
        int features = colEnd - colStart;

        NdArray result = NdArray.zeros(new Shape(batchSize, features), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = result.getData();

        int totalFeatures = input.getShape().get(1);
        for (int b = 0; b < batchSize; b++) {
            for (int i = 0; i < features; i++) {
                int inIdx = (rowStart + b) * totalFeatures + colStart + i;
                int outIdx = b * features + i;
                com.zifang.util.numpy.Array.set(outData, outIdx,
                        com.zifang.util.numpy.Array.get(inData, inIdx));
            }
        }

        return result;
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    @Override
    /**
     * train方法。
     */
    public void train() {
        super.train();
        encoder.train();
        decoder.train();
    }

    @Override
    /**
     * eval方法。
     */
    public void eval() {
        super.eval();
        encoder.eval();
        decoder.eval();
    }
}

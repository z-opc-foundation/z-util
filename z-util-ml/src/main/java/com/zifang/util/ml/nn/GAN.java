package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

import java.util.List;
import java.util.Random;

/**
 * GAN (Generative Adversarial Network) implementation.
 * <p>
 * The GAN consists of:
 * - Generator: Takes random noise and generates fake samples
 * - Discriminator: Distinguishes real samples from fake samples
 * <p>
 * Training:
 * 1. Train D: real loss = -log(D(real)), fake loss = -log(1 - D(G(z)))
 * 2. Train G: loss = -log(D(G(z))) (maximize D's error)
 */
public class GAN {

    private static final double EPSILON = 1e-12;
    private Module generator;
    private Module discriminator;
    private com.zifang.util.ml.optim.Optimizer genOptimizer;
    private com.zifang.util.ml.optim.Optimizer discOptimizer;
    private int latentDim;
    private Random random;

    /**
     * GAN方法。
     * * @param generator Module类型参数
     *
     * @param discriminator Module类型参数
     * @param latentDim     int类型参数
     * @param genOptimizer  com.zifang.util.ml.optim.Optimizer类型参数
     * @param discOptimizer com.zifang.util.ml.optim.Optimizer类型参数
     */
    public GAN(Module generator, Module discriminator, int latentDim,
               com.zifang.util.ml.optim.Optimizer genOptimizer,
               com.zifang.util.ml.optim.Optimizer discOptimizer) {
        this.generator = generator;
        this.discriminator = discriminator;
        this.latentDim = latentDim;
        this.genOptimizer = genOptimizer;
        this.discOptimizer = discOptimizer;
        this.random = new Random();

        // Register generator parameters
        int paramIdx = 0;
        for (NdArray param : generator.parameters()) {
            genOptimizer.addParameter("gen_param_" + paramIdx, param);
            paramIdx++;
        }

        // Register discriminator parameters
        paramIdx = 0;
        for (NdArray param : discriminator.parameters()) {
            discOptimizer.addParameter("disc_param_" + paramIdx, param);
            paramIdx++;
        }
    }

    /**
     * Train the GAN on real samples.
     */
    public void train(NdArray realSamples, int epochs, int batchSize) {
        int nSamples = realSamples.getShape().get(0);
        int actualBatchSize = Math.min(batchSize, nSamples);

        for (int epoch = 0; epoch < epochs; epoch++) {
            NdArray shuffledReal = shuffle(realSamples);

            int numBatches = nSamples / actualBatchSize;

            for (int batch = 0; batch < numBatches; batch++) {
                int startIdx = batch * actualBatchSize;
                int endIdx = Math.min(startIdx + actualBatchSize, nSamples);

                NdArray realBatch = sliceBatch(shuffledReal, startIdx, endIdx);
                NdArray noise = generateNoise(endIdx - startIdx);

                trainDiscriminator(realBatch, noise);
                trainGenerator(noise);

                if (batch % 100 == 0) {
                    double discLoss = computeDiscriminatorLoss(realBatch, noise);
                    double genLoss = computeGeneratorLoss(noise);
                    System.out.printf("Epoch [%d/%d] Batch [%d/%d] Disc Loss: %.4f Gen Loss: %.4f%n",
                            epoch + 1, epochs, batch + 1, numBatches, discLoss, genLoss);
                }
            }
        }
    }

    private void trainDiscriminator(NdArray realSamples, NdArray noise) {
        NdArray fakeSamples = generator.forward(noise);

        NdArray realPredictions = discriminator.forward(realSamples);
        NdArray fakePredictions = discriminator.forward(fakeSamples);

        double realLoss = computeBCELoss(realPredictions, ones(realSamples.getShape().get(0)));
        double fakeLoss = computeBCELoss(fakePredictions, zeros(fakeSamples.getShape().get(0)));

        discOptimizer.zeroGrad();
        applyGradientStep(discriminator, 0.001);
    }

    private void trainGenerator(NdArray noise) {
        NdArray fakeSamples = generator.forward(noise);
        NdArray fakePredictions = discriminator.forward(fakeSamples);

        double genLoss = computeBCELoss(fakePredictions, ones(noise.getShape().get(0)));

        genOptimizer.zeroGrad();
        applyGradientStep(generator, 0.001);
    }

    /**
     * generate方法。
     * * @param nSamples int类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray generate(int nSamples) {
        NdArray noise = generateNoise(nSamples);
        return generator.forward(noise);
    }

    /**
     * getGeneratorParams方法。
     *
     * @return NdArray[]类型返回值
     */
    public NdArray[] getGeneratorParams() {
        List<NdArray> params = generator.parameters();
        return params.toArray(new NdArray[0]);
    }

    private NdArray generateNoise(int nSamples) {
        NdArray noise = NdArray.zeros(new Shape(nSamples, latentDim), DType.FLOAT32);
        Object noiseData = noise.getData();

        for (int i = 0; i < nSamples * latentDim; i++) {
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
            com.zifang.util.numpy.Array.set(noiseData, i, (float) z);
        }

        return noise;
    }

    private double computeBCELoss(NdArray predictions, NdArray targets) {
        int size = predictions.size();
        Object predData = predictions.getData();
        Object targetData = targets.getData();

        double loss = 0.0;
        for (int i = 0; i < size; i++) {
            float pred = clamp(((Number) com.zifang.util.numpy.Array.get(predData, i)).floatValue(), (float) EPSILON, (float) (1 - EPSILON));
            float target = ((Number) com.zifang.util.numpy.Array.get(targetData, i)).floatValue();
            loss += -target * Math.log(pred) - (1 - target) * Math.log(1 - pred);
        }

        return loss / size;
    }

    private double computeDiscriminatorLoss(NdArray realSamples, NdArray noise) {
        int batchSize = realSamples.getShape().get(0);

        NdArray fakeSamples = generator.forward(noise);
        NdArray realPred = discriminator.forward(realSamples);
        NdArray fakePred = discriminator.forward(fakeSamples);

        return computeBCELoss(realPred, ones(batchSize)) +
                computeBCELoss(fakePred, zeros(batchSize));
    }

    private double computeGeneratorLoss(NdArray noise) {
        NdArray fakeSamples = generator.forward(noise);
        NdArray fakePred = discriminator.forward(fakeSamples);

        return computeBCELoss(fakePred, ones(noise.getShape().get(0)));
    }

    private void applyGradientStep(Module module, double lr) {
        List<NdArray> params = module.parameters();
        for (NdArray param : params) {
            Object paramData = param.getData();
            int size = param.size();

            for (int j = 0; j < size; j++) {
                float val = ((Number) com.zifang.util.numpy.Array.get(paramData, j)).floatValue();
                val += (float) (random.nextGaussian() * lr * 0.01);
                com.zifang.util.numpy.Array.set(paramData, j, val);
            }
        }
    }

    private NdArray shuffle(NdArray input) {
        int[] indices = new int[input.getShape().get(0)];
        for (int i = 0; i < indices.length; i++) indices[i] = i;

        for (int i = indices.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;
        }

        NdArray shuffled = NdArray.zeros(input.getShape(), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = shuffled.getData();
        int rowSize = input.size() / input.getShape().get(0);

        for (int i = 0; i < indices.length; i++) {
            for (int j = 0; j < rowSize; j++) {
                int srcIdx = indices[i] * rowSize + j;
                int dstIdx = i * rowSize + j;
                Object val = com.zifang.util.numpy.Array.get(inData, srcIdx);
                com.zifang.util.numpy.Array.set(outData, dstIdx, val);
            }
        }

        return shuffled;
    }

    private NdArray sliceBatch(NdArray input, int start, int end) {
        int batchSize = end - start;
        Shape shape = input.getShape();

        NdArray batch = NdArray.zeros(new Shape(batchSize, shape.get(1)), DType.FLOAT32);
        Object inData = input.getData();
        Object outData = batch.getData();
        int rowSize = shape.get(1);

        for (int i = 0; i < batchSize; i++) {
            for (int j = 0; j < rowSize; j++) {
                int srcIdx = (start + i) * rowSize + j;
                int dstIdx = i * rowSize + j;
                Object val = com.zifang.util.numpy.Array.get(inData, srcIdx);
                com.zifang.util.numpy.Array.set(outData, dstIdx, val);
            }
        }
        return batch;
    }

    private NdArray ones(int n) {
        NdArray result = NdArray.zeros(new Shape(n, 1), DType.FLOAT32);
        Object data = result.getData();
        for (int i = 0; i < n; i++) {
            com.zifang.util.numpy.Array.set(data, i, 1.0f);
        }
        return result;
    }

    private NdArray zeros(int n) {
        return NdArray.zeros(new Shape(n, 1), DType.FLOAT32);
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * getGenerator方法。
     *
     * @return Module类型返回值
     */
    public Module getGenerator() {
        return generator;
    }

    /**
     * getDiscriminator方法。
     *
     * @return Module类型返回值
     */
    public Module getDiscriminator() {
        return discriminator;
    }

    /**
     * train方法。
     */
    public void train() {
        generator.train();
        discriminator.train();
    }

    /**
     * eval方法。
     */
    public void eval() {
        generator.eval();
        discriminator.eval();
    }
}

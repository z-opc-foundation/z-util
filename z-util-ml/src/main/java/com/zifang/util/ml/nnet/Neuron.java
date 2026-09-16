package com.zifang.util.ml.nnet;

import java.util.Random;

/**
 * 神经元
 */
public class Neuron {

    private static final Random RANDOM = new Random();
    private final double[] weights;
    private final double bias;
    private double output;
    private double delta;

    /**
     * 创建神经元
     *
     * @param inputCount 输入数量
     */
    public Neuron(int inputCount) {
        this.weights = new double[inputCount];
        initializeWeights();
        this.bias = RANDOM.nextDouble() * 2 - 1;
    }

    private void initializeWeights() {
        // Xavier初始化
        double limit = Math.sqrt(6.0 / (weights.length + 1));
        for (int i = 0; i < weights.length; i++) {
            weights[i] = RANDOM.nextDouble() * 2 * limit - limit;
        }
    }

    /**
     * 计算神经元输出
     *
     * @param inputs             输入数据
     * @param activationFunction 激活函数
     * @return 神经元输出值
     */
    public double calculateOutput(double[] inputs, ActivationFunction activationFunction) {
        double sum = 0;
        for (int i = 0; i < inputs.length; i++) {
            sum += inputs[i] * weights[i];
        }
        sum += bias;
        this.output = activationFunction.activate(sum);
        return this.output;
    }

    /**
     * 获取神经元的权重数组
     *
     * @return 权重数组副本
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     * 获取神经元的偏置值
     *
     * @return 偏置值
     */
    public double getBias() {
        return bias;
    }

    /**
     * 获取神经元上一次计算的输出值
     *
     * @return 输出值
     */
    public double getOutput() {
        return output;
    }

    /**
     * 设置神经元的输出值
     *
     * @param output 输出值
     */
    public void setOutput(double output) {
        this.output = output;
    }

    /**
     * 获取神经元的误差值（用于反向传播）
     *
     * @return 误差值
     */
    public double getDelta() {
        return delta;
    }

    /**
     * 设置神经元的误差值（用于反向传播）
     *
     * @param delta 误差值
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }
}
package com.zifang.util.ml.nnet;

import java.util.ArrayList;
import java.util.List;

/**
 * 神经网络
 */
public class NeuralNetwork {

    private final List<Layer> layers;
    private LossFunction lossFunction;
    private double learningRate;

    /**
     * NeuralNetwork方法。
     */
    public NeuralNetwork() {
        this.layers = new ArrayList<>();
        this.learningRate = 0.01;
    }

    /**
     * 设置学习率
     *
     * @param learningRate 学习率，必须大于0
     * @return 当前神经网络实例（支持链式调用）
     */
    public NeuralNetwork learningRate(double learningRate) {
        this.learningRate = learningRate;
        return this;
    }

    /**
     * 设置损失函数
     *
     * @param lossFunction 损失函数实例，不能为null
     * @return 当前神经网络实例（支持链式调用）
     */
    public NeuralNetwork lossFunction(LossFunction lossFunction) {
        this.lossFunction = lossFunction;
        return this;
    }

    /**
     * 添加层到神经网络
     *
     * @param layer 要添加的层，不能为null
     * @return 当前神经网络实例（支持链式调用）
     */
    public NeuralNetwork addLayer(Layer layer) {
        layers.add(layer);
        return this;
    }

    /**
     * 前向传播：根据输入计算神经网络输出
     *
     * @param inputs 输入数据，数组长度需与输入层匹配
     * @return 神经网络输出数组
     */
    public double[] predict(double[] inputs) {
        double[] current = inputs;
        for (Layer layer : layers) {
            current = layer.forward(current);
        }
        return current;
    }

    /**
     * 执行一步训练：前向传播 + 反向传播
     *
     * @param inputs  输入数据，数组长度需与输入层匹配
     * @param targets 目标数据，数组长度需与输出层匹配
     * @return 本次训练的损失值
     */
    public double train(double[] inputs, double[] targets) {
        // 前向传播
        double[] outputs = predict(inputs);

        // 计算损失
        double loss = 0;
        if (lossFunction != null) {
            loss = lossFunction.compute(outputs, targets);
        }

        // 反向传播
        double[] gradients = lossFunction != null
                ? lossFunction.gradient(outputs, targets)
                : calculateDefaultGradient(outputs, targets);

        for (int i = layers.size() - 1; i >= 0; i--) {
            gradients = layers.get(i).backward(gradients);
        }

        return loss;
    }

    private double[] calculateDefaultGradient(double[] predictions, double[] targets) {
        double[] gradient = new double[predictions.length];
        for (int i = 0; i < predictions.length; i++) {
            gradient[i] = predictions[i] - targets[i];
        }
        return gradient;
    }

    /**
     * 获取神经网络的所有层
     *
     * @return 层列表
     */
    public List<Layer> getLayers() {
        return layers;
    }

    /**
     * 获取当前学习率
     *
     * @return 学习率
     */
    public double getLearningRate() {
        return learningRate;
    }
}
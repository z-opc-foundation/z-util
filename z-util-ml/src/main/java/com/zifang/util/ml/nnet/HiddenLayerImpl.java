package com.zifang.util.ml.nnet;

import java.util.Arrays;

/**
 * 隐藏层实现
 */
public class HiddenLayerImpl implements Layer {

    private final int neuronCount;
    private final Neuron[] neurons;
    private final ActivationFunction activationFunction;
    private double[] inputs;
    private double[] outputs;

    /**
     * 创建隐藏层
     *
     * @param inputCount         输入数量（上一层的神经元数量）
     * @param neuronCount        当前层的神经元数量
     * @param activationFunction 激活函数
     * @throws IllegalArgumentException 当参数小于1时
     */
    public HiddenLayerImpl(int inputCount, int neuronCount, ActivationFunction activationFunction) {
        this.neuronCount = neuronCount;
        this.activationFunction = activationFunction;
        this.neurons = new Neuron[neuronCount];
        this.outputs = new double[neuronCount];

        for (int i = 0; i < neuronCount; i++) {
            neurons[i] = new Neuron(inputCount);
        }
    }

    @Override
    /**
     * forward方法。
     *      * @param inputs double[]类型参数
     * @return double[]类型返回值
     */
    public double[] forward(double[] inputs) {
        this.inputs = Arrays.copyOf(inputs, inputs.length);
        this.outputs = new double[neuronCount];

        for (int i = 0; i < neuronCount; i++) {
            outputs[i] = neurons[i].calculateOutput(inputs, activationFunction);
        }

        return outputs;
    }

    @Override
    /**
     * backward方法。
     *      * @param gradients double[]类型参数
     * @return double[]类型返回值
     */
    public double[] backward(double[] gradients) {
        double[] inputGradients = new double[inputs.length];

        for (int i = 0; i < neuronCount; i++) {
            double gradient = gradients[i] * activationFunction.derivative(neurons[i].getOutput());
            neurons[i].setDelta(gradient);

            double[] weights = neurons[i].getWeights();
            for (int j = 0; j < inputs.length; j++) {
                inputGradients[j] += gradient * weights[j];
            }
        }

        return inputGradients;
    }

    @Override
    /**
     * getOutput方法。
     * @return double[]类型返回值
     */
    public double[] getOutput() {
        return outputs;
    }

    @Override
    /**
     * getLayerType方法。
     * @return LayerType类型返回值
     */
    public LayerType getLayerType() {
        return LayerType.HIDDEN;
    }

    /**
     * 获取该层的神经元数组
     *
     * @return 神经元数组
     */
    public Neuron[] getNeurons() {
        return neurons;
    }

    /**
     * 获取该层的神经元数量
     *
     * @return 神经元数量
     */
    public int getNeuronCount() {
        return neuronCount;
    }
}
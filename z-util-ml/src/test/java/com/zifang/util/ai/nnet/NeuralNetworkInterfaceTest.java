package com.zifang.util.ml.nnet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 神经网络接口测试
 */

/**
 * NeuralNetworkInterfaceTest类。
 */
public class NeuralNetworkInterfaceTest {

    @Test
    void testSigmoidActivation() {
        ActivationFunction sigmoid = new SigmoidActivation();

        assertEquals(0.5, sigmoid.activate(0), 0.0001);
        assertTrue(sigmoid.activate(-10) < 0.0001);
        assertTrue(sigmoid.activate(10) > 0.9999);

        double output = sigmoid.activate(0);
        double deriv = sigmoid.derivative(0);
        assertEquals(output * (1 - output), deriv, 0.0001);
    }

    @Test
    void testReLUActivation() {
        ActivationFunction relu = new ReLUActivation();

        assertEquals(0, relu.activate(-5), 0);
        assertEquals(5, relu.activate(5), 0);
        assertEquals(0, relu.derivative(-5), 0);
        assertEquals(1, relu.derivative(5), 0);
    }

    @Test
    void testMSELoss() {
        LossFunction mse = new MSELoss();

        double[] predictions = {0.9, 0.1};
        double[] targets = {1.0, 0.0};

        double loss = mse.compute(predictions, targets);
        assertTrue(loss < 0.1);

        double[] gradient = mse.gradient(predictions, targets);
        assertEquals(2, gradient.length);
    }

    @Test
    void testNeuronCreation() {
        Neuron neuron = new Neuron(3);

        assertEquals(3, neuron.getWeights().length);
        assertEquals(0, neuron.getOutput(), 0);
        assertEquals(0, neuron.getDelta(), 0);
    }

    @Test
    void testNeuronForward() {
        Neuron neuron = new Neuron(2);
        neuron.getWeights()[0] = 0.5;
        neuron.getWeights()[1] = 0.5;

        ActivationFunction sigmoid = new SigmoidActivation();
        double[] inputs = {1.0, 1.0};

        double output = neuron.calculateOutput(inputs, sigmoid);

        assertTrue(output > 0.5 && output < 1.0);
    }

    @Test
    void testHiddenLayerForward() {
        ActivationFunction sigmoid = new SigmoidActivation();
        HiddenLayerImpl layer = new HiddenLayerImpl(2, 3, sigmoid);

        double[] inputs = {1.0, 0.5};
        double[] outputs = layer.forward(inputs);

        assertEquals(3, outputs.length);
        for (double output : outputs) {
            assertTrue(output >= 0 && output <= 1);
        }
    }

    @Test
    void testNeuralNetworkBuilder() {
        NeuralNetwork network = new NeuralNetwork()
                .learningRate(0.1)
                .lossFunction(new MSELoss());

        network.addLayer(new HiddenLayerImpl(2, 4, new SigmoidActivation()));
        network.addLayer(new HiddenLayerImpl(4, 2, new SigmoidActivation()));

        assertEquals(2, network.getLayers().size());
        assertEquals(0.1, network.getLearningRate());
    }

    @Test
    void testNeuralNetworkPrediction() {
        NeuralNetwork network = new NeuralNetwork()
                .learningRate(0.1)
                .lossFunction(new MSELoss());

        network.addLayer(new HiddenLayerImpl(2, 4, new SigmoidActivation()));
        network.addLayer(new HiddenLayerImpl(4, 1, new SigmoidActivation()));

        double[] input = {1.0, 0.0};
        double[] output = network.predict(input);

        assertEquals(1, output.length);
        assertTrue(output[0] >= 0 && output[0] <= 1);
    }

    @Test
    void testNeuralNetworkTraining() {
        NeuralNetwork network = new NeuralNetwork()
                .learningRate(0.5)
                .lossFunction(new MSELoss());

        network.addLayer(new HiddenLayerImpl(2, 4, new SigmoidActivation()));
        network.addLayer(new HiddenLayerImpl(4, 1, new SigmoidActivation()));

        double lossBefore = 0;
        for (int i = 0; i < 100; i++) {
            double[] input = {0.5, 0.5};
            double[] target = {1.0};
            lossBefore += network.train(input, target);
        }
        lossBefore /= 100;

        double[] output = network.predict(new double[]{0.5, 0.5});
        System.out.println("Prediction: " + output[0] + ", target=1.0");

        assertTrue(output[0] >= 0 && output[0] <= 1);
    }
}
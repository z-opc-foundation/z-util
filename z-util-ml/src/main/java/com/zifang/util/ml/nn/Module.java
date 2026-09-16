package com.zifang.util.ml.nn;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import com.zifang.util.numpy.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all neural network modules.
 * Provides forward/backward propagation and parameter management.
 */
public abstract class Module {

    protected boolean training = true;
    /**
     * ArrayList<>方法。
     *
     * @return List<NdArray> parameters = new类型返回值
     */
    protected List<NdArray> parameters = new ArrayList<>();

    /**
     * Forward pass - computes output from input
     */
    public abstract NdArray forward(NdArray input);

    /**
     * Backward pass - computes gradient with respect to input
     *
     * @param gradOutput Gradient from upstream (dL/doutput)
     * @return Gradient with respect to input (dL/dinput)
     */
    public abstract NdArray backward(NdArray gradOutput);

    /**
     * Sets the module to training mode
     */
    public void train() {
        this.training = true;
    }

    /**
     * Sets the module to evaluation mode
     */
    public void eval() {
        this.training = false;
    }

    /**
     * Returns whether the module is in training mode
     */
    public boolean isTraining() {
        return training;
    }

    /**
     * Returns all parameters of this module
     */
    public List<NdArray> parameters() {
        return parameters;
    }

    /**
     * Registers a parameter tensor
     */
    protected void registerParameter(String name, NdArray param) {
        parameters.add(param);
    }

    /**
     * Creates a zero-initialized parameter tensor with given shape
     */
    protected NdArray createParameter(int... shape) {
        NdArray param = NdArray.zeros(new Shape(shape), DType.FLOAT32);
        parameters.add(param);
        return param;
    }

    /**
     * Applies Xavier/Glorot uniform initialization
     */
    protected NdArray initXavierUniform(NdArray tensor, int fanIn, int fanOut) {
        double limit = Math.sqrt(6.0 / (fanIn + fanOut));
        Object data = tensor.getData();
        int size = tensor.size();
        for (int i = 0; i < size; i++) {
            double value = (Math.random() * 2 - 1) * limit;
            com.zifang.util.numpy.Array.set(data, i, value);
        }
        return tensor;
    }

    /**
     * Applies Xavier/Glorot normal initialization
     */
    protected NdArray initXavierNormal(NdArray tensor, int fanIn, int fanOut) {
        double std = Math.sqrt(2.0 / (fanIn + fanOut));
        Object data = tensor.getData();
        int size = tensor.size();
        for (int i = 0; i < size; i++) {
            // Box-Muller transform for Gaussian
            double u1 = Math.random();
            double u2 = Math.random();
            double value = std * Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
            com.zifang.util.numpy.Array.set(data, i, value);
        }
        return tensor;
    }

    /**
     * Applies Kaiming/He normal initialization
     */
    protected NdArray initKaimingNormal(NdArray tensor, int fanIn) {
        double std = Math.sqrt(2.0 / fanIn);
        Object data = tensor.getData();
        int size = tensor.size();
        for (int i = 0; i < size; i++) {
            double u1 = Math.random();
            double u2 = Math.random();
            double value = std * Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
            com.zifang.util.numpy.Array.set(data, i, value);
        }
        return tensor;
    }

    /**
     * Resets all parameters to zero
     */
    public void zeroGrad() {
        for (NdArray param : parameters) {
            Object data = param.getData();
            int size = param.size();
            for (int i = 0; i < size; i++) {
                com.zifang.util.numpy.Array.set(data, i, 0.0f);
            }
        }
    }
}

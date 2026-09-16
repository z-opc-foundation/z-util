package com.zifang.util.ml.optim;

import com.zifang.util.numpy.NdArray;

/**
 * Represents a single parameter with its gradient for optimization.
 */
public class Parameter {
    private final String name;
    private final NdArray data;
    private NdArray gradient;

    /**
     * Parameter方法。
     * * @param name String类型参数
     *
     * @param data NdArray类型参数
     */
    public Parameter(String name, NdArray data) {
        this.name = name;
        this.data = data;
        this.gradient = null;
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * getData方法。
     *
     * @return NdArray类型返回值
     */
    public NdArray getData() {
        return data;
    }

    /**
     * getGradient方法。
     *
     * @return NdArray类型返回值
     */
    public NdArray getGradient() {
        return gradient;
    }

    /**
     * setGradient方法。
     * * @param gradient NdArray类型参数
     */
    public void setGradient(NdArray gradient) {
        this.gradient = gradient;
    }

    /**
     * getData方法。
     * * @param grad NdArray类型参数
     *
     * @return NdArray类型返回值
     */
    public NdArray getData(NdArray grad) {
        return data;
    }
}

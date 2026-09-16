package com.zifang.util.ml.nnet;

/**
 * 激活函数接口
 * <p>
 * 定义激活函数所需的基本操作，用于为神经元引入非线性
 */
public interface ActivationFunction {

    /**
     * 计算激活值
     *
     * @param input 输入值（神经元的加权求和结果）
     * @return 激活后的值
     */
    double activate(double input);

    /**
     * 计算激活函数在给定输入处的导数
     *
     * @param input 输入值（通常是激活函数的输出）
     * @return 导数值
     */
    double derivative(double input);
}
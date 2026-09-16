package com.zifang.util.ml.nnet;

/**
 * 损失函数接口
 * <p>
 * 定义损失函数所需的基本操作，用于评估神经网络的预测结果与真实结果的差异
 */
public interface LossFunction {

    /**
     * 计算损失值
     *
     * @param predictions 预测值数组
     * @param targets     目标值数组
     * @return 损失值，值越小表示预测越准确
     * @throws IllegalArgumentException 当数组长度不匹配时
     */
    double compute(double[] predictions, double[] targets);

    /**
     * 计算损失函数关于预测值的梯度
     *
     * @param predictions 预测值数组
     * @param targets     目标值数组
     * @return 梯度数组，用于反向传播
     * @throws IllegalArgumentException 当数组长度不匹配时
     */
    double[] gradient(double[] predictions, double[] targets);
}
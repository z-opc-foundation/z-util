package com.zifang.util.ml.nnet;

/**
 * 神经网络层接口
 * <p>
 * 定义神经网络中一层所需的基本操作，包括前向传播和反向传播
 */
public interface Layer {

    /**
     * 前向传播：将输入数据通过该层产生输出
     *
     * @param inputs 输入数据，数组长度需与该层的输入数量匹配
     * @return 输出数据，数组长度与该层的神经元数量匹配
     * @throws IllegalArgumentException 当输入数组长度不匹配时
     */
    double[] forward(double[] inputs);

    /**
     * 反向传播：根据输出层的梯度计算当前层的梯度
     *
     * @param gradients 从下一层传来的梯度，数组长度与该层的神经元数量匹配
     * @return 输入层的梯度，用于传递给上一层
     */
    double[] backward(double[] gradients);

    /**
     * 获取该层上一次前向传播的输出
     *
     * @return 输出数据数组
     */
    double[] getOutput();

    /**
     * 获取该层的类型
     *
     * @return 层类型（INPUT、HIDDEN或OUTPUT）
     */
    LayerType getLayerType();

    /**
     * 获取该层的神经元数量
     *
     * @return 神经元数量
     */
    int getNeuronCount();

    /**
     * 神经网络层类型枚举
     */
    enum LayerType {
        /**
         * 输入层
         */
        INPUT,
        /**
         * 隐藏层
         */
        HIDDEN,
        /**
         * 输出层
         */
        OUTPUT
    }
}
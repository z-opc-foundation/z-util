package com.zifang.util.visuallization.chart;

import com.zifang.util.ml.nnet.Layer;
import com.zifang.util.ml.nnet.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

/**
 * 神经网络可视化器
 * 用于可视化神经网络拓扑结构和训练过程中的损失变化
 */
public class NNVisualizer {

    private final NetworkGraph networkGraph;
    private final LineChart lossChart;
    private final List<Double> lossHistory;
    private int iteration = 0;

    /**
     * 创建神经网络可视化器
     */
    public NNVisualizer() {
        this.networkGraph = new NetworkGraph("Neural Network Topology", 800, 600);
        this.lossChart = new LineChart("Training Loss", 600, 400);
        this.lossHistory = new ArrayList<>();
    }

    /**
     * 初始化网络拓扑结构
     *
     * @param network 神经网络实例
     */
    public void initializeNetwork(NeuralNetwork network) {
        networkGraph.clear();
        List<Layer> layers = network.getLayers();

        if (layers.isEmpty()) {
            return;
        }

        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            String layerName = layer.getLayerType().name();
            networkGraph.addLayer(i, layer.getNeuronCount(), layerName);
        }

        networkGraph.render();
    }

    /**
     * 更新训练损失
     *
     * @param loss 当前损失值
     */
    public void updateLoss(double loss) {
        lossHistory.add(loss);
        updateLossChart();
        iteration++;
    }

    /**
     * 更新神经元激活值（用于热力图显示）
     *
     * @param layerIndex  层索引
     * @param activations 激活值数组，值范围建议为0-1
     */
    public void updateActivations(int layerIndex, double[] activations) {
        List<Double> activationList = new ArrayList<>();
        for (double a : activations) {
            activationList.add(a);
        }
        networkGraph.setLayerActivations(layerIndex, activationList);
        networkGraph.render();
    }

    /**
     * 获取网络拓扑图组件
     *
     * @return 网络拓扑图实例
     */
    public NetworkGraph getNetworkGraph() {
        return networkGraph;
    }

    /**
     * 获取损失曲线图组件
     *
     * @return 损失曲线图实例
     */
    public LineChart getLossChart() {
        return lossChart;
    }

    /**
     * 获取当前迭代次数
     *
     * @return 迭代次数
     */
    public int getIteration() {
        return iteration;
    }

    /**
     * 获取损失历史记录
     *
     * @return 损失值列表副本
     */
    public List<Double> getLossHistory() {
        return new ArrayList<>(lossHistory);
    }

    /**
     * 更新损失图表
     */
    private void updateLossChart() {
        lossChart.clear();
        lossChart.addSeries("Loss", new ArrayList<>(lossHistory));
        lossChart.setAxisLabels("Iteration", "Loss");
        lossChart.render();
    }
}

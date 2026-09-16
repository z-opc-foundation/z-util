package com.zifang.util.visuallization.swing.algrithm.lesson2.a;

/**
 * 算法可视化客户端入口
 * 用于启动圆形动画可视化
 *
 * @author zifang
 * @version 1.0
 * @since 2020-01-01
 */
public class Client {

    /**
     * 主方法，程序入口
     * 创建可视化场景并启动动画演示
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        int sceneWidth = 800;
        int sceneHeight = 800;
        int N = 100;

        AlgoVisualizer visualizer = new AlgoVisualizer(sceneWidth, sceneHeight, N);
    }
}

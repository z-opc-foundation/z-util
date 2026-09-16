package com.zifang.util.visuallization.swing.algrithm.lesson2.b;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;

/**
 * 算法可视化器（模板类）
 * 提供算法可视化的基础框架结构
 * 可作为创建自定义算法可视化器的父类
 *
 * @author zifang
 * @version 1.0
 * @since 2020-01-01
 */
public class AlgoVisualizer {

    private Object data;
    private AlgoFrame frame;

    /**
     * 创建可视化器
     *
     * @param sceneWidth  场景宽度（像素）
     * @param sceneHeight 场景高度（像素）
     */
    public AlgoVisualizer(int sceneWidth, int sceneHeight) {

        EventQueue.invokeLater(() -> {
            frame = new AlgoFrame("Welcome", sceneWidth, sceneHeight);
            frame.addKeyListener(new AlgoKeyListener());
            frame.addMouseListener(new AlgoMouseListener());
            new Thread(() -> {
                run();
            }).start();
        });
    }

    /**
     * 主方法，程序入口
     * 创建一个宽度800、高度800的可视化窗口
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {

        int sceneWidth = 800;
        int sceneHeight = 800;

        // TODO: 根据需要设置其他参数，初始化visualizer
        AlgoVisualizer visualizer = new AlgoVisualizer(sceneWidth, sceneHeight);
    }

    /**
     * 动画逻辑（由子类实现）
     */
    private void run() {
    }

    /**
     * 键盘事件监听器内部类
     * 预留用于处理键盘输入事件
     */
    private class AlgoKeyListener extends KeyAdapter {
    }

    /**
     * 鼠标事件监听器内部类
     * 预留用于处理鼠标点击事件
     */
    private class AlgoMouseListener extends MouseAdapter {
    }
}

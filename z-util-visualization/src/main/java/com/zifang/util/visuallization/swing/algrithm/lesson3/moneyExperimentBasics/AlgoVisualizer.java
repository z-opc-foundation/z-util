package com.zifang.util.visuallization.swing.algrithm.lesson3.moneyExperimentBasics;

import java.awt.*;

/**
 * 金钱实验可视化器基础版
 * 演示随机财富分配实验的基本效果
 * 实验规则：每个人初始拥有100单位财富，每轮随机选择一人转移1单位财富给另一个人
 *
 * @author zifang
 * @version 1.0
 * @since 2020-01-01
 */
public class AlgoVisualizer {

    private static int DELAY = 10;
    private int[] money;
    private AlgoFrame frame;

    /**
     * 创建可视化器
     *
     * @param sceneWidth  场景宽度（像素）
     * @param sceneHeight 场景高度（像素）
     */
    public AlgoVisualizer(int sceneWidth, int sceneHeight) {

        money = new int[100];
        for (int i = 0; i < money.length; i++)
            money[i] = 100;

        EventQueue.invokeLater(() -> {
            frame = new AlgoFrame("Money Problem", sceneWidth, sceneHeight);
            new Thread(() -> {
                run();
            }).start();
        });
    }

    /**
     * 主方法，程序入口
     * 创建一个宽度1000、高度800的可视化窗口
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {

        int sceneWidth = 1000;
        int sceneHeight = 800;

        AlgoVisualizer vis = new AlgoVisualizer(sceneWidth, sceneHeight);
    }

    /**
     * 运行动画循环
     * 持续执行财富随机分配实验，并在窗口中实时显示结果
     */
    public void run() {

        while (true) {

            frame.render(money);
            AlgoVisHelper.pause(DELAY);

            for (int i = 0; i < money.length; i++) {
                if (money[i] > 0) {
                    int j = (int) (Math.random() * money.length);
                    money[i] -= 1;
                    money[j] += 1;
                }
            }
        }
    }
}
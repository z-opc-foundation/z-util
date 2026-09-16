package com.zifang.util.visuallization.swing.algrithm.lesson3.moneyExperimentBasics2;

import java.awt.*;
import java.util.Arrays;

/**
 * 金钱实验可视化器增强版
 * 演示改进后的随机财富分配实验（排序、批量处理、允许负值）
 * 改进点：
 * 1. 每轮批量处理50次财富转移
 * 2. 显示前对财富数据进行排序
 * 3. 允许负财富值显示
 *
 * @author zifang
 * @version 1.0
 * @since 2020-01-01
 */
public class AlgoVisualizer {

    private static int DELAY = 40;
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
     * 批量执行财富随机分配实验，每批50次转移
     */
    public void run() {

        while (true) {

            Arrays.sort(money);
            frame.render(money);
            AlgoVisHelper.pause(DELAY);

            for (int k = 0; k < 50; k++) {
                for (int i = 0; i < money.length; i++) {
                    int j = (int) (Math.random() * money.length);
                    money[i] -= 1;
                    money[j] += 1;
                }
            }
        }
    }
}
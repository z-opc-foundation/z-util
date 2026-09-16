package com.zifang.util.visuallization.swing.algrithm.lesson3.moneyExperimentBasics2;

import javax.swing.*;
import java.awt.*;

/**
 * 金钱实验可视化框架（增强版）
 * 支持正负财富的显示，蓝色表示正值，红色表示负值
 */
public class AlgoFrame extends JFrame {

    private int canvasWidth;
    private int canvasHeight;
    private int[] money;

    /**
     * 创建可视化框架
     *
     * @param title        窗口标题
     * @param canvasWidth  画布宽度
     * @param canvasHeight 画布高度
     */
    public AlgoFrame(String title, int canvasWidth, int canvasHeight) {

        super(title);

        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        AlgoCanvas canvas = new AlgoCanvas();
        setContentPane(canvas);
        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        setVisible(true);
    }

    /**
     * 创建可视化框架（默认尺寸1024x768）
     *
     * @param title 窗口标题
     */
    public AlgoFrame(String title) {

        this(title, 1024, 768);
    }

    /**
     * 获取画布宽度
     *
     * @return 画布宽度
     */
    public int getCanvasWidth() {
        return canvasWidth;
    }

    /**
     * 获取画布高度
     *
     * @return 画布高度
     */
    public int getCanvasHeight() {
        return canvasHeight;
    }

    /**
     * 渲染财富数据
     *
     * @param money 财富数组
     */
    public void render(int[] money) {
        this.money = money;
        repaint();
    }

    /**
     * 算法画布内部类
     * 继承自JPanel，负责绘制财富分配可视化（支持正负值）
     * 使用双缓冲技术提升渲染性能
     * 正值显示为蓝色，负值显示为红色
     */
    private class AlgoCanvas extends JPanel {

        /**
         * AlgoCanvas方法。
         */
        public AlgoCanvas() {
            // 双缓存
            super(true);
        }

        @Override
        /**
         * paintComponent方法。
         *      * @param g Graphics类型参数
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            // 抗锯齿
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.addRenderingHints(hints);

            // 具体绘制
            int w = canvasWidth / money.length;
            for (int i = 0; i < money.length; i++)
                if (money[i] > 0) {
                    AlgoVisHelper.setColor(g2d, AlgoVisHelper.Blue);
                    AlgoVisHelper.fillRectangle(g2d,
                            i * w + 1, canvasHeight / 2 - money[i], w - 1, money[i]);
                } else {
                    AlgoVisHelper.setColor(g2d, AlgoVisHelper.Red);
                    AlgoVisHelper.fillRectangle(g2d,
                            i * w + 1, canvasHeight / 2, w - 1, -money[i]);
                }
        }

        @Override
        /**
         * getPreferredSize方法。
         * @return Dimension类型返回值
         */
        public Dimension getPreferredSize() {
            return new Dimension(canvasWidth, canvasHeight);
        }
    }
}
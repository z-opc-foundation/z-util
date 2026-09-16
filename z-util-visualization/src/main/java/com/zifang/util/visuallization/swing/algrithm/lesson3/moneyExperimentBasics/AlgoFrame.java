package com.zifang.util.visuallization.swing.algrithm.lesson3.moneyExperimentBasics;

import javax.swing.*;
import java.awt.*;

/**
 * 金钱实验可视化框架
 * 提供金钱分配实验的图表绘制功能
 */
public class AlgoFrame extends JFrame {

    int[] money;
    private int canvasWidth;
    private int canvasHeight;

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
     * 继承自JPanel，负责绘制财富分配可视化
     * 使用双缓冲技术提升渲染性能
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
            AlgoVisHelper.setColor(g2d, AlgoVisHelper.Blue);

            int w = canvasWidth / money.length;
            for (int i = 0; i < money.length; i++)
                AlgoVisHelper.fillRectangle(g2d,
                        i * w + 1, canvasHeight - money[i], w - 1, money[i]);

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
package com.zifang.util.visuallization.swing.algrithm.lesson2.a;

import javax.swing.*;
import java.awt.*;

/**
 * 算法可视化框架 - 画布框架
 * 提供算法可视化所需的绘图基础组件
 */
public class AlgoFrame extends JFrame {

    private int canvasWidth;
    private int canvasHeight;
    private Circle[] circles;

    /**
     * 创建算法可视化框架
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
     * 创建算法可视化框架（使用默认尺寸1024x768）
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
     * 渲染圆形数据
     *
     * @param circles 圆形数组
     */
    public void render(Circle[] circles) {
        this.circles = circles;
        repaint();
    }

    /**
     * 算法画布内部类
     * 继承自JPanel，负责绘制圆形动画
     * 使用双缓冲技术提升渲染性能，支持抗锯齿
     */
    private class AlgoCanvas extends JPanel {

        /**
         * 创建算法画布
         * 启用双缓冲以减少闪烁
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
            AlgoVisHelper.setStrokeWidth(g2d, 1);
            AlgoVisHelper.setColor(g2d, Color.RED);
            for (Circle circle : circles)
                if (circle.isFilled)
                    AlgoVisHelper.fillCircle(g2d, circle.x, circle.y, circle.getR());
                else
                    AlgoVisHelper.strokeCircle(g2d, circle.x, circle.y, circle.getR());
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



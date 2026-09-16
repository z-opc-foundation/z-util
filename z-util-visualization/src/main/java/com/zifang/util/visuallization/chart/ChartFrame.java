package com.zifang.util.visuallization.chart;

import javax.swing.*;
import java.awt.*;

/**
 * 图表窗口框架
 * 提供基于Swing的图表绘制基础框架，支持双缓冲和抗锯齿渲染
 */
public abstract class ChartFrame extends JFrame {

    protected int canvasWidth;
    protected int canvasHeight;
    protected ChartCanvas canvas;

    /**
     * 创建图表窗口框架
     *
     * @param title        窗口标题
     * @param canvasWidth  画布宽度
     * @param canvasHeight 画布高度
     */
    public ChartFrame(String title, int canvasWidth, int canvasHeight) {
        super(title);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        initCanvas();
    }

    /**
     * 创建图表窗口框架（使用默认尺寸800x600）
     *
     * @param title 窗口标题
     */
    public ChartFrame(String title) {
        this(title, 800, 600);
    }

    /**
     * 暂停指定时间（用于动画控制）
     *
     * @param millis 暂停时间（毫秒）
     */
    public static void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 初始化画布组件
     */
    protected void initCanvas() {
        canvas = createCanvas();
        setContentPane(canvas);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    /**
     * 创建具体的画布实例（抽象方法，由子类实现）
     *
     * @return 画布组件
     */
    protected abstract ChartCanvas createCanvas();

    /**
     * 获取画布宽度
     *
     * @return 画布宽度（像素）
     */
    public int getCanvasWidth() {
        return canvasWidth;
    }

    /**
     * 获取画布高度
     *
     * @return 画布高度（像素）
     */
    public int getCanvasHeight() {
        return canvasHeight;
    }

    /**
     * 触发画布重绘
     */
    public void render() {
        canvas.repaint();
    }

    protected class ChartCanvas extends JPanel {

        /**
         * ChartCanvas方法。
         */
        public ChartCanvas() {
            super(true);
        }

        @Override
        /**
         * paintComponent方法。
         *      * @param g Graphics类型参数
         */
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            enableAntialiasing(g2d);
        }

        /**
         * enableAntialiasing方法。
         * * @param g2d Graphics2D类型参数
         */
        protected void enableAntialiasing(Graphics2D g2d) {
            RenderingHints hints = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.addRenderingHints(hints);
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
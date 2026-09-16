package com.zifang.util.visuallization.swing.algrithm.lesson2.a;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * 算法可视化帮助类
 * 提供绘图辅助方法，用于算法可视化中的基本图形绘制操作
 *
 * @author zifang
 * @version 1.0
 * @since 2020-01-01
 */
public class AlgoVisHelper {

    /**
     * 私有构造函数，防止实例化
     */
    private AlgoVisHelper() {
    }

    /**
     * 绘制空心圆轮廓
     *
     * @param g 图形上下文，用于绘制图形
     * @param x 圆心X坐标（像素）
     * @param y 圆心Y坐标（像素）
     * @param r 圆的半径（像素）
     */
    public static void strokeCircle(Graphics2D g, int x, int y, int r) {

        Ellipse2D circle = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
        g.draw(circle);
    }

    /**
     * 绘制实心圆（填充圆）
     *
     * @param g 图形上下文，用于绘制图形
     * @param x 圆心X坐标（像素）
     * @param y 圆心Y坐标（像素）
     * @param r 圆的半径（像素）
     */
    public static void fillCircle(Graphics2D g, int x, int y, int r) {

        Ellipse2D circle = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
        g.fill(circle);
    }

    /**
     * 设置图形绘制颜色
     *
     * @param g     图形上下文，用于设置颜色
     * @param color 要设置的Color对象，包括预定义颜色如Color.RED等
     */
    public static void setColor(Graphics2D g, Color color) {
        g.setColor(color);
    }

    /**
     * 设置线条宽度
     *
     * @param g 图形上下文，用于设置线条属性
     * @param w 线条宽度（像素）
     */
    public static void setStrokeWidth(Graphics2D g, int w) {
        int strokeWidth = w;
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    /**
     * 线程暂停指定时间
     * 用于动画播放时的帧间隔控制
     *
     * @param t 暂停时间（毫秒）
     */
    public static void pause(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            System.out.println("Error sleeping");
        }
    }

}

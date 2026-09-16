package com.zifang.util.visuallization.swing.algrithm.lesson2.a;

import java.awt.*;

/**
 * 圆形数据类
 * 表示具有位置、半径和速度的圆形对象
 */
public class Circle {

    public int x, y;
    public int vx, vy;
    public boolean isFilled = false;
    private int r;

    /**
     * 创建圆形
     *
     * @param x  X坐标
     * @param y  Y坐标
     * @param r  半径
     * @param vx X方向速度
     * @param vy Y方向速度
     */
    public Circle(int x, int y, int r, int vx, int vy) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * 获取半径
     *
     * @return 半径
     */
    public int getR() {
        return r;
    }

    /**
     * 移动圆形（带边界碰撞检测）
     *
     * @param minx 最小X坐标
     * @param miny 最小Y坐标
     * @param maxx 最大X坐标
     * @param maxy 最大Y坐标
     */
    public void move(int minx, int miny, int maxx, int maxy) {
        x += vx;
        y += vy;

        checkCollision(minx, miny, maxx, maxy);
    }

    /**
     * 检测边界碰撞
     * 当圆形碰到边界时，反转相应的速度分量
     *
     * @param minx 最小X坐标
     * @param miny 最小Y坐标
     * @param maxx 最大X坐标
     * @param maxy 最大Y坐标
     */
    private void checkCollision(int minx, int miny, int maxx, int maxy) {
        if (x - r < minx || x + r > maxx) {
            vx = -vx;
        }
        if (y - r < miny || y + r > maxy) {
            vy = -vy;
        }
    }

    /**
     * 检测指定点是否在圆内
     * 使用欧几里得距离公式判断点到圆心的距离是否小于等于半径
     *
     * @param p 要检测的坐标点
     * @return 如果点P在圆内（包括边界）返回true，否则返回false
     */
    public boolean contain(Point p) {
        return (x - p.x) * (x - p.x) + (y - p.y) * (y - p.y) <= r * r;
    }

}

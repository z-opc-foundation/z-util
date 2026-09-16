package com.zifang.util.visuallization.robot;

import java.awt.*;

/**
 * Robot测试类
 * 演示Java Robot类移动鼠标的基本功能
 *
 * @author zifang
 * @version 1.0
 * @since 2020-01-01
 */
public class Robots {

    /**
     * 主方法，程序入口
     * 创建Robot对象并将鼠标移动到屏幕坐标(500, 501)
     *
     * @param args 命令行参数（未使用）
     * @throws AWTException 如果创建Robot对象失败
     */
    public static void main(String[] args) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(500, 501);
    }
}

package com.zifang.util.visuallization.robot;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * 秦码Robot演示类
 * 使用Robot自动化输入"HelloWorld"
 */
public class QinMaRobot {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(500, 500);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        // 输入HelloWorld
        robot.keyPress(KeyEvent.VK_CAPS_LOCK);
        robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
        robot.delay(500);
        robot.keyPress(KeyEvent.VK_H);
        robot.keyRelease(KeyEvent.VK_H);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_CAPS_LOCK);
        robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_E);
        robot.keyRelease(KeyEvent.VK_E);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_L);
        robot.keyRelease(KeyEvent.VK_L);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_L);
        robot.keyRelease(KeyEvent.VK_L);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_O);
        robot.keyRelease(KeyEvent.VK_O);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_CAPS_LOCK);
        robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_W);
        robot.keyRelease(KeyEvent.VK_W);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_CAPS_LOCK);
        robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_O);
        robot.keyRelease(KeyEvent.VK_O);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_R);
        robot.keyRelease(KeyEvent.VK_R);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_L);
        robot.keyRelease(KeyEvent.VK_L);
        robot.delay(200);
        robot.keyPress(KeyEvent.VK_D);
        robot.keyRelease(KeyEvent.VK_D);
        robot.delay(200);

    }

}

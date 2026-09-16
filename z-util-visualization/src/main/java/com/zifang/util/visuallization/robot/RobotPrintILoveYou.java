package com.zifang.util.visuallization.robot;


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Robot打印"ILoveYou"演示类
 * 演示使用Robot自动打开记事本并输入文字
 */
public class RobotPrintILoveYou {

// 打出一个大写的Q 
//	myRobot.keyPress(KeyEvent.VK_SHIFT);    // 模拟键盘按下shift键 
//	myRobot.keyPress(KeyEvent.VK_Q);        // 模拟键盘按下Q键（小写） 
//	myRobot.keyRelease(KeyEvent.VK_Q);      // 模拟键盘释放Q键 
//	myRobot.keyRelease(KeyEvent.VK_SHIFT);  // 模拟键盘释放shift键 
// 移动鼠标到坐标（x,y)处，并点击左键 
//	myRobot.mouseMove(x, y);                // 移动鼠标到坐标（x,y）处 
//	myRobot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);     // 模拟按下鼠标左键 
//	myRobot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);   // 模拟释放鼠标左键 
//	myRobot.mousePress(KeyEvent.BUTTON3_DOWN_MASK);     // 模拟按下鼠标右键
//	myRobot.mouseRelease(KeyEvent.BUTTON3_DOWN_MASK);   // 模拟释放鼠标右键 
// 设置每次输入的延迟为200ms 
//myRobot.setAutoDelay(200); ｉ

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        try {
//创建Windows命令打开记事本
            try {
                Runtime.getRuntime().exec("notepad");
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
//创建机器人
            Robot rb = new Robot();
            rb.keyPress(KeyEvent.VK_SHIFT);
            rb.keyRelease(KeyEvent.VK_SHIFT);
            rb.keyPress(KeyEvent.VK_I);
            rb.keyPress(KeyEvent.VK_SPACE);
            rb.keyPress(KeyEvent.VK_L);
            rb.keyPress(KeyEvent.VK_O);
            rb.keyPress(KeyEvent.VK_V);
            rb.keyPress(KeyEvent.VK_E);
            rb.keyPress(KeyEvent.VK_SPACE);
            rb.keyPress(KeyEvent.VK_Y);
            rb.keyPress(KeyEvent.VK_O);
            rb.keyPress(KeyEvent.VK_U);
//输入中文（复制）
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection("你好，世界"), null);
            rb.keyPress(KeyEvent.VK_CONTROL);
            rb.keyPress(KeyEvent.VK_V);
            rb.keyRelease(KeyEvent.VK_V);
            rb.keyRelease(KeyEvent.VK_CONTROL);

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}


//附加滑动
//实现滑动
//myRobot.mouseMove(540, 130);// 移动鼠标到坐标（x,y）处 
//myRobot.mousePress(KeyEvent.BUTTON1_DOWN_MASK);// 模拟按下鼠标左键 
//myRobot.setAutoDelay(2000); 
//myRobot.mouseMove(600, 130);
//myRobot.setAutoDelay(1000);
//myRobot.mouseMove(735, 130);
//myRobot.setAutoDelay(1000); 
//myRobot.mouseMove(835, 130);
//myRobot.mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);   // 模拟释放鼠标左键
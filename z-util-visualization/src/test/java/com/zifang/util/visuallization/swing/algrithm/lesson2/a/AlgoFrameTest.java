package com.zifang.util.visuallization.swing.algrithm.lesson2.a;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * AlgoFrame 单元测试
 * 测试算法可视化框架的构造和方法
 */

/**
 * AlgoFrameTest类。
 */
public class AlgoFrameTest {

    private AlgoFrame frame;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        // 使用小尺寸创建Frame用于测试，避免实际显示窗口
        frame = new AlgoFrame("Test Frame", 800, 600);
    }

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() {
        if (frame != null) {
            frame.dispose();
        }
    }

    @Test
    /**
     * testConstructorWithAllParameters方法。
     */
    public void testConstructorWithAllParameters() {
        AlgoFrame testFrame = new AlgoFrame("Test Title", 1024, 768);
        try {
            assertEquals(1024, testFrame.getCanvasWidth());
            assertEquals(768, testFrame.getCanvasHeight());
        } finally {
            testFrame.dispose();
        }
    }

    @Test
    /**
     * testConstructorWithDefaultSize方法。
     */
    public void testConstructorWithDefaultSize() {
        AlgoFrame testFrame = new AlgoFrame("Default Size Test");
        try {
            assertEquals(1024, testFrame.getCanvasWidth());
            assertEquals(768, testFrame.getCanvasHeight());
        } finally {
            testFrame.dispose();
        }
    }

    @Test
    /**
     * testGetCanvasWidth方法。
     */
    public void testGetCanvasWidth() {
        assertEquals(800, frame.getCanvasWidth());
    }

    @Test
    /**
     * testGetCanvasHeight方法。
     */
    public void testGetCanvasHeight() {
        assertEquals(600, frame.getCanvasHeight());
    }

    @Test
    /**
     * testGetCanvasWidthWithDifferentSizes方法。
     */
    public void testGetCanvasWidthWithDifferentSizes() {
        AlgoFrame smallFrame = new AlgoFrame("Small", 320, 240);
        try {
            assertEquals(320, smallFrame.getCanvasWidth());
            assertEquals(240, smallFrame.getCanvasHeight());
        } finally {
            smallFrame.dispose();
        }
    }

    @Test
    /**
     * testGetCanvasWidthWithLargeSize方法。
     */
    public void testGetCanvasWidthWithLargeSize() {
        AlgoFrame largeFrame = new AlgoFrame("Large", 1920, 1080);
        try {
            assertEquals(1920, largeFrame.getCanvasWidth());
            assertEquals(1080, largeFrame.getCanvasHeight());
        } finally {
            largeFrame.dispose();
        }
    }

    @Test
    /**
     * testRenderWithNullCircles方法。
     */
    public void testRenderWithNullCircles() {
        // 测试render方法接受null数组
        frame.render(null);
        // 不应抛出异常
    }

    @Test
    /**
     * testRenderWithEmptyCircles方法。
     */
    public void testRenderWithEmptyCircles() {
        // 测试render方法接受空数组
        Circle[] emptyCircles = new Circle[0];
        frame.render(emptyCircles);
        // 不应抛出异常
    }

    @Test
    /**
     * testRenderWithSingleCircle方法。
     */
    public void testRenderWithSingleCircle() {
        Circle circle = new Circle(100, 100, 50, 5, 5);
        circle.isFilled = false;
        frame.render(new Circle[]{circle});
        // 不应抛出异常
    }

    @Test
    /**
     * testRenderWithMultipleCircles方法。
     */
    public void testRenderWithMultipleCircles() {
        Circle circle1 = new Circle(100, 100, 50, 5, 5);
        circle1.isFilled = true;
        Circle circle2 = new Circle(200, 200, 30, -3, -3);
        circle2.isFilled = false;
        Circle circle3 = new Circle(300, 150, 40, 2, -2);
        circle3.isFilled = true;
        frame.render(new Circle[]{circle1, circle2, circle3});
        // 不应抛出异常
    }

    @Test
    /**
     * testRenderMultipleTimes方法。
     */
    public void testRenderMultipleTimes() {
        Circle circle = new Circle(100, 100, 50, 5, 5);
        // 多次调用render方法
        frame.render(new Circle[]{circle});
        frame.render(new Circle[]{circle});
        frame.render(null);
        frame.render(new Circle[]{});
        // 不应抛出异常
    }

    @Test
    /**
     * testFrameTitle方法。
     */
    public void testFrameTitle() {
        AlgoFrame titledFrame = new AlgoFrame("My Custom Title", 500, 400);
        try {
            assertEquals("My Custom Title", titledFrame.getTitle());
        } finally {
            titledFrame.dispose();
        }
    }

    @Test
    /**
     * testEdgeCasesZeroDimensions方法。
     */
    public void testEdgeCasesZeroDimensions() {
        AlgoFrame zeroFrame = new AlgoFrame("Zero Size", 0, 0);
        try {
            assertEquals(0, zeroFrame.getCanvasWidth());
            assertEquals(0, zeroFrame.getCanvasHeight());
        } finally {
            zeroFrame.dispose();
        }
    }

    @Test
    /**
     * testEdgeCasesNegativeDimensions方法。
     */
    public void testEdgeCasesNegativeDimensions() {
        // 负数尺寸也应该被接受（不抛异常）
        AlgoFrame negFrame = new AlgoFrame("Negative Size", -100, -200);
        try {
            assertEquals(-100, negFrame.getCanvasWidth());
            assertEquals(-200, negFrame.getCanvasHeight());
        } finally {
            negFrame.dispose();
        }
    }

    @Test
    /**
     * testRenderWithFilledCircle方法。
     */
    public void testRenderWithFilledCircle() {
        Circle filledCircle = new Circle(50, 50, 25, 0, 0);
        filledCircle.isFilled = true;
        frame.render(new Circle[]{filledCircle});
        // 不应抛出异常
    }

    @Test
    /**
     * testRenderWithStrokeCircle方法。
     */
    public void testRenderWithStrokeCircle() {
        Circle strokeCircle = new Circle(50, 50, 25, 0, 0);
        strokeCircle.isFilled = false;
        frame.render(new Circle[]{strokeCircle});
        // 不应抛出异常
    }

    @Test
    /**
     * testMultipleFrameInstances方法。
     */
    public void testMultipleFrameInstances() {
        AlgoFrame frame1 = new AlgoFrame("Frame 1", 100, 100);
        AlgoFrame frame2 = new AlgoFrame("Frame 2", 200, 200);
        AlgoFrame frame3 = new AlgoFrame("Frame 3", 300, 300);
        try {
            assertEquals(100, frame1.getCanvasWidth());
            assertEquals(200, frame2.getCanvasWidth());
            assertEquals(300, frame3.getCanvasWidth());
        } finally {
            frame1.dispose();
            frame2.dispose();
            frame3.dispose();
        }
    }
}
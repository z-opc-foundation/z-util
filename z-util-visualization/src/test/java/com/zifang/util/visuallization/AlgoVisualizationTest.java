package com.zifang.util.visuallization;

import com.zifang.util.visuallization.swing.algrithm.lesson2.a.AlgoVisHelper;
import com.zifang.util.visuallization.swing.algrithm.lesson2.a.Circle;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 算法可视化相关类的单元测试
 */
class AlgoVisualizationTest {

    // ==================== Circle 测试 ====================

    @Test
    void testCircleConstructor() {
        Circle circle = new Circle(100, 200, 50, 5, -3);
        assertEquals(100, circle.x);
        assertEquals(200, circle.y);
        assertEquals(50, circle.getR());
        assertEquals(5, circle.vx);
        assertEquals(-3, circle.vy);
        assertFalse(circle.isFilled);
    }

    @Test
    void testCircleMove() {
        Circle circle = new Circle(100, 100, 30, 10, 10);
        int originalX = circle.x;
        int originalY = circle.y;
        circle.move(0, 0, 800, 600);
        assertEquals(originalX + 10, circle.x);
        assertEquals(originalY + 10, circle.y);
    }

    @Test
    void testCircleMoveBoundaryCollision() {
        Circle circle = new Circle(10, 100, 30, -5, 10);
        circle.move(0, 0, 800, 600);
        assertTrue(circle.vx > 0);
    }

    @Test
    void testCircleMoveTopBoundaryCollision() {
        Circle circle = new Circle(100, 10, 30, 10, -5);
        circle.move(0, 0, 800, 600);
        assertTrue(circle.vy > 0);
    }

    @Test
    void testCircleContain() {
        Circle circle = new Circle(100, 100, 50, 0, 0);
        assertTrue(circle.contain(new Point(100, 100)));
        assertTrue(circle.contain(new Point(120, 100)));
        assertFalse(circle.contain(new Point(200, 100)));
    }

    @Test
    void testCircleContainOnBoundary() {
        Circle circle = new Circle(100, 100, 50, 0, 0);
        assertTrue(circle.contain(new Point(150, 100)));
    }

    @Test
    void testCircleFilled() {
        Circle circle = new Circle(0, 0, 10, 0, 0);
        assertFalse(circle.isFilled);
        circle.isFilled = true;
        assertTrue(circle.isFilled);
    }

    // ==================== AlgoVisHelper 测试 ====================

    @Test
    void testAlgoVisHelperStrokeCircle() {
        // 测试绘制方法是否能正常执行而不抛异常
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AlgoVisHelper.strokeCircle(g2d, 50, 50, 30);
        g2d.dispose();
    }

    @Test
    void testAlgoVisHelperFillCircle() {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AlgoVisHelper.fillCircle(g2d, 50, 50, 30);
        g2d.dispose();
    }

    @Test
    void testAlgoVisHelperSetColor() {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AlgoVisHelper.setColor(g2d, Color.RED);
        assertEquals(Color.RED, g2d.getColor());
        g2d.dispose();
    }

    @Test
    void testAlgoVisHelperSetStrokeWidth() {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AlgoVisHelper.setStrokeWidth(g2d, 5);
        g2d.dispose();
    }

    @Test
    void testAlgoVisHelperPause() {
        long start = System.currentTimeMillis();
        AlgoVisHelper.pause(30);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 25);
    }
}
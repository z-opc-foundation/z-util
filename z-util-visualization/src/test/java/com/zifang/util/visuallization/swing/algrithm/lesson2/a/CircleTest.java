package com.zifang.util.visuallization.swing.algrithm.lesson2.a;

import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

/**
 * Circle 单元测试
 * 测试圆形数据类的构造、移动、碰撞检测和包含判断
 */

/**
 * CircleTest类。
 */
public class CircleTest {

    // ==================== 构造函数测试 ====================

    @Test
    /**
     * testConstructorWithAllParameters方法。
     */
    public void testConstructorWithAllParameters() {
        Circle circle = new Circle(100, 200, 50, 3, -2);
        assertEquals(100, circle.x);
        assertEquals(200, circle.y);
        assertEquals(50, circle.getR());
        assertEquals(3, circle.vx);
        assertEquals(-2, circle.vy);
        assertFalse(circle.isFilled);
    }

    @Test
    /**
     * testDefaultIsFilled方法。
     */
    public void testDefaultIsFilled() {
        Circle circle = new Circle(0, 0, 10, 0, 0);
        assertFalse(circle.isFilled);
    }

    @Test
    /**
     * testConstructorWithZeroVelocity方法。
     */
    public void testConstructorWithZeroVelocity() {
        Circle circle = new Circle(100, 100, 30, 0, 0);
        assertEquals(0, circle.vx);
        assertEquals(0, circle.vy);
    }

    @Test
    /**
     * testConstructorWithNegativeVelocity方法。
     */
    public void testConstructorWithNegativeVelocity() {
        Circle circle = new Circle(100, 100, 30, -5, -10);
        assertEquals(-5, circle.vx);
        assertEquals(-10, circle.vy);
    }

    @Test
    /**
     * testConstructorWithZeroRadius方法。
     */
    public void testConstructorWithZeroRadius() {
        Circle circle = new Circle(100, 100, 0, 0, 0);
        assertEquals(0, circle.getR());
    }

    // ==================== getR 测试 ====================

    @Test
    /**
     * testGetR方法。
     */
    public void testGetR() {
        Circle circle = new Circle(0, 0, 25, 0, 0);
        assertEquals(25, circle.getR());
    }

    @Test
    /**
     * testGetRWithLargeRadius方法。
     */
    public void testGetRWithLargeRadius() {
        Circle circle = new Circle(0, 0, 1000, 0, 0);
        assertEquals(1000, circle.getR());
    }

    // ==================== move 测试 ====================

    @Test
    /**
     * testMoveBasic方法。
     */
    public void testMoveBasic() {
        Circle circle = new Circle(100, 100, 10, 5, 3);
        circle.move(0, 0, 200, 200);
        assertEquals(105, circle.x);
        assertEquals(103, circle.y);
    }

    @Test
    /**
     * testMoveWithZeroVelocity方法。
     */
    public void testMoveWithZeroVelocity() {
        Circle circle = new Circle(100, 100, 10, 0, 0);
        circle.move(0, 0, 200, 200);
        assertEquals(100, circle.x);
        assertEquals(100, circle.y);
    }

    @Test
    /**
     * testMoveNegativeVelocity方法。
     */
    public void testMoveNegativeVelocity() {
        Circle circle = new Circle(100, 100, 10, -5, -3);
        circle.move(0, 0, 200, 200);
        assertEquals(95, circle.x);
        assertEquals(97, circle.y);
    }

    @Test
    /**
     * testMoveMultipleTimes方法。
     */
    public void testMoveMultipleTimes() {
        Circle circle = new Circle(50, 50, 10, 5, 5);
        circle.move(0, 0, 200, 200);
        circle.move(0, 0, 200, 200);
        circle.move(0, 0, 200, 200);
        assertEquals(65, circle.x);
        assertEquals(65, circle.y);
    }

    @Test
    /**
     * testMoveCollisionWithLeftBoundary方法。
     */
    public void testMoveCollisionWithLeftBoundary() {
        Circle circle = new Circle(15, 50, 10, -5, 0);
        circle.move(0, 0, 100, 100);
        // x - r < minx 时速度反转
        // 15 - 10 = 5 > 0，所以不会碰撞
        // 但如果x = 5, r = 10, x - r = -5 < 0，会碰撞
    }

    @Test
    /**
     * testMoveCollisionWithRightBoundary方法。
     */
    public void testMoveCollisionWithRightBoundary() {
        Circle circle = new Circle(50, 50, 10, 5, 0);
        int minx = 0, miny = 0, maxx = 100, maxy = 100;
        // 初始: x=50, x+r=60 < maxx=100, 不会碰撞
        circle.move(minx, miny, maxx, maxy);
        // 移动后: x=55, x+r=65 < maxx=100
        assertEquals(55, circle.x);
    }

    @Test
    /**
     * testMoveCollisionTopBoundary方法。
     */
    public void testMoveCollisionTopBoundary() {
        Circle circle = new Circle(50, 50, 10, 0, -5);
        circle.move(0, 0, 100, 100);
        // y - r = 40 > 0，不会碰撞
        assertEquals(45, circle.y);
    }

    @Test
    /**
     * testMoveCollisionBottomBoundary方法。
     */
    public void testMoveCollisionBottomBoundary() {
        Circle circle = new Circle(50, 50, 10, 0, 5);
        circle.move(0, 0, 100, 100);
        // y + r = 60 < 100，不会碰撞
        assertEquals(55, circle.y);
    }

    @Test
    /**
     * testMoveAtBoundaryNoCollision方法。
     */
    public void testMoveAtBoundaryNoCollision() {
        Circle circle = new Circle(50, 50, 10, 0, 0);
        circle.move(0, 0, 100, 100);
        assertEquals(50, circle.x);
        assertEquals(50, circle.y);
    }

    // ==================== contain 测试 ====================

    @Test
    /**
     * testContainPointInside方法。
     */
    public void testContainPointInside() {
        Circle circle = new Circle(100, 100, 50, 0, 0);
        Point p = new Point(100, 100);
        assertTrue(circle.contain(p));
    }

    @Test
    /**
     * testContainPointOnBoundary方法。
     */
    public void testContainPointOnBoundary() {
        Circle circle = new Circle(100, 100, 50, 0, 0);
        // (100, 100)到(150, 100)的距离 = 50，正好等于半径
        Point p = new Point(150, 100);
        assertTrue(circle.contain(p));
    }

    @Test
    /**
     * testContainPointOutside方法。
     */
    public void testContainPointOutside() {
        Circle circle = new Circle(100, 100, 50, 0, 0);
        Point p = new Point(200, 200);
        assertFalse(circle.contain(p));
    }

    @Test
    /**
     * testContainPointOutsideFarAway方法。
     */
    public void testContainPointOutsideFarAway() {
        Circle circle = new Circle(100, 100, 10, 0, 0);
        Point p = new Point(500, 500);
        assertFalse(circle.contain(p));
    }

    @Test
    /**
     * testContainPointInsideDifferentQuadrant方法。
     */
    public void testContainPointInsideDifferentQuadrant() {
        Circle circle = new Circle(100, 100, 50, 0, 0);
        // 左上
        Point p1 = new Point(60, 80);
        assertTrue(circle.contain(p1));
        // 右上
        Point p2 = new Point(140, 80);
        assertTrue(circle.contain(p2));
        // 左下
        Point p3 = new Point(60, 120);
        assertTrue(circle.contain(p3));
        // 右下
        Point p4 = new Point(140, 120);
        assertTrue(circle.contain(p4));
    }

    @Test
    /**
     * testContainPointExactlyAtCenter方法。
     */
    public void testContainPointExactlyAtCenter() {
        Circle circle = new Circle(100, 100, 30, 0, 0);
        Point center = new Point(100, 100);
        assertTrue(circle.contain(center));
    }

    @Test
    /**
     * testContainWithZeroRadiusCircleCenter方法。
     */
    public void testContainWithZeroRadiusCircleCenter() {
        Circle circle = new Circle(100, 100, 0, 0, 0);
        Point p = new Point(100, 100);
        assertTrue(circle.contain(p));
    }

    @Test
    /**
     * testContainWithZeroRadiusCircleOutside方法。
     */
    public void testContainWithZeroRadiusCircleOutside() {
        Circle circle = new Circle(100, 100, 0, 0, 0);
        Point p = new Point(101, 101);
        assertFalse(circle.contain(p));
    }

    @Test
    /**
     * testContainLargeRadius方法。
     */
    public void testContainLargeRadius() {
        Circle circle = new Circle(0, 0, 1000, 0, 0);
        Point p = new Point(500, 500);
        assertTrue(circle.contain(p));
    }

    @Test
    /**
     * testContainSmallRadius方法。
     */
    public void testContainSmallRadius() {
        Circle circle = new Circle(100, 100, 1, 0, 0);
        Point inside = new Point(100, 100);
        Point outside = new Point(102, 102);
        assertTrue(circle.contain(inside));
        assertFalse(circle.contain(outside));
    }

    @Test
    /**
     * testContainNegativeCoordinates方法。
     */
    public void testContainNegativeCoordinates() {
        Circle circle = new Circle(-100, -100, 50, 0, 0);
        Point p = new Point(-100, -100);
        assertTrue(circle.contain(p));
    }

    // ==================== 边界和碰撞组合测试 ====================

    @Test
    /**
     * testMoveAndContainCombination方法。
     */
    public void testMoveAndContainCombination() {
        Circle circle = new Circle(50, 50, 20, 10, 10);
        // 移动后位置 (60, 60)
        circle.move(0, 0, 200, 200);
        // 检查移动后的圆是否包含其圆心
        Point newCenter = new Point(60, 60);
        assertTrue(circle.contain(newCenter));
    }

    @Test
    /**
     * testCollisionBounceOffLeftWall方法。
     */
    public void testCollisionBounceOffLeftWall() {
        // 模拟碰撞左墙
        Circle circle = new Circle(15, 50, 10, -10, 0);
        // 当 x - r < minx 时触发碰撞，速度反转
        // 初始: x=15, r=10, x-r=5 > 0，不会碰撞
        // 移动后: x=5, x-r=-5 < 0，会碰撞并反转速度
        circle.move(0, 0, 100, 100);
        // 下一次move会检测到碰撞
        circle.move(0, 0, 100, 100);
    }

    @Test
    /**
     * testCollisionBounceOffRightWall方法。
     */
    public void testCollisionBounceOffRightWall() {
        Circle circle = new Circle(85, 50, 10, 10, 0);
        // x+r = 95 < 100，不会碰撞
        circle.move(0, 0, 100, 100);
        assertEquals(95, circle.x);
    }

    // ==================== 字段修改测试 ====================

    @Test
    /**
     * testPublicFieldsModification方法。
     */
    public void testPublicFieldsModification() {
        Circle circle = new Circle(100, 100, 50, 5, 5);
        circle.x = 200;
        circle.y = 300;
        circle.vx = -10;
        circle.vy = -20;
        circle.isFilled = true;

        assertEquals(200, circle.x);
        assertEquals(300, circle.y);
        assertEquals(-10, circle.vx);
        assertEquals(-20, circle.vy);
        assertTrue(circle.isFilled);
    }

    @Test
    /**
     * testMoveAfterVelocityChange方法。
     */
    public void testMoveAfterVelocityChange() {
        Circle circle = new Circle(50, 50, 10, 5, 5);
        circle.move(0, 0, 200, 200);
        assertEquals(55, circle.x);

        // 修改速度
        circle.vx = 100;
        circle.vy = 100;

        circle.move(0, 0, 200, 200);
        assertEquals(155, circle.x);
    }

    @Test
    /**
     * testIsFilledToggle方法。
     */
    public void testIsFilledToggle() {
        Circle circle = new Circle(100, 100, 50, 0, 0);
        assertFalse(circle.isFilled);

        circle.isFilled = true;
        assertTrue(circle.isFilled);

        circle.isFilled = false;
        assertFalse(circle.isFilled);
    }

    // ==================== equals 和 hashCode 继承测试 ====================

    @Test
    /**
     * testCircleEqualsSameInstance方法。
     */
    public void testCircleEqualsSameInstance() {
        Circle circle = new Circle(100, 100, 50, 5, 5);
        assertEquals(circle, circle);
    }

    @Test
    /**
     * testCircleEqualsNull方法。
     */
    public void testCircleEqualsNull() {
        Circle circle = new Circle(100, 100, 50, 5, 5);
        assertNotEquals(circle, null);
    }

    @Test
    /**
     * testCircleEqualsDifferentClass方法。
     */
    public void testCircleEqualsDifferentClass() {
        Circle circle = new Circle(100, 100, 50, 5, 5);
        assertNotEquals(circle, "not a circle");
    }
}
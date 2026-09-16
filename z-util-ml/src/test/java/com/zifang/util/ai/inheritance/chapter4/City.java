package com.zifang.util.ml.inheritance.chapter4;

/**
 * A simple abstraction of a city. This class maintains Cartesian coordinates
 * and also knows the Pythagorean theorem.
 *
 * @author zifang
 */

/**
 * City类。
 */
public class City {
    private int x;
    private int y;

    /**
     * Initalize a city
     *
     * @param x X position of city
     * @param y Y position of city
     */
    /**
     * City方法。
     * * @param x int类型参数
     *
     * @param y int类型参数
     */
    public City(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculate distance from another city
     * <p>
     * Pythagorean theorem: a^2 + b^2 = c^2
     *
     * @param city The city to calculate the distance from
     * @return distance The distance from the given city
     */
    /**
     * distanceFrom方法。
     * * @param city City类型参数
     *
     * @return double类型返回值
     */
    public double distanceFrom(City city) {
        // Give difference in x,y
        double deltaXSq = Math.pow((city.getX() - this.getX()), 2);
        double deltaYSq = Math.pow((city.getY() - this.getY()), 2);

        // Calculate shortest path
        double distance = Math.sqrt(Math.abs(deltaXSq + deltaYSq));
        return distance;
    }

    /**
     * Get x position of city
     *
     * @return x X position of city
     */
    /**
     * getX方法。
     *
     * @return int类型返回值
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get y position of city
     *
     * @return y Y position of city
     */
    /**
     * getY方法。
     *
     * @return int类型返回值
     */
    public int getY() {
        return this.y;
    }
}

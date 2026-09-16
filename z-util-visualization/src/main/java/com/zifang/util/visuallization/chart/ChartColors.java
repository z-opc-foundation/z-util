package com.zifang.util.visuallization.chart;

import java.awt.*;

/**
 * 图表颜色定义
 * 提供预设的调色板和颜色获取方法
 */
public class ChartColors {

    /**
     * 预定义的颜色调色板
     */
    public static final Color[] PALETTE = {
            new Color(0x2196F3),  // Blue
            new Color(0x4CAF50),  // Green
            new Color(0xF44336),  // Red
            new Color(0xFF9800),  // Orange
            new Color(0x9C27B0),  // Purple
            new Color(0x00BCD4),  // Cyan
            new Color(0xFFEB3B),  // Yellow
            new Color(0x795548),  // Brown
            new Color(0xE91E63),  // Pink
            new Color(0x607D8B),  // BlueGrey
    };

    private ChartColors() {
    }

    /**
     * 根据索引获取颜色（循环调色板）
     *
     * @param index 颜色索引
     * @return 对应的颜色
     */
    public static Color getColor(int index) {
        return PALETTE[index % PALETTE.length];
    }
}
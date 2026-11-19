package com.zifang.util.monitor.chart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 图表渲染测试。
 */
class TimeSeriesChartTest {

    private static ChartSeries series() {
        return ChartSeries.builder("jvm.memory.heap.used")
                .unit("bytes")
                .point(1000L, 1048576)
                .point(2000L, 2097152)
                .point(3000L, 3145728)
                .build();
    }

    @Test
    /**
     * testRendersSvgStructure方法。
     */
    void testRendersSvgStructure() {
        String svg = TimeSeriesChart.builder()
                .title("堆内存")
                .unit("bytes")
                .range(1000L, 3000L)
                .series(series())
                .build()
                .toSvg();

        assertTrue(svg.startsWith("<svg"));
        assertTrue(svg.contains("</svg>"));
        assertTrue(svg.contains("polyline"));
        assertTrue(svg.contains("堆内存"));
        assertTrue(svg.contains("jvm.memory.heap.used"));
    }

    @Test
    /**
     * testEmptySeriesRendersPlaceholder方法。
     */
    void testEmptySeriesRendersPlaceholder() {
        String svg = TimeSeriesChart.builder()
                .title("空图")
                .series(ChartSeries.builder("nothing").build())
                .build()
                .toSvg();

        assertTrue(svg.contains("无数据"));
    }

    @Test
    /**
     * testEscapesTitle方法。
     */
    void testEscapesTitle() {
        String svg = TimeSeriesChart.builder()
                .title("<script>alert(1)</script>")
                .series(series())
                .build()
                .toSvg();

        assertFalse(svg.contains("<script>"));
        assertTrue(svg.contains("&lt;script&gt;"));
    }

    @Test
    /**
     * testSinglePointRendersCircle方法。
     */
    void testSinglePointRendersCircle() {
        String svg = TimeSeriesChart.builder()
                .range(1000L, 1000L)
                .series(ChartSeries.builder("one").point(1000L, 5).build())
                .build()
                .toSvg();

        assertTrue(svg.contains("circle"));
    }

    @Test
    /**
     * testMultipleSeriesGetDistinctColors方法。
     */
    void testMultipleSeriesGetDistinctColors() {
        String svg = TimeSeriesChart.builder()
                .range(1000L, 3000L)
                .series(series())
                .series(ChartSeries.builder("b").point(1000L, 1).point(3000L, 3).build())
                .build()
                .toSvg();

        assertTrue(svg.contains("#007bff"));
        assertTrue(svg.contains("#28a745"));
    }

    @Test
    /**
     * testSparklineRenders方法。
     */
    void testSparklineRenders() {
        String sparkline = Sparkline.render(series(), 120, 24);
        assertTrue(sparkline.startsWith("<svg"));
        assertTrue(sparkline.contains("polyline"));

        assertTrue(Sparkline.render(ChartSeries.builder("empty").build(), 120, 24).isEmpty());
        assertTrue(Sparkline.render(series(), 0, 24).isEmpty());
    }
}

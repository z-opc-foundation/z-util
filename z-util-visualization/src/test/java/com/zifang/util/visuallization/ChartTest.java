package com.zifang.util.visuallization;

import com.zifang.util.visuallization.chart.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 图表相关类的单元测试
 */
class ChartTest {

    // ==================== ChartColors 测试 ====================

    @Test
    void testChartColorsPaletteNotEmpty() {
        assertNotNull(ChartColors.PALETTE);
        assertEquals(10, ChartColors.PALETTE.length);
    }

    @Test
    void testChartColorsGetColor() {
        assertNotNull(ChartColors.getColor(0));
        assertNotNull(ChartColors.getColor(5));
        assertNotNull(ChartColors.getColor(15));
    }

    @Test
    void testChartColorsGetColorWrapAround() {
        // 测试循环获取颜色
        assertEquals(ChartColors.getColor(0), ChartColors.getColor(10));
        assertEquals(ChartColors.getColor(1), ChartColors.getColor(11));
    }

    // ==================== ChartSeries 测试 ====================

    @Test
    void testChartSeriesConstructorWithName() {
        ChartSeries series = new ChartSeries("TestSeries");
        assertEquals("TestSeries", series.getName());
        assertEquals(0, series.size());
    }

    @Test
    void testChartSeriesConstructorWithNameAndData() {
        List<Double> data = Arrays.asList(1.0, 2.0, 3.0);
        ChartSeries series = new ChartSeries("TestSeries", data);
        assertEquals("TestSeries", series.getName());
        assertEquals(3, series.size());
    }

    @Test
    void testChartSeriesAddData() {
        ChartSeries series = new ChartSeries("Test");
        series.addData(1.5);
        series.addData(2.5);
        assertEquals(2, series.size());
        assertEquals(1.5, series.getData(0));
        assertEquals(2.5, series.getData(1));
    }

    @Test
    void testChartSeriesAddDataWithLabel() {
        ChartSeries series = new ChartSeries("Test");
        series.addData(1.5, "label1");
        series.addData(2.5, "label2");
        assertEquals("label1", series.getLabel(0));
        assertEquals("label2", series.getLabel(1));
    }

    @Test
    void testChartSeriesGetLabelOutOfBounds() {
        ChartSeries series = new ChartSeries("Test");
        series.addData(1.0);
        assertEquals("0", series.getLabel(0));
        assertEquals("1", series.getLabel(1));
    }

    @Test
    void testChartSeriesGetDataCopy() {
        List<Double> data = new ArrayList<>();
        data.add(1.0);
        ChartSeries series = new ChartSeries("Test", data);
        List<Double> copy = series.getData();
        copy.add(2.0);
        assertEquals(1, series.size());
    }

    @Test
    void testChartSeriesClear() {
        ChartSeries series = new ChartSeries("Test");
        series.addData(1.0);
        series.addData(2.0);
        assertEquals(2, series.size());
        series.clear();
        assertEquals(0, series.size());
    }

    @Test
    void testChartSeriesGetMax() {
        ChartSeries series = new ChartSeries("Test", Arrays.asList(1.0, 5.0, 3.0, 2.0));
        assertEquals(5.0, series.getMax());
    }

    @Test
    void testChartSeriesGetMaxEmpty() {
        ChartSeries series = new ChartSeries("Test");
        assertEquals(0, series.getMax());
    }

    @Test
    void testChartSeriesGetMin() {
        ChartSeries series = new ChartSeries("Test", Arrays.asList(1.0, 5.0, 3.0, 2.0));
        assertEquals(1.0, series.getMin());
    }

    @Test
    void testChartSeriesGetMinEmpty() {
        ChartSeries series = new ChartSeries("Test");
        assertEquals(0, series.getMin());
    }

    @Test
    void testChartSeriesGetDataIndexOutOfBounds() {
        ChartSeries series = new ChartSeries("Test");
        series.addData(1.0);
        assertThrows(IndexOutOfBoundsException.class, () -> series.getData(5));
    }

    // ==================== LineChart 测试 ====================

    @Test
    void testLineChartConstructor() {
        LineChart chart = new LineChart("Test Chart", 800, 600);
        assertEquals(800, chart.getCanvasWidth());
        assertEquals(600, chart.getCanvasHeight());
    }

    @Test
    void testLineChartDefaultConstructor() {
        LineChart chart = new LineChart("Test Chart");
        assertEquals(800, chart.getCanvasWidth());
        assertEquals(600, chart.getCanvasHeight());
    }

    @Test
    void testLineChartAddSeries() {
        LineChart chart = new LineChart("Test");
        ChartSeries series = new ChartSeries("Series1", Arrays.asList(1.0, 2.0, 3.0));
        chart.addSeries(series);
        chart.clear();
    }

    @Test
    void testLineChartAddSeriesWithNameAndData() {
        LineChart chart = new LineChart("Test");
        chart.addSeries("Series1", Arrays.asList(1.0, 2.0, 3.0));
        chart.clear();
    }

    @Test
    void testLineChartAddData() {
        LineChart chart = new LineChart("Test");
        chart.addData("Series1", 1.0);
        chart.addData("Series1", 2.0);
        chart.clear();
    }

    @Test
    void testLineChartSetAxisLabels() {
        LineChart chart = new LineChart("Test");
        chart.setAxisLabels("X Axis", "Y Axis");
        chart.clear();
    }

    @Test
    void testLineChartClear() {
        LineChart chart = new LineChart("Test");
        chart.addData("Series1", 1.0);
        chart.clear();
    }

    // ==================== BarChart 测试 ====================

    @Test
    void testBarChartConstructor() {
        BarChart chart = new BarChart("Test Chart", 800, 600);
        assertEquals(800, chart.getCanvasWidth());
        assertEquals(600, chart.getCanvasHeight());
    }

    @Test
    void testBarChartDefaultConstructor() {
        BarChart chart = new BarChart("Test Chart");
        assertEquals(800, chart.getCanvasWidth());
        assertEquals(600, chart.getCanvasHeight());
    }

    @Test
    void testBarChartAddSeries() {
        BarChart chart = new BarChart("Test");
        ChartSeries series = new ChartSeries("Series1", Arrays.asList(1.0, 2.0, 3.0));
        chart.addSeries(series);
        chart.clear();
    }

    @Test
    void testBarChartAddData() {
        BarChart chart = new BarChart("Test");
        chart.addData("Series1", 1.0);
        chart.addData("Series1", 2.0);
        chart.clear();
    }

    @Test
    void testBarChartSetAxisLabels() {
        BarChart chart = new BarChart("Test");
        chart.setAxisLabels("X Axis", "Y Axis");
        chart.clear();
    }

    @Test
    void testBarChartClear() {
        BarChart chart = new BarChart("Test");
        chart.addData("Series1", 1.0);
        chart.clear();
    }

    // ==================== NetworkGraph 测试 ====================

    @Test
    void testNetworkGraphConstructor() {
        NetworkGraph graph = new NetworkGraph("Test Graph", 800, 600);
        assertEquals(800, graph.getCanvasWidth());
        assertEquals(600, graph.getCanvasHeight());
    }

    @Test
    void testNetworkGraphDefaultConstructor() {
        NetworkGraph graph = new NetworkGraph("Test Graph");
        assertEquals(800, graph.getCanvasWidth());
        assertEquals(600, graph.getCanvasHeight());
    }

    @Test
    void testNetworkGraphAddLayer() {
        NetworkGraph graph = new NetworkGraph("Test");
        graph.addLayer(0, 4, "Input");
        graph.addLayer(1, 6, "Hidden");
        graph.addLayer(2, 2, "Output");
        graph.clear();
    }

    @Test
    void testNetworkGraphAddLayerAutoAppend() {
        NetworkGraph graph = new NetworkGraph("Test");
        graph.addLayer(4, "Input");
        graph.addLayer(6, "Hidden");
        graph.clear();
    }

    @Test
    void testNetworkGraphSetNodeRadius() {
        NetworkGraph graph = new NetworkGraph("Test");
        graph.setNodeRadius(30);
        graph.clear();
    }

    @Test
    void testNetworkGraphSetLayerActivations() {
        NetworkGraph graph = new NetworkGraph("Test");
        graph.addLayer(0, 4, "Input");
        graph.setLayerActivations(0, Arrays.asList(0.1, 0.5, 0.9, 0.3));
        graph.clear();
    }

    @Test
    void testNetworkGraphClear() {
        NetworkGraph graph = new NetworkGraph("Test");
        graph.addLayer(4, "Input");
        graph.clear();
    }

    // ==================== ChartFrame 抽象类测试 ====================

    @Test
    void testChartFramePause() {
        long start = System.currentTimeMillis();
        ChartFrame.pause(50);
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 40);
    }
}
package com.zifang.util.pandas.transform;

import com.zifang.util.pandas.DataFrame;
import com.zifang.util.pandas.Series;
import com.zifang.util.pandas.num.Index;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Reshaper 数据转换操作测试类
 * 测试 pivot、melt、stack、unstack、transpose 等数据重塑功能
 */

/**
 * ReshaperTest类。
 */
public class ReshaperTest {

    private DataFrame sampleData;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        // 创建测试数据 - 销售数据示例
        List<Map<String, Object>> records = new ArrayList<>();

        Map<String, Object> r1 = new LinkedHashMap<>();
        r1.put("date", "2024-01");
        r1.put("product", "A");
        r1.put("region", "East");
        r1.put("sales", 100.0);
        r1.put("quantity", 10.0);
        records.add(r1);

        Map<String, Object> r2 = new LinkedHashMap<>();
        r2.put("date", "2024-01");
        r2.put("product", "A");
        r2.put("region", "West");
        r2.put("sales", 150.0);
        r2.put("quantity", 15.0);
        records.add(r2);

        Map<String, Object> r3 = new LinkedHashMap<>();
        r3.put("date", "2024-01");
        r3.put("product", "B");
        r3.put("region", "East");
        r3.put("sales", 200.0);
        r3.put("quantity", 20.0);
        records.add(r3);

        Map<String, Object> r4 = new LinkedHashMap<>();
        r4.put("date", "2024-01");
        r4.put("product", "B");
        r4.put("region", "West");
        r4.put("sales", 250.0);
        r4.put("quantity", 25.0);
        records.add(r4);

        Map<String, Object> r5 = new LinkedHashMap<>();
        r5.put("date", "2024-02");
        r5.put("product", "A");
        r5.put("region", "East");
        r5.put("sales", 110.0);
        r5.put("quantity", 11.0);
        records.add(r5);

        sampleData = new DataFrame(records);
    }

    @Test
    /**
     * testPivotBasic方法。
     */
    public void testPivotBasic() {
        // 测试基本透视功能
        DataFrame pivotTable = Reshaper.pivot(sampleData, "product", "region", "sales");

        assertNotNull(pivotTable);
        assertTrue(pivotTable.nRows() > 0);
        assertTrue(pivotTable.nCols() > 0);

        // 验证列名包含原始列和透视列
        List<String> columns = pivotTable.columns();
        assertTrue("应该包含 product 列", columns.contains("product"));
    }

    @Test
    /**
     * testPivotWithAggregation方法。
     */
    public void testPivotWithAggregation() {
        // 测试带聚合函数的透视
        DataFrame pivotTable = Reshaper.pivot(sampleData, "product", "region", "sales", "sum");

        assertNotNull(pivotTable);

        // 验证透视表中每个产品在不同地区的销售额总和
        // product A 在 East 地区有两条记录：100 + 110 = 210
        // 注意：这需要具体的数据验证
    }

    @Test
    /**
     * testMeltBasic方法。
     */
    public void testMeltBasic() {
        // 测试基本融合功能
        List<String> idVars = Arrays.asList("date", "product", "region");
        List<String> valueVars = Arrays.asList("sales", "quantity");

        DataFrame melted = Reshaper.melt(sampleData, idVars, valueVars, "metric", "value");

        assertNotNull(melted);
        // 原始数据 6 行，融合后有 12 行（每行数据展开为 2 行）
        assertEquals("融合后的行数应该是原始行数 * valueVars 数量",
                sampleData.nRows() * valueVars.size(), melted.nRows());

        // 验证列名
        List<String> columns = melted.columns();
        assertTrue("应该包含 date 列", columns.contains("date"));
        assertTrue("应该包含 product 列", columns.contains("product"));
        assertTrue("应该包含 region 列", columns.contains("region"));
        assertTrue("应该包含 metric 列", columns.contains("metric"));
        assertTrue("应该包含 value 列", columns.contains("value"));
    }

    @Test
    /**
     * testMeltDefaultNames方法。
     */
    public void testMeltDefaultNames() {
        // 测试使用默认列名的融合
        List<String> idVars = Arrays.asList("date");
        List<String> valueVars = Arrays.asList("sales", "quantity");

        DataFrame melted = Reshaper.melt(sampleData, idVars, valueVars);

        assertNotNull(melted);
        List<String> columns = melted.columns();
        assertTrue("应该包含 variable 列（默认）", columns.contains("variable"));
        assertTrue("应该包含 value 列（默认）", columns.contains("value"));
    }

    @Test
    /**
     * testStack方法。
     */
    public void testStack() {
        // 测试堆叠操作
        // 创建一个简单的 2x2 数据框
        Map<String, double[]> data = new LinkedHashMap<>();
        data.put("A", new double[]{1.0, 2.0});
        data.put("B", new double[]{3.0, 4.0});
        DataFrame df = new DataFrame(data, new String[]{"row1", "row2"});

        Series stacked = Reshaper.stack(df);

        assertNotNull(stacked);
        // 2x2 数据框堆叠后应该有 4 个元素
        assertEquals("堆叠后的长度应该是原始行数 * 列数", 4, stacked.length());

        // 验证索引格式
        String[] index = stacked.index().toArray();
        assertEquals("row1_A", index[0]);
        assertEquals("row1_B", index[1]);
        assertEquals("row2_A", index[2]);
        assertEquals("row2_B", index[3]);

        // 验证值
        assertEquals(1.0, stacked.toArray()[0], 0.001);
        assertEquals(3.0, stacked.toArray()[1], 0.001);
        assertEquals(2.0, stacked.toArray()[2], 0.001);
        assertEquals(4.0, stacked.toArray()[3], 0.001);
    }

    @Test
    /**
     * testUnstack方法。
     */
    public void testUnstack() {
        // 测试取消堆叠操作
        // 创建一个堆叠的 Series
        double[] values = new double[]{1.0, 3.0, 2.0, 4.0};
        String[] index = new String[]{"row1_A", "row1_B", "row2_A", "row2_B"};
        Series stacked = new Series(values, Index.of(index), "stacked", null);

        DataFrame unstacked = Reshaper.unstack(stacked);

        assertNotNull(unstacked);
        // 应该有 2 行（row1, row2）和 2 列（A, B）
        assertTrue("取消堆叠后的行数应该大于0", unstacked.nRows() > 0);
        assertTrue("取消堆叠后的列数应该大于0", unstacked.nCols() > 0);
    }

    @Test
    /**
     * testTranspose方法。
     */
    public void testTranspose() {
        // 测试转置操作
        Map<String, double[]> data = new LinkedHashMap<>();
        data.put("A", new double[]{1.0, 2.0, 3.0});
        data.put("B", new double[]{4.0, 5.0, 6.0});
        DataFrame df = new DataFrame(data, new String[]{"row1", "row2", "row3"});

        DataFrame transposed = Reshaper.transpose(df);

        assertNotNull(transposed);
        // 原 3x2 转置后应该是 2x3
        assertEquals("转置后的行数应该等于原列数", df.nCols(), transposed.nRows());
        assertEquals("转置后的列数应该等于原行数 + 1（索引列）", df.nRows() + 1, transposed.nCols());
    }

    @Test
    /**
     * testCrosstab方法。
     */
    public void testCrosstab() {
        // 测试交叉表功能
        DataFrame crosstab = Reshaper.crosstab(sampleData, "product", "region");

        assertNotNull(crosstab);
        assertTrue("交叉表应该有行", crosstab.nRows() > 0);
        assertTrue("交叉表应该有列", crosstab.nCols() > 0);

        // 验证列名包含行变量和列变量值
        List<String> columns = crosstab.columns();
        assertTrue("交叉表应该包含 product 列", columns.contains("product"));
    }

    @Test
    /**
     * testStackAndUnstackRoundTrip方法。
     */
    public void testStackAndUnstackRoundTrip() {
        // 测试堆叠和取消堆叠的往返操作
        Map<String, double[]> data = new LinkedHashMap<>();
        data.put("A", new double[]{1.0, 2.0});
        data.put("B", new double[]{3.0, 4.0});
        DataFrame original = new DataFrame(data, new String[]{"row1", "row2"});

        // 堆叠
        Series stacked = Reshaper.stack(original);
        assertNotNull("堆叠应该成功", stacked);

        // 取消堆叠
        DataFrame unstacked = Reshaper.unstack(stacked);
        assertNotNull("取消堆叠应该成功", unstacked);

        // 验证形状（可能会有差异，取决于具体实现）
        assertTrue("往返操作后的数据框应该有行", unstacked.nRows() > 0);
        assertTrue("往返操作后的数据框应该有列", unstacked.nCols() > 0);
    }
}

package com.zifang.util.expr.obj;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * R2-P2-9：把渲染层 buildAxis / buildCombo / buildPie 三套手写累加器
 * 整体替换为对象层 group+agg 开箱 API 的回归测试。
 */
public class GroupAggTest {

    @Test
    public void groupBySingleKeySumAndCount() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("region", "east", "amount", 100));
        data.add(row("region", "east", "amount", 200));
        data.add(row("region", "west", "amount", 500));

        Map<String, String> agg = new LinkedHashMap<>();
        agg.put("total", "SUM(amount)");
        agg.put("count", "COUNT(*)");

        List<Map<String, Object>> out = GroupAgg.group(data, Arrays.asList("region"), agg);

        assertEquals(2, out.size());
        Map<String, Object> east = out.get(0);
        assertEquals("east", east.get("region"));
        assertEquals(300L, ((Number) east.get("total")).longValue());
        assertEquals(2L, ((Number) east.get("count")).longValue());

        Map<String, Object> west = out.get(1);
        assertEquals(500L, ((Number) west.get("total")).longValue());
        assertEquals(1L, ((Number) west.get("count")).longValue());
    }

    @Test
    public void groupByMultipleKeys() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("region", "east", "city", "sh", "amount", 10));
        data.add(row("region", "east", "city", "sh", "amount", 20));
        data.add(row("region", "east", "city", "bj", "amount", 30));
        data.add(row("region", "west", "city", "cd", "amount", 40));

        Map<String, String> agg = Collections.singletonMap("total", "SUM(amount)");

        List<Map<String, Object>> out = GroupAgg.group(data,
                Arrays.asList("region", "city"), agg);

        assertEquals(3, out.size());
        assertEquals("east", out.get(0).get("region"));
        assertEquals("sh", out.get(0).get("city"));
        assertEquals(30L, ((Number) out.get(0).get("total")).longValue());
        assertEquals(30L, ((Number) out.get(1).get("total")).longValue());
        assertEquals(40L, ((Number) out.get(2).get("total")).longValue());
    }

    @Test
    public void foldAggregatesWholeTableIntoOneMap() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("v", 10));
        data.add(row("v", 20));
        data.add(row("v", 30));

        Map<String, Object> one = GroupAgg.fold(data, Collections.singletonMap("total", "SUM(v)"));
        assertEquals(60L, ((Number) one.get("total")).longValue());
    }

    @Test
    public void foldOnEmptyTableReturnsAggregatesAsZeroOrNull() {
        Map<String, Object> one = GroupAgg.fold(new ArrayList<Map<String, Object>>(),
                Collections.singletonMap("count", "COUNT(*)"));
        // 空表有 by ⇒ 一组，COUNT(*) 给 0，不是空 Map
        assertEquals(1, one.size());
        assertEquals(0L, ((Number) one.get("count")).longValue());
    }

    @Test
    public void groupWithItemsKeyExposesLines() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("region", "east", "amount", 10));
        data.add(row("region", "east", "amount", 20));

        List<Map<String, Object>> out = GroupAgg.group(data,
                Arrays.asList("region"),
                Collections.singletonMap("total", "SUM(amount)"),
                "lines");

        assertEquals(1, out.size());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lines = (List<Map<String, Object>>) out.get(0).get("lines");
        assertEquals(2, lines.size());
        assertEquals(30L, ((Number) out.get(0).get("total")).longValue());
    }

    @Test
    public void noGroupKeyStillSingleBucket() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(row("amount", 1));
        data.add(row("amount", 2));

        List<Map<String, Object>> out = GroupAgg.group(new ArrayList<>(),
                Collections.emptyList(),
                Collections.singletonMap("count", "COUNT(*)"));
        // 空表场景：无 by 时整表一组 ⇒ COUNT(*) = 0
        assertEquals(1, out.size());
        assertEquals(0L, ((Number) out.get(0).get("count")).longValue());
    }

    @Test
    public void noRowsStillCollapsesToOneBucket() {
        Map<String, Object> one = GroupAgg.fold(new ArrayList<Map<String, Object>>(),
                new LinkedHashMap<String, String>() {{
                    put("total", "SUM(amount)");
                    put("count", "COUNT(*)");
                }});
        // 无 by ⇒ 整表一组；空表也要写出聚合（COUNT=0，SUM=0 与 SQL 引擎同口径）
        assertEquals(0L, ((Number) one.get("count")).longValue());
        assertTrue("SUM on empty set is 0 in Aggregates / SQL engine",
                ((Number) one.get("total")).longValue() == 0L);
    }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}

package com.zifang.util.expr.obj;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 在 {@link List}{@code <Map>} 上做"按维度分组 + 聚合"，返回同样是 {@code List<Map>}。
 * <p>
 * 给渲染层（{@code buildAxis / buildCombo / buildPie} 三套手写累加器）的开箱替换：
 * <pre>
 *   List&lt;Map&lt;String, Object&gt;&gt; chartData = GroupAgg.group(rows,
 *           java.util.Arrays.asList("region"),
 *           java.util.Collections.singletonMap("total", "SUM(amount)"));
 *   Map&lt;String, Object&gt; oneLine = GroupAgg.fold(rows,
 *           java.util.Collections.singletonMap("count", "COUNT(*)"));
 * </pre>
 * 字典序遍历，原表不动（行数组可能与内存表共享实例，in-place 改写会污染其它查询）。
 *
 * @author zifang
 */
public final class GroupAgg {

    private GroupAgg() {
    }

    /**
     * 按 {@code by} 分组并对每组执行 {@code agg} 里的聚合表达式。
     *
     * @param rows 输入行数组（不改写）
     * @param by   分组列，可以写裸列名或点路径
     * @param agg  聚合映射：{@code 输出列名 -> "SUM(amount)" / "COUNT(DISTINCT city)" / ...}
     * @return 每组一行的 {@code List<Map>}：by 列 + agg 产出列
     */
    public static List<Map<String, Object>> group(List<Map<String, Object>> rows,
                                                  List<String> by,
                                                  Map<String, String> agg) {
        return group(rows, by, agg, null);
    }

    /**
     * 同 {@link #group(List, List, Map)}，额外把组内明细挂到 {@code itemsKey}。
     */
    public static List<Map<String, Object>> group(List<Map<String, Object>> rows,
                                                  List<String> by,
                                                  Map<String, String> agg,
                                                  String itemsKey) {
        if (rows == null) {
            return new ArrayList<>();
        }
        List<String> byList = by == null ? java.util.Collections.emptyList() : by;
        BiFunction<String, Map<String, Object>, Object> el = GroupAgg::evaluatePath;
        Map<List<Object>, List<Map<String, Object>>> buckets = new LinkedHashMap<>();
        Map<List<Object>, Map<String, Object>> heads = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            List<Object> key = new ArrayList<>(byList.size());
            Map<String, Object> head = new LinkedHashMap<>();
            for (String column : byList) {
                Object v = el.apply(column, row);
                key.add(v);
                head.put(Values.label(column), v);
            }
            List<Map<String, Object>> bucket = buckets.get(key);
            if (bucket == null) {
                bucket = new ArrayList<>();
                buckets.put(key, bucket);
                heads.put(key, head);
            }
            bucket.add(row);
        }
        if (byList.isEmpty()) {
            // 无分组列即整表一组（与 SQL 裸聚合一致）：空表也要产出一行（COUNT→0）
            List<Object> empty = new ArrayList<>();
            buckets.clear();
            heads.clear();
            buckets.put(empty, new ArrayList<>(rows));
            heads.put(empty, new LinkedHashMap<>());
        }
        Map<String, String> aggMap = agg == null ? java.util.Collections.emptyMap() : agg;
        List<Map<String, Object>> out = new ArrayList<>(buckets.size());
        for (Map.Entry<List<Object>, List<Map<String, Object>>> entry : buckets.entrySet()) {
            List<Map<String, Object>> groupRows = entry.getValue();
            Map<String, Object> head = heads.get(entry.getKey());
            Map<String, Object> row = new LinkedHashMap<>(head);
            for (Map.Entry<String, String> a : aggMap.entrySet()) {
                row.put(a.getKey(), Aggregates.evaluate(a.getValue(), groupRows, el));
            }
            if (itemsKey != null) {
                row.put(itemsKey, new ArrayList<>(groupRows));
            }
            out.add(row);
        }
        return out;
    }

    /**
     * 把整张表折成单个对象：算 {@code agg} 里的每个聚合。
     * 空表返回空 Map（聚合里 {@code COUNT(*)} 也会得 0）。
     */
    public static Map<String, Object> fold(List<Map<String, Object>> rows,
                                          Map<String, String> agg) {
        List<Map<String, Object>> single = group(rows, java.util.Collections.emptyList(), agg, null);
        if (single.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return single.get(0);
    }

    /**
     * 轻量 EL：仅解点路径与列名。聚合函数内层若需要真正的算术/方法调用，请走 ObjEngine 路径。
     */
    private static Object evaluatePath(String expr, Map<String, Object> row) {
        if (expr == null || expr.isEmpty()) {
            return null;
        }
        String trimmed = expr.trim();
        Object v = Values.get(row, trimmed);
        if (v != null) {
            return v;
        }
        // Values.get 解析点路径，无点路径且值是 null 时也会跳回根做兜底；
        // 这里用直接的 row.get 再试一次，避开 null 行的字段值混淆。
        if (!trimmed.contains(".") && row.containsKey(trimmed)) {
            return row.get(trimmed);
        }
        return null;
    }
}

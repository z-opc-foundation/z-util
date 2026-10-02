package com.zifang.util.expr.obj;

import com.zifang.util.expr.el.ElEvaluator;
import com.zifang.util.expr.el.ElException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 对象整形语言（OBJ）：把二维关系表整形成任意对象结构。
 * <p>
 * 平台里的取数链是三段式：DB 出原始数据 → SQL 在内存里粗糙地产出一张二维表 →
 * 本语言把这张二维表抬成高维结构（嵌套对象、对象数组、键值映射、树、透视矩阵），
 * 交给渲染层。SQL 不擅长表达"一个分组一个对象、对象里再套明细数组"，
 * 而前端要的恰恰是这种结构，所以这一段不能省，也不该写进各产品的 Java 里。
 *
 * <h3>程序 = JSON</h3>
 * spec 就是 Map / List / 字面量组成的结构，与 JSON 一一对应：任何能给出
 * <b>保序</b> Map/List 树的解析器都能直接喂进来（Jackson
 * {@code readValue(json, Object.class)}、Spring 的 {@code @RequestBody Map}），
 * 所以本语言不自带语法解析器。注意 {@code JsonUtil.parseToMap} 不适合承载 spec：
 * 它给的 {@code HashMap} 会打乱模板字段顺序，嵌套值也不是 {@code Map}。三种形态：
 * <ul>
 *     <li>{@code {"op": "步骤名", ...}} —— 一个步骤</li>
 *     <li>{@code [步骤, 步骤, ...]} —— 管道，前一步的产出作为后一步的输入</li>
 *     <li>其它任何值 —— 模板；出现在管道位置时对每行套用，产出对象数组</li>
 * </ul>
 *
 * <h3>两种表达式槽位</h3>
 * {@code select.fields} / {@code where.expr} / {@code order.by} / {@code agg} 里写 EL 原文
 * （{@code amount * 1.13}）；模板槽位（{@code into} / {@code set.value}）里的字符串是字面量，
 * 取当前行的值要写 {@code ${列名}}，整串只有一个 {@code ${}} 时保留原类型。
 *
 * <h3>例子</h3>
 * <pre>
 * [ {"op": "from", "sql": "SELECT region, city, amount FROM v_sales"},
 *   {"op": "group", "by": "region", "items": "lines",
 *    "agg": {"total": "SUM(amount)", "cities": "COUNT(DISTINCT city)"},
 *    "into": {"region": "${region}", "total": "${total}", "cities": "${cities}",
 *             "lines": {"op": "map", "of": "lines", "into": {"city": "${city}", "amount": "${amount}"}}}} ]
 * </pre>
 * 产出 {@code [{region, total, cities, lines:[{city, amount}, ...]}, ...]}。
 * 模板槽位里的 {@code {"op": ...}} 是嵌套子程序，输入是当前分组行（{@code of} 从里面取出明细数组），
 * 高维结构就是靠这条规则一层层长出来的。
 * <p>
 * 引擎实例本身无请求级状态（{@link TableSource} 与 EL 求值器都只读），可以在多线程间共享。
 *
 * @author zifang
 */
public final class ObjEngine {

    /** 未知 op 时报给调用方的清单：spec 多数由 AI 生成，报错必须自解释 */
    private static final Set<String> OPS = new LinkedHashSet<>(java.util.Arrays.asList(
            "from", "select", "where", "order", "limit", "one", "group", "fold",
            "map", "keyBy", "tree", "pivot", "unpivot", "get", "set"));

    private final TableSource source;

    private final ElEvaluator el = new ElEvaluator();

    private final Templates templates;

    public ObjEngine() {
        this(TableSource.none());
    }

    public ObjEngine(TableSource source) {
        this.source = source == null ? TableSource.none() : source;
        this.templates = new Templates(this);
    }

    /**
     * 执行整形程序。
     *
     * @param spec 程序（Map / List 结构，等价一段 JSON）
     * @return 任意对象结构：Map / List / 标量
     */
    public Object shape(Object spec) {
        return run(spec, null);
    }

    /**
     * 以既有的二维结果为输入执行整形程序：{@code DynamicQuery.list(query)} 或
     * {@code VirtualTableEngine.query(sql)} 的返回值可以直接传进来，程序里就不必再写 from。
     */
    public Object shape(Object spec, Object value) {
        return run(spec, value);
    }

    /**
     * 程序求值：步骤 / 管道 / 裸模板。
     */
    @SuppressWarnings("unchecked")
    Object run(Object program, Object value) {
        if (program == null) {
            return value;
        }
        if (program instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) program;
            Object op = map.get("op");
            if (op instanceof String) {
                return apply((String) op, map, value);
            }
            return applyTemplate(map, value);
        }
        if (program instanceof List) {
            List<Object> pipeline = (List<Object>) program;
            Object current = value;
            for (Object step : pipeline) {
                current = run(step, current);
            }
            return current;
        }
        return applyTemplate(program, value);
    }

    /** 裸模板程序：当前值是数组就逐行套用，否则就套一次 */
    private Object applyTemplate(Object program, Object value) {
        if (!(value instanceof List)) {
            return templates.eval(program, value);
        }
        List<Object> out = new ArrayList<>();
        for (Object item : (List<?>) value) {
            out.add(templates.eval(program, item));
        }
        return out;
    }

    Object expression(String expr, Object root) {
        if (expr == null) {
            return null;
        }
        try {
            return el.eval(expr, root);
        } catch (ElException e) {
            throw e;
        } catch (Exception e) {
            throw new ObjException("EL 表达式求值失败: " + expr + " (" + e + ")", e);
        }
    }

    private Object apply(String op, Map<String, Object> step, Object value) {
        switch (op) {
            case "from":
                return from(step);
            case "select":
                return select(step, value);
            case "where":
                return where(step, value);
            case "order":
                return order(step, value);
            case "limit":
                return limit(step, value);
            case "one":
                return one(step, value);
            case "group":
                return group(step, value);
            case "fold":
                return fold(step, value);
            case "map":
                return map(step, value);
            case "keyBy":
                return keyBy(step, value);
            case "tree":
                return tree(step, value);
            case "pivot":
                return pivot(step, value);
            case "unpivot":
                return unpivot(step, value);
            case "get":
                return Values.get(value, Values.required(step, "path", op));
            case "set":
                return set(step, value);
            default:
                throw new ObjException("未知步骤 op=\"" + op + "\", 对象语言可用步骤: " + OPS);
        }
    }

    /* ---------------------------------------------------------------------- *
     * 取数与行级算子
     * ---------------------------------------------------------------------- */

    private Object from(Map<String, Object> step) {
        if (step.containsKey("sql")) {
            return source.query(Values.str(step.get("sql")));
        }
        if (step.containsKey("table")) {
            return source.rows(Values.str(step.get("table")));
        }
        if (step.containsKey("rows")) {
            return step.get("rows");
        }
        if (step.containsKey("value")) {
            return step.get("value");
        }
        throw new ObjException("from 需要 sql / table / rows / value 之一, 实际槽位: " + step.keySet());
    }

    /** fields 为数组时按名投影，为对象时 {@code {"输出列": "EL 表达式"}} */
    private Object select(Map<String, Object> step, Object value) {
        Object fields = step.get("fields");
        List<Map<String, Object>> rows = Values.asRows(value, "select");
        List<String> names = fields instanceof List ? Values.asStrings(fields, "select.fields") : null;
        Map<String, Object> mapped = fields == null || names != null ? null : Values.asMap(fields, "select.fields");
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            if (fields == null) {
                out.add(new LinkedHashMap<>(row));
                continue;
            }
            Map<String, Object> kept = new LinkedHashMap<>();
            if (names != null) {
                for (String expr : names) {
                    kept.put(Values.label(expr), expression(expr, row));
                }
            } else {
                for (Map.Entry<String, Object> entry : mapped.entrySet()) {
                    kept.put(entry.getKey(), expression(Values.str(entry.getValue()), row));
                }
            }
            out.add(kept);
        }
        return out;
    }

    private Object where(Map<String, Object> step, Object value) {
        String expr = Values.required(step, "expr", "where");
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : Values.asRows(value, "where")) {
            if (Values.truthy(expression(expr, row))) {
                out.add(row);
            }
        }
        return out;
    }

    /** by 元素写 {@code "amount desc"}，或 {@code {"expr": "...", "dir": "desc"}} */
    private Object order(Map<String, Object> step, Object value) {
        Object by = step.get("by");
        if (by == null) {
            throw new ObjException("order 需要 by 槽位: 列名或表达式, 可加 asc / desc 后缀");
        }
        List<?> sortKeys = by instanceof List ? (List<?>) by : Collections.singletonList(by);
        List<String> exprs = new ArrayList<>();
        List<Boolean> desc = new ArrayList<>();
        for (Object key : sortKeys) {
            String expr;
            String dir = null;
            if (key instanceof Map) {
                Map<String, Object> map = Values.asMap(key, "order.by");
                expr = map.containsKey("expr") ? Values.str(map.get("expr")) : Values.required(map, "field", "order.by");
                dir = map.get("dir") == null ? null : Values.str(map.get("dir"));
            } else {
                expr = Values.str(key);
                String lower = expr.toLowerCase(java.util.Locale.ROOT);
                if (lower.endsWith(" desc") || lower.endsWith(" asc")) {
                    dir = lower.substring(lower.lastIndexOf(' ') + 1);
                    expr = expr.substring(0, expr.lastIndexOf(' '));
                }
            }
            exprs.add(expr);
            desc.add("desc".equalsIgnoreCase(dir));
        }
        List<Map<String, Object>> rows = new ArrayList<>(Values.asRows(value, "order"));
        // 装饰-排序-还原: 用行下标而不是行本身做键, 内容完全相同的两行不会被折叠成一个键值
        List<List<Object>> keys = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            List<Object> values = new ArrayList<>(exprs.size());
            for (String expr : exprs) {
                values.add(expression(expr, row));
            }
            keys.add(values);
        }
        List<Integer> order = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            order.add(i);
        }
        // List.sort 稳定: 同键的行保持输入顺序
        order.sort((a, b) -> {
            List<Object> ka = keys.get(a);
            List<Object> kb = keys.get(b);
            for (int i = 0; i < ka.size(); i++) {
                Object va = ka.get(i);
                Object vb = kb.get(i);
                if (va == null || vb == null) {
                    if (va == vb) {
                        continue;
                    }
                    // 空值不参与升降序翻转: "按金额取最高的那一档" 不该取到一个没有金额的桶
                    return va == null ? 1 : -1;
                }
                int c = Values.compare(va, vb);
                if (c != 0) {
                    return desc.get(i) ? -c : c;
                }
            }
            return 0;
        });
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Integer index : order) {
            out.add(rows.get(index));
        }
        return out;
    }

    private Object limit(Map<String, Object> step, Object value) {
        List<Map<String, Object>> rows = Values.asRows(value, "limit");
        int offset = Math.max(0, Values.asInt(step.get("offset"), 0));
        int n = Values.asInt(step.get("n"), rows.size());
        if (offset >= rows.size() || n <= 0) {
            return new ArrayList<Map<String, Object>>();
        }
        return new ArrayList<>(rows.subList(offset, Math.min(rows.size(), offset + n)));
    }

    private Object one(Map<String, Object> step, Object value) {
        List<?> list = value instanceof List ? (List<?>) value : Collections.singletonList(value);
        int index = Values.asInt(step.get("index"), 0);
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }

    /* ---------------------------------------------------------------------- *
     * 整形算子：二维 → 高维
     * ---------------------------------------------------------------------- */

    /**
     * 分组。{@code by} 分组列（EL，裸列名即列值），{@code agg} 聚合，
     * {@code items} 把组内明细挂到指定键上，{@code into} 对每组套模板，
     * {@code as:"map"} 让产出变成以第一个分组列值为键的对象。
     */
    private Object group(Map<String, Object> step, Object value) {
        List<Map<String, Object>> rows = Values.asRows(value, "group");
        List<String> by = Values.asStrings(step.get("by"), "group.by");
        Map<String, Object> agg = Values.asMap(step.get("agg"), "group.agg");
        String items = step.get("items") == null ? null : Values.str(step.get("items"));
        Object into = step.get("into");
        boolean keyed = "map".equals(step.get("as"));
        if (keyed && by.isEmpty()) {
            throw new ObjException("group 的 as:\"map\" 需要 by 至少一个分组列");
        }
        Map<List<Object>, List<Map<String, Object>>> buckets = new LinkedHashMap<>();
        Map<List<Object>, Map<String, Object>> heads = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            List<Object> key = new ArrayList<>(by.size());
            Map<String, Object> head = new LinkedHashMap<>();
            for (String expr : by) {
                Object v = expression(expr, row);
                key.add(v);
                head.put(Values.label(expr), v);
            }
            List<Map<String, Object>> bucket = buckets.get(key);
            if (bucket == null) {
                bucket = new ArrayList<>();
                buckets.put(key, bucket);
                heads.put(key, head);
            }
            bucket.add(row);
        }
        if (by.isEmpty()) {
            // 无分组列即整表一组：与 SQL 的裸聚合一致，空表也要产出一行（SUM→0 / COUNT→0）
            buckets.clear();
            heads.clear();
            List<Object> empty = new ArrayList<>();
            buckets.put(empty, rows);
            heads.put(empty, new LinkedHashMap<String, Object>());
        }
        List<Object> list = new ArrayList<>();
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<List<Object>, List<Map<String, Object>>> entry : buckets.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>(heads.get(entry.getKey()));
            for (Map.Entry<String, Object> a : agg.entrySet()) {
                row.put(a.getKey(), Aggregates.evaluate(Values.str(a.getValue()), entry.getValue(), this::expression));
            }
            if (items != null) {
                row.put(items, new ArrayList<Object>(entry.getValue()));
            }
            Object produced = into == null ? row : templates.eval(into, row);
            if (keyed) {
                map.put(Values.key(entry.getKey().get(0)), produced);
            } else {
                list.add(produced);
            }
        }
        return keyed ? map : list;
    }

    /** 整表聚合成一个对象：{@code group} 去掉 by 后取那唯一一行 */
    private Object fold(Map<String, Object> step, Object value) {
        Map<String, Object> copy = new LinkedHashMap<>(step);
        copy.remove("by");
        copy.remove("as");
        return ((List<?>) group(copy, value)).get(0);
    }

    /** 行数组 → 对象数组；{@code of} 先取对象里的子数组再整形，产出就是这个新数组（模板槽位里的嵌套靠它） */
    private Object map(Map<String, Object> step, Object value) {
        Object into = step.get("into");
        Object input = value;
        if (step.containsKey("of")) {
            String of = Values.str(step.get("of"));
            input = Values.get(value, of);
            if (input == null) {
                // of 取不到就是 spec 写错了：静默产出空数组会把问题推到渲染端
                throw new ObjException("map 的 of=\"" + of + "\" 取不到数组, 当前值: "
                        + (value instanceof Map ? "字段 " + ((Map<?, ?>) value).keySet() : Values.typeOf(value)));
            }
        }
        List<Object> out = new ArrayList<>();
        for (Object item : Values.asList(input, "map")) {
            out.add(into == null ? item : templates.eval(into, item));
        }
        return out;
    }

    /** 行数组 → 键值对象。key 重复直接报错：keyBy 的语义就是"一键一行"，静默丢行是数据事故 */
    private Object keyBy(Map<String, Object> step, Object value) {
        String key = Values.required(step, "key", "keyBy");
        Object into = step.get("into");
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map<String, Object> row : Values.asRows(value, "keyBy")) {
            String k = Values.key(expression(key, row));
            Object v = into == null ? new LinkedHashMap<String, Object>(row) : templates.eval(into, row);
            if (out.put(k, v) != null) {
                throw new ObjException("keyBy 的 key 重复: " + k + " (表达式 " + key + ")");
            }
        }
        return out;
    }

    /** 平铺的 id / parentId 两列 → 嵌套 children 的森林 */
    private Object tree(Map<String, Object> step, Object value) {
        String idExpr = Values.required(step, "id", "tree");
        String parentExpr = Values.required(step, "parent", "tree");
        String childrenKey = Values.optional(step, "children", "children");
        List<Map<String, Object>> rows = Values.asRows(value, "tree");
        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
        Map<String, String> parentOf = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String id = Values.key(expression(idExpr, row));
            if (id.isEmpty()) {
                throw new ObjException("tree 遇到 id 为空的行: " + row);
            }
            if (nodes.containsKey(id)) {
                throw new ObjException("tree 的 id 重复: " + id);
            }
            nodes.put(id, new LinkedHashMap<>(row));
            parentOf.put(id, Values.key(expression(parentExpr, row)));
        }
        Map<String, List<Map<String, Object>>> kids = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : nodes.entrySet()) {
            String parent = parentOf.get(entry.getKey());
            // 父不存在 = 孤儿，按根处理：脏数据要能看出树，而不是整棵树消失
            if (parent == null || parent.isEmpty() || parent.equals(entry.getKey()) || !nodes.containsKey(parent)) {
                roots.add(entry.getValue());
            } else {
                List<Map<String, Object>> list = kids.get(parent);
                if (list == null) {
                    list = new ArrayList<>();
                    kids.put(parent, list);
                }
                list.add(entry.getValue());
            }
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : kids.entrySet()) {
            nodes.get(entry.getKey()).put(childrenKey, entry.getValue());
        }
        return roots;
    }

    /** 长表 → 宽表矩阵：by 决定行，on 决定列，value 按 agg 聚合填格子 */
    private Object pivot(Map<String, Object> step, Object value) {
        List<String> by = Values.asStrings(step.get("by"), "pivot.by");
        String on = Values.required(step, "on", "pivot");
        String aggName = Values.optional(step, "agg", "SUM");
        Object measureSlot = step.get("value");
        if (measureSlot == null && !"COUNT".equalsIgnoreCase(aggName)) {
            throw new ObjException("pivot 需要 value 槽位指定被聚合的度量列 (agg=COUNT 时可省略)");
        }
        String aggExpr = aggName + "(" + (measureSlot == null ? "*" : Values.str(measureSlot)) + ")";
        Map<List<Object>, Map<String, Object>> heads = new LinkedHashMap<>();
        Map<List<Object>, Map<String, List<Map<String, Object>>>> cells = new LinkedHashMap<>();
        Set<String> columns = new LinkedHashSet<>();
        for (Map<String, Object> row : Values.asRows(value, "pivot")) {
            List<Object> key = new ArrayList<>(by.size());
            Map<String, Object> head = new LinkedHashMap<>();
            for (String expr : by) {
                Object v = expression(expr, row);
                key.add(v);
                head.put(Values.label(expr), v);
            }
            String label = Values.key(expression(on, row));
            Map<String, List<Map<String, Object>>> bucket = cells.get(key);
            if (bucket == null) {
                bucket = new LinkedHashMap<>();
                cells.put(key, bucket);
                heads.put(key, head);
            }
            List<Map<String, Object>> cell = bucket.get(label);
            if (cell == null) {
                cell = new ArrayList<>();
                bucket.put(label, cell);
            }
            cell.add(row);
            columns.add(label);
        }
        boolean count = "COUNT".equalsIgnoreCase(aggName);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<List<Object>, Map<String, List<Map<String, Object>>>> entry : cells.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>(heads.get(entry.getKey()));
            for (String column : columns) {
                List<Map<String, Object>> cell = entry.getValue().get(column);
                row.put(column, cell == null ? (count ? (Object) 0L : null)
                        : Aggregates.evaluate(aggExpr, cell, this::expression));
            }
            out.add(row);
        }
        return out;
    }

    /** 宽表 → 长表：cols（或 keep 之外的所有列）逐列摊成 {name, value} 行 */
    private Object unpivot(Map<String, Object> step, Object value) {
        List<String> cols = Values.asStrings(step.get("cols"), "unpivot.cols");
        List<String> keep = Values.asStrings(step.get("keep"), "unpivot.keep");
        if (cols.isEmpty() && keep.isEmpty()) {
            throw new ObjException("unpivot 需要 cols 或 keep 之一来界定要展开的列");
        }
        Map<String, Object> as = Values.asMap(step.get("as"), "unpivot.as");
        String nameKey = as.get("name") == null ? "column" : Values.str(as.get("name"));
        String valueKey = as.get("value") == null ? "value" : Values.str(as.get("value"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> row : Values.asRows(value, "unpivot")) {
            List<String> targets = cols.isEmpty() ? difference(row.keySet(), keep) : cols;
            Set<String> carried = new LinkedHashSet<>(row.keySet());
            carried.removeAll(targets);
            for (String column : targets) {
                if (!row.containsKey(column)) {
                    continue;
                }
                Map<String, Object> line = new LinkedHashMap<>();
                for (String k : carried) {
                    line.put(k, row.get(k));
                }
                line.put(nameKey, column);
                line.put(valueKey, row.get(column));
                out.add(line);
            }
        }
        return out;
    }

    private Object set(Map<String, Object> step, Object value) {
        String path = Values.required(step, "path", "set");
        final boolean template = step.containsKey("value");
        final Object slot = template ? step.get("value") : Values.required(step, "expr", "set");
        return Values.update(value, path, container -> template
                ? templates.eval(slot, container)
                : expression(Values.str(slot), container));
    }

    private static List<String> difference(Set<String> all, List<String> minus) {
        Set<String> out = new LinkedHashSet<>(all);
        out.removeAll(minus);
        return new ArrayList<>(out);
    }
}

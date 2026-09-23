package com.zifang.util.db.memory;

import com.zifang.util.db.query.DynamicQuery;
import com.zifang.util.db.query.Query;
import com.zifang.util.db.query.SqlSpec;
import com.zifang.util.expr.sql.engine.Table;
import com.zifang.util.expr.sql.engine.VirtualTableEngine;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JDBC 取数与内存 SQL 引擎（z-util-expr-sql）的接合层。
 * <p>
 * 本层只负责把库里的表搬进内存，join / group by / 聚合 / 表达式函数等复杂处理
 * 全部委托给 {@link VirtualTableEngine}，不重复实现关系算子。
 * <pre>
 *   InMemoryTables mem = new InMemoryTables(dq)
 *           .load("t_order", Query.select().from("t_order"))
 *           .load("t_user", Query.select("id", "name", "dept").from("t_user"))
 *           .index("t_order", "user_id");
 *   List&lt;Map&lt;String, Object&gt;&gt; rows = mem.sql(
 *           "SELECT u.dept, COUNT(*) AS cnt, SUM(o.amount) AS total FROM t_order o"
 *                   + " INNER JOIN t_user u ON o.user_id = u.id GROUP BY u.dept");
 * </pre>
 * 表名大小写不敏感（由引擎统一小写归档），同名 {@code load} 覆盖旧数据。
 * 内存语义由 {@link DynamicQuery#maxRows(int)} 兜底，取数前请按量级设置。
 *
 * @author zifang
 */
public final class InMemoryTables {

    private final DynamicQuery source;

    private final VirtualTableEngine engine;

    /**
     * 纯内存用法：不接数据库，只 {@link #load(String, List)} 手工喂数据。
     */
    public InMemoryTables() {
        this(null, new VirtualTableEngine());
    }

    public InMemoryTables(DynamicQuery source) {
        this(source, new VirtualTableEngine());
    }

    private InMemoryTables(DynamicQuery source, VirtualTableEngine engine) {
        this.source = source;
        this.engine = engine;
    }

    /**
     * 结构化取数并注册为内存表。
     */
    public InMemoryTables load(String tableName, Query query) {
        return load(tableName, requireSource().list(query));
    }

    /**
     * 参数化 SQL（含 {@link com.zifang.util.db.query.SqlTemplate} 产物）取数并注册为内存表。
     */
    public InMemoryTables load(String tableName, SqlSpec spec) {
        return load(tableName, requireSource().list(spec));
    }

    /**
     * 任意来源的行数据注册为内存表。
     */
    public InMemoryTables load(String tableName, List<Map<String, Object>> rows) {
        engine.register(tableName, rows);
        return this;
    }

    /**
     * 为内存表建索引，加速等值 WHERE 与等值 JOIN。
     */
    public InMemoryTables index(String tableName, String... columns) {
        engine.createIndex(tableName, columns);
        return this;
    }

    /**
     * 对内存表执行任意 SELECT，支持 join、group by、聚合与内置函数。
     */
    public List<Map<String, Object>> sql(String selectSql) {
        return engine.query(selectSql);
    }

    /**
     * 取已注册的内存表，用 {@link Table} 的链式算子（where/sort/groupBy/addColumn/aggregate）继续处理。
     */
    public Table table(String tableName) {
        return engine.getTable(tableName);
    }

    public VirtualTableEngine engine() {
        return engine;
    }

    public Set<String> names() {
        return engine.getTableNames();
    }

    private DynamicQuery requireSource() {
        if (source == null) {
            throw new IllegalStateException("未绑定 DynamicQuery, 无法从数据库取数; 请用 load(tableName, rows) 或带 DynamicQuery 的构造");
        }
        return source;
    }
}

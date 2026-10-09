package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.engine.ast.Expression;
import com.zifang.util.expr.sql.engine.ast.SelectStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 内存 SQL 引擎门面。
 * <p>
 * 提供虚拟表注册、索引管理和 SQL 查询的统一入口。
 *
 * <pre>
 * // 使用示例
 * VirtualTableEngine engine = new VirtualTableEngine();
 *
 * // 注册虚拟表
 * List&lt;Map&lt;String, Object&gt;&gt; data = ...;
 * engine.register("users", data);
 *
 * // 创建索引加速查询
 * engine.createIndex("users", "id");
 * engine.createIndex("users", "name");
 *
 * // SQL 查询（自动利用索引）
 * List&lt;Map&lt;String, Object&gt;&gt; result = engine.query("SELECT name, age FROM users WHERE id = 1");
 *
 * // 转 Bean
 * List&lt;UserDTO&gt; dtos = engine.queryAs("SELECT name, age FROM users WHERE age &gt; 18", UserDTO.class);
 *
 * // 多表 JOIN（自动利用索引加速）
 * engine.register("orders", orderData);
 * engine.createIndex("orders", "user_id");
 * List&lt;Map&lt;String, Object&gt;&gt; joined = engine.query(
 *     "SELECT u.name, o.product FROM users u INNER JOIN orders o ON u.id = o.user_id");
 * </pre>
 */
public class VirtualTableEngine {

    private final TableRegistry registry;
    private final SqlAstBuilder astBuilder;
    private final SqlEngine engine;

    public VirtualTableEngine() {
        this.registry = new TableRegistry();
        this.astBuilder = new SqlAstBuilder();
        this.engine = new SqlEngine(registry);
    }

    // ==================== 表管理 ====================

    /**
     * 注册虚拟表。
     *
     * @param name 表名
     * @param data 行数据（List of Map）
     */
    public void register(String name, List<Map<String, Object>> data) {
        registry.register(name, data);
    }

    /**
     * 注册 Table 对象。
     */
    public void register(String name, Table table) {
        registry.register(name, table.toMapList());
    }

    /**
     * 注销虚拟表。
     */
    public void unregister(String name) {
        registry.unregister(name);
    }

    /**
     * 获取所有已注册的表名。
     */
    public Set<String> getTableNames() {
        return registry.getTableNames();
    }

    /**
     * 检查表是否存在。
     */
    public boolean hasTable(String name) {
        return registry.contains(name);
    }

    /**
     * 清空所有表。
     */
    public void clear() {
        registry.clear();
    }

    // ==================== 索引管理 ====================

    /**
     * 为指定表的指定列创建索引。
     * <p>
     * 索引可加速以下场景：
     * <ul>
     *   <li>WHERE column = value（等值查询）</li>
     *   <li>JOIN ON a.column = b.column（等值连接）</li>
     * </ul>
     *
     * @param tableName  表名
     * @param columnName 列名
     */
    public void createIndex(String tableName, String columnName) {
        VirtualTable table = registry.getTable(tableName);
        table.createIndex(columnName);
    }

    /**
     * 为指定表的多个列批量创建索引。
     *
     * @param tableName  表名
     * @param columnNames 列名数组
     */
    public void createIndex(String tableName, String... columnNames) {
        VirtualTable table = registry.getTable(tableName);
        for (String col : columnNames) {
            table.createIndex(col);
        }
    }

    /**
     * 检查指定表的指定列是否有索引。
     */
    public boolean hasIndex(String tableName, String columnName) {
        VirtualTable table = registry.getTable(tableName);
        return table.hasIndex(columnName);
    }

    // ==================== SQL 查询 ====================

    /**
     * 执行 SQL 查询，返回 List&lt;Map&lt;String, Object&gt;&gt;。
     *
     * @param sql SQL 查询语句（仅支持 SELECT）
     * @return 结果行列表
     */
    public List<Map<String, Object>> query(String sql) {
        SelectStmt stmt = astBuilder.parse(sql);
        return engine.execute(stmt);
    }

    /**
     * 执行 SQL 查询并转换为 Bean 列表。
     *
     * @param sql       SQL 查询语句
     * @param beanClass 目标 Bean 类
     * @param <T>       Bean 类型
     * @return Bean 列表
     */
    public <T> List<T> queryAs(String sql, Class<T> beanClass) {
        List<Map<String, Object>> rows = query(sql);
        return BeanConverter.toBeans(rows, beanClass);
    }

    /**
     * 执行 SQL 查询，返回 Table 对象。
     */
    public Table queryTable(String sql) {
        List<Map<String, Object>> result = query(sql);
        return Table.fromMaps("result", result);
    }

    /**
     * 获取指定表的 Table 包装对象。
     */
    public Table getTable(String name) {
        VirtualTable vt = registry.getTable(name);
        return Table.fromVirtualTable(vt, name);
    }

    /**
     * 获取底层 TableRegistry（高级用法）。
     */
    public TableRegistry getRegistry() {
        return registry;
    }

    // ==================== 多段 SQL 管线 ====================

    /**
     * 单段声明：{@code as} 为阶段产出在本引擎里注册的表名（后续阶段可引用），{@code sql} 为该段 SELECT。
     * 顺序执行；最后一段的 SQL 结果作为返回值，前面每段的结果都已注册为同名内存表。
     * <p>
     * 替代 FT 侧在 LcPipeline / LcDatasetEngine / LcScriptEngine 重复实现的「
     * 多段 SQL + 中间表注册」语义。
     */
    public static final class Stage {
        private final String as;
        private final String sql;

        public Stage(String as, String sql) {
            this.as = as;
            this.sql = sql;
        }

        public String as() {
            return as;
        }

        public String sql() {
            return sql;
        }
    }

    /**
     * 执行多段 SQL 管线。
     *
     * @param stages 多段声明，每段 {@code {"as":"t1","sql":"..."}}; 最后一段执行结果即为返回值，
     *               前面的段按顺序执行并把结果以 {@code as} 名为表名注册到本引擎
     * @return 最后一段 SQL 的查询结果
     */
    public List<Map<String, Object>> pipeline(List<Stage> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new SqlException("pipeline 至少要有一段声明");
        }
        for (int i = 0; i < stages.size(); i++) {
            Stage stage = stages.get(i);
            if (stage == null || stage.sql() == null) {
                throw new SqlException("第 " + i + " 段缺少 sql");
            }
            List<Map<String, Object>> rows = query(stage.sql());
            if (stage.as() != null) {
                registry.register(stage.as(), rows);
            }
        }
        Stage last = stages.get(stages.size() - 1);
        return query(last.sql());
    }

    /**
     * 取一行作为临时表（手工喂数据），再跑后续管线时该表仍可见。
     */
    public void stage(String as, List<Map<String, Object>> rows) {
        registry.register(as, rows);
    }

    /**
     * 取一段 SQL 跑出结果并以 {@code as} 为表名注册；便于「先 stage = 把派生表建好」再写后续 SQL 引用。
     */
    public void stage(String as, String sql) {
        registry.register(as, query(sql));
    }

    // ==================== Schema 推断（dry-run） ====================

    /**
     * 轻量级 schema 推断：解析 SELECT，输出每列的名称与推断类型，<b>不实际执行</b>。
     * 适合前端「数据集输出列」面板：不用跑一遍管线就能列清单。
     * <p>
     * 类型推断规则：
     * <ul>
     *   <li>列名直接照搬底层表（{@code String} 兜底，VirtualTable 列元数据里登记的类型优先）</li>
     *   <li>字面量 → 字面量自身的类型</li>
     *   <li>{@code CAST(... AS T)} → T</li>
     *   <li>聚合函数 → COUNT: {@code LONG}, SUM/AVG: {@code DECIMAL}, MAX/MIN: 同列</li>
     *   <li>算术表达式 → {@code DOUBLE}，比较/逻辑 → {@code BOOLEAN}</li>
     *   <li>其它函数沿用 {@link SqlFunctionRegistry} 元数据；都没有就 {@code UNKNOWN}</li>
     * </ul>
     */
    public List<ColumnDef> explain(String sql) {
        SelectStmt stmt = astBuilder.parse(sql);
        List<ColumnDef> out = new ArrayList<>();
        VirtualTable main = stmt.getTableName() != null && registry.contains(stmt.getTableName())
                ? registry.getTable(stmt.getTableName()) : null;
        for (Expression item : stmt.getSelectItems()) {
            String name = engine.explainName(item);
            String type = engine.explainType(item, main);
            out.add(new ColumnDef(name, type));
        }
        return out;
    }

    /**
     * 单列 schema：{@code name} 为输出列名（含 AS 别名），{@code type} 为推断类型。
     */
    public static final class ColumnDef {
        private final String name;
        private final String type;

        public ColumnDef(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String name() {
            return name;
        }

        public String type() {
            return type;
        }

        @Override
        public String toString() {
            return name + " " + type;
        }
    }
}

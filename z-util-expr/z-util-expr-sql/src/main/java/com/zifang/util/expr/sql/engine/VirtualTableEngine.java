package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.engine.ast.SelectStmt;

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
}

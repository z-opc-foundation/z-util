package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.SqlException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 虚拟表注册中心。
 * <p>
 * 管理命名虚拟表的注册、注销和查询。
 * 线程安全。
 */
public class TableRegistry {

    private final Map<String, VirtualTable> tables = new ConcurrentHashMap<>();

    /**
     * 注册虚拟表。
     *
     * @param name 表名
     * @param data 行数据
     */
    public void register(String name, java.util.List<Map<String, Object>> data) {
        if (name == null || name.trim().isEmpty()) {
            throw new SqlException("表名不能为空");
        }
        tables.put(name.toLowerCase(), new VirtualTable(data));
    }

    /**
     * 注销虚拟表。
     */
    public void unregister(String name) {
        tables.remove(name.toLowerCase());
    }

    /**
     * 获取虚拟表。
     *
     * @param name 表名
     * @return 虚拟表
     * @throws SqlException 表不存在时抛出
     */
    public VirtualTable getTable(String name) {
        VirtualTable table = tables.get(name.toLowerCase());
        if (table == null) {
            throw new SqlException("表不存在: " + name);
        }
        return table;
    }

    /**
     * 检查表是否存在。
     */
    public boolean contains(String name) {
        return tables.containsKey(name.toLowerCase());
    }

    /**
     * 获取所有已注册的表名。
     */
    public Set<String> getTableNames() {
        return tables.keySet();
    }

    /**
     * 清空所有表。
     */
    public void clear() {
        tables.clear();
    }
}

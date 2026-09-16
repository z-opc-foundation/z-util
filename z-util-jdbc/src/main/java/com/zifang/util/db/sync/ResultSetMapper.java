package com.zifang.util.db.sync;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自研 ResultSet → Java 对象映射工具，对标 commons-dbutils 的核心 handler。
 * <p>
 * 设计目标：
 * <ul>
 *     <li>不引入第三方依赖，纯粹基于 {@link ResultSet} API 实现</li>
 *     <li>支持将任意行转为 {@code Map&lt;String, Object&gt;}</li>
 *     <li>支持将首行首列标量值转为 {@link Object}</li>
 * </ul>
 *
 * <p>典型用法：
 * <pre>
 *   List&lt;Map&lt;String, Object&gt;&gt; rows = ResultSetMapper.toMapList(rs);
 *   Object scalar = ResultSetMapper.toScalar(rs);
 * </pre>
 */
public final class ResultSetMapper {

    private ResultSetMapper() {
    }

    /**
     * 将整个 ResultSet 解析为 {@code List<Map<String, Object>>}，列名由
     * {@link ResultSetMetaData#getColumnLabel()} 取得（优先取 alias，其次取列名）。
     */
    public static List<Map<String, Object>> toMapList(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>(columnCount * 2);
            for (int i = 1; i <= columnCount; i++) {
                String name = meta.getColumnLabel(i);
                if (name == null || name.isEmpty()) {
                    name = meta.getColumnName(i);
                }
                row.put(name, rs.getObject(i));
            }
            result.add(row);
        }
        return result;
    }

    /**
     * 取 ResultSet 第一行第一列的值，ResultSet 为空时返回 null。
     */
    public static Object toScalar(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getObject(1);
        }
        return null;
    }
}

package com.zifang.util.expr.obj;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 对象语言的取数入口：{@code from.table} / {@code from.sql} 两个槽位背后的实现。
 * <p>
 * 本模块只定义接口，不依赖 z-util-jdbc 与 z-util-expr-sql：内存 SQL 引擎的适配器由
 * {@code InMemoryTables.shape(...)} 提供，这样对象语言本身可以只吃一批行数据独立使用。
 *
 * @author zifang
 */
public interface TableSource {

    /**
     * 按表名取二维行数据；表不存在时返回空集合还是抛异常由实现决定（内存实现按大小写不敏感查表）。
     */
    List<Map<String, Object>> rows(String tableName);

    /**
     * 执行任意 SELECT 取二维结果。默认不支持：纯行数据的用法没有 SQL 引擎。
     */
    default List<Map<String, Object>> query(String sql) {
        throw new ObjException("当前 TableSource 不支持 from.sql 取数, 请改用 from.rows / from.table, 或绑定 InMemoryTables");
    }

    static TableSource none() {
        return new TableSource() {
            @Override
            public List<Map<String, Object>> rows(String tableName) {
                throw new ObjException("未绑定 TableSource, 无法用 from.table 取数; 请传入 TableSource 或改用 from.rows");
            }

            @Override
            public List<Map<String, Object>> query(String sql) {
                throw new ObjException("未绑定 TableSource, 无法用 from.sql 取数; 请传入 TableSource 或改用 from.rows");
            }
        };
    }

    static TableSource of(Map<String, List<Map<String, Object>>> tables) {
        Map<String, List<Map<String, Object>>> copy = new java.util.LinkedHashMap<>(tables);
        return name -> copy.getOrDefault(name, Collections.emptyList());
    }
}

package com.zifang.util.db.dialect;

import com.zifang.util.db.meta.DataSourceDTO;

/**
 * 数据库方言：把"与库有关的 SQL 差异"收在一处。
 * <p>
 * 只负责四件事——连接串构造、标识符引用、分页与计数 SQL 生成、探活语句。
 * 不做聚合/函数映射，动态查询只产出行级 SQL。
 *
 * @author zifang
 */
public interface Dialect {

    /**
     * 方言标识，取值 mysql / postgres / h2，与 {@code DataSourceDTO.datasourceType} 对应。
     */
    String id();

    /**
     * JDBC 驱动类名。
     */
    String driverClassName();

    /**
     * 由数据源定义拼出 JDBC URL。
     */
    String buildUrl(DataSourceDTO def);

    /**
     * 引用标识符，接受 {@code table} 或 {@code schema.table}，逐段校验后按方言加上引用符。
     */
    String quote(String qualifiedName);

    /**
     * 追加分页子句；limit&lt;=0 时不追加。数值由内部生成为字面量，不参与注入。
     */
    String limitClause(long offset, long limit);

    /**
     * 把任意查询包成计数查询。
     */
    String countSql(String selectSql);

    /**
     * 探活 SQL，用于建池后校验连通性。
     */
    String validationQuery();
}

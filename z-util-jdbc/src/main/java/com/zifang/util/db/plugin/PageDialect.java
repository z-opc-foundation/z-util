package com.zifang.util.db.plugin;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 分页SQL方言
 * <p>
 * 针对不同数据库生成物理分页SQL：
 * <ul>
 *   <li>LIMIT：MySQL、MariaDB、PostgreSQL、H2、SQLite、HSQLDB等，采用 LIMIT n OFFSET m 语法</li>
 *   <li>ROWNUM：Oracle，采用 ROWNUM 子查询包装语法（兼容Oracle各版本）</li>
 *   <li>FETCH：SQL Server 2012+、DB2、Derby等，采用 OFFSET ... ROWS FETCH NEXT ... ROWS ONLY 语法</li>
 * </ul>
 * <p>
 * 可通过{@link #resolve(Connection)}根据连接元数据自动识别，也可通过{@link #resolve(String)}
 * 按数据库产品名称识别；未识别的产品默认采用LIMIT语法。
 *
 * @author zifang
 * @see MyBatisPageInterceptor
 */
public enum PageDialect {

    /**
     * LIMIT/OFFSET语法
     */
    LIMIT {
        @Override
        public String buildPageSql(String sql, long offset, long size) {
            return sql + " LIMIT " + size + " OFFSET " + offset;
        }
    },

    /**
     * Oracle ROWNUM包装语法
     */
    ROWNUM {
        @Override
        public String buildPageSql(String sql, long offset, long size) {
            long end = offset + size;
            return "SELECT * FROM ( SELECT row_.*, ROWNUM rownum_ FROM ( " + sql
                    + " ) row_ WHERE ROWNUM <= " + end + " ) WHERE rownum_ > " + offset;
        }
    },

    /**
     * 标准OFFSET/FETCH语法
     */
    FETCH {
        @Override
        public String buildPageSql(String sql, long offset, long size) {
            return sql + " OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY";
        }
    };

    /**
     * 构建分页SQL
     *
     * @param sql    原始查询SQL
     * @param offset 起始偏移量（从0开始）
     * @param size   每页记录数
     * @return 改写后的分页SQL
     */
    public abstract String buildPageSql(String sql, long offset, long size);

    /**
     * 根据数据库连接元数据识别分页方言
     *
     * @param connection 数据库连接
     * @return 识别出的分页方言；无法识别时返回LIMIT
     * @throws SQLException 读取数据库元数据失败时抛出
     */
    public static PageDialect resolve(Connection connection) throws SQLException {
        if (connection == null) {
            return LIMIT;
        }
        return resolve(connection.getMetaData().getDatabaseProductName());
    }

    /**
     * 根据数据库产品名称识别分页方言
     *
     * @param productName 数据库产品名称，如 MySQL、Oracle、Microsoft SQL Server
     * @return 识别出的分页方言；无法识别时返回LIMIT
     */
    public static PageDialect resolve(String productName) {
        if (productName == null) {
            return LIMIT;
        }
        String name = productName.toLowerCase();
        if (name.contains("mysql") || name.contains("mariadb") || name.contains("postgres")
                || name.contains("h2") || name.contains("sqlite") || name.contains("hsqldb")) {
            return LIMIT;
        }
        if (name.contains("oracle")) {
            return ROWNUM;
        }
        if (name.contains("sql server") || name.contains("sqlserver") || name.contains("db2")
                || name.contains("derby")) {
            return FETCH;
        }
        return LIMIT;
    }
}

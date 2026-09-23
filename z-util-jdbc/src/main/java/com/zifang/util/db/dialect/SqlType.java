package com.zifang.util.db.dialect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 跨库归一化的字段类型。
 * <p>
 * 各库的 {@code TYPE_NAME}（varchar/longtext/int4...）不一致，但 {@link java.sql.Types}
 * 的编码是 JDBC 标准，因此以编码为键归一，下游拿到的类型不再随库漂移。
 * <p>
 * 值本身不做归一：{@code ResultSet.getObject} 交给驱动的 Java 类型由上层决定怎么呈现
 * （报表侧要 LocalDate，导出侧要原始 BigDecimal），这里不替它们做主。
 *
 * @author zifang
 */
public enum SqlType {

    STRING,
    INTEGER,
    LONG,
    DOUBLE,
    DECIMAL,
    BOOLEAN,
    DATE,
    TIME,
    TIMESTAMP,
    BINARY,
    OTHER;

    /**
     * JDBC 类型编码 → 归一类型。
     */
    public static SqlType of(int jdbcType) {
        switch (jdbcType) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
                return STRING;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return INTEGER;
            case Types.BIGINT:
                return LONG;
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return DOUBLE;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return DECIMAL;
            case Types.BIT:
            case Types.BOOLEAN:
                return BOOLEAN;
            case Types.DATE:
                return DATE;
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return TIME;
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return TIMESTAMP;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return BINARY;
            default:
                return OTHER;
        }
    }

    /**
     * 取结果集第 col 列（1-based）的归一类型。
     */
    public static SqlType of(ResultSet rs, int col) throws SQLException {
        return of(rs.getMetaData().getColumnType(col));
    }
}

package com.zifang.util.expr.sql;

/**
 * SQL 解析异常
 */
public class SqlException extends RuntimeException {

    public SqlException(String message) {
        super(message);
    }

    public SqlException(String message, Throwable cause) {
        super(message, cause);
    }
}

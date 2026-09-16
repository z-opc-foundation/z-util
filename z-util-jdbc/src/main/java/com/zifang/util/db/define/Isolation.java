package com.zifang.util.db.define;

import java.sql.Connection;

/**
 * 事务隔离级别枚举
 */
public enum Isolation {

    /**
     * 使用数据库默认隔离级别
     */
    DEFAULT(Connection.TRANSACTION_NONE),

    /**
     * 读未提交，允许脏读
     */
    READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

    /**
     * 读已提交，阻止脏读
     */
    READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

    /**
     * 可重复读，阻止脏读和不可重复读
     */
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

    /**
     * 串行化，阻止所有并发问题
     */
    SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

    private final int level;

    Isolation(int level) {
        this.level = level;
    }

    /**
     * getLevel方法。
     *
     * @return int类型返回值
     */
    public int getLevel() {
        return level;
    }
}

package com.zifang.util.db.query;

/**
 * 分组连接方式。
 *
 * @author zifang
 */
public enum Logic {

    AND(" AND "),
    OR(" OR ");

    private final String keyword;

    Logic(String keyword) {
        this.keyword = keyword;
    }

    public String keyword() {
        return keyword;
    }
}

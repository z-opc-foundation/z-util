package com.zifang.util.db.dialect;

/**
 * 方言内部使用的入参检查。
 *
 * @author zifang
 */
final class Check {

    private Check() {
    }

    static String text(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
        return value.trim();
    }

    static int port(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }
}

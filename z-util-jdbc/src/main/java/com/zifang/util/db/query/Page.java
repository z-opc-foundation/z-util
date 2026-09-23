package com.zifang.util.db.query;

import java.util.Collections;
import java.util.List;

/**
 * 一次分页取数的结果：行 + 满足同条件的总行数。
 *
 * @author zifang
 */
public final class Page<T> {

    private final List<T> rows;

    private final long total;

    private final long offset;

    private final long limit;

    public Page(List<T> rows, long total, long offset, long limit) {
        this.rows = rows == null ? Collections.<T>emptyList() : Collections.unmodifiableList(rows);
        this.total = total;
        this.offset = offset;
        this.limit = limit;
    }

    public List<T> rows() {
        return rows;
    }

    public long total() {
        return total;
    }

    public long offset() {
        return offset;
    }

    public long limit() {
        return limit;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    /**
     * 页码从 1 开始；limit 未设（全量取数）时按整页处理。
     */
    public long pageNo() {
        if (limit <= 0) {
            return 1;
        }
        return offset / limit + 1;
    }

    public long pageCount() {
        if (limit <= 0) {
            return total == 0 ? 0 : 1;
        }
        return (total + limit - 1) / limit;
    }

    @Override
    public String toString() {
        return "Page{rows=" + rows.size() + ", total=" + total + ", offset=" + offset + ", limit=" + limit + "}";
    }
}

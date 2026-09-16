package com.zifang.util.core.meta.page;

import com.zifang.util.core.meta.SortField;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link IPageable} 的简单通用实现。
 * <p>
 * 适用于已拿到 records / current / size / total 四要素、无需排序信息的场景
 * （如 MyBatis-Plus {@code Page} 结果向 IPageable 的直接转换）。
 *
 * @param <T> 数据类型
 */
public class SimpleIPageable<T> implements IPageable<T> {

    private static final long serialVersionUID = 1L;

    private List<T> records;
    private long total;
    private long size;
    private long current;
    private List<SortField> orders;

    public SimpleIPageable() {
    }

    public SimpleIPageable(List<T> records, long current, long size, long total) {
        this.records = records;
        this.current = current;
        this.size = size;
        this.total = total;
    }

    @Override
    public List<SortField> orders() {
        return orders;
    }

    public void setOrders(List<SortField> orders) {
        this.orders = orders;
    }

    @Override
    public List<T> getRecords() {
        return records;
    }

    @Override
    public IPageable<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    public long getTotal() {
        return total;
    }

    @Override
    public IPageable<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public IPageable<T> setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public long getCurrent() {
        return current;
    }

    @Override
    public IPageable<T> setCurrent(long current) {
        this.current = current;
        return this;
    }

    /**
     * 空分页结果。
     */
    public static <T> SimpleIPageable<T> empty(long current, long size) {
        return new SimpleIPageable<>(new ArrayList<T>(), current, size, 0L);
    }
}

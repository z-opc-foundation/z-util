package com.zifang.util.core.meta.page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zifang.util.core.meta.KeepLongSerializer;
import com.zifang.util.core.meta.SortField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author zifang
 */
public class Pageable<T> implements IPageable<T> {

    private static final long serialVersionUID = -8805933490739835015L;

    /**
     * 查询数据列表
     */
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    /**
     * Collections.emptyList方法。
     * @return List<T> records =类型返回值
     */
    protected List<T> records = Collections.emptyList();

    /**
     * 总数
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    protected long total = 0;
    /**
     * 每页显示条数，默认 10
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    protected long size = 10;

    /**
     * 当前页
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    protected long current = 1;

    /**
     * 排序字段信息
     */
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    /**
     * ArrayList<>方法。
     * @return List<SortField> orders = new类型返回值
     */
    protected List<SortField> orders = new ArrayList<>();

    /**
     * Pageable方法。
     */
    public Pageable() {
    }

    /**
     * 分页构造函数
     *
     * @param current 当前页
     * @param size    每页显示条数
     */
    public Pageable(long current, long size) {
        this(current, size, 0);
    }

    /**
     * Pageable方法。
     * * @param current long类型参数
     *
     * @param size  long类型参数
     * @param total long类型参数
     */
    public Pageable(long current, long size, long total) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
    }

    /**
     * getOrders方法。
     *
     * @return List<SortField>类型返回值
     */
    public List<SortField> getOrders() {
        return orders;
    }

    /**
     * setOrders方法。
     * * @param orders ListSortField类型参数
     */
    public void setOrders(List<SortField> orders) {
        this.orders = orders;
    }

    /**
     * 是否存在上一页
     *
     * @return true / false
     */
    public boolean hasPrevious() {
        return this.current > 1;
    }

    /**
     * 是否存在下一页
     *
     * @return true / false
     */
    public boolean hasNext() {
        return this.current < this.getPages();
    }

    @Override
    /**
     * getRecords方法。
     * @return List<T>类型返回值
     */
    public List<T> getRecords() {
        return this.records;
    }

    @Override
    /**
     * setRecords方法。
     *      * @param records ListT类型参数
     * @return Pageable<T>类型返回值
     */
    public Pageable<T> setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    @Override
    /**
     * getTotal方法。
     * @return long类型返回值
     */
    public long getTotal() {
        return this.total;
    }

    @Override
    /**
     * setTotal方法。
     *      * @param total long类型参数
     * @return Pageable<T>类型返回值
     */
    public Pageable<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    @Override
    /**
     * getSize方法。
     * @return long类型返回值
     */
    public long getSize() {
        return this.size;
    }

    @Override
    /**
     * setSize方法。
     *      * @param size long类型参数
     * @return Pageable<T>类型返回值
     */
    public Pageable<T> setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    /**
     * getCurrent方法。
     * @return long类型返回值
     */
    public long getCurrent() {
        return this.current;
    }

    @Override
    /**
     * setCurrent方法。
     *      * @param current long类型参数
     * @return Pageable<T>类型返回值
     */
    public Pageable<T> setCurrent(long current) {
        this.current = current;
        return this;
    }


    /**
     * 查找 order 中正序排序的字段数组
     *
     * @param filter 过滤器
     * @return 返回正序排列的字段数组
     */
    @Deprecated
    private String[] mapOrderToArray(Predicate<SortField> filter) {
        List<String> columns = new ArrayList<>(orders.size());
        orders.forEach(i -> {
            if (filter.test(i)) {
                columns.add(i.getColumn());
            }
        });
        return columns.toArray(new String[0]);
    }

    /**
     * 移除符合条件的条件
     *
     * @param filter 条件判断
     */
    @Deprecated
    private void removeOrder(Predicate<SortField> filter) {
        for (int i = orders.size() - 1; i >= 0; i--) {
            if (filter.test(orders.get(i))) {
                orders.remove(i);
            }
        }
    }

    /**
     * 添加新的排序条件
     *
     * @param items 条件
     * @return 返回分页参数本身
     */
    public Pageable<T> addOrder(SortField... items) {
        orders.addAll(Arrays.asList(items));
        return this;
    }

    /**
     * 添加新的排序条件
     *
     * @param items 条件
     * @return 返回分页参数本身
     */
    public Pageable<T> addOrder(List<SortField> items) {
        orders.addAll(items);
        return this;
    }


    @Override
    /**
     * orders方法。
     * @return List<SortField>类型返回值
     */
    public List<SortField> orders() {
        return getOrders();
    }

}

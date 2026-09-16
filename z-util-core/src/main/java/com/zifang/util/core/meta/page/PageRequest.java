package com.zifang.util.core.meta.page;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zifang.util.core.meta.BaseRequest;
import com.zifang.util.core.meta.KeepLongSerializer;
import com.zifang.util.core.meta.SortField;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 分页请求
 */
public class PageRequest extends BaseRequest {
    private static final long serialVersionUID = -8059516751569851680L;

    @JsonSerialize(using = KeepLongSerializer.class)
    private Long current = 1L;

    @JsonSerialize(using = KeepLongSerializer.class)
    private Long size = 10L;

    private List<SortField> orders = new ArrayList<>();

    /**
     * PageRequest方法。
     */
    public PageRequest() {
    }

    /**
     * of方法。
     * * @param current long类型参数
     *
     * @param size long类型参数
     * @return static PageRequest类型返回值
     */
    public static PageRequest of(Long current, Long size) {
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(current);
        pageRequest.setSize(size);
        return pageRequest;
    }

    /**
     * of方法。
     * * @param current long类型参数
     *
     * @param size   long类型参数
     * @param orders ListSortField类型参数
     * @return static PageRequest类型返回值
     */
    public static PageRequest of(Long current, Long size, List<SortField> orders) {
        PageRequest pageRequest = of(current, size);
        pageRequest.setOrders(orders);
        return pageRequest;
    }

    /**
     * getCurrent方法。
     *
     * @return long类型返回值
     */
    public Long getCurrent() {
        return current;
    }

    /**
     * setCurrent方法。
     * * @param current long类型参数
     */
    public void setCurrent(Long current) {
        this.current = current;
    }

    /**
     * getSize方法。
     *
     * @return long类型返回值
     */
    public Long getSize() {
        return size;
    }

    /**
     * setSize方法。
     * * @param size long类型参数
     */
    public void setSize(Long size) {
        this.size = size;
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
     * 添加新的排序条件
     *
     * @param items 条件
     * @return 返回分页参数本身
     */
    public PageRequest addOrder(List<SortField> items) {
        orders.addAll(items);
        return this;
    }

    /**
     * 移除符合条件的条件
     *
     * @param filter 条件判断
     */
    public void removeOrder(Predicate<SortField> filter) {
        for (int i = orders.size() - 1; i >= 0; i--) {
            if (filter.test(orders.get(i))) {
                orders.remove(i);
            }
        }
    }

    /**
     * 查找 order 中正序排序的字段数组
     *
     * @param filter 过滤器
     * @return 返回正序排列的字段数组
     */
    public String[] mapOrderToArray(Predicate<SortField> filter) {
        List<String> columns = new ArrayList<>(orders.size());
        orders.forEach(i -> {
            if (filter.test(i)) {
                columns.add(i.getColumn());
            }
        });
        return columns.toArray(new String[0]);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "PageRequest{current=" + current + ", size=" + size + ", orders=" + orders + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRequest that = (PageRequest) o;
        return java.util.Objects.equals(current, that.current) &&
                java.util.Objects.equals(size, that.size) &&
                java.util.Objects.equals(orders, that.orders);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(current, size, orders);
    }
}
package com.zifang.util.core.meta.page;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zifang.util.core.meta.KeepLongSerializer;
import com.zifang.util.core.meta.SortField;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * IPageable接口。
 */
public interface IPageable<T> extends Serializable {

    /**
     * 获取排序信息，排序的字段和正反序
     *
     * @return 排序信息
     */
    List<SortField> orders();

    /**
     * 计算当前分页偏移量
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    default long offset() {
        long current = getCurrent();
        if (current <= 1L) {
            return 0L;
        }
        return (current - 1) * getSize();
    }

    /**
     * 当前分页总页数
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    default long getPages() {
        if (getSize() == 0) {
            return 0L;
        }
        long pages = getTotal() / getSize();
        if (getTotal() % getSize() != 0) {
            pages++;
        }
        return pages;
    }

    /**
     * 内部什么也不干
     * <p>只是为了 json 反序列化时不报错</p>
     */
    default IPageable<T> setPages(long pages) {
        // to do nothing
        return this;
    }

    /**
     * 分页记录列表
     *
     * @return 分页对象记录列表
     */
    List<T> getRecords();

    /**
     * 设置分页记录列表
     */
    IPageable<T> setRecords(List<T> records);

    /**
     * 当前满足条件总行数
     *
     * @return 总条数
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    long getTotal();

    /**
     * 设置当前满足条件总行数
     */
    IPageable<T> setTotal(long total);

    /**
     * 获取每页显示条数
     *
     * @return 每页显示条数
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    long getSize();

    /**
     * 设置每页显示条数
     */
    IPageable<T> setSize(long size);

    /**
     * 当前页
     *
     * @return 当前页
     */
    @JsonSerialize(using = KeepLongSerializer.class)
    long getCurrent();

    /**
     * 设置当前页
     */
    IPageable<T> setCurrent(long current);

    /**
     * IPage 的泛型转换
     *
     * @param mapper 转换函数
     * @param <R>    转换后的泛型
     * @return 转换泛型后的 IPage
     */
    @SuppressWarnings("unchecked")
    default <R> IPageable<R> convert(Function<? super T, ? extends R> mapping) {
        List<T> recordList = this.getRecords();
        if (null == recordList) {
            recordList = Collections.emptyList();
        }
        List<R> collect = recordList.stream().map(mapping).collect(Collectors.toList());
        return ((IPageable<R>) this).setRecords(collect);
    }

}
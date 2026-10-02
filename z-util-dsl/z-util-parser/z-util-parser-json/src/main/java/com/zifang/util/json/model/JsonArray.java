package com.zifang.util.json.model;

import com.zifang.util.json.BeautifyJsonUtils;
import com.zifang.util.json.exception.JsonTypeException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonArray implements Iterable<Object> {

    private List<Object> list = new ArrayList<>();

    /**
     * 向数组中添加一个元素。
     *
     * @param obj 元素，可以是基本类型、String、JsonObject、JsonArray或null
     */
    /**
     * add方法。
     * * @param obj Object类型参数
     */
    public void add(Object obj) {
        list.add(obj);
    }

    /**
     * 根据索引获取元素。
     *
     * @param index 索引位置
     * @return 对应的元素
     */
    /**
     * get方法。
     * * @param index int类型参数
     *
     * @return Object类型返回值
     */
    public Object get(int index) {
        return list.get(index);
    }

    /**
     * 获取数组长度。
     *
     * @return 元素数量
     */
    /**
     * size方法。
     *
     * @return int类型返回值
     */
    public int size() {
        return list.size();
    }

    /**
     * 获取指定索引处的JsonObject元素。
     *
     * @param index 索引位置
     * @return JsonObject元素
     * @throws JsonTypeException 如果该位置不是JsonObject类型
     */
    /**
     * getJsonObject方法。
     * * @param index int类型参数
     *
     * @return JsonObject类型返回值
     */
    public JsonObject getJsonObject(int index) {
        Object obj = list.get(index);
        if (!(obj instanceof JsonObject)) {
            throw new JsonTypeException("Type of value is not JsonObject");
        }

        return (JsonObject) obj;
    }

    /**
     * 获取指定索引处的JsonArray元素。
     *
     * @param index 索引位置
     * @return JsonArray元素
     * @throws JsonTypeException 如果该位置不是JsonArray类型
     */
    /**
     * getJsonArray方法。
     * * @param index int类型参数
     *
     * @return JsonArray类型返回值
     */
    public JsonArray getJsonArray(int index) {
        Object obj = list.get(index);
        if (!(obj instanceof JsonArray)) {
            throw new JsonTypeException("Type of value is not JsonArray");
        }

        return (JsonArray) obj;
    }

    /**
     * 判断数组是否为空。
     *
     * @return 是否为空
     */
    /**
     * isEmpty方法。
     *
     * @return boolean类型返回值
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * 移除指定索引处的元素。
     *
     * @param index 索引位置
     * @return 被移除的元素
     */
    /**
     * remove方法。
     * * @param index int类型参数
     *
     * @return Object类型返回值
     */
    public Object remove(int index) {
        return list.remove(index);
    }

    /**
     * 判断数组是否包含指定元素。
     *
     * @param obj 要查找的元素
     * @return 是否包含
     */
    /**
     * contains方法。
     * * @param obj Object类型参数
     *
     * @return boolean类型返回值
     */
    public boolean contains(Object obj) {
        return list.contains(obj);
    }

    /**
     * 获取指定索引处的 String 值。
     *
     * @param index 索引位置
     * @return String 值，类型不匹配返回 null
     */
    /**
     * getString方法。
     * * @param index int类型参数
     *
     * @return String类型返回值
     */
    public String getString(int index) {
        Object v = list.get(index);
        return v instanceof String ? (String) v : null;
    }

    /**
     * 获取指定索引处的 Integer 值。
     *
     * @param index 索引位置
     * @return Integer 值，类型不匹配返回 null
     */
    /**
     * getInt方法。
     * * @param index int类型参数
     *
     * @return int类型返回值
     */
    public Integer getInt(int index) {
        Object v = list.get(index);
        if (v instanceof Number) return ((Number) v).intValue();
        return null;
    }

    /**
     * 获取指定索引处的 Long 值。
     *
     * @param index 索引位置
     * @return Long 值，类型不匹配返回 null
     */
    /**
     * getLong方法。
     * * @param index int类型参数
     *
     * @return long类型返回值
     */
    public Long getLong(int index) {
        Object v = list.get(index);
        if (v instanceof Number) return ((Number) v).longValue();
        return null;
    }

    /**
     * 获取指定索引处的 Double 值。
     *
     * @param index 索引位置
     * @return Double 值，类型不匹配返回 null
     */
    /**
     * getDouble方法。
     * * @param index int类型参数
     *
     * @return double类型返回值
     */
    public Double getDouble(int index) {
        Object v = list.get(index);
        if (v instanceof Number) return ((Number) v).doubleValue();
        return null;
    }

    /**
     * 获取指定索引处的 Boolean 值。
     *
     * @param index 索引位置
     * @return Boolean 值，类型不匹配返回 null
     */
    /**
     * getBoolean方法。
     * * @param index int类型参数
     *
     * @return boolean类型返回值
     */
    public Boolean getBoolean(int index) {
        Object v = list.get(index);
        if (v instanceof Boolean) return (Boolean) v;
        return null;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return BeautifyJsonUtils.beautify(this);
    }

    /**
     * 返回数组的迭代器。
     *
     * @return 迭代器实例
     */
    /**
     * iterator方法。
     *
     * @return Iterator类型返回值
     */
    public Iterator iterator() {
        return list.iterator();
    }
}

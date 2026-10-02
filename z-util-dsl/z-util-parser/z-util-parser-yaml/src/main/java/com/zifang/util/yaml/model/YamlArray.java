package com.zifang.util.yaml.model;

import com.zifang.util.yaml.define.YamlNodeType;

import java.util.*;

/**
 * YAML 序列节点，对应 YAML 中的序列（sequence）结构，
 * 即有序的元素列表，类似 JSON 中的数组。
 *
 * @author zifang
 */
/**
 * YamlArray类。
 */

/**
 * YamlArray类。
 */
public class YamlArray implements List<Object> {

    private final List<Object> delegate = new ArrayList<>();

    @Override
    /**
     * size方法。
     * @return int类型返回值
     */
    /**
     * size方法。
     * @return int类型返回值
     */
    public int size() {
        return delegate.size();
    }

    @Override
    /**
     * isEmpty方法。
     * @return boolean类型返回值
     */
    /**
     * isEmpty方法。
     * @return boolean类型返回值
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    /**
     * contains方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    /**
     * contains方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    /**
     * get方法。
     *      * @param index int类型参数
     * @return Object类型返回值
     */
    /**
     * get方法。
     *      * @param index int类型参数
     * @return Object类型返回值
     */
    public Object get(int index) {
        return delegate.get(index);
    }

    @Override
    /**
     * add方法。
     *      * @param element Object类型参数
     * @return boolean类型返回值
     */
    /**
     * add方法。
     *      * @param element Object类型参数
     * @return boolean类型返回值
     */
    public boolean add(Object element) {
        return delegate.add(element);
    }

    @Override
    /**
     * set方法。
     *      * @param index int类型参数
     * @param element Object类型参数
     * @return Object类型返回值
     */
    /**
     * set方法。
     *      * @param index int类型参数
     * @param element Object类型参数
     * @return Object类型返回值
     */
    public Object set(int index, Object element) {
        return delegate.set(index, element);
    }

    @Override
    /**
     * add方法。
     *      * @param index int类型参数
     * @param element Object类型参数
     */
    /**
     * add方法。
     *      * @param index int类型参数
     * @param element Object类型参数
     */
    public void add(int index, Object element) {
        delegate.add(index, element);
    }

    @Override
    /**
     * remove方法。
     *      * @param index int类型参数
     * @return Object类型返回值
     */
    /**
     * remove方法。
     *      * @param index int类型参数
     * @return Object类型返回值
     */
    public Object remove(int index) {
        return delegate.remove(index);
    }

    @Override
    /**
     * remove方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    /**
     * remove方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    /**
     * clear方法。
     */
    /**
     * clear方法。
     */
    public void clear() {
        delegate.clear();
    }

    @Override
    /**
     * iterator方法。
     * @return Iterator<Object>类型返回值
     */
    /**
     * iterator方法。
     * @return Iterator<Object>类型返回值
     */
    public Iterator<Object> iterator() {
        return delegate.iterator();
    }

    @Override
    /**
     * toArray方法。
     * @return Object[]类型返回值
     */
    /**
     * toArray方法。
     * @return Object[]类型返回值
     */
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * toArray方法。
     *      * @param a T[]类型参数
     * @return <T> T[]类型返回值
     */
    /**
     * toArray方法。
     *      * @param a T[]类型参数
     * @return <T> T[]类型返回值
     */
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    /**
     * containsAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    /**
     * containsAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    /**
     * addAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    /**
     * addAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    public boolean addAll(Collection<? extends Object> c) {
        return delegate.addAll(c);
    }

    @Override
    /**
     * addAll方法。
     *      * @param index int类型参数
     * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    /**
     * addAll方法。
     *      * @param index int类型参数
     * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    public boolean addAll(int index, Collection<? extends Object> c) {
        return delegate.addAll(index, c);
    }

    @Override
    /**
     * removeAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    /**
     * removeAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    /**
     * retainAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    /**
     * retainAll方法。
     *      * @param c Collection?类型参数
     * @return boolean类型返回值
     */
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    /**
     * indexOf方法。
     *      * @param o Object类型参数
     * @return int类型返回值
     */
    /**
     * indexOf方法。
     *      * @param o Object类型参数
     * @return int类型返回值
     */
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    /**
     * lastIndexOf方法。
     *      * @param o Object类型参数
     * @return int类型返回值
     */
    /**
     * lastIndexOf方法。
     *      * @param o Object类型参数
     * @return int类型返回值
     */
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    /**
     * listIterator方法。
     * @return ListIterator<Object>类型返回值
     */
    /**
     * listIterator方法。
     * @return ListIterator<Object>类型返回值
     */
    public ListIterator<Object> listIterator() {
        return delegate.listIterator();
    }

    @Override
    /**
     * listIterator方法。
     *      * @param index int类型参数
     * @return ListIterator<Object>类型返回值
     */
    /**
     * listIterator方法。
     *      * @param index int类型参数
     * @return ListIterator<Object>类型返回值
     */
    public ListIterator<Object> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    /**
     * subList方法。
     *      * @param fromIndex int类型参数
     * @param toIndex int类型参数
     * @return List<Object>类型返回值
     */
    /**
     * subList方法。
     *      * @param fromIndex int类型参数
     * @param toIndex int类型参数
     * @return List<Object>类型返回值
     */
    public List<Object> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    /**
     * getNodeType方法。
     * @return YamlNodeType类型返回值
     */
    /**
     * getNodeType方法。
     *
     * @return YamlNodeType类型返回值
     */
    public YamlNodeType getNodeType() {
        return YamlNodeType.SEQUENCE;
    }

    /**
     * 获取指定索引的字符串值。
     *
     * @param index 索引
     * @return 字符串值，若不存在或类型不匹配返回 null
     */
    /**
     * getString方法。
     *      * @param index int类型参数
     * @return String类型返回值
     */
    /**
     * getString方法。
     * * @param index int类型参数
     *
     * @return String类型返回值
     */
    public String getString(int index) {
        Object val = get(index);
        return val instanceof String ? (String) val : null;
    }

    /**
     * 获取指定索引的整数值。
     *
     * @param index 索引
     * @return 整数值，若不存在或类型不匹配返回 null
     */
    /**
     * getInt方法。
     *      * @param index int类型参数
     * @return int类型返回值
     */
    /**
     * getInt方法。
     * * @param index int类型参数
     *
     * @return int类型返回值
     */
    public Integer getInt(int index) {
        Object val = get(index);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }

    /**
     * 获取指定索引的嵌套映射。
     *
     * @param index 索引
     * @return YamlMap，若不存在或类型不匹配返回 null
     */
    /**
     * getMap方法。
     *      * @param index int类型参数
     * @return YamlMap类型返回值
     */
    /**
     * getMap方法。
     * * @param index int类型参数
     *
     * @return YamlMap类型返回值
     */
    public YamlMap getMap(int index) {
        Object val = get(index);
        return val instanceof YamlMap ? (YamlMap) val : null;
    }

    /**
     * 获取指定索引的嵌套列表。
     *
     * @param index 索引
     * @return YamlArray，若不存在或类型不匹配返回 null
     */
    /**
     * getArray方法。
     *      * @param index int类型参数
     * @return YamlArray类型返回值
     */
    /**
     * getArray方法。
     * * @param index int类型参数
     *
     * @return YamlArray类型返回值
     */
    public YamlArray getArray(int index) {
        Object val = get(index);
        return val instanceof YamlArray ? (YamlArray) val : null;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return delegate.toString();
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YamlArray)) return false;
        return delegate.equals(((YamlArray) o).delegate);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return delegate.hashCode();
    }
}

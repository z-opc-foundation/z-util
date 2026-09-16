package com.zifang.util.core.pattern.command;

import java.util.*;

/**
 * 命令上下文
 * <p>
 * 携带命令执行所需的数据和状态
 *
 * @author zifang
 */
public class CommandContext implements Map<String, Object> {

    private final Map<String, Object> data;
    private final List<Command<?>> executedCommands;
    private boolean interrupted;

    /**
     * CommandContext方法。
     */
    public CommandContext() {
        this.data = new HashMap<>();
        this.executedCommands = new ArrayList<>();
        this.interrupted = false;
    }

    /**
     * CommandContext方法。
     * * @param initialData MapString,类型参数
     */
    public CommandContext(Map<String, Object> initialData) {
        this.data = new HashMap<>(initialData);
        this.executedCommands = new ArrayList<>();
        this.interrupted = false;
    }

    /**
     * 添加已执行的命令
     */
    public void addExecutedCommand(Command<?> command) {
        this.executedCommands.add(command);
    }

    /**
     * 获取已执行的命令列表
     */
    public List<Command<?>> getExecutedCommands() {
        return Collections.unmodifiableList(executedCommands);
    }

    /**
     * 中断命令执行
     */
    public void interrupt() {
        this.interrupted = true;
    }

    /**
     * 是否被中断
     */
    public boolean isInterrupted() {
        return interrupted;
    }

    /**
     * 获取值并类型转换
     */
    @SuppressWarnings("unchecked")
    /**
     * get方法。
     *      * @param key String类型参数
     * @param type ClassT类型参数
     * @return <T> T类型返回值
     */
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value != null && !type.isInstance(value)) {
            throw new ClassCastException("Value for key " + key + " is not of type " + type);
        }
        return (T) value;
    }

    /**
     * 获取值，带默认值
     */
    public Object getOrDefault(String key, Object defaultValue) {
        Object value = data.get(key);
        return value != null ? value : defaultValue;
    }

    // Map接口实现

    @Override
    /**
     * size方法。
     * @return int类型返回值
     */
    public int size() {
        return data.size();
    }

    @Override
    /**
     * isEmpty方法。
     * @return boolean类型返回值
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    /**
     * containsKey方法。
     *      * @param key Object类型参数
     * @return boolean类型返回值
     */
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    /**
     * containsValue方法。
     *      * @param value Object类型参数
     * @return boolean类型返回值
     */
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    /**
     * get方法。
     *      * @param key Object类型参数
     * @return Object类型返回值
     */
    public Object get(Object key) {
        return data.get(key);
    }

    @Override
    /**
     * put方法。
     *      * @param key String类型参数
     * @param value Object类型参数
     * @return Object类型返回值
     */
    public Object put(String key, Object value) {
        return data.put(key, value);
    }

    @Override
    /**
     * remove方法。
     *      * @param key Object类型参数
     * @return Object类型返回值
     */
    public Object remove(Object key) {
        return data.remove(key);
    }

    @Override
    /**
     * putAll方法。
     *      * @param m Map?类型参数
     */
    public void putAll(Map<? extends String, ?> m) {
        data.putAll(m);
    }

    @Override
    /**
     * clear方法。
     */
    public void clear() {
        data.clear();
    }

    @Override
    /**
     * keySet方法。
     * @return Set<String>类型返回值
     */
    public Set<String> keySet() {
        return data.keySet();
    }

    @Override
    /**
     * values方法。
     * @return Collection<Object>类型返回值
     */
    public Collection<Object> values() {
        return data.values();
    }

    @Override
    /**
     * entrySet方法。
     * @return Set<Entry<String, Object>>类型返回值
     */
    public Set<Entry<String, Object>> entrySet() {
        return data.entrySet();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "CommandContext{" +
                "data=" + data +
                ", executedCommands=" + executedCommands.size() +
                ", interrupted=" + interrupted +
                '}';
    }
}

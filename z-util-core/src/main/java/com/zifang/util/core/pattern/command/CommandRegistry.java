package com.zifang.util.core.pattern.command;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * 命令注册表
 * <p>
 * 提供命令的注册、查找、分类管理功能
 *
 * @author zifang
 */
public class CommandRegistry<C extends CommandContext> {

    private final Map<String, Command<C>> commands;
    private final Map<String, String> categories;
    private final Map<String, String> descriptions;
    private CommandFactory<C> factory;

    /**
     * CommandRegistry方法。
     */
    public CommandRegistry() {
        this.commands = new ConcurrentHashMap<>();
        this.categories = new ConcurrentHashMap<>();
        this.descriptions = new ConcurrentHashMap<>();
        this.factory = new DefaultCommandFactory<>();
    }

    /**
     * 注册命令
     */
    public CommandRegistry<C> register(String name, Command<C> command) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Command name cannot be null or empty");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        commands.put(name, command);
        return this;
    }

    /**
     * 注册命令并设置分类
     */
    public CommandRegistry<C> register(String name, String category, Command<C> command) {
        register(name, command);
        categories.put(name, category);
        return this;
    }

    /**
     * 注册命令并设置描述
     */
    public CommandRegistry<C> register(String name, String category, String description, Command<C> command) {
        register(name, category, command);
        descriptions.put(name, description);
        return this;
    }

    /**
     * 批量注册
     */
    @SafeVarargs
    /**
     * registerAll方法。
     *      * @param pairs PairString,类型参数
     * @return final CommandRegistry<C>类型返回值
     */
    public final CommandRegistry<C> registerAll(Pair<String, Command<C>>... pairs) {
        for (Pair<String, Command<C>> pair : pairs) {
            register(pair.getKey(), pair.getValue());
        }
        return this;
    }

    /**
     * 获取命令
     */
    public Command<C> get(String name) {
        return commands.get(name);
    }

    /**
     * 是否包含命令
     */
    public boolean contains(String name) {
        return commands.containsKey(name);
    }

    /**
     * 移除命令
     */
    public Command<C> remove(String name) {
        categories.remove(name);
        descriptions.remove(name);
        return commands.remove(name);
    }

    /**
     * 获取命令分类
     */
    public String getCategory(String name) {
        return categories.get(name);
    }

    /**
     * 获取命令描述
     */
    public String getDescription(String name) {
        String desc = descriptions.get(name);
        if (desc != null) {
            return desc;
        }
        Command<C> cmd = commands.get(name);
        return cmd != null ? cmd.getDescription() : null;
    }

    /**
     * 获取所有命令名称
     */
    public Set<String> getCommandNames() {
        return Collections.unmodifiableSet(commands.keySet());
    }

    /**
     * 获取所有命令
     */
    public Map<String, Command<C>> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

    /**
     * 按分类获取命令
     */
    public Map<String, Command<C>> getCommandsByCategory(String category) {
        Map<String, Command<C>> result = new HashMap<>();
        for (Map.Entry<String, String> entry : categories.entrySet()) {
            if (category.equals(entry.getValue())) {
                Command<C> cmd = commands.get(entry.getKey());
                if (cmd != null) {
                    result.put(entry.getKey(), cmd);
                }
            }
        }
        return result;
    }

    /**
     * 获取所有分类
     */
    public Set<String> getCategories() {
        return Collections.unmodifiableSet(new HashSet<>(categories.values()));
    }

    /**
     * 按名称过滤器查找命令
     */
    public Map<String, Command<C>> find(Predicate<String> filter) {
        Map<String, Command<C>> result = new HashMap<>();
        for (String name : commands.keySet()) {
            if (filter.test(name)) {
                result.put(name, commands.get(name));
            }
        }
        return result;
    }

    /**
     * 命令数量
     */
    public int size() {
        return commands.size();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    /**
     * 清空所有命令
     */
    public void clear() {
        commands.clear();
        categories.clear();
        descriptions.clear();
    }

    /**
     * 创建命令执行器
     */
    public CommandExecutor<C> executor() {
        return new CommandExecutor<>(this);
    }

    /**
     * 获取命令工厂
     */
    public CommandFactory<C> getFactory() {
        return factory;
    }

    /**
     * 设置命令工厂
     */
    public CommandRegistry<C> setFactory(CommandFactory<C> factory) {
        this.factory = factory;
        return this;
    }

    /**
     * 命令工厂接口
     */
    public interface CommandFactory<C extends CommandContext> {
        Command<C> create(String name);
    }

    /**
     * 默认命令工厂
     */
    private static class DefaultCommandFactory<C extends CommandContext> implements CommandFactory<C> {
        @Override
        /**
         * create方法。
         *      * @param name String类型参数
         * @return Command<C>类型返回值
         */
        public Command<C> create(String name) {
            throw new IllegalStateException("Cannot create command '" + name + "' - no factory configured");
        }
    }

    /**
     * 简单的键值对
     */
    public static class Pair<K, V> {
        private final K key;
        private final V value;

        /**
         * Pair方法。
         * * @param key K类型参数
         *
         * @param value V类型参数
         */
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * of方法。
         * * @param key K类型参数
         *
         * @param value V类型参数
         * @return static <K, V> Pair<K, V>类型返回值
         */
        public static <K, V> Pair<K, V> of(K key, V value) {
            return new Pair<>(key, value);
        }

        /**
         * getKey方法。
         *
         * @return K类型返回值
         */
        public K getKey() {
            return key;
        }

        /**
         * getValue方法。
         *
         * @return V类型返回值
         */
        public V getValue() {
            return value;
        }
    }
}

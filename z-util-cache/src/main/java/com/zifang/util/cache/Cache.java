package com.zifang.util.cache;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * 通用缓存接口（对标 Guava Cache / Caffeine 的核心 API）。
 * <p>
 * 关键能力：
 * <ul>
 *   <li>类型安全：键值由泛型 {@code K}/{@code V} 约束</li>
 *   <li>过期策略：写入过期 {@code expireAfterWrite}、访问过期 {@code expireAfterAccess}</li>
 *   <li>容量限制：超限按 LRU 淘汰</li>
 *   <li>批量操作：{@link #getAllPresent}、{@link #putAll}</li>
 *   <li>移除回调：{@link RemovalListener}</li>
 *   <li>统计：{@link #stats()}</li>
 *   <li>视图：{@link #asMap()}（只读快照）</li>
 * </ul>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface Cache<K, V> {

    /**
     * 根据 key 取值；不存在或已过期返回 null。命中统计 +1。
     */
    V get(K key);

    /**
     * key 存在且未过期返回 true。
     */
    boolean contains(K key);

    /**
     * 取值：未命中时通过 loader 加载并回填。loader 抛错时抛出原始异常。
     * <p>
     * 默认实现可用 {@code getIfPresent} + put 模拟；具体实现可加锁防击穿。
     */
    V get(K key, Function<? super K, ? extends V> loader);

    /**
     * 单 key 批量取。只返回当前在缓存中的 key（不触发 loader）。
     */
    Map<K, V> getAllPresent(Iterable<? extends K> keys);

    /**
     * 存值，使用默认过期策略。覆盖旧值时触发 RemovalListener。
     */
    void put(K key, V value);

    /**
     * 批量存。
     */
    void putAll(Map<? extends K, ? extends V> map);

    /**
     * 主动加载并写缓存。返回值最终落在缓存中（无论是否曾经存在）。
     * 用于提前刷热。
     */
    void put(K key, Function<? super K, ? extends V> loader);

    /**
     * 删除一个 key；不存在返回 false。
     */
    boolean remove(K key);

    /**
     * 清空所有条目（触发 RemovalListener 回调，cause = COLLECTED）。
     */
    void invalidateAll();

    /**
     * 清空所有 key（已废弃的别名，保留向后兼容）。
     */
    void clear();

    /**
     * 当前条目数（不包含已过期但未清理的）。
     */
    long size();

    /**
     * 缓存名。
     */
    String getName();

    /**
     * 统计快照。返回的是实时统计的实现，非副本。
     */
    CacheStats stats();

    /**
     * 缓存内容的视图（{@link Map}）。支持修改（put/remove 会反映到缓存），
     * 但不能添加新 key（违反缓存约束）。注意：返回的 map 不会包含已过期但未清理的条目。
     */
    Map<K, V> asMap();

    /**
     * 监听器（只读）。
     */
    Set<RemovalListener<K, V>> removalListeners();

    /**
     * 关闭后台线程（清理调度器等）。实现保证幂等。
     */
    void shutdown();
}

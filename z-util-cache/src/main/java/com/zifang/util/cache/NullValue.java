package com.zifang.util.cache;

/**
 * 缓存击穿防护哨兵：当 loader 返回 null 时缓存这个 sentinel，避免恶意/不存在 key
 * 反复穿透到下游。调用方应通过 {@link Cache#get(Object)} 的返回值做 null 判断——
 * 建议配合 {@link Cache#contains(Object)} 使用：
 * <pre>{@code
 *   V v = cache.get(key, k -> dao.find(k));
 *   if (v == null && cache.contains(key)) {
 *       // 命中 NullValue，下游确实没有
 *   }
 * }</pre>
 */
public final class NullValue {

    public static final NullValue INSTANCE = new NullValue();

    private NullValue() {
    }

    @Override
    public String toString() {
        return "NullValue";
    }
}

package com.zifang.util.core.feature;

import java.util.Map;
import java.util.Set;

/**
 * 特性开关 SPI（自研，对标 FF4j / Togglz）。
 * <p>
 * 实现：
 * <ul>
 *   <li>{@link InMemoryFeatureStore}：进程内 Map（默认）</li>
 *   <li>用户可自己实现：从 DB / 配置中心 / Redis 拉</li>
 * </ul>
 */
public interface FeatureStore {

    /**
     * 开关是否启用。
     */
    boolean isEnabled(String featureKey);

    /**
     * 开关是否启用 + 检查属性条件（attr != null 时按值匹配）。
     */
    boolean isEnabled(String featureKey, Map<String, String> attributes);

    /**
     * 列出所有特性 key。
     */
    Set<String> featureKeys();
}

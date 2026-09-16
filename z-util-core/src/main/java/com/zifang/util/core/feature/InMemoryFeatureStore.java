package com.zifang.util.core.feature;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 进程内 FeatureStore：Map 存储 + 可选属性匹配。
 */
public class InMemoryFeatureStore implements FeatureStore {

    private final ConcurrentMap<String, FeatureState> features = new ConcurrentHashMap<>();

    public InMemoryFeatureStore define(String key, boolean enabled) {
        return define(key, enabled, Collections.emptyMap());
    }

    public InMemoryFeatureStore define(String key, boolean enabled, Map<String, String> attributes) {
        features.put(key, new FeatureState(enabled, attributes));
        return this;
    }

    public void setEnabled(String key, boolean enabled) {
        FeatureState s = features.get(key);
        if (s != null) s.enabled = enabled;
        else features.put(key, new FeatureState(enabled, Collections.emptyMap()));
    }

    @Override
    public boolean isEnabled(String key) {
        return isEnabled(key, Collections.emptyMap());
    }

    @Override
    public boolean isEnabled(String key, Map<String, String> attributes) {
        FeatureState s = features.get(key);
        if (s == null) return false;
        if (!s.enabled) return false;
        // 属性匹配：所有 required 都要匹配
        for (Map.Entry<String, String> e : s.attributes.entrySet()) {
            String expected = e.getValue();
            String actual = attributes == null ? null : attributes.get(e.getKey());
            if (expected == null ? actual != null : !expected.equals(actual)) return false;
        }
        return true;
    }

    @Override
    public Set<String> featureKeys() {
        return features.keySet();
    }

    private static final class FeatureState {
        final Map<String, String> attributes;
        volatile boolean enabled;

        FeatureState(boolean e, Map<String, String> a) {
            this.enabled = e;
            this.attributes = a;
        }
    }
}

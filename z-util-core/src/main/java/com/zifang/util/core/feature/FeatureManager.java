package com.zifang.util.core.feature;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 特性开关门面（对标 Togglz FeatureManager）。
 * <p>
 * 用法：
 * <pre>{@code
 *   FeatureManager fm = new FeatureManager(new InMemoryFeatureStore());
 *   if (fm.isEnabled("new-checkout")) { ... }
 *   fm.runIf("new-checkout", () -> doStuff());
 * }</pre>
 */
public class FeatureManager {

    private final FeatureStore store;

    public FeatureManager(FeatureStore store) {
        this.store = store;
    }

    public boolean isEnabled(String key) {
        return store.isEnabled(key);
    }

    public boolean isEnabled(String key, Map<String, String> attributes) {
        return store.isEnabled(key, attributes);
    }

    /**
     * 启用则执行 task，否则返回 fallback（或 null）。
     */
    public <T> T runIf(String key, Supplier<T> task, Supplier<T> fallback) {
        return isEnabled(key) ? task.get() : (fallback == null ? null : fallback.get());
    }

    public void runIf(String key, Runnable task, Runnable fallback) {
        if (isEnabled(key)) task.run();
        else if (fallback != null) fallback.run();
    }

    public <T> T runIf(String key, Map<String, String> attributes, Supplier<T> task, Supplier<T> fallback) {
        return isEnabled(key, attributes) ? task.get() : (fallback == null ? null : fallback.get());
    }

    public FeatureStore store() {
        return store;
    }
}

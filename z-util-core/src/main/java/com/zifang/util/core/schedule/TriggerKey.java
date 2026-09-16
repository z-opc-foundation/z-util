package com.zifang.util.core.schedule;

import java.util.Objects;

/**
 * 自研触发器标识，由 name + group 唯一确定。
 * 对标 {@code org.quartz.TriggerKey}，不依赖 Quartz。
 */
public final class TriggerKey {

    public static final String DEFAULT_GROUP = "DEFAULT";

    private final String name;
    private final String group;

    private TriggerKey(String name, String group) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.group = group == null ? DEFAULT_GROUP : group;
    }

    public static TriggerKey triggerKey(String name, String group) { return new TriggerKey(name, group); }
    public static TriggerKey triggerKey(String name) { return new TriggerKey(name, DEFAULT_GROUP); }

    public String getName() { return name; }
    public String getGroup() { return group; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TriggerKey)) return false;
        TriggerKey other = (TriggerKey) o;
        return name.equals(other.name) && group.equals(other.group);
    }
    @Override public int hashCode() { return Objects.hash(name, group); }
    @Override public String toString() { return group + "." + name; }
}

package com.zifang.util.core.schedule;

import java.util.Map;
import java.util.Objects;

/**
 * 自研任务标识，由 name + group 唯一确定。
 * 对标 {@code org.quartz.JobKey}，不依赖 Quartz。
 */
public final class JobKey {

    public static final String DEFAULT_GROUP = "DEFAULT";

    private final String name;
    private final String group;

    private JobKey(String name, String group) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.group = group == null ? DEFAULT_GROUP : group;
    }

    public static JobKey jobKey(String name, String group) {
        return new JobKey(name, group);
    }

    public static JobKey jobKey(String name) {
        return new JobKey(name, DEFAULT_GROUP);
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getFullName() {
        return group + "." + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobKey)) return false;
        JobKey other = (JobKey) o;
        return name.equals(other.name) && group.equals(other.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, group);
    }

    @Override
    public String toString() {
        return group + "." + name;
    }
}

package com.zifang.util.core.schedule;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 任务定义（不可变）。对标 {@code org.quartz.JobDetail}。
 */
public final class JobDetail {

    private final JobKey key;
    private final Class<? extends Job> jobClass;
    private final JobDataMap jobDataMap;
    private final String description;
    private final boolean durable;
    private final boolean concurrent;

    private JobDetail(Builder b) {
        this.key = Objects.requireNonNull(b.key, "job key must not be null");
        this.jobClass = Objects.requireNonNull(b.jobClass, "jobClass must not be null");
        this.jobDataMap = new JobDataMap(b.jobDataMap);
        this.description = b.description;
        this.durable = b.durable;
        this.concurrent = b.concurrent;
    }

    public JobKey getKey() {
        return key;
    }

    public Class<? extends Job> getJobClass() {
        return jobClass;
    }

    public JobDataMap getJobDataMap() {
        return new JobDataMap(jobDataMap);
    }

    public String getDescription() {
        return description;
    }

    public boolean isDurable() {
        return durable;
    }

    public boolean isConcurrentExecutionDisallowed() {
        return !concurrent;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private JobKey key;
        private Class<? extends Job> jobClass;
        private Map<String, Object> jobDataMap = new HashMap<>();
        private String description;
        private boolean durable;
        private boolean concurrent = true;

        public Builder withIdentity(JobKey key) {
            this.key = key;
            return this;
        }

        public Builder withIdentity(String name, String group) {
            this.key = JobKey.jobKey(name, group);
            return this;
        }

        public Builder withIdentity(String name) {
            this.key = JobKey.jobKey(name);
            return this;
        }

        public Builder ofType(Class<? extends Job> jobClass) {
            this.jobClass = jobClass;
            return this;
        }

        public Builder usingJobData(String key, Object value) {
            this.jobDataMap.put(key, value);
            return this;
        }

        public Builder usingJobData(Map<String, ?> data) {
            if (data != null) {
                this.jobDataMap.putAll(data);
            }
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder storeDurably() {
            this.durable = true;
            return this;
        }

        public Builder nonConcurrent() {
            this.concurrent = false;
            return this;
        }

        public JobDetail build() {
            return new JobDetail(this);
        }
    }
}

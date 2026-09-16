package com.zifang.util.core.schedule;

/**
 * 自研日历间隔触发器（按月/周的固定间隔）。简化版实现：
 * <p>
 * 注意：自研版本仅支持按"天"为最小单位的间隔，内部委托给
 * {@link SimpleTrigger} 实现近似语义。
 */
public final class CalendarIntervalTrigger implements Trigger {

    private final SimpleTrigger delegate;

    private CalendarIntervalTrigger(SimpleTrigger delegate) {
        this.delegate = delegate;
    }

    public long getIntervalInDays() {
        return delegate.getIntervalMillis() / 86_400_000L;
    }

    public SimpleTrigger getDelegate() {
        return delegate;
    }

    @Override
    public TriggerKey getKey() {
        return delegate.getKey();
    }

    @Override
    public JobKey getJobKey() {
        return delegate.getJobKey();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public java.util.Date getNextFireTime() {
        return delegate.getNextFireTime();
    }

    @Override
    public void setNextFireTime(java.util.Date nextFireTime) {
        delegate.setNextFireTime(nextFireTime);
    }

    @Override
    public java.util.Date getPreviousFireTime() {
        return delegate.getPreviousFireTime();
    }

    @Override
    public void setPreviousFireTime(java.util.Date previousFireTime) {
        delegate.setPreviousFireTime(previousFireTime);
    }

    @Override
    public int getPriority() {
        return delegate.getPriority();
    }

    @Override
    public java.util.Date getStartTime() {
        return delegate.getStartTime();
    }

    @Override
    public java.util.Date getEndTime() {
        return delegate.getEndTime();
    }

    @Override
    public MisfirePolicy getMisfirePolicy() {
        return delegate.getMisfirePolicy();
    }

    @Override
    public String getCalendarName() {
        return delegate.getCalendarName();
    }

    @Override
    public java.util.TimeZone getTimeZone() {
        return delegate.getTimeZone();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private TriggerKey key;
        private JobKey jobKey;
        private String description;
        private long intervalDays = 1;
        private int repeatCount = 0;
        private boolean repeatForever;
        private java.util.Date startTime = new java.util.Date();
        private java.util.Date endTime;
        private MisfirePolicy misfirePolicy = MisfirePolicy.SMART_POLICY;

        public Builder withIdentity(TriggerKey key) {
            this.key = key;
            return this;
        }

        public Builder withIdentity(String name, String group) {
            this.key = TriggerKey.triggerKey(name, group);
            return this;
        }

        public Builder forJob(JobKey jobKey) {
            this.jobKey = jobKey;
            return this;
        }

        public Builder forJob(String name, String group) {
            this.jobKey = JobKey.jobKey(name, group);
            return this;
        }

        public Builder forJob(String name) {
            this.jobKey = JobKey.jobKey(name);
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withIntervalInDays(int days) {
            this.intervalDays = days;
            return this;
        }

        public Builder withRepeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
            return this;
        }

        public Builder repeatForever() {
            this.repeatForever = true;
            return this;
        }

        public Builder startAt(java.util.Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder startNow() {
            this.startTime = new java.util.Date();
            return this;
        }

        public Builder endAt(java.util.Date endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder withMisfirePolicy(MisfirePolicy policy) {
            this.misfirePolicy = policy;
            return this;
        }

        public CalendarIntervalTrigger build() {
            SimpleTrigger.Builder b = SimpleTrigger.newBuilder()
                    .withIdentity(key)
                    .forJob(jobKey)
                    .withIntervalInMilliseconds(intervalDays * 86_400_000L)
                    .withRepeatCount(repeatForever ? -1 : repeatCount)
                    .startAt(startTime)
                    .withMisfirePolicy(misfirePolicy);
            if (description != null) b.withDescription(description);
            if (endTime != null) b.endAt(endTime);
            return new CalendarIntervalTrigger(b.build());
        }
    }
}

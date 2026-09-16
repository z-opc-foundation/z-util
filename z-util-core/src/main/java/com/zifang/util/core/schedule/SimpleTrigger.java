package com.zifang.util.core.schedule;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * 自研简单触发器：按固定间隔重复执行。
 */
public final class SimpleTrigger implements Trigger {

    private final TriggerKey key;
    private final JobKey jobKey;
    private final String description;
    private final long intervalMillis;
    private final int repeatCount;
    private final Date startTime;
    private final Date endTime;
    private final int priority;
    private final MisfirePolicy misfirePolicy;
    private final TimeZone timeZone;

    private volatile Date previousFireTime;
    private volatile Date nextFireTime;
    private int timesTriggered = 0;

    private SimpleTrigger(Builder b) {
        this.key = b.key; this.jobKey = b.jobKey; this.description = b.description;
        this.intervalMillis = b.intervalMillis; this.repeatCount = b.repeatCount;
        this.startTime = b.startTime; this.endTime = b.endTime;
        this.priority = b.priority; this.misfirePolicy = b.misfirePolicy;
        this.timeZone = b.timeZone == null ? TimeZone.getDefault() : b.timeZone;
        this.nextFireTime = b.startTime;
    }

    public long getIntervalMillis() { return intervalMillis; }
    public int getRepeatCount() { return repeatCount; }
    public int getTimesTriggered() { return timesTriggered; }

    @Override public TriggerKey getKey() { return key; }
    @Override public JobKey getJobKey() { return jobKey; }
    @Override public String getDescription() { return description; }
    @Override public Date getNextFireTime() { return nextFireTime; }
    @Override public void setNextFireTime(Date nextFireTime) { this.nextFireTime = nextFireTime; }
    @Override public Date getPreviousFireTime() { return previousFireTime; }
    @Override public void setPreviousFireTime(Date previousFireTime) { this.previousFireTime = previousFireTime; }
    @Override public int getPriority() { return priority; }
    @Override public Date getStartTime() { return startTime; }
    @Override public Date getEndTime() { return endTime; }
    @Override public MisfirePolicy getMisfirePolicy() { return misfirePolicy; }
    @Override public String getCalendarName() { return null; }
    @Override public TimeZone getTimeZone() { return timeZone; }

    public void incrementTimesTriggered() { this.timesTriggered++; }

    public boolean mayFireAgain() {
        if (repeatCount == -1) return true;
        return timesTriggered < repeatCount + 1;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private TriggerKey key; private JobKey jobKey; private String description;
        private long intervalMillis = 0; private int repeatCount = 0;
        private Date startTime = new Date(); private Date endTime;
        private int priority = 5; private MisfirePolicy misfirePolicy = MisfirePolicy.SMART_POLICY;
        private TimeZone timeZone;

        public Builder withIdentity(TriggerKey key) { this.key = key; return this; }
        public Builder withIdentity(String name, String group) { this.key = TriggerKey.triggerKey(name, group); return this; }
        public Builder forJob(JobKey jobKey) { this.jobKey = jobKey; return this; }
        public Builder forJob(String name, String group) { this.jobKey = JobKey.jobKey(name, group); return this; }
        public Builder forJob(String name) { this.jobKey = JobKey.jobKey(name); return this; }
        public Builder withDescription(String description) { this.description = description; return this; }
        public Builder withInterval(long time, TimeUnit unit) { this.intervalMillis = unit.toMillis(time); return this; }
        public Builder withIntervalInMilliseconds(long intervalMillis) { this.intervalMillis = intervalMillis; return this; }
        public Builder withIntervalInSeconds(int seconds) { return withIntervalInMilliseconds(seconds * 1000L); }
        public Builder withIntervalInMinutes(int minutes) { return withIntervalInMilliseconds(minutes * 60_000L); }
        public Builder withIntervalInHours(int hours) { return withIntervalInMilliseconds(hours * 3_600_000L); }
        public Builder withRepeatCount(int repeatCount) { this.repeatCount = repeatCount; return this; }
        public Builder repeatForever() { this.repeatCount = -1; return this; }
        public Builder startAt(Date startTime) { this.startTime = startTime; return this; }
        public Builder startNow() { this.startTime = new Date(); return this; }
        public Builder endAt(Date endTime) { this.endTime = endTime; return this; }
        public Builder withPriority(int priority) { this.priority = priority; return this; }
        public Builder withMisfirePolicy(MisfirePolicy policy) { this.misfirePolicy = policy; return this; }
        public Builder inTimeZone(TimeZone timeZone) { this.timeZone = timeZone; return this; }

        public SimpleTrigger build() {
            if (key == null) throw new IllegalStateException("TriggerKey must be set");
            if (jobKey == null) throw new IllegalStateException("JobKey must be set");
            return new SimpleTrigger(this);
        }
    }
}

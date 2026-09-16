package com.zifang.util.core.schedule;

import java.util.concurrent.TimeUnit;

/**
 * 自研 TriggerBuilder。提供 fluent API 创建 SimpleTrigger / CronTrigger。
 */
public final class TriggerBuilder {

    private final TriggerKey key;
    private final JobKey jobKey;
    private final String description;
    private long intervalMillis = 0;
    private int repeatCount = 0;
    private boolean repeatForever;
    private String cron;
    private java.util.Date startTime = new java.util.Date();
    private java.util.Date endTime;
    private MisfirePolicy misfirePolicy = MisfirePolicy.SMART_POLICY;
    private java.util.TimeZone timeZone;

    private TriggerBuilder(TriggerKey key, JobKey jobKey, String description) {
        this.key = key; this.jobKey = jobKey; this.description = description;
    }

    public static TriggerBuilder newTrigger() { return new TriggerBuilder(null, null, null); }
    public static TriggerBuilder newSimpleTrigger() { return new TriggerBuilder(null, null, null); }
    public static TriggerBuilder newCronTrigger() { return new TriggerBuilder(null, null, null); }

    public TriggerBuilder withIdentity(TriggerKey key) { return new TriggerBuilder(key, this.jobKey, this.description); }
    public TriggerBuilder withIdentity(String name, String group) { return new TriggerBuilder(TriggerKey.triggerKey(name, group), this.jobKey, this.description); }
    public TriggerBuilder withIdentity(String name) { return new TriggerBuilder(TriggerKey.triggerKey(name), this.jobKey, this.description); }
    public TriggerBuilder forJob(JobKey jobKey) { return new TriggerBuilder(this.key, jobKey, this.description); }
    public TriggerBuilder forJob(String name, String group) { return new TriggerBuilder(this.key, JobKey.jobKey(name, group), this.description); }
    public TriggerBuilder forJob(String name) { return new TriggerBuilder(this.key, JobKey.jobKey(name), this.description); }
    public TriggerBuilder withDescription(String description) { return new TriggerBuilder(this.key, this.jobKey, description); }

    public TriggerBuilder withInterval(long time, TimeUnit unit) { this.intervalMillis = unit.toMillis(time); this.cron = null; return this; }
    public TriggerBuilder withIntervalInMilliseconds(long millis) { return withInterval(millis, TimeUnit.MILLISECONDS); }
    public TriggerBuilder withIntervalInSeconds(int seconds) { return withInterval(seconds, TimeUnit.SECONDS); }
    public TriggerBuilder withIntervalInMinutes(int minutes) { return withInterval(minutes, TimeUnit.MINUTES); }
    public TriggerBuilder withIntervalInHours(int hours) { return withInterval(hours, TimeUnit.HOURS); }
    public TriggerBuilder withRepeatCount(int repeatCount) { this.repeatCount = repeatCount; return this; }
    public TriggerBuilder repeatForever() { this.repeatForever = true; return this; }
    public TriggerBuilder withCron(String cron) { this.cron = cron; this.intervalMillis = 0; return this; }
    public TriggerBuilder withCron(String cron, java.util.TimeZone timeZone) { this.cron = cron; this.timeZone = timeZone; this.intervalMillis = 0; return this; }
    public TriggerBuilder startAt(java.util.Date startTime) { this.startTime = startTime; return this; }
    public TriggerBuilder startNow() { this.startTime = new java.util.Date(); return this; }
    public TriggerBuilder endAt(java.util.Date endTime) { this.endTime = endTime; return this; }
    public TriggerBuilder withMisfirePolicy(MisfirePolicy policy) { this.misfirePolicy = policy; return this; }
    public TriggerBuilder inTimeZone(java.util.TimeZone timeZone) { this.timeZone = timeZone; return this; }

    public Trigger build() {
        if (key == null) throw new IllegalStateException("TriggerKey must be set");
        if (jobKey == null) throw new IllegalStateException("JobKey must be set");
        if (cron != null) {
            CronTrigger.Builder b = CronTrigger.newBuilder()
                    .withIdentity(key).forJob(jobKey)
                    .withCron(cron, timeZone).startAt(startTime).withMisfirePolicy(misfirePolicy);
            if (description != null) b.withDescription(description);
            if (endTime != null) b.endAt(endTime);
            return b.build();
        }
        SimpleTrigger.Builder b = SimpleTrigger.newBuilder()
                .withIdentity(key).forJob(jobKey)
                .withIntervalInMilliseconds(intervalMillis)
                .withRepeatCount(repeatForever ? -1 : repeatCount)
                .startAt(startTime).withMisfirePolicy(misfirePolicy);
        if (description != null) b.withDescription(description);
        if (endTime != null) b.endAt(endTime);
        if (timeZone != null) b.inTimeZone(timeZone);
        return b.build();
    }
}

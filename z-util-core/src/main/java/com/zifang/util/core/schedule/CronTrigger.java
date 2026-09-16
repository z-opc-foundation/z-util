package com.zifang.util.core.schedule;

import java.util.Date;
import java.util.TimeZone;

/**
 * 自研 Cron 触发器。
 */
public final class CronTrigger implements Trigger {

    private final TriggerKey key;
    private final JobKey jobKey;
    private final String description;
    private final CronExpression cronExpression;
    private final Date startTime;
    private final Date endTime;
    private final int priority;
    private final MisfirePolicy misfirePolicy;
    private final TimeZone timeZone;
    private volatile Date nextFireTime;
    private volatile Date previousFireTime;

    private CronTrigger(Builder b) {
        this.key = b.key; this.jobKey = b.jobKey; this.description = b.description;
        this.cronExpression = b.cronExpression;
        this.startTime = b.startTime; this.endTime = b.endTime;
        this.priority = b.priority; this.misfirePolicy = b.misfirePolicy;
        this.timeZone = b.cronExpression.getTimeZone();
        this.nextFireTime = b.cronExpression.getNextValidTimeAfter(b.startTime);
    }

    public CronExpression getCronExpression() { return cronExpression; }
    public String getCron() { return cronExpression.getExpression(); }

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

    public void advanceToNextFireTime() {
        Date now = new Date();
        Date next = cronExpression.getNextValidTimeAfter(previousFireTime == null ? now : previousFireTime);
        if (next != null && endTime != null && next.after(endTime)) next = null;
        this.nextFireTime = next;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private TriggerKey key; private JobKey jobKey; private String description;
        private CronExpression cronExpression;
        private Date startTime = new Date(); private Date endTime;
        private int priority = 5; private MisfirePolicy misfirePolicy = MisfirePolicy.SMART_POLICY;

        public Builder withIdentity(TriggerKey key) { this.key = key; return this; }
        public Builder withIdentity(String name, String group) { this.key = TriggerKey.triggerKey(name, group); return this; }
        public Builder forJob(JobKey jobKey) { this.jobKey = jobKey; return this; }
        public Builder forJob(String name, String group) { this.jobKey = JobKey.jobKey(name, group); return this; }
        public Builder forJob(String name) { this.jobKey = JobKey.jobKey(name); return this; }
        public Builder withDescription(String description) { this.description = description; return this; }
        public Builder withCron(String cron) {
            try { this.cronExpression = new CronExpression(cron); }
            catch (Exception e) { throw new IllegalArgumentException("Invalid cron: " + cron, e); }
            return this;
        }
        public Builder withCron(String cron, TimeZone timeZone) {
            try { this.cronExpression = new CronExpression(cron, timeZone); }
            catch (Exception e) { throw new IllegalArgumentException("Invalid cron: " + cron, e); }
            return this;
        }
        public Builder startAt(Date startTime) { this.startTime = startTime; return this; }
        public Builder startNow() { this.startTime = new Date(); return this; }
        public Builder endAt(Date endTime) { this.endTime = endTime; return this; }
        public Builder withPriority(int priority) { this.priority = priority; return this; }
        public Builder withMisfirePolicy(MisfirePolicy policy) { this.misfirePolicy = policy; return this; }

        public CronTrigger build() {
            if (key == null) throw new IllegalStateException("TriggerKey must be set");
            if (jobKey == null) throw new IllegalStateException("JobKey must be set");
            if (cronExpression == null) throw new IllegalStateException("Cron expression must be set");
            return new CronTrigger(this);
        }
    }
}

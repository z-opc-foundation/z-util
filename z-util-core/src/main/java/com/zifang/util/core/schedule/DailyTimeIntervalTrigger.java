package com.zifang.util.core.schedule;

/**
 * 自研每日时间区间触发器：每周某几天、每天某段时间重复。
 * <p>
 * 简化版：内部使用 {@link CronTrigger} 实现（每天指定小时段）。
 */
public final class DailyTimeIntervalTrigger implements Trigger {

    private final CronTrigger delegate;

    private DailyTimeIntervalTrigger(CronTrigger delegate) {
        this.delegate = delegate;
    }

    public CronTrigger getDelegate() {
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
        private int startHour = 9;
        private int endHour = 17;
        private int intervalMinutes = 60;
        private String daysOfWeek = "MON-FRI";
        private int repeatCount = -1;
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

        public Builder startingDailyAt(int hour, int minute) {
            this.startHour = hour;
            return this;
        }

        public Builder endingDailyAt(int hour, int minute) {
            this.endHour = hour;
            return this;
        }

        public Builder withInterval(int interval, java.util.concurrent.TimeUnit unit) {
            this.intervalMinutes = (int) unit.toMinutes(interval);
            return this;
        }

        public Builder withIntervalInMinutes(int minutes) {
            this.intervalMinutes = minutes;
            return this;
        }

        public Builder onDaysOfWeek(java.util.Set<java.time.DayOfWeek> days) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (java.time.DayOfWeek d : days) {
                if (i++ > 0) sb.append(",");
                sb.append(quartzDayName(d));
            }
            this.daysOfWeek = sb.toString();
            return this;
        }

        public Builder withRepeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
            return this;
        }

        public Builder startAt(java.util.Date startTime) {
            this.startTime = startTime;
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

        private static String quartzDayName(java.time.DayOfWeek d) {
            switch (d) {
                case MONDAY: return "2";
                case TUESDAY: return "3";
                case WEDNESDAY: return "4";
                case THURSDAY: return "5";
                case FRIDAY: return "6";
                case SATURDAY: return "7";
                case SUNDAY: return "1";
                default: return "?";
            }
        }

        public DailyTimeIntervalTrigger build() {
            String cron = String.format("0 %d/%d %d-%d * * %s",
                    0, intervalMinutes, startHour, endHour, daysOfWeek);
            CronTrigger.Builder b = CronTrigger.newBuilder()
                    .withIdentity(key)
                    .forJob(jobKey)
                    .withCron(cron)
                    .startAt(startTime)
                    .withMisfirePolicy(misfirePolicy);
            if (description != null) b.withDescription(description);
            if (endTime != null) b.endAt(endTime);
            return new DailyTimeIntervalTrigger(b.build());
        }
    }
}

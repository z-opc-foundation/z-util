package com.zifang.util.core.schedule;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 自研日历抽象。对标 {@code org.quartz.Calendar}。
 */
public abstract class ScheduleCalendar {

    private String name;
    private TimeZone timeZone;
    private String description;

    public ScheduleCalendar() { this.timeZone = TimeZone.getDefault(); }
    public ScheduleCalendar(TimeZone timeZone) { this.timeZone = timeZone; }

    public abstract boolean isTimeIncluded(long timeStamp);
    public abstract java.util.Calendar getBaseCalendar();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TimeZone getTimeZone() { return timeZone; }
    public void setTimeZone(TimeZone timeZone) { this.timeZone = timeZone; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static class AlwaysIncludedCalendar extends ScheduleCalendar {
        @Override public boolean isTimeIncluded(long timeStamp) { return true; }
        @Override public java.util.Calendar getBaseCalendar() { return Calendar.getInstance(getTimeZone()); }
    }

    public static class WeeklyExcludeCalendar extends ScheduleCalendar {
        private final boolean[] excludeDays = new boolean[8];
        public WeeklyExcludeCalendar() {}
        public void excludeDay(int dayOfWeek) { excludeDays[dayOfWeek] = true; }
        public void excludeSaturday() { excludeDays[Calendar.SATURDAY] = true; excludeDays[Calendar.SUNDAY] = true; }
        @Override public boolean isTimeIncluded(long timeStamp) {
            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.setTimeInMillis(timeStamp);
            return !excludeDays[cal.get(Calendar.DAY_OF_WEEK)];
        }
        @Override public java.util.Calendar getBaseCalendar() { return Calendar.getInstance(getTimeZone()); }
    }
}

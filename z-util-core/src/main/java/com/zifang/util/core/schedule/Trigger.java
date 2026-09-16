package com.zifang.util.core.schedule;

import java.util.Date;
import java.util.TimeZone;

/**
 * 自研触发器抽象。对标 {@code org.quartz.Trigger}。
 */
public interface Trigger {
    TriggerKey getKey();
    JobKey getJobKey();
    String getDescription();
    Date getNextFireTime();
    void setNextFireTime(Date nextFireTime);
    Date getPreviousFireTime();
    void setPreviousFireTime(Date previousFireTime);
    int getPriority();
    Date getStartTime();
    Date getEndTime();
    MisfirePolicy getMisfirePolicy();
    String getCalendarName();
    TimeZone getTimeZone();
}

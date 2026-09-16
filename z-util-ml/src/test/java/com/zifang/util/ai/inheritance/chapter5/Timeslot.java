package com.zifang.util.ai.inheritance.chapter5;

/**
 * Simple timeslot abstraction -- just represents a timeslot (like "Wed 9:00am-11:00am").
 *
 * @author zifang
 */

/**
 * Timeslot类。
 */
public class Timeslot {
    private final int timeslotId;
    private final String timeslot;

    /**
     * Initalize new Timeslot
     *
     * @param timeslotId The ID for this timeslot
     * @param timeslot   The timeslot being initalized
     */
    /**
     * Timeslot方法。
     * * @param timeslotId int类型参数
     *
     * @param timeslot String类型参数
     */
    public Timeslot(int timeslotId, String timeslot) {
        this.timeslotId = timeslotId;
        this.timeslot = timeslot;
    }

    /**
     * Returns the timeslotId
     *
     * @return timeslotId
     */
    /**
     * getTimeslotId方法。
     *
     * @return int类型返回值
     */
    public int getTimeslotId() {
        return this.timeslotId;
    }

    /**
     * Returns the timeslot
     *
     * @return timeslot
     */
    /**
     * getTimeslot方法。
     *
     * @return String类型返回值
     */
    public String getTimeslot() {
        return this.timeslot;
    }
}

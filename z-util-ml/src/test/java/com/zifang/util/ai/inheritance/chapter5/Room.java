package com.zifang.util.ai.inheritance.chapter5;

/**
 * Simple Room abstraction -- used to store the room capacity and compare against the student Group's size.
 */

/**
 * Room类。
 */
public class Room {
    private final int roomId;
    private final String roomNumber;
    private final int capacity;

    /**
     * Initialize new Room
     *
     * @param roomId     The ID for this classroom
     * @param roomNumber The room number
     * @param capacity   The room capacity
     */
    /**
     * Room方法。
     * * @param roomId int类型参数
     *
     * @param roomNumber String类型参数
     * @param capacity   int类型参数
     */
    public Room(int roomId, String roomNumber, int capacity) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
    }

    /**
     * Return roomId
     *
     * @return roomId
     */
    /**
     * getRoomId方法。
     *
     * @return int类型返回值
     */
    public int getRoomId() {
        return this.roomId;
    }

    /**
     * Return room number
     *
     * @return roomNumber
     */
    /**
     * getRoomNumber方法。
     *
     * @return String类型返回值
     */
    public String getRoomNumber() {
        return this.roomNumber;
    }

    /**
     * Return room capacity
     *
     * @return capacity
     */
    /**
     * getRoomCapacity方法。
     *
     * @return int类型返回值
     */
    public int getRoomCapacity() {
        return this.capacity;
    }
}
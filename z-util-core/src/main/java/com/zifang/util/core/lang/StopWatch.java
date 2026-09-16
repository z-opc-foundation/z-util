package com.zifang.util.core.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * StopWatch 性能计时工具（对标 Spring StopWatch / Apache Commons StopWatch）。
 * <p>
 * 支持分段计时，便于在日志中输出每一步耗时。
 * <p>
 * 用法：
 * <pre>{@code
 *   StopWatch sw = new StopWatch("order-pipeline");
 *   sw.start("step1");
 *   doStep1();
 *   sw.stop();
 *   sw.start("step2");
 *   doStep2();
 *   sw.stop();
 *   System.out.println(sw.prettyPrint());
 * }</pre>
 *
 * @author zifang
 */
public class StopWatch {

    private final String id;
    private final List<TaskInfo> taskList = new ArrayList<>();
    private TaskInfo currentTask;
    private long startTimeNanos;
    private long totalTimeNanos;
    private boolean running;

    public StopWatch() {
        this("");
    }

    public StopWatch(String id) {
        this.id = id;
    }

    public void start(String taskName) throws IllegalStateException {
        if (running) {
            throw new IllegalStateException("StopWatch is already running. Stop current task first.");
        }
        currentTask = new TaskInfo(taskName);
        startTimeNanos = System.nanoTime();
        running = true;
    }

    public void stop() throws IllegalStateException {
        if (!running) {
            throw new IllegalStateException("StopWatch is not running. Start a task first.");
        }
        long elapsed = System.nanoTime() - startTimeNanos;
        currentTask.setTimeNanos(elapsed);
        taskList.add(currentTask);
        totalTimeNanos += elapsed;
        running = false;
        currentTask = null;
    }

    /**
     * 统计自开始以来总耗时（包含未结束的 task）。
     */
    public long getTotalTimeNanos() {
        if (running) {
            return System.nanoTime() - startTimeNanos + totalTimeNanos;
        }
        return totalTimeNanos;
    }

    public long getTotalTimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(getTotalTimeNanos());
    }

    public boolean isRunning() {
        return running;
    }

    public String getId() {
        return id;
    }

    public TaskInfo[] getTaskInfo() {
        return taskList.toArray(new TaskInfo[0]);
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append("StopWatch '").append(id).append("': running time = ")
                .append(getTotalTimeNanos()).append(" ns\n");
        if (!taskList.isEmpty()) {
            sb.append("---------------------------------------------\n");
            sb.append("ms        %     Task name\n");
            sb.append("---------------------------------------------\n");
            long total = getTotalTimeNanos();
            for (TaskInfo t : taskList) {
                long ms = TimeUnit.NANOSECONDS.toMillis(t.getTimeNanos());
                double pct = total == 0 ? 0.0 : (t.getTimeNanos() * 100.0 / total);
                sb.append(String.format("%-9d%-6.2f%s%n", ms, pct, t.getTaskName()));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return prettyPrint();
    }

    public static class TaskInfo {
        private final String taskName;
        private long timeNanos;

        TaskInfo(String taskName) {
            this.taskName = taskName;
        }

        public String getTaskName() {
            return taskName;
        }

        public long getTimeNanos() {
            return timeNanos;
        }

        void setTimeNanos(long nanos) {
            this.timeNanos = nanos;
        }

        public long getTimeMillis() {
            return TimeUnit.NANOSECONDS.toMillis(timeNanos);
        }
    }
}
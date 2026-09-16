package com.zifang.util.core.schedule.listener;

import com.zifang.util.core.schedule.JobExecutionContext;
import com.zifang.util.core.schedule.JobKey;
import com.zifang.util.core.schedule.Trigger;
import com.zifang.util.core.schedule.TriggerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 自研监听器管理器。
 */
public class ListenerManager {

    private final List<JobListenerEntry> jobListeners = new CopyOnWriteArrayList<>();
    private final List<TriggerListenerEntry> triggerListeners = new CopyOnWriteArrayList<>();
    private final List<SchedulerListener> schedulerListeners = new CopyOnWriteArrayList<>();

    public void addJobListener(JobListener listener) { addJobListener(listener, Matcher.anyJobs()); }
    public void addJobListener(JobListener listener, Matcher matcher) { jobListeners.add(new JobListenerEntry(listener, matcher)); }
    public void addTriggerListener(TriggerListener listener) { addTriggerListener(listener, Matcher.anyTriggers()); }
    public void addTriggerListener(TriggerListener listener, Matcher matcher) { triggerListeners.add(new TriggerListenerEntry(listener, matcher)); }
    public void addSchedulerListener(SchedulerListener listener) { schedulerListeners.add(listener); }

    public boolean removeJobListener(String name) { return jobListeners.removeIf(e -> e.listener.getName().equals(name)); }
    public boolean removeTriggerListener(String name) { return triggerListeners.removeIf(e -> e.listener.getName().equals(name)); }
    public boolean removeSchedulerListener(SchedulerListener listener) { return schedulerListeners.remove(listener); }

    public List<JobListener> getJobListeners() {
        List<JobListener> list = new ArrayList<>();
        for (JobListenerEntry e : jobListeners) list.add(e.listener);
        return list;
    }
    public List<TriggerListener> getTriggerListeners() {
        List<TriggerListener> list = new ArrayList<>();
        for (TriggerListenerEntry e : triggerListeners) list.add(e.listener);
        return list;
    }
    public List<SchedulerListener> getSchedulerListeners() { return new ArrayList<>(schedulerListeners); }

    public void notifyJobToBeExecuted(JobKey jobKey, JobExecutionContext ctx) {
        for (JobListenerEntry e : jobListeners) if (e.matcher.matchesJob(jobKey)) e.listener.jobToBeExecuted(ctx);
    }
    public void notifyJobWasExecuted(JobKey jobKey, JobExecutionContext ctx, Throwable exception) {
        for (JobListenerEntry e : jobListeners) if (e.matcher.matchesJob(jobKey)) e.listener.jobWasExecuted(ctx, exception);
    }
    public void notifyTriggerFired(TriggerKey triggerKey, Trigger trigger, JobExecutionContext ctx) {
        for (TriggerListenerEntry e : triggerListeners) if (e.matcher.matchesTrigger(triggerKey)) e.listener.triggerFired(trigger, ctx);
    }
    public void notifySchedulerStarted() { for (SchedulerListener s : schedulerListeners) s.schedulerStarted(); }
    public void notifySchedulerShutdown() { for (SchedulerListener s : schedulerListeners) s.schedulerShutdown(); }
    public void notifySchedulerError(String msg, Throwable cause) { for (SchedulerListener s : schedulerListeners) s.schedulerError(msg, cause); }

    private static class JobListenerEntry {
        final JobListener listener;
        final Matcher matcher;
        JobListenerEntry(JobListener l, Matcher m) { this.listener = l; this.matcher = m; }
    }
    private static class TriggerListenerEntry {
        final TriggerListener listener;
        final Matcher matcher;
        TriggerListenerEntry(TriggerListener l, Matcher m) { this.listener = l; this.matcher = m; }
    }
}

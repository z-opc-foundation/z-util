package com.zifang.util.core.schedule;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 自研调度器管理器，核心实现基于 {@link ScheduledExecutorService}。
 */
public class SchedulerManager {

    private final String name;
    private final ScheduledExecutorService executor;
    private final Map<JobKey, JobDetail> jobs = new ConcurrentHashMap<>();
    private final Map<TriggerKey, Trigger> triggers = new ConcurrentHashMap<>();
    private final Map<TriggerKey, ScheduledFuture<?>> futures = new ConcurrentHashMap<>();
    private final Map<JobKey, Boolean> jobPaused = new ConcurrentHashMap<>();
    private final com.zifang.util.core.schedule.listener.ListenerManager listenerManager =
            new com.zifang.util.core.schedule.listener.ListenerManager();
    private volatile boolean allPaused = false;
    private volatile boolean started = false;
    private volatile boolean shutdown = false;

    SchedulerManager(String name, ScheduledExecutorService executor) {
        this.name = Objects.requireNonNull(name);
        this.executor = Objects.requireNonNull(executor);
    }

    public void start() { started = true; }

    public void shutdown() { shutdown(true); }

    public void shutdown(boolean waitForJobsComplete) {
        if (shutdown) return;
        shutdown = true;
        for (ScheduledFuture<?> f : futures.values()) f.cancel(false);
        if (waitForJobsComplete) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) executor.shutdownNow();
            } catch (InterruptedException e) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
        } else { executor.shutdownNow(); }
    }

    public boolean isStarted() { return started; }
    public boolean isShutdown() { return shutdown; }
    public String getSchedulerName() { return name; }

    public SchedulerMetaData getMetaData() {
        return new SchedulerMetaData(name, started, jobs.size(), threadPoolSize());
    }

    private int threadPoolSize() {
        if (executor instanceof ScheduledThreadPoolExecutor) {
            return ((ScheduledThreadPoolExecutor) executor).getCorePoolSize();
        }
        return 0;
    }

    public com.zifang.util.core.schedule.listener.ListenerManager getListenerManager() { return listenerManager; }

    public Date scheduleJob(JobDetail job, Trigger trigger) {
        jobs.put(job.getKey(), job);
        triggers.put(trigger.getKey(), trigger);
        scheduleNextFire(trigger);
        return trigger.getNextFireTime();
    }

    public void addJob(JobDetail job) { addJob(job, true); }
    public void addJob(JobDetail job, boolean replace) {
        if (replace) jobs.put(job.getKey(), job);
        else if (jobs.putIfAbsent(job.getKey(), job) != null)
            throw new SchedulerRuntimeException("Job already exists: " + job.getKey());
    }

    public boolean deleteJob(JobKey jobKey) {
        JobDetail removed = jobs.remove(jobKey);
        if (removed == null) return false;
        for (TriggerKey tk : new java.util.ArrayList<>(triggers.keySet())) {
            Trigger t = triggers.get(tk);
            if (t.getJobKey().equals(jobKey)) unscheduleJob(tk);
        }
        return true;
    }
    public boolean deleteJob(String name, String group) { return deleteJob(JobKey.jobKey(name, group)); }
    public boolean deleteJob(String name) { return deleteJob(JobKey.jobKey(name)); }

    public Trigger getTrigger(TriggerKey triggerKey) { return triggers.get(triggerKey); }
    public Trigger getTrigger(String name, String group) { return triggers.get(TriggerKey.triggerKey(name, group)); }
    public Trigger getTrigger(String name) { return triggers.get(TriggerKey.triggerKey(name)); }

    public void pauseTrigger(TriggerKey triggerKey) {
        ScheduledFuture<?> f = futures.remove(triggerKey);
        if (f != null) f.cancel(false);
    }
    public void pauseTrigger(String name, String group) { pauseTrigger(TriggerKey.triggerKey(name, group)); }
    public void pauseTrigger(String name) { pauseTrigger(TriggerKey.triggerKey(name)); }

    public void resumeTrigger(TriggerKey triggerKey) {
        if (!futures.containsKey(triggerKey)) {
            Trigger t = triggers.get(triggerKey);
            if (t != null) scheduleNextFire(t);
        }
    }
    public void resumeTrigger(String name, String group) { resumeTrigger(TriggerKey.triggerKey(name, group)); }
    public void resumeTrigger(String name) { resumeTrigger(TriggerKey.triggerKey(name)); }

    public void pauseAllTriggers() {
        allPaused = true;
        for (TriggerKey tk : new java.util.ArrayList<>(futures.keySet())) {
            ScheduledFuture<?> f = futures.remove(tk);
            if (f != null) f.cancel(false);
        }
    }
    public void resumeAllTriggers() {
        allPaused = false;
        for (Trigger t : triggers.values()) {
            if (!futures.containsKey(t.getKey())) scheduleNextFire(t);
        }
    }

    public Trigger unscheduleJob(TriggerKey triggerKey) {
        ScheduledFuture<?> f = futures.remove(triggerKey);
        if (f != null) f.cancel(false);
        return triggers.remove(triggerKey);
    }
    public Trigger unscheduleJob(String name, String group) { return unscheduleJob(TriggerKey.triggerKey(name, group)); }
    public Trigger unscheduleJob(String name) { return unscheduleJob(TriggerKey.triggerKey(name)); }

    public Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) {
        unscheduleJob(triggerKey);
        triggers.put(newTrigger.getKey(), newTrigger);
        scheduleNextFire(newTrigger);
        return newTrigger.getNextFireTime();
    }

    public void pauseJob(JobKey jobKey) {
        jobPaused.put(jobKey, Boolean.TRUE);
        for (Trigger t : triggers.values()) {
            if (t.getJobKey().equals(jobKey)) pauseTrigger(t.getKey());
        }
    }
    public void resumeJob(JobKey jobKey) {
        jobPaused.remove(jobKey);
        for (Trigger t : triggers.values()) {
            if (t.getJobKey().equals(jobKey)) resumeTrigger(t.getKey());
        }
    }

    public void triggerJob(JobKey jobKey) { triggerJob(jobKey, null); }
    public void triggerJob(JobKey jobKey, Map<String, ?> data) {
        JobDetail job = jobs.get(jobKey);
        if (job == null) throw new SchedulerRuntimeException("Job not found: " + jobKey);
        Trigger t = null;
        for (Trigger candidate : triggers.values()) {
            if (candidate.getJobKey().equals(jobKey)) { t = candidate; break; }
        }
        executeJob(job, t, data);
    }

    public JobDetail getJob(JobKey jobKey) { return jobs.get(jobKey); }
    public JobDetail getJob(String name, String group) { return jobs.get(JobKey.jobKey(name, group)); }
    public JobDetail getJob(String name) { return jobs.get(JobKey.jobKey(name)); }
    public boolean checkExists(JobKey jobKey) { return jobs.containsKey(jobKey); }
    public List<JobKey> getJobKeys() { return jobs.keySet().stream().collect(Collectors.toList()); }
    public List<String> getJobGroupNames() { return jobs.keySet().stream().map(JobKey::getGroup).distinct().sorted().collect(Collectors.toList()); }
    public JobKey getTriggerJobKey(TriggerKey triggerKey) {
        Trigger t = triggers.get(triggerKey);
        if (t == null) throw new SchedulerRuntimeException("Trigger not found: " + triggerKey);
        return t.getJobKey();
    }
    public List<TriggerKey> getTriggerKeys() { return triggers.keySet().stream().collect(Collectors.toList()); }
    public List<String> getTriggerGroupNames() { return triggers.keySet().stream().map(TriggerKey::getGroup).distinct().sorted().collect(Collectors.toList()); }

    private void scheduleNextFire(Trigger t) {
        if (shutdown || allPaused) return;
        if (jobPaused.getOrDefault(t.getJobKey(), Boolean.FALSE)) return;
        Date next = t.getNextFireTime();
        if (next == null) return;
        long delay = next.getTime() - System.currentTimeMillis();
        if (delay < 0) delay = 0;
        ScheduledFuture<?> f = executor.schedule(() -> fire(t), delay, TimeUnit.MILLISECONDS);
        futures.put(t.getKey(), f);
    }

    private void fire(Trigger t) {
        if (shutdown) return;
        if (allPaused || jobPaused.getOrDefault(t.getJobKey(), Boolean.FALSE)) return;
        JobDetail job = jobs.get(t.getJobKey());
        if (job == null) { futures.remove(t.getKey()); return; }
        Date now = new Date();
        t.setPreviousFireTime(t.getNextFireTime());
        executeJob(job, t, null);
        if (t instanceof SimpleTrigger) {
            SimpleTrigger st = (SimpleTrigger) t;
            st.incrementTimesTriggered();
            if (!st.mayFireAgain()) { triggers.remove(st.getKey()); futures.remove(st.getKey()); return; }
            Date next = new Date(now.getTime() + st.getIntervalMillis());
            if (st.getEndTime() != null && next.after(st.getEndTime())) {
                triggers.remove(st.getKey()); futures.remove(st.getKey()); return;
            }
            st.setNextFireTime(next);
        } else if (t instanceof CronTrigger) {
            ((CronTrigger) t).advanceToNextFireTime();
        }
        if (t.getNextFireTime() != null) scheduleNextFire(t);
        else futures.remove(t.getKey());
    }

    private void executeJob(JobDetail job, Trigger trigger, Map<String, ?> extraData) {
        JobDataMap merged = new JobDataMap(job.getJobDataMap());
        if (extraData != null) merged.putAll(extraData);
        JobExecutionContext ctx = new JobExecutionContext(job, trigger, System.currentTimeMillis(),
                trigger != null && trigger.getNextFireTime() != null ? trigger.getNextFireTime().getTime() : System.currentTimeMillis());
        try {
            Job instance = job.getJobClass().getDeclaredConstructor().newInstance();
            instance.execute(ctx);
        } catch (Throwable e) {
            System.err.println("[SchedulerManager] job execution failed: " + e);
            e.printStackTrace();
        }
    }
}

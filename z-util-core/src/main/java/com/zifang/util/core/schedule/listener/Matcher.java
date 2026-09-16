package com.zifang.util.core.schedule.listener;

import com.zifang.util.core.schedule.JobKey;
import com.zifang.util.core.schedule.TriggerKey;

/**
 * 自研 Listener 匹配规则。简化版。
 */
public class Matcher {

    private final boolean matchAll;
    private final java.util.Set<String> jobGroups;
    private final java.util.Set<String> triggerGroups;
    private final java.util.Set<String> jobNamePrefixes;

    private Matcher(boolean matchAll, java.util.Set<String> jobGroups,
                    java.util.Set<String> triggerGroups,
                    java.util.Set<String> jobNamePrefixes) {
        this.matchAll = matchAll;
        this.jobGroups = jobGroups;
        this.triggerGroups = triggerGroups;
        this.jobNamePrefixes = jobNamePrefixes;
    }

    public static Matcher anyJobs() { return new Matcher(true, null, null, null); }

    public static Matcher jobGroupEqualsTo(String... groups) {
        java.util.Set<String> set = new java.util.HashSet<>();
        if (groups != null) for (String g : groups) set.add(g);
        return new Matcher(false, set, null, null);
    }

    public static Matcher jobNameStartsWith(String prefix) {
        java.util.Set<String> set = new java.util.HashSet<>();
        if (prefix != null) set.add(prefix);
        return new Matcher(false, null, null, set);
    }

    public static Matcher anyTriggers() { return new Matcher(true, null, null, null); }

    public static Matcher triggerGroupEqualsTo(String... groups) {
        java.util.Set<String> set = new java.util.HashSet<>();
        if (groups != null) for (String g : groups) set.add(g);
        return new Matcher(false, null, set, null);
    }

    public boolean matchesJob(JobKey jobKey) {
        if (matchAll) return true;
        if (jobGroups != null && !jobGroups.isEmpty()) return jobGroups.contains(jobKey.getGroup());
        if (jobNamePrefixes != null && !jobNamePrefixes.isEmpty()) {
            for (String prefix : jobNamePrefixes) {
                if (jobKey.getName().startsWith(prefix)) return true;
            }
            return false;
        }
        return false;
    }

    public boolean matchesTrigger(TriggerKey triggerKey) {
        if (matchAll) return true;
        if (triggerGroups != null && !triggerGroups.isEmpty()) return triggerGroups.contains(triggerKey.getGroup());
        return false;
    }
}

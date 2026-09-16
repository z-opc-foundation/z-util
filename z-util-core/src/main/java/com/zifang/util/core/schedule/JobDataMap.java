package com.zifang.util.core.schedule;

import java.util.HashMap;
import java.util.Map;

/**
 * 自研 JobDataMap：每个 job 携带的字符串 → 对象 上下文。
 * 对标 {@code org.quartz.JobDataMap}。
 */
public class JobDataMap extends HashMap<String, Object> {
    public JobDataMap() { super(); }
    public JobDataMap(Map<String, ?> source) { super(); if (source != null) putAll(source); }
    public String getString(String key) { Object v = get(key); return v == null ? null : v.toString(); }
    @SuppressWarnings("unchecked") public <T> T getAs(String key) { return (T) get(key); }
}

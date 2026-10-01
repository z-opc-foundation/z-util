package com.zifang.util.aop.aopgen;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 记录型钩子：把 before/after 事件按序存下，供测试断言。
 */
public class RecordingHook implements MethodHook {

    public final List<String> events = new CopyOnWriteArrayList<>();

    @Override
    public void before(String method, Object[] args) {
        events.add("before:" + method + ":" + Arrays.toString(args));
    }

    @Override
    public void after(String method, Object result) {
        events.add("after:" + method + ":" + result);
    }
}

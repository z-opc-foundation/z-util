package com.zifang.util.proxy.weave;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 织入测试的静态钩子记录器。
 */
public class WeaveRecorder {

    public static final List<String> ENTRIES = new CopyOnWriteArrayList<>();

    private WeaveRecorder() {
    }

    public static void record(String owner, String method) {
        ENTRIES.add(owner + "#" + method);
    }

    public static void clear() {
        ENTRIES.clear();
    }
}

package com.zifang.util.monitor.platform;

import java.util.Collections;
import java.util.Map;

/**
 * 降级网络探针 —— 不认识的平台一律返回空 Map，对应指标不产出。
 *
 * @author zifang
 */
public class NoopNetProbe implements NetStatsProbe {

    @Override
    public Map<String, long[]> interfaceCounters() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Integer> tcpStates() {
        return Collections.emptyMap();
    }
}

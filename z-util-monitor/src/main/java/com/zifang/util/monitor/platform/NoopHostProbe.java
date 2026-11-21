package com.zifang.util.monitor.platform;

/**
 * 降级宿主机探针 —— 不认识的平台一律返回 -1，对应指标不产出。
 *
 * @author zifang
 */
public class NoopHostProbe implements HostProbe {

    @Override
    public long uptimeMillis() {
        return -1;
    }

    @Override
    public int openFileDescriptorCount() {
        return -1;
    }

    @Override
    public long fileDescriptorLimit() {
        return -1;
    }
}

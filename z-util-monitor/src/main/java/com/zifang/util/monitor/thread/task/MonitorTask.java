package com.zifang.util.monitor.thread.task;

import com.zifang.util.json.model.JsonObject;
import com.zifang.util.monitor.thread.Monitorable;
import com.zifang.util.monitor.thread.Status;
import com.zifang.util.monitor.thread.StatusLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * 监控线程，定时访问监控组件集合中的所有组件。
 *
 * @author zifang
 */
public class MonitorTask implements Runnable {

    /**
     * 日志对象。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorTask.class);

    /**
     * 监控组件集合。
     */
    private Collection<Monitorable> monitorSet;

    /**
     * 构造函数。
     *
     * @param monitorSet 监控组件集合。
     */
    public MonitorTask(Collection<Monitorable> monitorSet) {
        this.monitorSet = monitorSet;
    }

    @Override
    /**
     * run方法。
     */
    public void run() {
        LOGGER.info("{} start", this.getClass().getSimpleName());
        JsonObject jsonObject;
        for (Monitorable monitorable : monitorSet) {
            //防止监控组件的任何异常导致此线程执行出错。
            try {
                jsonObject = new JsonObject();
                Status status = monitorable.status();
                jsonObject.put("component", monitorable.componentName());
                jsonObject.put("statusLevel", status.getLevel().toString());
                jsonObject.put("status", status.getStatus());
                if (status.getLevel() != StatusLevel.OK) {
                    monitorable.alarm();
                }
                LOGGER.info(jsonObject.toString());
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        LOGGER.info("{} end", this.getClass().getSimpleName());
    }
}

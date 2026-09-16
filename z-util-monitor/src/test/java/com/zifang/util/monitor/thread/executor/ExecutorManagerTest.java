package com.zifang.util.monitor.thread.executor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ExecutorManagerTest类。
 */
public class ExecutorManagerTest {

    @Test
    /**
     * testExecutorManagerCreation方法。
     */
    public void testExecutorManagerCreation() {
        List<ThreadPoolConfigUnit> configList = new ArrayList<>();
        ThreadPoolConfigUnit unit = new ThreadPoolConfigUnit();
        unit.setPoolName("TestPool");
        unit.setPoolSize(2);
        configList.add(unit);

        ExecutorManager manager = new ExecutorManager(configList);
        assertNotNull(manager);
        assertNotNull(manager.getExecutor("TestPool"));
        assertNotNull(manager.getMonitorManager());

        manager.getMonitorManager().shutdown(true);
    }

    @Test
    /**
     * testExecutorManagerWithNullList方法。
     */
    public void testExecutorManagerWithNullList() {
        ExecutorManager manager = new ExecutorManager(null);
        assertNotNull(manager);
    }

    @Test
    /**
     * testExecutorManagerWithEmptyList方法。
     */
    public void testExecutorManagerWithEmptyList() {
        ExecutorManager manager = new ExecutorManager(new ArrayList<>());
        assertNotNull(manager);
    }

    @Test
    /**
     * testGetExecutor方法。
     */
    public void testGetExecutor() {
        List<ThreadPoolConfigUnit> configList = new ArrayList<>();
        ThreadPoolConfigUnit unit = new ThreadPoolConfigUnit();
        unit.setPoolName("TestPool");
        unit.setPoolSize(2);
        configList.add(unit);

        ExecutorManager manager = new ExecutorManager(configList);
        assertNotNull(manager.getExecutor("TestPool"));
        assertNull(manager.getExecutor("NonExistent"));

        manager.getMonitorManager().shutdown(true);
    }

    @Test
    /**
     * testConstants方法。
     */
    public void testConstants() {
        assertEquals(0xF, ExecutorManager.TASK_EXECUTE_TYPE_MASK);
        assertEquals(0x1, ExecutorManager.CPU_INTENSIVE);
        assertEquals(0x2, ExecutorManager.IO_INTENSIVE);
    }
}

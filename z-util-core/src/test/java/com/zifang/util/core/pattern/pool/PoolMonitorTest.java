package com.zifang.util.core.pattern.pool;

import com.zifang.util.core.pattern.pool.monitor.PoolListener;
import com.zifang.util.core.pattern.pool.monitor.PoolStats;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 对象池监控测试
 */

/**
 * PoolMonitorTest类。
 */
public class PoolMonitorTest {

    private MonitoredObjectPool<StringBuffer> pool;

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() {
        if (pool != null) {
            pool.close();
        }
    }

    @Test
    /**
     * testMonitorStats方法。
     */
    public void testMonitorStats() throws Exception {
        PoolListener<StringBuffer> listener = new PoolListener<StringBuffer>() {
            @Override
            /**
             * onBorrow方法。
             *      * @param object StringBuffer类型参数
             * @param waitTime long类型参数
             */
            public void onBorrow(StringBuffer object, long waitTime) {
            }

            @Override
            /**
             * onReturn方法。
             *      * @param object StringBuffer类型参数
             * @param waitTime long类型参数
             */
            public void onReturn(StringBuffer object, long waitTime) {
            }

            @Override
            /**
             * onCreate方法。
             *      * @param object StringBuffer类型参数
             */
            public void onCreate(StringBuffer object) {
            }

            @Override
            /**
             * onDestroy方法。
             *      * @param object StringBuffer类型参数
             */
            public void onDestroy(StringBuffer object) {
            }

            @Override
            /**
             * onValidate方法。
             *      * @param object StringBuffer类型参数
             * @param valid boolean类型参数
             */
            public void onValidate(StringBuffer object, boolean valid) {
            }

            @Override
            /**
             * onClose方法。
             */
            public void onClose() {
            }

            @Override
            /**
             * onEvict方法。
             *      * @param object StringBuffer类型参数
             */
            public void onEvict(StringBuffer object) {
            }
        };

        pool = PoolUtils.createMonitoredPool(
                StringBuffer::new,
                sb -> {
                },
                listener
        );

        StringBuffer sb1 = pool.borrowObject();
        assertNotNull(sb1);

        PoolStats stats = pool.getStats();
        assertTrue(stats.getBorrowCount() >= 1);

        pool.returnObject(sb1);

        assertTrue(stats.getReturnCount() >= 1);
    }

    @Test
    /**
     * testMonitorListener方法。
     */
    public void testMonitorListener() throws Exception {
        AtomicInteger borrowCount = new AtomicInteger(0);
        AtomicInteger returnCount = new AtomicInteger(0);

        PoolListener<StringBuffer> listener = new PoolListener<StringBuffer>() {
            @Override
            /**
             * onBorrow方法。
             *      * @param object StringBuffer类型参数
             * @param waitTime long类型参数
             */
            public void onBorrow(StringBuffer object, long waitTime) {
                borrowCount.incrementAndGet();
            }

            @Override
            /**
             * onReturn方法。
             *      * @param object StringBuffer类型参数
             * @param waitTime long类型参数
             */
            public void onReturn(StringBuffer object, long waitTime) {
                returnCount.incrementAndGet();
            }

            @Override
            /**
             * onCreate方法。
             *      * @param object StringBuffer类型参数
             */
            public void onCreate(StringBuffer object) {
            }

            @Override
            /**
             * onDestroy方法。
             *      * @param object StringBuffer类型参数
             */
            public void onDestroy(StringBuffer object) {
            }

            @Override
            /**
             * onValidate方法。
             *      * @param object StringBuffer类型参数
             * @param valid boolean类型参数
             */
            public void onValidate(StringBuffer object, boolean valid) {
            }

            @Override
            /**
             * onClose方法。
             */
            public void onClose() {
            }

            @Override
            /**
             * onEvict方法。
             *      * @param object StringBuffer类型参数
             */
            public void onEvict(StringBuffer object) {
            }
        };

        pool = PoolUtils.createMonitoredPool(
                StringBuffer::new,
                sb -> {
                },
                listener
        );

        StringBuffer sb1 = pool.borrowObject();
        assertEquals(1, borrowCount.get());

        pool.returnObject(sb1);
        assertEquals(1, returnCount.get());

        StringBuffer sb2 = pool.borrowObject();
        assertEquals(2, borrowCount.get());
    }

    @Test
    /**
     * testMonitorDisable方法。
     */
    public void testMonitorDisable() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        PoolListener<StringBuffer> listener = new PoolListener<StringBuffer>() {
            @Override
            /**
             * onBorrow方法。
             *      * @param object StringBuffer类型参数
             * @param waitTime long类型参数
             */
            public void onBorrow(StringBuffer object, long waitTime) {
                count.incrementAndGet();
            }

            @Override
            /**
             * onReturn方法。
             *      * @param object StringBuffer类型参数
             * @param waitTime long类型参数
             */
            public void onReturn(StringBuffer object, long waitTime) {
            }

            @Override
            /**
             * onCreate方法。
             *      * @param object StringBuffer类型参数
             */
            public void onCreate(StringBuffer object) {
            }

            @Override
            /**
             * onDestroy方法。
             *      * @param object StringBuffer类型参数
             */
            public void onDestroy(StringBuffer object) {
            }

            @Override
            /**
             * onValidate方法。
             *      * @param object StringBuffer类型参数
             * @param valid boolean类型参数
             */
            public void onValidate(StringBuffer object, boolean valid) {
            }

            @Override
            /**
             * onClose方法。
             */
            public void onClose() {
            }

            @Override
            /**
             * onEvict方法。
             *      * @param object StringBuffer类型参数
             */
            public void onEvict(StringBuffer object) {
            }
        };

        pool = PoolUtils.createMonitoredPool(
                StringBuffer::new,
                sb -> {
                },
                listener
        );

        pool.getMonitor().setEnabled(false);

        StringBuffer sb1 = pool.borrowObject();
        assertEquals(0, count.get()); // 监控被禁用，不计数

        pool.getMonitor().setEnabled(true);

        StringBuffer sb2 = pool.borrowObject();
        assertEquals(1, count.get());
    }

    @Test
    /**
     * testMonitorStatsConsumer方法。
     */
    public void testMonitorStatsConsumer() throws Exception {
        AtomicInteger updateCount = new AtomicInteger(0);

        pool = PoolUtils.createMonitoredPool(
                StringBuffer::new,
                sb -> {
                }
        );

        pool.getMonitor().addStatsConsumer(stats -> {
            if (stats.getBorrowCount() > 0) {
                updateCount.incrementAndGet();
            }
        });

        StringBuffer sb1 = pool.borrowObject();
        assertTrue(updateCount.get() > 0);
    }

    @Test
    /**
     * testMonitorReset方法。
     */
    public void testMonitorReset() throws Exception {
        pool = PoolUtils.createMonitoredPool(
                StringBuffer::new,
                sb -> {
                }
        );

        StringBuffer sb1 = pool.borrowObject();
        pool.returnObject(sb1);

        PoolStats stats = pool.getStats();
        assertTrue(stats.getBorrowCount() >= 1);

        pool.getMonitor().reset();

        assertEquals(0, stats.getBorrowCount());
        assertEquals(0, stats.getReturnCount());
    }
}
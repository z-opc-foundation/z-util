package com.zifang.util.db.lock;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * DB 分布式锁测试（用 H2 in-memory）。
 */
public class DbDistributedLockTest {

    private static org.h2.jdbcx.JdbcDataSource DS;

    @BeforeClass
    public static void setup() throws Exception {
        DS = new org.h2.jdbcx.JdbcDataSource();
        DS.setURL("jdbc:h2:mem:locktest;DB_CLOSE_DELAY=-1");
        DS.setUser("sa");
        DS.setPassword("");
        try (java.sql.Connection c = DS.getConnection();
             java.sql.Statement s = c.createStatement()) {
            s.execute("CREATE TABLE distributed_lock (" +
                    "lock_key VARCHAR(128) PRIMARY KEY, " +
                    "token VARCHAR(64) NOT NULL, " +
                    "expire_at BIGINT NOT NULL)");
        }
    }

    @AfterClass
    public static void teardown() throws Exception {
        if (DS != null) {
            try (java.sql.Connection c = DS.getConnection();
                 java.sql.Statement s = c.createStatement()) {
                s.execute("DROP ALL OBJECTS");
            }
        }
    }

    @Test
    public void testAcquireAndRelease() {
        DbDistributedLock lock = new DbDistributedLock(DS, "order:create", 5000);
        String t1 = lock.tryLock();
        assertNotNull(t1);
        // 立即再抢（不等待）→ 应失败
        String t2 = lock.tryLock();
        assertNull("second acquire without wait should fail", t2);
        // CAS 释放
        assertTrue(lock.unlock(t1));
        // 释放后能再拿
        String t3 = lock.tryLock();
        assertNotNull(t3);
        assertTrue(lock.unlock(t3));
    }

    @Test
    public void testExpirationAllowsReacquire() throws Exception {
        DbDistributedLock lock = new DbDistributedLock(DS, "order:pay", 200);   // 200ms 过期
        String t1 = lock.tryLock();
        assertNotNull(t1);
        // 等 300ms 让锁自动过期
        Thread.sleep(300);
        String t2 = lock.tryLock();
        assertNotNull("after expiration should re-acquire", t2);
        lock.unlock(t2);
    }

    @Test
    public void testWrongTokenNotReleased() {
        DbDistributedLock lock = new DbDistributedLock(DS, "any:key", 5000);
        String t1 = lock.tryLock();
        assertNotNull(t1);
        assertFalse(lock.unlock("not-mine"));
        assertTrue(lock.unlock(t1));
    }

    @Test
    public void testConcurrentMutualExclusion() throws Exception {
        DbDistributedLock lock = new DbDistributedLock(DS, "concurrent:key", 5000);
        AtomicInteger insideCount = new AtomicInteger();
        AtomicInteger maxConcurrent = new AtomicInteger();
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    String t = lock.tryLock(1_000_000_000L);   // 1s
                    if (t != null) {
                        int c = insideCount.incrementAndGet();
                        maxConcurrent.updateAndGet(prev -> Math.max(prev, c));
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ignored) {
                        }
                        insideCount.decrementAndGet();
                        lock.unlock(t);
                    }
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        // 任何时刻 insideCount ≤ 1
        assertTrue("max concurrent should be 1, got " + maxConcurrent.get(), maxConcurrent.get() <= 1);
    }
}

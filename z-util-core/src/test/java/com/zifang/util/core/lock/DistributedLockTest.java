package com.zifang.util.core.lock;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * 文件锁测试（自研，对标 Redisson FileLock）。
 * <p>
 * DB 分布式锁已迁移到 z-util-jdbc 的 {@code com.zifang.util.db.lock.DbDistributedLock}。
 */
public class DistributedLockTest {

    @Test
    public void testFileLock_mutualExclusion() throws Exception {
        Path tmp = Files.createTempFile("zutil-lock-", ".lock");
        tmp.toFile().deleteOnExit();
        FileDistributedLock lock = new FileDistributedLock(tmp.toString());

        String token1 = lock.tryLock();
        assertNotNull(token1);
        assertNull(lock.tryLock());
        assertTrue(lock.unlock(token1));
        String token2 = lock.tryLock();
        assertNotNull(token2);
        assertTrue(lock.unlock(token2));
    }

    @Test
    public void testFileLock_unlockWrongToken() throws Exception {
        Path tmp = Files.createTempFile("zutil-lock-", ".lock");
        FileDistributedLock lock = new FileDistributedLock(tmp.toString());
        String token = lock.tryLock();
        assertNotNull(token);
        assertFalse(lock.unlock("bogus"));
        assertTrue(lock.unlock(token));
    }

    @Test
    public void testFileLock_concurrent() throws Exception {
        Path tmp = Files.createTempFile("zutil-lock-", ".lock");
        final FileDistributedLock lock = new FileDistributedLock(tmp.toString());
        final AtomicInteger acquired = new AtomicInteger();
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    String t = lock.tryLock(200_000_000L);
                    if (t != null) {
                        acquired.incrementAndGet();
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException ignored) {
                        }
                        lock.unlock(t);
                    }
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        assertTrue("expected at least 1 acquire, got " + acquired.get(), acquired.get() >= 1);
    }
}

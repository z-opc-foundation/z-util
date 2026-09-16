package com.zifang.util.core.lock;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于文件 + Java NIO FileLock 的单机 / 同主机跨进程分布式锁（对标 Redisson Lock 的"文件锁"思路）。
 * <p>
 * 原理：用 {@link FileLock#tryLock()}（操作系统级排他锁）+ 文件内容存持有 token。
 * <p>
 * <b>局限</b>：FileLock 是进程级别的，跨主机无效。如需跨主机集群，请用 DB 锁（{@link DbDistributedLock}）。
 * <p>
 * 注意：FileLock 在 Windows 上的行为与 Unix 不同；推荐 Linux / macOS 使用。
 */
public class FileDistributedLock implements DistributedLock {

    private final Path lockFile;
    private final ConcurrentMap<String, FileChannel> channels = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, FileLock> locks = new ConcurrentHashMap<>();

    public FileDistributedLock(String lockFilePath) {
        this.lockFile = Paths.get(lockFilePath);
        try {
            Path parent = lockFile.getParent();
            if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);
            if (!Files.exists(lockFile)) Files.createFile(lockFile);
        } catch (IOException e) {
            throw new LockException("create lock file failed: " + lockFile, e);
        }
    }

    @Override
    public String tryLock() {
        return tryLock(0L);
    }

    @Override
    public String tryLock(long waitNanos) {
        long deadline = waitNanos > 0 ? System.nanoTime() + waitNanos : 0L;
        String token = UUID.randomUUID().toString();
        while (true) {
            try {
                FileChannel ch = FileChannel.open(lockFile,
                        StandardOpenOption.READ, StandardOpenOption.WRITE);
                FileLock lock = ch.tryLock(0L, Long.MAX_VALUE, false);
                if (lock != null) {
                    writeToken(ch, token);
                    channels.put(token, ch);
                    locks.put(token, lock);
                    return token;
                }
                ch.close();
            } catch (OverlappingFileLockException ignored) {
                // 当前 JVM 已持有 → 跳过
            } catch (IOException e) {
                // 不致命，继续重试
            }
            if (deadline == 0L) return null;
            if (System.nanoTime() >= deadline) return null;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    @Override
    public boolean unlock(String token) {
        if (token == null) return false;
        FileLock lock = locks.remove(token);
        FileChannel ch = channels.remove(token);
        if (lock == null || ch == null) return false;
        try {
            lock.release();
            ch.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void writeToken(FileChannel ch, String token) {
        try {
            ch.position(0);
            byte[] bytes = token.getBytes("UTF-8");
            java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(bytes);
            ch.write(buf);
            ch.truncate(bytes.length);
        } catch (IOException ignored) {
        }
    }
}

package com.zifang.util.db.lock;

import com.zifang.util.core.lock.DistributedLock;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于关系型数据库的分布式锁（自研，对标 Redisson Lock 的"DB 锁"思路）。
 * <p>
 * 原理：每把锁对应一行 {@code (lock_key, token, expire_at)}。
 * <ul>
 *   <li>获取：查询现有 row；不存在则 INSERT；存在但已过期则 CAS UPDATE 抢锁；
 *       存在且未过期则失败（除非是自己续期）。</li>
 *   <li>释放：{@code DELETE WHERE token = ?}（CAS 释放，删错就是别人）。</li>
 * </ul>
 * <p>
 * 注意：使用方负责建表：
 * <pre>{@code
 *   CREATE TABLE distributed_lock (
 *       lock_key VARCHAR(128) PRIMARY KEY,
 *       token    VARCHAR(64) NOT NULL,
 *       expire_at BIGINT NOT NULL
 *   );
 * }</pre>
 *
 * <h3>位置说明</h3>
 * 本类归属 z-util-jdbc 模块：DB 锁天然依赖 DataSource / Connection 生命周期，
 * 与 z-util-jdbc 的 connection pool / transaction 管理同源。z-util-lock 只保留
 * 不依赖数据源的文件锁 / 进程锁实现。
 */
public class DbDistributedLock implements DistributedLock {

    /**
     * MySQL / H2 / PostgreSQL 通用实现（DELETE WHERE 跨库兼容）。
     */
    public static final Dialect STANDARD = new Dialect() {
        @Override
        public String releaseSql() {
            return "DELETE FROM distributed_lock WHERE lock_key = ? AND token = ?";
        }
    };
    private final DataSource ds;
    private final String key;
    private final long expireMillis;
    private final Dialect dialect;
    /**
     * 当前进程持有的 token（用于 release 时校验）
     */
    private final ConcurrentMap<String, String> tokens = new ConcurrentHashMap<>();

    public DbDistributedLock(DataSource ds, String lockKey, long expireMs) {
        this(ds, lockKey, expireMs, STANDARD);
    }

    public DbDistributedLock(DataSource ds, String lockKey, long expireMs, Dialect dialect) {
        if (ds == null) throw new NullPointerException("ds");
        if (lockKey == null || lockKey.isEmpty()) throw new IllegalArgumentException("lockKey");
        if (expireMs <= 0) throw new IllegalArgumentException("expireMs must be > 0");
        this.ds = ds;
        this.key = lockKey;
        this.expireMillis = expireMs;
        this.dialect = dialect;
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
            if (acquire(token)) {
                tokens.put(token, key);
                return token;
            }
            if (deadline == 0L) return null;
            if (System.nanoTime() >= deadline) return null;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    private boolean acquire(String token) {
        long now = System.currentTimeMillis();
        long expireAt = now + expireMillis;
        try (Connection c = ds.getConnection()) {
            // 1) 查询现有持有者
            String currentToken;
            long currentExpire;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT token, expire_at FROM distributed_lock WHERE lock_key = ?")) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentToken = rs.getString(1);
                        currentExpire = rs.getLong(2);
                    } else {
                        currentToken = null;
                        currentExpire = 0L;
                    }
                }
            }
            // 2) 决定是 INSERT 还是 UPDATE
            if (currentToken == null) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO distributed_lock (lock_key, token, expire_at) VALUES (?, ?, ?)")) {
                    ps.setString(1, key);
                    ps.setString(2, token);
                    ps.setLong(3, expireAt);
                    try {
                        ps.executeUpdate();
                        return true;
                    } catch (SQLException dup) {
                        // 并发：别人先 INSERT 了，递归一次
                        return acquire(token);
                    }
                }
            } else if (currentToken.equals(token)) {
                // 自己持锁 → 续期
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE distributed_lock SET expire_at = ? WHERE lock_key = ? AND token = ?")) {
                    ps.setLong(1, expireAt);
                    ps.setString(2, key);
                    ps.setString(3, token);
                    ps.executeUpdate();
                    return true;
                }
            } else if (currentExpire < now) {
                // 别人持锁但已过期 → 抢（CAS）
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE distributed_lock SET token = ?, expire_at = ? " +
                                "WHERE lock_key = ? AND token = ? AND expire_at < ?")) {
                    ps.setString(1, token);
                    ps.setLong(2, expireAt);
                    ps.setString(3, key);
                    ps.setString(4, currentToken);
                    ps.setLong(5, now);
                    return ps.executeUpdate() > 0;
                }
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean unlock(String token) {
        if (token == null) return false;
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(dialect.releaseSql())) {
            ps.setString(1, key);
            ps.setString(2, token);
            int affected = ps.executeUpdate();
            tokens.remove(token);
            return affected > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * SQL 方言接口（释放时不同方言的占位符可能不同）。
     */
    public interface Dialect {
        /**
         * 释放 SQL：参数 lock_key, token。
         */
        String releaseSql();
    }
}

package com.zifang.util.db.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于数据库的序列生成器
 * <p>
 * 通过数据库表存储序列状态，支持批量获取，性能高效。
 * 适用于需要严格连续序列号或有数据库依赖的场景。
 *
 * <p>表结构：
 * <pre>
 * CREATE TABLE IF NOT EXISTS sequence_info (
 *     name VARCHAR(64) PRIMARY KEY,
 *     current_value BIGINT NOT NULL DEFAULT 0,
 *     increment_by BIGINT NOT NULL DEFAULT 1,
 *     description VARCHAR(255)
 * );
 * </pre>
 *
 * <p>使用示例：
 * <pre>
 * DatabaseSequence seq = new DatabaseSequence(dataSource, "order_no");
 * seq.initializeIfNotExists();  // 首次运行建表
 * long id = seq.next();
 * </pre>
 *
 * @see Sequence
 */
public class DatabaseSequence implements Sequence {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSequence.class);

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS sequence_info (" +
                    "name VARCHAR(64) PRIMARY KEY," +
                    "current_value BIGINT NOT NULL DEFAULT 0," +
                    "increment_by BIGINT NOT NULL DEFAULT 1," +
                    "description VARCHAR(255)" +
                    ")";

    private static final String UPSERT_SQL =
            "INSERT INTO sequence_info (name, current_value, increment_by, description) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = name";

    private static final String SELECT_SQL =
            "SELECT current_value FROM sequence_info WHERE name = ? FOR UPDATE";

    private static final String UPDATE_SQL =
            "UPDATE sequence_info SET current_value = current_value + ? WHERE name = ?";

    private static final String INIT_SQL =
            "INSERT IGNORE INTO sequence_info (name, current_value, increment_by) VALUES (?, 0, 1)";

    private final DataSource dataSource;
    private final String name;
    private final int batchSize;
    private final AtomicLong currentValue;
    private final AtomicLong nextValue;
    private volatile boolean initialized = false;

    /**
     * 创建数据库序列生成器（批量大小=1）
     *
     * @param dataSource 数据源
     * @param name       序列名称（唯一标识）
     */
    public DatabaseSequence(DataSource dataSource, String name) {
        this(dataSource, name, 1);
    }

    /**
     * 创建数据库序列生成器
     *
     * @param dataSource 数据源
     * @param name       序列名称
     * @param batchSize  批量获取大小（每次从数据库申请的号段大小，推荐 100~1000）
     */
    public DatabaseSequence(DataSource dataSource, String name, int batchSize) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为 null");
        }
        if (name == null || com.zifang.util.core.lang.StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("序列名称不能为空");
        }
        if (batchSize <= 0 || batchSize > 10000) {
            throw new IllegalArgumentException("batchSize 必须在 (0, 10000] 范围内");
        }
        this.dataSource = dataSource;
        this.name = name;
        this.batchSize = batchSize;
        this.currentValue = new AtomicLong(0);
        this.nextValue = new AtomicLong(0);
    }

    /**
     * 初始化序列（建表+插入记录）
     */
    public void initializeIfNotExists() {
        if (initialized) {
            return;
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(CREATE_TABLE_SQL)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new SequenceException("创建序列表失败", e);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INIT_SQL)) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.debug("序列记录可能已存在: {}", name);
        }
        initialized = true;
        log.info("数据库序列初始化完成: name={}, batchSize={}", name, batchSize);
    }

    /**
     * 生成下一个序号
     */
    @Override
    /**
     * next方法。
     * @return synchronized long类型返回值
     */
    public synchronized long next() {
        if (!initialized) {
            initializeIfNotExists();
        }

        long value = nextValue.getAndIncrement();
        if (value >= currentValue.get()) {
            // 需要从数据库获取新的号段
            fetchNextBatch();
            value = nextValue.getAndIncrement();
        }
        return value;
    }

    /**
     * 批量生成序号
     */
    @Override
    /**
     * next方法。
     *      * @param count int类型参数
     * @return long[]类型返回值
     */
    public long[] next(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count 必须大于 0");
        }
        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = next();
        }
        return ids;
    }

    /**
     * 从数据库获取下一个号段
     */
    private void fetchNextBatch() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement lockPs = conn.prepareStatement(SELECT_SQL)) {
                lockPs.setString(1, name);
                try (ResultSet rs = lockPs.executeQuery()) {
                    long current = 0;
                    if (rs.next()) {
                        current = rs.getLong("current_value");
                    } else {
                        throw new SequenceException("序列不存在: " + name + "，请先调用 initializeIfNotExists()");
                    }

                    long newValue = current + batchSize;
                    try (PreparedStatement updatePs = conn.prepareStatement(UPDATE_SQL)) {
                        updatePs.setLong(1, batchSize);
                        updatePs.setString(2, name);
                        int updated = updatePs.executeUpdate();
                        if (updated == 0) {
                            throw new SequenceException("更新序列失败: " + name);
                        }
                    }

                    currentValue.set(newValue);
                    nextValue.set(current + 1);
                    conn.commit();
                    log.debug("从数据库获取号段: name={}, start={}, end={}", name, current + 1, newValue);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw new SequenceException("从数据库获取序列号失败: " + name, e);
            }
        } catch (SQLException e) {
            throw new SequenceException("获取数据库连接失败", e);
        }
    }

    /**
     * 获取序列名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取当前值（不申请新号段）
     */
    public long getCurrentValue() {
        return currentValue.get();
    }

    /**
     * 重置序列（慎用）
     */
    public void reset(long startValue) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE sequence_info SET current_value = ? WHERE name = ?")) {
            ps.setLong(1, startValue);
            ps.setString(2, name);
            ps.executeUpdate();
            currentValue.set(startValue);
            nextValue.set(startValue);
            log.warn("序列已重置: name={}, startValue={}", name, startValue);
        } catch (SQLException e) {
            throw new SequenceException("重置序列失败: " + name, e);
        }
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return String.format("DatabaseSequence[name=%s, currentValue=%d, nextValue=%d, batchSize=%d]",
                name, currentValue.get(), nextValue.get(), batchSize);
    }
}

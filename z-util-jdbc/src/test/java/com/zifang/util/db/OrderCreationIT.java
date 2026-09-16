package com.zifang.util.db;

import com.zifang.util.db.lock.DbDistributedLock;
import com.zifang.util.distributes.sequence.NanoId;
import com.zifang.util.distributes.sequence.SegmentIdGenerator;
import com.zifang.util.distributes.sequence.UuidV7;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * 端到端场景 4：z-opc 订单创建全流程。
 * <p>
 * <ol>
 *   <li>{@code SegmentIdGenerator} 发号段 ID（避免主键热点）</li>
 *   <li>{@code DbDistributedLock} 锁住"user_id + product_id"防超卖</li>
 *   <li>{@code UuidV7} 生成幂等 key（按时间序入库）</li>
 *   <li>{@code NanoId} 短码订单号（21 字符）</li>
 * </ol>
 * <p>
 * z-opc 替换 aspectj / hutool / jedis / redisson 后的纯自研栈演示。
 */
public class OrderCreationIT {

    private static JdbcDataSource DS;

    @BeforeClass
    public static void setup() throws Exception {
        DS = new JdbcDataSource();
        DS.setURL("jdbc:h2:mem:ordertest;DB_CLOSE_DELAY=-1");
        DS.setUser("sa");
        DS.setPassword("");
        try (Connection c = DS.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE distributed_lock (" +
                    "lock_key VARCHAR(128) PRIMARY KEY, " +
                    "token VARCHAR(64) NOT NULL, " +
                    "expire_at BIGINT NOT NULL)");
            s.execute("CREATE TABLE id_segment (biz_tag VARCHAR(64) PRIMARY KEY, max_id BIGINT NOT NULL DEFAULT 0, step INT NOT NULL DEFAULT 100)");
            s.execute("CREATE TABLE t_order (id BIGINT PRIMARY KEY, order_no VARCHAR(64), user_id VARCHAR(64), product_id VARCHAR(64), amount INT, idempotency_key VARCHAR(64))");
            s.execute("INSERT INTO id_segment (biz_tag, max_id, step) VALUES ('order', 0, 100)");
        }
    }

    @AfterClass
    public static void teardown() throws Exception {
        if (DS != null) {
            try (Connection c = DS.getConnection(); Statement s = c.createStatement()) {
                s.execute("DROP ALL OBJECTS");
            }
        }
    }

    @After
    public void clearLock() throws Exception {
        // 每个 test 结束清掉锁表和 t_order，避免测试间干扰
        try (Connection c = DS.getConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM distributed_lock WHERE 1=1");
            s.execute("DELETE FROM t_order WHERE 1=1");
        } catch (Exception ignored) {
            // 表可能未建（@BeforeClass 失败时），忽略
        }
    }

    @Test
    public void testOrderCreation() throws Exception {
        OrderService svc = new OrderService(DS);
        String orderNo = svc.createOrder("alice", "PROD-001", 2);
        assertNotNull(orderNo);
        assertTrue("orderNo should start with O, got " + orderNo, orderNo.startsWith("O"));

        try (Connection c = DS.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM t_order")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    public void testConcurrentOrders_idUnique() throws Exception {
        OrderService svc = new OrderService(DS);
        int n = 20;
        Set<String> orderNos = java.util.Collections.synchronizedSet(new HashSet<>());
        Thread[] ts = new Thread[4];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < n; j++) {
                    try {
                        // 用 (thread, j) 做 user → 80 个不同 user，不互锁
                        String no = svc.createOrder("u" + Thread.currentThread().getId() + "_" + j, "p", 1);
                        orderNos.add(no);
                    } catch (Exception e) {
                        fail("unexpected: " + e.getMessage());
                    }
                }
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        assertEquals(4 * n, orderNos.size());
    }

    /**
     * 号段加载器（真实生产连 MySQL）。
     */
    static class DbSegmentLoader implements SegmentIdGenerator.SegmentLoader {
        private final DataSource ds;

        DbSegmentLoader(DataSource ds) {
            this.ds = ds;
        }

        @Override
        public long[] loadSegment(String bizTag, int step) {
            try (Connection c = ds.getConnection()) {
                c.setAutoCommit(false);
                try (PreparedStatement upd = c.prepareStatement(
                        "UPDATE id_segment SET max_id = max_id + ? WHERE biz_tag = ?")) {
                    upd.setInt(1, step);
                    upd.setString(2, bizTag);
                    upd.executeUpdate();
                }
                long[] seg = new long[2];
                try (PreparedStatement sel = c.prepareStatement(
                        "SELECT max_id FROM id_segment WHERE biz_tag = ?")) {
                    sel.setString(1, bizTag);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (rs.next()) {
                            long maxId = rs.getLong(1);
                            seg[0] = maxId - step + 1;
                            seg[1] = maxId;
                        }
                    }
                }
                c.commit();
                return seg;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class OrderService {
        private final DataSource ds;
        private final SegmentIdGenerator idGen;
        private final NanoId nanoId = new NanoId(12);

        public OrderService(DataSource ds) {
            this.ds = ds;
            this.idGen = new SegmentIdGenerator("order", 100, new DbSegmentLoader(ds));
        }

        public String createOrder(String userId, String productId, int amount) throws Exception {
            String lockKey = "order:" + userId + ":" + productId;
            DbDistributedLock lock = new DbDistributedLock(ds, lockKey, 3000);
            String token = lock.tryLock(200_000_000L);   // 200ms
            if (token == null) {
                throw new RuntimeException("system busy, try again");
            }
            try {
                long id = idGen.nextId();
                String orderNo = "O" + nanoId.next();
                String idempotencyKey = UuidV7.next();

                try (Connection c = ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(
                             "INSERT INTO t_order (id, order_no, user_id, product_id, amount, idempotency_key) " +
                                     "VALUES (?, ?, ?, ?, ?, ?)")) {
                    ps.setLong(1, id);
                    ps.setString(2, orderNo);
                    ps.setString(3, userId);
                    ps.setString(4, productId);
                    ps.setInt(5, amount);
                    ps.setString(6, idempotencyKey);
                    ps.executeUpdate();
                }
                return orderNo;
            } finally {
                lock.unlock(token);
            }
        }
    }
}

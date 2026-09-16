package com.zifang.util.db.transaction;

import com.zifang.util.db.define.Isolation;
import com.zifang.util.db.define.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * 事务管理器：管理 JDBC Connection 的获取/释放、嵌套、传播。
 * <p>
 * 核心思路：
 * <ul>
 *   <li>每个线程维护一个 {@link ConnectionHolder} 栈，栈底是"最外层"事务，栈顶是"当前"连接</li>
 *   <li>同一线程内嵌套调用 {@link #begin(Propagation, Isolation, boolean)} 时，根据传播行为决定：
 *       <ul>
 *         <li>REQUIRED / SUPPORTS / MANDATORY：复用最外层连接（最常用）</li>
 *         <li>REQUIRES_NEW：挂起外层，开新连接（压栈）</li>
 *         <li>NESTED：用外层连接的 savepoint</li>
 *         <li>NOT_SUPPORTED / NEVER：以非事务执行（不入栈）</li>
 *       </ul>
 *   </li>
 *   <li>{@link #commit()} 只在最外层 commit 时才真正提交；内层 commit 仅做 savepoint release</li>
 *   <li>异常路径：{@link #rollback()} 标记 outer 失败，最外层 commit 时检测到则回滚</li>
 * </ul>
 *
 * <h3>典型使用</h3>
 * <pre>{@code
 *   tm.begin(Propagation.REQUIRED, Isolation.DEFAULT, false);
 *   try {
 *       // 业务 SQL（自动从栈顶取连接）
 *       tm.commit();
 *   } catch (Throwable t) {
 *       tm.rollback();
 *       throw t;
 *   }
 * }</pre>
 *
 * <h3>配合 SqlExecutor</h3>
 * 调用方把 {@link #currentConnection()} 传给 SqlExecutor.execute(connection, sql, ...)，
 * 框架就会自动让"同一线程的所有 SQL 走同一个连接"，天然支持事务。
 */
public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    /**
     * 每个线程的连接栈。
     */
    private static final ThreadLocal<Deque<ConnectionHolder>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 失败标记：rollback-only 传播到最外层 commit 时使用。
     */
    private static final ThreadLocal<Boolean> ROLLBACK_ONLY = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        if (dataSource == null) throw new IllegalArgumentException("dataSource");
        this.dataSource = dataSource;
    }

    private static int toJdbc(Isolation i) {
        switch (i) {
            case READ_UNCOMMITTED:
                return Connection.TRANSACTION_READ_UNCOMMITTED;
            case READ_COMMITTED:
                return Connection.TRANSACTION_READ_COMMITTED;
            case REPEATABLE_READ:
                return Connection.TRANSACTION_REPEATABLE_READ;
            case SERIALIZABLE:
                return Connection.TRANSACTION_SERIALIZABLE;
            default:
                return -1; // 用 DB 默认
        }
    }

    private static int stackDepth(Deque<?> s) {
        return s.size();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 开启事务（简化版：默认 REQUIRED 传播 + 默认隔离 + 读写）。
     */
    public void begin() {
        begin(Propagation.REQUIRED, Isolation.DEFAULT, false);
    }

    /**
     * 开启事务。
     *
     * @param propagation 传播行为
     * @param isolation   隔离级别
     * @param readOnly    是否只读
     */
    public void begin(Propagation propagation, Isolation isolation, boolean readOnly) {
        Deque<ConnectionHolder> stack = STACK.get();
        ConnectionHolder outer = stack.peek();

        if (outer != null) {
            // 已经有活动连接 → 按传播行为处理
            switch (propagation) {
                case MANDATORY:
                    // 必须有，OK
                    log.debug("[{}] propagation=MANDATORY, reuse outer conn", stackDepth(stack));
                    return;
                case NEVER:
                    throw new TransactionException("已有活动事务，但传播行为为 NEVER");
                case NOT_SUPPORTED:
                    log.debug("[{}] propagation=NOT_SUPPORTED, run non-transactional", stackDepth(stack));
                    return; // 不入栈
                case SUPPORTS:
                case REQUIRED:
                default:
                    log.debug("[{}] propagation={}, reuse outer conn", stackDepth(stack), propagation);
                    return; // 复用最外层
                case REQUIRES_NEW:
                    log.debug("[{}] propagation=REQUIRES_NEW, suspend outer + open new", stackDepth(stack));
                    outer.suspend();
                    break;
                case NESTED:
                    log.debug("[{}] propagation=NESTED, create savepoint on outer", stackDepth(stack));
                    outer.createSavepointIfAbsent();
                    return;
            }
        }

        // 真开新连接
        ConnectionHolder holder = openConnection(isolation, readOnly);
        stack.push(holder);
        log.debug("[{}] opened new conn (autoCommit={})", stackDepth(stack), holder.conn == null ? "n/a" : "n/a");
    }

    private ConnectionHolder openConnection(Isolation isolation, boolean readOnly) {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            if (readOnly) {
                conn.setReadOnly(true);
            }
            if (isolation != Isolation.DEFAULT) {
                conn.setTransactionIsolation(toJdbc(isolation));
            }
            return new ConnectionHolder(conn);
        } catch (SQLException e) {
            throw new TransactionException("无法从 DataSource 获取 Connection: " + e.getMessage(), e);
        }
    }

    /**
     * 当前线程是否处于事务中。
     */
    public boolean isActive() {
        return !STACK.get().isEmpty();
    }

    /**
     * 当前栈深度（0 = 无事务）。
     */
    public int getDepth() {
        return STACK.get().size();
    }

    /**
     * 标记整个事务为 rollback-only（即使内层不抛异常也会回滚）。
     */
    public void setRollbackOnly() {
        ROLLBACK_ONLY.set(Boolean.TRUE);
    }

    public boolean isRollbackOnly() {
        return Boolean.TRUE.equals(ROLLBACK_ONLY.get());
    }

    /**
     * 拿到当前线程的顶层连接（无连接则返回 null）。业务代码应优先用此连接以确保在同一事务里。
     */
    public Connection currentConnection() {
        ConnectionHolder top = STACK.get().peek();
        return top == null ? null : top.conn;
    }

    /**
     * 提交。最外层 commit 才真正提交；内层只 release savepoint。
     */
    public void commit() {
        Deque<ConnectionHolder> stack = STACK.get();
        ConnectionHolder top = stack.peek();
        if (top == null) {
            throw new TransactionException("没有活动事务，无法提交");
        }
        try {
            if (top.suspendedOuter != null) {
                // REQUIRES_NEW 嵌套：释放自己的连接，恢复 outer
                top.releaseSavepoint();
                top.conn.commit();
                top.conn.close();
                ConnectionHolder outer = top.suspendedOuter;
                top.suspendedOuter = null;
                stack.pop();
                outer.resume();
                log.debug("[{}] REQUIRES_NEW committed, outer resumed", stackDepth(stack));
                return;
            }
            if (top.savepoint != null) {
                // NESTED 嵌套：只 release savepoint
                top.releaseSavepoint();
                stack.pop();
                log.debug("[{}] NESTED savepoint released", stackDepth(stack));
                return;
            }
            if (isRollbackOnly()) {
                top.conn.rollback();
                top.conn.close();
                stack.pop();
                ROLLBACK_ONLY.remove();
                log.debug("[{}] rollback-only flag set, rolled back", stackDepth(stack));
                return;
            }
            top.conn.commit();
            top.conn.close();
            stack.pop();
            log.debug("[{}] committed", stackDepth(stack));
            if (stack.isEmpty()) STACK.remove();
        } catch (SQLException e) {
            throw new TransactionException("提交失败: " + e.getMessage(), e);
        }
    }

    /**
     * 回滚。直接关连接，抛 RuntimeException 让调用方处理。
     */
    public void rollback() {
        Deque<ConnectionHolder> stack = STACK.get();
        ConnectionHolder top = stack.peek();
        if (top == null) {
            throw new TransactionException("没有活动事务，无法回滚");
        }
        try {
            if (top.savepoint != null) {
                top.conn.rollback(top.savepoint);
                top.savepoint = null;
                stack.pop();
                log.debug("[{}] NESTED rolled back to savepoint", stackDepth(stack));
                return;
            }
            top.conn.rollback();
            top.conn.close();
            stack.pop();
            ROLLBACK_ONLY.remove();
            log.debug("[{}] rolled back", stackDepth(stack));
            if (stack.isEmpty()) STACK.remove();
        } catch (SQLException e) {
            throw new TransactionException("回滚失败: " + e.getMessage(), e);
        }
    }

    /**
     * 当前线程所有活动连接的描述（调试用）。
     */
    public Map<String, Object> describe() {
        Map<String, Object> m = new HashMap<>();
        m.put("depth", getDepth());
        m.put("rollbackOnly", isRollbackOnly());
        return m;
    }

    /**
     * 每个事务持有一个连接 + 可选 savepoint + 可选挂起的外层 holder。
     */
    private static class ConnectionHolder {
        final Connection conn;
        java.sql.Savepoint savepoint;
        ConnectionHolder suspendedOuter;

        ConnectionHolder(Connection conn) {
            this.conn = conn;
        }

        void createSavepointIfAbsent() {
            try {
                if (savepoint == null) savepoint = conn.setSavepoint();
            } catch (SQLException e) {
                throw new TransactionException("setSavepoint 失败: " + e.getMessage(), e);
            }
        }

        void releaseSavepoint() {
            try {
                if (savepoint != null) {
                    conn.releaseSavepoint(savepoint);
                    savepoint = null;
                }
            } catch (SQLException e) {
                throw new TransactionException("releaseSavepoint 失败: " + e.getMessage(), e);
            }
        }

        void suspend() { /* 占位：outer 留作外层 holder */ }

        void resume() { /* 占位：恢复 outer 的 active 状态 */ }
    }

    // ===== 给模板/拦截器用的状态查询 =====

    /**
     * 事务异常。
     */
    public static class TransactionException extends RuntimeException {
        public TransactionException(String message) {
            super(message);
        }

        public TransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

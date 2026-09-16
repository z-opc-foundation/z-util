package com.zifang.util.db.transaction;

import com.zifang.util.db.define.Isolation;
import com.zifang.util.db.define.Propagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 编程式事务模板，简化事务代码。
 * <p>
 * 通过回调模式，把"begin / commit / rollback"封装在模板里，业务代码只需关注业务逻辑。
 *
 * <h3>典型使用</h3>
 * <pre>{@code
 *   transactionTemplate.execute(() -> {
 *       repository.save(order);
 *       repository.save(orderItem);
 *   });
 *
 *   Long id = transactionTemplate.executeWithResult(() -> {
 *       repository.save(order);
 *       return order.getId();
 *   });
 * }</pre>
 *
 * @see TransactionManager
 */
public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private TransactionManager transactionManager;
    private Propagation propagation = Propagation.REQUIRED;
    private Isolation isolation = Isolation.DEFAULT;
    private boolean readOnly = false;

    public TransactionTemplate() {
    }

    public TransactionTemplate(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setPropagation(Propagation propagation) {
        this.propagation = propagation;
    }

    public void setIsolation(Isolation isolation) {
        this.isolation = isolation;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * 执行事务，无返回值。
     */
    public void execute(TransactionAction action) {
        executeWithResult(() -> {
            action.execute();
            return null;
        });
    }

    /**
     * 执行事务，有返回值。
     */
    public <T> T executeWithResult(TransactionFunction<T> action) {
        TransactionManager tm = getTransactionManager();
        boolean isNewTransaction = !tm.isActive();
        if (isNewTransaction) {
            tm.begin(propagation, isolation, readOnly);
        }
        try {
            T result = action.execute();
            if (isNewTransaction && tm.isActive()) {
                tm.commit();
            }
            return result;
        } catch (RuntimeException e) {
            if (isNewTransaction && tm.isActive()) {
                tm.rollback();
            }
            throw e;
        } catch (Error e) {
            if (isNewTransaction && tm.isActive()) {
                tm.rollback();
            }
            throw e;
        } catch (Throwable t) {
            if (isNewTransaction && tm.isActive()) {
                tm.rollback();
            }
            throw new TransactionManager.TransactionException("事务执行失败: " + t.getMessage(), t);
        }
    }

    private TransactionManager getTransactionManager() {
        if (transactionManager == null) {
            throw new TransactionManager.TransactionException("TransactionManager 未设置");
        }
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager tm) {
        this.transactionManager = tm;
    }

    @FunctionalInterface
    public interface TransactionAction {
        void execute();
    }

    @FunctionalInterface
    public interface TransactionFunction<T> {
        T execute();
    }
}

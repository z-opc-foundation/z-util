package com.zifang.util.db.transaction;

import com.zifang.util.db.define.Transactional;
import com.zifang.util.proxy.aspects.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * 事务拦截器，实现 {@link Aspect} 接口，
 * 拦截标注了 {@link Transactional} 的方法，自动管理事务。
 *
 * <p>该拦截器通过 AOP 机制工作，配合代理框架使用：
 * <ul>
 *   <li>方法执行前：开启事务（根据传播行为）</li>
 *   <li>方法正常返回：提交事务</li>
 *   <li>方法抛出异常：根据 rollbackFor / noRollbackFor 判断是否回滚</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * TransactionInterceptor interceptor = new TransactionInterceptor(transactionManager);
 * proxyFactory.addAspect(interceptor);
 * </pre>
 */
public class TransactionInterceptor implements Aspect {

    private static final Logger log = LoggerFactory.getLogger(TransactionInterceptor.class);

    private final TransactionManager transactionManager;

    public TransactionInterceptor(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public boolean before(Object target, Method method, Object[] args) {
        Transactional transactional = findTransactional(target.getClass(), method);
        if (transactional == null) {
            return true; // 不拦截无 @Transactional 标注的方法
        }

        boolean isNewTransaction = !transactionManager.isActive();
        if (isNewTransaction) {
            transactionManager.begin(
                    transactional.propagation(),
                    transactional.isolation(),
                    transactional.readOnly()
            );
            log.debug("事务拦截器开启事务: method={}, propagation={}, isolation={}",
                    method.getName(), transactional.propagation(), transactional.isolation());
        } else {
            log.debug("事务拦截器加入已存在事务: method={}", method.getName());
        }
        return true;
    }

    @Override
    public boolean after(Object target, Method method, Object[] args, Object returnVal) {
        Transactional transactional = findTransactional(target.getClass(), method);
        if (transactional == null) return true;
        if (transactionManager.isActive()) {
            try {
                transactionManager.commit();
            } catch (Exception e) {
                log.error("事务提交失败: method={}", method.getName(), e);
                throw new TransactionManager.TransactionException("事务提交失败", e);
            }
        }
        return true;
    }

    @Override
    public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
        Transactional transactional = findTransactional(target.getClass(), method);
        if (transactional == null) return true;

        boolean shouldRollback = shouldRollback(transactional, e);
        if (shouldRollback) {
            log.warn("事务回滚，method={}, exception={}", method.getName(), e.getMessage());
            if (transactionManager.isActive()) {
                transactionManager.rollback();
            }
        } else {
            log.debug("异常不在回滚范围内，尝试提交: method={}, exception={}",
                    method.getName(), e.getMessage());
            if (transactionManager.isActive()) {
                try {
                    transactionManager.commit();
                } catch (Exception commitEx) {
                    log.error("提交失败（异常后）: method={}", method.getName(), commitEx);
                    throw new TransactionManager.TransactionException("提交失败", commitEx);
                }
            }
        }
        return true;
    }

    private Transactional findTransactional(Class<?> clazz, Method method) {
        Transactional methodTx = method.getAnnotation(Transactional.class);
        if (methodTx != null) return methodTx;
        return clazz.getAnnotation(Transactional.class);
    }

    private boolean shouldRollback(Transactional transactional, Throwable e) {
        Class<? extends Throwable> exClass = e.getClass();

        for (Class<? extends Throwable> noRollback : transactional.noRollbackFor()) {
            if (noRollback.isAssignableFrom(exClass)) return false;
        }
        String exName = exClass.getName();
        for (String noRollbackName : transactional.noRollbackForClassName()) {
            if (noRollbackName.equals(exName)) return false;
        }
        if (transactional.rollbackFor().length > 0) {
            for (Class<? extends Throwable> rollbackFor : transactional.rollbackFor()) {
                if (rollbackFor.isAssignableFrom(exClass)) return true;
            }
            return false;
        }
        if (transactional.rollbackForClassName().length > 0) {
            for (String rollbackForName : transactional.rollbackForClassName()) {
                if (rollbackForName.equals(exName)) return true;
                try {
                    if (Class.forName(rollbackForName).isAssignableFrom(exClass)) return true;
                } catch (ClassNotFoundException ignored) {
                }
            }
            return false;
        }
        return (e instanceof RuntimeException) || (e instanceof Error);
    }
}

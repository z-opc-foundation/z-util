package com.zifang.util.db.define;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 事务注解，支持类和方法级别
 * <p>
 * 标注在类上时，该类所有方法均受事务管理；
 * 标注在方法上时，仅该方法受事务管理。
 * 方法级注解优先于类级注解。
 *
 * <p>使用示例：
 * <pre>
 * &#64;Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
 * public void transfer(Account from, Account to, BigDecimal amount) {
 *     // 业务逻辑，发生异常自动回滚
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
/**
 * Transactional注解。
 */
public @interface Transactional {

    /**
     * 事务传播行为，默认为 REQUIRED
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事务隔离级别，默认为数据库默认级别
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * 是否只读事务，默认 false
     */
    boolean readOnly() default false;

    /**
     * 触发回滚的异常类型数组，默认为 RuntimeException 和 Error
     * 设置为空数组则所有异常均不回滚
     */
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * 触发回滚的异常类名数组，与 rollbackFor 二选一
     */
    String[] rollbackForClassName() default {};

    /**
     * 不触发回滚的异常类型数组
     */
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * 不触发回滚的异常类名数组
     */
    String[] noRollbackForClassName() default {};
}

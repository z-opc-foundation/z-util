package com.zifang.util.workflow.annoation;

import java.lang.annotation.*;

/**
 * 引擎服务拦截器注解。
 * <p>
 * 标记一个类为工作流引擎服务的拦截器。
 * 用于在服务执行前后进行拦截处理，如日志记录、性能监控、事务管理等。
 *
 * @see com.zifang.util.workflow.engine.interfaces.AbstractEngineService
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * EngineServiceInterceptor注解。
 */
public @interface EngineServiceInterceptor {

}

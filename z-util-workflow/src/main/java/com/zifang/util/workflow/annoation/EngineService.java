package com.zifang.util.workflow.annoation;


import java.lang.annotation.*;

/**
 * 引擎服务注解。
 * <p>
 * 标记一个类为工作流引擎服务实现类。
 * 用于自动注册和发现引擎服务。
 * <p>
 * 属性：
 * <ul>
 *   <li>name - 服务单元名称，用于唯一标识服务</li>
 *   <li>engine - 所属引擎类型，默认为"spark"</li>
 * </ul>
 *
 * @see com.zifang.util.workflow.engine.interfaces.AbstractEngineService
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * EngineService注解。
 */
public @interface EngineService {
    /**
     * 服务单元名称，用于唯一标识服务。
     *
     * @return 服务名称
     */
    String name();

    /**
     * 所属引擎类型。
     *
     * @return 引擎类型，默认"spark"
     */
    String engine() default "spark";
}

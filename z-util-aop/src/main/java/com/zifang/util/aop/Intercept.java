package com.zifang.util.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自研 AOP 切点注解：标注在方法上，等同于 AspectJ 的 {@code @Around}。
 * <p>
 * 用法：业务方法标注此注解 + {@link Advise} 实现拦截逻辑，
 * 通过 {@link ProxyFactory#wrap} 生成代理。
 *
 * <pre>{@code
 *   public class MyService {
 *       @Intercept(MyAdvise.class)
 *       public String hello() { return "hi"; }
 *   }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Intercept {

    /**
     * 拦截器实现类。必须有无参构造。
     */
    Class<? extends Advise>[] value();
}

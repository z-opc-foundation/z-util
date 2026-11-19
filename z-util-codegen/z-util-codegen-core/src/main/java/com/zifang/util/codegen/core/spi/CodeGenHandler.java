package com.zifang.util.codegen.core.spi;

import java.util.Set;

/**
 * 代码生成 handler：引擎的可扩展点。
 *
 * <p>一个 handler 声明自己关心的注解全限定名，引擎在注解处理轮次里把命中的元素交给它。
 * handler 通过 {@link HandlerContext} 拿到类型模型、成员注入口与源码生成口。</p>
 *
 * <p>实现类通过 {@code META-INF/services/com.zifang.util.codegen.core.spi.CodeGenHandler} 注册，
 * 由 {@code ServiceLoader} 发现。</p>
 *
 * @author zifang
 */
public interface CodeGenHandler {

    /** 本 handler 关心的注解全限定名。 */
    Set<String> supportedAnnotations();

    /** 处理一个被注解的元素。 */
    void handle(HandlerContext context);
}

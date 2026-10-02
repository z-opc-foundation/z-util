package com.zifang.util.aop.proxy;

import com.zifang.util.aop.aopgen.AspectToHookAdapter;
import com.zifang.util.aop.aopgen.SourceProxyFactory;
import com.zifang.util.aop.aspects.Aspect;

/**
 * 类代理切面工厂（自研，替代 cglib Enhancer）。
 * <p>
 * 通过 {@link SourceProxyFactory} 生成目标子类源码 + 内存编译 + 自定义 ClassLoader
 * 定义，实现"不要求目标实现接口"的子类代理——与 cglib Enhancer 等价，但零三方依赖、
 * 纯 JDK（javax.tools 编译）+ 本模块 ASM 编织，Java 8 直接可用（无模块系统限制）。
 * <p>
 * 范围限制见 {@link SourceProxyFactory}：仅目标类自身声明的 public 非 static 非 final
 * 非 synthetic 方法可被代理。
 */
public class ClassProxyFactory extends ProxyFactory {
    private static final long serialVersionUID = 1L;

    @Override
    public <T> T proxy(T target, Aspect aspect) {
        return SourceProxyFactory.proxy(target, new AspectToHookAdapter(aspect, target));
    }
}

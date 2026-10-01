package com.zifang.util.aop.aopgen;

import com.zifang.util.aop.aspects.Aspect;

import java.lang.reflect.Method;

/**
 * 把既有的 {@link Aspect} 契约适配成 {@link MethodHook}，让 SourceProxyFactory 生成的
 * 字节码代理可以直接复用 aop 模块的切面定义，而不必每个 Aspect 都重写一遍钩子接口。
 * <p>
 * 缺点：方法按 obj 形参数查找（EchoService 这类无重载目标够用；多版本目标需要
 * 拓展解析）。
 */
public class AspectToHookAdapter implements MethodHook {

    private final Aspect aspect;
    private final Object target;
    private Object[] currentArgs;

    public AspectToHookAdapter(Aspect aspect, Object target) {
        this.aspect = aspect;
        this.target = target;
    }

    @Override
    public void before(String method, Object[] args) {
        this.currentArgs = args;
        Method m = resolve(method, args);
        aspect.before(target, m, args);
    }

    @Override
    public void after(String method, Object result) {
        Method m = resolve(method, currentArgs);
        Object[] args = currentArgs;
        this.currentArgs = null;
        aspect.after(target, m, args, result);
    }

    @Override
    public void afterException(String method, Throwable t) {
        Method m = resolve(method, currentArgs);
        Object[] args = currentArgs;
        this.currentArgs = null;
        aspect.afterException(target, m, args, t);
    }

    private Method resolve(String name, Object[] args) {
        int arity = args == null ? 0 : args.length;
        for (Method candidate : target.getClass().getDeclaredMethods()) {
            if (candidate.getName().equals(name) && candidate.getParameterCount() == arity) {
                return candidate;
            }
        }
        throw new IllegalStateException("无法为目标 " + target.getClass().getName()
                + " 解析 " + name + "/" + arity + "，多版本方法需要扩展解析");
    }
}
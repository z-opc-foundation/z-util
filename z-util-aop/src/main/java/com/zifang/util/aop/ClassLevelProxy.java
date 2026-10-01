package com.zifang.util.aop;

import com.zifang.util.proxy.aopgen.AspectToHookAdapter;
import com.zifang.util.proxy.aopgen.SourceProxyFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 类级别字节码代理入口：不要求目标实现任何接口（绕过 {@link ProxyFactory#wrap(Object)} 的
 * JDK Proxy 接口限制），通过 {@link SourceProxyFactory} 生成子类源码 + 内存编译 + 自定义
 * ClassLoader 定义。
 * <p>
 * 当前语义限制：{@link Advise#around} 的 {@code chain.proceed()} 实际在子类源码里
 * 已以 {@code super.method(...)} 形式展开过——所以传给 {@link Advise} 的 {@link Advise.Chain}
 * 会立刻返回结果；Advise 实现应当在 {@code around} 里"早一次"+ 再 proceed() 取结果，链式
 * 嵌套（如 try-finally + 二次 proceed）不在当前支持范围。
 *
 * <h3>用法</h3>
 * <pre>{@code
 *   GreeterConcrete svc = new GreeterConcrete();
 *   GreeterConcrete proxy = ClassLevelProxy.wrap(svc, LogAdvise.class);
 *   proxy.hello();  // LogAdvise.around 被调一次（前置日志+链返回=super.hello 的结果+后置日志）
 * }</pre>
 */
public final class ClassLevelProxy {

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Advise>, Advise> ADVICE_CACHE = new ConcurrentHashMap<>();

    private ClassLevelProxy() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target, Class<? extends Advise> adviseClass) {
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        if (adviseClass == null) {
            throw new NullPointerException("adviseClass must not be null");
        }
        Advise<T> advise = (Advise<T>) ADVICE_CACHE.computeIfAbsent(adviseClass, c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("advise must have no-arg constructor: " + c, e);
            }
        });
        return SourceProxyFactory.proxy(target, new AdviseToHookAdapter(advise, target));
    }

    /**
     * 把 {@link Advise} 转成 {@link SourceProxyFactory} 期望的 MethodHook。
     * 在 after() 钩子里调 {@code advise.around}，把"super 实际已跑完的结果"作为 chain 回报。
     * <p>
     * 限制：{@code Advise} 不可能在 super 之前拦住（字节码生成时 super 已展开）；
     * 不能用于改返回值或吞异常的 Advise，只能用于日志/计时类。
     */
    private static final class AdviseToHookAdapter
            extends AspectToHookAdapter {

        AdviseToHookAdapter(Advise advice, Object target) {
            super(new ChainedAspect(advice, target), target);
        }
    }

    /**
     * 把 Advise 包装成 Aspect：before 永远放行，after 把 super 结果塞给 Advise.around 的 chain。
     */
    private static final class ChainedAspect implements com.zifang.util.proxy.aspects.Aspect {
        private final Advise advise;
        @SuppressWarnings("unused")
        private final Object target;

        ChainedAspect(Advise advise, Object target) {
            this.advise = advise;
            this.target = target;
        }

        @Override
        public boolean before(Object target, Method method, Object[] args) {
            return true;
        }

        @Override
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            try {
                Advise.Chain chain = () -> returnVal;
                advise.around(target, method, args, chain);
            } catch (Throwable t) {
                System.err.println("[ClassLevelProxy] advise threw: " + t);
            }
            return true;
        }

        @Override
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            return true;
        }
    }
}
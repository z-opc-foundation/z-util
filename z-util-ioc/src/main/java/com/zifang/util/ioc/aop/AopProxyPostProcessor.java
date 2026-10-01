package com.zifang.util.ioc.aop;

import com.zifang.util.aop.Advise;
import com.zifang.util.aop.ProxyFactory;
import com.zifang.util.ioc.Injector;
import com.zifang.util.ioc.exception.ProvisionException;
import com.zifang.util.ioc.metadata.BeanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Bean 后处理器：检测 BeanDefinition 是否匹配 AOP 拦截规则，
 * 若匹配则使用 z-util-aop 的 {@link ProxyFactory} 生成代理。
 *
 * <p>代理策略：对每个匹配的方法，{@link com.zifang.util.aop.Intercept} 注解将在运行时由
 * ProxyFactory 读取；此处采用一种动态注册方式——通过临时生成 wrapper 类或直接在调用前将
 * advise 链应用到 {@code @Intercept} 注解上。
 *
 * <h3>实现要点：</h3>
 * <ol>
 *   <li>本实现采取「运行时附加代理」策略：保留原 Bean 实例，通过
 *       {@link ProxyFactory#wrap(Object)} 创建代理并把 advise 链写入代理。</li>
 *   <li>由于 {@link com.zifang.util.aop.Intercept} 是方法级注解，本实现额外提供一个
 *       动态 {@code AdviseChainWrapper} —— 任何匹配的方法都会先被 advise 链包裹。</li>
 * </ol>
 */
public class AopProxyPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(AopProxyPostProcessor.class);

    private final List<InterceptorBinding> bindings;
    private final ConcurrentMap<Class<?>, List<InterceptorBinding>> classIndex = new ConcurrentHashMap<>();

    public AopProxyPostProcessor(List<InterceptorBinding> bindings) {
        this.bindings = bindings;
        if (bindings != null) {
            for (InterceptorBinding b : bindings) {
                // 预热索引（无实际作用，保留扩展点）
            }
        }
    }

    private static Class<?>[] collectInterfaces(Class<?> klass) {
        java.util.LinkedHashSet<Class<?>> set = new java.util.LinkedHashSet<>();
        while (klass != null) {
            for (Class<?> i : klass.getInterfaces()) set.add(i);
            klass = klass.getSuperclass();
        }
        return set.toArray(new Class<?>[0]);
    }

    /**
     * 若 BeanDefinition 指向的实例匹配任何拦截规则，包装为代理；
     * 否则返回原实例。
     *
     * @param bd          Bean 定义
     * @param rawInstance 刚创建出的原始实例
     * @return 可能被包装的实例
     */
    public Object postProcess(BeanDefinition bd, Object rawInstance, Injector injector) {
        if (rawInstance == null || bindings == null || bindings.isEmpty()) {
            return rawInstance;
        }
        Class<?> beanClass = rawInstance.getClass();
        List<InterceptorBinding> matched = findMatched(beanClass);
        if (matched.isEmpty()) {
            return rawInstance;
        }
        // 仅代理实现了接口的对象（沿用 z-util-aop 的 JDK Proxy 限制）
        if (beanClass.getInterfaces().length == 0) {
            log.warn("AOP skipped: bean {} has no interface; JDK proxy requires interface.", beanClass.getName());
            return rawInstance;
        }
        return wrapWithAdvises(rawInstance, matched, injector);
    }

    private List<InterceptorBinding> findMatched(Class<?> beanClass) {
        List<InterceptorBinding> matched = classIndex.get(beanClass);
        if (matched != null) {
            return matched;
        }
        matched = new ArrayList<>();
        for (InterceptorBinding b : bindings) {
            if (b.matchesClass(beanClass)) {
                matched.add(b);
            }
        }
        // 注意：此处不做缓存，因为同一类被多 Module 配置时需要动态合并
        return matched;
    }

    /**
     * 使用自定义 InvocationHandler 包装原始 Bean。
     * <p>
     * 由于 {@link com.zifang.util.aop.Intercept} 是注解（静态方法上），
     * 而 AOP 规则是动态的，本实现直接基于 JDK Proxy 自定义 InvocationHandler，
     * 在调用时按方法签名查找匹配规则。
     */
    private Object wrapWithAdvises(Object target, List<InterceptorBinding> bindings, Injector injector) {
        // 借助 ProxyFactory.wrap 创建代理，但需解决：原 ProxyFactory 强依赖 @Intercept 注解
        // 这里通过生成代理后立刻替换 InvocationHandler 的方式不可行（ProxyFactory 是 final）。
        // 因此采用代理 InvocationHandler 的子方案：
        java.lang.reflect.InvocationHandler handler = new AdviseChainHandler<>(target, bindings, injector);
        Class<?>[] interfaces = collectInterfaces(target.getClass());
        if (interfaces.length == 0) {
            return target;
        }
        return java.lang.reflect.Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                handler);
    }

    /**
     * 动态 Advise 链的 InvocationHandler。
     */
    private static final class AdviseChainHandler<T> implements java.lang.reflect.InvocationHandler {
        private final T target;
        private final List<InterceptorBinding> bindings;
        @SuppressWarnings("unused")
        private final Injector injector;

        AdviseChainHandler(T target, List<InterceptorBinding> bindings, Injector injector) {
            this.target = target;
            this.bindings = bindings;
            this.injector = injector;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // Object 自带方法直接透传
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }
            List<Advise<T>> advises = collectAdvises(method);
            if (advises.isEmpty()) {
                return method.invoke(target, args);
            }
            return runChain(target, method, args, advises, 0);
        }

        private List<Advise<T>> collectAdvises(Method method) {
            List<Advise<T>> result = new ArrayList<>();
            for (InterceptorBinding b : bindings) {
                if (b.matchesMethod(method)) {
                    for (Class<? extends Advise> c : b.getAdvises()) {
                        try {
                            @SuppressWarnings("unchecked")
                            Advise<T> a = (Advise<T>) c.getDeclaredConstructor().newInstance();
                            result.add(a);
                        } catch (ReflectiveOperationException e) {
                            throw new ProvisionException("Cannot instantiate advise: " + c.getName(), e);
                        }
                    }
                }
            }
            return result;
        }

        private Object runChain(T target, Method method, Object[] args,
                                List<Advise<T>> advises, int idx) throws Throwable {
            if (idx == advises.size()) {
                try {
                    return method.invoke(target, args);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    throw ite.getCause();
                }
            }
            Advise<T> current = advises.get(idx);
            int next = idx + 1;
            return current.around(target, method, args, () -> runChain(target, method, args, advises, next));
        }
    }
}

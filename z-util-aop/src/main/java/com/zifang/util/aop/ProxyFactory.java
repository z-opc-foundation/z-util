package com.zifang.util.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 自研 JDK 动态代理工厂（无 aspectj / cglib / spring-aop 依赖）。
 * <p>
 * 用法：
 * <pre>{@code
 *   MyService svc = new MyService();
 *   MyService proxy = ProxyFactory.wrap(svc);
 *   // 调用 proxy.hello() 会自动按方法上的 @Intercept 注解链式触发 advise
 * }</pre>
 *
 * <h3>设计取舍</h3>
 * <ul>
 *   <li><b>仅接口代理</b>：与 JDK Proxy 保持一致，零三方依赖。
 *       如需类代理请用 spring-aop 或 cglib（已知有这些能力时不重复造）。</li>
 *   <li><b>advice 链</b>：同一方法多个 advise，按 {@code @Intercept.value()} 数组顺序串行执行。</li>
 *   <li><b>advice 实例</b>：默认每个目标对象复用同一组 advise 实例（首次创建后缓存）。
 *       若 advice 需要有状态隔离，请让 advise 内部自行同步。</li>
 *   <li><b>异常</b>：通过 {@link Throwable} 透传（不强制 {@code Exception}），advice 可抛 Error/任意 checked。</li>
 * </ul>
 */
public final class ProxyFactory {

    /**
     * advice 实例缓存：adviseClass → 实例。
     */
    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Class<? extends Advise>, Advise> ADVICE_CACHE = new ConcurrentHashMap<>();

    private ProxyFactory() {
    }

    /**
     * 给目标对象生成代理。目标对象必须实现至少一个接口（{@link Proxy} 限制）。
     *
     * @param target 真实目标
     * @param <T>    目标接口类型
     * @return 代理实例，类型为目标对象最具体的接口
     */
    @SuppressWarnings("unchecked")
    public static <T> T wrap(T target) {
        if (target == null) throw new NullPointerException("target must not be null");
        Class<?>[] interfaces = collectInterfaces(target.getClass());
        if (interfaces.length == 0) {
            throw new IllegalArgumentException(
                    "target must implement at least one interface (JDK Proxy limit): " + target.getClass());
        }
        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                new InterceptHandler<>(target));
    }

    /**
     * 收集 target 类的所有接口（包括父类链上的）。
     */
    private static Class<?>[] collectInterfaces(Class<?> klass) {
        java.util.LinkedHashSet<Class<?>> set = new java.util.LinkedHashSet<>();
        while (klass != null) {
            for (Class<?> i : klass.getInterfaces()) set.add(i);
            klass = klass.getSuperclass();
        }
        return set.toArray(new Class<?>[0]);
    }

    @SuppressWarnings("unchecked")
    private static <T> Advise<T> newAdvice(Class<? extends Advise> clazz) {
        return ADVICE_CACHE.computeIfAbsent(clazz, c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("advise must have no-arg constructor: " + c, e);
            }
        });
    }

    /**
     * 内部：拦截处理器。
     */
    private static final class InterceptHandler<T> implements InvocationHandler {
        private final T target;

        InterceptHandler(T target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1) 不拦截 Object 自带方法（hashCode/equals/toString），避免 advice 链空跑
            Intercept ann = findIntercept(method, target);
            if (ann == null) {
                return method.invoke(target, args);
            }
            // 2) 构造 advice 链
            List<Advise<T>> advises = new ArrayList<>(ann.value().length);
            for (Class<? extends Advise> c : ann.value()) {
                advises.add(newAdvice(c));
            }
            return runChain(target, method, args, advises, 0);
        }

        /**
         * JDK Proxy 进来的 method 是接口方法（注解 @Intercept 在实现类上），
         * 所以要按方法签名（name + 参数类型）在 target 类上找到具体方法，再读注解。
         */
        private Intercept findIntercept(Method interfaceMethod, Object target) {
            try {
                Method impl = target.getClass().getMethod(
                        interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                return impl.getAnnotation(Intercept.class);
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        private Object runChain(T target, Method method, Object[] args,
                                List<Advise<T>> advises, int idx) throws Throwable {
            if (idx == advises.size()) {
                // 链尾：调真实方法
                try {
                    return method.invoke(target, args);
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    // 拆包装，把真实异常向上抛（advice 见到的是原始异常）
                    throw ite.getCause();
                }
            }
            Advise<T> current = advises.get(idx);
            int next = idx + 1;
            return current.around(target, method, args, () -> runChain(target, method, args, advises, next));
        }
    }
}

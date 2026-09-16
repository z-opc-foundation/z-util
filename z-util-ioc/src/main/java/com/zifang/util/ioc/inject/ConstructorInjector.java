package com.zifang.util.ioc.inject;

import com.zifang.util.ioc.Injector;
import com.zifang.util.ioc.exception.ProvisionException;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * 构造器注入器，对标 Guice 的构造函数注入策略。
 *
 * <h3>解析顺序：</h3>
 * <ol>
 *   <li>查找带 {@link Inject} 注解的构造函数（JSR 330 标准）</li>
 *   <li>若只有一个 public 构造函数，使用之</li>
 *   <li>否则使用无参构造函数（仅在没有更优选项时）</li>
 * </ol>
 *
 * <p>按构造器参数的类型与限定符从容器解析依赖，参数可为 {@link javax.inject.Provider} 实现延迟获取。
 */
public class ConstructorInjector {

    private final Injector injector;

    public ConstructorInjector(Injector injector) {
        this.injector = injector;
    }

    /**
     * 选取合适的构造函数。
     */
    public Constructor<?> selectConstructor(Class<?> clazz) {
        Constructor<?> injectCtor = null;
        Constructor<?> noArgCtor = null;
        Constructor<?> singlePublicCtor = null;
        Constructor<?>[] ctors = clazz.getDeclaredConstructors();
        int publicCount = 0;
        for (Constructor<?> c : ctors) {
            if (Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers())) {
                publicCount++;
            }
            if (c.isAnnotationPresent(Inject.class)) {
                injectCtor = c;
            } else if (c.getParameterCount() == 0) {
                noArgCtor = c;
            }
            if (Modifier.isPublic(c.getModifiers())) {
                singlePublicCtor = c;
            }
        }
        if (injectCtor != null) {
            return injectCtor;
        }
        if (publicCount == 1) {
            return singlePublicCtor;
        }
        if (noArgCtor != null) {
            return noArgCtor;
        }
        throw new ProvisionException("No suitable constructor found for " + clazz.getName()
                + " (need @Inject constructor, single public constructor, or no-arg constructor)");
    }

    /**
     * 实例化 clazz，按构造器参数注入。
     */
    public <T> T instantiate(Class<T> clazz) {
        Constructor<?> ctor = selectConstructor(clazz);
        ctor.setAccessible(true);
        Parameter[] params = ctor.getParameters();
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            args[i] = resolveParameter(params[i]);
        }
        try {
            @SuppressWarnings("unchecked")
            T instance = (T) ctor.newInstance(args);
            return instance;
        } catch (InvocationTargetException ite) {
            throw new ProvisionException("Constructor invocation failed for " + clazz.getName(), ite.getCause());
        } catch (ReflectiveOperationException roe) {
            throw new ProvisionException("Failed to instantiate " + clazz.getName(), roe);
        }
    }

    private Object resolveParameter(Parameter param) {
        return resolveParameter(param, -1);
    }

    private Object resolveParameter(Parameter param, int index) {
        InjectionPoint ip = InjectionPoint.forParameter(param, index);
        if (ip.isProvider()) {
            return new com.zifang.util.ioc.provider.InternalProvider<>(
                    injector, ip.getRequiredType(), ip.getQualifier());
        }
        return injector.getInstance(ip.getRequiredType(), ip.getQualifier());
    }
}

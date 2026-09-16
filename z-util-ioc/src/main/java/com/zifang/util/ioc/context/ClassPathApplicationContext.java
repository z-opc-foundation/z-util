package com.zifang.util.ioc.context;

import com.zifang.util.ioc.Injector;
import com.zifang.util.ioc.Module;
import com.zifang.util.ioc.annotation.Bean;
import com.zifang.util.ioc.annotation.Component;
import com.zifang.util.ioc.annotation.Configuration;
import com.zifang.util.ioc.binder.AbstractModule;
import com.zifang.util.ioc.core.DefaultInjector;
import com.zifang.util.ioc.exception.NoSuchBeanException;
import com.zifang.util.ioc.metadata.BeanDefinition;
import com.zifang.util.ioc.metadata.Scope;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * IOC 容器上下文（向后兼容入口）。
 *
 * <p>内部委托给新的 {@link DefaultInjector}。
 * 新代码建议直接使用 {@link Injector#createInjector(Module...)}。
 *
 * <h3>使用模式：</h3>
 * <pre>{@code
 * ClassPathApplicationContext ctx = new ClassPathApplicationContext("com.example");
 * ctx.registerBeanClass(MyService.class);
 * ctx.init();
 * MyService svc = ctx.getBean(MyService.class);
 * ctx.close();
 * }</pre>
 */
public class ClassPathApplicationContext {

    private final String basePackage;
    private final List<Class<?>> pendingRegistrations = new ArrayList<>();
    private DefaultInjector injector;
    private volatile boolean initialized = false;

    public ClassPathApplicationContext(String basePackage) {
        this.basePackage = basePackage;
    }

    /**
     * 手动注册一个类（延迟到 {@link #init()} 时生效）。
     */
    public ClassPathApplicationContext registerBeanClass(Class<?> clazz) {
        pendingRegistrations.add(clazz);
        return this;
    }

    /**
     * 触发容器初始化。
     */
    public void init() {
        if (initialized) return;
        final ClassPathApplicationContext self = this;
        Module registrationModule = new AbstractModule() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            protected void configure() {
                for (Class<?> clazz : pendingRegistrations) {
                    String name = self.resolveBeanName(clazz);
                    Scope scope = self.detectScope(clazz);
                    boolean isConfig = clazz.isAnnotationPresent(Configuration.class);
                    if (isConfig) {
                        bind(clazz).to((Class) clazz).in(scope);
                    } else {
                        bind(clazz).to((Class) clazz).in(scope);
                    }
                }
            }
        };
        injector = new DefaultInjector(registrationModule);
        initialized = true;
    }

    /**
     * 解析 Bean 名称，优先级：
     * <ol>
     *   <li>{@link Named#value()}</li>
     *   <li>{@link Component#value()}</li>
     *   <li>类名首字母小写</li>
     * </ol>
     */
    String resolveBeanName(Class<?> clazz) {
        Named named = clazz.getAnnotation(Named.class);
        if (named != null && !named.value().isEmpty()) {
            return named.value();
        }
        Component comp = clazz.getAnnotation(Component.class);
        if (comp != null && !comp.value().isEmpty()) {
            return comp.value();
        }
        return toLowerFirst(clazz.getSimpleName());
    }

    String resolveBeanMethodName(Method m) {
        Named named = m.getAnnotation(Named.class);
        if (named != null && !named.value().isEmpty()) {
            return named.value();
        }
        Bean bean = m.getAnnotation(Bean.class);
        if (bean != null && !bean.value().isEmpty()) {
            return bean.value();
        }
        return m.getName();
    }

    /**
     * 检测作用域。
     */
    Scope detectScope(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Singleton.class)) {
            return Scope.SINGLETON;
        }
        Component comp = clazz.getAnnotation(Component.class);
        if (comp != null && "prototype".equals(comp.scope())) {
            return Scope.PROTOTYPE;
        }
        return Scope.DEFAULT;
    }

    private String toLowerFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * 获取 Bean 实例（按类型）。
     */
    public <T> T getBean(Class<T> type) {
        checkInit();
        return injector.getInstance(type);
    }

    /**
     * 获取 Bean 实例（按名称）。
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        checkInit();
        for (BeanDefinition bd : injector.getAllBeanDefinitions()) {
            if (name.equals(bd.getName())) {
                return (T) injector.getInstance(bd.getBeanClass(), bd.getKey().getQualifier());
            }
            if (name.equals(toLowerFirst(bd.getBeanClass().getSimpleName()))) {
                return (T) injector.getInstance(bd.getBeanClass());
            }
        }
        throw new NoSuchBeanException("No bean named: " + name);
    }

    public boolean containsBean(String name) {
        if (!initialized) return false;
        for (BeanDefinition bd : injector.getAllBeanDefinitions()) {
            if (name.equals(bd.getName())) {
                return true;
            }
            if (name.equals(toLowerFirst(bd.getBeanClass().getSimpleName()))) {
                return true;
            }
        }
        return false;
    }

    public Collection<BeanDefinition> getAllBeanDefinitions() {
        return initialized ? injector.getAllBeanDefinitions() : java.util.Collections.emptyList();
    }

    /**
     * 关闭容器：调用所有 PreDestroy（逆序）。
     */
    public void close() {
        if (injector != null) {
            injector.close();
        }
        initialized = false;
    }

    private void checkInit() {
        if (!initialized) {
            throw new IllegalStateException("Container not initialized. Call init() first.");
        }
    }
}

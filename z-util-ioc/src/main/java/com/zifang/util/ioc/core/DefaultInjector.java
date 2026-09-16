package com.zifang.util.ioc.core;

import com.zifang.util.ioc.Injector;
import com.zifang.util.ioc.Module;
import com.zifang.util.ioc.aop.AopProxyPostProcessor;
import com.zifang.util.ioc.binder.DefaultBinder;
import com.zifang.util.ioc.exception.*;
import com.zifang.util.ioc.inject.ConstructorInjector;
import com.zifang.util.ioc.inject.FieldInjector;
import com.zifang.util.ioc.lifecycle.LifecycleManager;
import com.zifang.util.ioc.metadata.BeanDefinition;
import com.zifang.util.ioc.metadata.Scope;
import com.zifang.util.ioc.provider.InternalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的 {@link Injector} 实现，对标 Guice 的 {@code InjectorImpl}。
 *
 * <h3>核心职责：</h3>
 * <ul>
 *   <li>安装并执行所有 {@link Module}，收集绑定</li>
 *   <li>解析依赖（类型 / 限定符 / 名称）</li>
 *   <li>实现 singleton / prototype 作用域语义</li>
 *   <li>触发 JIT 绑定（未显式绑定但有具体类的场景）</li>
 *   <li>检测循环依赖</li>
 *   <li>在创建后应用 AOP 代理</li>
 *   <li>生命周期回调：{@link PostConstruct} / {@link PreDestroy}</li>
 * </ul>
 */
public class DefaultInjector implements Injector {

    private static final Logger log = LoggerFactory.getLogger(DefaultInjector.class);

    private final BeanRegistry registry = new DefaultBeanRegistry();
    private final DefaultBinder binder;
    private final AopProxyPostProcessor aopPostProcessor;
    private final ConstructorInjector constructorInjector;
    private final FieldInjector fieldInjector;
    private final LifecycleManager lifecycleManager;

    /**
     * 用于循环依赖检测的线程局部创建栈
     */
    private final ThreadLocal<List<String>> creationStack = ThreadLocal.withInitial(ArrayList::new);

    /**
     * eager singleton 在创建阶段立即实例化
     */
    private final Map<BeanDefinition, Object> eagerSingletons = new ConcurrentHashMap<>();

    /**
     * 子 Injector 链：用于 createChildInjector()
     */
    private final DefaultInjector parent;

    public DefaultInjector(Module... modules) {
        this(null, modules);
    }

    private DefaultInjector(DefaultInjector parent, Module... modules) {
        this.parent = parent;
        this.binder = new DefaultBinder((DefaultBeanRegistry) registry);
        this.aopPostProcessor = new AopProxyPostProcessor(binder.getInterceptorBindings());
        this.constructorInjector = new ConstructorInjector(this);
        this.fieldInjector = new FieldInjector(this);
        this.lifecycleManager = new LifecycleManager();
        // 如果有父容器，继承其拦截规则（绑定按需在 lookup 时回落）
        if (parent != null) {
            binder.getInterceptorBindings().addAll(parent.binder.getInterceptorBindings());
        }
        installModules(modules);
        createEagerSingletons();
    }

    private void installModules(Module... modules) {
        if (modules == null) return;
        for (Module m : modules) {
            if (m == null) continue;
            try {
                binder.install(m);
            } catch (RuntimeException e) {
                throw new BindingException("Module installation failed: " + m.getClass().getName(), e);
            }
        }
    }

    private void createEagerSingletons() {
        for (BeanDefinition bd : registry.getAll()) {
            if (bd.isEager() && bd.isSingleton() && bd.getInstance() == null) {
                try {
                    Object instance = createOrGet(bd);
                    eagerSingletons.put(bd, instance);
                } catch (RuntimeException e) {
                    log.warn("Eager singleton creation failed: {} -> {}", bd.getKey(), e.toString());
                }
            }
        }
    }

    // ============== Injector API ==============

    @Override
    public <T> T getInstance(Class<T> type) {
        return getInstance(type, (Class<? extends Annotation>) null);
    }

    @Override
    public <T> T getInstance(Class<T> type, Class<? extends Annotation> qualifier) {
        return resolve(type, qualifier, null);
    }

    @Override
    public <T> T getInstance(Class<T> type, String name) {
        return resolve(type, null, name);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        return getProvider(type, null);
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type, Class<? extends Annotation> qualifier) {
        return new InternalProvider<>(this, type, qualifier);
    }

    @Override
    public Injector createChildInjector(Module... modules) {
        return new DefaultInjector(this, modules);
    }

    // ============== Resolution ==============

    @SuppressWarnings("unchecked")
    private <T> T resolve(Class<T> type, Class<? extends Annotation> qualifier, String name) {
        if (type == Provider.class) {
            throw new ProvisionException("Provider<T> must be requested with the actual element type");
        }
        BeanDefinition bd = findBinding(type, qualifier, name);
        if (bd == null && qualifier == null && name == null && isConcreteClass(type)) {
            bd = BeanDefinition.jit(type, detectScope(type));
            registry.register(bd);
            log.debug("JIT binding registered: {}", type.getName());
        }
        if (bd == null) {
            throw new NoSuchBeanException("No bean for type " + type.getName()
                    + (qualifier != null ? " @" + qualifier.getSimpleName() : "")
                    + (name != null ? " [\"" + name + "\"]" : ""));
        }
        return (T) createOrGet(bd);
    }

    private BeanDefinition findBinding(Class<?> type, Class<? extends Annotation> qualifier, String name) {
        if (qualifier != null) {
            BeanDefinition bd = registry.get(type, qualifier);
            if (bd != null) {
                return bd;
            }
            return parent == null ? null : parent.findBinding(type, qualifier, name);
        }
        if (name != null && !name.isEmpty()) {
            BeanDefinition bd = registry.get(type, name);
            if (bd != null) return bd;
            return parent == null ? null : parent.findBinding(type, qualifier, name);
        }
        BeanDefinition bd = registry.get(type);
        if (bd != null) return bd;
        List<BeanDefinition> candidates = registry.getAll(type);
        if (!candidates.isEmpty()) {
            if (candidates.size() == 1) return candidates.get(0);
            throw new ProvisionException("Multiple beans for type " + type.getName()
                    + "; use @Named or other qualifier to disambiguate.");
        }
        if (parent != null) {
            return parent.findBinding(type, qualifier, name);
        }
        return null;
    }

    /**
     * 创建或获取 Bean 实例（按作用域语义）。
     */
    private Object createOrGet(BeanDefinition bd) {
        // 若 bd 来自父容器（按引用判断），克隆到本容器，避免污染父实例
        BeanDefinition localBd = registry.get(bd.getKey());
        if (parent != null && localBd != null && localBd != bd) {
            // 同 key 的 BD 已在 child 中（可能是其他模块注入的），使用 child 自己的
            bd = localBd;
        } else if (parent != null && localBd == null) {
            // child 没有该 key，从 parent 克隆过来
            BeanDefinition cloned = cloneBdForChild(bd);
            try {
                registry.register(cloned);
            } catch (com.zifang.util.ioc.exception.BindingException ignore) {
                cloned = registry.get(bd.getKey());
            }
            bd = cloned;
        }
        BeanDefinition resolved = followLinks(bd);

        if (resolved.getBindingType() == BeanDefinition.BindingType.INSTANCE) {
            return resolved.getInstance();
        }
        if (resolved.getBindingType() == BeanDefinition.BindingType.PROVIDER) {
            return resolved.getProvider().get();
        }

        if (resolved.isSingleton()) {
            Object existing = resolved.getInstance();
            if (existing != null) {
                return existing;
            }
            synchronized (resolved) {
                if (resolved.getInstance() != null) {
                    return resolved.getInstance();
                }
                List<String> stack = creationStack.get();
                String keyStr = resolved.getKey().toString();
                if (stack.contains(keyStr)) {
                    List<String> chain = new ArrayList<>(stack.subList(stack.indexOf(keyStr), stack.size()));
                    chain.add(keyStr);
                    throw new CircularDependencyException(chain);
                }
                stack.add(keyStr);
                try {
                    Object instance = instantiateAndInject(resolved);
                    Object wrapped = aopPostProcessor.postProcess(resolved, instance, this);
                    resolved.setInstance(wrapped);
                    resolved.setCreationTime(System.currentTimeMillis());
                    invokePostConstruct(wrapped);
                    return wrapped;
                } finally {
                    stack.remove(stack.size() - 1);
                }
            }
        }

        return instantiateAndInject(resolved);
    }

    /**
     * 克隆父 BeanDefinition 到子容器，使其生命周期独立。
     * <p>
     * 故意不复制 instance：子容器应创建自己的实例，避免与父共享状态。
     */
    private BeanDefinition cloneBdForChild(BeanDefinition parentBd) {
        return new BeanDefinition(
                parentBd.getKey(),
                parentBd.getBeanClass(),
                parentBd.getScope(),
                parentBd.getBindingType(),
                parentBd.isConfiguration(),
                parentBd.getFactoryMethod(),
                parentBd.getConfigClass(),
                parentBd.getProvider(),
                parentBd.isEager());
    }

    /**
     * 跟踪 linked 链直到非 linked 的 BeanDefinition。
     */
    private BeanDefinition followLinks(BeanDefinition bd) {
        BeanDefinition current = bd;
        java.util.Set<BeanDefinition> visited = new java.util.HashSet<>();
        while (current.getBindingType() == BeanDefinition.BindingType.LINKED) {
            if (!visited.add(current)) break;
            Class<?> impl = current.getBeanClass();
            BeanDefinition next = registry.get(impl);
            if (next == null) {
                next = BeanDefinition.jit(impl, current.getScope());
                registry.register(next);
            }
            current = next;
        }
        return current;
    }

    /**
     * 实例化 + 字段注入。
     */
    private Object instantiateAndInject(BeanDefinition bd) {
        try {
            Object instance;
            switch (bd.getBindingType()) {
                case FACTORY_METHOD:
                    instance = invokeFactoryMethod(bd);
                    break;
                case JIT:
                case LINKED:
                case COMPONENT:
                default:
                    instance = constructorInjector.instantiate(bd.getBeanClass());
                    break;
            }
            fieldInjector.inject(instance);
            return instance;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create bean: " + bd.getKey(), e);
        }
    }

    private Object invokeFactoryMethod(BeanDefinition bd) {
        try {
            Class<?> configClass = bd.getConfigClass();
            Object configInstance = constructorInjector.instantiate(configClass);
            fieldInjector.inject(configInstance);
            Method m = bd.getFactoryMethod();
            m.setAccessible(true);
            return m.invoke(configInstance);
        } catch (InvocationTargetException ite) {
            throw new ProvisionException("Factory method threw", ite.getCause());
        } catch (ReflectiveOperationException roe) {
            throw new ProvisionException("Failed to invoke factory method", roe);
        }
    }

    private void invokePostConstruct(Object instance) {
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PostConstruct.class) && m.getParameterCount() == 0) {
                try {
                    m.setAccessible(true);
                    m.invoke(instance);
                } catch (Exception e) {
                    throw new ProvisionException("@PostConstruct failed: " + m, e);
                }
            }
        }
    }

    private void invokePreDestroy(Object instance) {
        for (Method m : instance.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(PreDestroy.class) && m.getParameterCount() == 0) {
                try {
                    m.setAccessible(true);
                    m.invoke(instance);
                } catch (Exception e) {
                    log.warn("@PreDestroy failed: {} -> {}", m, e.toString());
                }
            }
        }
    }

    /**
     * 关闭容器：逆序调用所有 singleton 的 {@link PreDestroy}。
     */
    public void close() {
        List<Object> singletons = new ArrayList<>();
        for (BeanDefinition bd : registry.getAll()) {
            if (bd.isSingleton() && bd.getInstance() != null && !bd.isConfiguration()) {
                singletons.add(bd.getInstance());
            }
        }
        for (int i = singletons.size() - 1; i >= 0; i--) {
            invokePreDestroy(singletons.get(i));
        }
    }

    private Scope detectScope(Class<?> type) {
        if (type.isAnnotationPresent(Singleton.class)) {
            return Scope.SINGLETON;
        }
        return Scope.DEFAULT;
    }

    private boolean isConcreteClass(Class<?> type) {
        return !type.isInterface() && !java.lang.reflect.Modifier.isAbstract(type.getModifiers());
    }

    public BeanRegistry getRegistry() {
        return registry;
    }

    public Collection<BeanDefinition> getAllBeanDefinitions() {
        return registry.getAll();
    }
}

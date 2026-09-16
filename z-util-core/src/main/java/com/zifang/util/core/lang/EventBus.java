package com.zifang.util.core.lang;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 进程内事件总线（对标 Guava EventBus / Spring ApplicationEvent）。
 * <p>
 * 用法：
 * <pre>{@code
 *   EventBus bus = new EventBus();
 *   bus.register(new MyListener());
 *   bus.post(new OrderCreatedEvent(orderId));
 * }</pre>
 * <p>
 * 监听者方法需加 {@link Subscribe} 注解。
 *
 * @author zifang
 */
public class EventBus {

    private final Map<Class<?>, CopyOnWriteArrayList<Subscriber>> subscribers = new ConcurrentHashMap<>();

    private static Method invokeConsumer(Class<?> eventType) {
        try {
            return Consumer.class.getMethod("accept", Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 注册一个监听者（其 public 方法中带 {@link Subscribe} 的会被订阅）。
     */
    public void register(Object listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        for (Method m : listener.getClass().getMethods()) {
            if (m.isAnnotationPresent(Subscribe.class) && m.getParameterCount() == 1) {
                Class<?> eventType = m.getParameterTypes()[0];
                subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                        .add(new Subscriber(listener, m));
            }
        }
    }

    /**
     * 注销监听者。
     */
    public void unregister(Object listener) {
        if (listener == null) return;
        for (CopyOnWriteArrayList<Subscriber> list : subscribers.values()) {
            list.removeIf(s -> s.target == listener);
        }
    }

    /**
     * 发布事件。
     */
    public void post(Object event) {
        if (event == null) return;
        CopyOnWriteArrayList<Subscriber> list = subscribers.get(event.getClass());
        if (list == null) return;
        for (Subscriber s : list) {
            try {
                s.method.invoke(s.target, event);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(
                        "EventBus invocation failed for " + s.method, e);
            }
        }
    }

    /**
     * 简化版的 lambda 订阅。
     * <p>
     * 用法：{@code bus.subscribe(MyEvent.class, e -> doSomething(e));}
     */
    public <E> void subscribe(Class<E> eventType, Consumer<E> handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null");
        }
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new Subscriber(handler, invokeConsumer(eventType)));
    }

    /**
     * 标注订阅方法。
     */
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
    public @interface Subscribe {
    }

    private static final class Subscriber {
        final Object target;
        final Method method;

        Subscriber(Object t, Method m) {
            this.target = t;
            this.method = m;
        }
    }
}
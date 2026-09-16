package com.zifang.util.core.pattern.pool;

import com.zifang.util.core.pattern.pool.monitor.PoolListener;

import java.util.function.Supplier;

/**
 * 池化工具类
 */
public class PoolUtils {

    private PoolUtils() {
    }

    /**
     * 创建一个使用简单对象的池
     */
    public static <T> ObjectPool<T> createSimplePool(Supplier<T> creator, java.util.function.Consumer<T> destroyer) {
        return createSimplePool(creator, destroyer, new PoolConfig());
    }

    /**
     * createSimplePool方法。
     * * @param creator SupplierT类型参数
     *
     * @param destroyer java.util.function.ConsumerT类型参数
     * @param config    PoolConfig类型参数
     * @return static <T> ObjectPool<T>类型返回值
     */
    public static <T> ObjectPool<T> createSimplePool(Supplier<T> creator, java.util.function.Consumer<T> destroyer, PoolConfig config) {
        PooledObjectFactory<T> factory = new PooledObjectFactory<T>() {
            @Override
            /**
             * makeObject方法。
             * @return PooledObject<T>类型返回值
             */
            public PooledObject<T> makeObject() {
                return new PooledObject<>(creator.get());
            }

            @Override
            /**
             * destroyObject方法。
             *      * @param p PooledObjectT类型参数
             */
            public void destroyObject(PooledObject<T> p) {
                if (p != null && p.getObject() != null) {
                    destroyer.accept(p.getObject());
                }
            }

            @Override
            /**
             * validateObject方法。
             *      * @param p PooledObjectT类型参数
             * @return boolean类型返回值
             */
            public boolean validateObject(PooledObject<T> p) {
                return p.getObject() != null;
            }

            @Override
            /**
             * activateObject方法。
             *      * @param p PooledObjectT类型参数
             */
            public void activateObject(PooledObject<T> p) {
            }

            @Override
            /**
             * passivateObject方法。
             *      * @param p PooledObjectT类型参数
             */
            public void passivateObject(PooledObject<T> p) {
            }
        };

        return new StackObjectPool<>(factory, config);
    }

    /**
     * 创建带代理的对象池
     */
    public static <T> ObjectPool<T> createProxiedPool(Supplier<T> creator, java.util.function.Consumer<T> destroyer) {
        return createSimplePool(creator, destroyer);
    }

    /**
     * 执行带池化的操作
     */
    public static <T, R> R executeWithPool(ObjectPool<T> pool, java.util.function.Function<T, R> action) throws Exception {
        T obj = pool.borrowObject();
        try {
            return action.apply(obj);
        } finally {
            pool.returnObject(obj);
        }
    }

    /**
     * 创建默认配置的栈对象池
     */
    public static <T> StackObjectPool<T> createStackPool(PooledObjectFactory<T> factory) {
        return new StackObjectPool<>(factory);
    }

    /**
     * 创建带配置的栈对象池
     */
    public static <T> StackObjectPool<T> createStackPool(PooledObjectFactory<T> factory, PoolConfig config) {
        return new StackObjectPool<>(factory, config);
    }

    /**
     * 创建带监控的简单对象池
     */
    public static <T> MonitoredObjectPool<T> createMonitoredPool(
            Supplier<T> creator,
            java.util.function.Consumer<T> destroyer,
            PoolListener<T> listener) {
        PooledObjectFactory<T> factory = new PooledObjectFactory<T>() {
            @Override
            /**
             * makeObject方法。
             * @return PooledObject<T>类型返回值
             */
            public PooledObject<T> makeObject() {
                return new PooledObject<>(creator.get());
            }

            @Override
            /**
             * destroyObject方法。
             *      * @param p PooledObjectT类型参数
             */
            public void destroyObject(PooledObject<T> p) {
                if (p != null && p.getObject() != null) {
                    destroyer.accept(p.getObject());
                }
            }

            @Override
            /**
             * validateObject方法。
             *      * @param p PooledObjectT类型参数
             * @return boolean类型返回值
             */
            public boolean validateObject(PooledObject<T> p) {
                return p.getObject() != null;
            }

            @Override
            /**
             * activateObject方法。
             *      * @param p PooledObjectT类型参数
             */
            public void activateObject(PooledObject<T> p) {
            }

            @Override
            /**
             * passivateObject方法。
             *      * @param p PooledObjectT类型参数
             */
            public void passivateObject(PooledObject<T> p) {
            }
        };
        return new MonitoredObjectPool<>(factory, new PoolConfig(), listener);
    }

    /**
     * 创建带监控的简单对象池
     */
    public static <T> MonitoredObjectPool<T> createMonitoredPool(
            Supplier<T> creator,
            java.util.function.Consumer<T> destroyer) {
        return createMonitoredPool(creator, destroyer, null);
    }
}

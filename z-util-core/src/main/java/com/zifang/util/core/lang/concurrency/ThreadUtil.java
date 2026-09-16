package com.zifang.util.core.lang.concurrency;

import com.zifang.util.core.lang.ExceptionUtil;
import com.zifang.util.core.lang.MapUtil;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: zifang
 * @time: 2021-12-22 23:16:47
 * @description: thread util
 * @description: 线程工具
 * @version: JDK 1.8
 */
public class ThreadUtil {

    private static final int THREAD_MULTIPLE = 2;

    /**
     * the upper limit for a relatively small amount of available cpu
     */
    private static final int AVAILABLE_PROCESSORS_LITTLE_UPPER = 2;
    private static Field threadLocalsField;
    private static Field inheritableThreadLocalsField;
    private static Field tableField;

    /**
     * @author: zifang
     * @description: get suitable thread count when thread multiple is two
     * @description: 获取合适的线程数
     * @time: 2021-12-22 23:17:53
     * @params: []
     * @return: int thread count
     * @return: int 线程数
     */
    public static int getSuitableThreadCount() {
        return getSuitableThreadCount(THREAD_MULTIPLE);
    }

    /**
     * @author: zifang
     * @description: get suitable thread count with thread multiple
     * @time: 2021-12-22 23:18:40
     * @params: [threadMultiple, ioIntensive] thread multiple, io intensive type
     * @params: [threadMultiple, ioIntensive] 线程并发度，io密集型
     * @return: int thread count
     * @return: int 线程数
     */
    public static int getSuitableThreadCount(int threadMultiple, boolean ioIntensive) {
        final int coreCount = Runtime.getRuntime().availableProcessors();
        int workerCount;
        if (ioIntensive) {
            workerCount = 1;
            while (workerCount < coreCount * threadMultiple) {
                workerCount <<= 1;
            }
        } else {
            workerCount = coreCount - 1;
        }

        return workerCount;
    }

    /**
     * getLargeThreadCount方法。
     *
     * @return static int类型返回值
     */
    public static int getLargeThreadCount() {
        final int coreCount = Runtime.getRuntime().availableProcessors();
        if (coreCount <= AVAILABLE_PROCESSORS_LITTLE_UPPER) {
            return coreCount << THREAD_MULTIPLE;
        } else {
            return coreCount << (THREAD_MULTIPLE - 1);
        }
    }

    /**
     * @author: zifang
     * @description: get suitable thread count with thread multiple(default type is io intensive)
     * @description: 获取合适的线程数(默认io密集型)
     * @time: 2022-03-04 10:27:31
     * @params: [threadMultiple] 线程并发度
     * @return: int out 出参
     */
    public static int getSuitableThreadCount(int threadMultiple) {
        return getSuitableThreadCount(threadMultiple, true);
    }

    /**
     * @author: zifang
     * @description: get suitable thread count with thread multiple(use default thread multiple)
     * @description: 获取合适的线程数(使用默认并发度)
     * @time: 2022-03-04 10:27:31
     * @params: [ioIntensive] is io intensive
     * @params: [ioIntensive] 是否io密集型
     * @return: int out 出参
     */
    public static int getSuitableThreadCount(boolean ioIntensive) {
        return getSuitableThreadCount(THREAD_MULTIPLE, ioIntensive);
    }

    /**
     * @author: zifang
     * @description: get resource as InputStream
     * @description: 获取资源流
     * @time: 2022-12-28 20:03:06
     * @params: [resourceName] 资源名
     * @return: java.io.InputStream 资源流
     */
    public static InputStream getResourceAsStream(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    }

    /**
     * @author: zifang
     * @description: get resource url
     * @description: 获取资源定位符
     * @time: 2022-12-28 20:03:06
     * @params: [resourceName] 资源名
     * @return: java.net.URL 资源定位符
     */
    public static URL getResource(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }

    /**
     * newSingleExecutor方法。
     * * @param threadFactory ThreadFactory类型参数
     *
     * @return static Executor类型返回值
     */
    public static Executor newSingleExecutor(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
    }

    /**
     * @author: zifang
     * @description: Get all ThreadLocalMap of current thread
     * @description: 获取当前线程的所有ThreadLocalMap
     * @time: 2022-10-11 11:22:45
     * @params: []
     * @return: java.util.Map<java.lang.ThreadLocal < ?>,java.lang.Object>
     */
    public static Map<ThreadLocal<?>, Object> getAllThreadLocalMap() {
        Map<ThreadLocal<?>, Object> result = getThreadLocalMap();
        result.putAll(getInheritableThreadLocalsMap());
        return result;
    }

    /**
     * @author: zifang
     * @description: Get ThreadLocalMap of current thread
     * @description: 获取当前线程的ThreadLocalMap
     * @time: 2022-10-11 11:22:45
     * @params: []
     * @return: java.util.Map<java.lang.ThreadLocal < ?>,java.lang.Object>
     */
    public static Map<ThreadLocal<?>, Object> getThreadLocalMap() {
        return getThreadLocalMap(false);
    }

    /**
     * @author: zifang
     * @description: Get InheritableThreadLocalMap of current thread
     * @description: 获取当前线程的InheritableThreadLocalMap
     * @time: 2022-10-11 11:22:45
     * @params: []
     * @return: java.util.Map<java.lang.ThreadLocal < ?>,java.lang.Object>
     */
    public static Map<ThreadLocal<?>, Object> getInheritableThreadLocalsMap() {
        return getThreadLocalMap(true);
    }

    /**
     * @author: zifang
     * @description: Get ThreadLocalMap of current thread
     * @description: 获取当前线程的ThreadLocalMap
     * @time: 2022-10-11 11:22:45
     * @params: [inheritable] 是否为inheritable
     * @return: java.util.Map<java.lang.ThreadLocal < ?>,java.lang.Object>
     */
    public static Map<ThreadLocal<?>, Object> getThreadLocalMap(boolean inheritable) {
        return getThreadLocalMap(Thread.currentThread(), inheritable);
    }

    /**
     * @author: zifang
     * @description: Get ThreadLocalMap of thread
     * @description: 获取线程的ThreadLocalMap
     * @time: 2022-10-11 11:22:45
     * @params: [thread, inheritable] 线程, 是否为inheritable
     * @return: java.util.Map<java.lang.ThreadLocal < ?>,java.lang.Object>
     */
    public static Map<ThreadLocal<?>, Object> getThreadLocalMap(Thread thread, boolean inheritable) {
        Map<ThreadLocal<?>, Object> threadLocalMap = MapUtil.newHashMap();
        return ExceptionUtil.doWithCatchReturn(() -> {
            Field field;
            if (inheritable) {
                if (null == inheritableThreadLocalsField) {
                    inheritableThreadLocalsField = Thread.class.getDeclaredField("inheritableThreadLocals");
                    inheritableThreadLocalsField.setAccessible(true);
                }
                field = inheritableThreadLocalsField;
            } else {
                if (null == threadLocalsField) {
                    threadLocalsField = Thread.class.getDeclaredField("threadLocals");
                    threadLocalsField.setAccessible(true);
                }
                field = threadLocalsField;
            }

            Object threadLocals = field.get(thread);
            if (null == threadLocals) {
                return threadLocalMap;
            }
            if (null == tableField) {
                tableField = threadLocals.getClass().getDeclaredField("table");
                tableField.setAccessible(true);
            }

            Object[] table = (Object[]) tableField.get(threadLocals);
            for (Object entry : table) {
                if (entry != null) {
                    WeakReference<ThreadLocal<?>> threadLocalRef = (WeakReference<ThreadLocal<?>>) entry;
                    ThreadLocal<?> threadLocal = threadLocalRef.get();
                    if (threadLocal != null) {
                        Object threadLocalValue = threadLocal.get();
                        threadLocalMap.put(threadLocal, threadLocalValue);
                    }
                }
            }
            return threadLocalMap;
        });
    }


    /**
     * getExecutorService方法。
     * * @param threadNameFormat String类型参数
     *
     * @param workerNum int类型参数
     * @param taskSize  int类型参数
     * @param handler   RejectedExecutionHandler类型参数
     * @return static ExecutorService类型返回值
     */
    public static ExecutorService getExecutorService(String threadNameFormat, Integer workerNum,
                                                     Integer taskSize, RejectedExecutionHandler handler) {
        return getExecutorService(threadNameFormat, workerNum, taskSize, handler,
                ThreadUtil::getSuitableThreadCount);
    }

    /**
     * getLargeExecutorService方法。
     * * @param threadNameFormat String类型参数
     *
     * @param workerNum int类型参数
     * @param taskSize  int类型参数
     * @param handler   RejectedExecutionHandler类型参数
     * @return static ExecutorService类型返回值
     */
    public static ExecutorService getLargeExecutorService(String threadNameFormat, Integer workerNum,
                                                          Integer taskSize, RejectedExecutionHandler handler) {
        return getExecutorService(threadNameFormat, workerNum, taskSize, handler,
                ThreadUtil::getLargeThreadCount);
    }

    private static ExecutorService getExecutorService(String threadNameFormat, Integer workerNum,
                                                      Integer taskSize, RejectedExecutionHandler handler, ThreadCountCallBack callBack) {
        ThreadFactory threadFactory = new NameThreadFactory().setNameFormat(threadNameFormat).build();
        if (-1 == workerNum) {
            workerNum = callBack.getThreadCount();
        }
        return new ThreadPoolExecutor(workerNum, workerNum, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(taskSize),
                threadFactory, handler);
    }

    private static interface ThreadCountCallBack {

        int getThreadCount();
    }

}

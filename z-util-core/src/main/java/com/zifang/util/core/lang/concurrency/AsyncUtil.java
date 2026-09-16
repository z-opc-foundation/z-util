package com.zifang.util.core.lang.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 异步并发执行工具。
 * <p>
 * 将多个任务并发提交到共享线程池并按提交顺序聚合结果，
 * 适用于一次性并行执行多个互不依赖的查询或计算后统一消费的场景。
 * <p>
 * 共享线程池使用守护线程并允许核心线程超时回收，不阻止 JVM 退出。
 *
 * @author zifang
 */
public class AsyncUtil {

    private static final Logger log = LoggerFactory.getLogger(AsyncUtil.class);

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(
            ThreadUtil.getSuitableThreadCount(),
            Math.max(ThreadUtil.getSuitableThreadCount() * 8, 64),
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new NameThreadFactory().setNameFormat("async-util-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    static {
        ((ThreadPoolExecutor) EXECUTOR).allowCoreThreadTimeOut(true);
    }

    private AsyncUtil() {
    }

    /**
     * 并发执行多个任务并按提交顺序聚合结果。
     * <p>
     * 任一任务抛出异常时，异常会在聚合阶段原样抛出（以 CompletionException 包装），
     * 需要调用方自行处理。
     *
     * @param suppliers 任务列表
     * @param <T>       任务返回值类型
     * @return 与任务顺序一致的结果列表；入参为空时返回空列表
     */
    @SafeVarargs
    public static <T> List<T> executeThrown(Supplier<T>... suppliers) {
        List<T> result = new ArrayList<>();
        if (suppliers == null || suppliers.length == 0) {
            return result;
        }
        for (CompletableFuture<T> future : futures(suppliers)) {
            result.add(future.join());
        }
        return result;
    }

    /**
     * 并发执行多个任务并按提交顺序聚合结果。
     * <p>
     * 与 {@link #executeThrown(Supplier[])} 不同，任一任务执行异常或被中断时，
     * 该位置的结果记为 null，其余任务结果不受影响。
     *
     * @param suppliers 任务列表
     * @param <T>       任务返回值类型
     * @return 与任务顺序一致的结果列表；入参为空时返回空列表
     */
    @SafeVarargs
    public static <T> List<T> execute(Supplier<T>... suppliers) {
        return execute(-1, null, suppliers);
    }

    /**
     * 并发执行多个任务并按提交顺序聚合结果，支持整体超时。
     * <p>
     * 每个任务的结果获取最多等待 timeout 个 unit 时间，超时或异常时该位置记为 null。
     *
     * @param timeout   超时时长，小于等于 0 表示不设超时
     * @param unit      超时时间单位
     * @param suppliers 任务列表
     * @param <T>       任务返回值类型
     * @return 与任务顺序一致的结果列表；入参为空时返回空列表
     */
    @SafeVarargs
    public static <T> List<T> execute(long timeout, TimeUnit unit, Supplier<T>... suppliers) {
        List<T> result = new ArrayList<>();
        if (suppliers == null || suppliers.length == 0) {
            return result;
        }
        for (CompletableFuture<T> future : futures(suppliers)) {
            T value = null;
            try {
                value = timeout > 0 ? future.get(timeout, unit) : future.get();
            } catch (Exception e) {
                log.warn("async task failed", e);
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 异步执行多个任务，不等待完成也不返回结果。
     * <p>
     * 任务抛出的异常会被记录日志后吞掉，不影响其余任务的提交。
     *
     * @param tasks 任务列表
     */
    public static void executeRunnable(Runnable... tasks) {
        if (tasks == null || tasks.length == 0) {
            return;
        }
        for (Runnable task : tasks) {
            CompletableFuture.runAsync(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.warn("async runnable failed", e);
                }
            }, EXECUTOR);
        }
    }

    @SafeVarargs
    private static <T> List<CompletableFuture<T>> futures(Supplier<T>... suppliers) {
        List<CompletableFuture<T>> futures = new ArrayList<>(suppliers.length);
        for (Supplier<T> supplier : suppliers) {
            futures.add(CompletableFuture.supplyAsync(supplier, EXECUTOR));
        }
        return futures;
    }
}

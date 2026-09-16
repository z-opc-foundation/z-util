package com.zifang.util.core.lang.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 并行任务组：在调用方指定的线程池中登记多段并行任务，最后统一等待全部任务结束。
 * <p>
 * 支持三类任务形态：
 * <ul>
 *   <li>无依赖任务 {@link #submit(Supplier)}，提交即并行执行；</li>
 *   <li>依赖前序结果的任务 {@link #submitAfter(CompletableFuture, Function)}，
 *       前序 Future 完成后同池执行映射；</li>
 *   <li>外部创建的 Future 通过 {@link #register(CompletableFuture)} 纳入统一等待。</li>
 * </ul>
 * 典型用法：在单线程内顺序 submit / submitAfter 登记，再 {@link #awaitAll()}，
 * 各任务通过返回的 {@link CompletableFuture#join()} 取结果。
 * <p>
 * 本类非线程安全，不要在多线程中同时调用 submit。
 *
 * @author zifang
 */
public final class ParallelTaskGroup {

    private static final Logger log = LoggerFactory.getLogger(ParallelTaskGroup.class);

    private final Executor executor;

    private final List<CompletableFuture<?>> tracked = new ArrayList<>();

    private ParallelTaskGroup(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    /**
     * 基于指定线程池创建任务组
     *
     * @param executor 任务执行线程池，不允许为null
     * @return 任务组实例
     */
    public static ParallelTaskGroup on(Executor executor) {
        return new ParallelTaskGroup(executor);
    }

    /**
     * 登记一个异步任务，返回的Future已纳入{@link #awaitAll()}统一等待
     *
     * @param supplier 任务逻辑
     * @param <T>      任务返回值类型
     * @return 任务Future
     */
    public <T> CompletableFuture<T> submit(Supplier<T> supplier) {
        return submit(null, supplier);
    }

    /**
     * 登记异步任务，失败时按任务名记录日志便于定位
     *
     * @param taskName 任务名，用于失败日志定位，可为null
     * @param supplier 任务逻辑
     * @param <T>      任务返回值类型
     * @return 任务Future
     */
    public <T> CompletableFuture<T> submit(String taskName, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> runNamed(taskName, supplier), executor);
        tracked.add(future);
        return future;
    }

    /**
     * 在前序Future完成后，在同一线程池执行映射，返回的Future已纳入{@link #awaitAll()}统一等待
     *
     * @param prerequisite 前序任务Future
     * @param mapper       映射函数
     * @param <T>          前序结果类型
     * @param <R>          映射结果类型
     * @return 映射任务Future
     */
    public <T, R> CompletableFuture<R> submitAfter(CompletableFuture<T> prerequisite, Function<T, R> mapper) {
        return submitAfter(null, prerequisite, mapper);
    }

    /**
     * 带任务名的依赖任务，映射阶段异常会按名记录日志
     *
     * @param taskName     任务名，用于失败日志定位，可为null
     * @param prerequisite 前序任务Future
     * @param mapper       映射函数
     * @param <T>          前序结果类型
     * @param <R>          映射结果类型
     * @return 映射任务Future
     */
    public <T, R> CompletableFuture<R> submitAfter(String taskName, CompletableFuture<T> prerequisite, Function<T, R> mapper) {
        Objects.requireNonNull(prerequisite, "prerequisite");
        Objects.requireNonNull(mapper, "mapper");
        CompletableFuture<R> future = prerequisite.thenApplyAsync(t -> runNamed(taskName, () -> mapper.apply(t)), executor);
        tracked.add(future);
        return future;
    }

    /**
     * 将外部创建的Future纳入本次统一等待
     *
     * @param future 外部Future
     * @return 当前任务组
     */
    public ParallelTaskGroup register(CompletableFuture<?> future) {
        tracked.add(Objects.requireNonNull(future, "future"));
        return this;
    }

    /**
     * 阻塞直至本组登记的全部任务结束；任一失败抛出CompletionException（与{@link CompletableFuture#join()}语义一致）
     */
    public void awaitAll() {
        if (tracked.isEmpty()) {
            return;
        }
        CompletableFuture.allOf(tracked.toArray(new CompletableFuture<?>[0])).join();
    }

    /**
     * 带超时的等待；超时抛出TimeoutException，任务异常抛出ExecutionException
     *
     * @param timeout 超时时长
     * @param unit    时长单位
     * @throws java.util.concurrent.TimeoutException     等待超时
     * @throws java.util.concurrent.ExecutionException   任务执行失败
     * @throws java.util.concurrent.InterruptedException 等待线程被中断
     */
    public void awaitAll(long timeout, TimeUnit unit) throws Exception {
        if (tracked.isEmpty()) {
            return;
        }
        CompletableFuture.allOf(tracked.toArray(new CompletableFuture<?>[0])).get(timeout, unit);
    }

    private static <T> T runNamed(String taskName, Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException | Error e) {
            if (taskName != null && !taskName.isEmpty()) {
                log.error("parallel task failed: {}", taskName, e);
            }
            throw e;
        }
    }
}

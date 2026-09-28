package com.zifang.util.core.lang.concurrency;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * MDC 日志上下文的跨线程传播工具。
 * <p>
 * {@link com.zifang.util.core.trace.TraceContextHolder} 已经把 traceId/spanId 写进 SLF4J MDC，
 * 但 MDC 是 ThreadLocal 的：任务一换线程，日志就丢链路。本类补执行器层这一环——
 * 提交时快照调用线程的 MDC，执行时绑定，执行后<b>恢复</b>线程原有值。
 *
 * <h3>为什么恢复而不是清空</h3>
 * 拒绝策略为 {@code CallerRunsPolicy} 时任务会在<b>调用线程</b>上就地执行，
 * 若收尾 {@code MDC.clear()} 会把调用线程自己的日志上下文一起清掉；
 * 池内线程原本无上下文，恢复即等价于清空。
 *
 * <h3>用法</h3>
 * <pre>{@code
 *   executor.execute(MdcPropagation.wrap(task));          // 任意执行器
 *   new ThreadPoolExecutor(4, 4, 0L, MILLISECONDS,
 *           new LinkedBlockingQueue<>()) { ... }          // 或改用 MdcThreadPoolExecutor
 *   CompletableFuture.runAsync(job, MdcPropagation.view(pool));
 * }</pre>
 */
public final class MdcPropagation {

    private MdcPropagation() {
    }

    /**
     * 包装任务：执行期间持有提交线程的 MDC 快照，结束后恢复执行线程原值。
     *
     * @param task 待执行任务，为 null 时返回 null
     * @return 带上下文传播的任务包装
     */
    public static Runnable wrap(Runnable task) {
        if (task == null) {
            return null;
        }
        final Map<String, String> captured = MDC.getCopyOfContextMap();
        return new Runnable() {
            @Override
            public void run() {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                restore(captured);
                try {
                    task.run();
                } finally {
                    restore(previous);
                }
            }

            @Override
            public String toString() {
                return "MdcWrapped[" + task + "]";
            }
        };
    }

    /**
     * 包装带返回值的任务，语义同 {@link #wrap(Runnable)}。
     *
     * @param task 待执行任务，为 null 时返回 null
     * @param <V>  返回值类型
     * @return 带上下文传播的任务包装
     */
    public static <V> Callable<V> wrap(final Callable<V> task) {
        if (task == null) {
            return null;
        }
        final Map<String, String> captured = MDC.getCopyOfContextMap();
        return new Callable<V>() {
            @Override
            public V call() throws Exception {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                restore(captured);
                try {
                    return task.call();
                } finally {
                    restore(previous);
                }
            }
        };
    }

    /**
     * 装饰任意执行器，使其提交的任务自动传播 MDC（可直接传给 CompletableFuture 的 executor 参数）。
     *
     * @param executor 被装饰的执行器，为 null 时抛 IllegalArgumentException
     * @return 装饰后的执行器
     */
    public static java.util.concurrent.Executor view(final java.util.concurrent.Executor executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor must not be null");
        }
        return new java.util.concurrent.Executor() {
            @Override
            public void execute(Runnable command) {
                executor.execute(wrap(command));
            }
        };
    }

    /**
     * 当前线程 MDC 的快照（可用于手工在别处绑定）。
     *
     * @return 快照，可能为 null
     */
    public static Map<String, String> snapshot() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 把快照绑定到当前线程；null 或空快照等价于清空。
     */
    public static void restore(Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(context);
        }
    }
}

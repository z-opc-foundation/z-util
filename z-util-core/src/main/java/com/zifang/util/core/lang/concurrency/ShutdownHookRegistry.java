package com.zifang.util.core.lang.concurrency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 关闭钩子注册表。
 * <p>
 * 全站有二十来处手写 {@code Runtime.getRuntime().addShutdownHook(new Thread(...))}，
 * 由此带来三类问题：同一个 hook 被注册两次（如服务既在 {@code start()} 里注册又在
 * 引导类的 {@code main()} 里注册）、hook 之间没有顺序且无法摘除、
 * 某个 hook 卡死（如 {@code syncUninterruptibly()}）就把整个关停流程拖住。
 * <p>
 * 本注册表只在 JVM 里挂<b>一个</b>真实 hook，内部维护一张有序表：
 *
 * <ul>
 *   <li>按 key 幂等：同 key 重复注册直接返回 false，消灭重复注册这一类 bug</li>
 *   <li>逆序执行（后注册的先跑），符合"先起的后关"的资源释放直觉</li>
 *   <li>每个 hook 独立线程 + 超时上限，单个卡死不影响其余</li>
 *   <li>单个 hook 抛异常只记日志，不中断后续 hook</li>
 *   <li>可 {@code remove(key)}、可 {@code runAll()}——测试里反复启动服务时不必真关停 JVM</li>
 * </ul>
 *
 * <h3>超时之后</h3>
 * JVM 关停阶段无法强杀线程。超时只是"不再等待"：该 hook 线程被放弃并记 WARN，
 * 注册表继续跑后面的 hook。
 */
public final class ShutdownHookRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ShutdownHookRegistry.class);

    /** 单个 hook 的默认等待上限。 */
    public static final long DEFAULT_TIMEOUT_MILLIS = 30_000L;

    private static final Map<String, Hook> HOOKS = new LinkedHashMap<String, Hook>();
    private static long unnamedSeq = 0L;
    private static boolean jvmHookInstalled = false;

    private ShutdownHookRegistry() {
    }

    /**
     * 以自动生成的 key 注册关闭钩子。
     *
     * @param hook 关停动作
     * @return true 表示新增成功
     */
    public static synchronized boolean register(Runnable hook) {
        return register("hook-" + (++unnamedSeq), hook, DEFAULT_TIMEOUT_MILLIS);
    }

    /**
     * 按 key 注册关闭钩子，超时用默认上限。
     *
     * @param key  唯一标识，重复注册会被忽略
     * @param hook 关停动作
     * @return true 表示新增成功，false 表示 key 已存在或 hook 为 null
     */
    public static synchronized boolean register(String key, Runnable hook) {
        return register(key, hook, DEFAULT_TIMEOUT_MILLIS);
    }

    /**
     * 按 key 注册关闭钩子并指定等待上限。
     *
     * @param key            唯一标识，重复注册会被忽略
     * @param hook           关停动作
     * @param timeoutMillis  最长等待毫秒数，小于等于 0 表示不等待限时
     * @return true 表示新增成功，false 表示 key 已存在或 hook 为 null
     */
    public static synchronized boolean register(String key, Runnable hook, long timeoutMillis) {
        if (hook == null) {
            return false;
        }
        String realKey = key == null || key.isEmpty() ? "hook-" + (++unnamedSeq) : key;
        if (HOOKS.containsKey(realKey)) {
            LOG.debug("shutdown hook already registered, ignored: {}", realKey);
            return false;
        }
        HOOKS.put(realKey, new Hook(realKey, hook, timeoutMillis));
        installJvmHook();
        return true;
    }

    /**
     * 摘除指定 key 的钩子（服务在测试中重启时使用）。
     *
     * @param key 注册时使用的 key
     * @return true 表示确实移除了一个钩子
     */
    public static synchronized boolean remove(String key) {
        return HOOKS.remove(key) != null;
    }

    /**
     * 当前登记的钩子数量。
     */
    public static synchronized int size() {
        return HOOKS.size();
    }

    /**
     * 清空登记表（只清内部表，已挂到 JVM 的钩子无法摘除，但空表下它什么都不做）。
     */
    public static synchronized void clear() {
        HOOKS.clear();
    }

    /**
     * 按逆序执行全部钩子但保留登记，等价于"模拟一次关停"。
     * <p>
     * 供测试和"显式 stop + 关停钩子"两条路径并存的场景调用；重复调用会重复执行，
     * 需要只跑一次时先 {@link #clear()}。
     */
    public static synchronized void runAll() {
        List<Hook> snapshot;
        synchronized (HOOKS) {
            snapshot = new ArrayList<Hook>(HOOKS.values());
        }
        for (int i = snapshot.size() - 1; i >= 0; i--) {
            runOne(snapshot.get(i));
        }
    }

    private static synchronized int sizeOrNull() {
        return HOOKS.size();
    }

    private static void installJvmHook() {
        if (jvmHookInstalled) {
            return;
        }
        jvmHookInstalled = true;
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    runAll();
                }
            }, "z-util-shutdown-hook"));
        } catch (IllegalStateException e) {
            LOG.warn("JVM 已进入关停流程，关闭钩子无法再注册: {}", e.getMessage());
        }
    }

    private static void runOne(final Hook hook) {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    hook.action.run();
                } catch (Throwable t) {
                    if (t instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    LOG.warn("shutdown hook [{}] failed: {}", hook.key, t.toString(), t);
                }
            }
        }, "shutdown-hook-" + hook.key);
        worker.start();
        try {
            if (hook.timeoutMillis > 0) {
                worker.join(hook.timeoutMillis);
            } else {
                worker.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (worker.isAlive()) {
            LOG.warn("shutdown hook [{}] still running after {}ms, abandoned", hook.key, hook.timeoutMillis);
        }
    }

    private static final class Hook {

        private final String key;
        private final Runnable action;
        private final long timeoutMillis;

        private Hook(String key, Runnable action, long timeoutMillis) {
            this.key = key;
            this.action = action;
            this.timeoutMillis = timeoutMillis;
        }
    }
}

package com.zifang.util.core.resilience;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 熔断器（自研，对标 Resilience4j CircuitBreaker）。
 * <p>
 * 三态机：
 * <pre>
 *           failure threshold reached
 *  CLOSED ────────────────────────────► OPEN
 *     ▲                                   │
 *     │  success threshold reached        │ sleep window
 *     └──────────── HALF_OPEN ◄───────────┘
 *                       │
 *                       │ failure
 *                       ▼
 *                     OPEN
 * </pre>
 * <p>
 * 调用流程：
 * <ol>
 *   <li>CLOSED：直接调用；失败计数 +1，达到阈值 → OPEN</li>
 *   <li>OPEN：所有调用立即抛 {@link CircuitOpenException}；等 {@code openDurationMs} 后 → HALF_OPEN</li>
 *   <li>HALF_OPEN：放行一个试探调用；成功计数 +1，达到阈值 → CLOSED；失败 → OPEN</li>
 * </ol>
 */
public class CircuitBreaker {

    private final int failureThreshold;
    private final int successThreshold;
    private final long openDurationNanos;
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicLong failureCount = new AtomicLong();
    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong openedAtNanos = new AtomicLong();

    public CircuitBreaker(int failureThreshold, int successThreshold, long openDurationMs) {
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.openDurationNanos = openDurationMs * 1_000_000L;
    }

    public <T> T call(java.util.concurrent.Callable<T> task) {
        if (!allowRequest()) throw new CircuitOpenException("circuit open: " + name());
        try {
            T result = task.call();
            onSuccess();
            return result;
        } catch (RuntimeException e) {
            onFailure();
            throw e;
        } catch (Exception e) {
            onFailure();
            throw new RuntimeException(e);
        }
    }

    private boolean allowRequest() {
        State s = state.get();
        if (s == State.CLOSED || s == State.HALF_OPEN) return true;
        // OPEN：检查是否到了 half-open 时间
        if (System.nanoTime() - openedAtNanos.get() >= openDurationNanos) {
            if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                successCount.set(0);
                return true;
            }
            return true;   // race winner
        }
        return false;
    }

    private void onSuccess() {
        State s = state.get();
        if (s == State.HALF_OPEN) {
            long c = successCount.incrementAndGet();
            if (c >= successThreshold) {
                state.set(State.CLOSED);
                failureCount.set(0);
                successCount.set(0);
            }
        } else if (s == State.CLOSED) {
            failureCount.set(0);   // 重置
        }
    }

    private void onFailure() {
        State s = state.get();
        if (s == State.HALF_OPEN) {
            state.set(State.OPEN);
            openedAtNanos.set(System.nanoTime());
            successCount.set(0);
        } else if (s == State.CLOSED) {
            long c = failureCount.incrementAndGet();
            if (c >= failureThreshold) {
                state.set(State.OPEN);
                openedAtNanos.set(System.nanoTime());
            }
        }
    }

    public State getState() {
        return state.get();
    }

    public String name() {
        return getClass().getSimpleName() + "@" + System.identityHashCode(this);
    }

    public enum State {CLOSED, HALF_OPEN, OPEN}
}

package com.zifang.util.core.lang;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 流畅断言工具（对标 AssertJ / Spring Assert）。
 * <p>
 * 与 JDK {@code Objects.requireNonNull} / Apache {@code Validate} 不同，本类：
 * <ul>
 *   <li>每个方法返回入参，方便链式调用</li>
 *   <li>支持 {@link Supplier} 形式的延迟消息</li>
 *   <li>对集合/Map/字符串提供领域特定的断言</li>
 * </ul>
 * <p>
 * 用法：
 * <pre>{@code
 *   String name = Assert.notBlank(req.getName(), "name");
 *   List<Long> ids = Assert.notEmpty(req.getIds(), "ids");
 *   User user = Assert.notNull(userRepo.find(id), () -> "user not found: " + id);
 * }</pre>
 *
 * @author zifang
 */
public final class Assert {

    private Assert() {
    }

    // ===== 通用断言 =====

    public static <T> T notNull(T obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return obj;
    }

    public static <T> T notNull(T obj, Supplier<String> msgSupplier) {
        if (obj == null) {
            throw new IllegalArgumentException(msgSupplier.get());
        }
        return obj;
    }

    public static String notBlank(String s, String name) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return s;
    }

    public static String notEmpty(String s, String name) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return s;
    }

    public static <T, C extends Collection<T>> C notEmpty(C c, String name) {
        if (c == null || c.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return c;
    }

    public static <K, V, M extends Map<K, V>> M notEmpty(M m, String name) {
        if (m == null || m.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return m;
    }

    public static <T> T[] notEmpty(T[] arr, String name) {
        if (arr == null || arr.length == 0) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return arr;
    }

    // ===== 数值断言 =====

    public static long positive(long v, String name) {
        if (v <= 0) throw new IllegalArgumentException(name + " must be positive, but was " + v);
        return v;
    }

    public static int positive(int v, String name) {
        if (v <= 0) throw new IllegalArgumentException(name + " must be positive, but was " + v);
        return v;
    }

    public static long nonNegative(long v, String name) {
        if (v < 0) throw new IllegalArgumentException(name + " must be non-negative, but was " + v);
        return v;
    }

    public static double inRange(double v, double min, double max, String name) {
        if (v < min || v > max) {
            throw new IllegalArgumentException(
                    name + " must be in [" + min + ", " + max + "], but was " + v);
        }
        return v;
    }

    // ===== 状态断言 =====

    public static void isTrue(boolean cond, String msg) {
        if (!cond) throw new IllegalArgumentException(msg);
    }

    public static void isTrue(boolean cond, Supplier<String> msgSupplier) {
        if (!cond) throw new IllegalArgumentException(msgSupplier.get());
    }

    public static void state(boolean cond, String msg) {
        if (!cond) throw new IllegalStateException(msg);
    }

    // ===== 字符串断言 =====

    public static String matches(String s, String pattern, String name) {
        notNull(s, name);
        if (!s.matches(pattern)) {
            throw new IllegalArgumentException(
                    name + "='" + s + "' does not match pattern=" + pattern);
        }
        return s;
    }

    public static String maxLength(String s, int max, String name) {
        notNull(s, name);
        if (s.length() > max) {
            throw new IllegalArgumentException(
                    name + " length " + s.length() + " exceeds max " + max);
        }
        return s;
    }
}
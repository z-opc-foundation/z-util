package com.zifang.util.core.lang;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author: zifang
 * @time: 2021-11-09 10:53:00
 * @description: exception util
 * @version: JDK 1.8
 */
public class ExceptionUtil {

    /**
     * @author: zifang
     * @description: get origin throwable
     * @time: 2021/11/9 10:54
     * @params: [throwable] request
     * @return: java.lang.Throwable response
     */
    public static Throwable getOriginThrowable(Throwable throwable) {
        Throwable originalThrowable = throwable;
        if (throwable instanceof InvocationTargetException
                || throwable instanceof UndeclaredThrowableException) {
            originalThrowable = getOriginThrowable(throwable.getCause());
        }
        return originalThrowable;
    }

    /**
     * getOriginException方法。
     * * @param runtimeException RuntimeException类型参数
     *
     * @return static Throwable类型返回值
     */
    public static Throwable getOriginException(RuntimeException runtimeException) {
        Throwable throwable = runtimeException.getCause();
        if (null == throwable) {
            return runtimeException;
        } else if (throwable instanceof RuntimeException) {
            return getOriginException((RuntimeException) throwable);
        } else {
            return throwable;
        }
    }

    /**
     * toString方法。
     * * @param e Throwable类型参数
     *
     * @return static String类型返回值
     */
    public static String toString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * toString方法。
     * * @param e Throwable类型参数
     *
     * @param retainLength int类型参数
     * @return static String类型返回值
     */
    public static String toString(Throwable e, int retainLength) {
        if (retainLength <= 0) {
            throw new RuntimeException("Length don't allow less than zero");
        }
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().substring(0, retainLength);
    }

    /**
     * doWithCatch方法。
     * * @param callback ExceptionCallbackReturnT类型参数
     *
     * @param wrapperException ClassE类型参数
     * @return static <T, E extends RuntimeException> T类型返回值
     */
    public static <T, E extends RuntimeException> T doWithCatch(ExceptionCallbackReturn<T> callback,
                                                                Class<E> wrapperException) {
        try {
            return callback.doOperate();
        } catch (Exception e) {
            try {
                RuntimeException runtimeException = wrapperException.newInstance();
                runtimeException.initCause(e);
                throw runtimeException;
            } catch (Exception ex) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * doWithCatchReturn方法。
     * * @param callback ExceptionCallbackReturnT类型参数
     *
     * @return static <T> T类型返回值
     */
    public static <T> T doWithCatchReturn(ExceptionCallbackReturn<T> callback) {
        try {
            return callback.doOperate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * doWithCatch方法。
     * * @param callback ExceptionCallback类型参数
     *
     * @return static void类型返回值
     */
    public static void doWithCatch(ExceptionCallback callback) {
        try {
            callback.doOperate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * doWithIgnore方法。
     * * @param callback ExceptionCallback类型参数
     *
     * @return static void类型返回值
     */
    public static void doWithIgnore(ExceptionCallback callback) {
        try {
            callback.doOperate();
        } catch (Exception ignored) {
        }
    }

    /**
     * assertTrue方法。
     * * @param condition boolean类型参数
     *
     * @param message String类型参数
     * @return static void类型返回值
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    @FunctionalInterface
/**
 * ExceptionCallbackReturn接口。
 */
    public interface ExceptionCallbackReturn<T> {

        T doOperate() throws Exception;
    }

    @FunctionalInterface
/**
 * ExceptionCallback接口。
 */
    public interface ExceptionCallback {

        void doOperate() throws Exception;

    }

}

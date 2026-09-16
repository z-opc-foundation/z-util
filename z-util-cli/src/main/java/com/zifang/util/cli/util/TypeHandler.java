package com.zifang.util.cli.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Handles conversion of option string values to typed values.
 */
public class TypeHandler {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    private TypeHandler() {
    }

    /**
     * Convert a string value to a Number.
     */
    public static Number toNumber(final String value, final Class<?> targetClass) throws ParseException {
        if (value == null) {
            return null;
        }
        if (targetClass == Integer.class || targetClass == Integer.TYPE) {
            return Integer.decode(value);
        }
        if (targetClass == Long.class || targetClass == Long.TYPE) {
            return Long.decode(value);
        }
        if (targetClass == Double.class || targetClass == Double.TYPE) {
            return NumberFormat.getInstance().parse(value);
        }
        if (targetClass == Float.class || targetClass == Float.TYPE) {
            return Float.parseFloat(value);
        }
        if (targetClass == Short.class || targetClass == Short.TYPE) {
            return Short.decode(value);
        }
        if (targetClass == Byte.class || targetClass == Byte.TYPE) {
            return Byte.decode(value);
        }
        throw new ParseException("Cannot parse value as " + targetClass.getName(), 0);
    }

    /**
     * Convert a string value to a Date using the given pattern.
     */
    public static Date toDate(final String value, final String pattern) throws ParseException {
        if (value == null) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.parse(value);
    }

    /**
     * Convert a string value to a Date using the default format.
     */
    public static Date toDate(final String value) throws ParseException {
        if (value == null) {
            return null;
        }
        return DateFormat.getDateInstance().parse(value);
    }

    /**
     * Convert a string value to a URI.
     */
    public static URI toURI(final String value) {
        if (value == null) {
            return null;
        }
        return URI.create(value);
    }

    /**
     * Convert a string value to a URL.
     */
    public static URL toURL(final String value) throws java.net.MalformedURLException {
        if (value == null) {
            return null;
        }
        return new URL(value);
    }

    /**
     * Convert a string value to a File.
     */
    public static File toFile(final String value) {
        if (value == null) {
            return null;
        }
        return new File(value);
    }

    /**
     * Convert a string value to a Class.
     */
    @SuppressWarnings("unchecked")
    /**
     * toClass方法。
     *      * @param value final类型参数
     * @return static <T> Class<T>类型返回值
     */
    public static <T> Class<T> toClass(final String value) throws ClassNotFoundException {
        if (value == null) {
            return null;
        }
        return (Class<T>) Class.forName(value);
    }

    /**
     * Check if a string represents a number.
     */
    public static boolean isNumber(final String value) {
        if (value == null) {
            return false;
        }
        return NUMBER_PATTERN.matcher(value).matches();
    }

    /**
     * Convert a string value to the specified type.
     */
    @SuppressWarnings("unchecked")
    /**
     * cast方法。
     *      * @param value final类型参数
     * @param targetClass final类型参数
     * @return static <T> T类型返回值
     */
    public static <T> T cast(final String value, final Class<T> targetClass) throws ParseException, MalformedURLException, ClassNotFoundException {
        if (value == null) {
            return null;
        }
        if (targetClass == String.class) {
            return (T) value;
        }
        if (targetClass == Boolean.class || targetClass == Boolean.TYPE) {
            return (T) Boolean.valueOf(value);
        }
        if (targetClass == Integer.class || targetClass == Integer.TYPE) {
            return (T) toNumber(value, targetClass);
        }
        if (targetClass == Long.class || targetClass == Long.TYPE) {
            return (T) toNumber(value, targetClass);
        }
        if (targetClass == Double.class || targetClass == Double.TYPE) {
            return (T) toNumber(value, targetClass);
        }
        if (targetClass == Float.class || targetClass == Float.TYPE) {
            return (T) toNumber(value, targetClass);
        }
        if (targetClass == Short.class || targetClass == Short.TYPE) {
            return (T) toNumber(value, targetClass);
        }
        if (targetClass == Byte.class || targetClass == Byte.TYPE) {
            return (T) toNumber(value, targetClass);
        }
        if (targetClass == Date.class) {
            return (T) toDate(value);
        }
        if (targetClass == URI.class) {
            return (T) toURI(value);
        }
        if (targetClass == URL.class) {
            return (T) toURL(value);
        }
        if (targetClass == File.class) {
            return (T) toFile(value);
        }
        if (Class.class.isAssignableFrom(targetClass)) {
            return (T) toClass(value);
        }
        throw new ParseException("Cannot cast value to " + targetClass.getName(), 0);
    }
}

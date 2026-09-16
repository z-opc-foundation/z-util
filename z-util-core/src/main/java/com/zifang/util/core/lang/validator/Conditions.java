package com.zifang.util.core.lang.validator;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 各种验证条件的集中管理
 */
public class Conditions {

    /**
     * 判断对象是否非空的谓词
     */
    public static Predicate<Object> IS_NOT_NULL = Objects::nonNull;

    /**
     * 判断对象是否为空（为null）的谓词
     */
    public static Predicate<Object> IS_NULL = Objects::isNull;

    /**
     * 判断对象是否为数组的谓词
     */
    public static Predicate<Object> IS_ARRAY = (e) -> true;
}

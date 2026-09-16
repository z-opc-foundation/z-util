package com.zifang.util.validation.core;

import java.lang.reflect.Field;

/**
 * 校验器接口
 *
 * @param <T> 注解类型
 */
public interface Validator<T> {

    /**
     * 校验字段
     *
     * @param target     目标对象
     * @param field      字段
     * @param annotation 注解
     * @param result     校验结果收集器
     */
    void validate(Object target, Field field, T annotation, ValidateResult result);
}
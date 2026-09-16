package com.zifang.util.http.base.define;

import com.zifang.util.core.lang.tuples.Pair;

import java.lang.reflect.Parameter;

/**
 * 参数值对
 * <p>
 * 用于将方法参数和对应的值封装成一个键值对。
 * </p>
 *
 * @param <Parameter> 参数类型
 * @param <Object>    值类型
 * @author zifang
 * @see Pair
 */
public class ParameterValuePair extends Pair<Parameter, Object> {

    /**
     * 构造一个参数值对。
     *
     * @param parameter 方法参数
     * @param obj       参数对应的值
     */
    public ParameterValuePair(Parameter parameter, Object obj) {
        super(parameter, obj);
    }

    /**
     * 获取参数。
     *
     * @return 方法参数
     */
    public Parameter getParameter() {
        return getA();
    }

    /**
     * 获取参数值。
     *
     * @return 参数对应的值
     */
    public Object getObj() {
        return getB();
    }


}

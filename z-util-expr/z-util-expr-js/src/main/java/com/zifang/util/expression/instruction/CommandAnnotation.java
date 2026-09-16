package com.zifang.util.expression.instruction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 命令注解
 * 用于标记方法为指令处理方法，注解值即为指令码
 *
 * @author zifang
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandAnnotation {

    /**
     * 指令码
     *
     * @return 指令的唯一标识字符串
     */
    String value();
}

package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明当前类是给定 service 接口的 SPI 提供者：编译期合并生成/更新
 * {@code META-INF/services/{service}} 注册文件，免去手写与漏写。
 *
 * <p>对标 Google AutoService。一个类可以实现多个 service，写数组即可；
 * 处理器会校验提供者与接口的 assignability，不匹配直接报编译错误。</p>
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZAutoService {

    /** 本类作为提供者的 service 接口（一个或多个）。 */
    Class<?>[] value();
}

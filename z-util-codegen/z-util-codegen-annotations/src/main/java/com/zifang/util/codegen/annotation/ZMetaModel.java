package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个实体类：编译期生成同包字段元模型 {@code {SimpleName}_}——
 * 每个实例字段一个 {@code public static final String} 常量（值 = 字段名，
 * 常量名 = 字段名的 UPPER_SNAKE_CASE），外加 {@code ENTITY}（类全名）与 {@code ALL}（全部字段名）。
 *
 * <p>对标 JPA Metamodel / QueryDSL 的字段引用：动态拼 SQL、拼排序字段时
 * 用 {@code User_.USER_NAME} 代替裸字符串，改名即编译错。</p>
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZMetaModel {
}

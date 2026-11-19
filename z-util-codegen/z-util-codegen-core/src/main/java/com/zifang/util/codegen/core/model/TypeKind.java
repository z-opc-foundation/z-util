package com.zifang.util.codegen.core.model;

/**
 * 被处理类型的形态。
 *
 * @author zifang
 */
public enum TypeKind {

    /** 普通类。 */
    CLASS,
    /** 接口。 */
    INTERFACE,
    /** 枚举。 */
    ENUM,
    /** 记录类（record）。 */
    RECORD,
    /** 注解类型。 */
    ANNOTATION
}

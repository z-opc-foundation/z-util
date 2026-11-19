package com.zifang.util.codegen.core.spi;

import com.zifang.util.codegen.core.spec.ConstructorSpec;
import com.zifang.util.codegen.core.spec.FieldSpec;
import com.zifang.util.codegen.core.spec.MethodSpec;

/**
 * 成员注入出口：把方法、构造器、字段直接注入目标类本体（与 Lombok 同义）。
 *
 * <p>重复签名会被跳过而不是报错——handler 可以放心地对同一字段多次声明。</p>
 *
 * @author zifang
 */
public interface MemberEmitter {

    /**
     * 注入方法。
     *
     * @return true 表示注入成功；false 表示同名同参方法已存在，本次跳过
     */
    boolean addMethod(MethodSpec spec);

    /**
     * 注入构造器。
     *
     * @return true 表示注入成功；false 表示同参构造器已存在，本次跳过
     */
    boolean addConstructor(ConstructorSpec spec);

    /**
     * 注入字段。
     *
     * @return true 表示注入成功；false 表示同名字段已存在，本次跳过
     */
    boolean addField(FieldSpec spec);
}

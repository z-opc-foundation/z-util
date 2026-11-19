package com.zifang.util.codegen.it.model;

import com.zifang.util.codegen.annotation.ZData;
import com.zifang.util.codegen.annotation.ZNonNull;

/**
 * {@code @ZData} 的测试夹具：一次性拿到 getter/setter/toString/equals/必填参构造器。
 */
@ZData
public class Account {

    @ZNonNull
    private final String id;

    private String owner;

    private int balance;

    private boolean active;
}

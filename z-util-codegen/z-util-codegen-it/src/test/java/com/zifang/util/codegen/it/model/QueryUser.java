package com.zifang.util.codegen.it.model;

import com.zifang.util.codegen.annotation.ZMetaModel;

/**
 * 元模型夹具：camelCase 与 URL 大写连写两种命名形态。
 */
@ZMetaModel
public class QueryUser {

    private String userName;
    private int age;
    private String urlValue;

    public String getUserName() {
        return userName;
    }
}

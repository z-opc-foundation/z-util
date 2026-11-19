package com.zifang.util.codegen.it.model;

import com.zifang.util.codegen.annotation.ZBuilder;
import com.zifang.util.codegen.annotation.ZGetter;

/**
 * {@code @ZBuilder} 的测试夹具。
 */
@ZBuilder
@ZGetter
public class Product {

    private String sku;

    private String title;

    private int price;

    public Product(String sku, String title, int price) {
        this.sku = sku;
        this.title = title;
        this.price = price;
    }
}

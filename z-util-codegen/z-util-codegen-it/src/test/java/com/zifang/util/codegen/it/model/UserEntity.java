package com.zifang.util.codegen.it.model;

/**
 * 转换测试用的源对象。
 */
public class UserEntity {

    private String id;

    private String name;

    private AddressEntity address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }
}

package io.zifu.z.serialize.it.model;

import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;

/**
 * Simple test message for codegen verification.
 */
@ZMessage(id = 500, name = "it.SimpleUser")
public class SimpleUser {

    @ZField(id = 1, type = FieldType.VARINT)
    public long id;

    @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
    public String name;

    @ZField(id = 3, type = FieldType.VARINT)
    public int age;

    @ZField(id = 4, type = FieldType.VARINT)
    public boolean active;

    public SimpleUser() {}
}

package io.zifu.z.serialize.bench;

import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;

@ZMessage(id = 100, name = "bench.User")
public class User {
    @ZField(id = 1, type = FieldType.VARINT)
    public long id;
    @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
    public String name;
    @ZField(id = 3, type = FieldType.VARINT)
    public int age;
    @ZField(id = 4, type = FieldType.VARINT)
    public boolean active;
    @ZField(id = 5, type = FieldType.FIXED64)
    public double balance;

    public User() {
    }
}

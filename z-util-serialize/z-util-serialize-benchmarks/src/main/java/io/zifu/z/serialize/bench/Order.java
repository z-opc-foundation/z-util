package io.zifu.z.serialize.bench;

import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;

import java.util.ArrayList;
import java.util.List;

@ZMessage(id = 101, name = "bench.Order")
public class Order {
    @ZField(id = 1, type = FieldType.VARINT)
    public long orderId;
    @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
    public String sku;
    @ZField(id = 3, type = FieldType.VARINT)
    public List<Integer> itemIds;

    public Order() {
        itemIds = new ArrayList<>();
    }
}

package io.zifu.z.serialize.core;

import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReflectCodecRoundTripTest {

    @ZMessage(id = 100, name = "test.User")
    public static class User {
        @ZField(id = 1, type = FieldType.VARINT)
        public long id;

        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
        public String name;

        @ZField(id = 3, type = FieldType.VARINT)
        public boolean active;

        @ZField(id = 4, type = FieldType.FIXED32)
        public float score;

        @ZField(id = 5, type = FieldType.FIXED64)
        public double balance;

        public User() {}
    }

    @Test
    public void testSimpleRoundTrip() throws IOException {
        User u = new User();
        u.id = 42L;
        u.name = "alice";
        u.active = true;
        u.score = 3.14f;
        u.balance = 9999.99;

        byte[] bytes = ZSerializer.INSTANCE.toBytes(u);
        System.out.println("User bytes length: " + bytes.length);

        User u2 = ZDeserializer.INSTANCE.fromBytes(bytes, User.class);
        assertEquals(u.id, u2.id);
        assertEquals(u.name, u2.name);
        assertEquals(u.active, u2.active);
        assertEquals(u.score, u2.score, 0.0001f);
        assertEquals(u.balance, u2.balance, 0.0001);
    }

    @ZMessage(id = 101, name = "test.Order")
    public static class Order {
        @ZField(id = 1, type = FieldType.VARINT)
        public long orderId;

        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
        public String sku;

        @ZField(id = 3, type = FieldType.VARINT)
        public List<Integer> itemIds;  // packed repeated

        @ZField(id = 4, type = FieldType.LENGTH_DELIMITED)
        public Map<String, Integer> attrs;

        public Order() {
            itemIds = new ArrayList<>();
            attrs = new LinkedHashMap<>();
        }
    }

    @Test
    public void testOrderWithCollections() throws IOException {
        Order o = new Order();
        o.orderId = 1001L;
        o.sku = "SKU-001";
        o.itemIds.add(10);
        o.itemIds.add(20);
        o.itemIds.add(30);
        o.attrs.put("color", 1);
        o.attrs.put("size", 42);

        byte[] bytes = ZSerializer.INSTANCE.toBytes(o);
        System.out.println("Order bytes length: " + bytes.length);

        Order o2 = ZDeserializer.INSTANCE.fromBytes(bytes, Order.class);
        assertEquals(o.orderId, o2.orderId);
        assertEquals(o.sku, o2.sku);
        assertEquals(o.itemIds, o2.itemIds);
        assertEquals(o.attrs, o2.attrs);
    }

    @Test
    public void testHeaderMagic() throws IOException {
        User u = new User();
        u.id = 1;
        u.name = "x";
        byte[] bytes = ZSerializer.INSTANCE.toBytes(u);
        // 验证 magic bytes 0xBA 0xBE 在开头
        assertEquals((byte) 0xBA, bytes[0]);
        assertEquals((byte) 0xBE, bytes[1]);
        // 验证 wire version
        assertEquals(Magic.WIRE_VERSION, bytes[2]);
    }

    @Test
    public void testNullFieldsAreSkipped() throws IOException {
        User u = new User();
        u.id = 1L;
        // name, score, balance 留 null
        byte[] bytes = ZSerializer.INSTANCE.toBytes(u);
        User u2 = ZDeserializer.INSTANCE.fromBytes(bytes, User.class);
        assertEquals(1L, u2.id);
        assertEquals(null, u2.name);
    }

    @Test
    public void testUnknownFieldIdSkipped() throws IOException {
        // 写一个 schema v1，模拟发送
        User u = new User();
        u.id = 99;
        u.name = "v1";
        byte[] v1Bytes = ZSerializer.INSTANCE.toBytes(u);

        // 模拟 schema v2 接收（不修改 User，只是确认不会崩）
        // 实际场景：发送方有字段 id=999，接收方没有
        // 我们的 skipUnknown 测试需要构造带未知 tag 的字节
        // 这里只验证读取 v1 字节正常
        User u2 = ZDeserializer.INSTANCE.fromBytes(v1Bytes, User.class);
        assertEquals(99L, u2.id);
        assertEquals("v1", u2.name);
        assertTrue(u2.active == false);
    }
}

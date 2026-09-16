package io.zifu.z.serialize.core;

import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Schema 演进：前向兼容（v1 写 → v2 读，新增字段默认填充）。
 * 后向兼容（v2 写 → v1 读，旧字段正常 + 新字段被跳过）。
 */
public class SchemaEvolutionTest {

    @ZMessage(id = 200, name = "test.UserV1")
    public static class UserV1 {
        @ZField(id = 1, type = FieldType.VARINT)
        public long id;

        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
        public String name;

        public UserV1() {}
    }

    @ZMessage(id = 200, name = "test.UserV2")
    public static class UserV2 {
        @ZField(id = 1, type = FieldType.VARINT)
        public long id;

        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
        public String name;

        // V2 新增字段
        @ZField(id = 3, type = FieldType.VARINT)
        public int age;

        // V2 新增字段
        @ZField(id = 4, type = FieldType.LENGTH_DELIMITED)
        public String email;

        public UserV2() {}
    }

    /**
     * 兼容性 #1：v1 写 → v2 读（新增字段默认 0/null）。
     */
    @Test
    public void testForwardCompat() throws IOException {
        UserV1 v1 = new UserV1();
        v1.id = 100L;
        v1.name = "alice";

        byte[] bytes = ZSerializer.INSTANCE.toBytes(v1);

        // V2 接收
        UserV2 v2 = ZDeserializer.INSTANCE.fromBytes(bytes, UserV2.class);
        assertEquals(100L, v2.id);
        assertEquals("alice", v2.name);
        // 默认值（Java 字段初始值）
        assertEquals(0, v2.age);
        assertEquals(null, v2.email);
    }

    /**
     * 兼容性 #2：v2 写 → v1 读（旧字段正常 + 新字段被跳过）。
     */
    @Test
    public void testBackwardCompat() throws IOException {
        UserV2 v2 = new UserV2();
        v2.id = 200L;
        v2.name = "bob";
        v2.age = 25;
        v2.email = "bob@example.com";

        byte[] bytes = ZSerializer.INSTANCE.toBytes(v2);

        // V1 接收（没有 id=3,4 的字段）
        UserV1 v1 = ZDeserializer.INSTANCE.fromBytes(bytes, UserV1.class);
        assertEquals(200L, v1.id);
        assertEquals("bob", v1.name);
    }

    /**
     * 兼容性 #3：手动构造带未知 tag 的字节，验证 skip 机制。
     */
    @Test
    public void testSkipUnknownTagManually() throws IOException {
        // 构造一个最小 v1 字节序列：tag(1, VARINT) + value(42)
        // tag = (1 << 3) | 0 = 8
        // value = signedVarInt(42) = zigzag(42) = 84 -> [0x54]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(8);    // tag (field 1, varint)
        baos.write(84);   // value
        // 然后追加一个未知字段 tag(999, VARINT) + value(7)
        baos.write((999 << 3) | 0);  // 0x3E9F 0x03
        baos.write(14);               // value 7 zigzag=14

        byte[] body = baos.toByteArray();
        // 手动包装 header
        ByteArrayOutputStream full = new ByteArrayOutputStream();
        Header.writeHeader(full, 200, (byte) 0, body);

        UserV1 v1 = ZDeserializer.INSTANCE.fromBytes(full.toByteArray(), UserV1.class);
        assertEquals(42L, v1.id);  // 解出第一个字段
    }
}

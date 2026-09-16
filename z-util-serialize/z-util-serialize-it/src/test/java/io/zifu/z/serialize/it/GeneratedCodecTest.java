package io.zifu.z.serialize.it;

import io.zifu.z.serialize.core.CodecRegistry;
import io.zifu.z.serialize.core.ZDeserializer;
import io.zifu.z.serialize.core.ZSerializer;
import io.zifu.z.serialize.it.model.SimpleUser;
import io.zifu.z.serialize.it.model.SimpleUserCodec;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 验证 codegen 生成的 Codec 能正确序列化/反序列化。
 *
 * <p>测试逻辑：使用 codegen 生成的 {@code SimpleUserCodec}（通过 CodecRegistry 注册）
 * 对 {@link SimpleUser} 进行序列化+反序列化 round-trip。</p>
 */
public class GeneratedCodecTest {

    @Before
    public void setUp() {
        // 强制触发 SimpleUserCodec 的 static initializer（注册到 CodecRegistry）
        CodecRegistry.register(SimpleUser.class, SimpleUserCodec.INSTANCE);
    }

    @Test
    public void testCodecAutoRegistered() {
        assertTrue("SimpleUserCodec should be registered",
                CodecRegistry.isRegistered(SimpleUser.class));
        assertNotNull(CodecRegistry.get(SimpleUser.class));
    }

    @Test
    public void testRoundTrip() throws Exception {
        SimpleUser u = new SimpleUser();
        u.id = 42L;
        u.name = "Alice";
        u.age = 30;
        u.active = true;

        // 序列化
        byte[] bytes = ZSerializer.INSTANCE.toBytes(u);
        assertNotNull(bytes);
        assertTrue("Encoded bytes should not be empty", bytes.length > 0);

        // 反序列化
        SimpleUser u2 = ZDeserializer.INSTANCE.fromBytes(bytes, SimpleUser.class);

        assertEquals(u.id, u2.id);
        assertEquals(u.name, u2.name);
        assertEquals(u.age, u2.age);
        assertEquals(u.active, u2.active);
    }

    @Test
    public void testRoundTripWithNullFields() throws Exception {
        SimpleUser u = new SimpleUser();
        u.id = 100L;
        u.name = null;
        u.age = 0;
        u.active = false;

        byte[] bytes = ZSerializer.INSTANCE.toBytes(u);
        SimpleUser u2 = ZDeserializer.INSTANCE.fromBytes(bytes, SimpleUser.class);

        assertEquals(100L, u2.id);
        assertNull(u2.name);
        assertEquals(0, u2.age);
        assertFalse(u2.active);
    }

    @Test
    public void testZeroReflectionViaGeneratedCodec() throws Exception {
        assertEquals("Should use generated SimpleUserCodec",
                "io.zifu.z.serialize.it.model.SimpleUserCodec",
                CodecRegistry.get(SimpleUser.class).getClass().getName());
    }
}

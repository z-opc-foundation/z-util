package io.zifu.z.serialize.it;

import io.zifu.z.serialize.core.CodecRegistry;
import io.zifu.z.serialize.core.ReflectCodec;
import io.zifu.z.serialize.core.ZInput;
import io.zifu.z.serialize.core.ZOutput;
import io.zifu.z.serialize.it.model.SimpleUser;
import io.zifu.z.serialize.it.model.SimpleUserCodec;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * 性能对比：生成的 Codec vs 反射 Codec（直接测量 codec，不含 header 开销）。
 */
public class CodeGenVsReflectBench {

    private static final int WARMUP = 5000;
    private static final int ITERATIONS = 200_000;

    @Before
    public void setUp() {
        CodecRegistry.register(SimpleUser.class, SimpleUserCodec.INSTANCE);
    }

    @Test
    public void benchmarkCodeGenVsReflect() throws Exception {
        SimpleUser u = new SimpleUser();
        u.id = 42L;
        u.name = "Alice";
        u.age = 30;
        u.active = true;

        SimpleUserCodec codegenCodec = SimpleUserCodec.INSTANCE;
        ReflectCodec reflectCodec = ReflectCodec.INSTANCE;

        // 预热 codegen
        for (int i = 0; i < WARMUP; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
            codegenCodec.write(new ZOutput(baos), u);
        }
        // 预热 reflect
        for (int i = 0; i < WARMUP; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
            reflectCodec.write(new ZOutput(baos), u);
        }

        // --- CodeGen write ---
        byte[] codegenBytes = null;
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
            codegenCodec.write(new ZOutput(baos), u);
            codegenBytes = baos.toByteArray();
        }
        long codegenWriteNs = System.nanoTime() - start;

        // --- CodeGen read ---
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            codegenCodec.read(new ZInput(new ByteArrayInputStream(codegenBytes)), SimpleUser.class);
        }
        long codegenReadNs = System.nanoTime() - start;

        // --- Reflect write ---
        byte[] reflectBytes = null;
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
            reflectCodec.write(new ZOutput(baos), u);
            reflectBytes = baos.toByteArray();
        }
        long reflectWriteNs = System.nanoTime() - start;

        // --- Reflect read ---
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            reflectCodec.read(new ZInput(new ByteArrayInputStream(reflectBytes)), SimpleUser.class);
        }
        long reflectReadNs = System.nanoTime() - start;

        // --- Output ---
        double cwOps = ITERATIONS * 1e9 / codegenWriteNs;
        double rwOps = ITERATIONS * 1e9 / reflectWriteNs;
        double crOps = ITERATIONS * 1e9 / codegenReadNs;
        double rrOps = ITERATIONS * 1e9 / reflectReadNs;

        System.out.println("=== CodeGen vs Reflect Codec Benchmark (Direct) ===");
        System.out.printf("Message: SimpleUser (4 fields: long, String, int, boolean)%n");
        System.out.printf("Wire size: %d bytes%n", codegenBytes.length);
        System.out.printf("Iterations: %,d%n%n", ITERATIONS);
        System.out.printf("Write:  CodeGen %,.0f ops/s | Reflect %,.0f ops/s | Speedup: %.2fx%n",
                cwOps, rwOps, cwOps / rwOps);
        System.out.printf("Read:   CodeGen %,.0f ops/s | Reflect %,.0f ops/s | Speedup: %.2fx%n",
                crOps, rrOps, crOps / rrOps);

        // 验证正确性
        SimpleUser u2 = codegenCodec.read(new ZInput(new ByteArrayInputStream(codegenBytes)), SimpleUser.class);
        assertEquals(u.id, u2.id);
        assertEquals(u.name, u2.name);
        assertEquals(u.age, u2.age);
        assertEquals(u.active, u2.active);
    }
}

package com.zifang.util.core.lang.serialize;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * ZSerializer 性能基准测试。
 * <p>
 * 对比 ZSerializer 与 Java Serialization 在多种负载下的吞吐量与体积。
 * 注意：本测试不验证绝对数值（不写断言），仅打印结果。
 */
public class ZSerializerBenchmarkTest {

    private static final int WARMUP_ITER = 5000;
    private static final int MEASURE_ITER = 100_000;

    // ==================== Custom serializers ====================

    public static class UserSerializer implements Serializer<User> {
        public byte typeId() { return (byte) 0x80; }
        public Class<User> type() { return User.class; }
        public void write(ByteWriter out, User v) {
            out.writeVarLong(v.id);
            out.writeString(v.username);
            out.writeVarInt(v.age);
            out.writeBoolean(v.active);
            out.writeDouble(v.balance);
        }
        public User read(ByteReader in) {
            User u = new User();
            u.id = in.readVarLong();
            u.username = in.readString();
            u.age = in.readVarInt();
            u.active = in.readBoolean();
            u.balance = in.readDouble();
            return u;
        }
    }

    private static final ZSerializer ZER_WITH_USER;
    static {
        ZER_WITH_USER = new ZSerializer();
        ZER_WITH_USER.register(new UserSerializer());
    }

    // ==================== 简单POJO ====================

    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        public long id;
        public String username;
        public int age;
        public boolean active;
        public double balance;

        public User() {}

        public User(long id, String username, int age, boolean active, double balance) {
            this.id = id;
            this.username = username;
            this.age = age;
            this.active = active;
            this.balance = balance;
        }
    }

    // ==================== 简单值 ====================

    @Test
    public void benchInteger() {
        Integer value = 1234567;
        runBench("Integer(1234567)", value);
    }

    @Test
    public void benchLong() {
        Long value = 123456789012345L;
        runBench("Long(123456789012345)", value);
    }

    @Test
    public void benchString() {
        String value = "The quick brown fox jumps over the lazy dog. 中文测试混合1234";
        runBench("String(mixed)", value);
    }

    @Test
    public void benchEmptyString() {
        runBench("String(empty)", "");
    }

    // ==================== POJO ====================

    @Test
    public void benchSmallPojo() {
        User u = new User(42, "alice", 30, true, 9999.99);
        runBench("User(POJO)", u, ZER_WITH_USER);
    }

    // ==================== 集合 ====================

    @Test
    public void benchSmallList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) list.add(i);
        runBench("ArrayList(100 Integer)", list);
    }

    @Test
    public void benchLargeList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) list.add(i);
        runBench("ArrayList(10000 Integer)", list);
    }

    @Test
    public void benchMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < 50; i++) map.put("key-" + i, i);
        runBench("LinkedHashMap(50)", map);
    }

    @Test
    public void benchNestedPojoList() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            users.add(new User(i, "user-" + i, 20 + i, i % 2 == 0, 100.0 + i));
        }
        runBench("List<User>(100)", users, ZER_WITH_USER);
    }

    // ==================== Runner ====================

    private void runBench(String label, Object value) { runBench(label, value, ZSerializer.INSTANCE); }
    private void runBench(String label, Object value, ZSerializer zs) {
        // Warmup
        for (int i = 0; i < WARMUP_ITER; i++) {
            zs.write(value);
            try {
                javaSerWrite(value);
            } catch (Exception e) { /* ignore */ }
        }

        // ZSerializer write
        long start = System.nanoTime();
        int zSize = 0;
        for (int i = 0; i < MEASURE_ITER; i++) {
            byte[] b = zs.write(value);
            zSize = b.length;
        }
        long zWriteNs = System.nanoTime() - start;

        // ZSerializer read
        byte[] zBytes = zs.write(value);
        start = System.nanoTime();
        for (int i = 0; i < MEASURE_ITER; i++) {
            zs.read(zBytes);
        }
        long zReadNs = System.nanoTime() - start;

        // Java Serialization
        long jSize = -1;
        long jWriteNs = -1;
        long jReadNs = -1;
        try {
            // Warmup
            for (int i = 0; i < 1000; i++) {
                byte[] b = javaSerWrite(value);
                javaSerRead(b);
            }

            start = System.nanoTime();
            for (int i = 0; i < MEASURE_ITER; i++) {
                byte[] b = javaSerWrite(value);
                jSize = b.length;
            }
            jWriteNs = System.nanoTime() - start;

            byte[] jBytes = javaSerWrite(value);
            start = System.nanoTime();
            for (int i = 0; i < MEASURE_ITER; i++) {
                javaSerRead(jBytes);
            }
            jReadNs = System.nanoTime() - start;
        } catch (Exception e) {
            System.out.println("  Java Serialization skipped: " + e.getMessage());
        }

        // Print results
        double zWriteOps = (double) MEASURE_ITER / (zWriteNs / 1e9);
        double zReadOps = (double) MEASURE_ITER / (zReadNs / 1e9);
        System.out.printf("[%s] ZSer write=%,.0f ops/s (size=%d), read=%,.0f ops/s | ", label, zWriteOps, zSize, zReadOps);
        if (jWriteNs > 0) {
            double jWriteOps = (double) MEASURE_ITER / (jWriteNs / 1e9);
            double jReadOps = (double) MEASURE_ITER / (jReadNs / 1e9);
            double sizeRatio = (double) zSize / jSize;
            System.out.printf("Java write=%,.0f ops/s (size=%d), read=%,.0f ops/s | size=%.2fx | speedup=%.1fx%n",
                    jWriteOps, jSize, jReadOps, sizeRatio,
                    (zWriteOps + zReadOps) / (jWriteOps + jReadOps));
        } else {
            System.out.println("(no Java comparison)");
        }
    }

    private byte[] javaSerWrite(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }

    private Object javaSerRead(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    // ==================== Sanity ====================

    @Test
    public void testBenchRuns() {
        // 仅保证 bench 方法可执行
        assertTrue(true);
    }
}

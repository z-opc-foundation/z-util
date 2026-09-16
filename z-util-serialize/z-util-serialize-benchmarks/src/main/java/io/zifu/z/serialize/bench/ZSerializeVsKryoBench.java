package io.zifu.z.serialize.bench;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;
import io.zifu.z.serialize.core.ZSerializer;
import io.zifu.z.serialize.core.ZDeserializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZSerializeVsKryoBench {

    @ZMessage(id = 100, name = "bench.User")
    public static class User {
        @ZField(id = 1, type = FieldType.VARINT) public long id;
        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED) public String name;
        @ZField(id = 3, type = FieldType.VARINT) public int age;
        @ZField(id = 4, type = FieldType.VARINT) public boolean active;
        @ZField(id = 5, type = FieldType.FIXED64) public double balance;
        public User() {}
    }

    @ZMessage(id = 101, name = "bench.Order")
    public static class Order {
        @ZField(id = 1, type = FieldType.VARINT) public long orderId;
        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED) public String sku;
        @ZField(id = 3, type = FieldType.VARINT) public List<Integer> itemIds;
        public Order() { itemIds = new ArrayList<>(); }
    }

    private static final int WARMUP = 10_000;
    private static final int ITERATIONS = 200_000;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Z-Serialize vs Kryo Benchmark ===");
        System.out.println("Warmup=" + WARMUP + ", Iterations=" + ITERATIONS);
        System.out.println();

        Kryo kryo = new Kryo();
        kryo.register(User.class);
        kryo.register(Order.class);
        kryo.register(ArrayList.class);
        kryo.setRegistrationRequired(true);

        benchUser(kryo);
        benchOrder(kryo);

        System.out.println("=== Done ===");
    }

    private static void benchUser(Kryo kryo) throws IOException {
        User u = new User();
        u.id = 42L; u.name = "alice"; u.age = 30; u.active = true; u.balance = 9999.99;

        byte[] zBytes = ZSerializer.INSTANCE.toBytes(u);
        for (int i = 0; i < WARMUP; i++) ZSerializer.INSTANCE.toBytes(u);
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) ZSerializer.INSTANCE.toBytes(u);
        long zWriteNs = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) ZDeserializer.INSTANCE.fromBytes(zBytes, User.class);
        long zReadNs = System.nanoTime() - start;

        byte[] kBytes = kryoSer(kryo, u);
        for (int i = 0; i < WARMUP; i++) kryoSer(kryo, u);
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) kryoSer(kryo, u);
        long kWriteNs = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) kryoDeser(kryo, kBytes);
        long kReadNs = System.nanoTime() - start;

        printResult("User(Simple)", zBytes.length, kBytes.length, zWriteNs, zReadNs, kWriteNs, kReadNs);
    }

    private static void benchOrder(Kryo kryo) throws IOException {
        Order o = new Order();
        o.orderId = 1001L; o.sku = "SKU-001";
        o.itemIds.add(10); o.itemIds.add(20); o.itemIds.add(30);

        byte[] zBytes = ZSerializer.INSTANCE.toBytes(o);
        for (int i = 0; i < WARMUP; i++) ZSerializer.INSTANCE.toBytes(o);
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) ZSerializer.INSTANCE.toBytes(o);
        long zWriteNs = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) ZDeserializer.INSTANCE.fromBytes(zBytes, Order.class);
        long zReadNs = System.nanoTime() - start;

        byte[] kBytes = kryoSer(kryo, o);
        for (int i = 0; i < WARMUP; i++) kryoSer(kryo, o);
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) kryoSer(kryo, o);
        long kWriteNs = System.nanoTime() - start;
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) kryoDeser(kryo, kBytes);
        long kReadNs = System.nanoTime() - start;

        printResult("Order(List)", zBytes.length, kBytes.length, zWriteNs, zReadNs, kWriteNs, kReadNs);
    }

    private static void printResult(String label, int zSize, int kSize,
                                    long zWriteNs, long zReadNs, long kWriteNs, long kReadNs) {
        double zW = (double) ITERATIONS / (zWriteNs / 1e9);
        double zR = (double) ITERATIONS / (zReadNs / 1e9);
        double kW = (double) ITERATIONS / (kWriteNs / 1e9);
        double kR = (double) ITERATIONS / (kReadNs / 1e9);
        System.out.printf("[%s]%n", label);
        System.out.printf("  Z-Ser : write=%,.0f ops/s (size=%d), read=%,.0f ops/s%n", zW, zSize, zR);
        System.out.printf("  Kryo  : write=%,.0f ops/s (size=%d), read=%,.0f ops/s%n", kW, kSize, kR);
        System.out.printf("  Speed : write=%.1fx, read=%.1fx (vs Kryo)%n", zW/kW, zR/kR);
        System.out.printf("  Size  : Z-Ser is %.1fx of Kryo%n", (double)zSize/kSize);
        System.out.println();
    }

    @SuppressWarnings("unchecked")
    private static <T> byte[] kryoSer(Kryo kryo, T obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        Output out = new Output(baos);
        kryo.writeClassAndObject(out, obj);
        out.close();
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    private static Object kryoDeser(Kryo kryo, byte[] bytes) {
        return kryo.readClassAndObject(new Input(new ByteArrayInputStream(bytes)));
    }
}

package com.zifang.util.core.lang.serialize;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * ZSerializer 全面单元测试 - 覆盖所有支持类型 + 边界场景。
 */
public class ZSerializerTest {

    private final ZSerializer ser = ZSerializer.INSTANCE;

    // ==================== Null ====================

    @Test
    public void testNull() {
        byte[] data = ser.write(null);
        assertNull(ser.read(data));
    }

    // ==================== Boolean / Byte / Short / Char ====================

    @Test
    public void testBoolean() {
        assertEquals(Boolean.TRUE, ser.read(ser.write(Boolean.TRUE)));
        assertEquals(Boolean.FALSE, ser.read(ser.write(Boolean.FALSE)));
    }

    @Test
    public void testByte() {
        for (int v = -128; v <= 127; v++) {
            Byte b = (byte) v;
            assertEquals(b, ser.read(ser.write(b)));
        }
    }

    @Test
    public void testShort() {
        for (int v = -1000; v <= 1000; v++) {
            assertEquals((short) v, ser.read(ser.write((short) v)));
        }
    }

    @Test
    public void testCharacter() {
        assertEquals(Character.valueOf('A'), ser.read(ser.write(Character.valueOf('A'))));
        assertEquals(Character.valueOf('中'), ser.read(ser.write(Character.valueOf('中'))));
    }

    // ==================== Int / Long / Float / Double ====================

    @Test
    public void testInteger() {
        int[] values = {0, 1, -1, 100, -100, Integer.MAX_VALUE, Integer.MIN_VALUE, 12345};
        for (int v : values) {
            assertEquals(Integer.valueOf(v), ser.read(ser.write(Integer.valueOf(v))));
        }
    }

    @Test
    public void testLong() {
        long[] values = {0L, 1L, -1L, Long.MAX_VALUE, Long.MIN_VALUE, 1L << 40};
        for (long v : values) {
            assertEquals(Long.valueOf(v), ser.read(ser.write(Long.valueOf(v))));
        }
    }

    @Test
    public void testFloat() {
        for (float v : new float[]{0f, 1.5f, -3.14f, Float.MAX_VALUE, Float.MIN_VALUE}) {
            assertEquals(Float.valueOf(v), ser.read(ser.write(Float.valueOf(v))));
        }
    }

    @Test
    public void testDouble() {
        for (double v : new double[]{0d, 1.5d, -3.14d, Double.MAX_VALUE, Double.MIN_VALUE,
                Math.PI, Math.E}) {
            assertEquals(Double.valueOf(v), ser.read(ser.write(Double.valueOf(v))));
        }
    }

    // ==================== String ====================

    @Test
    public void testString() {
        assertEquals("hello", ser.read(ser.write("hello")));
        assertEquals("", ser.read(ser.write("")));
        assertNull(ser.read(ser.write((String) null)));
        assertEquals("中文测试", ser.read(ser.write("中文测试")));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append("x");
        assertEquals(sb.toString(), ser.read(ser.write(sb.toString())));
    }

    // ==================== Arrays ====================

    @Test
    public void testByteArray() {
        byte[] arr = {1, 2, 3, 4, 5};
        assertArrayEquals(arr, (byte[]) ser.read(ser.write(arr)));
    }

    @Test
    public void testEmptyByteArray() {
        byte[] empty = new byte[0];
        assertArrayEquals(empty, (byte[]) ser.read(ser.write(empty)));
    }

    @Test
    public void testNullByteArray() {
        assertNull(ser.read(ser.write((byte[]) null)));
    }

    @Test
    public void testBooleanArray() {
        boolean[] arr = {true, false, true, true};
        assertArrayEquals(arr, (boolean[]) ser.read(ser.write(arr)));
    }

    @Test
    public void testIntArray() {
        int[] arr = {1, 2, 3, 4, 5};
        assertArrayEquals(arr, (int[]) ser.read(ser.write(arr)));
    }

    @Test
    public void testLongArray() {
        long[] arr = {1L, 2L, 3L, 4L, Long.MAX_VALUE};
        assertArrayEquals(arr, (long[]) ser.read(ser.write(arr)));
    }

    @Test
    public void testDoubleArray() {
        double[] arr = {1.1, 2.2, 3.3};
        assertArrayEquals(arr, (double[]) ser.read(ser.write(arr)), 0.0001);
    }

    @Test
    public void testStringArray() {
        String[] arr = {"a", "b", "c"};
        Object result = ser.read(ser.write(arr));
        assertArrayEquals(arr, (String[]) result);
    }

    @Test
    public void testObjectArray() {
        Object[] arr = {1, "two", 3.0, true, null};
        Object[] result = (Object[]) ser.read(ser.write(arr));
        assertEquals(arr.length, result.length);
        assertEquals(arr[0], result[0]);
        assertEquals(arr[1], result[1]);
        assertEquals(arr[2], result[2]);
        assertEquals(arr[3], result[3]);
        assertNull(result[4]);
    }

    @Test
    public void testEmptyObjectArray() {
        Object[] arr = new Object[0];
        Object[] result = (Object[]) ser.read(ser.write(arr));
        assertEquals(0, result.length);
    }

    // ==================== Collections ====================

    @Test
    public void testArrayList() {
        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        Object result = ser.read(ser.write(list));
        assertTrue(result instanceof ArrayList);
        assertEquals(list, result);
    }

    @Test
    public void testEmptyArrayList() {
        ArrayList<Integer> empty = new ArrayList<>();
        Object result = ser.read(ser.write(empty));
        assertTrue(result instanceof ArrayList);
        assertEquals(0, ((ArrayList<?>) result).size());
    }

    @Test
    public void testHashSet() {
        HashSet<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        Object result = ser.read(ser.write(set));
        assertTrue(result instanceof HashSet);
        assertEquals(set, result);
    }

    @Test
    public void testLinkedHashSet() {
        LinkedHashSet<String> set = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        Object result = ser.read(ser.write(set));
        assertTrue(result instanceof LinkedHashSet);
        assertEquals(set, result);
    }

    @Test
    public void testTreeSet() {
        TreeSet<Integer> set = new TreeSet<>(Arrays.asList(5, 3, 1, 4, 2));
        Object result = ser.read(ser.write(set));
        assertTrue(result instanceof TreeSet);
        assertEquals(set, result);
    }

    // ==================== Maps ====================

    @Test
    public void testHashMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        Object result = ser.read(ser.write(map));
        assertTrue(result instanceof HashMap);
        assertEquals(map, result);
    }

    @Test
    public void testLinkedHashMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        Object result = ser.read(ser.write(map));
        assertTrue(result instanceof LinkedHashMap);
        // LinkedHashMap preserves order
        assertArrayEquals(map.keySet().toArray(),
                ((LinkedHashMap<?, ?>) result).keySet().toArray());
        assertEquals(map, result);
    }

    @Test
    public void testTreeMap() {
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(3, "three");
        map.put(1, "one");
        map.put(2, "two");
        Object result = ser.read(ser.write(map));
        assertTrue(result instanceof TreeMap);
        assertEquals(map, result);
    }

    @Test
    public void testEmptyMap() {
        HashMap<String, Integer> empty = new HashMap<>();
        Object result = ser.read(ser.write(empty));
        assertTrue(result instanceof HashMap);
        assertEquals(0, ((Map<?, ?>) result).size());
    }

    // ==================== Date / Time ====================

    @Test
    public void testDate() {
        Date now = new Date();
        Object result = ser.read(ser.write(now));
        assertEquals(now, result);
    }

    @Test
    public void testLocalDate() {
        LocalDate d = LocalDate.of(2026, 9, 6);
        Object result = ser.read(ser.write(d));
        assertEquals(d, result);
    }

    @Test
    public void testLocalTime() {
        LocalTime t = LocalTime.of(12, 34, 56, 789);
        Object result = ser.read(ser.write(t));
        assertEquals(t, result);
    }

    @Test
    public void testLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2026, 9, 6, 12, 34, 56, 789);
        Object result = ser.read(ser.write(ldt));
        assertEquals(ldt, result);
    }

    @Test
    public void testInstant() {
        Instant in = Instant.ofEpochSecond(1234567890L, 999999999);
        Object result = ser.read(ser.write(in));
        assertEquals(in, result);
    }

    @Test
    public void testUuid() {
        UUID u = UUID.fromString("12345678-1234-1234-1234-123456789012");
        Object result = ser.read(ser.write(u));
        assertEquals(u, result);
    }

    // ==================== BigDecimal / BigInteger ====================

    @Test
    public void testBigDecimal() {
        BigDecimal bd = new BigDecimal("12345.6789");
        Object result = ser.read(ser.write(bd));
        assertEquals(bd, result);
    }

    @Test
    public void testBigInteger() {
        BigInteger bi = new BigInteger("12345678901234567890");
        Object result = ser.read(ser.write(bi));
        assertEquals(bi, result);
    }

    // ==================== Class / Enum / Optional ====================

    @Test
    public void testClass() {
        Object result = ser.read(ser.write(String.class));
        assertEquals(String.class, result);
    }

    @Test
    public void testEnum() {
        TestEnum v = TestEnum.B;
        Object result = ser.read(ser.write(v));
        assertEquals(v, result);
    }

    @Test
    public void testOptionalPresent() {
        Optional<String> opt = Optional.of("hello");
        Object result = ser.read(ser.write(opt));
        assertEquals(opt, result);
    }

    @Test
    public void testOptionalEmpty() {
        Optional<String> opt = Optional.empty();
        Object result = ser.read(ser.write(opt));
        assertEquals(opt, result);
    }

    // ==================== Nested / Complex ====================

    @Test
    public void testNestedListOfMaps() {
        List<Map<String, Object>> complex = new ArrayList<>();
        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", 1);
        m1.put("name", "Alice");
        m1.put("tags", Arrays.asList("admin", "user"));
        complex.add(m1);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", 2);
        m2.put("name", "Bob");
        m2.put("tags", new ArrayList<>());
        complex.add(m2);

        Object result = ser.read(ser.write(complex));
        assertTrue(result instanceof List);
        assertEquals(complex, result);
    }

    @Test
    public void testDeepNested() {
        Map<String, Object> level4 = new HashMap<>();
        level4.put("d", "deep");
        Map<String, Object> level3 = new HashMap<>();
        level3.put("c", level4);
        Map<String, Object> level2 = new HashMap<>();
        level2.put("b", level3);
        Map<String, Object> level1 = new HashMap<>();
        level1.put("a", level2);

        Object result = ser.read(ser.write(level1));
        assertEquals(level1, result);
    }

    @Test
    public void testArrayListOfStrings() {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Object result = ser.read(ser.write(list));
        assertEquals(list, result);
    }

    @Test
    public void testMapWithArrayValues() {
        Map<String, int[]> map = new HashMap<>();
        map.put("a", new int[]{1, 2, 3});
        map.put("b", new int[]{4, 5});
        Object result = ser.read(ser.write(map));
        assertTrue(result instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertArrayEquals(new int[]{1, 2, 3}, (int[]) resultMap.get("a"));
        assertArrayEquals(new int[]{4, 5}, (int[]) resultMap.get("b"));
    }

    // ==================== Custom SPI ====================

    @Test
    public void testCustomSerializerRegistration() {
        ZSerializer custom = new ZSerializer();
        custom.register(new PointSerializer());
        Point original = new Point(3, 7);
        Object result = custom.read(custom.write(original));
        assertEquals(original, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testCustomSerializerConflictThrows() {
        ZSerializer custom = new ZSerializer();
        custom.register(new PointSerializer());
        custom.register(new AnotherPointSerializer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterBuiltinTypeIdRejected() {
        ZSerializer custom = new ZSerializer();
        Serializer<Byte> bad = new Serializer<Byte>() {
            public byte typeId() { return TypeId.INT; }
            public Class<Byte> type() { return Byte.class; }
            public void write(ByteWriter out, Byte v) {}
            public Byte read(ByteReader in) { return null; }
        };
        custom.register(bad);
    }

    @Test
    public void testUnsupportedTypeThrows() {
        try {
            ser.write(new Object() {});
            fail("Expected SerializationException");
        } catch (SerializationException e) {
            // 期望
        }
    }

    // ==================== Size 优化验证 ====================

    @Test
    public void testSmallIntTakesOneByte() {
        byte[] data = ser.write(Integer.valueOf(1));
        assertEquals(2, data.length);  // type(1) + varint(1)
    }

    @Test
    public void testEmptyArrayTakesMinimalSpace() {
        byte[] data = ser.write(new int[0]);
        assertEquals(2, data.length);  // type + varint(0)
    }

    @Test
    public void testEmptyStringTakesTwoBytes() {
        byte[] data = ser.write("");
        assertEquals(2, data.length);  // type + varint(0)
    }

    enum TestEnum {
        A, B, C
    }

    public static class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }

    public static class PointSerializer implements Serializer<Point> {
        public byte typeId() { return (byte) 0x80; }
        public Class<Point> type() { return Point.class; }
        public void write(ByteWriter out, Point v) {
            out.writeInt(v.x);
            out.writeInt(v.y);
        }
        public Point read(ByteReader in) {
            return new Point(in.readInt(), in.readInt());
        }
    }

    public static class AnotherPointSerializer implements Serializer<Point> {
        public byte typeId() { return (byte) 0x80; }
        public Class<Point> type() { return Point.class; }
        public void write(ByteWriter out, Point v) {}
        public Point read(ByteReader in) { return null; }
    }
}

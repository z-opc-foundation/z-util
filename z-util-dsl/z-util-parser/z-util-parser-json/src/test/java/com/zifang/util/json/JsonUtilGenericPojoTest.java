package com.zifang.util.json;

import com.zifang.util.json.define.TypeReference;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link JsonUtil} 反序列化泛型 POJO（非 List/Map 的包装类型）。
 *
 * <p>背景：{@code Pageable<ZConfigDTO>} 这类 {@code Wrapper<T>} 之前根本反序列化不了。
 * {@code convertValue} 里 List/Map 的 {@code ParameterizedType} 有专门分支，
 * 其余 {@code ParameterizedType} 落到 {@code deserializePojo((Class<T>) targetType)} ——
 * unchecked 转换不改变运行时对象，传进去的仍是 {@code ParameterizedTypeImpl}，
 * 紧接着 {@code clazz.getDeclaredConstructor()} 就抛：</p>
 * <pre>ClassCastException: class sun.reflect...ParameterizedTypeImpl cannot be cast to class java.lang.Class</pre>
 *
 * <p>修法是把泛型实参绑到字段声明的类型变量上（{@code List<T>} → {@code List<Row>}），
 * 元素类型才不会退化成裸 Object / LinkedHashMap。</p>
 */
public class JsonUtilGenericPojoTest {

    /** 模拟 Pageable<T>：一个带 List<T> 字段的泛型包装类。 */
    public static class Wrapper<T> {
        private List<T> records;
        private long total;
        private long size;
        private long current;

        public List<T> getRecords() { return records; }
        public long getTotal() { return total; }
        public long getSize() { return size; }
        public long getCurrent() { return current; }
    }

    /** 嵌套一层泛型，验证替换是递归的而非只看一层。 */
    public static class Box<T> {
        private Wrapper<T> inner;
        public Wrapper<T> getInner() { return inner; }
    }

    public static class Row {
        private Long id;
        private String dataId;
        public Long getId() { return id; }
        public String getDataId() { return dataId; }
    }

    private static final String JSON = "{\"records\":[{\"id\":1,\"dataId\":\"a.yaml\"},"
            + "{\"id\":2,\"dataId\":\"b.yaml\"}],\"total\":10,\"size\":20,\"current\":1}";

    @Test
    public void testGenericPojoKeepsElementType() {
        Wrapper<Row> w = JsonUtil.fromJson(JSON, new TypeReference<Wrapper<Row>>() {});
        assertNotNull(w);
        assertEquals(10L, w.getTotal());
        assertEquals(20L, w.getSize());
        assertEquals(1L, w.getCurrent());
        assertEquals(2, w.getRecords().size());
    }

    /**
     * 钉"元素被还原成 Row 而不是 LinkedHashMap"——这是本修复的实质。
     * 修复前这里会得到 LinkedHashMap，取 id 要强转，故用反射读字段而不是调 getId()。
     */
    @Test
    public void testGenericPojoElementIsRealClassNotMap() {
        Wrapper<Row> w = JsonUtil.fromJson(JSON, new TypeReference<Wrapper<Row>>() {});
        Object first = w.getRecords().get(0);
        assertEquals("元素应被还原为 Row，而不是裸 Map", Row.class, first.getClass());
        assertEquals(Long.valueOf(1L), ((Row) first).getId());
        assertEquals("a.yaml", ((Row) first).getDataId());
    }

    @Test
    public void testNestedGenericPojo() {
        Box<Row> box = JsonUtil.fromJson("{\"inner\":" + JSON + "}", new TypeReference<Box<Row>>() {});
        assertNotNull(box.getInner());
        assertEquals(10L, box.getInner().getTotal());
        assertEquals(Row.class, box.getInner().getRecords().get(0).getClass());
        assertEquals("b.yaml", ((Row) box.getInner().getRecords().get(1)).getDataId());
    }

    /** 无泛型信息的场景不受影响：非泛型 POJO 照常反序列化。 */
    @Test
    public void testPlainPojoStillWorks() {
        Row row = JsonUtil.fromJson("{\"id\":7,\"dataId\":\"c.yaml\"}", Row.class);
        assertEquals(Long.valueOf(7L), row.getId());
        assertEquals("c.yaml", row.getDataId());
    }

    /** List<T> 这条原本就支持的路径不能被改坏。 */
    @Test
    public void testGenericListStillWorks() {
        List<Row> rows = JsonUtil.fromJson("[{\"id\":1,\"dataId\":\"a.yaml\"}]",
                new TypeReference<List<Row>>() {});
        assertEquals(1, rows.size());
        assertEquals(Row.class, rows.get(0).getClass());
    }
}

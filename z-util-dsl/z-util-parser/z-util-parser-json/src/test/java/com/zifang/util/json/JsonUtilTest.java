package com.zifang.util.json;

import com.zifang.util.json.define.TypeReference;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * JsonUtilTest类。
 */
public class JsonUtilTest {

    // ==================== toJson 基本类型 ====================

    @Test
    /**
     * testToJsonNull方法。
     */
    public void testToJsonNull() {
        assertEquals("null", JsonUtil.toJson(null));
    }

    @Test
    /**
     * testToJsonString方法。
     */
    public void testToJsonString() {
        assertEquals("\"hello\"", JsonUtil.toJson("hello"));
    }

    @Test
    /**
     * testToJsonStringWithQuotes方法。
     */
    public void testToJsonStringWithQuotes() {
        assertEquals("\"hello\\\"world\"", JsonUtil.toJson("hello\"world"));
    }

    @Test
    /**
     * testToJsonStringWithNewline方法。
     */
    public void testToJsonStringWithNewline() {
        assertEquals("\"hello\\nworld\"", JsonUtil.toJson("hello\nworld"));
    }

    @Test
    /**
     * testToJsonInteger方法。
     */
    public void testToJsonInteger() {
        assertEquals("42", JsonUtil.toJson(42));
    }

    @Test
    /**
     * testToJsonLong方法。
     */
    public void testToJsonLong() {
        assertEquals("42", JsonUtil.toJson(42L));
    }

    @Test
    /**
     * testToJsonDouble方法。
     */
    public void testToJsonDouble() {
        assertEquals("3.14", JsonUtil.toJson(3.14));
    }

    @Test
    /**
     * testToJsonBoolean方法。
     */
    public void testToJsonBoolean() {
        assertEquals("true", JsonUtil.toJson(true));
        assertEquals("false", JsonUtil.toJson(false));
    }

    // ==================== toJson 集合 ====================

    @Test
    /**
     * testToJsonEmptyList方法。
     */
    public void testToJsonEmptyList() {
        assertEquals("[]", JsonUtil.toJson(new ArrayList<>()));
    }

    @Test
    /**
     * testToJsonList方法。
     */
    public void testToJsonList() {
        List<Object> list = Arrays.asList(1, "two", true);
        String json = JsonUtil.toJson(list);
        assertTrue(json.contains("1"));
        assertTrue(json.contains("\"two\""));
        assertTrue(json.contains("true"));
    }

    @Test
    /**
     * testToJsonEmptyMap方法。
     */
    public void testToJsonEmptyMap() {
        assertEquals("{}", JsonUtil.toJson(new LinkedHashMap<>()));
    }

    @Test
    /**
     * testToJsonMap方法。
     */
    public void testToJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "test");
        map.put("age", 18);
        String json = JsonUtil.toJson(map);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"test\""));
        assertTrue(json.contains("\"age\""));
        assertTrue(json.contains("18"));
    }

    // ==================== toJson POJO ====================

    @Test
    /**
     * testToJsonPojo方法。
     */
    public void testToJsonPojo() {
        Person p = new Person("Tom", 20);
        String json = JsonUtil.toJson(p);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"Tom\""));
        assertTrue(json.contains("\"age\""));
        assertTrue(json.contains("20"));
    }

    @Test
    /**
     * testToJsonPojoWithNull方法。
     */
    public void testToJsonPojoWithNull() {
        Person p = new Person(null, 0);
        String json = JsonUtil.toJson(p);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("null"));
    }

    @Test
    /**
     * testParseObject方法。
     */
    public void testParseObject() {
        JsonObject obj = JsonUtil.parseObject("{\"name\":\"test\",\"age\":18}");
        assertEquals("test", obj.get("name"));
        assertEquals(18L, ((Number) obj.get("age")).longValue());
    }

    // ==================== parseObject / parseArray ====================

    @Test
    /**
     * testParseArray方法。
     */
    public void testParseArray() {
        JsonArray arr = JsonUtil.parseArray("[1,2,3]");
        assertEquals(3, arr.size());
        assertEquals(1L, ((Number) arr.get(0)).longValue());
    }

    @Test
    /**
     * testParseEmptyObject方法。
     */
    public void testParseEmptyObject() {
        JsonObject obj = JsonUtil.parseObject("{}");
        assertEquals(0, obj.size());
    }

    @Test
    /**
     * testParseEmptyArray方法。
     */
    public void testParseEmptyArray() {
        JsonArray arr = JsonUtil.parseArray("[]");
        assertEquals(0, arr.size());
    }

    @Test
    /**
     * testFromJsonString方法。
     */
    public void testFromJsonString() {
        String result = JsonUtil.fromJson("\"hello\"", String.class);
        assertEquals("hello", result);
    }

    // ==================== fromJson 基本类型 ====================

    @Test
    /**
     * testFromJsonInt方法。
     */
    public void testFromJsonInt() {
        Integer result = JsonUtil.fromJson("42", Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    /**
     * testFromJsonLong方法。
     */
    public void testFromJsonLong() {
        Long result = JsonUtil.fromJson("42", Long.class);
        assertEquals(Long.valueOf(42), result);
    }

    @Test
    /**
     * testFromJsonDouble方法。
     */
    public void testFromJsonDouble() {
        Double result = JsonUtil.fromJson("3.14", Double.class);
        assertEquals(3.14, result, 0.001);
    }

    @Test
    /**
     * testFromJsonBoolean方法。
     */
    public void testFromJsonBoolean() {
        Boolean result = JsonUtil.fromJson("true", Boolean.class);
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    /**
     * testFromJsonNull方法。
     */
    public void testFromJsonNull() {
        String result = JsonUtil.fromJson("null", String.class);
        assertNull(result);
    }

    @Test
    /**
     * testFromJsonListTypeRef方法。
     */
    public void testFromJsonListTypeRef() {
        String json = "[1,2,3]";
        List<Integer> result = JsonUtil.fromJson(json, new TypeReference<List<Integer>>() {
        });
        assertEquals(3, result.size());
        assertEquals(1, (int) result.get(0));
        assertEquals(2, (int) result.get(1));
        assertEquals(3, (int) result.get(2));
    }

    // ==================== fromJson 泛型 ====================

    @Test
    /**
     * testFromJsonListOfObjects方法。
     */
    public void testFromJsonListOfObjects() {
        String json = "[{\"name\":\"Tom\"},{\"name\":\"Jerry\"}]";
        List<Person> result = JsonUtil.fromJson(json, new TypeReference<List<Person>>() {
        });
        assertEquals(2, result.size());
        assertEquals("Tom", result.get(0).name);
        assertEquals("Jerry", result.get(1).name);
    }

    @Test
    /**
     * testFromJsonMapTypeRef方法。
     */
    public void testFromJsonMapTypeRef() {
        String json = "{\"name\":\"test\",\"age\":18}";
        Map<String, Object> result = JsonUtil.fromJson(json, new TypeReference<Map<String, Object>>() {
        });
        assertEquals("test", result.get("name"));
        assertEquals(18L, ((Number) result.get("age")).longValue());
    }

    @Test
    /**
     * testFromJsonPojo方法。
     */
    public void testFromJsonPojo() {
        String json = "{\"name\":\"Tom\",\"age\":20}";
        Person p = JsonUtil.fromJson(json, Person.class);
        assertEquals("Tom", p.name);
        assertEquals(20, p.age);
    }

    // ==================== fromJson POJO ====================

    @Test
    /**
     * testRoundtripPojo方法。
     */
    public void testRoundtripPojo() {
        Person original = new Person("Alice", 30);
        String json = JsonUtil.toJson(original);
        Person restored = JsonUtil.fromJson(json, Person.class);
        assertEquals("Alice", restored.name);
        assertEquals(30, restored.age);
    }

    // ==================== roundtrip ====================

    @Test
    /**
     * testRoundtripNestedObject方法。
     */
    public void testRoundtripNestedObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("person", new Person("Bob", 25));
        map.put("active", true);
        String json = JsonUtil.toJson(map);
        JsonObject obj = JsonUtil.parseObject(json);
        JsonObject person = (JsonObject) obj.get("person");
        assertEquals("Bob", person.get("name"));
    }

    @Test
    /**
     * testToJsonEmptyPojo方法。
     */
    public void testToJsonEmptyPojo() {
        String json = JsonUtil.toJson(new Person());
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"age\""));
    }

    // ==================== 边界情况 ====================

    @Test
    /**
     * testFromJsonEmptyJsonObject方法。
     */
    public void testFromJsonEmptyJsonObject() {
        String json = "{}";
        Person p = JsonUtil.fromJson(json, Person.class);
        assertNotNull(p);
    }

    @Test
    /**
     * testUnicodeChars方法。
     */
    public void testUnicodeChars() {
        String json = JsonUtil.toJson("中文测试");
        assertEquals("\"中文测试\"", json);
    }

    @Test
    /**
     * testSpecialCharsInString方法。
     */
    public void testSpecialCharsInString() {
        String json = JsonUtil.toJson("a\tb\rc\nd");
        assertTrue(json.contains("\\t"));
        assertTrue(json.contains("\\r"));
        assertTrue(json.contains("\\n"));
    }

    // ==================== 字符串级便捷操作 ====================

    @Test
    /**
     * testGetString方法。
     */
    public void testGetString() {
        String json = "{\"name\":\"tom\",\"age\":18,\"male\":true}";
        assertEquals("tom", JsonUtil.getString(json, "name"));
        // 数字值返回其JSON表示
        assertEquals("18", JsonUtil.getString(json, "age"));
        // 键不存在
        assertNull(JsonUtil.getString(json, "missing"));
        // null/空白时视为空对象
        assertNull(JsonUtil.getString(null, "name"));
        assertNull(JsonUtil.getString("  ", "name"));
    }

    @Test
    /**
     * testGetInteger方法。
     */
    public void testGetInteger() {
        String json = "{\"a\":18,\"b\":\"20\",\"c\":\"12.5\",\"d\":\"abc\"}";
        assertEquals(Integer.valueOf(18), JsonUtil.getInteger(json, "a"));
        assertEquals(Integer.valueOf(20), JsonUtil.getInteger(json, "b"));
        assertEquals(Integer.valueOf(12), JsonUtil.getInteger(json, "c"));
        assertNull(JsonUtil.getInteger(json, "d"));
        assertNull(JsonUtil.getInteger(json, "missing"));
    }

    @Test
    /**
     * testGetLongAndDouble方法。
     */
    public void testGetLongAndDouble() {
        String json = "{\"a\":10000000000,\"b\":\"123\",\"c\":3.14,\"d\":\"2.5\"}";
        assertEquals(Long.valueOf(10000000000L), JsonUtil.getLong(json, "a"));
        assertEquals(Long.valueOf(123L), JsonUtil.getLong(json, "b"));
        assertEquals(Double.valueOf(3.14), JsonUtil.getDouble(json, "c"));
        assertEquals(Double.valueOf(2.5), JsonUtil.getDouble(json, "d"));
        assertNull(JsonUtil.getLong("{\"a\":\"x\"}", "a"));
        assertNull(JsonUtil.getDouble("{\"a\":\"x\"}", "a"));
    }

    @Test
    /**
     * testGetBoolean方法。
     */
    public void testGetBoolean() {
        String json = "{\"a\":true,\"b\":\"false\",\"c\":\"yes\"}";
        assertEquals(Boolean.TRUE, JsonUtil.getBoolean(json, "a"));
        assertEquals(Boolean.FALSE, JsonUtil.getBoolean(json, "b"));
        assertNull(JsonUtil.getBoolean(json, "c"));
        assertNull(JsonUtil.getBoolean(json, "missing"));
    }

    @Test
    /**
     * testGetBooleanWithDefault方法。
     */
    public void testGetBooleanWithDefault() {
        String json = "{\"a\":true,\"c\":\"yes\"}";
        // 正常解析时返回解析值
        assertEquals(Boolean.TRUE, JsonUtil.getBoolean(json, "a", Boolean.FALSE));
        // 键不存在时返回默认值
        assertEquals(Boolean.FALSE, JsonUtil.getBoolean(json, "missing", Boolean.FALSE));
        // 值无法解析时返回默认值
        assertEquals(Boolean.TRUE, JsonUtil.getBoolean(json, "c", Boolean.TRUE));
        // json为null时返回默认值
        assertEquals(Boolean.TRUE, JsonUtil.getBoolean(null, "a", Boolean.TRUE));
    }

    @Test
    /**
     * testGetStringWithDefault方法。
     */
    public void testGetStringWithDefault() {
        String json = "{\"name\":\"tom\",\"age\":18}";
        // 正常解析时返回解析值
        assertEquals("tom", JsonUtil.getString(json, "name", "default"));
        // 非字符串值返回其JSON表示
        assertEquals("18", JsonUtil.getString(json, "age", "default"));
        // 键不存在时返回默认值
        assertEquals("default", JsonUtil.getString(json, "missing", "default"));
        // json为null时返回默认值
        assertEquals("default", JsonUtil.getString(null, "name", "default"));
        // json为空白时返回默认值
        assertEquals("default", JsonUtil.getString("  ", "name", "default"));
    }

    @Test
    /**
     * testGetIntegerWithDefault方法。
     */
    public void testGetIntegerWithDefault() {
        String json = "{\"a\":18,\"b\":\"20\",\"c\":\"xyz\"}";
        // 正常解析时返回解析值
        assertEquals(Integer.valueOf(18), JsonUtil.getInteger(json, "a", 0));
        // 数字字符串可解析
        assertEquals(Integer.valueOf(20), JsonUtil.getInteger(json, "b", 0));
        // 值无法解析时返回默认值
        assertEquals(Integer.valueOf(0), JsonUtil.getInteger(json, "c", 0));
        // 键不存在时返回默认值
        assertEquals(Integer.valueOf(0), JsonUtil.getInteger(json, "missing", 0));
        // json为null时返回默认值
        assertEquals(Integer.valueOf(0), JsonUtil.getInteger(null, "a", 0));
    }

    @Test
    /**
     * testParseToMap方法。JSON对象字符串安全解析为Map。
     */
    public void testParseToMap() {
        // 正常对象解析
        Map<String, Object> map = JsonUtil.parseToMap("{\"name\":\"tom\",\"age\":18}");
        assertEquals(2, map.size());
        assertEquals("tom", map.get("name"));
        assertEquals(Integer.valueOf(18), map.get("age"));
        // json为null/空白返回空Map
        assertTrue(JsonUtil.parseToMap(null).isEmpty());
        assertTrue(JsonUtil.parseToMap("  ").isEmpty());
        // 解析失败返回空Map
        assertTrue(JsonUtil.parseToMap("xx").isEmpty());
        // 非对象结构返回空Map
        assertTrue(JsonUtil.parseToMap("[1,2]").isEmpty());
        // 字面量null返回空Map
        assertTrue(JsonUtil.parseToMap("null").isEmpty());
        // 空对象返回空Map
        assertTrue(JsonUtil.parseToMap("{}").isEmpty());
    }

    @Test
    /**
     * testGetObjectAndList方法。
     */
    public void testGetObjectAndList() {
        String json = "{\"person\":{\"name\":\"tom\",\"age\":18},\"tags\":[\"a\",\"b\"]}";
        Person person = JsonUtil.getObject(json, "person", Person.class);
        assertNotNull(person);
        assertEquals("tom", person.name);
        assertEquals(18, person.age);

        List<String> tags = JsonUtil.getList(json, "tags", String.class);
        assertEquals(Arrays.asList("a", "b"), tags);

        // 非数组值返回null
        assertNull(JsonUtil.getList(json, "person", Person.class));
        assertNull(JsonUtil.getObject(json, "missing", Person.class));
    }

    @Test
    /**
     * testUpdateJson方法。
     */
    public void testUpdateJson() {
        String json = "{\"a\":1,\"b\":\"x\"}";
        // 覆盖已有键（键顺序不保证，语义断言）
        String updated = JsonUtil.updateJson(json, "a", 2);
        assertEquals(Integer.valueOf(2), JsonUtil.getInteger(updated, "a"));
        assertEquals("x", JsonUtil.getString(updated, "b"));
        // 新增键
        updated = JsonUtil.updateJson(json, "c", true);
        assertEquals(Integer.valueOf(1), JsonUtil.getInteger(updated, "a"));
        assertEquals("x", JsonUtil.getString(updated, "b"));
        assertEquals(Boolean.TRUE, JsonUtil.getBoolean(updated, "c"));
        // 空JSON视为空对象
        assertEquals("{\"a\":1}", JsonUtil.updateJson(null, "a", 1));
        // 写入POJO
        updated = JsonUtil.updateJson("{}", "p", new Person("tom", 18));
        Person p = JsonUtil.getObject(updated, "p", Person.class);
        assertEquals("tom", p.name);
        assertEquals(18, p.age);
        // 写入集合
        assertEquals("{\"list\":[1,2]}", JsonUtil.updateJson("{}", "list", Arrays.asList(1, 2)));
    }

    @Test
    /**
     * testCombineJsonString方法。
     */
    public void testCombineJsonString() {
        String a = "{\"a\":1,\"b\":\"x\"}";
        String b = "{\"b\":\"y\",\"c\":3}";
        // 后者覆盖前者同名键（键顺序不保证，语义断言）
        String merged = JsonUtil.combineJsonString(a, b);
        assertEquals(Integer.valueOf(1), JsonUtil.getInteger(merged, "a"));
        assertEquals("y", JsonUtil.getString(merged, "b"));
        assertEquals(Integer.valueOf(3), JsonUtil.getInteger(merged, "c"));
        // 任一为空直接返回另一个
        assertEquals(b, JsonUtil.combineJsonString(null, b));
        assertEquals(a, JsonUtil.combineJsonString(a, null));
        assertEquals(a, JsonUtil.combineJsonString(a, "  "));
    }

    @Test
    /**
     * testParseQuietly方法。
     */
    public void testParseQuietly() {
        // 合法输入正常返回
        JsonObject obj = JsonUtil.parseObjectQuietly("{\"a\":1}");
        assertNotNull(obj);
        assertEquals(Integer.valueOf(1), JsonUtil.getInteger("{\"a\":1}", "a"));
        // 非法格式返回null而不抛异常
        assertNull(JsonUtil.parseObjectQuietly("{invalid}"));
        assertNull(JsonUtil.parseArrayQuietly("[1,2"));
        // null/空白保持原有宽容行为
        assertEquals(0, JsonUtil.parseObjectQuietly(null).size());
        assertEquals(0, JsonUtil.parseArrayQuietly(" ").size());
    }

    @Test
    /**
     * testFromJsonQuietly方法。
     */
    public void testFromJsonQuietly() {
        // 合法输入正常返回
        Person p = JsonUtil.fromJsonQuietly("{\"name\":\"tom\",\"age\":18}", Person.class);
        assertNotNull(p);
        assertEquals("tom", p.name);
        // 非法格式返回null而不抛异常
        assertNull(JsonUtil.fromJsonQuietly("not a json", Person.class));
        // null/空白返回null
        assertNull(JsonUtil.fromJsonQuietly(null, Person.class));
        // 序列化宽容
        assertEquals("\"中文\"", JsonUtil.toJsonQuietly("中文"));
    }

    @Test
    /**
     * testFromMap方法。
     */
    public void testFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "tom");
        map.put("age", 18);
        Person p = JsonUtil.fromMap(map, Person.class);
        assertNotNull(p);
        assertEquals("tom", p.name);
        assertEquals(18, p.age);
        // null返回null
        assertNull(JsonUtil.fromMap(null, Person.class));
    }

    @Test
    /**
     * testToJsonWithLongAsString方法。
     */
    public void testToJsonWithLongAsString() {
        // 顶层Long转字符串
        assertEquals("\"123\"", JsonUtil.toJsonWithLongAsString(123L));
        // 非Long类型不受影响
        assertEquals("45", JsonUtil.toJsonWithLongAsString(45));
        assertEquals("\"abc\"", JsonUtil.toJsonWithLongAsString("abc"));
        // Map嵌套中的Long
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", 123456789012345L);
        map.put("name", "tom");
        String json = JsonUtil.toJsonWithLongAsString(map);
        // Long序列化为带引号的字符串
        assertTrue(json.contains("\"id\":\"123456789012345\""));
        assertEquals("tom", JsonUtil.getString(json, "name"));
        // POJO字段中的Long
        LongHolder holder = new LongHolder();
        holder.id = 9876543210L;
        holder.name = "x";
        holder.count = 5;
        String pojoJson = JsonUtil.toJsonWithLongAsString(holder);
        assertTrue(pojoJson.contains("\"id\":\"9876543210\""));
        assertEquals("x", JsonUtil.getString(pojoJson, "name"));
        assertEquals(Integer.valueOf(5), JsonUtil.getInteger(pojoJson, "count"));
        // 开关用完即复位，普通序列化不受影响
        assertEquals("123", JsonUtil.toJson(123L));
        // null入参
        assertEquals("null", JsonUtil.toJsonWithLongAsString(null));
    }

    @Test
    /**
     * testRemoveJson方法。
     */
    public void testRemoveJson() {
        // 移除已有键
        assertEquals("{\"b\":2}", JsonUtil.removeJson("{\"a\":1,\"b\":2}", "a"));
        // 键不存在时内容不变
        assertEquals("{\"a\":1}", JsonUtil.removeJson("{\"a\":1}", "z"));
        // null/空白视为空对象
        assertEquals("{}", JsonUtil.removeJson(null, "a"));
        // 嵌套对象整体作为顶层键被移除
        assertEquals("{\"b\":1}", JsonUtil.removeJson("{\"a\":{\"x\":1},\"b\":1}", "a"));
    }

    @Test
    /**
     * testGetUndeclaredFields方法。
     */
    public void testGetUndeclaredFields() {
        // 未声明的字段进入结果
        Map<String, Object> extra = JsonUtil.getUndeclaredFields(
                "{\"name\":\"tom\",\"age\":18,\"extra\":\"x\",\"flag\":true}", Person.class);
        assertEquals(2, extra.size());
        assertEquals("x", extra.get("extra"));
        assertEquals(Boolean.TRUE, extra.get("flag"));
        // 全部已声明时返回空Map
        assertTrue(JsonUtil.getUndeclaredFields(
                "{\"name\":\"tom\",\"age\":18}", Person.class).isEmpty());
        // null入参返回空Map
        assertTrue(JsonUtil.getUndeclaredFields(null, Person.class).isEmpty());
        assertTrue(JsonUtil.getUndeclaredFields("{\"a\":1}", null).isEmpty());
        // 非法json返回空Map
        assertTrue(JsonUtil.getUndeclaredFields("not-json", Person.class).isEmpty());
    }

    /**
     * Long字段测试用POJO。
     */
    public static class LongHolder {
        public Long id;
        public String name;
        public int count;
    }

    public static class Person {
        public String name;
        public int age;

        /**
         * Person方法。
         */
        public Person() {
        }

        /**
         * Person方法。
         * * @param name String类型参数
         *
         * @param age int类型参数
         */
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
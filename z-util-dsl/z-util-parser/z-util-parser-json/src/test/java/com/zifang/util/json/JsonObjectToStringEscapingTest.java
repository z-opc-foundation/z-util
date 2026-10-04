package com.zifang.util.json;

import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link JsonObject} / {@link JsonArray} 的 {@code toString()} 必须产出**可解析的** JSON。
 *
 * <p><b>缺陷</b>：两者原本都委托 {@code BeautifyJsonUtils.beautify}，而后者把字符串
 * value（以及 key）直接 {@code sb.append(value)} 拼进引号里，不做任何转义。
 * 实测（JDK 17，parser-json target/classes）：
 * {@code JsonUtil.toJson(new JsonObject with "\n")} 产出带裸 0x0A 的文本，
 * {@code JsonUtil.parseObject} 直接抛 {@code JsonParseException}。</p>
 *
 * <p><b>为什么影响面这么大</b>：{@code JsonUtil.toJson} 对 JsonObject/JsonArray 是
 * {@code return t.toString()} 短路——也就是说<b>任何用 JsonObject 拼结果再交给
 * toJson 的代码，只要值里含 \n / \t / " / \，产出的都是非法 JSON</b>，
 * 而 Map 走 {@code solveMap → escapeString} 却是好的。同一个值、两条路径、两种结果。</p>
 */
public class JsonObjectToStringEscapingTest {

    private static final String MULTILINE = "# 偏好\n- 用 JSON";
    private static final String TRICKY = "a\tb\"c\\d";

    private static void assertParses(String label, String json) {
        assertNotNull(label + " 不该为 null", json);
        try {
            JsonUtil.parseObject(json);
        } catch (Throwable t) {
            throw new AssertionError(label + " 产出的不是合法 JSON: " + json, t);
        }
    }

    /** 数组要用 parseArray；用 parseObject 会因"不是对象"而抛 JsonTypeException，那是形状问题不是转义问题。 */
    private static void assertArrayParses(String label, String json) {
        assertNotNull(label + " 不该为 null", json);
        try {
            JsonUtil.parseArray(json);
        } catch (Throwable t) {
            throw new AssertionError(label + " 产出的不是合法 JSON: " + json, t);
        }
    }

    // ==================== value ====================

    @Test
    public void jsonObjectWithMultilineValueIsValidJson() {
        JsonObject o = new JsonObject();
        o.put("content", MULTILINE);
        assertParses("JsonObject 含换行值", o.toString());
    }

    @Test
    public void jsonObjectWithTabQuoteBackslashIsValidJson() {
        JsonObject o = new JsonObject();
        o.put("k", TRICKY);
        assertParses("JsonObject 含 tab/引号/反斜杠", o.toString());
    }

    @Test
    public void jsonArrayWithMultilineElementIsValidJson() {
        JsonArray a = new JsonArray();
        a.add(MULTILINE);
        a.add(TRICKY);
        assertArrayParses("JsonArray 含换行元素", a.toString());
    }

    /** key 同样要转义 —— key 里出现引号或控制字符一样会破坏 JSON。 */
    @Test
    public void jsonObjectKeyWithQuoteIsValidJson() {
        JsonObject o = new JsonObject();
        o.put("we\"ird\nkey", "v");
        assertParses("JsonObject 的 key 含引号/换行", o.toString());
    }

    /** 嵌套：内层的字符串值同样要走转义。 */
    @Test
    public void nestedJsonObjectIsValidJson() {
        JsonObject inner = new JsonObject();
        inner.put("content", MULTILINE);
        JsonObject outer = new JsonObject();
        outer.put("data", inner);
        assertParses("嵌套 JsonObject", outer.toString());
    }

    // ==================== 与 Map 路径的一致性 ====================

    /**
     * 同一个值，Map 路径（solveMap → escapeString）与 JsonObject 路径（toString）必须等价。
     * 修复前两者给出两种结果——这是本缺陷最容易被忽略的形态。
     */
    @Test
    public void mapPathAndJsonObjectPathAgreeOnEscaping() {
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("content", MULTILINE);
        JsonObject o = new JsonObject();
        o.put("content", MULTILINE);

        String viaMap = JsonUtil.toJson(m);
        String viaObject = JsonUtil.toJson(o);

        assertParses("Map 路径", viaMap);
        assertParses("JsonObject 路径", viaObject);

        // 两者都必须是"换行被转义成两个字符"的形式
        assertTrue("Map 路径不应含裸换行", !viaMap.matches("(?s).*\"content\"\\s*:\\s*\".*\\n.*\".*"));
        assertTrue("JsonObject 路径不应含裸换行",
                !viaObject.matches("(?s).*\"content\"\\s*:\\s*\".*\\n.*\".*"));
    }

    /** 转义后取值必须还原成原字符串——不能只做到"能解析"。 */
    @Test
    public void escapedValueRoundTripsBackToOriginal() {
        JsonObject o = new JsonObject();
        o.put("content", MULTILINE);
        JsonObject back = JsonUtil.parseObject(o.toString());
        assertEquals(MULTILINE, back.getString("content"));

        JsonObject t = new JsonObject();
        t.put("k", TRICKY);
        assertEquals(TRICKY, JsonUtil.parseObject(t.toString()).getString("k"));
    }

    /** JsonArray 同理：解析回来要能取到原值。 */
    @Test
    public void jsonArrayRoundTrips() {
        JsonArray a = new JsonArray();
        a.add(MULTILINE);
        a.add(TRICKY);
        JsonArray back = JsonUtil.parseArray(a.toString());
        assertEquals(2, back.size());
        assertEquals(MULTILINE, back.get(0));
        assertEquals(TRICKY, back.get(1));
    }

    /** 非字符串类型不受影响，防止这次改动误伤。 */
    @Test
    public void nonStringValuesUnaffected() {
        JsonObject o = new JsonObject();
        o.put("n", 42);
        o.put("d", 1.5);
        o.put("b", true);
        o.put("z", null);
        String json = o.toString();
        assertParses("数值/布尔/null", json);
        JsonObject back = JsonUtil.parseObject(json);
        assertEquals(Integer.valueOf(42), back.getInt("n"));
        assertEquals(Boolean.TRUE, back.getBoolean("b"));
    }
}

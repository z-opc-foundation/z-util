package com.zifang.util.core.lang;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * TemplateUtilTest类。
 */
public class TemplateUtilTest {

    /**
     * 测试用普通对象（含继承链）
     */
    static class Base {
        private String barcode = "123456";
    }

    static class Sample extends Base {
        private LocalDateTime reportCheckTime = LocalDateTime.of(2022, 4, 22, 12, 30, 0);
        private Integer count = 7;
    }

    @Test
    /**
     * testRenderMap方法。Map 取值与日期格式化。
     */
    public void testRenderMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("year", LocalDateTime.of(2022, 4, 22, 12, 30, 0));
        map.put("barcode", "123456");
        map.put("count", 7);
        String result = TemplateUtil.render("{year, yyyy}/{barcode}_{year, HHmmss}.pdf", map);
        assertEquals("2022/123456_123000.pdf", result);
        assertEquals("count=7", TemplateUtil.render("count={count}", map));
    }

    @Test
    /**
     * testRenderBean方法。对象反射取值（含继承链字段）与三种日期类型格式化。
     */
    public void testRenderBean() {
        Sample sample = new Sample();
        assertEquals("123456_20220422", TemplateUtil.render("{barcode}_{reportCheckTime, yyyyMMdd}", sample));
        assertEquals("12:30:00", TemplateUtil.render("{reportCheckTime, HH:mm:ss}", sample));
        assertEquals("7", TemplateUtil.render("{count}", sample));
    }

    @Test
    /**
     * testRenderDateTypes方法。Date 与 LocalDate 类型的格式化及无模板时的 toString。
     */
    public void testRenderDateTypes() {
        Map<String, Object> map = new HashMap<>();
        map.put("date", new Date(0));
        map.put("localDate", LocalDate.of(2022, 4, 22));
        assertEquals("19700101", TemplateUtil.render("{date, yyyyMMdd}", map));
        assertEquals("2022-04-22", TemplateUtil.render("{localDate, yyyy-MM-dd}", map));
        // 无格式模板时取 toString
        assertEquals(String.valueOf(LocalDate.of(2022, 4, 22)), TemplateUtil.render("{localDate}", map));
    }

    @Test
    /**
     * testRenderKeepPlaceholder方法。字段不存在或值为 null 时占位符保持原样。
     */
    public void testRenderKeepPlaceholder() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", null);
        assertEquals("name={name}, miss={missing}", TemplateUtil.render("name={name}, miss={missing}", map));
        Sample sample = new Sample();
        assertEquals("x={notExist}", TemplateUtil.render("x={notExist}", sample));
    }

    @Test
    /**
     * testRenderPatternOnNonDate方法。格式模板对非日期值不生效，取 toString。
     */
    public void testRenderPatternOnNonDate() {
        Map<String, Object> map = new HashMap<>();
        map.put("count", 42);
        map.put("text", "abc");
        assertEquals("42", TemplateUtil.render("{count, yyyy}", map));
        assertEquals("abc", TemplateUtil.render("{text, yyyy}", map));
    }

    @Test
    /**
     * testRenderSpecialChars方法。替换值含正则特殊字符与花括号时正确处理。
     */
    public void testRenderSpecialChars() {
        Map<String, Object> map = new HashMap<>();
        map.put("path", "a/b\\c");
        map.put("braces", "x{y}z");
        assertEquals("a/b\\c", TemplateUtil.render("{path}", map));
        assertEquals("x{y}z", TemplateUtil.render("{braces}", map));
    }

    @Test
    /**
     * testRenderNullArgs方法。null 模板或 null 参数的边界处理。
     */
    public void testRenderNullArgs() {
        assertNull(TemplateUtil.render(null, new HashMap<String, Object>()));
        assertEquals("a={b}", TemplateUtil.render("a={b}", null));
        assertEquals("", TemplateUtil.render("", new HashMap<String, Object>()));
        assertEquals("plain", TemplateUtil.render("plain", new HashMap<String, Object>()));
    }

    @Test
    /**
     * testRenderAdjacentPlaceholders方法。相邻占位符依次替换。
     */
    public void testRenderAdjacentPlaceholders() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        assertEquals("12", TemplateUtil.render("{a}{b}", map));
        assertEquals("1-2", TemplateUtil.render("{a}-{b}", map));
    }

    @Test
    /**
     * testRenderCustomDelimiter方法。自定义定界符的占位符替换。
     */
    public void testRenderCustomDelimiter() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "tom");
        map.put("age", 18);

        assertEquals("tom is 18", TemplateUtil.render("${name} is ${age}", map, "${", "}"));
        assertEquals("tom is 18", TemplateUtil.render("$name$ is $age$", map, "$", "$"));
        // key 不存在或值为 null 时占位符保持原样
        assertEquals("tom / ${missing}", TemplateUtil.render("${name} / ${missing}", map, "${", "}"));
    }

    @Test
    /**
     * testRenderCustomDelimiterNullSafety方法。自定义定界符版的空参边界。
     */
    public void testRenderCustomDelimiterNullSafety() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "1");

        assertEquals("a=${b}", TemplateUtil.render("a=${b}", null, "${", "}"));
        assertEquals("a=${b}", TemplateUtil.render("a=${b}", new HashMap<String, Object>(), "${", "}"));
        // prefix/suffix 为 null 时回落到默认花括号
        assertEquals("1", TemplateUtil.render("{a}", map, null, null));
    }

    @Test
    /**
     * testRenderByOrder方法。占位符按出现顺序依次替换为参数。
     */
    public void testRenderByOrder() {
        assertEquals("x + y = done", TemplateUtil.renderByOrder("{0} + {1} = {2}", "{", "}", "x", "y", "done"));
        assertEquals("2024-report", TemplateUtil.renderByOrder("#year#-report", "#", "#", 2024));
        // 替换后的内容不再参与后续匹配
        assertEquals("1{b}", TemplateUtil.renderByOrder("{a}{b}", "{", "}", "1"));
    }

    @Test
    /**
     * testRenderByOrderFewerAndMoreArgs方法。参数不足时剩余占位符保持原样，多余参数被忽略。
     */
    public void testRenderByOrderFewerAndMoreArgs() {
        assertEquals("first {second}", TemplateUtil.renderByOrder("{first} {second}", "{", "}", "first"));
        assertEquals("only", TemplateUtil.renderByOrder("{a}", "{", "}", "only", "extra1", "extra2"));
    }

    @Test
    /**
     * testRenderByOrderNullSafety方法。按序替换的空参边界。
     */
    public void testRenderByOrderNullSafety() {
        assertNull(TemplateUtil.renderByOrder(null, "{", "}", "x"));
        assertEquals("a={b}", TemplateUtil.renderByOrder("a={b}", "{", "}", (Object[]) null));
        assertEquals("a={b}", TemplateUtil.renderByOrder("a={b}", "{", "}", new Object[0]));
        // 无闭合标记时原样返回
        assertEquals("a {unclosed", TemplateUtil.renderByOrder("a {unclosed", "{", "}", "x"));
    }
}

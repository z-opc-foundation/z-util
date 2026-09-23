package com.zifang.util.db.query;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 命名参数模板：占位识别、字面量/注释跳过与缺参报错。
 */
public class SqlTemplateTest {

    @Test
    public void bindsInOrderOfAppearance() {
        SqlTemplate template = SqlTemplate.of(
                "SELECT * FROM t_order WHERE biz_date = ${day} AND amount > ${min} OR status = ${day}");
        assertEquals(Arrays.asList("day", "min", "day"), template.placeholders());
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("day", "2026-09-23");
        params.put("min", 100);
        SqlSpec spec = template.bind(params);
        assertEquals("SELECT * FROM t_order WHERE biz_date = ? AND amount > ? OR status = ?", spec.sql());
        assertEquals(Arrays.<Object>asList("2026-09-23", 100, "2026-09-23"), spec.params());
    }

    @Test
    public void dollarInsideLiteralAndCommentIsNotPlaceholder() {
        SqlTemplate template = SqlTemplate.of("SELECT * FROM t -- ${ignored}\n"
                + "/* ${alsoIgnored} */ WHERE note = '${third}' AND id = ${id}");
        assertEquals(Arrays.asList("id"), template.placeholders());
        SqlSpec spec = template.bind(single("id", 7));
        assertTrue(spec.sql(), spec.sql().contains("'${third}'"));
        assertTrue(spec.sql(), spec.sql().contains("-- ${ignored}"));
        assertEquals(Arrays.<Object>asList(7), spec.params());
    }

    @Test
    public void nullValueIsAllowed() {
        SqlSpec spec = SqlTemplate.of("DELETE FROM t WHERE id = ${id}").bind(single("id", null));
        assertEquals(Collections.singletonList(null), spec.params());
    }

    @Test
    public void missingParamAndBadShapesFail() {
        try {
            SqlTemplate.of("SELECT * FROM t WHERE a = ${a}").bind(single("b", 1));
            fail("缺参数应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("缺少参数: a"));
        }
        try {
            SqlTemplate.of("SELECT * FROM t WHERE a = ${a}").bind(null);
            fail("params 为 null 且模板有占位应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("缺少参数"));
        }
        try {
            SqlTemplate.of("SELECT * FROM t WHERE a = ?").bind(single("a", 1));
            fail("裸 ? 应被拒");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("裸 ?"));
        }
        assertEquals(1, SqlTemplate.of("SELECT * FROM t WHERE a = ?").unboundMarkerCount());
        try {
            SqlTemplate.of("SELECT * FROM t WHERE a = ${}");
            fail("空参数名应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("非法参数名"));
        }
        try {
            SqlTemplate.of("SELECT * FROM t WHERE a = ${unclosed");
            fail("未闭合应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("未闭合"));
        }
        try {
            SqlTemplate.of("  ");
            fail("空模板应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("不能为空"));
        }
    }

    @Test
    public void rawAndParameterizedAreBothExposed() {
        SqlTemplate template = SqlTemplate.of("SELECT 1 FROM t WHERE a = ${a}");
        assertEquals("SELECT 1 FROM t WHERE a = ${a}", template.raw());
        assertEquals("SELECT 1 FROM t WHERE a = ?", template.toParameterized());
        assertEquals(0, template.unboundMarkerCount());
        assertTrue(template.toString().contains("a"));
    }

    private static Map<String, Object> single(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }
}

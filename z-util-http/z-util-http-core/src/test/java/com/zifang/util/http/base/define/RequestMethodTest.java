package com.zifang.util.http.base.define;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * RequestMethodTest类。
 */
public class RequestMethodTest {

    @Test
    /**
     * testEnumValues方法。
     */
    public void testEnumValues() {
        RequestMethod[] methods = RequestMethod.values();
        assertEquals(8, methods.length);
        assertEquals(RequestMethod.GET, RequestMethod.valueOf("GET"));
        assertEquals(RequestMethod.HEAD, RequestMethod.valueOf("HEAD"));
        assertEquals(RequestMethod.POST, RequestMethod.valueOf("POST"));
        assertEquals(RequestMethod.PUT, RequestMethod.valueOf("PUT"));
        assertEquals(RequestMethod.PATCH, RequestMethod.valueOf("PATCH"));
        assertEquals(RequestMethod.DELETE, RequestMethod.valueOf("DELETE"));
        assertEquals(RequestMethod.OPTIONS, RequestMethod.valueOf("OPTIONS"));
        assertEquals(RequestMethod.TRACE, RequestMethod.valueOf("TRACE"));
    }

    @Test
    /**
     * testEnumOrdinal方法。
     */
    public void testEnumOrdinal() {
        // 不写死 ordinal 数字。枚举里插入一个新值（比如补 TRACE）会让后面所有值的
        // ordinal 整体 +1，那样的断言会在一次跟本测试无关的改动后集体变红。
        // 这里断言「声明顺序」本身：顺序真变了必须显式改这一行，而不是悄悄漂移。
        assertArrayEquals(
                new String[]{"GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE"},
                Arrays.stream(RequestMethod.values())
                        .map(Enum::name)
                        .toArray(String[]::new));
    }

    @Test
    /**
     * testEnumToString方法。
     */
    public void testEnumToString() {
        assertEquals("GET", RequestMethod.GET.toString());
        assertEquals("POST", RequestMethod.POST.toString());
    }
}

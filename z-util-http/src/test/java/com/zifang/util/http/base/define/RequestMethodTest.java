package com.zifang.util.http.base.define;

import org.junit.Ignore;
import org.junit.Test;

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

    @Ignore
    @Test
    /**
     * testEnumOrdinal方法。
     */
    public void testEnumOrdinal() {
        assertEquals(0, RequestMethod.GET.ordinal());
        assertEquals(2, RequestMethod.POST.ordinal());
        assertEquals(4, RequestMethod.DELETE.ordinal());
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

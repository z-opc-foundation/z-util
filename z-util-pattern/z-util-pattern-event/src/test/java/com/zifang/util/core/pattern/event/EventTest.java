package com.zifang.util.core.pattern.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 烟雾: Event 的 source/type/timestamp/data 四个字段必须贯通,
 * 缺一不可改 (data 不可空, timestamp 不可为 0).
 */
class EventTest {

    @Test
    @DisplayName("source 与 type 必须可读回, data 缺省给空 Map 而非 null")
    void sourceTypeAndDefaultData() {
        Object src = new Object();
        Event e = new Event(src, "USER_CREATED");
        assertSame(src, e.getSource());
        assertEquals("USER_CREATED", e.getType());
        assertNotNull(e.getData(), "data 不能是 null —— 调用方免去 NPE");
        assertEquals(0, e.getData().size());
    }

    @Test
    @DisplayName("timestamp 必须大于 0 (构造时即锁住当前毫秒)")
    void timestampMonotonic() {
        Event e = new Event("src", "TYP");
        assertEquals(true, e.getTimestamp() > 0L, "timestamp 必须 > 0, 否则下游 dedupe 失效");
    }

    @Test
    @DisplayName("显式传入的 data 必须可读回原引用")
    void explicitDataFieldPassed() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("k", "v");
        Event e = new Event("src", "TYP", data);
        assertSame(data, e.getData(), "data 字段应该是引用透传, 不该拷贝");
        assertEquals("v", e.getData().get("k"));
    }
}
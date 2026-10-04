package com.zifang.util.json;

import com.zifang.util.json.define.TypeReference;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link JsonUtil} 对 {@code java.time}（JSR-310）类型的序列化/反序列化。
 *
 * <p>背景：{@code toJson} 的分派链原本没有 {@code java.time} 分支，这些类型一律掉进
 * {@code solvePojo} 反射其私有字段。两个 JDK 上都实测过，且坏法不同：</p>
 * <ul>
 *   <li>JDK 9+ 抛 {@code InaccessibleObjectException}（module java.base 不 "opens java.time"）——
 *       连带整条含该字段的响应序列化失败；</li>
 *   <li>JDK 8 不抛，但静默输出 JDK 内部结构。{@code ZonedDateTime} 最夸张：整张时区转换表
 *       （数千个数字）被灌进 JSON，一个字段就能把响应体撑到几百 KB。</li>
 * </ul>
 *
 * <p>断言一律用字面量，不用 {@code value.toString()} 推导期望值——那样断言会与实现同源、
 * 永远为真，失去牙齿。</p>
 */
public class JsonUtilJavaTimeTest {

    /** 承载 java.time 字段的 POJO，用于验证"字段嵌套"这条真实路径。 */
    public static class Event {
        private String name = "release";
        private LocalDateTime createdAt = LocalDateTime.of(2026, 10, 4, 12, 30, 5);
        private LocalDate day = LocalDate.of(2026, 10, 4);
        private ZonedDateTime at = ZonedDateTime.of(2026, 10, 4, 12, 30, 5, 0, ZoneId.of("Asia/Shanghai"));

        public String getName() { return name; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDate getDay() { return day; }
        public ZonedDateTime getAt() { return at; }
    }

    // ==================== 序列化 ====================

    @Test
    public void testToJsonLocalDateTime() {
        assertEquals("\"2026-10-04T12:30:05\"",
                JsonUtil.toJson(LocalDateTime.of(2026, 10, 4, 12, 30, 5)));
    }

    @Test
    public void testToJsonLocalDateAndTime() {
        assertEquals("\"2026-10-04\"", JsonUtil.toJson(LocalDate.of(2026, 10, 4)));
        assertEquals("\"12:30:05\"", JsonUtil.toJson(LocalTime.of(12, 30, 5)));
    }

    @Test
    public void testToJsonInstantAndAmount() {
        assertEquals("\"2026-09-21T14:13:20Z\"", JsonUtil.toJson(Instant.ofEpochSecond(1790000000L)));
        assertEquals("\"PT1H30M\"", JsonUtil.toJson(Duration.ofMinutes(90)));
        assertEquals("\"2026-10\"", JsonUtil.toJson(YearMonth.of(2026, 10)));
        assertEquals("\"--10-04\"", JsonUtil.toJson(MonthDay.of(10, 4)));
    }

    /**
     * JDK 8 上 ZonedDateTime 会把整张时区转换表灌进 JSON（实测数千个数字）。
     * 这条断言同时钉住"内容正确"和"长度可控"两个性质。
     */
    @Test
    public void testToJsonZonedDateTimeDoesNotLeakZoneDatabase() {
        String json = JsonUtil.toJson(ZonedDateTime.of(2026, 10, 4, 12, 30, 5, 0, ZoneId.of("Asia/Shanghai")));
        assertEquals("\"2026-10-04T12:30:05+08:00[Asia/Shanghai]\"", json);
        assertTrue("时区转换表泄漏进 JSON，实际长度=" + json.length(), json.length() < 60);
    }

    /** POJO 字段嵌套这条真实路径（z-config 的 ZConfigDTO 就是这个形状）。 */
    @Test
    public void testToJsonPojoWithJavaTimeFields() {
        assertEquals("{\"name\":\"release\",\"createdAt\":\"2026-10-04T12:30:05\","
                        + "\"day\":\"2026-10-04\","
                        + "\"at\":\"2026-10-04T12:30:05+08:00[Asia/Shanghai]\"}",
                JsonUtil.toJson(new Event()));
    }

    @Test
    public void testToJsonMapValueJavaTime() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("day", LocalDate.of(2026, 10, 4));
        assertEquals("{\"day\":\"2026-10-04\"}", JsonUtil.toJson(map));
    }

    @Test
    public void testToJsonListOfJavaTime() {
        assertEquals("[\"2026-10-04\",\"2026-10-05\"]",
                JsonUtil.toJson(Arrays.asList(LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 5))));
    }

    // ==================== 反序列化 ====================

    @Test
    public void testFromJsonRoundTripKeepsValues() {
        Event origin = new Event();
        Event back = JsonUtil.fromJson(JsonUtil.toJson(origin), Event.class);

        assertNotNull(back);
        assertEquals(LocalDateTime.of(2026, 10, 4, 12, 30, 5), back.getCreatedAt());
        assertEquals(LocalDate.of(2026, 10, 4), back.getDay());
        assertEquals(ZoneId.of("Asia/Shanghai"), back.getAt().getZone());
    }

    @Test
    public void testFromJsonGenericListKeepsValues() {
        List<LocalDate> back = JsonUtil.fromJson("[\"2026-10-04\",\"2026-10-05\"]",
                new TypeReference<List<LocalDate>>() {});
        assertEquals(Arrays.asList(LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 5)), back);
    }

    /** 时间戳是 Instant 最常见的传输形态，数字形态要能读回来。 */
    @Test
    public void testFromJsonInstantFromEpochMillis() {
        assertEquals(Instant.ofEpochSecond(1790000000L), JsonUtil.fromJson("1790000000000", Instant.class));
    }

    /** 格式非法时降级为 null，而不是把整次反序列化炸掉。 */
    @Test
    public void testFromJsonInvalidIsoDegradesToNullField() {
        Event back = JsonUtil.fromJson("{\"createdAt\":\"not-a-time\",\"day\":\"2026-10-04\"}", Event.class);
        assertNotNull(back);
        assertNull(back.getCreatedAt());
        assertEquals("同一次反序列化里合法字段应照常填充", LocalDate.of(2026, 10, 4), back.getDay());
    }
}

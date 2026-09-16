package com.zifang.util.core.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson {@link ObjectMapper} 单例工厂。
 * <p>
 * 提供线程安全的全局共享 ObjectMapper，统一以下行为：
 * <ul>
 *   <li>注册 {@link JavaTimeModule}，支持 {@code java.timeLocalDate}/{@code LocalDateTime}/{@code Instant} 序列化</li>
 *   <li>{@code FAIL_ON_UNKNOWN_PROPERTIES = false}，反序列化时忽略未知字段</li>
 *   <li>{@code FAIL_ON_EMPTY_BEANS = false}，空对象序列化为 {@code {}}</li>
 *   <li>{@code WRITE_DATES_AS_TIMESTAMPS = false}，日期序列化为 ISO 字符串</li>
 *   <li>{@code SerializationInclusion.NON_NULL}，不序列化 null 字段</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * ObjectMapper mapper = JsonMapperFactory.getDefault();
 * String json = mapper.writeValueAsString(myObject);
 * MyDto dto = mapper.readValue(json, MyDto.class);
 * }</pre>
 *
 * @author zifang
 */
public final class JsonMapperFactory {

    /**
     * 全局共享的默认 ObjectMapper（懒加载）。
     */
    private static final ObjectMapper DEFAULT = newDefault();

    private JsonMapperFactory() {
    }

    /**
     * 创建新 ObjectMapper 实例，应用默认配置。
     *
     * @return 新创建的 ObjectMapper
     */
    public static ObjectMapper newDefault() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    /**
     * 获取全局共享的默认 ObjectMapper。
     * <p>
     * 该实例仅在首次调用时创建，后续调用直接返回缓存。
     * 该实例不可被调用方修改配置；如需自定义配置请使用 {@link #newDefault()}。
     *
     * @return 默认 ObjectMapper 单例
     */
    public static ObjectMapper getDefault() {
        return DEFAULT;
    }
}
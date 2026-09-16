package com.zifang.util.core.meta;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;

import java.io.IOException;

/**
 * Jackson 序列化器，用于保持 Long 类型数值不丢失精度。
 * <p>
 * 在某些 JavaScript 环境中，Long 类型数值可能超过 JavaScript Number 的安全整数范围，
 * 此序列化器确保 Long 值以完整精度输出。
 *
 * @author zifang
 * @see NumberSerializers.Base
 */
@JacksonStdImpl
public class KeepLongSerializer extends NumberSerializers.Base<Object> {

    private static final long serialVersionUID = -1194198701939237302L;

    public KeepLongSerializer() {
        super(Long.class, NumberType.LONG, "number");
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeNumber((Long) value);
    }
}

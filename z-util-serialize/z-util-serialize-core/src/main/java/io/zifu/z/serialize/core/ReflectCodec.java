package io.zifu.z.serialize.core;

import io.zifu.z.serialize.annotation.ZMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于 {@link SchemaRegistry} 元数据的反射编解码器。
 *
 * <p>这是"动态模式"的实现，性能比 codegen 慢 2~5 倍，但不需要 APT。
 * 适合开发期快速迭代，生产环境建议切换到 codegen。</p>
 *
 * <p><b>线程安全：</b>无状态，可共享。</p>
 */
public final class ReflectCodec implements Codec {

    public static final ReflectCodec INSTANCE = new ReflectCodec();

    private ReflectCodec() {}

    // ==================== Encode ====================

    @Override
    public void write(ZOutput out, Object message) throws IOException {
        SchemaRegistry.SchemaDescriptor schema = SchemaRegistry.get(message.getClass());
        writeMessage(out, message, schema);
    }

    private void writeMessage(ZOutput out, Object message,
                              SchemaRegistry.SchemaDescriptor schema) throws IOException {
        for (SchemaRegistry.FieldDescriptor fd : schema.getFields()) {
            if (!fd.getRemoved().isEmpty()) continue;  // 已移除字段不写

            Object value;
            try {
                value = fd.getField().get(message);
            } catch (IllegalAccessException e) {
                throw new IOException("Cannot read field " + fd.getName(), e);
            }
            if (value == null) continue;

            if (fd.isCollection()) {
                writeCollection(out, fd, (Collection<?>) value);
            } else if (fd.isMap()) {
                writeMap(out, fd, (Map<?, ?>) value);
            } else {
                writeField(out, fd, value);
            }
        }
    }

    private void writeField(ZOutput out, SchemaRegistry.FieldDescriptor fd,
                            Object value) throws IOException {
        int fieldId = fd.getId();
        Class<?> type = fd.getJavaType();

        switch (fd.getType()) {
            case VARINT:
                out.writeTag(fieldId, WireType.VARINT);
                writeVarIntValue(out, type, value);
                break;

            case FIXED32:
                out.writeTag(fieldId, WireType.FIXED32);
                if (type == float.class || type == Float.class) {
                    out.writeFloat((Float) value);
                } else {
                    out.writeFixed32(((Number) value).intValue());
                }
                break;

            case FIXED64:
                out.writeTag(fieldId, WireType.FIXED64);
                if (type == double.class || type == Double.class) {
                    out.writeDouble((Double) value);
                } else {
                    out.writeFixed64(((Number) value).longValue());
                }
                break;

            case LENGTH_DELIMITED:
                out.writeTag(fieldId, WireType.LENGTH_DELIMITED);
                if (type == String.class) {
                    out.writeLengthDelimited(((String) value).getBytes(StandardCharsets.UTF_8));
                } else if (type == byte[].class) {
                    out.writeLengthDelimited((byte[]) value);
                } else if (type.isAnnotationPresent(ZMessage.class)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
                    ZOutput nested = new ZOutput(baos);
                    writeMessage(nested, value, SchemaRegistry.get(type));
                    out.writeLengthDelimited(baos.toByteArray());
                } else {
                    throw new IOException("Unsupported LENGTH_DELIMITED type: " + type);
                }
                break;

            default:
                throw new IOException("Unsupported field type: " + fd.getType());
        }
    }

    private void writeVarIntValue(ZOutput out, Class<?> type, Object value) throws IOException {
        if (type == boolean.class || type == Boolean.class) {
            out.writeVarInt(((Boolean) value) ? 1 : 0);
        } else if (type == int.class || type == Integer.class) {
            out.writeSignedVarInt((Integer) value);
        } else if (type == long.class || type == Long.class) {
            out.writeSignedVarLong((Long) value);
        } else if (type.isEnum()) {
            out.writeSignedVarInt(((Enum<?>) value).ordinal());
        } else {
            throw new IOException("Unsupported VARINT Java type: " + type);
        }
    }

    private void writeCollection(ZOutput out, SchemaRegistry.FieldDescriptor fd,
                                 Collection<?> values) throws IOException {
        // 支持 packed repeated：仅对 VARINT 元素生效
        boolean canPack = fd.getType() == io.zifu.z.serialize.annotation.FieldType.VARINT;
        if (canPack) {
            // 一次性写到 buffer
            ByteArrayOutputStream packed = new ByteArrayOutputStream();
            ZOutput p = new ZOutput(packed);
            for (Object v : values) {
                writeVarIntValue(p, fd.getElementType() != null ? fd.getElementType() : v.getClass(), v);
            }
            out.writeTag(fd.getId(), WireType.LENGTH_DELIMITED);
            out.writeLengthDelimited(packed.toByteArray());
        } else {
            for (Object v : values) {
                writeField(out, fd, v);
            }
        }
    }

    private void writeMap(ZOutput out, SchemaRegistry.FieldDescriptor fd,
                          Map<?, ?> map) throws IOException {
        // Map 编码：每个 entry 作为 length-delimited message
        // entry 内: tag(1) key + tag(2) value
        for (Map.Entry<?, ?> e : map.entrySet()) {
            ByteArrayOutputStream entry = new ByteArrayOutputStream(64);
            ZOutput nested = new ZOutput(entry);
            // key
            if (e.getKey() == null) {
                nested.writeTag(1, WireType.VARINT);
                nested.writeVarInt(0);
            } else if (e.getKey() instanceof String) {
                nested.writeTag(1, WireType.LENGTH_DELIMITED);
                nested.writeLengthDelimited(((String) e.getKey()).getBytes(StandardCharsets.UTF_8));
            } else if (e.getKey() instanceof Integer) {
                nested.writeTag(1, WireType.VARINT);
                nested.writeSignedVarInt((Integer) e.getKey());
            } else if (e.getKey() instanceof Long) {
                nested.writeTag(1, WireType.VARINT);
                nested.writeSignedVarLong((Long) e.getKey());
            } else {
                // fallback: toString
                nested.writeTag(1, WireType.LENGTH_DELIMITED);
                nested.writeLengthDelimited(e.getKey().toString().getBytes(StandardCharsets.UTF_8));
            }
            // value
            if (e.getValue() == null) {
                nested.writeTag(2, WireType.VARINT);
                nested.writeVarInt(0);
            } else if (e.getValue() instanceof String) {
                nested.writeTag(2, WireType.LENGTH_DELIMITED);
                nested.writeLengthDelimited(((String) e.getValue()).getBytes(StandardCharsets.UTF_8));
            } else if (e.getValue() instanceof Integer) {
                nested.writeTag(2, WireType.VARINT);
                nested.writeSignedVarInt((Integer) e.getValue());
            } else if (e.getValue() instanceof Long) {
                nested.writeTag(2, WireType.VARINT);
                nested.writeSignedVarLong((Long) e.getValue());
            } else if (e.getValue() instanceof Boolean) {
                nested.writeTag(2, WireType.VARINT);
                nested.writeVarInt(((Boolean) e.getValue()) ? 1 : 0);
            } else if (e.getValue() instanceof Number) {
                nested.writeTag(2, WireType.VARINT);
                nested.writeVarInt(((Number) e.getValue()).intValue());
            } else {
                nested.writeTag(2, WireType.LENGTH_DELIMITED);
                nested.writeLengthDelimited(e.getValue().toString().getBytes(StandardCharsets.UTF_8));
            }
            out.writeTag(fd.getId(), WireType.LENGTH_DELIMITED);
            out.writeLengthDelimited(entry.toByteArray());
        }
    }

    // ==================== Decode ====================

    @Override
    public <T> T read(ZInput in, Class<T> messageClass) throws IOException {
        SchemaRegistry.SchemaDescriptor schema = SchemaRegistry.get(messageClass);
        T message;
        try {
            message = messageClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IOException("Cannot instantiate " + messageClass.getName()
                    + " (requires public no-arg constructor)", e);
        }
        readMessage(in, message, schema);
        return message;
    }

    private void readMessage(ZInput in, Object message,
                             SchemaRegistry.SchemaDescriptor schema) throws IOException {
        // 简化：循环至 EOF（如果流是字节流），否则按 length-delimited body 读取
        while (in.hasMore()) {
            int tag = in.readTag();
            int fieldId = ZInput.fieldId(tag);
            int wireType = ZInput.wireTypeOf(tag);

            SchemaRegistry.FieldDescriptor fd = schema.getFieldsById().get(fieldId);
            if (fd == null) {
                skipUnknown(in, wireType);
                continue;
            }
            readField(in, message, fd, wireType);
        }
    }

    private void skipUnknown(ZInput in, int wireType) throws IOException {
        switch (wireType) {
            case WireType.VARINT:          in.readVarInt(); break;
            case WireType.FIXED64:         in.readFixed64(); break;
            case WireType.FIXED32:         in.readFixed32(); break;
            case WireType.LENGTH_DELIMITED: in.skipLengthDelimited(); break;
            default: throw new IOException("Cannot skip unknown wire type: " + wireType);
        }
    }

    private void readField(ZInput in, Object message, SchemaRegistry.FieldDescriptor fd,
                           int wireType) throws IOException {
        Class<?> type = fd.getJavaType();
        Field f = fd.getField();

        if (fd.isCollection()) {
            readCollection(in, message, fd, wireType);
            return;
        }
        if (fd.isMap()) {
            readMap(in, message, fd, wireType);
            return;
        }

        Object value;
        switch (fd.getType()) {
            case VARINT:
                value = readVarIntValue(in, type);
                break;

            case FIXED32:
                if (type == float.class || type == Float.class) value = in.readFloat();
                else value = in.readFixed32();
                break;

            case FIXED64:
                if (type == double.class || type == Double.class) value = in.readDouble();
                else value = in.readFixed64();
                break;

            case LENGTH_DELIMITED:
                if (type == String.class) {
                    value = new String(in.readLengthDelimited(), StandardCharsets.UTF_8);
                } else if (type == byte[].class) {
                    value = in.readLengthDelimited();
                } else if (type.isAnnotationPresent(ZMessage.class)) {
                    byte[] bytes = in.readLengthDelimited();
                    value = read(new ZInput(new ByteArrayInputStream(bytes)), type);
                } else {
                    throw new IOException("Unsupported LENGTH_DELIMITED type: " + type);
                }
                break;

            default:
                throw new IOException("Unsupported field type: " + fd.getType());
        }

        try {
            f.set(message, value);
        } catch (IllegalAccessException e) {
            throw new IOException("Cannot set field " + fd.getName(), e);
        }
    }

    private Object readVarIntValue(ZInput in, Class<?> type) throws IOException {
        if (type == boolean.class || type == Boolean.class) return in.readVarInt() != 0;
        if (type == int.class || type == Integer.class) return in.readSignedVarInt();
        if (type == long.class || type == Long.class) return in.readSignedVarLong();
        if (type.isEnum()) {
            int ord = in.readSignedVarInt();
            Object[] constants = type.getEnumConstants();
            if (ord < 0 || ord >= constants.length) {
                throw new IOException("Invalid enum ordinal: " + ord + " for " + type.getName());
            }
            return constants[ord];
        }
        throw new IOException("Unsupported VARINT type: " + type);
    }

    @SuppressWarnings("unchecked")
    private void readCollection(ZInput in, Object message, SchemaRegistry.FieldDescriptor fd,
                                int wireType) throws IOException {
        Field f = fd.getField();
        try {
            Collection<Object> coll = (Collection<Object>) f.get(message);
            if (coll == null) {
                coll = new java.util.ArrayList<>();
                f.set(message, coll);
            }

            Class<?> elemType = fd.getElementType() != null ? fd.getElementType() : Object.class;

            // packed: length-delimited 包含多个 varint
            if (fd.getType() == io.zifu.z.serialize.annotation.FieldType.VARINT
                    && wireType == WireType.LENGTH_DELIMITED) {
                byte[] bytes = in.readLengthDelimited();
                ZInput nested = new ZInput(new ByteArrayInputStream(bytes));
                while (nested.hasMore()) {
                    coll.add(readVarIntValue(nested, elemType));
                }
                return;
            }

            // 非 packed：单个元素
            switch (fd.getType()) {
                case VARINT:    coll.add(readVarIntValue(in, elemType)); break;
                case FIXED32:   coll.add(in.readFloat()); break;
                case FIXED64:   coll.add(in.readDouble()); break;
                case LENGTH_DELIMITED:
                    if (elemType == String.class) {
                        coll.add(new String(in.readLengthDelimited(), StandardCharsets.UTF_8));
                    } else {
                        coll.add(in.readLengthDelimited());
                    }
                    break;
                default: throw new IOException("Unsupported collection element type");
            }
        } catch (IllegalAccessException e) {
            throw new IOException("Collection read error", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void readMap(ZInput in, Object message, SchemaRegistry.FieldDescriptor fd,
                         int wireType) throws IOException {
        Field f = fd.getField();
        try {
            Map<Object, Object> map = (Map<Object, Object>) f.get(message);
            if (map == null) {
                map = new LinkedHashMap<>();
                f.set(message, map);
            }
            byte[] entryBytes = in.readLengthDelimited();
            ByteArrayInputStream bais = new ByteArrayInputStream(entryBytes);
            ZInput nested = new ZInput(bais);
            Object key = null, value = null;
            while (bais.available() > 0) {
                int entryTag = nested.readTag();
                int entryFieldId = ZInput.fieldId(entryTag);
                int entryWireType = ZInput.wireTypeOf(entryTag);
                if (entryFieldId == 1) {
                    // key
                    if (entryWireType == WireType.LENGTH_DELIMITED) {
                        key = new String(nested.readLengthDelimited(), StandardCharsets.UTF_8);
                    } else if (entryWireType == WireType.VARINT) {
                        key = (int) nested.readSignedVarLong();
                    } else {
                        skipUnknown(nested, entryWireType);
                    }
                } else if (entryFieldId == 2) {
                    // value（按 elementType 类型读）
                    if (entryWireType == WireType.VARINT) {
                        Class<?> valueType = fd.getElementType();
                        if (valueType == Integer.class || valueType == int.class) {
                            value = nested.readSignedVarInt();
                        } else if (valueType == Long.class || valueType == long.class) {
                            value = nested.readSignedVarLong();
                        } else if (valueType == Boolean.class || valueType == boolean.class) {
                            value = nested.readVarInt() != 0;
                        } else {
                            value = nested.readSignedVarLong();
                        }
                    } else if (entryWireType == WireType.LENGTH_DELIMITED) {
                        if (fd.getElementType() == String.class) {
                            value = new String(nested.readLengthDelimited(), StandardCharsets.UTF_8);
                        } else {
                            value = nested.readLengthDelimited();
                        }
                    } else {
                        skipUnknown(nested, entryWireType);
                    }
                } else {
                    skipUnknown(nested, entryWireType);
                }
            }
            map.put(key, value);
        } catch (IllegalAccessException e) {
            throw new IOException("Map read error", e);
        }
    }
}

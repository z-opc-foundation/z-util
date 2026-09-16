package io.zifu.z.serialize.formats.json;

import io.zifu.z.serialize.annotation.ZMessage;
import io.zifu.z.serialize.core.SchemaRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * JSON 互转器：将 {@code @ZMessage} 对象转为人类可读 JSON，反之亦然。
 *
 * <p>JSON 输出包含字段 ID（{@code _id}），便于其他语言解析时定位。
 * 例如：</p>
 *
 * <pre>{@code
 * {
 *     "_id": 100,                          // 来自 @ZMessage.id
 *     "_type": "test.User",                // 来自 @ZMessage.name
 *     "1": 42,                             // @ZField(id=1) → key="1"
 *     "2": "alice"                         // @ZField(id=2) → key="2"
 * }
 * }</pre>
 *
 * <p>数值 key 保证跨语言顺序无关；字段名可读性通过 {@code _fields} 数组补充：</p>
 *
 * <pre>{@code
 * {
 *     "_id": 100,
 *     "_fields": ["id", "name"],
 *     "1": 42,
 *     "2": "alice"
 * }
 * }</pre>
 */
public final class JsonTranscoder {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonTranscoder() {}

    /** 将消息转为 JSON 字符串（缩进格式，便于阅读）。 */
    public static String toJson(Object message) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        encodeInto(root, message);
        return MAPPER.writeValueAsString(root);
    }

    /** 将消息转为紧凑 JSON 字符串。 */
    public static String toCompactJson(Object message) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        encodeInto(root, message);
        return MAPPER.writeValueAsString(root);
    }

    /** 从 JSON 字符串解码消息。 */
    public static <T> T fromJson(String json, Class<T> messageClass) throws IOException {
        JsonNode node = MAPPER.readTree(json);
        return decodeFrom(node, messageClass);
    }

    private static void encodeInto(ObjectNode root, Object message) {
        SchemaRegistry.SchemaDescriptor schema = SchemaRegistry.get(message.getClass());
        root.put("_id", schema.getId());
        if (!schema.getName().isEmpty()) {
            root.put("_type", schema.getName());
        }
        // 字段名映射（顺序保持）
        ArrayNode fields = root.putArray("_fields");
        for (SchemaRegistry.FieldDescriptor fd : schema.getFields()) {
            if (!fd.getRemoved().isEmpty()) continue;
            Object v;
            try {
                v = fd.getField().get(message);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot read " + fd.getName(), e);
            }
            if (v == null) continue;
            fields.add(fd.getName());
            root.set(String.valueOf(fd.getId()), encodeValue(v));
        }
    }

    private static JsonNode encodeValue(Object v) {
        if (v == null) return MAPPER.nullNode();
        if (v instanceof Boolean) return MAPPER.valueToTree(v);
        if (v instanceof Number) return MAPPER.valueToTree(v);
        if (v instanceof String) return MAPPER.valueToTree(v);
        if (v instanceof Collection) {
            ArrayNode arr = MAPPER.createArrayNode();
            for (Object o : (Collection<?>) v) arr.add(encodeValue(o));
            return arr;
        }
        if (v instanceof Map) {
            ObjectNode obj = MAPPER.createObjectNode();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                obj.set(String.valueOf(e.getKey()), encodeValue(e.getValue()));
            }
            return obj;
        }
        if (v.getClass().isAnnotationPresent(ZMessage.class)) {
            ObjectNode obj = MAPPER.createObjectNode();
            encodeInto(obj, v);
            return obj;
        }
        if (v.getClass().isEnum()) {
            return MAPPER.valueToTree(((Enum<?>) v).name());
        }
        // fallback
        return MAPPER.valueToTree(v.toString());
    }

    private static <T> T decodeFrom(JsonNode node, Class<T> messageClass) throws IOException {
        SchemaRegistry.SchemaDescriptor schema = SchemaRegistry.get(messageClass);
        try {
            T instance = messageClass.getDeclaredConstructor().newInstance();
            for (SchemaRegistry.FieldDescriptor fd : schema.getFields()) {
                if (!fd.getRemoved().isEmpty()) continue;
                JsonNode fieldNode = node.get(String.valueOf(fd.getId()));
                if (fieldNode == null || fieldNode.isNull()) continue;
                Object v = decodeValue(fieldNode, fd.getJavaType(), fd.getElementType());
                fd.getField().set(instance, v);
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IOException("Cannot decode JSON to " + messageClass.getName(), e);
        }
    }

    private static Object decodeValue(JsonNode n, Class<?> type, Class<?> elemType) {
        if (n.isNull()) return null;
        if (type == boolean.class || type == Boolean.class) return n.booleanValue();
        if (type == int.class || type == Integer.class) return n.intValue();
        if (type == long.class || type == Long.class) return n.longValue();
        if (type == float.class || type == Float.class) return n.floatValue();
        if (type == double.class || type == Double.class) return n.doubleValue();
        if (type == String.class) return n.asText();
        if (type.isEnum()) {
            // 支持 enum by name
            return Enum.valueOf((Class<Enum>) type.asSubclass(Enum.class), n.asText());
        }
        if (Collection.class.isAssignableFrom(type)) {
            java.util.Collection<Object> coll = new java.util.ArrayList<>();
            for (JsonNode item : n) {
                Object o = decodeValue(item, elemType, null);
                coll.add(o);
            }
            return coll;
        }
        if (Map.class.isAssignableFrom(type)) {
            Map<Object, Object> map = new java.util.LinkedHashMap<>();
            n.fields().forEachRemaining(e -> {
                Object key = e.getKey();
                Object value = decodeValue(e.getValue(), elemType, null);
                map.put(key, value);
            });
            return map;
        }
        if (type.isAnnotationPresent(ZMessage.class)) {
            try {
                return decodeFrom(n, type);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return n.asText();
    }
}

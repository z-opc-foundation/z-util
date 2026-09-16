package io.zifu.z.serialize.core;

import io.zifu.z.serialize.annotation.ZEnum;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;
import io.zifu.z.serialize.annotation.ZMap;
import io.zifu.z.serialize.annotation.ZOneof;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schema 注册表：从 {@link ZMessage}/{@link ZField} 注解构建内存中的元数据。
 *
 * <p>反射序列化器、代码生成器、JSON 转码器都基于本类提供的元数据工作。
 * 因此 {@link SchemaRegistry} 是"schema-first"模型的单一真相源。</p>
 *
 * @author zifang
 */
public final class SchemaRegistry {

    private static final Map<Class<?>, SchemaDescriptor> CACHE = new ConcurrentHashMap<>();

    private SchemaRegistry() {}

    /** 从带 {@code @ZMessage} 注解的类构建 schema 描述符（带缓存）。 */
    public static SchemaDescriptor get(Class<?> messageClass) {
        SchemaDescriptor cached = CACHE.get(messageClass);
        if (cached != null) return cached;

        ZMessage ann = messageClass.getAnnotation(ZMessage.class);
        if (ann == null) {
            throw new IllegalArgumentException("Class " + messageClass.getName()
                    + " is not annotated with @ZMessage");
        }

        List<FieldDescriptor> fields = new ArrayList<>();
        Map<Integer, FieldDescriptor> byId = new LinkedHashMap<>();
        Map<String, FieldDescriptor> byName = new HashMap<>();

        for (Field f : messageClass.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (Modifier.isTransient(f.getModifiers())) continue;

            ZField zf = f.getAnnotation(ZField.class);
            if (zf == null) continue;

            FieldDescriptor fd = buildFieldDescriptor(f, zf);
            fields.add(fd);
            if (fd.getId() > 0) {
                FieldDescriptor existing = byId.put(fd.getId(), fd);
                if (existing != null) {
                    throw new IllegalStateException("Duplicate field id " + fd.getId()
                            + " in " + messageClass.getName()
                            + ": " + existing.getName() + " and " + fd.getName());
                }
            }
            byName.put(fd.getName(), fd);
        }

        SchemaDescriptor desc = new SchemaDescriptor(
                messageClass, ann.id(), ann.name(), ann.version(),
                ann.compressed(), ann.compression(),
                ann.encrypted(), ann.index(),
                fields, byId, byName);
        CACHE.put(messageClass, desc);
        return desc;
    }

    /** 清除缓存（用于注解处理器或动态重载场景）。 */
    public static void invalidate() {
        CACHE.clear();
    }

    private static FieldDescriptor buildFieldDescriptor(Field f, ZField zf) {
        f.setAccessible(true);

        Type genericType = f.getGenericType();
        boolean isCollection = genericType instanceof ParameterizedType
                && Collection.class.isAssignableFrom((Class<?>) ((ParameterizedType) genericType).getRawType());
        boolean isMap = genericType instanceof ParameterizedType
                && Map.class.isAssignableFrom((Class<?>) ((ParameterizedType) genericType).getRawType());

        boolean isEnum = f.getType().isEnum();
        Class<?> elementType = null;
        if (isCollection || isMap) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] args = pt.getActualTypeArguments();
            elementType = (Class<?>) args[args.length - 1];
        }

        // oneof 处理
        ZOneof oneof = f.getAnnotation(ZOneof.class);
        boolean isOneof = oneof != null;

        // map 处理
        ZMap mapAnn = f.getAnnotation(ZMap.class);
        boolean isMapField = mapAnn != null || isMap;

        return new FieldDescriptor(
                f, zf.id(), zf.type(),
                zf.deprecated(), zf.since(), zf.removed(),
                zf.packed(), zf.defaultValue(),
                isCollection, isMapField, isEnum, isOneof,
                elementType);
    }

    // ==================== 描述符 ====================

    /** Message 的完整 schema 描述符（不可变）。 */
    public static final class SchemaDescriptor {
        private final Class<?> messageClass;
        private final int id;
        private final String name;
        private final String version;
        private final boolean compressed;
        private final String compression;
        private final boolean encrypted;
        private final String index;
        private final List<FieldDescriptor> fields;
        private final Map<Integer, FieldDescriptor> fieldsById;
        private final Map<String, FieldDescriptor> fieldsByName;

        SchemaDescriptor(Class<?> messageClass, int id, String name, String version,
                         boolean compressed, String compression,
                         boolean encrypted, String index,
                         List<FieldDescriptor> fields,
                         Map<Integer, FieldDescriptor> fieldsById,
                         Map<String, FieldDescriptor> fieldsByName) {
            this.messageClass = messageClass;
            this.id = id;
            this.name = name;
            this.version = version;
            this.compressed = compressed;
            this.compression = compression;
            this.encrypted = encrypted;
            this.index = index;
            this.fields = fields;
            this.fieldsById = fieldsById;
            this.fieldsByName = fieldsByName;
        }

        public Class<?> getMessageClass() { return messageClass; }
        public int getId() { return id; }
        public String getName() { return name; }
        public String getVersion() { return version; }
        public boolean isCompressed() { return compressed; }
        public String getCompression() { return compression; }
        public boolean isEncrypted() { return encrypted; }
        public String getIndex() { return index; }
        public List<FieldDescriptor> getFields() { return fields; }
        public Map<Integer, FieldDescriptor> getFieldsById() { return fieldsById; }
        public Map<String, FieldDescriptor> getFieldsByName() { return fieldsByName; }
    }

    /** 单个字段的描述符（不可变）。 */
    public static final class FieldDescriptor {
        private final Field field;
        private final int id;
        private final io.zifu.z.serialize.annotation.FieldType type;
        private final boolean deprecated;
        private final String since;
        private final String removed;
        private final boolean packed;
        private final String defaultValue;
        private final boolean collection;
        private final boolean map;
        private final boolean isEnum;
        private final boolean oneof;
        private final Class<?> elementType;

        FieldDescriptor(Field field, int id,
                        io.zifu.z.serialize.annotation.FieldType type,
                        boolean deprecated, String since, String removed,
                        boolean packed, String defaultValue,
                        boolean collection, boolean map, boolean isEnum,
                        boolean oneof, Class<?> elementType) {
            this.field = field;
            this.id = id;
            this.type = type;
            this.deprecated = deprecated;
            this.since = since;
            this.removed = removed;
            this.packed = packed;
            this.defaultValue = defaultValue;
            this.collection = collection;
            this.map = map;
            this.isEnum = isEnum;
            this.oneof = oneof;
            this.elementType = elementType;
        }

        public Field getField() { return field; }
        public int getId() { return id; }
        public io.zifu.z.serialize.annotation.FieldType getType() { return type; }
        public boolean isDeprecated() { return deprecated; }
        public String getSince() { return since; }
        public String getRemoved() { return removed; }
        public boolean isPacked() { return packed; }
        public String getDefaultValue() { return defaultValue; }
        public boolean isCollection() { return collection; }
        public boolean isMap() { return map; }
        public boolean isEnum() { return isEnum; }
        public boolean isOneof() { return oneof; }
        public Class<?> getElementType() { return elementType; }

        public String getName() { return field.getName(); }
        public Class<?> getDeclaringClass() { return field.getDeclaringClass(); }

        /** 字段的 Java 类型（去除 Optional/Collection/Map 包装）。 */
        public Class<?> getJavaType() {
            Class<?> t = field.getType();
            return t;
        }
    }
}

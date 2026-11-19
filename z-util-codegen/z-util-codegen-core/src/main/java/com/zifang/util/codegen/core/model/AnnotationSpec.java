package com.zifang.util.codegen.core.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 注解规格：把 {@code javax.lang.model} 侧的注解镜像摊平成纯数据，使 handler 不必依赖具体注解类。
 *
 * <p>属性值已展开默认值，因此读取未显式声明的属性与读取显式声明的属性行为一致。</p>
 *
 * @author zifang
 */
public final class AnnotationSpec {

    private final String qualifiedName;
    private final Map<String, Object> values;

    public AnnotationSpec(String qualifiedName, Map<String, Object> values) {
        this.qualifiedName = qualifiedName;
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    /** 全部属性（含默认值）。 */
    public Map<String, Object> values() {
        return values;
    }

    public Object rawValue(String attribute) {
        return values.get(attribute);
    }

    public String string(String attribute, String fallback) {
        Object v = values.get(attribute);
        return v instanceof String s ? s : fallback;
    }

    public boolean bool(String attribute, boolean fallback) {
        Object v = values.get(attribute);
        return v instanceof Boolean b ? b : fallback;
    }

    public int integer(String attribute, int fallback) {
        Object v = values.get(attribute);
        return v instanceof Integer i ? i : fallback;
    }

    @SuppressWarnings("unchecked")
    public List<String> strings(String attribute) {
        Object v = values.get(attribute);
        return v instanceof List<?> list ? (List<String>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    public List<AnnotationSpec> annotations(String attribute) {
        Object v = values.get(attribute);
        return v instanceof List<?> list ? (List<AnnotationSpec>) list : List.of();
    }

    /** 枚举属性按常量名读取。 */
    public String enumeration(String attribute, String fallback) {
        Object v = values.get(attribute);
        return v instanceof String s ? s : fallback;
    }

    @Override
    public String toString() {
        return "@" + qualifiedName + values;
    }
}

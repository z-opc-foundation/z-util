package com.zifang.util.doc.pdf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** PDF 字典。取值助手带解引用回调由调用方处理；这里只存原始结构。 */
public final class PdfDict {

    private final Map<PdfName, Object> map = new LinkedHashMap<>();

    public void put(PdfName key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(new PdfName(key));
    }

    public PdfDict getDict(String key) {
        Object v = get(key);
        while (v instanceof PdfRef r) {
            throw new IllegalStateException("unresolved ref for /" + key);
        }
        return v instanceof PdfDict d ? d : null;
    }

    public Object getRaw(String key) {
        return get(key);
    }

    public List<Object> getArray(String key) {
        return get(key) instanceof List<?> l ? (List<Object>) l : null;
    }

    public Double getNumber(String key) {
        return get(key) instanceof Number n ? n.doubleValue() : null;
    }

    public double getNumberOr(String key, double dft) {
        Double v = getNumber(key);
        return v == null ? dft : v;
    }

    public String getNameValue(String key) {
        return get(key) instanceof PdfName n ? n.name : null;
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    public Map<PdfName, Object> asMap() {
        return map;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /** 深度取值：沿 ref 链解引用直到拿到目标（resolver 由调用方给）。 */
    public interface RefResolver {
        Object resolve(PdfRef ref);
    }

    public static Object resolve(Object v, RefResolver resolver, int depth) {
        while (v instanceof PdfRef r && depth-- > 0) {
            v = resolver.resolve(r);
        }
        return v;
    }

    /** 展开数组元素里的 ref。 */
    public static List<Object> resolveList(Object v, RefResolver resolver) {
        if (!(v instanceof List<?> l)) {
            return null;
        }
        List<Object> out = new ArrayList<>();
        for (Object o : l) {
            out.add(o instanceof PdfRef r ? resolver.resolve(r) : o);
        }
        return out;
    }
}

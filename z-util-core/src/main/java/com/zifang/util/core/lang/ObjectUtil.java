package com.zifang.util.core.lang;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * ObjectUtil类。
 */
public class ObjectUtil {
    /**
     * 采用对象的序列化完成对象的深克隆
     *
     * @param obj 待克隆的对象，必须实现Serializable接口
     * @return 深克隆后的新对象，如果克隆失败返回null
     */
    @SuppressWarnings("unchecked")
    /**
     * deepCloneObject方法。
     *      * @param obj T类型参数
     * @return static <T extends Serializable> T类型返回值
     */
    public static <T extends Serializable> T deepCloneObject(T obj) {
        T cloneObj = null;
        try {
            // 写入字节流
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream obs = new ObjectOutputStream(out);
            obs.writeObject(obj);
            obs.close();

            // 分配内存，写入原始对象，生成新对象
            ByteArrayInputStream ios = new ByteArrayInputStream(out.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(ios);
            // 返回生成的新对象
            cloneObj = (T) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloneObj;
    }

    /**
     * 利用序列化完成集合的深克隆
     *
     * @param collection 待克隆的集合，必须可序列化
     * @return 深克隆后的新集合
     * @throws ClassNotFoundException 当反序列化时找不到类时抛出
     * @throws java.io.IOException    当序列化或反序列化发生IO错误时抛出
     */
    @SuppressWarnings("unchecked")
    /**
     * deepCloneCollection方法。
     *      * @param collection CollectionT类型参数
     * @return static <T> Collection<T>类型返回值
     */
    public static <T> Collection<T> deepCloneCollection(Collection<T> collection) throws ClassNotFoundException, IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(collection);
        out.close();

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        Collection<T> dest = (Collection<T>) in.readObject();
        in.close();

        return dest;
    }

    /**
     * 判断对象是否为“空”
     * <p>
     * 支持的类型：null、空CharSequence、空Collection、空Map、空数组；其余类型仅判断null。
     *
     * @param obj 待判断对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(obj) == 0;
        }
        return false;
    }

    /**
     * 判断对象是否不为“空”
     *
     * @param obj 待判断对象
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 判断给定所有元素是否全部为空
     *
     * @param values 待判断参数
     * @return 全为空（含参数数组本身为空）返回true，否则false
     */
    public static boolean isAllEmpty(Object... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (Object val : values) {
            if (isNotEmpty(val)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断给定所有元素是否全部不为空
     *
     * @param values 待判断参数
     * @return 全不为空返回true（参数数组本身为空返回false），否则false
     */
    public static boolean isAllNotEmpty(Object... values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (Object val : values) {
            if (isEmpty(val)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断目标对象是否与候选集合中任意一个相等
     *
     * @param target   目标对象
     * @param candidates 候选对象集合
     * @param <T>      对象类型
     * @return 与任一候选相等返回true；候选为null或空返回false
     */
    @SafeVarargs
    public static <T> boolean equalsAny(T target, T... candidates) {
        if (candidates == null || candidates.length == 0) {
            return false;
        }
        for (T candidate : candidates) {
            if (Objects.equals(target, candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当对象不为null时获取其映射值
     *
     * @param obj    目标对象
     * @param mapper 取值函数
     * @param <T>    对象类型
     * @param <V>    返回值类型
     * @return obj为null时返回null，否则返回mapper的执行结果
     */
    public static <T, V> V getIfNotNull(T obj, Function<T, V> mapper) {
        return getIfNotNull(obj, mapper, null);
    }

    /**
     * 当对象不为null时获取其映射值，对象为null时返回默认值
     *
     * @param obj           目标对象
     * @param mapper        取值函数
     * @param defaultValue  对象为null时的默认值
     * @param <T>           对象类型
     * @param <V>           返回值类型
     * @return obj为null时返回defaultValue，否则返回mapper的执行结果
     */
    public static <T, V> V getIfNotNull(T obj, Function<T, V> mapper, V defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        return mapper.apply(obj);
    }
}

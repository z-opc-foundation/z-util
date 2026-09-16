package com.zifang.util.json.define;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 类型引用抽象类，用于在运行时获取泛型的实际类型。
 * <p>
 * 主要用于 JSON 反序列化时保留泛型类型信息，例如：
 * <pre>{@code
 * TypeReference<List<User>> typeRef = new TypeReference<List<User>>(){};
 * List<User> users = JsonUtil.fromJson(jsonString, typeRef);
 * }</pre>
 *
 * @param <T> 泛型类型
 * @author zifang
 */

/**
 * TypeReference类。
 */
public class TypeReference<T> {

    private final Type type;

    /**
     * 通过反射获取泛型的实际类型参数。
     * <p>
     * 利用子类继承时保留的泛型信息，通过 {@link ParameterizedType} 获取父类的类型参数。
     *
     * @throws RuntimeException 如果类声明中缺少泛型参数
     */
    /**
     * TypeReference方法。
     */
    protected TypeReference() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * 获取泛型的实际类型。
     *
     * @return 表示泛型类型的 {@link Type} 对象
     */
    /**
     * getType方法。
     *
     * @return Type类型返回值
     */
    public Type getType() {
        return type;
    }
}

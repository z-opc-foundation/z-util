package com.zifang.util.core.lang.converter;

import com.zifang.util.core.lang.PrimitiveUtil;
import com.zifang.util.core.lang.converter.converters.DefaultConverter;
import com.zifang.util.core.lang.reflect.ClassParser;
import com.zifang.util.core.lang.reflect.ClassParserFactory;
import com.zifang.util.core.lang.tuples.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类型转换器注册与管理中心。
 * <p>
 * 提供转换器的查找、注册和管理功能，支持基本类型、包装类型和自定义类型之间的转换。
 *
 * @author zifang
 * @see IConverter
 * @see ConvertCaller
 */
public class Converters {

    private static final Map<Pair<Class<?>, Class<?>>, IConverter<?, ?>> converterCache = new HashMap<>();

    static {
        DefaultConverter<Object, Object> defaultConverter = new DefaultConverter<>();
        Method[] methods = DefaultConverter.class.getDeclaredMethods();
        for (Method method : methods) {
            Parameter[] parameters = method.getParameters();
            Pair<Class<?>, Class<?>> pair = new Pair<>(parameters[0].getType(), parameters[1].getType());

            ConvertCaller<?, ?> iConverter = new ConvertCaller<>();
            iConverter.setFrom(parameters[0].getType());
            iConverter.setTarget(parameters[1].getType());
            iConverter.setCaller(defaultConverter);
            iConverter.setMethod(method);

            converterCache.put(pair, iConverter);
        }
    }

    /**
     * findConverter方法。
     * * @param from ClassF类型参数
     *
     * @param target ClassT类型参数
     * @return static <F,T> IConverter<F,T>类型返回值
     */
    public static <F, T> IConverter<F, T> findConverter(Class<F> from, Class<T> target) {

        // 获取原始类型
        Class<?> parsedFrom = PrimitiveUtil.getPrimitiveWrapper(from);
        Class<?> parsedTarget = PrimitiveUtil.getPrimitiveWrapper(target);

        Pair<Class<?>, Class<?>> pair = new Pair<>(parsedFrom, parsedTarget);

        if (converterCache.containsKey(pair)) {
            return (ConvertCaller<F, T>) converterCache.get(pair);
        } else {
            // 有可能参数是父类的
            ConvertCaller<F, T> convertCaller = (ConvertCaller<F, T>) findConverter0(parsedFrom, parsedTarget);
            ConvertCaller<F, T> copy = convertCaller.copy();
            copy.setFrom(parsedFrom);
            copy.setTarget(parsedTarget);

            converterCache.put(pair, copy);

            return copy;
        }
    }

    private static <F, T> IConverter<F, T> findConverter0(Class<F> a, Class<T> b) {
        Pair<Class<?>, Class<?>> pair = new Pair<>(a, b);

        // 直接寻找
        if (converterCache.containsKey(pair)) {
            return (IConverter<F, T>) converterCache.get(pair);
        }

        // 类型完全匹配
        for (Map.Entry<Pair<Class<?>, Class<?>>, IConverter<?, ?>> entry : converterCache.entrySet()) {
            if (entry.getKey().getA() == a && entry.getKey().getB() == b) {
                return (IConverter<F, T>) entry.getValue();
            }
        }

        // 继承寻找
        for (Map.Entry<Pair<Class<?>, Class<?>>, IConverter<?, ?>> entry : converterCache.entrySet()) {
            if (entry.getKey().getA().isAssignableFrom(a) && entry.getKey().getB().isAssignableFrom(b)) {
                return (IConverter<F, T>) entry.getValue();
            }
        }

//        return null;
        // todo 利用转换图能力进行转换
        throw new RuntimeException("没有找到对应的转换器" + a.getName() + "->" + b.getName());
    }

    /**
     * registerConverter方法。
     * * @param clazz Class?类型参数
     *
     * @return static <F,T> void类型返回值
     */
    public static <F, T> void registerConverter(Class<? extends IConverter<F, T>> clazz) {
        try {
            Object instance = clazz.newInstance();
            registerConverter((IConverter<F, T>) instance);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * registerConverter方法。
     * * @param converter IConverterF,类型参数
     *
     * @param from   Class?类型参数
     * @param target Class?类型参数
     * @return static <F,T> void类型返回值
     */
    public static <F, T> void registerConverter(IConverter<F, T> converter, Class<?> from, Class<?> target) {
        Pair<Class<?>, Class<?>> pair = new Pair<>(from, target);

        for (Method method : converter.getClass().getMethods()) {
            if (method.getName().equals("to")) {
                Parameter[] parameters = method.getParameters();
                if (parameters.length == 2 && parameters[1].getType() != Class.class) {
                    ConvertCaller<F, T> convertCaller = new ConvertCaller<>();
                    convertCaller.setFrom(pair.getA());
                    convertCaller.setTarget(pair.getB());
                    convertCaller.setMethod(method);
                    convertCaller.setCaller(converter);
                    converterCache.put(pair, convertCaller);
                }
            }
        }
    }

    /**
     * registerConverter方法。
     * * @param converter IConverterF,类型参数
     *
     * @return static <F,T> void类型返回值
     */
    public static <F, T> void registerConverter(IConverter<F, T> converter) {
        try {
            ClassParser classParser = new ClassParserFactory().getInstance(converter.getClass());
            Type type = classParser.getGenericType(IConverter.class);
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] types = parameterizedType.getActualTypeArguments();

                Class<?> fromType = types[0] instanceof Class
                        ? (Class<?>) types[0]
                        : (Class<?>) ((ParameterizedType) types[0]).getRawType();
                Class<?> targetType = types[1] instanceof Class
                        ? (Class<?>) types[1]
                        : (Class<?>) ((ParameterizedType) types[1]).getRawType();

                Pair<Class<?>, Class<?>> pair = new Pair<>(fromType, targetType);
                Method method = converter.getClass().getDeclaredMethod(
                        "to",
                        pair.getA(),
                        pair.getB()
                );

                ConvertCaller<F, T> convertCaller = new ConvertCaller<>();
                convertCaller.setFrom(pair.getA());
                convertCaller.setTarget(pair.getB());
                convertCaller.setMethod(method);
                convertCaller.setCaller(converter);

                converterCache.put(pair, convertCaller);
            } else {
                throw new RuntimeException("无法从当前转换器内捕获泛型信息，请检查是否传入lamda匿名内部类");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    /**
     * to方法。
     * * @param value Object类型参数
     *
     * @param clazz ClassT类型参数
     * @return static <F,T> T类型返回值
     */
    public static <F, T> T to(Object value, Class<T> clazz) {
        return findConverter((Class<F>) value.getClass(), clazz).to((F) value);
    }

    /**
     * 将Object安全地转换为指定元素类型的List。
     * <p>
     * 泛型擦除后无法直接强转 List&lt;T&gt;，本方法逐元素以目标类型做 cast：
     * 当 value 本身是 List 时，逐个元素转换后返回新列表；
     * 当 value 不是 List 或为 null 时返回空列表。
     *
     * @param value 待转换对象，通常是泛型擦除后的列表
     * @param clazz 目标元素类型
     * @param <T>   目标元素类型
     * @return 转换后的列表；元素类型不兼容时会抛出 ClassCastException
     */
    public static <T> List<T> castList(Object value, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                result.add(clazz.cast(item));
            }
        }
        return result;
    }
}

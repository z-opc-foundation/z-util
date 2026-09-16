package com.zifang.util.core.lang.reflect;

import com.zifang.util.core.pattern.composite.tree.ILeaf;
import com.zifang.util.core.pattern.composite.tree.LeafHelper;
import com.zifang.util.core.pattern.composite.tree.LeafWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类解析器 提供更加方便的信息抓取工具
 *
 * @author zifang
 */
public class ClassParser {

    /**
     * 当前针对的解析的类
     */
    private final Class<?> clazz;
    /**
     * 解析class类
     */
    private final LeafWrapper<Long, Long, ClassParserInfoWrapper> leafWrapper;
    /**
     * 解析过程使用的参变量
     */
    private Long leafIndex;

    /**
     * @param clazz 需要解析的类
     */
    public ClassParser(Class<?> clazz) {

        this.clazz = clazz;

        leafWrapper = doParser();
    }

    /**
     * 判断当前解析的类是否是普通的类（非接口、非抽象类、非枚举、非数组、非注解、非合成类、非原始类型）
     *
     * @return 是否是普通类
     */
    public boolean isNormalClass() {
        return null != clazz //
                && !clazz.isInterface() //
                && !Modifier.isAbstract(clazz.getModifiers()) //
                && !clazz.isEnum() //
                && !clazz.isArray() //
                && !clazz.isAnnotation() //
                && !clazz.isSynthetic() //
                && !clazz.isPrimitive();//
    }

    /**
     * 获取当前类的所有public字段
     *
     * @return public字段列表
     */
    public List<Field> getCurrentPublicField() {
        return getCurrentAllField().stream().filter(e -> Modifier.isPublic(e.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 获取当前类的所有protected字段
     *
     * @return protected字段列表
     */
    public List<Field> getCurrentProtectedField() {
        return getCurrentAllField().stream().filter(e -> Modifier.isProtected(e.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 获取当前类的所有private字段
     *
     * @return private字段列表
     */
    public List<Field> getCurrentPrivateField() {
        return getCurrentAllField().stream().filter(e -> Modifier.isPrivate(e.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 获取当前类的所有字段（不含父类字段）
     *
     * @return 当前类所有声明字段列表
     */
    public List<Field> getCurrentAllField() {
        return Arrays.asList(clazz.getDeclaredFields());
    }

    /**
     * 获取当前类的所有protected方法
     *
     * @return protected方法列表
     */
    public List<Method> getCurrentProtectedMethod() {
        return getCurrentAllMethod().stream().filter(e -> Modifier.isProtected(e.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 获取当前类的所有public方法
     *
     * @return public方法列表
     */
    public List<Method> getCurrentPublicMethod() {
        return getCurrentAllMethod().stream().filter(e -> Modifier.isPublic(e.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 获取当前类的所有包级别默认访问权限方法（无修饰符）
     *
     * @return 默认访问权限方法列表
     */
    public List<Method> getCurrentDefaultMethod() {
        return getCurrentAllMethod().stream().filter(e -> e.getModifiers() == 0).collect(Collectors.toList());
    }

    /**
     * 获取当前类的所有private方法
     *
     * @return private方法列表
     */
    public List<Method> getCurrentPrivateMethod() {
        return getCurrentAllMethod().stream().filter(e -> Modifier.isPrivate(e.getModifiers())).collect(Collectors.toList());
    }

    /**
     * 获取当前类的所有方法（不含父类方法）
     *
     * @return 当前类所有声明方法列表
     */
    public List<Method> getCurrentAllMethod() {
        return Arrays.asList(clazz.getDeclaredMethods());
    }


    private LeafWrapper<Long, Long, ClassParserInfoWrapper> doParser() {

        List<LeafWrapper<Long, Long, ClassParserInfoWrapper>> leafWrappers = loop(clazz, null, leafIndex);

        return LeafHelper.treeify(leafWrappers);
    }

    private List<LeafWrapper<Long, Long, ClassParserInfoWrapper>> loop(Class<?> clazz, Type type, Long parentId) {
        List<LeafWrapper<Long, Long, ClassParserInfoWrapper>> leafWrappers = new ArrayList<>();

        if (leafIndex == null) {
            leafIndex = 0L;
        }
        long current = leafIndex;
        ClassParserInfoWrapper classParserInfoWrapper = new ClassParserInfoWrapper();
        classParserInfoWrapper.setClazz(clazz);
        classParserInfoWrapper.setType(type);

        leafWrappers.add(LeafHelper.wrapper(current, parentId, classParserInfoWrapper));

        for (int i = 0; i < clazz.getInterfaces().length; i++) {
            ++leafIndex;
            leafWrappers.addAll(loop(clazz.getInterfaces()[i], clazz.getGenericInterfaces()[i], current));
        }

        if (clazz.getSuperclass() != null) {
            ++leafIndex;
            leafWrappers.addAll(loop(clazz.getSuperclass(), clazz.getGenericSuperclass(), current));
        }
        return leafWrappers;
    }

    /**
     * 获得与目标类型一致的泛型type信息
     *
     * @param matchClassType 要匹配查找的类类型
     * @return 泛型Type信息，若未找到返回null
     */
    public Type getGenericType(Class<?> matchClassType) {
        return reGne(leafWrapper, matchClassType);
    }

    private Type reGne(LeafWrapper<Long, Long, ClassParserInfoWrapper> leafWrapper, Class<?> matchClassType) {
        if (leafWrapper.getC().getClazz() == matchClassType) {
            return ((ClassParserInfoWrapper) ((LeafWrapper) leafWrapper.getParentLeaf()).getC()).getType();
        }
        if (leafWrapper.getSubLeaves() != null) {
            for (ILeaf iLeaf : leafWrapper.getSubLeaves()) {
                Type type = reGne((LeafWrapper) iLeaf, matchClassType);
                if (type != null) {
                    return type;
                }
            }
        }
        return null;
    }
}

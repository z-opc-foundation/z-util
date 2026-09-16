package com.zifang.util.source.generator.info;


import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 修饰符适配器
 * <p>
 * 用于将java.lang.reflect.Modifier中的修饰符常量转换为javaparser的Modifier对象。
 * 支持public、private、static等常见修饰符的映射转换。
 *
 * @author zifang
 * @version 1.0.0
 */
public class ModifierAdapter {

    /**
     * 修饰符映射表，从java.lang.reflect.Modifier到javaparser.ast.Modifier的转换
     */
    private static Map<Integer, com.github.javaparser.ast.Modifier> modifierMap = new LinkedHashMap<Integer, com.github.javaparser.ast.Modifier>() {
        {
            put(Modifier.PUBLIC, com.github.javaparser.ast.Modifier.publicModifier());
            put(Modifier.PRIVATE, com.github.javaparser.ast.Modifier.privateModifier());
            put(Modifier.STATIC, com.github.javaparser.ast.Modifier.staticModifier());

        }
    };

    /**
     * 获取javaparser修饰符关键字
     * <p>
     * 将java.lang.reflect.Modifier中的修饰符常量转换为javaparser.ast.Modifier.Keyword
     *
     * @param modifier java.lang.reflect.Modifier中的修饰符常量值
     * @return 对应的javaparser修饰符关键字
     */
    public static com.github.javaparser.ast.Modifier.Keyword getKeyWord(Integer modifier) {
        return modifierMap.get(modifier).getKeyword();
    }
}

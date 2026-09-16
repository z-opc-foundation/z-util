package com.zifang.util.source;

import org.junit.Test;

import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ModifierAdapter 测试
 */

/**
 * ModifierAdapterTest类。
 */
public class ModifierAdapterTest {

    @Test
    /**
     * testGetKeyWordPublic方法。
     */
    public void testGetKeyWordPublic() {
        // 测试 PUBLIC 修饰符转换
        assertNotNull(
                com.zifang.util.source.generator.info.ModifierAdapter.getKeyWord(Modifier.PUBLIC)
        );
    }

    @Test
    /**
     * testGetKeyWordPrivate方法。
     */
    public void testGetKeyWordPrivate() {
        // 测试 PRIVATE 修饰符转换
        assertNotNull(
                com.zifang.util.source.generator.info.ModifierAdapter.getKeyWord(Modifier.PRIVATE)
        );
    }

    @Test
    /**
     * testGetKeyWordStatic方法。
     */
    public void testGetKeyWordStatic() {
        // 测试 STATIC 修饰符转换
        assertNotNull(
                com.zifang.util.source.generator.info.ModifierAdapter.getKeyWord(Modifier.STATIC)
        );
    }

    @Test
    /**
     * testKeywordValues方法。
     */
    public void testKeywordValues() {
        // 验证返回的 Keyword 枚举值正确
        assertEquals(
                com.github.javaparser.ast.Modifier.Keyword.PUBLIC,
                com.zifang.util.source.generator.info.ModifierAdapter.getKeyWord(Modifier.PUBLIC)
        );
        assertEquals(
                com.github.javaparser.ast.Modifier.Keyword.PRIVATE,
                com.zifang.util.source.generator.info.ModifierAdapter.getKeyWord(Modifier.PRIVATE)
        );
        assertEquals(
                com.github.javaparser.ast.Modifier.Keyword.STATIC,
                com.zifang.util.source.generator.info.ModifierAdapter.getKeyWord(Modifier.STATIC)
        );
    }
}
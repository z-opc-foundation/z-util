package com.zifang.util.yaml;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * BeautifyYamlUtilsTest类。
 */

/**
 * BeautifyYamlUtilsTest类。
 */
public class BeautifyYamlUtilsTest {

    @Test
    /**
     * testBeautify_SimpleYaml方法。
     */
    /**
     * testBeautify_SimpleYaml方法。
     */
    public void testBeautify_SimpleYaml() {
        String input = "name: test";
        String result = BeautifyYamlUtils.beautify(input);
        assertNotNull(result);
        assertTrue(result.contains("name:"));
    }

    @Test
    /**
     * testBeautify_Null方法。
     */
    /**
     * testBeautify_Null方法。
     */
    public void testBeautify_Null() {
        assertNull(BeautifyYamlUtils.beautify(null));
    }

    @Test
    /**
     * testBeautify_Empty方法。
     */
    /**
     * testBeautify_Empty方法。
     */
    public void testBeautify_Empty() {
        assertEquals("", BeautifyYamlUtils.beautify(""));
    }

    @Test
    /**
     * testMinify_SimpleYaml方法。
     */
    /**
     * testMinify_SimpleYaml方法。
     */
    public void testMinify_SimpleYaml() {
        String input = "name: test\n\n\n\nage: 18";
        String result = BeautifyYamlUtils.minify(input);
        assertNotNull(result);
        assertFalse(result.contains("\n\n\n"));
    }

    @Test
    /**
     * testMinify_Null方法。
     */
    /**
     * testMinify_Null方法。
     */
    public void testMinify_Null() {
        assertNull(BeautifyYamlUtils.minify(null));
    }

    @Test
    /**
     * testSortKeys_SimpleMap方法。
     */
    /**
     * testSortKeys_SimpleMap方法。
     */
    public void testSortKeys_SimpleMap() {
        String input = "zebra: 1\napple: 2\nbanana: 3";
        String result = BeautifyYamlUtils.sortKeys(input);
        assertNotNull(result);
        // 键应该按字母顺序排列
        int appleIdx = result.indexOf("apple");
        int bananaIdx = result.indexOf("banana");
        int zebraIdx = result.indexOf("zebra");
        assertTrue(appleIdx < bananaIdx);
        assertTrue(bananaIdx < zebraIdx);
    }

    @Test
    /**
     * testSortKeys_NestedMap方法。
     */
    /**
     * testSortKeys_NestedMap方法。
     */
    public void testSortKeys_NestedMap() {
        String input = "z:\n  b: 1\n  a: 2";
        String result = BeautifyYamlUtils.sortKeys(input);
        assertNotNull(result);
        // 嵌套的键也应该排序
        assertTrue(result.indexOf("a:") < result.indexOf("b:"));
    }

    @Test
    /**
     * testSortKeys_Null方法。
     */
    /**
     * testSortKeys_Null方法。
     */
    public void testSortKeys_Null() {
        assertNull(BeautifyYamlUtils.sortKeys(null));
    }
}

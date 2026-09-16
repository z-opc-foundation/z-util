package com.zifang.util.parser.ini;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * INI 解析器测试
 */

/**
 * IniParserTest类。
 */
public class IniParserTest {

    private IniParser parser;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        parser = new IniParser();
    }

    @Test
    /**
     * testBasicSectionAndKeyValue方法。
     */
    public void testBasicSectionAndKeyValue() {
        String ini = "[section1]\nkey1=value1\nkey2=value2\n";
        IniFile file = parser.parse(ini);

        assertEquals(1, file.getSections().size());
        IniSection section = file.getSection("section1");
        assertNotNull(section);
        assertEquals("value1", section.get("key1"));
        assertEquals("value2", section.get("key2"));
    }

    @Test
    /**
     * testGlobalKeyValue方法。
     */
    public void testGlobalKeyValue() {
        String ini = "globalKey=globalValue\n[section1]\nkey1=value1\n";
        IniFile file = parser.parse(ini);

        assertEquals(2, file.getSections().size());
        IniSection global = file.getSections().get(0);
        assertNull(global.getName());
        assertEquals("globalValue", global.get("globalKey"));
    }

    @Test
    /**
     * testMultipleSections方法。
     */
    public void testMultipleSections() {
        String ini = "[section1]\nkey1=value1\n[section2]\nkey2=value2\nkey3=value3\n[section3]\nkey4=value4\n";
        IniFile file = parser.parse(ini);

        assertEquals(3, file.getSections().size());
        assertNotNull(file.getSection("section1"));
        assertNotNull(file.getSection("section2"));
        assertNotNull(file.getSection("section3"));
        assertEquals("value1", file.getSection("section1").get("key1"));
        assertEquals("value2", file.getSection("section2").get("key2"));
        assertEquals("value3", file.getSection("section2").get("key3"));
        assertEquals("value4", file.getSection("section3").get("key4"));
    }

    @Test
    /**
     * testCommentLines方法。
     */
    public void testCommentLines() {
        String ini = "; 这是注释行\n# 这也是注释行\n[section1]\nkey1=value1\n";
        IniFile file = parser.parse(ini);

        assertEquals(1, file.getSections().size());
        assertNotNull(file.getSection("section1"));
        assertEquals("value1", file.getSection("section1").get("key1"));
    }

    @Test
    /**
     * testInlineComment方法。
     */
    public void testInlineComment() {
        String ini = "[section1]\nkey1=value1 # 这是inline注释\nkey2=value2 ;这也是inline注释\n";
        IniFile file = parser.parse(ini);

        assertEquals("value1", file.getSection("section1").get("key1"));
        assertEquals("value2", file.getSection("section1").get("key2"));
    }

    @Test
    /**
     * testLineContinuation方法。
     */
    public void testLineContinuation() {
        String ini = "[section1]\nkey1=value1 \\\nvalue2\nkey3=value3\n";
        IniFile file = parser.parse(ini);

        assertEquals("value1 value2", file.getSection("section1").get("key1"));
        assertEquals("value3", file.getSection("section1").get("key3"));
    }

    @Test
    /**
     * testEmptySectionName方法。
     */
    public void testEmptySectionName() {
        String ini = "[]\nkey1=value1\n";
        IniFile file = parser.parse(ini);

        assertEquals(1, file.getSections().size());
        IniSection emptySection = file.getSections().get(0);
        assertEquals("", emptySection.getName());
        assertEquals("value1", emptySection.get("key1"));
    }

    @Test
    /**
     * testEmptyLinesIgnored方法。
     */
    public void testEmptyLinesIgnored() {
        String ini = "[section1]\n\n\nkey1=value1\n\n\n[section2]\n\nkey2=value2\n\n\n";
        IniFile file = parser.parse(ini);

        assertEquals(2, file.getSections().size());
        assertEquals("value1", file.getSection("section1").get("key1"));
        assertEquals("value2", file.getSection("section2").get("key2"));
    }

    @Test
    /**
     * testDuplicateKeyOverwrite方法。
     */
    public void testDuplicateKeyOverwrite() {
        String ini = "[section1]\nkey1=value1\nkey1=value2\nkey1=value3\n";
        IniFile file = parser.parse(ini);

        assertEquals("value3", file.getSection("section1").get("key1"));
    }

    @Test
    /**
     * testStoreAndParseRoundTrip方法。
     */
    public void testStoreAndParseRoundTrip() {
        String ini = "[section1]\nkey1=value1\nkey2=value2\n\n[section2]\nglobalKey=globalValue\nkey3=value3\n";
        IniFile file = parser.parse(ini);
        String stored = parser.store(file);
        IniFile parsedAgain = parser.parse(stored);

        assertEquals("value1", parsedAgain.getSection("section1").get("key1"));
        assertEquals("value2", parsedAgain.getSection("section1").get("key2"));
        assertEquals("value3", parsedAgain.getSection("section2").get("key3"));
    }

    @Test
    /**
     * testStoreGlobalSectionRoundTrip方法。
     */
    public void testStoreGlobalSectionRoundTrip() {
        String ini = "globalKey=globalValue\n[section1]\nkey1=value1\n";
        IniFile file = parser.parse(ini);
        String stored = parser.store(file);
        IniFile parsedAgain = parser.parse(stored);

        assertEquals("globalValue", parsedAgain.getSections().get(0).get("globalKey"));
        assertEquals("value1", parsedAgain.getSection("section1").get("key1"));
    }

    @Test(expected = IniException.class)
    /**
     * testInvalidLineThrowsException方法。
     */
    public void testInvalidLineThrowsException() {
        String ini = "[section1]\ninvalid line without equals sign\n";
        parser.parse(ini);
    }
}

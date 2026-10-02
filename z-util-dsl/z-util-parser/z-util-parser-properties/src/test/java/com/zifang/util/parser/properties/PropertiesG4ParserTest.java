package com.zifang.util.parser.properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * PropertiesG4Parser 烟雾测试：验证 G4 DSL 路径能正确产出 PropertiesModel。
 */
public class PropertiesG4ParserTest {

    private final PropertiesG4Parser parser = new PropertiesG4Parser();

    @Test
    public void testSimpleKeyValue() {
        PropertiesModel model = parser.parse("key1=value1\nkey2=value2");
        assertNotNull(model);
        assertEquals("value1", model.getProperty("key1"));
        assertEquals("value2", model.getProperty("key2"));
    }

    @Test
    public void testColonSeparator() {
        PropertiesModel model = parser.parse("key:value");
        assertEquals("value", model.getProperty("key"));
    }

    @Test
    public void testComment() {
        PropertiesModel model = parser.parse("# this is a comment\nkey=value");
        assertEquals("value", model.getProperty("key"));
    }

    @Test
    public void testEmpty() {
        PropertiesModel model = parser.parse("");
        assertNotNull(model);
    }
}

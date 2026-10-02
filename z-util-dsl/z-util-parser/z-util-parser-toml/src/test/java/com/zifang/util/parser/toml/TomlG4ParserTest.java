package com.zifang.util.parser.toml;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * TomlG4Parser 烟雾测试：验证 G4 DSL 路径能正确产出 TomlDocument。
 */
public class TomlG4ParserTest {

    private final TomlG4Parser parser = new TomlG4Parser();

    @Test
    public void testSimpleKeyValue() {
        TomlDocument doc = parser.parse("title = \"Hello\"\ncount = 42");
        assertNotNull(doc);
        assertEquals("Hello", doc.getRootTable().getString("title"));
        assertEquals(42, doc.getRootTable().getInteger("count").intValue());
    }

    @Test
    public void testSection() {
        TomlDocument doc = parser.parse("[server]\nhost = \"localhost\"\nport = 8080");
        assertNotNull(doc);
        assertEquals("localhost", doc.getTable("server").getString("host"));
        assertEquals(8080, doc.getTable("server").getInteger("port").intValue());
    }

    @Test
    public void testBoolean() {
        TomlDocument doc = parser.parse("flag = true\noff = false");
        assertEquals(true, doc.getRootTable().getBoolean("flag"));
        assertEquals(false, doc.getRootTable().getBoolean("off"));
    }

    @Test
    public void testEmpty() {
        TomlDocument doc = parser.parse("");
        assertNotNull(doc);
    }
}

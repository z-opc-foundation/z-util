package com.zifang.util.parser.ini;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * IniG4Parser 烟雾测试：验证 G4 DSL 路径能正确产出 IniFile。
 */
public class IniG4ParserTest {

    private final IniG4Parser parser = new IniG4Parser();

    @Test
    public void testSection() {
        IniFile file = parser.parse("[server]\nhost=localhost\nport=8080");
        assertNotNull(file);
        assertEquals(1, file.getSections().size());
        assertEquals("localhost", file.getSection("server").get("host"));
        assertEquals("8080", file.getSection("server").get("port"));
    }

    @Test
    public void testMultipleSections() {
        IniFile file = parser.parse("[a]\nx=1\n\n[b]\ny=2");
        assertEquals(2, file.getSections().size());
        assertEquals("1", file.getSection("a").get("x"));
        assertEquals("2", file.getSection("b").get("y"));
    }

    @Test
    public void testComment() {
        IniFile file = parser.parse("; comment\n# also comment\nkey=value");
        assertNotNull(file);
    }

    @Test
    public void testInlineComment() {
        IniFile file = parser.parse("key=value ; inline\nkey2=ok");
        assertNotNull(file);
    }
}

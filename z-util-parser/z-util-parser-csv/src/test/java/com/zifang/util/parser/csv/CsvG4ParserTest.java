package com.zifang.util.parser.csv;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * CsvG4Parser 烟雾测试：验证 G4 DSL 路径能正确产出 List<String[]>。
 */
public class CsvG4ParserTest {

    private final CsvG4Parser parser = new CsvG4Parser();

    @Test
    public void testSimpleRows() {
        List<String[]> rows = parser.parse("a,b,c\n1,2,3\nx,y,z");
        assertEquals(3, rows.size());
        assertEquals("a", rows.get(0)[0]);
        assertEquals("c", rows.get(0)[2]);
        assertEquals("3", rows.get(1)[2]);
        assertEquals("z", rows.get(2)[2]);
    }

    @Test
    public void testQuotedField() {
        List<String[]> rows = parser.parse("name,desc\n\"hello, world\",x");
        assertEquals(2, rows.size());
        assertEquals("hello, world", rows.get(1)[0]);
    }

    @Test
    public void testEscapedQuote() {
        List<String[]> rows = parser.parse("col\n\"he said \"\"hi\"\"\"");
        assertEquals(2, rows.size());
        assertEquals("he said \"hi\"", rows.get(1)[0]);
    }

    @Test
    public void testEmpty() {
        List<String[]> rows = parser.parse("");
        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }
}

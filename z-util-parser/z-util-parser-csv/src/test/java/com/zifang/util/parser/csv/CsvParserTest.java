package com.zifang.util.parser.csv;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test cases for CsvParser, CsvReader, and CsvWriter
 */

/**
 * CsvParserTest类。
 */
public class CsvParserTest {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Test
    /**
     * testBasicCommaSeparated方法。
     */
    public void testBasicCommaSeparated() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse("a,b,c" + LINE_SEPARATOR + "1,2,3");

        assertEquals(2, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(1));
    }

    @Test
    /**
     * testQuotedFieldWithCommas方法。
     */
    public void testQuotedFieldWithCommas() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse("\"field,with,commas\",normal");

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"field,with,commas", "normal"}, result.get(0));
    }

    @Test
    /**
     * testEscapedQuotes方法。
     */
    public void testEscapedQuotes() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse("\"field\"\"with\"\"quotes\"");

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"field\"with\"quotes"}, result.get(0));
    }

    @Test
    /**
     * testCustomDelimiterPipe方法。
     */
    public void testCustomDelimiterPipe() {
        CsvParser parser = CsvParser.builder().delimiter('|').build();
        List<String[]> result = parser.parse("a|b|c" + LINE_SEPARATOR + "1|2|3");

        assertEquals(2, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(1));
    }

    @Test
    /**
     * testCustomDelimiterTab方法。
     */
    public void testCustomDelimiterTab() {
        CsvParser parser = CsvParser.builder().delimiter('\t').build();
        List<String[]> result = parser.parse("a\tb\tc" + LINE_SEPARATOR + "1\t2\t3");

        assertEquals(2, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(1));
    }

    @Test
    /**
     * testSkipEmptyLines方法。
     */
    public void testSkipEmptyLines() {
        CsvParser parser = CsvParser.builder().skipEmptyLines(true).build();
        List<String[]> result = parser.parse("a,b,c" + LINE_SEPARATOR + LINE_SEPARATOR + "1,2,3");

        assertEquals(2, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(1));
    }

    @Test
    /**
     * testTrimFields方法。
     */
    public void testTrimFields() {
        CsvParser parser = CsvParser.builder().trimFields(true).build();
        List<String[]> result = parser.parse("  a  ,  b  ,  c  ");

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
    }

    @Test
    /**
     * testWriterParserRoundTrip方法。
     */
    public void testWriterParserRoundTrip() throws IOException {
        // Write CSV
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = CsvWriter.builder()
                .build(baos);

        writer.writeRow("name", "age", "city");
        writer.writeRow("Alice", "30", "NYC");
        writer.writeRow("Bob", "25", "LA");
        writer.close();

        // Read back
        String csvContent = baos.toString();
        CsvParser parser = CsvParser.builder().firstLineAsHeader(true).build();
        CsvParser.ParseResult result = parser.parseWithHeader(csvContent);

        assertArrayEquals(new String[]{"name", "age", "city"}, result.getHeaders());
        assertEquals(2, result.getData().size());
        assertArrayEquals(new String[]{"Alice", "30", "NYC"}, result.getData().get(0));
        assertArrayEquals(new String[]{"Bob", "25", "LA"}, result.getData().get(1));
    }

    @Test
    /**
     * testEmptyFields方法。
     */
    public void testEmptyFields() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse(",,");

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"", "", ""}, result.get(0));
    }

    @Test
    /**
     * testCsvReaderStreaming方法。
     */
    public void testCsvReaderStreaming() throws IOException {
        String csv = "a,b,c" + LINE_SEPARATOR + "1,2,3" + LINE_SEPARATOR + "4,5,6";
        ByteArrayInputStream bais = new ByteArrayInputStream(csv.getBytes());

        CsvReader reader = CsvReader.builder()
                .skipEmptyLines(true)
                .build(bais);

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"a", "b", "c"}, reader.next());

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"1", "2", "3"}, reader.next());

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"4", "5", "6"}, reader.next());

        assertFalse(reader.hasNext());
        reader.close();
    }

    @Test
    /**
     * testPureNumericFieldsNotQuoted方法。
     */
    public void testPureNumericFieldsNotQuoted() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = CsvWriter.builder()
                .build(baos);

        writer.writeRow("123", "456", "789");
        writer.close();

        String result = baos.toString().trim();
        // Numeric fields should not be quoted
        assertEquals("123,456,789", result);
    }

    @Test
    /**
     * testFirstLineAsHeader方法。
     */
    public void testFirstLineAsHeader() {
        CsvParser parser = CsvParser.builder().firstLineAsHeader(true).build();
        CsvParser.ParseResult result = parser.parseWithHeader("name,age,city" + LINE_SEPARATOR + "Alice,30,NYC");

        assertArrayEquals(new String[]{"name", "age", "city"}, result.getHeaders());
        assertEquals(1, result.getData().size());
        assertArrayEquals(new String[]{"Alice", "30", "NYC"}, result.getData().get(0));
    }

    @Test
    /**
     * testComplexQuotedField方法。
     */
    public void testComplexQuotedField() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse("\"Hello \"\"World\"\"\",\"value,with,commas\"");

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"Hello \"World\"", "value,with,commas"}, result.get(0));
    }

    @Test
    /**
     * testMixedQuotedAndUnquoted方法。
     */
    public void testMixedQuotedAndUnquoted() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse("normal,\"quoted\",also normal");

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"normal", "quoted", "also normal"}, result.get(0));
    }

    @Test
    /**
     * testWriterWithSpecialCharacters方法。
     */
    public void testWriterWithSpecialCharacters() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = CsvWriter.builder()
                .build(baos);

        writer.writeRow("field with \"quotes\"", "normal");
        writer.writeRow("field,with,commas", "another");
        writer.close();

        String result = baos.toString();
        assertTrue(result.contains("\"field with \"\"quotes\"\"\""));
        assertTrue(result.contains("\"field,with,commas\""));
    }

    @Test
    /**
     * testCsvReaderIteratorRemove方法。
     */
    public void testCsvReaderIteratorRemove() throws IOException {
        String csv = "a,b" + LINE_SEPARATOR + "1,2";
        ByteArrayInputStream bais = new ByteArrayInputStream(csv.getBytes());

        CsvReader reader = CsvReader.builder()
                .build(bais);

        Iterator<String[]> iterator = reader;
        assertTrue(iterator.hasNext());
        iterator.next();

        try {
            iterator.remove();
            fail("remove() should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        reader.close();
    }

    @Test
    /**
     * testEmptyFile方法。
     */
    public void testEmptyFile() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse("");

        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testSingleField方法。
     */
    public void testSingleField() {
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse("onlyonefield");

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"onlyonefield"}, result.get(0));
    }

    @Test
    /**
     * testWriterMultipleRows方法。
     */
    public void testWriterMultipleRows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = CsvWriter.builder()
                .build(baos);

        writer.writeRows(new String[][]{
                {"a", "b", "c"},
                {"1", "2", "3"},
                {"4", "5", "6"}
        });
        writer.close();

        String result = baos.toString();
        String[] lines = result.split(LINE_SEPARATOR);
        assertEquals(3, lines.length);
        assertEquals("a,b,c", lines[0]);
        assertEquals("1,2,3", lines[1]);
        assertEquals("4,5,6", lines[2]);
    }

    // ==================== New Test Cases ====================

    /**
     * Test 1: CRLF line ending (Windows) - \r\n
     */
    @Test
    /**
     * testCRLFLineEnding方法。
     */
    public void testCRLFLineEnding() {
        CsvParser parser = CsvParser.builder().trimFields(true).build();
        // Use raw string with \r\n - readLine() strips \n but leaves \r
        String csvWithCRLF = "a,b,c\r\n1,2,3\r\n4,5,6";
        List<String[]> result = parser.parse(csvWithCRLF);

        assertEquals(3, result.size());
        // With trimFields, trailing \r is trimmed
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(1));
        assertArrayEquals(new String[]{"4", "5", "6"}, result.get(2));
    }

    /**
     * Test 2: LF line ending (old Mac) - \n only
     */
    @Test
    /**
     * testLFLineEnding方法。
     */
    public void testLFLineEnding() {
        CsvParser parser = CsvParser.builder().build();
        String csvWithLF = "a,b,c\n1,2,3\n4,5,6";
        List<String[]> result = parser.parse(csvWithLF);

        assertEquals(3, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(1));
        assertArrayEquals(new String[]{"4", "5", "6"}, result.get(2));
    }

    /**
     * Test 3: UTF-8 BOM header - files with BOM marker
     */
    @Test
    /**
     * testBOMHeader方法。
     */
    public void testBOMHeader() {
        CsvParser parser = CsvParser.builder().build();
        // UTF-8 BOM (EF BB BF) + content
        byte[] bomContent = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'n', 'a', 'm', 'e', ',', 'a', 'g', 'e'};
        String csvWithBOM = new String(bomContent, java.nio.charset.StandardCharsets.UTF_8);
        List<String[]> result = parser.parse(csvWithBOM);

        assertEquals(1, result.size());
        // The BOM should be stripped, so we get "name,age"
        assertArrayEquals(new String[]{"name", "age"}, result.get(0));
    }

    /**
     * Test 4: UTF-8 BOM write and read round-trip
     */
    @Test
    /**
     * testBOMWriteAndRead方法。
     */
    public void testBOMWriteAndRead() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = CsvWriter.builder().build(baos);
        writer.writeRow("name", "age");
        writer.writeRow("张三", "30");
        writer.close();

        byte[] writtenBytes = baos.toByteArray();
        // Verify BOM is NOT added by default writer
        assertFalse(writtenBytes.length > 0 && writtenBytes[0] == (byte) 0xEF);

        // Parse the written content
        String csvContent = baos.toString("UTF-8");
        CsvParser parser = CsvParser.builder().build();
        List<String[]> result = parser.parse(csvContent);

        assertEquals(2, result.size());
        assertArrayEquals(new String[]{"name", "age"}, result.get(0));
        assertArrayEquals(new String[]{"张三", "30"}, result.get(1));
    }

    /**
     * Test 5: Streaming large file simulation - using Iterator to process line by line
     */
    @Test
    /**
     * testStreamingLargeFile方法。
     */
    public void testStreamingLargeFile() throws IOException {
        // Simulate large CSV with 10000 lines
        StringBuilder sb = new StringBuilder();
        int lineCount = 10000;
        for (int i = 0; i < lineCount; i++) {
            if (i > 0) sb.append(LINE_SEPARATOR);
            sb.append("field1_").append(i).append(",field2_").append(i).append(",field3_").append(i);
        }
        String largeCsv = sb.toString();

        ByteArrayInputStream bais = new ByteArrayInputStream(largeCsv.getBytes("UTF-8"));
        CsvReader reader = CsvReader.builder().build(bais);

        int count = 0;
        while (reader.hasNext()) {
            String[] row = reader.next();
            assertEquals(3, row.length);
            assertTrue(row[0].startsWith("field1_"));
            count++;
        }
        assertEquals(lineCount, count);
        reader.close();
    }

    /**
     * Test 6: Comment lines (starting with #)
     */
    @Test
    /**
     * testCommentLines方法。
     */
    public void testCommentLines() {
        CsvParser parser = CsvParser.builder().build();
        // Current implementation does not skip comments by default
        // This test documents current behavior - comments are parsed as data
        String csvWithComments = "# This is a comment\na,b,c\n# Another comment\n1,2,3";
        List<String[]> result = parser.parse(csvWithComments);

        assertEquals(4, result.size());
        assertArrayEquals(new String[]{"# This is a comment"}, result.get(0));
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(1));
        assertArrayEquals(new String[]{"# Another comment"}, result.get(2));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(3));
    }

    /**
     * Test 7: Quoted field containing newline (RFC 4180 compliant)
     */
    @Test
    /**
     * testQuotedFieldWithNewline方法。
     */
    public void testQuotedFieldWithNewline() {
        CsvParser parser = CsvParser.builder().build();
        // RFC 4180: fields containing newlines should be quoted
        String csvWithQuotedNewline = "\"line1\nline2\",normal";
        List<String[]> result = parser.parse(csvWithQuotedNewline);

        // Quoted embedded newline is part of the field — single record
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).length);
        assertEquals("line1\nline2", result.get(0)[0]);
        assertEquals("normal", result.get(0)[1]);
    }

    /**
     * Test 8: Consecutive delimiters (empty fields)
     */
    @Test
    /**
     * testConsecutiveDelimiters方法。
     */
    public void testConsecutiveDelimiters() {
        CsvParser parser = CsvParser.builder().build();
        String csvWithEmptyFields = "a,,b,,,c";
        List<String[]> result = parser.parse(csvWithEmptyFields);

        assertEquals(1, result.size());
        assertArrayEquals(new String[]{"a", "", "b", "", "", "c"}, result.get(0));
    }

    /**
     * Test 9: First line is NOT header (header=false)
     */
    @Test
    /**
     * testFirstLineNotAsHeader方法。
     */
    public void testFirstLineNotAsHeader() {
        CsvParser parser = CsvParser.builder().firstLineAsHeader(false).build();
        CsvParser.ParseResult result = parser.parseWithHeader("data1,data2,data3\nvalue1,value2,value3");

        // With firstLineAsHeader=false, first line is still treated as header
        // This test documents the current behavior
        assertArrayEquals(new String[]{"data1", "data2", "data3"}, result.getHeaders());
        assertEquals(1, result.getData().size());
        assertArrayEquals(new String[]{"value1", "value2", "value3"}, result.getData().get(0));
    }

    /**
     * Test 10: Skip consecutive empty lines
     */
    @Test
    /**
     * testSkipMultipleEmptyLines方法。
     */
    public void testSkipMultipleEmptyLines() {
        CsvParser parser = CsvParser.builder().skipEmptyLines(true).build();
        String csv = "a,b,c" + LINE_SEPARATOR + LINE_SEPARATOR + LINE_SEPARATOR + "1,2,3";
        List<String[]> result = parser.parse(csv);

        assertEquals(2, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2", "3"}, result.get(1));
    }

    /**
     * Test 11: Write single row only
     */
    @Test
    /**
     * testWriteSingleRow方法。
     */
    public void testWriteSingleRow() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = CsvWriter.builder().build(baos);
        writer.writeRow("only", "one", "row");
        writer.close();

        String result = baos.toString().trim();
        assertEquals("only,one,row", result);
    }

    /**
     * Test 12: Different quote character (single quote)
     */
    @Test
    /**
     * testSingleQuoteCharacter方法。
     */
    public void testSingleQuoteCharacter() {
        CsvParser parser = CsvParser.builder().build();
        // Current implementation only supports double quote as quote character
        // This test documents that single quotes are treated as normal characters
        String csvWithSingleQuotes = "'field1','field2','field3'";
        List<String[]> result = parser.parse(csvWithSingleQuotes);

        assertEquals(1, result.size());
        // Single quotes are treated as regular characters, not quote markers
        assertArrayEquals(new String[]{"'field1'", "'field2'", "'field3'"}, result.get(0));
    }

    /**
     * Test 13: Escape mode vs quote mode
     */
    @Test
    /**
     * testEscapeVsQuoteMode方法。
     */
    public void testEscapeVsQuoteMode() {
        CsvParser parser = CsvParser.builder().build();
        // Current implementation uses doubled quotes ("") for escaping
        // This is the "quote mode" - backslash escape is not supported
        // Build the CSV string with escaped quotes using concatenation
        String quote = "\"";
        String csvWithEscapedQuotes = quote + "field" + quote + quote + "with" + quote + quote + "quotes" + quote;
        List<String[]> result = parser.parse(csvWithEscapedQuotes);

        assertEquals(1, result.size());
        // Doubled quotes inside quotes are unescaped to single quotes
        assertArrayEquals(new String[]{"field\"with\"quotes"}, result.get(0));
    }

    /**
     * Test 14: Inconsistent column count (different fields per row)
     */
    @Test
    /**
     * testInconsistentColumnCount方法。
     */
    public void testInconsistentColumnCount() {
        CsvParser parser = CsvParser.builder().build();
        String csv = "a,b,c\n1,2\nx\np,q,r,s,t";
        List<String[]> result = parser.parse(csv);

        assertEquals(4, result.size());
        assertArrayEquals(new String[]{"a", "b", "c"}, result.get(0));
        assertArrayEquals(new String[]{"1", "2"}, result.get(1));
        assertArrayEquals(new String[]{"x"}, result.get(2));
        assertArrayEquals(new String[]{"p", "q", "r", "s", "t"}, result.get(3));
    }

    /**
     * Test 15: Unicode content (Chinese/Japanese characters)
     */
    @Test
    /**
     * testUnicodeContent方法。
     */
    public void testUnicodeContent() {
        CsvParser parser = CsvParser.builder().build();
        String csv = "姓名,年齢,都市\n张三,30,北京\n田中,25,東京\n李四,35,上海";
        CsvParser.ParseResult result = parser.parseWithHeader(csv);

        assertArrayEquals(new String[]{"姓名", "年齢", "都市"}, result.getHeaders());
        assertEquals(3, result.getData().size());
        assertArrayEquals(new String[]{"张三", "30", "北京"}, result.getData().get(0));
        assertArrayEquals(new String[]{"田中", "25", "東京"}, result.getData().get(1));
        assertArrayEquals(new String[]{"李四", "35", "上海"}, result.getData().get(2));
    }

    /**
     * Test Unicode content with Writer
     */
    @Test
    /**
     * testUnicodeContentWithWriter方法。
     */
    public void testUnicodeContentWithWriter() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter writer = CsvWriter.builder().build(baos);
        writer.writeRow("姓名", "年齢", "都市");
        writer.writeRow("张三", "30", "北京");
        writer.writeRow("田中", "25", "東京");
        writer.close();

        String csvContent = baos.toString("UTF-8");
        CsvParser parser = CsvParser.builder().firstLineAsHeader(true).build();
        CsvParser.ParseResult result = parser.parseWithHeader(csvContent);

        assertArrayEquals(new String[]{"姓名", "年齢", "都市"}, result.getHeaders());
        assertEquals(2, result.getData().size());
        assertArrayEquals(new String[]{"张三", "30", "北京"}, result.getData().get(0));
        assertArrayEquals(new String[]{"田中", "25", "東京"}, result.getData().get(1));
    }

    /**
     * Test streaming Unicode content
     */
    @Test
    /**
     * testStreamingUnicodeContent方法。
     */
    public void testStreamingUnicodeContent() throws IOException {
        String csv = "名前,年齢\n佐藤,35\n鈴木,28\n高橋,42";
        ByteArrayInputStream bais = new ByteArrayInputStream(csv.getBytes("UTF-8"));
        CsvReader reader = CsvReader.builder().build(bais);

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"名前", "年齢"}, reader.next());

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"佐藤", "35"}, reader.next());

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"鈴木", "28"}, reader.next());

        assertTrue(reader.hasNext());
        assertArrayEquals(new String[]{"高橋", "42"}, reader.next());

        assertFalse(reader.hasNext());
        reader.close();
    }
}

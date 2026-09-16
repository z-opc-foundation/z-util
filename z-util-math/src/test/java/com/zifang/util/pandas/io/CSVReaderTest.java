package com.zifang.util.pandas.io;

import com.zifang.util.pandas.DataFrame;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * CSVReader 测试
 */

/**
 * CSVReaderTest类。
 */
public class CSVReaderTest {

    @Test
    /**
     * testReadFromString方法。
     */
    public void testReadFromString() throws IOException {
        String csv = "name,age,score\nAlice,25,95.5\nBob,30,88.0\nCharlie,22,92.3";
        CSVReader reader = CSVReader.builder().hasHeader(true);
        DataFrame df = reader.readFromString(csv);

        assertEquals(3, df.nRows());
        assertEquals(3, df.nCols());
        assertTrue(df.columns().contains("name"));
        assertTrue(df.columns().contains("age"));
        assertTrue(df.columns().contains("score"));
    }

    @Test
    /**
     * testReadWithoutHeader方法。
     */
    public void testReadWithoutHeader() throws IOException {
        String csv = "Alice,25,95.5\nBob,30,88.0";
        CSVReader reader = CSVReader.builder().hasHeader(false);
        DataFrame df = reader.readFromString(csv);

        assertEquals(1, df.nRows());
        assertEquals(3, df.nCols());
    }

    @Test
    /**
     * testReadWithCustomDelimiter方法。
     */
    public void testReadWithCustomDelimiter() throws IOException {
        String csv = "name;age;score\nAlice;25;95.5\nBob;30;88.0";
        CSVReader reader = CSVReader.builder()
                .hasHeader(true)
                .delimiter(';');
        DataFrame df = reader.readFromString(csv);

        assertEquals(2, df.nRows());
        assertEquals(3, df.nCols());
    }

    @Test
    /**
     * testReadWithSkipRows方法。
     */
    public void testReadWithSkipRows() throws IOException {
        String csv = "header line\nname,age,score\nAlice,25,95.5\nBob,30,88.0";
        CSVReader reader = CSVReader.builder()
                .hasHeader(true)
                .skipRows(1);
        DataFrame df = reader.readFromString(csv);

        assertEquals(2, df.nRows());
        assertEquals(3, df.nCols());
    }

    @Test
    /**
     * testReadWithMaxRows方法。
     */
    public void testReadWithMaxRows() throws IOException {
        String csv = "name,age,score\nAlice,25,95.5\nBob,30,88.0\nCharlie,22,92.3";
        CSVReader reader = CSVReader.builder()
                .hasHeader(true)
                .maxRows(2);
        DataFrame df = reader.readFromString(csv);

        assertEquals(2, df.nRows());
    }

    @Test
    /**
     * testReadWithEmptyLines方法。
     */
    public void testReadWithEmptyLines() throws IOException {
        String csv = "name,age,score\nAlice,25,95.5\n\nBob,30,88.0\n";
        CSVReader reader = CSVReader.builder()
                .hasHeader(true);
        DataFrame df = reader.readFromString(csv);

        assertEquals(2, df.nRows());
    }

    @Test
    /**
     * testReadWithQuotedFields方法。
     */
    public void testReadWithQuotedFields() throws IOException {
        String csv = "name,city,score\n\"Alice\",\"New York\",95.5\n\"Bob\",\"Los Angeles\",88.0";
        CSVReader reader = CSVReader.builder().hasHeader(true);
        DataFrame df = reader.readFromString(csv);

        assertEquals(2, df.nRows());
        assertEquals(3, df.nCols());
    }

    @Test
    /**
     * testReadWithEscapedQuotes方法。
     */
    public void testReadWithEscapedQuotes() throws IOException {
        String csv = "name,city\n\"Alice\",\"\"\"NYC\"\"\" \nBob,Chicago";
        CSVReader reader = CSVReader.builder().hasHeader(true);
        DataFrame df = reader.readFromString(csv);

        assertEquals(2, df.nRows());
    }

    @Test
    /**
     * testReadWithNaNValues方法。
     */
    public void testReadWithNaNValues() throws IOException {
        String csv = "name,age,score\nAlice,25,95.5\nBob,abc,88.0\nCharlie,22,";
        CSVReader reader = CSVReader.builder().hasHeader(true);
        DataFrame df = reader.readFromString(csv);

        assertEquals(3, df.nRows());
        assertTrue(Double.isNaN(df.get("age").get(1)));
        assertTrue(Double.isNaN(df.get("score").get(2)));
    }

    @Test
    /**
     * testBuilderFluentInterface方法。
     */
    public void testBuilderFluentInterface() {
        CSVReader reader = CSVReader.builder()
                .delimiter(';')
                .quoteChar('\'')
                .hasHeader(false)
                .encoding("UTF-8")
                .skipRows(2)
                .maxRows(100);

        assertNotNull(reader);
    }
}
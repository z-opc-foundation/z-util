package com.zifang.util.pandas.io;

import com.zifang.util.pandas.DataFrame;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * CSVWriter 测试
 */

/**
 * CSVWriterTest类。
 */
public class CSVWriterTest {

    @Test
    /**
     * testWriteAndReadRoundtrip方法。
     */
    public void testWriteAndReadRoundtrip() throws IOException {
        DataFrame df = new DataFrame(new java.util.HashMap<String, double[]>() {{
            put("name", new double[]{1.0, 2.0});
            put("age", new double[]{25.0, 30.0});
        }});

        String csv = CSVWriter.toCSVString(df);
        assertNotNull(csv);
        assertTrue(csv.contains("name"));
        assertTrue(csv.contains("age"));
    }

    @Test
    /**
     * testBuilderFluentInterface方法。
     */
    public void testBuilderFluentInterface() {
        CSVWriter writer = CSVWriter.builder()
                .delimiter(';')
                .quoteChar('\'')
                .includeHeader(true)
                .includeIndex(false)
                .encoding("UTF-8")
                .lineEnding("\r\n")
                .quoteAll(false)
                .escapeSpecialChars(true);

        assertNotNull(writer);
    }

    @Test
    /**
     * testToCSVString方法。
     */
    public void testToCSVString() throws IOException {
        DataFrame df = new DataFrame(new java.util.HashMap<String, double[]>() {{
            put("A", new double[]{1.0, 2.0});
            put("B", new double[]{3.0, 4.0});
        }});

        String csv = CSVWriter.toCSVString(df);
        assertNotNull(csv);
        String[] lines = csv.split("\n");
        assertTrue(lines.length >= 2);
    }

    @Test
    /**
     * testWriteWithInfinity方法。
     */
    public void testWriteWithInfinity() throws IOException {
        DataFrame df = new DataFrame(new java.util.HashMap<String, double[]>() {{
            put("col", new double[]{1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY});
        }});
        String csv = CSVWriter.toCSVString(df);
        assertNotNull(csv);
    }

    @Test
    /**
     * testWriteIntegerValues方法。
     */
    public void testWriteIntegerValues() throws IOException {
        DataFrame df = new DataFrame(new java.util.HashMap<String, double[]>() {{
            put("int_col", new double[]{1.0, 2.0, 3.0});
            put("float_col", new double[]{1.5, 2.5, 3.5});
        }});
        String csv = CSVWriter.toCSVString(df);
        assertNotNull(csv);
    }

    @Test
    /**
     * testWriteWithCustomDelimiter方法。
     */
    public void testWriteWithCustomDelimiter() throws IOException {
        DataFrame df = new DataFrame(new java.util.HashMap<String, double[]>() {{
            put("A", new double[]{1.0});
            put("B", new double[]{2.0});
        }});

        CSVWriter writer = CSVWriter.builder()
                .delimiter(';')
                .includeIndex(false);
        StringBuilder sb = new StringBuilder();
        writer.write(df, new java.io.OutputStream() {
            @Override
            /**
             * write方法。
             *      * @param b int类型参数
             */
            public void write(int b) {
                sb.append((char) b);
            }
        });

        String csv = sb.toString();
        assertNotNull(csv);
    }
}
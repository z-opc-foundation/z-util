package com.zifang.util.office.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * U Word文档操作工具类的单元测试
 */

/**
 * UTest类。
 */
public class UTest {

    @Test
    /**
     * testAddText方法。
     */
    public void testAddText() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = U.addText(paragraph, "Hello World");

            assertNotNull(run);
            assertTrue(paragraph.getText().contains("Hello World"));
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testAddTextWithSize方法。
     */
    public void testAddTextWithSize() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = U.addText(paragraph, "Sized Text", 16);

            assertNotNull(run);
            assertTrue(run.getFontSize() > 0); // Verify font size was set
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testAddTextCenter方法。
     */
    public void testAddTextCenter() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            U.addTextCenter(paragraph, "Centered Text");

            assertNotNull(paragraph.getAlignment());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testAddTextRight方法。
     */
    public void testAddTextRight() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            U.addTextRight(paragraph, "Right Text");

            assertNotNull(paragraph.getAlignment());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testAddTextList方法。
     */
    public void testAddTextList() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            List<String> textList = Arrays.asList("Line 1", "Line 2", "Line 3");
            XWPFRun run = U.addText(paragraph, textList);

            assertNotNull(run);
            assertTrue(paragraph.getText().contains("Line 1"));
            assertTrue(paragraph.getText().contains("Line 2"));
            assertTrue(paragraph.getText().contains("Line 3"));
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testAddLoopText方法。
     */
    public void testAddLoopText() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            U.addLoopText(paragraph, "Repeated", 3);

            String text = paragraph.getText();
            // Text should contain "Repeated" at least 3 times
            assertTrue("Text should contain 'Repeated'", text.contains("Repeated"));
            assertEquals(3, paragraph.getRuns().size());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testCreateHeading1方法。
     */
    public void testCreateHeading1() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            U.createHeading1(paragraph, "Heading 1 Title");

            assertEquals("Heading 1", paragraph.getStyle());
            assertTrue(paragraph.getText().contains("Heading 1 Title"));
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testCreateHeading2方法。
     */
    public void testCreateHeading2() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            U.createHeading2(paragraph, "Heading 2 Title");

            assertEquals("Heading 2", paragraph.getStyle());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testHexToBytes方法。
     */
    public void testHexToBytes() {
        byte[] result = U.hexToBytes("4288BC");

        assertNotNull(result);
        assertEquals(3, result.length);
    }

    @Test
    /**
     * testInitialStyles方法。
     */
    public void testInitialStyles() {
        try (XWPFDocument document = new XWPFDocument()) {
            U.initialStyles(document);

            // Verify styles are created
            assertNotNull(document.getStyles());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testMergeCellsHorizontal方法。
     */
    public void testMergeCellsHorizontal() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            document.createTable(); // Create a table first
            XWPFTable table = document.createTable();

            // Ensure table has enough cells
            table.getRow(0).getCell(0).setText("Cell 0");
            table.getRow(0).addNewTableCell().setText("Cell 1");
            table.getRow(0).addNewTableCell().setText("Cell 2");

            U.mergeCellsHorizontal(table, 0, 0, 1);
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }

    @Test
    /**
     * testMergeCellsVertically方法。
     */
    public void testMergeCellsVertically() {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFTable table = document.createTable();

            // Ensure table has enough rows
            table.getRow(0).getCell(0).setText("Cell 0");
            table.createRow().getCell(0).setText("Cell 1");
            table.createRow().getCell(0).setText("Cell 2");

            U.mergeCellsVertically(table, 0, 0, 2);
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
    }
}
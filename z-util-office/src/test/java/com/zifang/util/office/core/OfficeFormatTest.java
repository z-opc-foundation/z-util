package com.zifang.util.office.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * OfficeFormat.detect 的往返测试：四种格式全部内存构建 → 逐个识别。
 */
public class OfficeFormatTest {

    @Test
    public void detect_docx() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText("d");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.write(out);
            assertEquals(OfficeFormat.Format.DOCX,
                    OfficeFormat.detect(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    @Test
    public void detect_xlsx() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            wb.createSheet("s");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            assertEquals(OfficeFormat.Format.XLSX,
                    OfficeFormat.detect(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    @Test
    public void detect_pptx() throws Exception {
        try (XMLSlideShow show = new XMLSlideShow()) {
            show.createSlide();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            show.write(out);
            assertEquals(OfficeFormat.Format.PPTX,
                    OfficeFormat.detect(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    @Test
    public void detect_pdf() throws Exception {
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            assertEquals(OfficeFormat.Format.PDF,
                    OfficeFormat.detect(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    @Test
    public void detect_ole2() throws Exception {
        byte[] head = new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, 0, 0, 0, 0};
        assertEquals(OfficeFormat.Format.OLE2, OfficeFormat.detect(new ByteArrayInputStream(head)));
    }

    @Test
    public void detect_unknown() throws Exception {
        assertEquals(OfficeFormat.Format.UNKNOWN,
                OfficeFormat.detect(new ByteArrayInputStream("plain text".getBytes())));
    }
}

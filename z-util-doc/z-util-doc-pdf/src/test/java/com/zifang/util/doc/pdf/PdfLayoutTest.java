package com.zifang.util.doc.pdf;

import com.zifang.util.doc.DocDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 版面拼接：行内片段按位置排序、只在真实空档处补空格；图形状态栈不影响解析。 */
class PdfLayoutTest {

    private final PdfDocReader reader = new PdfDocReader();

    @Test
    void runsConcatenatedAndBigGapGetsSpace() throws Exception {
        byte[] pdf = TestPdfs.positionedRuns(new String[]{"A", "B", "C", "D"},
                new double[]{72, 90, 108, 200});
        DocDocument doc = reader.read(pdf);
        String text = doc.toPlainText();
        assertTrue(text.contains("ABC D"), text);
    }

    @Test
    void graphicsStateStackWithoutFontIsSafe() throws Exception {
        byte[] pdf = TestPdfs.withGraphicsState("q BT /F1 12 Tf 72 700 Td (Hello) Tj ET Q\\n");
        DocDocument doc = reader.read(pdf);
        assertEquals("Hello", doc.toPlainText().strip());
    }
}

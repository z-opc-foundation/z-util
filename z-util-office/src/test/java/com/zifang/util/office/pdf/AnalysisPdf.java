package com.zifang.util.office.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * AnalysisPdf类。
 * <p>
 * pdfbox 3.x API：使用 {@link Loader#loadPDF(File)} 直接返回 PDDocument，
 * 不再需要 PDFParser + RandomAccessBuffer。
 */
public class AnalysisPdf {
    /**
     * main方法。
     *
     * @param args String[]类型参数
     */
    public static void main(String[] args) throws IOException {
        String output = "/Users/zifang/Downloads/t.pdf";

        try (PDDocument document = Loader.loadPDF(new File(output))) {
            PDFTextStripper stripper = new PDFTextStripper();
            //设置输出顺序（是否排序）
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());
            //文本内容
            String text = stripper.getText(document);
            System.out.println(text);
        }

        System.out.println("'");
    }
}

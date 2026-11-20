package com.zifang.util.doc.xls;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import com.zifang.util.doc.DocFormat;
import com.zifang.util.doc.DocFormatDetector;
import com.zifang.util.doc.DocParseException;
import com.zifang.util.doc.DocReader;
import com.zifang.util.doc.DocSpan;
import com.zifang.util.doc.ole.CfbReader;

import java.io.IOException;
import java.util.List;

/**
 * Excel 97-2003（BIFF8 二进制 .xls）读取器。零第三方依赖：OLE/CFB 容器解析 + BIFF 记录流自研实现。
 * 覆盖：多工作表、共享字符串、公式缓存值、数字/布尔/错误、日期格式、隐藏工作表。
 */
public final class XlsDocReader implements DocReader {

    private static final String WORKBOOK_STREAM = "Workbook";
    private static final String WORKBOOK_STREAM_LEGACY = "Book";

    @Override
    public DocFormat format() {
        return DocFormat.XLS;
    }

    @Override
    public boolean supports(byte[] data) {
        if (!DocFormatDetector.isOle2(data)) {
            return isRawBiffStream(data);
        }
        try {
            CfbReader cfb = new CfbReader(data);
            return cfb.hasStream(WORKBOOK_STREAM) || cfb.hasStream(WORKBOOK_STREAM_LEGACY);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** BIFF2-4 可能是没有 OLE 外壳的裸记录流（首记录即 BOF）。 */
    private static boolean isRawBiffStream(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        int type = (data[0] & 0xFF) | ((data[1] & 0xFF) << 8);
        return type == 0x0809 || type == 0x0409 || type == 0x0209 || type == 0x0009;
    }

    @Override
    public DocDocument read(byte[] data) throws IOException {
        byte[] stream;
        if (DocFormatDetector.isOle2(data)) {
            CfbReader cfb = new CfbReader(data);
            stream = cfb.readStream(WORKBOOK_STREAM);
            if (stream == null) {
                stream = cfb.readStream(WORKBOOK_STREAM_LEGACY);
            }
            if (stream == null) {
                throw new DocParseException("不是 xls：缺少 Workbook 流");
            }
        } else {
            stream = data;
        }
        BiffWorkbook book = BiffWorkbook.parse(stream);
        DocDocument doc = new DocDocument();
        for (BiffWorkbook.Sheet sheet : book.sheets()) {
            doc.add(new DocBlock.Sheet(sheet.name, sheet.rows, sheet.hidden));
            // 批注以 [批注 作者] 列引用 Quote 块挂在表后（与 docx 模块口径一致）
            for (BiffWorkbook.CellComment comment : sheet.comments) {
                String label = comment.author() == null || comment.author().isBlank()
                        ? "[批注] "
                        : "[批注 " + comment.author() + "] ";
                doc.add(new DocBlock.Quote(List.of(DocSpan.plain(
                        label + cellRef(comment.row(), comment.col()) + ": " + comment.text()))));
            }
        }
        return doc;
    }

    /** 行列 → Excel 引用（0 起行列 → A1 风格）。 */
    private static String cellRef(int row, int col) {
        StringBuilder sb = new StringBuilder();
        int c = col;
        do {
            sb.insert(0, (char) ('A' + c % 26));
            c = c / 26 - 1;
        } while (c >= 0);
        return sb.append(row + 1).toString();
    }
}

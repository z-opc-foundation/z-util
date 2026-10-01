package com.zifang.util.office.ppt;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PPT 模板引擎（v0）：扫描每页文本框里的占位符并替换。
 *
 * <p>支持：
 * <ul>
 *   <li>{@code ${var}} —— 文本框内简单变量替换</li>
 *   <li>{@code ${list[*]}} —— 占位符文本框复制 N 个，每个填入列表的逐项</li>
 * </ul>
 */
public class PptTemplate {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^${}]+)\\}");

    /**
     * 把模板流渲染后写入输出流。**不关闭任何流**。
     *
     * @param templateIn 模板 .pptx 输入流
     * @param out        输出流
     * @param model      数据模型
     * @throws IOException 读 / 写失败
     */
    public static void render(InputStream templateIn, OutputStream out, Map<String, Object> model) throws IOException {
        if (templateIn == null) {
            throw new IllegalArgumentException("templateIn must not be null");
        }
        if (out == null) {
            throw new IllegalArgumentException("out must not be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        try (XMLSlideShow show = new XMLSlideShow(templateIn)) {
            renderInPlace(show, model);
            show.write(out);
        }
    }

    /**
     * 在已有的演示文稿上原地渲染占位符。
     *
     * @param show  演示文稿
     * @param model 数据模型
     */
    public static void renderInPlace(XMLSlideShow show, Map<String, Object> model) {
        if (show == null) {
            throw new IllegalArgumentException("show must not be null");
        }
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        List<ListAction> listActions = new ArrayList<>();
        for (XSLFSlide slide : show.getSlides()) {
            java.util.List<org.apache.poi.xslf.usermodel.XSLFShape> shapes = slide.getShapes();
            for (org.apache.poi.xslf.usermodel.XSLFShape shape : shapes) {
                if (!(shape instanceof XSLFTextBox)) {
                    continue;
                }
                XSLFTextBox box = (XSLFTextBox) shape;
                String text = collectText(box);
                if (text == null || text.isEmpty()) {
                    continue;
                }
                Matcher m = PLACEHOLDER.matcher(text);
                if (!m.find()) {
                    continue;
                }
                String key = m.group(1).trim();
                String head = text.substring(0, m.start());
                String tail = text.substring(m.end());
                if (key.endsWith("[*]")) {
                    listActions.add(new ListAction(box, slide, key.substring(0, key.length() - 3)));
                    setText(box, head + tail);
                } else {
                    Object v = model.get(key);
                    setText(box, head + (v == null ? "" : v.toString()) + tail);
                }
            }
        }
        for (ListAction a : listActions) {
            Object v = model.get(a.base);
            if (!(v instanceof List)) {
                continue;
            }
            List<?> list = (List<?>) v;
            setText(a.box, "");
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    setText(a.box, toText(list.get(i)));
                } else {
                    XSLFTextBox copy = a.slide.createTextBox();
                    copy.setText(toText(list.get(i)));
                }
            }
        }
    }

    private static String toText(Object v) {
        return v == null ? "" : v.toString();
    }

    private static String collectText(XSLFTextBox box) {
        StringBuilder sb = new StringBuilder();
        for (XSLFTextParagraph p : box.getTextParagraphs()) {
            for (XSLFTextRun r : p.getTextRuns()) {
                sb.append(r.getRawText());
            }
        }
        return sb.toString();
    }

    private static void setText(XSLFTextBox box, String text) {
        if (box.getTextParagraphs().isEmpty()) {
            box.addNewTextParagraph().addNewTextRun().setText(text);
            return;
        }
        XSLFTextParagraph first = box.getTextParagraphs().get(0);
        // 清空：保留第一个 run 用于写入新文本；后续全部移除
        while (first.getTextRuns().size() > 1) {
            XSLFTextRun last = first.getTextRuns().get(first.getTextRuns().size() - 1);
            first.removeTextRun(last);
        }
        if (first.getTextRuns().isEmpty()) {
            first.addNewTextRun().setText(text);
        } else {
            first.getTextRuns().get(0).setText(text);
        }
    }

    /**
     * 构造一个最小占位符模型。
     */
    public static Map<String, Object> model() {
        return new LinkedHashMap<>();
    }

    private static final class ListAction {
        final XSLFTextBox box;
        final XSLFSlide slide;
        final String base;

        ListAction(XSLFTextBox box, XSLFSlide slide, String base) {
            this.box = box;
            this.slide = slide;
            this.base = base;
        }
    }
}

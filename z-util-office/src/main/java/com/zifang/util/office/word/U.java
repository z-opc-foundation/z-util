package com.zifang.util.office.word;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Word文档操作工具类
 * 提供Word文档的创建、编辑、格式化等常用操作
 *
 * @author zifang
 * @version 1.0
 */
public class U {

    public static Integer WIDTH = 9072;


    /**
     * addText方法。
     * * @param p1 XWPFParagraph类型参数
     *
     * @param s String类型参数
     * @param i int类型参数
     * @return static XWPFRun类型返回值
     */
    public static XWPFRun addText(XWPFParagraph p1, String s, int i) {
        XWPFRun titleParagraphRun = addText(p1, s);
        titleParagraphRun.setFontSize(i);
        return titleParagraphRun;
    }

    /**
     * 向段落中添加文本
     *
     * @param titleParagraph 目标段落
     * @param s              要添加的文本内容
     * @return 创建的文本运行对象
     */
    public static XWPFRun addText(XWPFParagraph titleParagraph, String s) {
        XWPFRun titleParagraphRun = titleParagraph.createRun();
        titleParagraphRun.addCarriageReturn();
        titleParagraphRun.setText(s);
        return titleParagraphRun;
    }

    /**
     * 向段落中添加居中对齐的文本
     *
     * @param titleParagraph 目标段落
     * @param s              要添加的文本内容
     * @return 创建的文本运行对象
     */
    public static XWPFRun addTextCenter(XWPFParagraph titleParagraph, String s) {
        XWPFRun titleParagraphRun = titleParagraph.createRun();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        titleParagraphRun.setText(s);
        return titleParagraphRun;
    }

    /**
     * 向段落中添加右对齐的文本
     *
     * @param titleParagraph 目标段落
     * @param s              要添加的文本内容
     * @return 创建的文本运行对象
     */
    public static XWPFRun addTextRight(XWPFParagraph titleParagraph, String s) {
        XWPFRun titleParagraphRun = titleParagraph.createRun();
        titleParagraph.setAlignment(ParagraphAlignment.RIGHT);
        titleParagraphRun.setText(s);
        return titleParagraphRun;
    }

    /**
     * 向段落中添加多行文本列表
     *
     * @param titleParagraph 目标段落
     * @param textList       要添加的文本列表
     * @return 创建的文本运行对象
     */
    public static XWPFRun addText(XWPFParagraph titleParagraph, List<String> textList) {
        XWPFRun titleParagraphRun = titleParagraph.createRun();
        titleParagraphRun.addCarriageReturn();
        for (String text : textList) {
            titleParagraphRun.setText(text);
            titleParagraphRun.addCarriageReturn();
        }
        return titleParagraphRun;
    }

    /**
     * 向段落中添加循环重复的文本
     *
     * @param titleParagraph 目标段落
     * @param s              要重复添加的文本内容
     * @param loopTimes      重复次数
     */
    public static void addLoopText(XWPFParagraph titleParagraph, String s, Integer loopTimes) {
        for (Integer i = 0; i < loopTimes; i++) {
            XWPFRun titleParagraphRun = titleParagraph.createRun();
            titleParagraphRun.addCarriageReturn();
            titleParagraphRun.setText(s);
        }
    }

    /**
     * 创建一级标题
     *
     * @param paragraph 目标段落
     * @param title     标题文本
     */
    public static void createHeading1(XWPFParagraph paragraph, String title) {
        paragraph.setStyle("Heading 1");
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);//标题加粗
    }

    /**
     * 创建二级标题
     *
     * @param paragraph 目标段落
     * @param title     标题文本
     */
    public static void createHeading2(XWPFParagraph paragraph, String title) {
        paragraph.setStyle("Heading 2");
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);//标题加粗
    }

    /**
     * 创建三级标题
     *
     * @param paragraph 目标段落
     * @param title     标题文本
     */
    public static void createHeading3(XWPFParagraph paragraph, String title) {
        paragraph.setStyle("Heading 3");
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);//标题加粗
    }


    /**
     * 创建四级标题
     *
     * @param paragraph 目标段落
     * @param title     标题文本
     */
    public static void createHeading4(XWPFParagraph paragraph, String title) {
        paragraph.setStyle("Heading 4");
        XWPFRun run = paragraph.createRun();
        run.setText(title);
        run.setBold(true);//标题加粗
    }

    /**
     * 向段落中添加图片
     *
     * @param paragraph 目标段落
     * @param path      图片文件路径
     * @throws Throwable 如果文件不存在或图片格式错误则抛出异常
     */
    public static void createGraph(XWPFParagraph paragraph, String path) throws Throwable {
        if (!new File(path).exists()) {
            throw new Exception("文件不存在啊:" + path);
        }
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        try {
            run.addPicture(new FileInputStream(path), XWPFDocument.PICTURE_TYPE_JPEG, path, Units.toEMU(400), Units.toEMU(300));
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Throwable(e);
        }
    }

    /**
     * 添加自定义标题样式
     *
     * @param docxDocument Word文档对象
     * @param strStyleId   样式ID
     * @param headingLevel 标题级别
     */
    public static void addCustomHeadingStyle(XWPFDocument docxDocument, String strStyleId, int headingLevel) {

        CTStyle ctStyle = CTStyle.Factory.newInstance();
        ctStyle.setStyleId(strStyleId);

        CTString styleName = CTString.Factory.newInstance();
        styleName.setVal(strStyleId);
        ctStyle.setName(styleName);

        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
        indentNumber.setVal(BigInteger.valueOf(headingLevel)); // lower number > style is more prominent in the formats bar
        ctStyle.setUiPriority(indentNumber);

        CTOnOff onoffnull = CTOnOff.Factory.newInstance();
        ctStyle.setUnhideWhenUsed(onoffnull); // style shows up in the formats bar
        ctStyle.setQFormat(onoffnull); // style defines a heading of the given level
        CTPPrGeneral ppr = CTPPrGeneral.Factory.newInstance();
        ppr.setOutlineLvl(indentNumber);
        ctStyle.setPPr(ppr);

        XWPFStyle style = new XWPFStyle(ctStyle); // is a null op if already defined
        XWPFStyles styles = docxDocument.createStyles();

        style.setType(STStyleType.PARAGRAPH);
        styles.addStyle(style);

    }

    /**
     * 添加自定义标题样式（带字体大小和颜色配置）
     *
     * @param docxDocument Word文档对象
     * @param styles       样式集合
     * @param strStyleId   样式ID
     * @param headingLevel 标题级别
     * @param pointSize    字体大小（磅）
     * @param hexColor     十六进制颜色值，如"4288BC"
     */
    public static void addCustomHeadingStyle(XWPFDocument docxDocument, XWPFStyles styles, String strStyleId, int headingLevel, int pointSize, String hexColor) {

        CTStyle ctStyle = CTStyle.Factory.newInstance();
        ctStyle.setStyleId(strStyleId);


        CTString styleName = CTString.Factory.newInstance();
        styleName.setVal(strStyleId);
        ctStyle.setName(styleName);

        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
        indentNumber.setVal(BigInteger.valueOf(headingLevel));

        // lower number > style is more prominent in the formats bar
        ctStyle.setUiPriority(indentNumber);

        CTOnOff onoffnull = CTOnOff.Factory.newInstance();
        ctStyle.setUnhideWhenUsed(onoffnull);

        // style shows up in the formats bar
        ctStyle.setQFormat(onoffnull);

        // style defines a heading of the given level
        CTPPrGeneral ppr = CTPPrGeneral.Factory.newInstance();
        ppr.setOutlineLvl(indentNumber);
        ctStyle.setPPr(ppr);

        XWPFStyle style = new XWPFStyle(ctStyle);

        CTHpsMeasure size = CTHpsMeasure.Factory.newInstance();
        size.setVal(new BigInteger(String.valueOf(pointSize)));

        CTHpsMeasure size2 = CTHpsMeasure.Factory.newInstance();
        size2.setVal(new BigInteger("24"));

        CTFonts fonts = CTFonts.Factory.newInstance();
        fonts.setAscii("Loma");


        CTRPr rpr = CTRPr.Factory.newInstance();
        rpr.setRFontsArray(new CTFonts[]{fonts});
        rpr.setSzArray(new CTHpsMeasure[]{size});
        rpr.setSzCsArray(new CTHpsMeasure[]{size2});


        CTColor color = CTColor.Factory.newInstance();
        color.setVal(hexToBytes(hexColor));
        rpr.setColorArray(new CTColor[]{color});
        style.getCTStyle().setRPr(rpr);
        // is a null op if already defined

        style.setType(STStyleType.PARAGRAPH);
        styles.addStyle(style);

    }

    /**
     * 将十六进制颜色字符串转换为字节数组
     *
     * @param hexString 十六进制颜色字符串，如"4288BC"
     * @return 转换后的字节数组
     */
    public static byte[] hexToBytes(String hexString) {
        HexBinaryAdapter adapter = new HexBinaryAdapter();
        byte[] bytes = adapter.unmarshal(hexString);
        return bytes;
    }

    /**
     * 初始化文档的样式定义
     * 设置1-4级标题的样式（字体大小、颜色等）
     *
     * @param document Word文档对象
     */
    public static void initialStyles(XWPFDocument document) {
        XWPFStyles styles = document.createStyles();
        //对document 进行处理
        U.addCustomHeadingStyle(document, styles, "Heading 1", 1, 44, "4288BC");
        U.addCustomHeadingStyle(document, styles, "Heading 2", 2, 32, "4288BC");
        U.addCustomHeadingStyle(document, styles, "Heading 3", 3, 32, "4288BC");
        U.addCustomHeadingStyle(document, styles, "Heading 4", 4, 32, "000000");
    }

    /**
     * 向文档中添加简单的数据表格
     *
     * @param document    Word文档对象
     * @param list        表头列表，每项为键值对(key为表头名称，value为数据字段名)
     * @param dataMapList 数据列表，每项为一个Map对象
     */
    public static void addSimpleTable(XWPFDocument document, List<Tuple<String, String>> list, List<Map<String, Object>> dataMapList) {
        //基本信息表格
        XWPFTable infoTable = document.createTable();


        CTTblBorders borders = infoTable.getCTTbl().getTblPr().addNewTblBorders();
        CTBorder hBorder = borders.addNewInsideH();
        hBorder.setVal(STBorder.Enum.forString("single"));
        hBorder.setSz(new BigInteger("1"));
        hBorder.setColor("000000");

        CTBorder vBorder = borders.addNewInsideV();
        vBorder.setVal(STBorder.Enum.forString("single"));
        vBorder.setSz(new BigInteger("1"));
        vBorder.setColor("000000");

        CTBorder lBorder = borders.addNewLeft();
        lBorder.setVal(STBorder.Enum.forString("single"));
        lBorder.setSz(new BigInteger("1"));
        lBorder.setColor("000000");

        CTBorder rBorder = borders.addNewRight();
        rBorder.setVal(STBorder.Enum.forString("single"));
        rBorder.setSz(new BigInteger("1"));
        rBorder.setColor("000000");

        CTBorder tBorder = borders.addNewTop();
        tBorder.setVal(STBorder.Enum.forString("single"));
        tBorder.setSz(new BigInteger("1"));
        tBorder.setColor("000000");

        CTBorder bBorder = borders.addNewBottom();
        bBorder.setVal(STBorder.Enum.forString("single"));
        bBorder.setSz(new BigInteger("1"));
        bBorder.setColor("000000");

        CTTblWidth infoTableWidth = infoTable.getCTTbl().addNewTblPr().addNewTblW();
        infoTableWidth.setType(STTblWidth.DXA);
        infoTableWidth.setW(BigInteger.valueOf(9072));

        //header 处理“
        for (int i = 0; i < list.size(); i++) {
            XWPFTableRow xwpfTableRow = infoTable.getRow(0);
            if (i == 0) {
                xwpfTableRow.getCell(0).setText(list.get(i).getKey());
            } else {
                xwpfTableRow.addNewTableCell().setText(list.get(i).getKey());
            }
        }

        for (Map<String, Object> data : dataMapList) {
            XWPFTableRow xwpfTableRow = infoTable.createRow();

            for (int i = 0; i < list.size(); i++) {

                if (i == 0) {
                    xwpfTableRow.getCell(0).setText(data.get(list.get(i).getValue()).toString());
                } else {
                    try {
                        Object o = data.get(list.get(i).getValue());
                        if (String.valueOf(o).endsWith(".0")) {
                            xwpfTableRow.getCell(i).setText(String.valueOf(o).split("[.]")[0]);
                        } else if (o instanceof Double) {
                            xwpfTableRow.getCell(i).setText(String.valueOf(Double.valueOf(o.toString()).doubleValue()));
                        } else if (o instanceof Integer) {
                            xwpfTableRow.getCell(i).setText(String.valueOf(Integer.valueOf(o.toString())));
                        } else {
                            xwpfTableRow.getCell(i).setText(String.valueOf(data.get(list.get(i).getValue())));

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 向文档中添加简单的数据表格（支持自定义列宽）
     * 支持调节行内各自间距
     *
     * @param document    Word文档对象
     * @param list        表头列表，每项为键值对(key为表头名称，value为数据字段名)
     * @param dataMapList 数据列表，每项为一个Map对象
     * @param dimention   列宽映射，key为列索引，value为宽度值
     */
    public static void addSimpleTable(XWPFDocument document, List<Tuple<String, String>> list, List<Map<String, Object>> dataMapList, Map<Integer, String> dimention) {
        //基本信息表格
        XWPFTable infoTable = document.createTable();

        CTTblWidth infoTableWidth = infoTable.getCTTbl().addNewTblPr().addNewTblW();
        //infoTableWidth.setType(STTblWidth.DXA);
        infoTableWidth.setW(BigInteger.valueOf(9072));

        //header 处理“
        for (int i = 0; i < list.size(); i++) {
            XWPFTableRow xwpfTableRow = infoTable.getRow(0);
            if (i == 0) {
                xwpfTableRow.getCell(0).setText(list.get(i).getKey());
                xwpfTableRow.getCell(0).setWidth(dimention.get(0));
            } else {
                XWPFTableCell cell = xwpfTableRow.addNewTableCell();
                cell.setText(list.get(i).getKey());
                cell.setWidth(dimention.get(i));
            }
        }

        for (Map<String, Object> data : dataMapList) {
            XWPFTableRow xwpfTableRow = infoTable.createRow();

            for (int i = 0; i < list.size(); i++) {

                if (i == 0) {
                    xwpfTableRow.getCell(0).setText(data.get(list.get(i).getValue()).toString());
                    xwpfTableRow.getCell(0).setWidth(dimention.get(i));
                } else {
                    xwpfTableRow.getCell(i).setText(data.get(list.get(i).getValue()).toString());
                    xwpfTableRow.getCell(i).setWidth(dimention.get(i));

                }
            }
        }
    }


    /**
     * 水平合并表格单元格
     *
     * @param table    目标表格
     * @param row      行索引
     * @param fromCell 起始单元格索引
     * @param toCell   结束单元格索引
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int fromCell, int toCell) {

        Integer start = null;
        Integer colNum = table.getRow(row).getTableCells().size();
        Integer toBe = U.WIDTH * (toCell - fromCell + 1) / colNum;
        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);

            if (cellIndex == fromCell) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
                start = cellIndex;
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
        table.getRow(row).getCell(start).setWidth("" + toBe);
    }

    // word跨行并单元格

    /**
     * 垂直合并表格单元格（跨行合并）
     *
     * @param table   目标表格
     * @param col     列索引
     * @param fromRow 起始行索引
     * @param toRow   结束行索引
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if (rowIndex == fromRow) {
                // The first merged cell is set with RESTART merge value
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                // Cells which join (merge) the first one, are set with CONTINUE
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    /**
     * 向表格追加数据行
     *
     * @param t_1_5_1 目标表格
     * @param mapList 数据列表，每项为一个Map对象
     */
    public static void appendingBody(XWPFTable t_1_5_1, List<Map<String, Object>> mapList) {
        for (Map<String, Object> map : mapList) {
            XWPFTableRow row = t_1_5_1.createRow();
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                row.getCell(i++).setText(entry.getValue().toString());
            }
        }
    }

    /**
     * 创建特殊的书龄销售库存表格表头
     * 该方法创建一个包含书龄、销售（品种、册数、码洋）、库存（品种、册数、码洋）的复杂表头
     *
     * @param infoTable 目标表格
     */
    public static void do_1_5_1_SpecialHeader(XWPFTable infoTable) {

        CTTblWidth infoTableWidth = infoTable.getCTTbl().addNewTblPr().addNewTblW();
        infoTableWidth.setType(STTblWidth.DXA);//9072
        infoTableWidth.setW(BigInteger.valueOf(9072));

        infoTable.getRow(0).getCell(0).setText("书龄");

        infoTable.getRow(0).getCell(1).setText("销售");
        infoTable.getRow(0).getCell(4).setText("库存");

        infoTable.getRow(1).getCell(1).setText("品种(万种)");
        infoTable.getRow(1).getCell(2).setText("册数(万册)");
        infoTable.getRow(1).getCell(3).setText("码洋(万元)");
        infoTable.getRow(1).getCell(4).setText("品种(万种)");
        infoTable.getRow(1).getCell(5).setText("册数(万册)");
        infoTable.getRow(1).getCell(6).setText("码洋(万元)");

        mergeCellsHorizontal(infoTable, 0, 1, 3);
        mergeCellsHorizontal(infoTable, 0, 4, 6);
        mergeCellsVertically(infoTable, 0, 0, 1);
    }
}

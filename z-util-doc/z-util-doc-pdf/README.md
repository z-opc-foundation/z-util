# z-util-doc-pdf

纯 Java PDF 读取器，`z-util-doc` 家族成员。**零 Apache 依赖**（不用 PDFBox），全部自己实现。

## 定位
- 入口类：`com.zifang.util.doc.pdf.PdfDocReader`（实现 `DocReader` SPI）。
- 文件骨架：`PdfParser` —— 经典 xref 表 + xref 流（PDF 1.5+ 对象流、`/Prev` 链）；加密文件直接抛 `DocParseException`。
- 过滤器：`PdfFilters` —— Flate / LZW / ASCIIHex / ASCII85 / RunLength + PNG 预测器还原。
- 内容流：`ContentExtractor` —— BT/ET、`Tj`/`TJ`/`Td`/`TD`/`Tm`/`T*` 文本算子 + `Do` 图片，产出带设备坐标的 TextPiece/ImagePiece。
- 字体：`PdfFont` —— 简单字体（1 字节码，Cp1252 兜底）与 Type0/CID（2 字节码），`/Widths`、`/W`、ToUnicode CMap（`ToUnicodeCMap`，bfchar/bfrange）。
- 版面：`LayoutAssembler` —— 字号定正文/标题（字号直方图，行数优先）、行距并段、bullet/编号识别、x 对齐简易表格、图片按页追加。

## 能力边界
- 加密 PDF 不支持；CID 字体无 ToUnicode 时提取为空。
- 表格是坐标聚类的简化版，复杂合并单元格/无线表格识别留给上层（MinerU 形态）。
- 图片只支持 DCTDecode（JPEG）原样提取，PNG/Flate 位图不解码。

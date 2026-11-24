# z-util-doc-pdf

纯 Java PDF 读取器，`z-util-doc` 家族成员。**零 Apache 依赖**（不用 PDFBox），全部自己实现。

## 定位
- 入口类：`com.zifang.util.doc.pdf.PdfDocReader`（实现 `DocReader` SPI）。
- 文件骨架：`PdfParser` —— 经典 xref 表 + xref 流（PDF 1.5+ 对象流、`/Prev` 链）；加密文件直接抛 `DocParseException`。
- 过滤器：`PdfFilters` —— Flate / LZW / ASCIIHex / ASCII85 / RunLength + PNG 预测器还原。
- 内容流：`ContentExtractor` —— BT/ET、`Tj`/`TJ`/`Td`/`TD`/`Tm`/`T*` 文本算子 + `Do` 图片，产出带设备坐标的 TextPiece/ImagePiece；
  `Do` 命中表单 XObject 时按表单自己的 /Resources 再解释一遍内容流。
- 字体：`PdfFont` —— 简单字体（1 字节码，Cp1252 兜底）、Type0/CID（2 字节码）与 Type3（按 /FontMatrix 换算字宽），`/Widths`、`/W`、ToUnicode CMap（`ToUnicodeCMap`，bfchar/bfrange）。
- 图片：`PdfImage` —— DCTDecode 原样输出 JPEG；其余滤镜解出裸采样后按色彩空间重新封装成 PNG
  （DeviceGray/DeviceRGB/DeviceCMYK/ICCBased/Indexed，CMYK 转 RGB，/SMask 补 alpha 通道，位深 1/2/4/8/16）。
- CCITT：`CcittFax` —— G3 一维（K=0）、G3 二维（K>0）与 G4（K=-1，T.6 全模式），
  支持 /EncodedByteAlign /EndOfLine /EndOfBlock /Rows /BlackIs1；位流损坏按行补白收尾。
- 版面：`LayoutAssembler` —— 字号定正文/标题（字号直方图，行数优先）、行距并段、bullet/编号识别、x 对齐简易表格、图片按页追加。

## 版面拼接规则
行内片段按 x 排序后直接拼接，词间距优先用片段自身的空格字形（PDF 里真正的 space glyph）；
没有空格字形时，用「相邻片段起点间距的中位数」当字距基准，明显的大空档才补空格或 tab；
中日韩全角文字之间不插空格。这样中文 PDF 不会出现「责 任 链 模 式」这种逐字空格。

## 能力边界
- 加密 PDF 不支持；CID 字体无 ToUnicode 时提取为空。
- 表格是坐标聚类的简化版，复杂合并单元格/无线表格识别留给上层（MinerU 形态）。
- 图片：JPXDecode（JPEG2000）、JBIG2 不解码；DIB 位图不封装。
- 内联图片（`BI...ID...EI`）跳过，只取 XObject 图片。
- 不做矢量绘图还原（路径、填充、描边）。

## 回归语料与验证
`src/test/resources/` 不放二进制语料，测试用手写最小 PDF（`TestPdfs`）构造文本、图片与遮罩场景。

验证方式：与 Apache PDFBox 3.0.8 在 10 个真实 PDF（中文课程讲义，含 Type3 字体、
图片页、多页文档）上逐项比对——
- **文本**：10 个文件的提取文本与 PDFBox 的 `PDFTextStripper` **逐行完全一致**；
- **图片**：11 张图的像素 **SHA-256 完全一致**（含 SMask alpha 通道）。

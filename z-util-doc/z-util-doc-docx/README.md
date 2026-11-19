# z-util-doc-docx

DOCX（OOXML）读取器，`z-util-doc` 家族成员，零第三方依赖（JDK Zip + DOM）。

## 定位
- 入口类：`com.zifang.util.doc.docx.DocxDocReader`（实现 `DocReader` SPI）。
- 解析 `word/document.xml`：段落/标题（pStyle 启发式）/列表（numbering.xml numFmt）/表格（gridSpan→colspan，首行 header）/图片（rels → media）。
- 行内样式：b/i/strike 与 hyperlink（r:id → rels 目标）；解析 docProps/core.xml 元数据。

## 能力边界
- 禁 DTD/外部实体（防 XXE）；vMerge 跨行合并、rStyle、sectPr/分页符不处理。
- 编造的样式名前缀无法识别时按普通段落处理。

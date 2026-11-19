# z-util-doc-define

`z-util-doc` 家族的通用层：文档模型 + Reader SPI + 魔数嗅探 + 导出器。零第三方依赖（纯 JDK）。

## 定位
- 文档模型：`DocDocument`（`DocMeta` + `List<DocBlock>`）；sealed `DocBlock` 的七种嵌套 record
  （`Paragraph` / `Heading` / `ListItem` / `Quote` / `CodeBlock` / `Table`（`Row`/`Cell`）/ `Image`）；
  行内样式 `DocSpan`（bold/italic/mono/strike/link）。
- 读取 SPI：`DocReader`（`format()` / `supports(byte[])` / `read(byte[])`），实现类在各格式子模块。
- 魔数嗅探：`DocFormatDetector`（`%PDF`、OLE2、zip 容器细分 docx/epub、RTF、HTML）。
- 导出器：`MarkdownExporter` / `PlainTextExporter` / `HtmlExporter`；门面 `DocConverter`。
- 注册表：`DocReaders`（手工 register，或 `discover()` 走 ServiceLoader）。

## 典型用法
```java
DocReaders readers = new DocReaders().register(new PdfDocReader(), new DocxDocReader());
DocDocument doc = readers.read(bytes);
String md = doc.toMarkdown();
```

## 能力边界
- 本模块不做任何格式解析，只定义模型与契约；MinerU 形态（版面/OCR/表格/公式识别管道）由上层在本家族之上组装。

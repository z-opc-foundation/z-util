# z-util-doc-txt

纯文本（txt）读取器，`z-util-doc` 家族成员，零第三方依赖。

## 定位
- 入口类：`com.zifang.util.doc.txt.TxtDocReader`（实现 `DocReader` SPI）。
- charset 自动判别：UTF-8 严格解码失败回退 GBK。
- 按空行分段为 `DocBlock.Paragraph`，段内单换行视为软换行保留；不设 pageCount。

## 能力边界
- 不支持 PDF/OLE2/zip/RTF/HTML（由 `DocFormatDetector` 排除）。
- 不识别任何富文本结构（标题、列表等需上层自行约定）。

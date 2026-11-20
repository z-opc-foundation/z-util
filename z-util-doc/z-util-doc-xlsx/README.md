# z-util-doc-xlsx

Excel 2007+（OOXML xlsx）读写器，`z-util-doc` 家族成员，**零第三方依赖**（JDK Zip + DOM，不用 POI/XSSF）。

## 定位
- 入口类：`com.zifang.util.doc.xlsx.XlsxDocReader`（实现 `DocReader` SPI）。
- 写入器：`com.zifang.util.doc.xlsx.XlsxWriter` —— 零依赖生成可被 Excel / WPS / LibreOffice 打开的工作簿。
- 命名空间无关（按 local-name 遍历），兼容 Excel / WPS / LibreOffice / Google Sheets 导出的 xlsx。

## 读能力
- **多工作表**：按 `xl/workbook.xml` 的 sheet 顺序输出，含隐藏表标记。
- **单元格类型**：共享字符串（`t="s"`）、内联字符串（`t="inlineStr"`）、数字、布尔、错误、日期/时间、
  公式（保留公式文本 + 缓存值）。
- **日期**：内置日期格式号（14-22/27-36/45-47/50-58）与自定义格式码（`yyyy-mm-dd` 等）都能识别，
  序列号按 1900 日期系统换算（含 1900-02-29 的历史偏差修正）。
- **合并单元格**：合并区左上角单元格带 colspan / rowspan，其余格不重复输出。
- **元数据**：`docProps/core.xml` 的 title / creator / subject / keywords。

## 写能力
- 字符串（走共享字符串表）、数字、布尔、日期（自定义 numFmt 164）、公式、多工作表。
- 产物结构：`[Content_Types].xml` / `_rels/.rels` / `xl/workbook.xml` / `xl/_rels/workbook.xml.rels` /
  `xl/worksheets/sheetN.xml` / `xl/styles.xml` / `xl/sharedStrings.xml` / `docProps/core.xml`。

## 能力边界
- 禁 DTD/外部实体（防 XXE）。
- 不读样式（字体/填充/边框）、条件格式、数据验证、图表、透视表、宏（xlsm）。
- 不做公式重算；读到的公式取缓存值。
- 加密工作簿（OLE 加密壳）不支持，报 `DocParseException`。

## 回归语料
`src/test/resources/corpus/` 收录 4 个真实 xlsx（xlsx-populate 的 MIT 示例产物，覆盖共享字符串、
样式、drawing、主题），外加 1 个加密工作簿作为负例。

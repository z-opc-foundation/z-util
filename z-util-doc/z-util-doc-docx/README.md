# z-util-doc-docx

DOCX（OOXML）读取器，`z-util-doc` 家族成员，零第三方依赖（JDK Zip + DOM）。

## 定位
- 入口类：`com.zifang.util.doc.docx.DocxDocReader`（实现 `DocReader` SPI）。
- 命名空间无关（按 local-name 遍历），兼容 MS Word / WPS / LibreOffice / Google Docs 导出的 docx，含 strict-format 变体与 UTF-8 BOM。

## 能力
- **正文递归遍历**：`w:sdt` 内容控件、`mc:AlternateContent`（Choice/Fallback）、`w:smartTag`、`w:ins` 修订包裹层全部解包，不会静默丢内容。
- **标题**：三重判定——pPr `outlineLvl` 优先，其次 styles.xml 的样式名（heading N / 标题 N / 数字 styleId），再次 styleId。
- **表格**：`gridSpan` → colspan、`vMerge` → rowspan（续格并入首格跨行合并）、`trPr/tblHeader` 表头行（全表无标记时首行兜底）、单元格内嵌套表格递归。
- **列表**：numbering.xml 完整建模——abstractNum 各 ilvl 的 numFmt/lvlText/start + num 的 startOverride；按文档顺序推进计数器，渲染真实编号（如 `1.2.3`、`•`）注入首 span；Markdown 导出器识别已注入标记不重复编号。
- **页眉页脚**：sectPr 的 headerReference/footerReference → rels → headerN.xml/footerN.xml，产出 `[页眉]/[页脚]` Quote 块。
- **脚注尾注**：footnotes.xml/endnotes.xml 解析；正文引用处注入 `[^n]` 标记，被引用注释文本汇总为文末 Quote。
- **文本框**：drawing/pict 内 `w:txbxContent` 段落提取为正常块（不丢文本框正文）。
- **图片三路径**：`a:blip r:embed`、VML `v:imagedata r:id`、`w:object` 内嵌图，均走 rels → media；EMU 尺寸（wp:extent）换算像素。
- **行内样式**：b/i/strike/dstrike、rFonts 等宽字体判定（Consolas/Courier/Menlo/Sarasa…）、hyperlink（外部 rel 目标与内部 `w:anchor` → `#锚点`）、tab/br/cr/noBreakHyphen。
- 元数据：docProps/core.xml（title/creator/subject/keywords/created/modified）。

## 能力边界
- 禁 DTD/外部实体（防 XXE）。
- 外部引用图片（TargetMode=External，包内无字节）不产出 Image 块。
- 域代码（fldChar/instrText）跳过，fldSimple 取其显示内容；批注（comments.xml）不读取。
- rStyle 字符样式、纵向文本方向、OMML 公式不做转换。

## 回归语料
`src/test/resources/corpus/` 收录 16 个真实 docx（mammoth 项目 MIT 测试集），覆盖表格/脚注/尾注/文本框/删除线/下划线/列表/批注/strict-format/utf8-bom/图片等特性。

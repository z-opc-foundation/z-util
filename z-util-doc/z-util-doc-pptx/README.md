# z-util-doc-pptx

PowerPoint 2007+（OOXML pptx）读取器，`z-util-doc` 家族成员，**零第三方依赖**（JDK Zip + DOM，不用 POI/XSLF）。

## 定位
- 入口类：`com.zifang.util.doc.pptx.PptxDocReader`（实现 `DocReader` SPI）。
- 命名空间无关（按 local-name 遍历），兼容 PowerPoint / WPS / LibreOffice 导出的 pptx。

## 能力
- **幻灯片顺序**：按 `ppt/presentation.xml` 的 `sldIdLst` 顺序输出（关系 id 用命名空间精确匹配，
  不会与同名的普通 `id` 属性混淆）。
- **形状文本**：标题占位符（`ph type=title/ctrTitle`）作为页标题，其余形状的段落转成正文块；
  run 级加粗 / 斜体 / 删除线按 `a:rPr` 判定。
- **组合形状**：递归展开 `p:grpSp` 里的子形状。
- **表格**：`a:tbl` 的行列与单元格文本，首行按表头处理。
- **图片**：`p:pic` → `a:blip r:embed` → slide rels → `ppt/media/*`，输出原始字节与 MIME；
  尺寸按 `a:ext` 的 EMU 换算像素（/9525）。
- **备注页**：`notesSlide` 的文字以 `[备注]` 段落输出（跳过页码占位符）。

## 能力边界
- 禁 DTD/外部实体（防 XXE）。
- 不读图表 / SmartArt / 动画 / 母版版式细节；母版与版式文字不产出（避免与幻灯片内容重复）。
- 嵌入对象（OLE）不解析；外部链接图片不产出 Image 块。

## 回归语料
`src/test/resources/corpus/` 收录 7 个真实 pptx（python-pptx 项目的 MIT 测试集，覆盖形状文本、
组合形状、表格、图片、备注页、空演示文稿）。

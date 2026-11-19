# z-util-doc-epub

EPUB 读取器，`z-util-doc` 家族成员，零第三方依赖（JDK Zip + DOM）。

## 定位
- 入口类：`com.zifang.util.doc.epub.EpubDocReader`（实现 `DocReader` SPI）。
- 解析 META-INF/container.xml → OPF（manifest/spine）→ 按 spine 顺序读 XHTML，多章节 append 进同一 DocDocument；章节首块非 Heading 时补插章节标题。
- 行内样式：em/i→italic、strong/b→bold、code→mono、a→link；支持 li（ol/ul 嵌套层级）、blockquote、pre（language-xxx）、table（th 表头、colspan/rowspan）、img（zip 内相对路径）。

## 能力边界
- 禁 DTD/外部实体（防 XXE）；XHTML 损坏的章节跳过不抛异常；图片取不到即跳过。
- 不渲染 CSS，样式仅保留语义映射。

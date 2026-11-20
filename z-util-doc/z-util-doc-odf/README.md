# z-util-doc-odf

OpenDocument 家族读取器（`.odt` / `.ods` / `.odp`），`z-util-doc` 家族成员，**零第三方依赖**
（JDK Zip 解包 + DOM 解析，不引 Apache POI）。

## 定位
- 入口类：`com.zifang.util.doc.odf.OdtDocReader` / `OdsDocReader` / `OdpDocReader`（均实现 `DocReader` SPI）。
- 共用层：`com.zifang.util.doc.odf.OdfSupport`（容器解包、命名空间无关遍历、段落文本与样式、图片提取）。
- 格式判定：读包内 `mimetype` 条目（ODF 的 mimetype 是 zip 的第一个未压缩条目）。

## 能力
- **ODT 文本**：`text:p` 段落、`text:h` 标题（按 `text:outline-level` 定级，1..6 夹取）、
  `text:list` 列表（含嵌套层级）、`table:table` 表格、`draw:frame` 图片与文本框。
- **字符样式**：`text:span` 的 `text:style-name` 指向的字符样式，解析
  `fo:font-weight` / `fo:font-style` / `style:text-line-through-style`，得到加粗、斜体、删除线；
  样式表同时取 `styles.xml` 的 `office:styles` 与 `content.xml` 的 `office:automatic-styles`，
  并沿 `style:parent-style-name` 做继承。
- **行内细节**：`text:s` 多个空格、`text:tab` 制表符、`text:line-break` 换行、`text:a` 超链接。
- **ODS 表格**：`table:table` 逐表输出（保留表名），单元格类型区分数值/百分比/货币、字符串、
  布尔、日期、时间；`table:formula` 公式（去掉 `of:` 命名空间前缀）随缓存值一起输出；
  `table:number-columns-spanned` / `table:number-rows-spanned` 记录合并跨度，
  `table:covered-table-cell` 记为被覆盖单元格。
- **行列重复**：`table:number-rows-repeated` / `table:number-columns-repeated` 展开，
  纯填充的空行整段跳过（避免把 1M 行的空表撑爆），重复展开上限 1024。
- **ODP 演示**：`draw:page` 逐页输出，`presentation:class="title"` 的形状作为页标题，
  文本框与自定义形状的文本按段落/列表输出，组合形状（`draw:g`）递归，
  `presentation:notes` 备注页以 `[备注] …` 段落附加。
- **图片**：`draw:image` 的 `xlink:href` 指向包内条目，按扩展名判定 MIME。

## 能力边界
- 不还原页眉页脚、分栏、脚注锚点、批注。
- 不解析 ODF 的自动重算（`table:formula` 只给缓存值，不做公式求值）。
- 样式只取加粗/斜体/删除线三项，不还原字号、颜色、对齐、缩进与页面布局。
- 图表（`chart:`）与嵌入对象（`draw:object`）不解析内容。

## 回归语料
`src/test/resources/corpus/` 收录 17 个真实 ODF 文件（odfpy 项目的 MIT 测试集）：
ODT 9 个（含列表、表格、样式、图片、两级列表）、ODS 4 个（含公式与中文表）、ODP 3 个（含多形状与备注）。

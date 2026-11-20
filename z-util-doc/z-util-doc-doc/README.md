# z-util-doc-doc

二进制 Word（.doc，Word 97-2003）读取器，`z-util-doc` 家族成员。**零 Apache 依赖**（不用 POI/HWPF），全部自己实现。

## 定位
- 入口类：`com.zifang.util.doc.doc.DocDocReader`（实现 `DocReader` SPI）。
- 容器：`CfbReader` —— OLE2/Compound File Binary：FAT / DIFAT / miniFAT / 目录 / 常规与 mini 流提取。
- 正文：`WordBinaryParser` —— FIB（`fWhichTblStm` 选 0Table/1Table）+ CLX piece table
  （压缩 cp1252 与 UTF-16LE 双分支），保留 `\r` 段落分隔。
  **FIB 偏移已用真实 Word 文件实测校验**（fibRgLw97 基址 64：ccpText@76、ccpFtn@80、
  ccpHdd@84、ccpAtn@92、ccpEdn@96；fcPlcfhdd@242、fcPlcfbteChpx@250、fcClx@418、
  fcPlfLst@738、fcPlfLfo@746、fcDggInfo@554）。
- 字符格式：plcfbteChpx → FKP 页 → CHPX grpprl sprm 解析（sprmCFBold 0x0835 /
  sprmCFItalic 0x0836 / sprmCFStrikeOut 0x0837，ToggleOperand 按低位判开；sprmCPicLocation 0x6A03
  取图片偏移），正文段落内按 run 叠加粗/斜/删除线 span。
- 段落属性：plcfbtePapx → PAPX FKP 页 → grpprl sprm（sprmPIstd 0x4600 / sprmPIlvl 0x260A /
  sprmPIfo 0x460B / sprmPFInTable 0x2416 / sprmPFTtp 0x2417），据此还原**标题、列表、表格**：
  - 样式表 STSH（`DocStyleSheet`）解析样式名与内置样式号，sti 1..9 或 "heading N"/"标题 N" 判定标题级别；
  - 样式自带的段落属性（UPX 里的 PAPX）沿 istdBase 继承，列表层级/列表号可以来自样式而非段落本身；
  - 表内段落按 0x07 归并成单元格、行尾标记（sprmPFTtp）收拢成行；连续行合成一张表；
  - 行尾段落的 sprmTDefTable 操作数（itcMac + rgdxaCenter 短整数 + rgTc80×20 字节）里
    TC80.rgf 的 fVertMerge/fVertRestart 还原纵向合并：续接格跳过、锚点格 rowspan 累加，
    续接格带 restart 但内容为空时也按续接处理（兼容个别写出器）。
- 列表：`DocLists` 解析 PlcfLst（cLst + LSTF 数组 + 各列表 LVL 数组；LSTF 28 字节 =
  lsid + tplc + rgistdPara + flags，简单列表 1 级、多级 9 级）与 PlcfLfo（LFO 16 字节 +
  LFOData：cp + LFOLVL 覆盖，fFormatting 整级换格式、fStartAt 换起始值）。LVL =
  LVLF(28 字节) + papx/chpx grpprl + Xst 编号文本（\u0000-\u0008 是各级计数器占位符）。
  按段落顺序推进每个 ilfo 的计数器（进入更浅层级重置更深层级，fNoRestart 除外），
  占位符按 nfc 渲染成十进制/大写小写罗马/大写小写字母/序数词，编号与项目符号文本
  作为首个 span 注入列表段落。
- 域代码：0x13/0x14/0x15 域结构整篇一次解析，**域指令丢弃、只留显示结果**
  （HYPERLINK、TOC、页码等不会混进正文）；域可以跨 run、跨段落，所以可见性按整篇文本统一计算。
- 图片：`DocPictures` 解析 OfficeArt（ESCHER）图片——绘图数据 `fcDggInfo` 里的
  DggContainer → BStoreContainer → BSE，BLIP 记录还原 PNG/JPEG/TIFF/EMF/WMF 原始字节；
  内联图片走 CHPX 的 sprmCPicLocation → Data 流 PICF → OfficeArt。
- 页眉页脚：plcfhdd 故事 CP 边界 → `[页眉]/[页脚]` Quote 块。
- 脚注 / 尾注 / 批注：按规范 CP 顺序（text→ftn→hdd→mcr→atn→edn）提取子文档文本为 Quote 块。
- 元数据：`SummaryInfoParser` —— `\u0005SummaryInformation` 属性集（title/author/subject/keywords/创建与修改时间，VT_LPSTR / VT_LPWSTR / VT_FILETIME）。

## 能力边界
- 只支持 Word 97-2003（nFib ≥ 193）；Word 6/95 的旧 FIB 布局直接抛 `DocParseException`，不做猜测解析。
- 加密 doc 不支持。
- 不还原表格的横向合并与 TAP 的列宽/边框；嵌套表格平铺进所在单元格（内部行标记不拆行）。
- 列表编号只支持常见 nfc（十进制/罗马/字母/序数词，其余兜底十进制）；不还原缩进、
  对齐与 grpprl 里的字体格式（编号文本按正文样式输出）。
- 图片挂在正文之后（绘图数据里的浮动图形没有稳定的正文锚点），不还原尺寸与位置。
- 不做字体、颜色、字号等字符级格式还原（文本整体按段落输出）。

## 回归语料与验证
`src/test/resources/corpus/` 收录 15 个真实 .doc（Apache POI 的 HWPF 测试集，Apache-2.0），
覆盖普通段落、表格、嵌套表格、列表、多级列表样式、页眉页脚、超链接域、TOC 域、
水印、图片、Word 6 旧格式。

验证方式：解析结果与 Apache POI 5.2.3 的 HWPF 解析逐文件比对（正文文本、段落样式、
列表号、表格行、表格单元格）——正文文本与表格结构全部一致；差异三处且都是刻意的：
域指令被丢弃（POI 的 `Range.text()` 原样保留指令文本）、图片挂在正文之后、
Word 6 文件明确报不支持（POI 用 `HWPFOldDocument` 另走一套旧格式解析）。
图片字节与 POI 的 `PicturesTable.getAllPictures()` 逐字节一致（SHA-256 相同）。

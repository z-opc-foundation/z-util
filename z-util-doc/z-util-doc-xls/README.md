# z-util-doc-xls

Excel 97-2003（二进制 .xls / BIFF）读取器，`z-util-doc` 家族成员，**零 Apache 依赖**（不用 POI/HSSF），
OLE/CFB 容器与 BIFF 记录流全部自研。

## 定位
- 入口类：`com.zifang.util.doc.xls.XlsDocReader`（实现 `DocReader` SPI）。
- 记录流解析：`com.zifang.util.doc.xls.BiffWorkbook`。
- 容器层复用 `z-util-doc-ole` 的 `CfbReader`（OLE2 / FAT / miniFAT / 目录 / 流提取）。

## 能力
- **工作表目录**：BOUNDSHEET 记录给表名、可见性与子流位置，支持隐藏表。
- **共享字符串**：SST 记录 + CONTINUE 跨段续接（字符数据被切断时按段首压缩标志解码，
  串边界断开时按下一个串的 flags 解码），富文本与拼音扩展字段正确跳过。
- **单元格**：LABELSST / LABEL / NUMBER / RK / MULRK / MULBLANK / BOOLERR / FORMULA(+STRING 缓存串)；
  RK 的「×100」标志与整数/浮点两种编码都正确还原。
- **公式**：只取缓存值（数值 / 字符串 / 布尔 / 错误 / 空串），不解析 RPN 公式表达式。
- **数字格式**：FORMAT + XF 记录还原单元格格式索引，General 时按 Excel 语义继承父样式 XF 的格式；
  日期/时间格式（内置号与自定义格式码）自动转成可读日期时间。
- **旧版降级**：BIFF5/BIFF4 的字节串与 BIFF2 的记录码（0x0002/0x0003/0x0004/0x0005）同样解析；
  没有 OLE 外壳的裸 BIFF 流（BOF 开头）也能读。
- **代码页**：CODEPAGE 记录决定字节串解码（GBK / Big5 / Shift_JIS / EUC-KR / windows-125x 等）。
- **批注**：NOTE 记录给行列与作者，objId 经 OBJ（ot=0x19）关联到相邻 TXO 记录，
  文本取 TXO 后首个 CONTINUE（首字节为编码标志，0=8 位 1=UTF-16），
  以 `[批注 作者] A1: 文本` Quote 块挂在表后；BIFF5 的内联批注直接读。

## 能力边界
- 不读图表、宏、透视表、条件格式、数据验证。
- 不解析公式表达式，只给缓存值。
- 加密工作簿不支持。
- BIFF8 的压缩字符串按 latin-1 解码（与主流实现一致），非 latin-1 内容的 8 位串可能需要按需调整。

## 回归语料与验证
`src/test/resources/corpus/` 收录 10 个 xls（xlrd 项目的 MIT 测试集加 1 个 POI 生成的批注语料，含 Excel 97 文件与 BIFF4 裸流）。
本模块的解析结果与 `xlrd` 逐格比对：1193 个单元格全部一致（仅时间格式的文本表示不同）。

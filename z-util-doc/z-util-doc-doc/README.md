# z-util-doc-doc

二进制 Word（.doc，Word 97-2003）读取器，`z-util-doc` 家族成员。**零 Apache 依赖**（不用 POI/HWPF），全部自己实现。

## 定位
- 入口类：`com.zifang.util.doc.doc.DocDocReader`（实现 `DocReader` SPI）。
- 容器：`CfbReader` —— OLE2/Compound File Binary：FAT / DIFAT / miniFAT / 目录 / 常规与 mini 流提取。
- 正文：`WordBinaryParser` —— FIB（`fWhichTblStm` 选 0Table/1Table）+ CLX piece table
  （压缩 cp1252 与 UTF-16LE 双分支），保留 `\r` 段落分隔，清理字段/对象控制符。
- 元数据：`SummaryInfoParser` —— `\u0005SummaryInformation` 属性集（title/author/subject/keywords/创建与修改时间，VT_LPSTR / VT_LPWSTR / VT_FILETIME）。

## 能力边界
- 只做文本级提取：不还原表格结构、字符样式、页眉页脚、脚注尾注（留给上层按需加）。
- 加密 doc 不支持；Word 95 及更早二进制格式未验证。

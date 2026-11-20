# z-util-doc-ole

OLE2 / Compound File Binary（CFB）容器读取层，`z-util-doc` 家族的公共 IO 层，**零第三方依赖**。

## 定位
- 入口类：`com.zifang.util.doc.ole.CfbReader`。
- 供 `z-util-doc-doc`（Word 97-2003）与 `z-util-doc-xls`（Excel 97-2003）共用，避免两份 CFB 实现。

## 能力
- **头部解析**：扇区大小（512 / 4096）、mini 扇区、FAT 扇区数、目录起始扇区。
- **FAT / DIFAT**：支持 109 个内联 DIFAT 项与 DIFAT 扇区链，扇区级自由链遍历带环路保护。
- **miniFAT 与 mini 流**：小于 4096 字节的流走 mini 扇区（64 字节），根目录项的流链即 mini 流容器。
- **目录**：逐条目读出名称（UTF-16LE）、类型（storage / stream / root）与大小、起始扇区。
- **流读取**：按名字取流，支持常规流与 mini 流。

## 能力边界
- 不做加密流（ agile encryption / RC4）解密。
- 不做事务流、存储层次的红黑树修复（依赖文件自身目录结构合法）。

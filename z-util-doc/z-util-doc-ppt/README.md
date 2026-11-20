# z-util-doc-ppt

PowerPoint 97-2003（二进制 .ppt / HSLF）读取器，`z-util-doc` 家族成员，**零第三方依赖**
（不用 POI/HSLF），OLE/CFB 容器复用 `z-util-doc-ole`，HSLF 记录流全部自研解析。

## 定位
- 入口类：`com.zifang.util.doc.ppt.PptDocReader`（实现 `DocReader` SPI）。
- 记录流解析：`com.zifang.util.doc.ppt.HslfDocument`（记录树、持久化链、幻灯片装配）。
- 记录节点：`com.zifang.util.doc.ppt.HslfRecord`（8 字节记录头 + 负载 / 子记录）。

## 能力
- **增量保存链**：ppt 保存过多次会在流里留下多份「UserEditAtom → 持久化目录（6002）」。
  从 OLE 的「Current User」流取最新编辑位置，沿链回溯各版本持久化目录，
  得到 sheet id → 记录偏移的映射（最新一版胜出），并据此定位当前版本的
  Document 容器、Slide 记录与 Notes 记录。
- **播放顺序**：播放顺序不等于文件顺序，按 Document 容器里
  SlideListWithText（instance=0）的 SlidePersistAtom 顺序表还原
  （语料 `incorrect_slide_order.ppt` 就是插单与播放顺序故意不一致的文件）。
- **两处文本来源**：绘图记录（PPDrawing → TextInfoAtom）里的文本框文字，
  以及 SlideListWithText 里跟在该页 SlidePersistAtom 之后的「大纲文本」
  （老格式把占位符文字存在这里）；两者合并，重复文本按内容去重。
- **字符集**：TextCharsAtom 按 UTF-16LE 解码，TextBytesAtom 按单字节（latin-1）解码。
- **占位符类型**：TextHeaderAtom 的 textType 区分标题（0/6）、正文（1/5）、其它（4）；
  标题取第一个标题型形状的第一段，正文只把作为标题的那个形状排除，其余照常输出。
- **段落切分**：CR 分段，段内软换行（VT）转成 `\n`。
- **备注页**：SlideAtom 的 notesID 指向幻灯片 id，据此找到 Notes 记录，
  备注文本以 `[备注] …` 段落附加在页尾（与 pptx 读取器口径一致）。

## 能力边界
- 不还原图片、表格、图表、SmartArt、动画与切换效果。
- 不解析旧格式（PPT 4.0 及更早）的 0x07D0 绘图记录里的文本。
- 加密 ppt（`Password_Protected-*.ppt`）不解密。
- 不做字体、颜色、字号等字符级格式还原（文本整体按段落输出）。

## 回归语料与验证
`src/test/resources/corpus/` 收录 29 个真实 ppt：Apache POI 项目的 MIT/Apache-2.0 测试集
（26 个，含多版本增量保存、乱序、页眉页脚、中日韩文字、备注、超链接、批注）
加 3 个用 POI 生成的对照样例。

验证方式：本模块的解析结果与 Apache POI 5.2.3 的 HSLF 解析逐页比对 ——
29 个文件的标题与正文段落全部一致（差异仅三处且都是刻意的）：
标题文本单独放在标题字段而不重复进正文、组合形状内的文字本模块能取到而 POI 的形状列表不展开、
备注单独以 `[备注]` 前缀输出。

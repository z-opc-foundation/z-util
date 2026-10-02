# z-util-office

> z-util 的 Office 工具子模块——Excel / Word / PDF / PPT 的读、写、模板渲染、合并拆分、提取、往返验证。**纯 Java 8 库**，不依赖 Spring / 不暴露 REST / 不做 CLI，是一个可被任何 Java 8+ 程序直接调用的工具包。

---

## 一、模块定位

按你说的"产品化的核心工具包"思路落地——一个 jar，四种格式，五个能力（创建 / 读取 / 修改 / 提取 / 验证）。不替调用方决定版式、不替调用方选模板、不打包成可执行程序。**所有方法接受 `InputStream` / `File` / `Workbook` / `Document` 等原生 POI / PDFBox 对象作为输入输出**，调用方自己决定 I/O 形态。

## 二、能力地图

| 格式 | 读 | 写 | 修改 | 提取 | 模板 |
|------|----|----|------|------|------|
| **Excel** `.xlsx / .xls` | `ExcelUtils.readFirstSheet` / `readAllSheets` / `getCellValue` / `readSheetNames` | `ExcelUtils.createWorkbook` / `writeSheet` / `writeRow` / `writeCell` / `write` | `ExcelTemplate.renderInPlace` | `getCellValue` 公式求值、日期、布尔全开；`extractPictures(in) → List<PictureRef>`（fileName/mimeType/data 三件套） | `ExcelTemplate.render` 三种占位符 |
| **Word** `.docx` | （经 POI `XWPFDocument` 直接读） | （经 POI + `U` 类静态方法构造） | `word.U` 静态 API：加文本/标题/图/表格/合并单元格 | `WordExtractor.extractParagraphs` / `extractText` / `readTable` / `readAllTables` / `tableCount` / `extractImages` / `extractMetadata` | `WordTemplate.render` 三种占位符；`WordMailMerge.render` / `renderToZip` 单模板 × N 数据 |
| **PDF** `.pdf` | `Loader.loadPDF` 直接读 | 经 PDFBox + `PdfUtil.fillImages` 图→PDF；`core.OfficeConverter` 三格式（Word/Excel/PPT）→PDF 文本渲染 | `PdfOperator.merge` / `split` / `replaceText` / `addWatermark` / `rotatePage` / `removePages` / `protect` | `PdfExtractor.extractText` / `extractMetadata` / `pageCount` / `pageRotation` / `isEncrypted` / `renderPageAsImage` / `extractImages`（每页 XObject 位图统一编码为 PNG） | （不提供模板引擎，PDF 的"模板"含义已被 POI 覆盖） |
| **PPT** `.pptx` | `PptUtils.read` / `collectAllText` / `collectText` | `PptUtils.create` / `addBlankSlide` / `addTextBox` / `write` | `PptTemplate.renderInPlace` | `collectText` / `collectAllText` | `PptTemplate.render` 两种占位符 |
| **跨格式** | `core.OfficeFormat.detect`（魔数 + OOXML 包内目录） | `core.OfficeConverter` DOCX/XLSX/PPTX → PDF 单向文本渲染（A4 逐行、Helvetica、非 ASCII → '?'） | — | `core.RoundTripAssert`：写→读→断言相等 / 包含 | `core.OfficePipeline` 链式门面：`open/bytes` → 读 (`readExcelSheets` / `readWordTables` / `extractWordText` / `extractWordImages` / `extractExcelPictures` / `extractPdfText` / `extractPdfMetadata` / `extractPdfImages`) → 改 (`pdfAddWatermark` / `pdfReplaceText` / `pdfRotatePage` / `pdfRemovePages` / `pdfProtect` / `pdfMergeWith`) → 转 (`convertToPdf()`) → 合并 (`mailMerge(records)`) → 出 (`saveAs` / `toBytes`)；PDF mutate 走临时文件，buffer 原地更新 |

## 三、占位符语法（v0）

三类占位符，所有模板引擎共享：

| 语法 | 含义 | 例 |
|------|------|----|
| `${name}` | 简单变量替换为 `model.get("name").toString()` | `${title}` → "年度报告" |
| `${list[*]}` | 占位符单元格 / 段落被**垂直展开**为列表逐项 | `${items[*]}` + `items=["a","b","c"]` → 三个段落/单元格 a/b/c |
| `${table}` | 占位符所在行被**水平展开**为 Map 的 key/value 对（仅 Excel / Word） | `${table}` + `table={"k1":"v1","k2":"v2"}` → 两列四格 |

**注意 v0 的限制**：
- 占位符只在**单元格 / 段落 / 文本框**这一层匹配；不解析条件 / 循环以外的 Mustache 语义。
- `list[*]` 的多出来项落在"占位符所在位置之后"——Word / PPT 中如果模板段落不在文档末尾，新段落会出现在文档末尾（v0 简化，不做 XmlCursor 重排）。
- Word 段落同段多个 `${var}` 已全部替换（W14 修复 multi-pass）；但 `${list[*]}` 与 `${var}` 混合时段落级 listActions 会覆盖文本，仍以 list 为主。
- `table` 关键字是保留字，不要用作 `${table}` 以外的变量名。

## 四、典型用法

```java
// 1) 从零创建一份 Word 并填值
XWPFDocument doc = new XWPFDocument();
XWPFParagraph p = doc.createParagraph();
U.addText(p, "Hello ${name}, welcome to ${dept}!");   // 同段多占位符一次全替
WordTemplate.renderInPlace(doc, Collections.singletonMap("name", "Alice"));
doc.write(new FileOutputStream("out.docx"));

// 2) 用模板批量生成 Excel
try (InputStream tpl = new FileInputStream("template.xlsx");
     OutputStream out = new FileOutputStream("report.xlsx")) {
    ExcelTemplate.render(tpl, out, model);
}

// 3) 合并 / 拆分 PDF
PdfOperator.merge(Arrays.asList(a, b, c), new File("merged.pdf"));
List<File> parts = PdfOperator.split(input, Arrays.asList(0, 2), outDir, "part");

// 4) 提取 PDF 文本 / 元数据 / 图片
String text = PdfExtractor.extractText(pdfFile);
Map<String, String> meta = PdfExtractor.extractMetadata(pdfFile);
List<byte[]> pngs = PdfExtractor.extractImages(pdfFile);   // 每页 XObject → PNG

// 5) 跨格式往返验证（写出去再读回来，断言相等 / 包含）
RoundTripAssert.assertEquals("expected", writer, reader);
RoundTripAssert.assertContains("substring", writer, reader);

// 6) 链式门面：一条管线走完"打开→改→转→出"
try (OfficePipeline p = OfficePipeline.open(new File("report.docx"))) {
    p.convertToPdf()                                   // DOCX → PDF（新管线）
     .pdfAddWatermark("CONFIDENTIAL")
     .pdfRotatePage(0, 90)
     .saveAs(new File("report.pdf"));
}

// 7) 邮件合并：单模板 × N 数据 → N 份文档
byte[] tpl = Files.readAllBytes(new File("letter.docx").toPath());
List<byte[]> letters = WordMailMerge.render(tpl, records);   // 或 pipeline.mailMerge(records)
WordMailMerge.renderToZip(tpl, records, new FileOutputStream("letters.zip"), "letter");
```

## 五、难点 / 已知坑（实测过）

1. **PDFBox 3.x 的 API 漂移**：`PDDocument.load(File)` 已删除，统一用 `Loader.loadPDF(...)`；`PDPage.getContents()` 在 3.x 改回 `InputStream`（懒加载），不再是 `PDStream`；`PDStream` 类已搬到 `org.apache.pdfbox.pdmodel.common`，且不再有 `getInputStream()`，用 `getByteArray()`。本仓的代码已经按 3.0.8 跑通。
2. **POI XSLF 多 run 写入**：POI 把"Hello world"内部切成多段 run，"Hello ${name}!" 在 POI 解析时通常有 2 个 run（"Hello " + "${name}!"）。`setText` 必须保留第一个 run、清掉其余，才能保证新文本顺序正确。
3. **POI XWPF `addRow(XmlObject)` 不存在**：`XWPFTable.addRow` 不接受 `CTRow.copy()`；v0 的"克隆模板行"用 `createRow()` + 补足单元格实现。
4. **WordTemplate 的 list[*] 不重排**：见上文"占位符语法"节末。修 v1 时把段落插入换 `XmlCursor` 的 `toNextToken()` + `moveXml` 之外的 API（XMLBeans 的 `moveXml` 拒绝从 end-token 移动）。
5. **PDF `replaceText` 是"按字节流暴力替换"**：仅替换内容流字节里的字符串，不会重新分栏 / 重排字距。多栏或字距信息复杂的 PDF 会错位。复杂版式请改用 `PDFTextStripper` + 自绘 `PDPageContentStream` 覆盖。
6. **`isEncrypted` 的实现语义**：带非空 user 密码的 PDF 在无密码 `loadPDF` 时 PDFBox 直接抛 `InvalidPasswordException`——本工具把它当作"已加密"的证据返回 true。因此 `isEncrypted` 对"有密码的文件"永远走异常路径，开销略高但语义正确。
7. **OOXML 格式检测靠 zip 首个匹配目录**：`OfficeFormat.detect` 解 zip 目录找 `word/` / `xl/` / `ppt/` 前缀；伪造的 zip（有 PK 头但无这三个目录）会返回 UNKNOWN。OLE2 容器（老 .doc/.xls/.ppt 共用 D0CF11E0 魔数）无法进一步区分，统一返回 OLE2。
8. **`OfficeFormat.detect(File)` 潜在缺陷**：内部用 `FileInputStream`，而 `FileInputStream` 不支持 `mark/reset`，检测的 `mark(8)` 会抛 `IOException("mark/reset not supported")`。`OfficePipeline.open(File)` 绕行：先 `Files.readAllBytes` 再喂 `ByteArrayInputStream` 走 `detect(InputStream)`。修 v1 需要把 `detect(File)` 内部改成 buffered stream。
9. **`OfficeConverter` 非 ASCII 一律 '?'**：PDFBox 内建 `PDType1Font(HELVETICA)` 用 WinAnsiEncoding，只覆盖 Latin-1；CJK / Emoji 走 `toWinAnsi` 兜底成 '?'，避免 `showText` 抛异常。要真渲染 CJK 必须嵌入 TTF（`PDType0Font.load(doc, new File("NotoSansCJK.ttf"))`），不在本 util 范围。
10. **ExcelUtils `extractPictures` 只走 XSSF 路径**：`XSSFWorkbook.getAllPictures()` 拿到的是 POI 5.x 里的 `XSSFPictureData`——**它不实现 `PictureData` 接口，`getFileName()` 不存在**。本 util 从 `pic.getPackagePart().getPartName().getName()` 取 part 全名（如 `/xl/media/image1.png`），截 basename 返回。HSSF 的 .xls 走 POIFS，方法会抛 IOException。
11. **Word `addPicture(byte[], int)` 不存在**：XWPFDocument 的公开方法是 `addPictureData(byte[], int)`；`XWPFRun.addPicture(InputStream, int, String, int, int)` 才是"往 run 里塞图 + 引用关系"的路径。测试 fixture 得两个都调才能既落 `word/media/` 又建引用。

## 六、测试过程与结果

按"往返是唯一证明"的原则，每个新增能力都对应一个 `_XxxTest`，流程都是：**内存构建输入 → 模板渲染或工具写入 → 重新打开 → 断言内容相等或包含**。

```bash
mvn -pl z-util-office -am clean test
```

最近一次跑通（v0.2 收官）：

```
[INFO] Tests run: 104, Failures: 0, Errors: 0, Skipped: 2
[INFO] BUILD SUCCESS
GATE_RC=0
```

- W0 基线：31 / 0 / 0 / 2（基线就有 2 个 `ATest` skip，是写测试时 `assertNotNull` 在 skip 块里）
- W9（v0 收官）：51 / 0 / 0 / 2 — 新增 20 个测试，覆盖 ExcelTemplate / WordTemplate / PdfOperator / PdfExtractor / PptUtils / PptTemplate / RoundTripAssert 的"创建→写→读→断言"闭环。
- W13（v0.1 补强）：67 / 0 / 0 / 2 — 再增 16 个测试，覆盖 WordExtractor（段落/表格/元数据）、ExcelUtils.readSheetNames、OfficeFormat.detect（docx/xlsx/pptx/pdf/OLE2/unknown 六路）、PDF rotatePage / removePages / renderPageAsImage / protect（加密后带密码可开）。
- W11+W12（v0.2 builder + 转换）：89 / 0 / 0 / 2 — 加 `OfficeConverter` 三格式 → PDF 转换、`OfficePipeline` 门面（open/bytes/read/convertToPdf/pdfAddWatermark/pdfReplaceText/pdfRotatePage/pdfRemovePages/pdfProtect/pdfMergeWith/saveAs/toBytes）。
- W13（v0.2 深度提取）：98 / 0 / 0 / 2 — 加 Word `readAllTables` / `tableCount` / `extractImages`、Excel `extractPictures → PictureRef`、PDF `extractImages` 每页位图 → PNG 字节；pipeline 门面同步。
- W14（v0.2 邮件合并）：104 / 0 / 0 / 2 — 加 `WordMailMerge.render` / `renderToZip`；同批把 `WordTemplate.renderInPlace` 段落替换升级为 multi-pass，支持同段多个 `${var}`（不再"只识别第一个"）。

测试日志分波次保存在 `_log/w*.log`：
- `w0-baseline.log`：基线
- `w1.log` ~ `w9.log`：v0 六波迭代 + 三轮编译错误修复 + 三轮运行错误修复
- `w10.log` ~ `w13.log`：v0.1 补强三轮（WordExtractor 类型修复、protect 探针定位、isEncrypted 异常语义修复）
- `w11-office-test.log` / `w13-office-test.log` / `w14-office-test.log`：v0.2 三波（builder + 转换、深度提取、mail-merge）

## 七、承诺表（规则）

| 承诺 | 状态 | 证据 |
|------|------|------|
| 仍是一个**库**（不暴露 HTTP、不做 CLI、不打包 fat jar） | ✅ | `pom.xml` 仅 `packaging=jar`，无 `spring-boot-maven-plugin`、无 `maven-shade-plugin` |
| Java 8 编译目标（与父仓一致） | ✅ | 沿用 `z-util` 父链的 `compile.version=8`，未引入 Java 9+ 语法（lambda 类型 cast 已改回普通 instanceof） |
| 四种格式全覆盖（Excel / Word / PDF / PPT） | ✅ | 15 个主源类 + 15 个测试类；跨格式有 `OfficeConverter` 三路径 + `OfficePipeline` 门面 |
| 占位符三件套（var / list[*] / table）跨 Excel / Word / PPT | ✅ | `ExcelTemplate` / `WordTemplate` / `PptTemplate` 共享 `\$\{([^${}]+)\}` 同一正则；Word 段落同段多 `${var}` 走 multi-pass（W14 修复） |
| 提取能力对称（每格式都有读回入口） | ✅ | PDF→`PdfExtractor.extractText/Metadata/pageCount/Images`、Word→`WordExtractor.extractText/readAllTables/extractImages/Metadata`、PPT→`collectText/collectAllText`、Excel→`readFirstSheet/readSheetNames/extractPictures` |
| 跨格式转换（Office → PDF） | ✅ | `OfficeConverter.wordToPdf/excelToPdf/pptToPdf`；`OfficePipeline.convertToPdf()` 门面串链；`OfficeConverterTest` 8 项 + `OfficePipelineTest` 4 项覆盖往返 |
| 单模板批量渲染（邮件合并） | ✅ | `WordMailMerge.render / renderToZip` + `OfficePipeline.mailMerge(records)`；`WordMailMergeTest` 6 项覆盖 |
| 真实往返测试（写→读→断言） | ✅ | 104 测试全绿；每波（W11/W12/W13/W14）都跑 `mvn clean test` 亲验 GATE_RC=0 |
| README 主仓同步（不再把空壳类列为入口） | ✅ | `z-util/README.md` 第 46 行入口表与实际类清单一致（`PdfConverter` 空壳已删） |
| 死依赖清理 | ✅ | `xmpbox` / `preflight` / `fontbox` 三个零代码引用的依赖已从 `pom.xml` 删除 |

## 八、未做（v0.2 明确不做）

- **PDF 模板引擎**：PDF 的"模板"概念边界模糊（是 PDF 文件本身？是被渲染的 HTML？），v0.2 只做合并 / 拆分 / 水印 / 文本替换 / 图片提取。
- **DOC（老 Word） / XLS（老 Excel） / PPT（老 PPT）**：本仓只覆盖 OOXML 格式（`.docx` / `.xlsx` / `.pptx`）。
- **CJK 字体嵌入**：`OfficeConverter` / PDF 水印 / PPT 文本框使用 PDFBox 内置标准字体（WinAnsi），CJK 走 '?' 占位。要真渲染中文请调用方自行嵌入 TTF。
- **反向 Office 转换**（PDF → DOCX / XLSX / PPTX）：版式信息几乎全丢，不做。
- **条件 / 循环 / Mustache 高级语义**：见上文 v0 限制。

---
_Maintained by z-opc-foundation._

# z-util-doc-html

HTML 读取器，`z-util-doc` 家族成员，**零第三方依赖**（不用 jsoup）：字符级标签扫描 + 宽容闭合（不要求标签配对）。

## 定位
- 入口类：`com.zifang.util.doc.html.HtmlDocReader`（实现 `DocReader` SPI，`DocFormat.HTML`）。
- 嗅探：`DocFormatDetector.isHtml`（前 512 字节含 `<!doctype html` / `<html`）。
- 解码：BOM 优先，其次 `meta charset` 声明（GBK 等按声明解），最后 UTF-8 坏字节替换。

## 能力
- **块级**：h1-h6 标题；p/div/section 等块级开标签自动断段；hr → `---` 段落（markdown 兼容）；caption 按文档顺序落成表前段落。
- **内联样式**：b/strong 加粗、i/em 斜体、s/strike/del 删除线、code/tt/kbd/samp 等宽、a href 链接（DocSpan.link），嵌套与跨段都按深度计数。
- **列表**：ul/ol 嵌套栈 → ListItem（ordered + level），未闭合 li 自动闭合。
- **表格**：th → header 行；colspan/rowspan 属性还原；单元格内多段落保留。
- **引用**：blockquote 内多段落合成一个 Quote（`\n` 分行）。
- **pre/code**：原文捕获保留空白；语言从 `class="language-xx"`（pre 自身或内层 code）提取；内层标签吸收不入文。
- **图片**：img → Image 块（alt/src 基名 + 扩展名猜 MIME），cell 内保留为块。
- **实体**：约 50 个常用命名实体 + 十进制/十六进制数字实体。
- **meta**：title → 标题；meta[name=author/keywords/description/generator] → 元数据；charset 记入 extra。
- **跳过**：script/style/template/svg/math 整段（内部假标签不影响配对）、注释、DOCTYPE、head 其余内容。
- **宽容性**：未闭合 p/li/tr/td、裸属性（`CLASS=intro`）、大写标签、`</scriptx` 假闭合都不乱。

## 能力边界
- 不渲染 CSS、不执行 JS、不下载图片（Image 块不带字节）。
- 表格不嵌套：嵌套表内容并入当前单元格文本。
- 模板语法（Vue/JSX 花括号）按普通文本处理。

## 回归语料
`src/test/resources/corpus/`：article（标题/样式/链接/meta）、tables（th/colspan/rowspan/caption）、lists（嵌套 ul/ol）、pre（语言标注/空白保留）、entities（命名/数字实体）、messy（GBK 字节、大写标签、未闭合 p·li、script/style/注释陷阱）。

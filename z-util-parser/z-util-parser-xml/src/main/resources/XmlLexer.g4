/*
 * XML Lexer G4
 * 覆盖 XML 1.0 常用结构：声明、处理指令、注释、CDATA、起止标签、属性、文本。
 * 词法粒度参考原手写 Tokenizer，由 Java 端 XmlG4Parser 进一步组装为 XDocument。
 *
 * 规则顺序遵循 DynamicLexer longest-match + 先定义优先：
 *  - XML_DECL 必须在 PI 之前（两者长度相同时优先匹配 XML_DECL）
 *  - COMMENT / CDATA 必须在 LT 之前
 *  - LT_SLASH / SLASH_GT 必须在 LT / GT 之前
 *  - ATTR_VALUE_DQ / ATTR_VALUE_SQ 必须在 LT 之前
 *
 * 已知 G4FileParser / DynamicLexer 限制：
 *  - '...\'...' 中的 \' 转义会被 DynamicLexer 误处理为反斜杠，单引号改用 [']
 *    char class 表达
 *  - body.indexOf("->") 不识别字符串字面量，所以 '-->' 必须拆成 '-' '-' '>'
 *    形式以避免 body 被误截断
 *  - DynamicLexer 用 longest-match，若 TEXT 用 ~[<]+ 会把 NAME / 属性等全吞掉。
 *    改成单字符 ~[<]，让 markup 规则靠更长匹配优先胜出；Java 端再把连续
 *    TEXT 合并成一个文本片段。
 */

lexer grammar XmlLexer;

// ===== 顶层特殊结构 =====
XML_DECL : '<?xml' [ \t\r\n]+ .*? '?>';
PI       : '<?' .*? '?>';
COMMENT  : '<!--' .*? '-' '-' '>';
CDATA    : '<![CDATA[' .*? ']]>';

// ===== 标签符号 =====
LT_SLASH : '</';
LT       : '<';
SLASH_GT : '/>';
GT       : '>';

// ===== 名称（标签名 / 属性名复用） =====
NAME     : [a-zA-Z_:] [a-zA-Z0-9_:.-]*;

// ===== 等号 =====
EQUALS   : '=';

// ===== 属性值（双 / 单引号，允许内部实体引用） =====
// 双引号形式：literal '"' + (非["<&] | 实体引用)* + literal '"'
ATTR_VALUE_DQ : '"' (~["<&] | '&' [a-zA-Z]+ ';' | '&' '#' [0-9]+ ';' | '&' '#x' [0-9a-fA-F]+ ';')* '"';

// 单引号形式：char class ['] + (非['<&] | 实体引用)* + char class [']
ATTR_VALUE_SQ : ['] (~['<&] | '&' [a-zA-Z]+ ';' | '&' '#' [0-9]+ ';' | '&' '#x' [0-9a-fA-F]+ ';')* ['];

// ===== 文本（单字符 ~[<]，fallback），由 Java 端合并连续 TEXT 为一个文本片段 =====
TEXT     : ~[<];

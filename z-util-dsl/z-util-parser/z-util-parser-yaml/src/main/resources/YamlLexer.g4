/*
 * YAML Lexer G4
 * 对标 YAML 1.2 规范
 * 使用 DynamicLexer 加载并运行
 */
lexer grammar YamlLexer;

// 行结束（跳过）
EOL: '\r'? '\n' -> channel(HIDDEN);
// 空白（跳过）
Space: [ \t]+ -> channel(HIDDEN);
// 注释（跳过）
Comment: '#' [^\n\r]* -> channel(HIDDEN);
// 文件结束
EOF: ;

// 文档分隔符
DocStart: '---';
DocEnd: '...';

// 显式标记
Exclaim: '!';
DoubleExclaim: '!!';
Tag: '!' [a-zA-Z0-9_-]+;

// 锚点和别名
AnchorName: '&' [a-zA-Z0-9_-]+;
AliasName: '*' [a-zA-Z0-9_-]+;

// 块标量指示符
BlockScalarStart: '|' | '>';
BlockChomping: [-+];
BlockIndent: [0-9];

// 块序列项标记
Dash: '-' ;
Colon: ':' ;
Quest: '?' ;

// 缩进标记（匹配空格/制表符序列，parser 用它判断层级，lexer 隐藏）
Indent: [ \t]+ ;

// 结构符号
LBracket: '[';
RBracket: ']';
LBrace: '{';
RBrace: '}';
Comma: ',';

// 引号字符串（避开 A-Z 范围中的 [ ]，直接排除这两个字符码点）
DqString: '"' ([ -!#$%&(*,-./0-9:;=?@A-Z_a-z{|~] | '\\"' | '\\\\' | '\\n' | '\\r' | '\\t')* '"';

SqString: '\'' [^']* '\'';

// 标量内容
Scalar: [a-zA-Z0-9_][a-zA-Z0-9_ \t]*;

// 数字
Number: '-'? [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;
Bool: 'true' | 'false' | 'True' | 'False' | 'TRUE' | 'FALSE';
Null: 'null' | 'Null' | 'NULL' | '~';

// 杂项
Directive: '%';

AnyChar: .;
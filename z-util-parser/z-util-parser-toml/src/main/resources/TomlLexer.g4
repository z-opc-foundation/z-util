lexer grammar TomlLexer;

// 空白字符（跳过）
WS: [ \t]+ -> channel(HIDDEN);
// 换行
NL: '\r'? '\n';
// 注释（跳过）
COMMENT: '#' ~[\r\n]* -> channel(HIDDEN);
// 数组表头 [[name]]
LBRACKET2: '[[';
RBRACKET2: ']]';
// section 表头 [name]
LBRACKET: '[';
RBRACKET: ']';
// 等号
EQUALS: '=';
// 逗号（数组内）
COMMA: ',';
// 点（dotted key）
DOT: '.';
// 布尔
BOOL: 'true' | 'false';
// null
NULL: '~' | 'null';
// 双引号字符串：支持 \" \\ \n \r \t \uXXXX 转义
STRING_DQ: '"' (~["\\\r\n] | '\\' [.])* '"';
// 单引号字符串（字面量，不支持转义）
STRING_SQ: '\'' (~['\r\n])* '\'';
// 数字：整数或浮点
NUMBER: '-'? [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;
// Key：字母数字下划线连字符
KEY: [a-zA-Z0-9_-]+;

lexer grammar CsvLexer;

// 空白字符（跳过）
WS: [ \t]+ -> channel(HIDDEN);
// 换行
NL: '\r'? '\n';
// 字段分隔符
COMMA: ',';
// 带引号字段：支持 "" 转义为单个 "
QUOTED: '"' (~["\r\n] | '""')* '"';
// 无引号字段：不包含逗号和换行
TEXT: ~[",\r\n"]+;

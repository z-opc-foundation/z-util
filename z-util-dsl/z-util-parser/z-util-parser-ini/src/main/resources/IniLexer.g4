lexer grammar IniLexer;

// 空白（跳过）
WS: [ \t]+ -> channel(HIDDEN);
// 换行
NL: '\r'? '\n';
// 注释行（; 或 # 开头）
COMMENT: (';' | '#') ~[\r\n]* -> channel(HIDDEN);
// 单行内容：除换行外的所有字符
LINE: ~[\r\n]+;

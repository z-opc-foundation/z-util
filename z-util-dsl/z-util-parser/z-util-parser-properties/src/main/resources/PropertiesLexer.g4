lexer grammar PropertiesLexer;

// 空白字符（跳过，不含换行）
WS: [ \t\f]+ -> channel(HIDDEN);
// 换行
NL: '\r'? '\n';
// 注释行（# 或 ! 开头）
COMMENT: ('#' | '!') ~[\r\n]* -> channel(HIDDEN);
// 等号或冒号
SEPARATOR: '=' | ':';
// 标识符（key 或 value，不含 =、:、空白、换行）
// 由 G4Parser 根据 SEPARATOR 位置判断是 key 还是 value
IDENTIFIER: ~[=\r\n\t\f: ]+;

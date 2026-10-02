lexer grammar ProtoLexer;

// 空白字符（跳过）
WS: [ \t\r\n\f]+ -> channel(HIDDEN);
// 单行注释
LINE_COMMENT: '//' ~[\r\n]* -> channel(HIDDEN);
// 块注释
BLOCK_COMMENT: '/*' .*? '*/' -> channel(HIDDEN);
// 符号
SEMI: ';';
LBRACE: '{';
RBRACE: '}';
LPAREN: '(';
RPAREN: ')';
LBRACKET: '[';
RBRACKET: ']';
LT: '<';
GT: '>';
EQUALS: '=';
COMMA: ',';
DOT: '.';
// 关键字
SYNTAX: 'syntax';
PACKAGE: 'package';
IMPORT: 'import';
PUBLIC: 'public';
WEAK: 'weak';
MESSAGE: 'message';
ENUM: 'enum';
SERVICE: 'service';
RPC: 'rpc';
RETURNS: 'returns';
STREAM: 'stream';
REPEATED: 'repeated';
OPTIONAL: 'optional';
REQUIRED: 'required';
// 字面量
STRING_LITERAL: '"' (~["\\\r\n] | '\\' [.])* '"';
INT_LITERAL: '-'? [0-9]+;
FLOAT_LITERAL: '-'? [0-9]+ '.' [0-9]+ ([eE] [+-]? [0-9]+)?;
// 标识符
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;

lexer grammar JsonLexer;

Space: [ \t]+ -> channel(HIDDEN);
EOL: '\r'? '\n' -> channel(HIDDEN);

LBrace: '{';
RBrace: '}';
LBracket: '[';
RBracket: ']';
Comma: ',';
Colon: ':';

Null: 'null';
Bool: 'true' | 'false';

Number: '-'? [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;

// unrolled loop 形式（与 (~["\\\r\n] | '\\' .)* 等价）：
// Java 正则对 (A|B)* 交替循环按字符递归 match，长字符串（约 >2KB）会 StackOverflow；
// [^...]* 为确定性字符类循环（Curly），('\\' . [...]*)* 迭代次数仅与转义序列个数相关，不深递归。
StringLiteral: '"' ~["\\\r\n]* ('\\' . ~["\\\r\n]*)* '"';

AnyChar: .;
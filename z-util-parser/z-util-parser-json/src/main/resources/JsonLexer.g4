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

StringLiteral: '"' (~["\\\r\n] | '\\' .)* '"';

AnyChar: .;
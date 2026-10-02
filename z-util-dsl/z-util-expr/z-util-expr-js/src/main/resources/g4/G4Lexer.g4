/*
 * G4语法文件词法规则
 * 用于解析ANTLR .g4文件本身
 */
lexer grammar G4Lexer;

// 空白字符
Space:  [ \t\r\n]+ -> channel(HIDDEN);

// 注释
Comment: '//' ~[\r\n]* -> channel(HIDDEN);
MLComment: '/*' .*? '*/' -> channel(HIDDEN);

// 关键字（按字母顺序）
Fragment A: 'a' | 'A';
Fragment B: 'b' | 'B';
Fragment C: 'c' | 'C';
Fragment D: 'd' | 'D';
Fragment E: 'e' | 'E';
Fragment F: 'f' | 'F';
Fragment G: 'g' | 'G';
Fragment H: 'h' | 'H';
Fragment I: 'i' | 'I';
Fragment J: 'j' | 'J';
Fragment K: 'k' | 'K';
Fragment L: 'l' | 'L';
Fragment M: 'm' | 'M';
Fragment N: 'n' | 'N';
Fragment O: 'o' | 'O';
Fragment P: 'p' | 'P';
Fragment Q: 'q' | 'Q';
Fragment R: 'r' | 'R';
Fragment S: 's' | 'S';
Fragment T: 't' | 'T';
Fragment U: 'u' | 'U';
Fragment V: 'v' | 'V';
Fragment W: 'w' | 'W';
Fragment X: 'x' | 'X';
Fragment Y: 'y' | 'Y';
Fragment Z: 'z' | 'Z';

Tokens: (T O K E N S) | (t o k e n s);
Lexer: (L E X E R) | (l e x e r);
Parser: (P A R S E R) | (p a r s e r);
Grammar: (G R A M M A R) | (g r a m m a r);
Import: (I M P O R T) | (i m p o r t);
Fragment: (F R A G M E N T) | (f r a g m e n t);
Channels: (C H A N N E L S) | (c h a n n e l s);
Options: (O P T I O N S) | (o p t i o n s);
Rule: (R U L E) | (r u l e);
Deprecated: (D E P R E C A T E D) | (d e p r e c a t e d);
Associated: (A S S O C I A T E D) | (a s s o c i a t e d);

// 标识符
Id: [a-zA-Z_][a-zA-Z0-9_]*;

// 字符串字面量
StringLiteral: '\'' (~['\r\n] | '\\' .)* '\'';

// 字符字面量
CharLiteral: '"' (~["\r\n] | '\\' .)* '"';

// 正则表达式字面量（用于lexer规则体）
RegexLiteral: '/' (~[\r\n/] | '\\' . | '/' ~[\r\n])+ '/' [a-zA-Z]*;

// 规则引用（::用于子规则）
RuleRef: [a-z][a-zA-Z0-9_]*;

// 小数
Decimal: [0-9]+ '.' [0-9]+;

// 整数
Int: [0-9]+;

// 符号
Colon: ':';
SemiColon: ';';
Comma: ',';
Star: '*';
Plus: '+';
Question: '?';
LeftParen: '(';
RightParen: ')';
LeftBracket: '[';
RightBracket: ']';
LeftBrace: '{';
RightBrace: '}';
Less: '<';
Greater: '>';
Equal: '=';
NotEqual: '!=';
And: '->';
Or: '|';
Assign: '=';

// 其他符号
Other: .;
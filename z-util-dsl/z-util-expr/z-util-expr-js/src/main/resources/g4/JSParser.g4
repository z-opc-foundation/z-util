/*
 * JavaScript-like 语言词法和语法定义
 */
lexer grammar JSLexer;

// ========== 词法规则 ==========

// 空白字符（跳过）
Space:  [ \t\r\n]+ -> channel(HIDDEN);

// 注释
LineComment: '//' ~[\r\n]* -> channel(HIDDEN);
BlockComment: '/*' .*? '*/' -> channel(HIDDEN);

// 关键字
Var: 'var';
Let: 'let';
Const: 'const';
Function: 'function';
Return: 'return';
If: 'if';
Else: 'else';
For: 'for';
While: 'while';
Do: 'do';
Break: 'break';
Continue: 'continue';
True: 'true';
False: 'false';
Null: 'null';
Undefined: 'undefined';

// 标识符
Identifier: [a-zA-Z_][a-zA-Z0-9_]*;

// 字符串字面量
StringLiteral: '"' (~["\\\r\n] | '\\' .)* '"';
SingleQuoteString: '\'' (~['\\\r\n] | '\\' .)* '\'';

// 数字字面量
DecimalLiteral: [0-9]+ '.' [0-9]+;
IntLiteral: [0-9]+;
HexLiteral: '0x' [0-9a-fA-F]+;

// 运算符
Plus: '+';
Minus: '-';
Star: '*';
Slash: '/';
Mod: '%';
Assign: '=';
Less: '<';
Greater: '>';
LessEqual: '<=';
GreaterEqual: '>=';
Equal: '==';
NotEqual: '!=';
And: '&&';
Or: '||';
Not: '!';
BitAnd: '&';
BitOr: '|';
BitNot: '~';
Xor: '^';

// 界符
LeftParen: '(';
RightParen: ')';
LeftBrace: '{';
RightBrace: '}';
LeftBracket: '[';
RightBracket: ']';
SemiColon: ';';
Comma: ',';
Dot: '.';
Question: '?';
Colon: ':';

// ========== 语法规则 ==========

parser grammar JSParser;

options { tokenVocab=JSLexer; }

// 程序入口
program
    : statement*
    ;

// 语句
statement
    : variableDeclaration
    | functionDeclaration
    | expressionStatement
    | ifStatement
    | forStatement
    | whileStatement
    | returnStatement
    | breakStatement
    | continueStatement
    | block
    ;

// 变量声明
variableDeclaration
    : (Var | Let | Const) Identifier ('=' expression)? SemiColon
    ;

// 函数声明
functionDeclaration
    : Function Identifier LeftParen parameterList? RightParen block
    ;

parameterList
    : Identifier (Comma Identifier)*
    ;

// 表达式语句
expressionStatement
    : expression? SemiColon
    ;

// 条件语句
ifStatement
    : If LeftParen expression RightParen statement (Else statement)?
    ;

// for循环
forStatement
    : For LeftParen (variableDeclaration | expressionStatement) expression? SemiColon expression? RightParen statement
    ;

// while循环
whileStatement
    : While LeftParen expression RightParen statement
    ;

// return语句
returnStatement
    : Return expression? SemiColon
    ;

// break语句
breakStatement
    : Break SemiColon
    ;

// continue语句
continueStatement
    : Continue SemiColon
    ;

// 语句块
block
    : LeftBrace statement* RightBrace
    ;

// 表达式
expression
    : assignmentExpression
    ;

assignmentExpression
    : conditionalExpression (Assign assignmentExpression)?
    ;

conditionalExpression
    : logicalOrExpression (Question expression Colon expression)?
    ;

logicalOrExpression
    : logicalAndExpression (Or logicalAndExpression)*
    ;

logicalAndExpression
    : bitwiseOrExpression (And bitwiseOrExpression)*
    ;

bitwiseOrExpression
    : bitwiseXorExpression (BitOr bitwiseXorExpression)*
    ;

bitwiseXorExpression
    : bitwiseAndExpression (Xor bitwiseAndExpression)*
    ;

bitwiseAndExpression
    : equalityExpression (BitAnd equalityExpression)*
    ;

equalityExpression
    : relationalExpression ((Equal | NotEqual) relationalExpression)*
    ;

relationalExpression
    : additiveExpression ((Less | Greater | LessEqual | GreaterEqual) additiveExpression)*
    ;

additiveExpression
    : multiplicativeExpression ((Plus | Minus) multiplicativeExpression)*
    ;

multiplicativeExpression
    : unaryExpression ((Star | Slash | Mod) unaryExpression)*
    ;

unaryExpression
    : (Not | Minus | Plus) unaryExpression
    | postfixExpression
    ;

postfixExpression
    : primaryExpression
    ;

primaryExpression
    : Identifier
    | literal
    | LeftParen expression RightParen
    | functionCall
    ;

// 字面量
literal
    : IntLiteral
    | DecimalLiteral
    | StringLiteral
    | SingleQuoteString
    | HexLiteral
    | True
    | False
    | Null
    | Undefined
    ;

// 函数调用
functionCall
    : Identifier LeftParen argumentList? RightParen
    ;

argumentList
    : expression (Comma expression)*
    ;

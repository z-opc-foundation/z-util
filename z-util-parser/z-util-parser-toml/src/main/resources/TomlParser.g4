parser grammar TomlParser;

options { tokenVocab=TomlLexer; }

// 文件：零或多条 section / tableArray / entry
file: (topItem NL?)*;
// 顶层条目：section、数组表、键值对
topItem: section | tableArray | entry;
// section 表头
section: LBRACKET dottedKey RBRACKET;
// 数组表头
tableArray: LBRACKET2 dottedKey RBRACKET2;
// 点分 key
dottedKey: KEY (DOT KEY)*;
// 键值对
entry: dottedKey EQUALS value;
// 值类型
value: STRING_DQ | STRING_SQ | NUMBER | BOOL | NULL | array;
// 数组
array: LBRACKET (value (COMMA value)*)? RBRACKET;

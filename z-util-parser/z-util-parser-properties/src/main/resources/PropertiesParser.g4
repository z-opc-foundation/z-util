parser grammar PropertiesParser;

options { tokenVocab=PropertiesLexer; }

// 文件：零或多条 entry（注释在 lexer 层跳过）
file: entry*;
// 单条 entry：key = value 或 key : value
entry: KEY SEPARATOR VALUE?;

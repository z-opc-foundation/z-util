parser grammar IniParser;

options { tokenVocab=IniLexer; }

// 文件：零或多条 token（LINE 或 NL）
file: item*;
// 顶层 token：行或换行
item: LINE | NL;

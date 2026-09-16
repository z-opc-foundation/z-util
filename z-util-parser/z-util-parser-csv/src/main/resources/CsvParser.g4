parser grammar CsvParser;

options { tokenVocab=CsvLexer; }

// 文件：零或多条记录，记录之间用换行分隔
file: record (NL record)* NL? EOF;
// 单条记录：字段之间用逗号分隔
record: field (COMMA field)*;
// 字段：带引号或无引号
field: QUOTED | TEXT;

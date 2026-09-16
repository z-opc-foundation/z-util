parser grammar ProtoParser;

options { tokenVocab=ProtoLexer; }

// 文件：零或多条顶层声明
file: topDecl*;
// 顶层声明
topDecl: syntaxDecl | packageDecl | importDecl | messageDecl | enumDecl | serviceDecl;
// syntax 声明
syntaxDecl: SYNTAX EQUALS STRING_LITERAL SEMI;
// package 声明
packageDecl: PACKAGE (IDENTIFIER DOT)* IDENTIFIER SEMI;
// import 声明
importDecl: IMPORT (PUBLIC | WEAK)? STRING_LITERAL SEMI;
// message 声明
messageDecl: MESSAGE IDENTIFIER LBRACE messageBody RBRACE;
messageBody: messageBodyItem*;
messageBodyItem: fieldDecl | messageDecl | enumDecl | SEMI;
// 字段声明
fieldDecl: (REPEATED | OPTIONAL | REQUIRED)? type IDENTIFIER EQUALS INT_LITERAL SEMI;
type: IDENTIFIER (DOT IDENTIFIER)* | scalarType;
scalarType: 'int32' | 'int64' | 'uint32' | 'uint64' | 'sint32' | 'sint64'
          | 'fixed32' | 'fixed64' | 'sfixed32' | 'sfixed64'
          | 'float' | 'double' | 'bool' | 'string' | 'bytes';
// enum 声明
enumDecl: ENUM IDENTIFIER LBRACE enumBody RBRACE;
enumBody: enumField*;
enumField: IDENTIFIER EQUALS INT_LITERAL SEMI;
// service 声明
serviceDecl: SERVICE IDENTIFIER LBRACE serviceBody RBRACE;
serviceBody: rpcDecl*;
rpcDecl: RPC IDENTIFIER LPAREN (STREAM)? IDENTIFIER RPAREN RETURNS LPAREN (STREAM)? IDENTIFIER RPAREN (LBRACE | SEMI);

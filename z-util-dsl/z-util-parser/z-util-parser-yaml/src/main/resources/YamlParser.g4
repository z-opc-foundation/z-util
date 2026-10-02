/*
 * YAML Parser G4 - 极简版
 * 只支持: key: value, key:\n  nested, - item
 */
parser grammar YamlParser;

options { tokenVocab=YamlLexer; }

// 顶层：零个或多个键值对
yaml
    : pair* EOF
    ;

// 单个键值对
pair
    : Scalar Colon Scalar
    | Scalar Colon LBrace pair (Comma pair)* RBrace
    | Scalar Colon LBracket Scalar (Comma Scalar)* RBracket
    | Scalar Colon
    ;

// 序列项
item
    : Dash Scalar
    ;

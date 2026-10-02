/*
 * G4语法文件语法规则
 */
parser grammar G4Parser;

options { tokenVocab=G4Lexer; }

g4File
    : header? (importStatement | lexerGrammar | parserGrammar)+
    ;

header
    : optionsSpec? channelsSpec?
    ;

importStatement
    : Import StringLiteral SemiColon
    ;

optionsSpec
    : Options '{' optionPair* '}'
    ;

optionPair
    : Id Assign (Id | StringLiteral | Int) SemiColon
    ;

channelsSpec
    : Channels '{' (Id SemiColon)* '}'
    ;

lexerGrammar
    : Lexer Id Colon rule+ // 暂时用简单的
    ;

parserGrammar
    : Parser Id Colon rule+
    ;

rule
    : RuleRef (Colon | Assign) ruleAltList SemiColon
    ;

ruleAltList
    : ruleAtom (Or ruleAtom)*
    ;

ruleAtom
    : ruleRef
    | stringLiteral
    | charLiteral
    | regexLiteral
    | Block
    | optional
    | closure
    | RuleRef? NotMatch
    ;

ruleRef
    : Id
    ;

stringLiteral
    : StringLiteral
    ;

charLiteral
    : CharLiteral
    ;

regexLiteral
    : RegexLiteral
    ;

Block
    : LeftParen ruleAltList RightParen
    ;

optional
    : ruleAtom Question
    ;

closure
    : ruleAtom (Star | Plus)
    ;

NotMatch
    : Less Not+ Greater
    ;

fragment Not: '~';

// 追加必要的词法规则（确保词法分析器完整）
fragment LowerCaseLetter: [a-z] ;
fragment UpperCaseLetter: [a-z] ;

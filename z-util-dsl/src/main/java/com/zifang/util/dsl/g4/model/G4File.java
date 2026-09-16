package com.zifang.util.dsl.g4.model;

/**
 * G4文件模型
 * 表示解析后的.g4文件内容
 */
public class G4File {

    private String grammarName;
    private String lexerGrammarName;
    private String parserGrammarName;

    /**
     * 默认构造函数
     */
    public G4File() {
    }

    /**
     * 获取语法名称
     *
     * @return grammar名称
     */
    public String getGrammarName() {
        return grammarName;
    }

    /**
     * 设置语法名称
     *
     * @param grammarName grammar名称
     */
    public void setGrammarName(String grammarName) {
        this.grammarName = grammarName;
    }

    /**
     * 获取词法分析器语法名称
     *
     * @return lexer grammar名称
     */
    public String getLexerGrammarName() {
        return lexerGrammarName;
    }

    /**
     * 设置词法分析器语法名称
     *
     * @param lexerGrammarName lexer grammar名称
     */
    public void setLexerGrammarName(String lexerGrammarName) {
        this.lexerGrammarName = lexerGrammarName;
    }

    /**
     * 获取语法分析器语法名称
     *
     * @return parser grammar名称
     */
    public String getParserGrammarName() {
        return parserGrammarName;
    }

    /**
     * 设置语法分析器语法名称
     *
     * @param parserGrammarName parser grammar名称
     */
    public void setParserGrammarName(String parserGrammarName) {
        this.parserGrammarName = parserGrammarName;
    }
}
package com.zifang.util.dsl.g4.model;

/**
 * Token定义（用于动态词法分析器）
 * 表示从G4文件解析出的Token规则
 */
public class TokenDefinition {

    private String name;
    private String pattern;      // 正则表达式
    private int precedence;      // 优先级
    private boolean isFragment;
    private boolean hidden;      // 是否为HIDDEN channel

    /**
     * 默认构造函数
     */
    public TokenDefinition() {
    }

    /**
     * 构造函数
     *
     * @param name       Token名称
     * @param pattern    正则表达式模式
     * @param precedence 优先级（数值越小优先级越高）
     */
    public TokenDefinition(String name, String pattern, int precedence) {
        this.name = name;
        this.pattern = pattern;
        this.precedence = precedence;
    }

    /**
     * 获取Token名称
     *
     * @return Token名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置Token名称
     *
     * @param name Token名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取正则表达式模式
     *
     * @return 正则表达式字符串
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 设置正则表达式模式
     *
     * @param pattern 正则表达式
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * 获取优先级
     *
     * @return 优先级数值
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * 设置优先级
     *
     * @param precedence 优先级数值
     */
    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    /**
     * 判断是否为fragment规则
     *
     * @return true表示是fragment规则
     */
    public boolean isFragment() {
        return isFragment;
    }

    /**
     * 设置是否为fragment规则
     *
     * @param fragment true表示是fragment规则
     */
    public void setFragment(boolean fragment) {
        isFragment = fragment;
    }

    /**
     * 判断是否为HIDDEN channel
     *
     * @return true表示隐藏在HIDDEN channel
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * 设置是否为HIDDEN channel
     *
     * @param hidden true表示隐藏在HIDDEN channel
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "TokenDefinition{" +
                "name='" + name + '\'' +
                ", pattern='" + pattern + '\'' +
                ", precedence=" + precedence +
                ", isFragment=" + isFragment +
                ", hidden=" + hidden +
                '}';
    }
}
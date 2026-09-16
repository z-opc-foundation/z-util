package com.zifang.util.devops.git.operations.core;

/**
 * tag 信息
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitTag {

    private String name;
    private String sha;
    private String message;
    private GitAuthor tagger;
    private Type type;

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * setName方法。
     * * @param name String类型参数
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getSha方法。
     *
     * @return String类型返回值
     */
    public String getSha() {
        return sha;
    }

    /**
     * setSha方法。
     * * @param sha String类型参数
     */
    public void setSha(String sha) {
        this.sha = sha;
    }

    /**
     * getMessage方法。
     *
     * @return String类型返回值
     */
    public String getMessage() {
        return message;
    }

    /**
     * setMessage方法。
     * * @param message String类型参数
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * getTagger方法。
     *
     * @return GitAuthor类型返回值
     */
    public GitAuthor getTagger() {
        return tagger;
    }

    /**
     * setTagger方法。
     * * @param tagger GitAuthor类型参数
     */
    public void setTagger(GitAuthor tagger) {
        this.tagger = tagger;
    }

    /**
     * getType方法。
     *
     * @return Type类型返回值
     */
    public Type getType() {
        return type;
    }

    /**
     * setType方法。
     * * @param type Type类型参数
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return (type == Type.ANNOTATED ? "[annotated] " : "") + name + " " + sha;
    }

    /**
     * Type枚举。
     */
    public enum Type {
        LIGHTWEIGHT, ANNOTATED
    }
}

package com.zifang.util.core.pattern.state;

/**
 * 状态码
 */
public class State {

    private String code;

    private String name;

    /**
     * State方法。
     */
    public State() {
    }

    /**
     * State方法。
     * * @param code String类型参数
     *
     * @param name String类型参数
     */
    public State(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * getCode方法。
     *
     * @return String类型返回值
     */
    public String getCode() {
        return code;
    }

    /**
     * setCode方法。
     * * @param code String类型参数
     */
    public void setCode(String code) {
        this.code = code;
    }

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

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "State{code=" + code + ", name=" + name + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return java.util.Objects.equals(code, state.code) &&
                java.util.Objects.equals(name, state.name);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(code, name);
    }
}
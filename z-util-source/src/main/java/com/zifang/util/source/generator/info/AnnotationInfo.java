package com.zifang.util.source.generator.info;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解信息封装类
 * <p>
 * 用于封装 Java 注解的完整信息，包括注解类型和键值对参数。
 *
 * @author zifang
 */
public class AnnotationInfo {

    /**
     * 注解类型全限定名
     */
    private String type;

    /**
     * 注解参数键值对
     */
    private List<AnnotationMember> members = new ArrayList<>();

    /**
     * AnnotationInfo方法。
     */
    public AnnotationInfo() {
    }

    /**
     * AnnotationInfo方法。
     * * @param type String类型参数
     */
    public AnnotationInfo(String type) {
        this.type = type;
    }

    /**
     * getType方法。
     *
     * @return String类型返回值
     */
    public String getType() {
        return type;
    }

    /**
     * setType方法。
     * * @param type String类型参数
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * getMembers方法。
     *
     * @return List<AnnotationMember>类型返回值
     */
    public List<AnnotationMember> getMembers() {
        return members;
    }

    /**
     * setMembers方法。
     * * @param members ListAnnotationMember类型参数
     */
    public void setMembers(List<AnnotationMember> members) {
        this.members = members;
    }

    /**
     * addMember方法。
     * * @param name String类型参数
     *
     * @param value String类型参数
     */
    public void addMember(String name, String value) {
        members.add(new AnnotationMember(name, value));
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        if (members.isEmpty()) {
            return "@" + type;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("@").append(type).append("(");
        for (int i = 0; i < members.size(); i++) {
            if (i > 0) sb.append(", ");
            AnnotationMember m = members.get(i);
            if (m.name != null && !m.name.isEmpty()) {
                sb.append(m.name).append(" = ");
            }
            sb.append(m.value);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 注解成员键值对
     */
    public static class AnnotationMember {
        private String name;
        private String value;

        /**
         * AnnotationMember方法。
         */
        public AnnotationMember() {
        }

        /**
         * AnnotationMember方法。
         * * @param name String类型参数
         *
         * @param value String类型参数
         */
        public AnnotationMember(String name, String value) {
            this.name = name;
            this.value = value;
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

        /**
         * getValue方法。
         *
         * @return String类型返回值
         */
        public String getValue() {
            return value;
        }

        /**
         * setValue方法。
         * * @param value String类型参数
         */
        public void setValue(String value) {
            this.value = value;
        }
    }
}

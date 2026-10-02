package com.zifang.util.parser.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Proto Message 模型。
 *
 * @author zifang
 */

/**
 * ProtoMessage类。
 */
public class ProtoMessage {

    private final String name;
    private final List<ProtoField> fields;
    private final List<ProtoEnum> enums;
    private final List<ProtoMessage> messages;

    /**
     * ProtoMessage方法。
     * * @param name String类型参数
     */
    public ProtoMessage(String name) {
        this.name = name;
        this.fields = new ArrayList<>();
        this.enums = new ArrayList<>();
        this.messages = new ArrayList<>();
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
     * getFields方法。
     *
     * @return List<ProtoField>类型返回值
     */
    public List<ProtoField> getFields() {
        return fields;
    }

    /**
     * addField方法。
     * * @param field ProtoField类型参数
     */
    public void addField(ProtoField field) {
        fields.add(field);
    }

    /**
     * getEnums方法。
     *
     * @return List<ProtoEnum>类型返回值
     */
    public List<ProtoEnum> getEnums() {
        return enums;
    }

    /**
     * addEnum方法。
     * * @param protoEnum ProtoEnum类型参数
     */
    public void addEnum(ProtoEnum protoEnum) {
        enums.add(protoEnum);
    }

    /**
     * getMessages方法。
     *
     * @return List<ProtoMessage>类型返回值
     */
    public List<ProtoMessage> getMessages() {
        return messages;
    }

    /**
     * addMessage方法。
     * * @param message ProtoMessage类型参数
     */
    public void addMessage(ProtoMessage message) {
        messages.add(message);
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
        ProtoMessage that = (ProtoMessage) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(fields, that.fields) &&
                Objects.equals(enums, that.enums) &&
                Objects.equals(messages, that.messages);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(name, fields, enums, messages);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("message ").append(name).append(" {");
        for (ProtoEnum e : enums) {
            sb.append(" ").append(e.toString());
        }
        for (ProtoMessage m : messages) {
            sb.append(" ").append(m.toString());
        }
        for (ProtoField f : fields) {
            sb.append(" ").append(f.toString());
        }
        sb.append(" }");
        return sb.toString();
    }
}

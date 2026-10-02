package com.zifang.util.parser.proto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Proto 文档根模型，包含 package、import、message、service 等顶级定义。
 *
 * @author zifang
 */

/**
 * ProtoDocument类。
 */
public class ProtoDocument {

    private final List<String> imports;
    private final List<ProtoMessage> messages;
    private final List<ProtoService> services;
    private String syntax;
    private String packageName;

    /**
     * ProtoDocument方法。
     */
    public ProtoDocument() {
        this.imports = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    /**
     * getSyntax方法。
     *
     * @return String类型返回值
     */
    public String getSyntax() {
        return syntax;
    }

    /**
     * setSyntax方法。
     * * @param syntax String类型参数
     */
    public void setSyntax(String syntax) {
        this.syntax = syntax;
    }

    /**
     * getPackageName方法。
     *
     * @return String类型返回值
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * setPackageName方法。
     * * @param packageName String类型参数
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * getImports方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getImports() {
        return Collections.unmodifiableList(imports);
    }

    /**
     * addImport方法。
     * * @param importStmt String类型参数
     */
    public void addImport(String importStmt) {
        imports.add(importStmt);
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

    /**
     * getServices方法。
     *
     * @return List<ProtoService>类型返回值
     */
    public List<ProtoService> getServices() {
        return services;
    }

    /**
     * addService方法。
     * * @param service ProtoService类型参数
     */
    public void addService(ProtoService service) {
        services.add(service);
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
        ProtoDocument that = (ProtoDocument) o;
        return Objects.equals(syntax, that.syntax) &&
                Objects.equals(packageName, that.packageName) &&
                Objects.equals(imports, that.imports) &&
                Objects.equals(messages, that.messages) &&
                Objects.equals(services, that.services);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(syntax, packageName, imports, messages, services);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (syntax != null) {
            sb.append("syntax = \"").append(syntax).append("\";\n");
        }
        if (packageName != null) {
            sb.append("package ").append(packageName).append(";\n");
        }
        for (String imp : imports) {
            sb.append("import \"").append(imp).append("\";\n");
        }
        for (ProtoMessage msg : messages) {
            sb.append(msg.toString()).append("\n");
        }
        for (ProtoService svc : services) {
            sb.append(svc.toString()).append("\n");
        }
        return sb.toString();
    }
}

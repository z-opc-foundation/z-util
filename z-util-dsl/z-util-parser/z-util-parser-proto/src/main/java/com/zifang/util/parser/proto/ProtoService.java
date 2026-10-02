package com.zifang.util.parser.proto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Proto Service 模型。
 *
 * @author zifang
 */

/**
 * ProtoService类。
 */
public class ProtoService {

    private final String name;
    private final List<ProtoRpc> rpcs;

    /**
     * ProtoService方法。
     * * @param name String类型参数
     */
    public ProtoService(String name) {
        this.name = name;
        this.rpcs = new ArrayList<>();
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
     * getRpcs方法。
     *
     * @return List<ProtoRpc>类型返回值
     */
    public List<ProtoRpc> getRpcs() {
        return rpcs;
    }

    /**
     * addRpc方法。
     * * @param rpc ProtoRpc类型参数
     */
    public void addRpc(ProtoRpc rpc) {
        rpcs.add(rpc);
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
        ProtoService that = (ProtoService) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(rpcs, that.rpcs);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(name, rpcs);
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("service ").append(name).append(" {");
        for (ProtoRpc rpc : rpcs) {
            sb.append(" ").append(rpc.toString());
        }
        sb.append(" }");
        return sb.toString();
    }
}

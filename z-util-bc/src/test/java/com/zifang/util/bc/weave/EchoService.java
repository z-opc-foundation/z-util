package com.zifang.util.bc.weave;

/**
 * 代理生成测试的目标类：覆盖普通返回 / 基本类型返回 / void / final / static。
 */
public class EchoService {

    public EchoService() {
    }

    public String echo(String x) {
        return "echo:" + x;
    }

    public int len(String s) {
        return s.length();
    }

    public void run() {
    }

    public final String stable() {
        return "stable";
    }

    public static String stat() {
        return "stat";
    }

    public String explode() {
        throw new IllegalStateException("boom");
    }
}

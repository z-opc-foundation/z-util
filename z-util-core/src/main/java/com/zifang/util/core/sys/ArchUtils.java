package com.zifang.util.core.sys;

/**
 * CPU 架构工具，参考 commons-lang3 的 {@code ArchUtils} 入口提供一致 API。
 * <p>
 * 典型用法：
 * <pre>{@code
 * Processor p = ArchUtils.getProcessor();
 * if (p.isX86()) {
 *     // 选择 x86 指令集
 * }
 * String archName = p.getArch().getArch(); // "amd64" / "aarch64" ...
 * }</pre>
 *
 * @author zifang
 */
public final class ArchUtils {

    private ArchUtils() {
    }

    public static Processor getProcessor() {
        return Processor.getProcessor();
    }
}
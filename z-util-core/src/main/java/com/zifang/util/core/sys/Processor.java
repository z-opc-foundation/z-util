package com.zifang.util.core.sys;

/**
 * 当前进程 CPU 信息。
 * <p>
 * 通过 {@code System.getProperty("os.arch")} 推断当前 JVM 的 CPU 架构，
 * 提供 {@link #getArch()}、{@link #getType()}、{@link #isX86()}、{@link #isPPC()} 等查询接口。
 * <p>
 * 该类为单例（{@link #getProcessor()} 全局共享一个实例），保证多次调用不会重复扫描系统属性。
 *
 * @author zifang
 */
public final class Processor {

    private static final Processor INSTANCE = new Processor();

    private final Arch arch;

    private Processor() {
        this.arch = detectArch();
    }

    public static Processor getProcessor() {
        return INSTANCE;
    }

    private static Arch detectArch() {
        String osArch = System.getProperty("os.arch", "");
        if (osArch == null || osArch.isEmpty()) {
            return Arch.UNKNOWN;
        }
        String lower = osArch.toLowerCase();
        if (containsAny(lower, "amd64", "x86_64", "x64")) {
            return Arch.X86_64;
        }
        if (lower.equals("x86") || lower.startsWith("i386") || lower.startsWith("i486")
                || lower.startsWith("i586") || lower.startsWith("i686")) {
            return Arch.X86;
        }
        if (containsAny(lower, "aarch64", "arm64")) {
            return Arch.AARCH64;
        }
        if (lower.startsWith("arm") || lower.equals("aarch32")) {
            return Arch.ARM;
        }
        if (containsAny(lower, "ppc64", "powerpc64")) {
            return Arch.PPC64;
        }
        if (containsAny(lower, "ppc", "powerpc")) {
            return Arch.PPC;
        }
        if (lower.contains("sparc")) {
            return Arch.SPARC;
        }
        if (lower.contains("mips")) {
            return Arch.MIPS;
        }
        if (containsAny(lower, "riscv", "rv64", "rv32")) {
            return Arch.RISCV;
        }
        if (containsAny(lower, "ia64", "itanium")) {
            return Arch.IA64;
        }
        if (lower.contains("s390")) {
            return Arch.S390;
        }
        return Arch.UNKNOWN;
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public Arch getArch() {
        return arch;
    }

    public Arch getType() {
        return arch;
    }

    public boolean isX86() {
        return arch.isX86();
    }

    public boolean isPPC() {
        return arch.isPPC();
    }
}
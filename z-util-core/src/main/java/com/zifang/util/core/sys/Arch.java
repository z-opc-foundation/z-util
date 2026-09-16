package com.zifang.util.core.sys;

/**
 * CPU 架构枚举。
 * <p>
 * 兼容 {@code System.getProperty("os.arch")} 的常见取值；包含但不限于：
 * <ul>
 *     <li>{@link #X86_64} - amd64 / x86_64 / x64</li>
 *     <li>{@link #X86} - x86 / i386 / i486 / i586 / i686</li>
 *     <li>{@link #AARCH64} - aarch64 / arm64</li>
 *     <li>{@link #ARM} - arm</li>
 *     <li>{@link #PPC64} - ppc64 / powerpc64</li>
 *     <li>{@link #PPC} - ppc / powerpc</li>
 *     <li>{@link #SPARC} - sparc / sparcv9</li>
 *     <li>{@link #MIPS} - mips / mips64</li>
 *     <li>{@link #RISCV} - riscv / riscv64</li>
 *     <li>{@link #IA64} - ia64 / itanium</li>
 *     <li>{@link #S390} - s390 / s390x</li>
 *     <li>{@link #UNKNOWN} - 无法识别的架构</li>
 * </ul>
 *
 * @author zifang
 */
public enum Arch {
    X86("x86"),
    X86_64("amd64"),
    ARM("arm"),
    AARCH64("aarch64"),
    PPC("ppc"),
    PPC64("ppc64"),
    SPARC("sparc"),
    MIPS("mips"),
    RISCV("riscv"),
    IA64("ia64"),
    S390("s390"),
    UNKNOWN("unknown");

    private final String arch;

    Arch(String arch) {
        this.arch = arch;
    }

    public String getArch() {
        return arch;
    }

    public boolean isX86() {
        return this == X86 || this == X86_64;
    }

    public boolean isPPC() {
        return this == PPC || this == PPC64;
    }
}
package com.zifang.util.core.lang.reflect;

import org.junit.Test;

import java.util.Set;

/**
 * ScannerUtil类。
 */
public class ScannerUtil {
    @Test
    /**
     * t方法。
     */
    public void t() {
        Set<Class<?>> set = PackageScanner.searchClasses("com.google");
        for (Class<?> aClass : set) {
            System.out.println(aClass.getName());
        }

        Set<Class<?>> set1 = PackageScanner.searchClasses("com.zifang.util.core.io");
        for (Class<?> aClass : set1) {
            System.out.println(aClass.getName());
        }
    }
}

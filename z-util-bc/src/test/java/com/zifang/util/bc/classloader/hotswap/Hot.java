package com.zifang.util.bc.classloader.hotswap;


/**
 * Hot类。
 */
public class Hot {
    /**
     * hot方法。
     */
    public void hot() {
        String test = "2";
        System.out.println(" version 1 : " + this.getClass().getClassLoader());
    }
}
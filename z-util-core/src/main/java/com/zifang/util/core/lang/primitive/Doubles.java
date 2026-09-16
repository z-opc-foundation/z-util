package com.zifang.util.core.lang.primitive;

import java.text.DecimalFormat;

/**
 * @author zifang
 */
public class Doubles {

    /**
     * 格式化一个float
     *
     * @param format 要格式化成的格式 such as #.00, #.#
     * @return 格式化后的字符串
     */
    public static String formatDouble(double f, String format) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(f);
    }

}

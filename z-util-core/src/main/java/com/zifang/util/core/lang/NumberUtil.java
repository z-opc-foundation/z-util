package com.zifang.util.core.lang;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

import static com.zifang.util.core.lang.StringUtil.isEmpty;
import static com.zifang.util.core.lang.regex.Patterns.FLOATING_POINT_NUMBER_PATTERN;

/**
 * <p>数字处理工具类，提供各种数字转换、比较和检查的工具方法。</p>
 *
 * <p>主要功能包括：</p>
 * <ul>
 *   <li>字符串到数字的转换（int, long, float, double, byte, short, BigDecimal, BigInteger）</li>
 *   <li>数组中的最大值和最小值查找</li>
 *   <li>三个值中的最大值和最小值</li>
 *   <li>数字有效性检查（是否可解析、是否为有效数字）</li>
 *   <li>数字比较（compare 方法）</li>
 * </ul>
 *
 * @author zifang
 */
public class NumberUtil {

    /**
     * Reusable Long cn.zifang.boot.util.crypt.constant for zero.
     */
    public static final Long LONG_ZERO = 0L;
    /**
     * Reusable Long cn.zifang.boot.util.crypt.constant for one.
     */
    public static final Long LONG_ONE = 1L;
    /**
     * Reusable Long cn.zifang.boot.util.crypt.constant for minus one.
     */
    public static final Long LONG_MINUS_ONE = -1L;
    /**
     * Reusable Integer cn.zifang.boot.util.crypt.constant for zero.
     */
    public static final Integer INTEGER_ZERO = 0;
    /**
     * Reusable Integer cn.zifang.boot.util.crypt.constant for one.
     */
    public static final Integer INTEGER_ONE = 1;
    /**
     * Reusable Integer cn.zifang.boot.util.crypt.constant for two
     */
    public static final Integer INTEGER_TWO = 2;
    /**
     * Reusable Integer cn.zifang.boot.util.crypt.constant for minus one.
     */
    public static final Integer INTEGER_MINUS_ONE = -1;
    /**
     * Reusable Short cn.zifang.boot.util.crypt.constant for zero.
     */
    public static final Short SHORT_ZERO = (short) 0;
    /**
     * Reusable Short cn.zifang.boot.util.crypt.constant for one.
     */
    public static final Short SHORT_ONE = (short) 1;
    /**
     * Reusable Short cn.zifang.boot.util.crypt.constant for minus one.
     */
    public static final Short SHORT_MINUS_ONE = (short) -1;
    /**
     * Reusable Byte cn.zifang.boot.util.crypt.constant for zero.
     */
    public static final Byte BYTE_ZERO = (byte) 0;
    /**
     * Reusable Byte cn.zifang.boot.util.crypt.constant for one.
     */
    public static final Byte BYTE_ONE = (byte) 1;
    /**
     * Reusable Byte cn.zifang.boot.util.crypt.constant for minus one.
     */
    public static final Byte BYTE_MINUS_ONE = (byte) -1;
    /**
     * Reusable Double cn.zifang.boot.util.crypt.constant for zero.
     */
    public static final Double DOUBLE_ZERO = 0.0d;
    /**
     * Reusable Double cn.zifang.boot.util.crypt.constant for one.
     */
    public static final Double DOUBLE_ONE = 1.0d;
    /**
     * Reusable Double cn.zifang.boot.util.crypt.constant for minus one.
     */
    public static final Double DOUBLE_MINUS_ONE = -1.0d;
    /**
     * Reusable Float cn.zifang.boot.util.crypt.constant for zero.
     */
    public static final Float FLOAT_ZERO = 0.0f;
    /**
     * Reusable Float cn.zifang.boot.util.crypt.constant for one.
     */
    public static final Float FLOAT_ONE = 1.0f;
    /**
     * Reusable Float cn.zifang.boot.util.crypt.constant for minus one.
     */
    public static final Float FLOAT_MINUS_ONE = -1.0f;

    /**
     * {@link Integer#MAX_VALUE} as a {@link Long}.
     */
    public static final Long LONG_INT_MAX_VALUE = (long) Integer.MAX_VALUE;

    /**
     * {@link Integer#MIN_VALUE} as a {@link Long}.
     */
    public static final Long LONG_INT_MIN_VALUE = (long) Integer.MIN_VALUE;


    /**
     * <p>NumberUtil 类不应以标准编程方式实例化。</p>
     *
     * <p>此类应该作为工具类使用，例如 {@code NumberUtil.toInt("6");}。</p>
     *
     * <p>此构造函数是 public 的，以允许需要 JavaBean 实例的工具进行操作。</p>
     */
    public NumberUtil() {
    }

    //-----------------------------------------------------------------------

    /**
     * <p>将字符串转换为 int 类型，转换失败时返回零。</p>
     *
     * <p>如果字符串为 {@code null}，则返回零。</p>
     *
     * <pre>
     *   NumberUtil.toInt(null) = 0
     *   NumberUtil.toInt("")   = 0
     *   NumberUtil.toInt("1")  = 1
     * </pre>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 字符串对应的 int 值，转换失败时返回零
     */
    public static int toInt(final String str) {
        return toInt(str, 0);
    }

    /**
     * <p>将字符串转换为 int 类型，转换失败时返回指定的默认值。</p>
     *
     * <p>如果字符串为 {@code null}，则返回默认值。</p>
     *
     * <pre>
     *   NumberUtil.toInt(null, 1) = 1
     *   NumberUtil.toInt("", 1)   = 1
     *   NumberUtil.toInt("1", 0)  = 1
     * </pre>
     *
     * @param str          要转换的字符串，可以为 null
     * @param defaultValue 默认值
     * @return 字符串对应的 int 值，转换失败时返回默认值
     */
    public static int toInt(final String str, final int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>将字符串转换为 long 类型，转换失败时返回零。</p>
     *
     * <p>如果字符串为 {@code null}，则返回零。</p>
     *
     * <pre>
     *   NumberUtil.toLong(null) = 0L
     *   NumberUtil.toLong("")   = 0L
     *   NumberUtil.toLong("1")  = 1L
     * </pre>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 字符串对应的 long 值，转换失败时返回零
     */
    public static long toLong(final String str) {
        return toLong(str, 0L);
    }

    /**
     * <p>将字符串转换为 long 类型，转换失败时返回指定的默认值。</p>
     *
     * <p>如果字符串为 {@code null}，则返回默认值。</p>
     *
     * <pre>
     *   NumberUtil.toLong(null, 1L) = 1L
     *   NumberUtil.toLong("", 1L)   = 1L
     *   NumberUtil.toLong("1", 0L)  = 1L
     * </pre>
     *
     * @param str          要转换的字符串，可以为 null
     * @param defaultValue 默认值
     * @return 字符串对应的 long 值，转换失败时返回默认值
     */
    public static long toLong(final String str, final long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>将字符串转换为 float 类型，转换失败时返回 0.0f。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 0.0f。</p>
     *
     * <pre>
     *   NumberUtil.toFloat(null)   = 0.0f
     *   NumberUtil.toFloat("")     = 0.0f
     *   NumberUtil.toFloat("1.5")  = 1.5f
     * </pre>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 字符串对应的 float 值，转换失败时返回 0.0f
     */
    public static float toFloat(final String str) {
        return toFloat(str, 0.0f);
    }

    /**
     * <p>将字符串转换为 float 类型，转换失败时返回指定的默认值。</p>
     *
     * <p>如果字符串为 {@code null}，则返回默认值。</p>
     *
     * <pre>
     *   NumberUtil.toFloat(null, 1.1f)   = 1.0f
     *   NumberUtil.toFloat("", 1.1f)     = 1.1f
     *   NumberUtil.toFloat("1.5", 0.0f)  = 1.5f
     * </pre>
     *
     * @param str          要转换的字符串，可以为 null
     * @param defaultValue 默认值
     * @return 字符串对应的 float 值，转换失败时返回默认值
     */
    public static float toFloat(final String str, final float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>将字符串转换为 double 类型，转换失败时返回 0.0d。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 0.0d。</p>
     *
     * <pre>
     *   NumberUtil.toDouble(null)   = 0.0d
     *   NumberUtil.toDouble("")     = 0.0d
     *   NumberUtil.toDouble("1.5")  = 1.5d
     * </pre>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 字符串对应的 double 值，转换失败时返回 0.0d
     */
    public static double toDouble(final String str) {
        return toDouble(str, 0.0d);
    }

    /**
     * <p>将字符串转换为 double 类型，转换失败时返回指定的默认值。</p>
     *
     * <p>如果字符串为 {@code null}，则返回默认值。</p>
     *
     * <pre>
     *   NumberUtil.toDouble(null, 1.1d)   = 1.1d
     *   NumberUtil.toDouble("", 1.1d)     = 1.1d
     *   NumberUtil.toDouble("1.5", 0.0d)  = 1.5d
     * </pre>
     *
     * @param str          要转换的字符串，可以为 null
     * @param defaultValue 默认值
     * @return 字符串对应的 double 值，转换失败时返回默认值
     */
    public static double toDouble(final String str, final double defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>将 BigDecimal 转换为 double 类型。</p>
     *
     * <p>如果 BigDecimal 值为 {@code null}，则返回指定的默认值 0.0d。</p>
     *
     * <pre>
     *   NumberUtil.toDouble(null)                     = 0.0d
     *   NumberUtil.toDouble(BigDecimal.valueOf(8.5d)) = 8.5d
     * </pre>
     *
     * @param value 要转换的 BigDecimal，可以为 null
     * @return BigDecimal 对应的 double 值，如果为 null 则返回 0.0d
     */
    public static double toDouble(final BigDecimal value) {
        return toDouble(value, 0.0d);
    }

    /**
     * <p>将 BigDecimal 转换为 double 类型。</p>
     *
     * <p>如果 BigDecimal 值为 {@code null}，则返回指定的默认值。</p>
     *
     * <pre>
     *   NumberUtil.toDouble(null, 1.1d)                     = 1.1d
     *   NumberUtil.toDouble(BigDecimal.valueOf(8.5d), 1.1d) = 8.5d
     * </pre>
     *
     * @param value        要转换的 BigDecimal，可以为 null
     * @param defaultValue 默认值
     * @return BigDecimal 对应的 double 值，如果为 null 则返回默认值
     */
    public static double toDouble(final BigDecimal value, final double defaultValue) {
        return value == null ? defaultValue : value.doubleValue();
    }

    //-----------------------------------------------------------------------

    /**
     * <p>将字符串转换为 byte 类型，转换失败时返回零。</p>
     *
     * <p>如果字符串为 {@code null}，则返回零。</p>
     *
     * <pre>
     *   NumberUtil.toByte(null) = 0
     *   NumberUtil.toByte("")   = 0
     *   NumberUtil.toByte("1")  = 1
     * </pre>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 字符串对应的 byte 值，转换失败时返回零
     */
    public static byte toByte(final String str) {
        return toByte(str, (byte) 0);
    }

    /**
     * <p>将字符串转换为 byte 类型，转换失败时返回指定的默认值。</p>
     *
     * <p>如果字符串为 {@code null}，则返回默认值。</p>
     *
     * <pre>
     *   NumberUtil.toByte(null, 1) = 1
     *   NumberUtil.toByte("", 1)   = 1
     *   NumberUtil.toByte("1", 0)  = 1
     * </pre>
     *
     * @param str          要转换的字符串，可以为 null
     * @param defaultValue 默认值
     * @return 字符串对应的 byte 值，转换失败时返回默认值
     */
    public static byte toByte(final String str, final byte defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Byte.parseByte(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>将字符串转换为 short 类型，转换失败时返回零。</p>
     *
     * <p>如果字符串为 {@code null}，则返回零。</p>
     *
     * <pre>
     *   NumberUtil.toShort(null) = 0
     *   NumberUtil.toShort("")   = 0
     *   NumberUtil.toShort("1")  = 1
     * </pre>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 字符串对应的 short 值，转换失败时返回零
     */
    public static short toShort(final String str) {
        return toShort(str, (short) 0);
    }

    /**
     * <p>将字符串转换为 short 类型，转换失败时返回指定的默认值。</p>
     *
     * <p>如果字符串为 {@code null}，则返回默认值。</p>
     *
     * <pre>
     *   NumberUtil.toShort(null, 1) = 1
     *   NumberUtil.toShort("", 1)   = 1
     *   NumberUtil.toShort("1", 0)  = 1
     * </pre>
     *
     * @param str          要转换的字符串，可以为 null
     * @param defaultValue 默认值
     * @return 字符串对应的 short 值，转换失败时返回默认值
     */
    public static short toShort(final String str, final short defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Short.parseShort(str);
        } catch (final NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * <p>将 BigDecimal 转换为指定小数位数的 BigDecimal，使用 RoundingMode.HALF_EVEN 进行舍入。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * <p>注意：BigDecimal 的 scale 是小数点后的位数。</p>
     *
     * @param value 要转换的 BigDecimal，可以为 null
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final BigDecimal value) {
        return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * <p>将 BigDecimal 转换为指定小数位数的 BigDecimal，并应用指定的舍入模式。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * @param value        要转换的 BigDecimal，可以为 null
     * @param scale        小数点后的位数
     * @param roundingMode 舍入模式
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final BigDecimal value, final int scale,
                                                final RoundingMode roundingMode) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(scale, (roundingMode == null) ? RoundingMode.HALF_EVEN : roundingMode
        );
    }

    /**
     * <p>将 Float 转换为指定小数位数的 BigDecimal，使用 RoundingMode.HALF_EVEN 进行舍入。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * <p>注意：BigDecimal 的 scale 是小数点后的位数。</p>
     *
     * @param value 要转换的 Float，可以为 null
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final Float value) {
        return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * <p>将 Float 转换为指定小数位数的 BigDecimal，并应用指定的舍入模式。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * @param value        要转换的 Float，可以为 null
     * @param scale        小数点后的位数
     * @param roundingMode 舍入模式
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final Float value, final int scale,
                                                final RoundingMode roundingMode) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return toScaledBigDecimal(BigDecimal.valueOf(value), scale, roundingMode);
    }

    /**
     * <p>将 Double 转换为指定小数位数的 BigDecimal，使用 RoundingMode.HALF_EVEN 进行舍入。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * <p>注意：BigDecimal 的 scale 是小数点后的位数。</p>
     *
     * @param value 要转换的 Double，可以为 null
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final Double value) {
        return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * <p>将 Double 转换为指定小数位数的 BigDecimal，并应用指定的舍入模式。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * @param value        要转换的 Double，可以为 null
     * @param scale        小数点后的位数
     * @param roundingMode 舍入模式
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final Double value, final int scale,
                                                final RoundingMode roundingMode) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return toScaledBigDecimal(
                BigDecimal.valueOf(value),
                scale,
                roundingMode
        );
    }

    /**
     * <p>将字符串转换为指定小数位数的 BigDecimal，使用 RoundingMode.HALF_EVEN 进行舍入。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * <p>注意：BigDecimal 的 scale 是小数点后的位数。</p>
     *
     * @param value 要转换的字符串，可以为 null
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final String value) {
        return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
    }

    /**
     * <p>将字符串转换为指定小数位数的 BigDecimal，并应用指定的舍入模式。</p>
     *
     * <p>如果输入值为 {@code null}，则返回 BigDecimal.ZERO。</p>
     *
     * @param value        要转换的字符串，可以为 null
     * @param scale        小数点后的位数
     * @param roundingMode 舍入模式
     * @return 舍入后的 BigDecimal
     */
    public static BigDecimal toScaledBigDecimal(final String value, final int scale,
                                                final RoundingMode roundingMode) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return toScaledBigDecimal(
                createBigDecimal(value),
                scale,
                roundingMode
        );
    }

    //-----------------------------------------------------------------------
    // must handle Long, Float, Integer, Float, Short,
    //                  BigDecimal, BigInteger and Byte
    // useful methods:
    // Byte.decode(String)
    // Byte.valueOf(String, int radix)
    // Byte.valueOf(String)
    // Double.valueOf(String)
    // Float.valueOf(String)
    // Float.valueOf(String)
    // Integer.valueOf(String, int radix)
    // Integer.valueOf(String)
    // Integer.decode(String)
    // Integer.getInteger(String)
    // Integer.getInteger(String, int val)
    // Integer.getInteger(String, Integer val)
    // Integer.valueOf(String)
    // Double.valueOf(String)
    // new Byte(String)
    // Long.valueOf(String)
    // Long.getLong(String)
    // Long.getLong(String, int)
    // Long.getLong(String, Integer)
    // Long.valueOf(String, int)
    // Long.valueOf(String)
    // Short.valueOf(String)
    // Short.decode(String)
    // Short.valueOf(String, int)
    // Short.valueOf(String)
    // new BigDecimal(String)
    // new BigInteger(String)
    // new BigInteger(String, int radix)
    // Possible inputs:
    // 45 45.5 45E7 4.5E7 Hex Oct Binary xxxF xxxD xxxf xxxd
    // plus minus everything. Probably more. A lot are not separable.

    /**
     * <p>将字符串转换为 java.lang.Number 对象。</p>
     *
     * <p>如果字符串以 {@code 0x}、{@code -0x}（大小写不敏感）或 {@code #}、{@code -#} 开头，
     * 则将其解析为十六进制整数；如果数字部分超过8位，则解析为 Long；如果超过16位，则解析为 BigInteger。</p>
     *
     * <p>然后检查字符串末尾是否有类型限定符，即 {@code 'f', 'F', 'd', 'D', 'l', 'L'} 之一。
     * 如果找到，则依次尝试从指定类型创建更大的类型，直到找到能表示该值的类型。</p>
     *
     * <p>如果没有找到类型限定符，则检查是否有小数点，然后依次尝试从 {@code Integer} 到 {@code BigInteger}
     * 以及从 {@code Float} 到 {@code BigDecimal} 的更大类型。</p>
     *
     * <p>以 leading {@code 0} 开头的整数值将被解析为八进制；返回的数字将根据情况是 Integer、Long 或 BigDecimal。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 {@code null}。</p>
     *
     * <p>此方法不会去除输入字符串的首尾空格，即带有前导或尾随空格的字符串会抛出 NumberFormatException。</p>
     *
     * @param str 包含数字的字符串，可以为 null
     * @return 从字符串创建的 Number（如果输入为 null 则返回 null）
     * @throws NumberFormatException 如果值无法转换
     */
    public static Number createNumber(final String str) {
        if (str == null) {
            return null;
        }
        if (StringUtil.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        // need to deal with all possible hex prefixes here
        final String[] hexPrefixes = {"0x", "0X", "-0x", "-0X", "#", "-#"};
        final int length = str.length();
        int pfxLen = 0;
        for (final String pfx : hexPrefixes) {
            if (str.startsWith(pfx)) {
                pfxLen += pfx.length();
                break;
            }
        }
        // we have a hex number
        if (pfxLen > 0) {
            // strip leading zeroes
            char firstSigDigit = 0;
            for (int i = pfxLen; i < length; i++) {
                firstSigDigit = str.charAt(i);
                // count leading zeroes
                if (firstSigDigit == '0') {
                    pfxLen++;
                } else {
                    break;
                }
            }
            final int hexDigits = length - pfxLen;
            // too many for Long
            if (hexDigits > 16 || hexDigits == 16 && firstSigDigit > '7') {
                return createBigInteger(str);
            }
            // too many for an int
            if (hexDigits > 8 || hexDigits == 8 && firstSigDigit > '7') {
                return createLong(str);
            }
            return createInteger(str);
        }
        final char lastChar = str.charAt(length - 1);
        final String man;
        final String dec;
        final String exp;
        final int decPos = str.indexOf('.');
        // assumes both not present
        final int expPos = str.indexOf('e') + str.indexOf('E') + 1;
        // if both e and E are present, this is caught by the checks on expPos (which prevent IOOBE)
        // and the parsing which will detect if e or E appear in a number due to using the wrong offset

        // there is a decimal point
        if (decPos > -1) {
            // there is an exponent
            if (expPos > -1) {
                // prevents double exponent causing IOOBE
                if (expPos < decPos || expPos > length) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                dec = str.substring(decPos + 1, expPos);
            } else {
                dec = str.substring(decPos + 1);
            }
            man = getMantissa(str, decPos);
        } else {
            if (expPos > -1) {
                // prevents double exponent causing IOOBE
                if (expPos > length) {
                    throw new NumberFormatException(str + " is not a valid number.");
                }
                man = getMantissa(str, expPos);
            } else {
                man = getMantissa(str);
            }
            dec = null;
        }
        if (!Character.isDigit(lastChar) && lastChar != '.') {
            if (expPos > -1 && expPos < length - 1) {
                exp = str.substring(expPos + 1, length - 1);
            } else {
                exp = null;
            }
            // Requesting a specific type
            final String numeric = str.substring(0, length - 1);
            final boolean allZeros = isAllZeros(man) && isAllZeros(exp);
            switch (lastChar) {
                case 'l':
                case 'L':
                    if (dec == null && exp == null && (
                            !numeric.isEmpty() && numeric.charAt(0) == '-' && isDigits(numeric.substring(1))
                                    || isDigits(numeric))) {
                        try {
                            return createLong(numeric);
                        } catch (final NumberFormatException nfe) {
                            // Too big for a long
                        }
                        return createBigInteger(numeric);

                    }
                    throw new NumberFormatException(str + " is not a valid number.");
                case 'f':
                case 'F':
                    try {
                        final Float f = createFloat(str);
                        if (!(f.isInfinite() || f == 0.0F && !allZeros)) {
                            // If it's too big for a float or the float value = 0 and the string
                            // has non-zeros in it, then float does not have the precision we want
                            return f;
                        }

                    } catch (final NumberFormatException nfe) {
                        // ignore the bad number
                    }
                    // $FALL-THROUGH$
                case 'd':
                case 'D':
                    try {
                        final Double d = createDouble(str);
                        if (!(d.isInfinite() || d == 0.0D && !allZeros)) {
                            return d;
                        }
                    } catch (final NumberFormatException nfe) {
                        // ignore the bad number
                    }
                    try {
                        return createBigDecimal(numeric);
                    } catch (final NumberFormatException e) {
                        // ignore the bad number
                    }
                    // $FALL-THROUGH$
                default:
                    throw new NumberFormatException(str + " is not a valid number.");

            }
        }
        // ser doesn't have a preference on the return type, so let's start
        // small and go from there...
        if (expPos > -1 && expPos < length - 1) {
            exp = str.substring(expPos + 1);
        } else {
            exp = null;
        }
        if (dec == null && exp == null) {
            // no decimal point and no exponent
            // Must be an Integer, Long, Biginteger
            try {
                return createInteger(str);
            } catch (final NumberFormatException nfe) {
                // ignore the bad number
            }
            try {
                return createLong(str);
            } catch (final NumberFormatException nfe) {
                // ignore the bad number
            }
            return createBigInteger(str);
        }

        // must be a Float, Double, BigDecimal
        final boolean allZeros = isAllZeros(man) && isAllZeros(exp);
        try {
            final Float f = createFloat(str);
            final Double d = createDouble(str);
            if (!f.isInfinite()
                    && !(f == 0.0F && !allZeros)
                    && f.toString().equals(d.toString())) {
                return f;
            }
            if (!d.isInfinite() && !(d == 0.0D && !allZeros)) {
                final BigDecimal b = createBigDecimal(str);
                if (b.compareTo(BigDecimal.valueOf(d)) == 0) {
                    return d;
                }
                return b;
            }
        } catch (final NumberFormatException nfe) {
            // ignore the bad number
        }
        return createBigDecimal(str);
    }

    /**
     * <p>Utility method for {@link #createNumber(String)}.</p>
     *
     * <p>Returns mantissa of the given number.</p>
     *
     * @param str the string representation of the number
     * @return mantissa of the given number
     */
    private static String getMantissa(final String str) {
        return getMantissa(str, str.length());
    }

    /**
     * <p>Utility method for {@link #createNumber(String)}.</p>
     *
     * <p>Returns mantissa of the given number.</p>
     *
     * @param str     the string representation of the number
     * @param stopPos the position of the exponent or decimal point
     * @return mantissa of the given number
     */
    private static String getMantissa(final String str, final int stopPos) {
        final char firstChar = str.charAt(0);
        final boolean hasSign = firstChar == '-' || firstChar == '+';

        return hasSign ? str.substring(1, stopPos) : str.substring(0, stopPos);
    }

    /**
     * <p>Utility method for {@link #createNumber(String)}.</p>
     *
     * <p>Returns {@code true} if s is {@code null}.</p>
     *
     * @param str the String to check
     * @return if it is all zeros or {@code null}
     */
    private static boolean isAllZeros(final String str) {
        if (str == null) {
            return true;
        }
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != '0') {
                return false;
            }
        }
        return !str.isEmpty();
    }

    //-----------------------------------------------------------------------

    /**
     * <p>将字符串转换为 Float 类型。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 {@code null}。</p>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 转换后的 Float（如果输入为 null 则返回 null）
     * @throws NumberFormatException 如果值无法转换
     */
    public static Float createFloat(final String str) {
        if (str == null) {
            return null;
        }
        return Float.valueOf(str);
    }

    /**
     * <p>将字符串转换为 Double 类型。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 {@code null}。</p>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 转换后的 Double（如果输入为 null 则返回 null）
     * @throws NumberFormatException 如果值无法转换
     */
    public static Double createDouble(final String str) {
        if (str == null) {
            return null;
        }
        return Double.valueOf(str);
    }

    /**
     * <p>将字符串转换为 Integer 类型，支持十六进制（0xhhhh）和八进制（0dddd）表示法。
     * 注意：前导零表示八进制；不会去除空格。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 {@code null}。</p>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 转换后的 Integer（如果输入为 null 则返回 null）
     * @throws NumberFormatException 如果值无法转换
     */
    public static Integer createInteger(final String str) {
        if (str == null) {
            return null;
        }
        // decode() handles 0xAABD and 0777 (hex and octal) as well.
        return Integer.decode(str);
    }

    /**
     * <p>将字符串转换为 Long 类型；自 3.1 起支持十六进制（0Xhhhh）和八进制（0ddd）表示法。
     * 注意：前导零表示八进制；不会去除空格。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 {@code null}。</p>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 转换后的 Long（如果输入为 null 则返回 null）
     * @throws NumberFormatException 如果值无法转换
     */
    public static Long createLong(final String str) {
        if (str == null) {
            return null;
        }
        return Long.decode(str);
    }

    /**
     * <p>将字符串转换为 BigInteger 类型；自 3.2 起支持十六进制（0x 或 #）和八进制（0）表示法。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 {@code null}。</p>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 转换后的 BigInteger（如果输入为 null 则返回 null）
     * @throws NumberFormatException 如果值无法转换
     */
    public static BigInteger createBigInteger(final String str) {
        if (str == null) {
            return null;
        }
        // offset within string
        int pos = 0;
        int radix = 10;
        // need to negate later
        boolean negate = false;
        if (str.startsWith("-")) {
            negate = true;
            pos = 1;
        }
        // hex
        if (str.startsWith("0x", pos) || str.startsWith("0X", pos)) {
            radix = 16;
            pos += 2;
        }
        // alternative hex (allowed by Long/Integer)
        else if (str.startsWith("#", pos)) {
            radix = 16;
            pos++;
        }
        // octal; so long as there are additional digits
        else if (str.startsWith("0", pos) && str.length() > pos + 1) {
            radix = 8;
            pos++;
            // default is to treat as decimal
        }

        final BigInteger value = new BigInteger(str.substring(pos), radix);
        return negate ? value.negate() : value;
    }

    /**
     * <p>将字符串转换为 BigDecimal 类型。</p>
     *
     * <p>如果字符串为 {@code null}，则返回 {@code null}。</p>
     *
     * @param str 要转换的字符串，可以为 null
     * @return 转换后的 BigDecimal（如果输入为 null 则返回 null）
     * @throws NumberFormatException 如果值无法转换
     */
    public static BigDecimal createBigDecimal(final String str) {
        if (str == null) {
            return null;
        }
        // handle JDK1.3.1 bug where "" throws IndexOutOfBoundsException
        if (StringUtil.isBlank(str)) {
            throw new NumberFormatException("A blank string is not a valid number");
        }
        return new BigDecimal(str);
    }

    // Min in array
    //--------------------------------------------------------------------

    /**
     * <p>返回数组中的最小值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最小值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static long min(final long... array) {
        // Validates input
        validateArray(array);

        // Finds and returns min
        long min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * <p>返回数组中的最小值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最小值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static int min(final int... array) {
        // Validates input
        validateArray(array);

        // Finds and returns min
        int min = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] < min) {
                min = array[j];
            }
        }

        return min;
    }

    /**
     * <p>返回数组中的最小值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最小值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static short min(final short... array) {
        // Validates input
        validateArray(array);

        // Finds and returns min
        short min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * <p>返回数组中的最小值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最小值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static byte min(final byte... array) {
        // Validates input
        validateArray(array);

        // Finds and returns min
        byte min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * <p>返回数组中的最小值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最小值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static double min(final double... array) {
        // Validates input
        validateArray(array);

        // Finds and returns min
        double min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Double.isNaN(array[i])) {
                return Double.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    /**
     * <p>返回数组中的最小值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最小值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static float min(final float... array) {
        // Validates input
        validateArray(array);

        // Finds and returns min
        float min = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Float.isNaN(array[i])) {
                return Float.NaN;
            }
            if (array[i] < min) {
                min = array[i];
            }
        }

        return min;
    }

    // Max in array
    //--------------------------------------------------------------------

    /**
     * <p>返回数组中的最大值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最大值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static long max(final long... array) {
        // Validates input
        validateArray(array);

        // Finds and returns max
        long max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    /**
     * <p>返回数组中的最大值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最大值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static int max(final int... array) {
        // Validates input
        validateArray(array);

        // Finds and returns max
        int max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    /**
     * <p>返回数组中的最大值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最大值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static short max(final short... array) {
        // Validates input
        validateArray(array);

        // Finds and returns max
        short max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    /**
     * <p>返回数组中的最大值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最大值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static byte max(final byte... array) {
        // Validates input
        validateArray(array);

        // Finds and returns max
        byte max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }

        return max;
    }

    /**
     * <p>返回数组中的最大值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最大值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static double max(final double... array) {
        // Validates input
        validateArray(array);

        // Finds and returns max
        double max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Double.isNaN(array[j])) {
                return Double.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    /**
     * <p>返回数组中的最大值。</p>
     *
     * @param array 输入数组，不能为 null 或空
     * @return 数组中的最大值
     * @throws IllegalArgumentException 如果数组为 null
     * @throws IllegalArgumentException 如果数组为空
     */
    public static float max(final float... array) {
        // Validates input
        validateArray(array);

        // Finds and returns max
        float max = array[0];
        for (int j = 1; j < array.length; j++) {
            if (Float.isNaN(array[j])) {
                return Float.NaN;
            }
            if (array[j] > max) {
                max = array[j];
            }
        }

        return max;
    }

    /**
     * Checks if the specified array is neither null nor empty.
     *
     * @param array the array to check
     * @throws IllegalArgumentException if {@code array} is either {@code null} or empty
     */
    private static void validateArray(final Object array) {
        notNull(array);
        isTrue(Array.getLength(array) != 0);
    }

    // 3 param min
    //-----------------------------------------------------------------------

    /**
     * <p>获取三个 long 值中的最小值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最小值
     */
    public static long min(long a, final long b, final long c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 int 值中的最小值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最小值
     */
    public static int min(int a, final int b, final int c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 short 值中的最小值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最小值
     */
    public static short min(short a, final short b, final short c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 byte 值中的最小值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最小值
     */
    public static byte min(byte a, final byte b, final byte c) {
        if (b < a) {
            a = b;
        }
        if (c < a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 double 值中的最小值。</p>
     *
     * <p>如果任一值为 {@code NaN}，则返回 {@code NaN}。
     * Infinity 会被正确处理。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最小值
     */
    public static double min(final double a, final double b, final double c) {
        return Math.min(Math.min(a, b), c);
    }

    /**
     * <p>获取三个 float 值中的最小值。</p>
     *
     * <p>如果任一值为 {@code NaN}，则返回 {@code NaN}。
     * Infinity 会被正确处理。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最小值
     */
    public static float min(final float a, final float b, final float c) {
        return Math.min(Math.min(a, b), c);
    }

    // 3 param max
    //-----------------------------------------------------------------------

    /**
     * <p>获取三个 long 值中的最大值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最大值
     */
    public static long max(long a, final long b, final long c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 int 值中的最大值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最大值
     */
    public static int max(int a, final int b, final int c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 short 值中的最大值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最大值
     */
    public static short max(short a, final short b, final short c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 byte 值中的最大值。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最大值
     */
    public static byte max(byte a, final byte b, final byte c) {
        if (b > a) {
            a = b;
        }
        if (c > a) {
            a = c;
        }
        return a;
    }

    /**
     * <p>获取三个 double 值中的最大值。</p>
     *
     * <p>如果任一值为 {@code NaN}，则返回 {@code NaN}。
     * Infinity 会被正确处理。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最大值
     */
    public static double max(final double a, final double b, final double c) {
        return Math.max(Math.max(a, b), c);
    }

    /**
     * <p>获取三个 float 值中的最大值。</p>
     *
     * <p>如果任一值为 {@code NaN}，则返回 {@code NaN}。
     * Infinity 会被正确处理。</p>
     *
     * @param a 第一个值
     * @param b 第二个值
     * @param c 第三个值
     * @return 三个值中的最大值
     */
    public static float max(final float a, final float b, final float c) {
        return Math.max(Math.max(a, b), c);
    }

    //-----------------------------------------------------------------------

    /**
     * <p>检查字符串是否只包含数字字符。</p>
     *
     * <p>null 和空字符串将返回 {@code false}。</p>
     *
     * @param str 要检查的字符串
     * @return 如果字符串只包含 Unicode 数字字符则返回 true
     */
    public static boolean isDigits(final String str) {
        return StringUtil.isNumeric(str);
    }

    /**
     * <p>检查字符串是否为有效的 Java 数字。</p>
     *
     * <p>有效数字包括：十六进制（以 {@code 0x} 或 {@code 0X} 开头）、
     * 八进制、科学计数法以及带类型限定符的数字（如 123L）。</p>
     *
     * <p>以前导零开头的非十六进制字符串被视为八进制值。因此字符串 {@code 09} 将返回 {@code false}，
     * 因为 {@code 9} 不是有效的八进制值。但是，以 {@code 0.} 开头的数字被视为十进制。</p>
     *
     * <p>{@code null} 和空/空白字符串将返回 {@code false}。</p>
     *
     * <p>注意：对于每个导致此方法返回 {@code true} 的输入，{@link #createNumber(String)} 应该返回一个数字。</p>
     *
     * @param str 要检查的字符串
     * @return 如果字符串格式正确则返回 true
     */
    public static boolean isCreatable(final String str) {
        if (isEmpty(str)) {
            return false;
        }
        final char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = chars[0] == '-' || chars[0] == '+' ? 1 : 0;
        // leading 0, skip if is a decimal number
        if (sz > start + 1 && chars[start] == '0' && !StringUtil.contains(str, '.')) {
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                            && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            } else if (Character.isDigit(chars[start + 1])) {
                // leading 0, but not hex, must be octal
                int i = start + 1;
                for (; i < chars.length; i++) {
                    if (chars[i] < '0' || chars[i] > '7') {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                    && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                    || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    /**
     * <p>检查给定的字符串是否是可以解析的数字。</p>
     *
     * <p>可解析的数字包括那些可以由 {@link Integer#parseInt(String)}、
     * {@link Long#parseLong(String)}、{@link Float#parseFloat(String)} 或
     * {@link Double#parseDouble(String)} 解析的字符串。
     * 在调用这些方法时，可以使用方法代替捕获 {@link java.text.ParseException}。</p>
     *
     * <p>十六进制和科学计数法表示的数字被认为是<strong>不可</strong>解析的。
     * 有关这些情况请参阅 {@link #isCreatable(String)}。</p>
     *
     * <p>{@code null} 和空字符串将返回 {@code false}。</p>
     *
     * @param str 要检查的字符串
     * @return 如果字符串是可解析的数字则返回 true
     */
    public static boolean isParsable(final String str) {
        if (isEmpty(str)) {
            return false;
        }
        if (str.charAt(str.length() - 1) == '.') {
            return false;
        }
        if (str.charAt(0) == '-') {
            if (str.length() == 1) {
                return false;
            }
            return withDecimalsParsing(str, 1);
        }
        return withDecimalsParsing(str, 0);
    }

    private static boolean withDecimalsParsing(final String str, final int beginIdx) {
        int decimalPoints = 0;
        for (int i = beginIdx; i < str.length(); i++) {
            final boolean isDecimalPoint = str.charAt(i) == '.';
            if (isDecimalPoint) {
                decimalPoints++;
            }
            if (decimalPoints > 1) {
                return false;
            }
            if (!isDecimalPoint && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>比较两个 int 值的大小。这是 Java 7 提供的相同功能。</p>
     *
     * @param x 第一个 int 值
     * @param y 第二个 int 值
     * @return 如果 {@code x == y} 则返回 0；如果 {@code x < y} 则返回小于0的值；如果 {@code x > y} 则返回大于0的值
     */
    public static int compare(final int x, final int y) {
        if (x == y) {
            return 0;
        }
        return x < y ? -1 : 1;
    }

    /**
     * <p>比较两个 long 值的大小。这是 Java 7 提供的相同功能。</p>
     *
     * @param x 第一个 long 值
     * @param y 第二个 long 值
     * @return 如果 {@code x == y} 则返回 0；如果 {@code x < y} 则返回小于0的值；如果 {@code x > y} 则返回大于0的值
     */
    public static int compare(final long x, final long y) {
        if (x == y) {
            return 0;
        }
        return x < y ? -1 : 1;
    }

    /**
     * <p>比较两个 short 值的大小。这是 Java 7 提供的相同功能。</p>
     *
     * @param x 第一个 short 值
     * @param y 第二个 short 值
     * @return 如果 {@code x == y} 则返回 0；如果 {@code x < y} 则返回小于0的值；如果 {@code x > y} 则返回大于0的值
     */
    public static int compare(final short x, final short y) {
        if (x == y) {
            return 0;
        }
        return x < y ? -1 : 1;
    }

    /**
     * <p>比较两个 byte 值的大小。这是 Java 7 提供的相同功能。</p>
     *
     * @param x 第一个 byte 值
     * @param y 第二个 byte 值
     * @return 如果 {@code x == y} 则返回 0；如果 {@code x < y} 则返回小于0的值；如果 {@code x > y} 则返回大于0的值
     */
    public static int compare(final byte x, final byte y) {
        return x - y;
    }

    private static <T> void notNull(final T object, final Object... values) {
        Objects.requireNonNull(object, () -> String.format("array", values));
    }

    private static void isTrue(final boolean expression, final Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format("Array cannot be empty.", values));
        }
    }

    /**
     * <p>检查字符串是否为浮点数。</p>
     *
     * <p>浮点数格式包括：正数、负数、小数、科学计数法等。</p>
     *
     * @param str 待检查的字符串
     * @return 如果字符串是浮点数格式则返回 true，否则返回 false
     */
    public static boolean isFloatingPointNumber(String str) {
        return FLOATING_POINT_NUMBER_PATTERN.matcher(str).matches();
    }

}

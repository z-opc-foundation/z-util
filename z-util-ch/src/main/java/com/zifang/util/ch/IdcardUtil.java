package com.zifang.util.ch;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 身份证号码工具类
 * <p>
 * 提供身份证号码的验证和解析功能，支持15位和18位身份证号码。
 * <p>
 * 15位身份证号码：第7、8位为出生年份（两位数），第9、10位为出生月份，第11、12位代表出生日期，第15位代表性别，奇数为男，偶数为女。
 * <p>
 * 18位身份证号码：第7、8、9、10位为出生年份（四位数），第11、第12位为出生月份，第13、14位代表出生日期，第17位代表性别，奇数为男，偶数为女。
 *
 * @author zifang
 */
public class IdcardUtil {

    /**
     * 省，直辖市代码表
     */
    private static final String[] PROVINCE_CODES = {
            "11:北京", "12:天津", "13:河北", "14:山西", "15:内蒙古",
            "21:辽宁", "22:吉林", "23:黑龙江", "31:上海", "32:江苏",
            "33:浙江", "34:安徽", "35:福建", "36:江西", "37:山东", "41:河南",
            "42:湖北", "43:湖南", "44:广东", "45:广西", "46:海南", "50:重庆",
            "51:四川", "52:贵州", "53:云南", "54:西藏", "61:陕西", "62:甘肃",
            "63:青海", "64:宁夏", "65:新疆", "71:台湾", "81:香港", "82:澳门", "91:国外"
    };

    /**
     * 每位加权因子
     */
    private static int[] power = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 省份代码与名称的映射，用于根据地址码前两位查询省份
     */
    private static final Map<String, String> PROVINCE_CODE_MAP = new HashMap<>();

    static {
        for (String entry : PROVINCE_CODES) {
            int idx = entry.indexOf(':');
            PROVINCE_CODE_MAP.put(entry.substring(0, idx), entry.substring(idx + 1));
        }
    }

    /**
     * 验证身份证是否合法
     * <p>
     * 该方法验证传入的身份证号码是否合法，支持15位和18位身份证的验证。
     * 15位身份证验证省份与出生日期；18位身份证在省份、出生日期基础上额外验证校验位。
     *
     * @param idcard 待验证的身份证号码
     * @return 如果身份证合法返回true，否则返回false
     */
    public static boolean isValidatedAllIdcard(String idcard) {
        if (idcard == null || idcard.isEmpty()) {
            return false;
        }
        if (idcard.length() == 15) {
            return validate15Idcard(idcard);
        }
        if (idcard.length() == 18) {
            return isValidate18Idcard(idcard);
        }
        return false;
    }

    /**
     * 判断18位身份证的合法性
     * <p>
     * 根据《中华人民共和国国家标准GB11643-1999》中有关公民身份号码的规定进行验证。
     * 公民身份号码是特征组合码，由十七位数字本体码和一位数字校验码组成。
     * <p>
     * 排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码。
     * <p>
     * 顺序码：表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号，
     * 顺序码的奇数分配给男性，偶数分配给女性。
     * <p>
     * 第十八位数字（校验码）的计算方法：
     * <ol>
     *   <li>将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：
     *       7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2</li>
     *   <li>将这17位数字和系数相乘的结果相加</li>
     *   <li>用加出来和除以11，看余数是多少</li>
     *   <li>余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字，其分别对应的最后一位身份证的号码为
     *       1 0 X 9 8 7 6 5 4 3 2</li>
     *   <li>如果余数是2，就会在身份证的第18位数字上出现罗马数字的Ⅹ。如果余数是10，身份证的最后一位号码就是2</li>
     * </ol>
     *
     * @param idcard 待验证的18位身份证号码
     * @return 如果是合法的身份证返回true，否则返回false
     */
    public static boolean isValidate18Idcard(String idcard) {
        if (idcard == null) {
            return false;
        }
        // 非18位为假
        if (idcard.length() != 18) {
            return false;
        }
        // 获取前17位
        String idcard17 = idcard.substring(0, 17);
        // 获取第18位
        String idcard18Code = idcard.substring(17, 18);

        // 前17位全部为数字
        if (!isDigital(idcard17)) {
            return false;
        }

        // 校验省份代码
        if (!checkProvinceCode(idcard.substring(0, 2))) {
            return false;
        }

        // 校验出生日期（第7~14位，yyyyMMdd）
        if (!isValidBirthday(idcard.substring(6, 14), false)) {
            return false;
        }

        int[] bit = converCharToInt(idcard17.toCharArray());
        int sum17 = getPowerSum(bit);
        // 将和值与11取模得到余数进行校验码判断
        String checkCode = getCheckCodeBySum(sum17);
        if (null == checkCode) {
            return false;
        }
        // 将身份证的第18位与算出来的校码进行匹配，不相等就为假
        return idcard18Code.equalsIgnoreCase(checkCode);
    }

    /**
     * 验证18位身份证号码的基本数字和位数格式
     * <p>
     * 该方法仅验证身份证号码的格式是否符合18位身份证的标准格式要求，
     * 不验证校验位是否正确。如需完整验证，请使用 {@link #isValidate18Idcard(String)} 方法。
     *
     * @param idcard 待验证的身份证号码
     * @return 如果格式符合18位身份证标准返回true，否则返回false
     */
    public static boolean is18Idcard(String idcard) {
        return Pattern.matches("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([\\d|x|X]{1})$", idcard);
    }

    /**
     * 判断15位身份证的合法性
     * <p>
     * 15位身份证号码由六位数字地址码、六位数字出生日期码（年份为两位）和三位数字顺序码组成。
     * 校验内容包括：位数、是否全为数字、省份代码、出生日期。
     *
     * @param idcard 待验证的15位身份证号码
     * @return 如果是合法的15位身份证返回true，否则返回false
     */
    public static boolean validate15Idcard(String idcard) {
        if (idcard == null) {
            return false;
        }
        // 非15位为假
        if (idcard.length() != 15) {
            return false;
        }
        // 15位全部为数字
        if (!isDigital(idcard)) {
            return false;
        }
        // 校验省份代码
        if (!checkProvinceCode(idcard.substring(0, 2))) {
            return false;
        }
        // 校验出生日期（第7~12位，yyMMdd，15位身份证年份均视为19xx年）
        return isValidBirthday(idcard.substring(6, 12), true);
    }

    /**
     * 将15位身份证号码转换为18位身份证号码
     * <p>
     * 转换规则：在六位地址码后将两位年份扩展为四位（补"19"前缀），
     * 再在末尾追加根据前17位计算出的校验码。
     *
     * @param idcard 15位身份证号码
     * @return 转换后的18位身份证号码；如果输入不是合法的15位号码则返回null
     */
    public static String convert15To18(String idcard) {
        if (!validate15Idcard(idcard)) {
            return null;
        }
        // 年份两位扩展为四位：15位身份证签发于2000年之前，统一补19前缀
        String idcard17 = idcard.substring(0, 6) + "19" + idcard.substring(6) ;
        int sum17 = getPowerSum(converCharToInt(idcard17.toCharArray()));
        String checkCode = getCheckCodeBySum(sum17);
        if (null == checkCode) {
            return null;
        }
        return idcard17 + checkCode;
    }

    /**
     * 从身份证号码中提取出生日期
     * <p>
     * 18位身份证取第7~14位（yyyyMMdd）；15位身份证取第7~12位（yyMMdd，视为19xx年）。
     *
     * @param idcard 身份证号码（15位或18位）
     * @return 出生日期字符串，格式为yyyy-MM-dd；无法解析时返回null
     */
    public static String getBirthday(String idcard) {
        if (idcard == null) {
            return null;
        }
        if (idcard.length() == 18) {
            String raw = idcard.substring(6, 14);
            if (!isValidBirthday(raw, false)) {
                return null;
            }
            return raw.substring(0, 4) + "-" + raw.substring(4, 6) + "-" + raw.substring(6, 8);
        }
        if (idcard.length() == 15) {
            String raw = idcard.substring(6, 12);
            if (!isValidBirthday(raw, true)) {
                return null;
            }
            return "19" + raw.substring(0, 2) + "-" + raw.substring(2, 4) + "-" + raw.substring(4, 6);
        }
        return null;
    }

    /**
     * 从身份证号码中提取省份名称
     *
     * @param idcard 身份证号码（15位或18位）
     * @return 省份名称；地址码无法识别时返回null
     */
    public static String getProvince(String idcard) {
        if (idcard == null || (idcard.length() != 15 && idcard.length() != 18)) {
            return null;
        }
        return PROVINCE_CODE_MAP.get(idcard.substring(0, 2));
    }

    /**
     * 校验省份代码是否在省份代码表中
     *
     * @param provinceCode 两位省份代码
     * @return 合法返回true，否则返回false
     */
    private static boolean checkProvinceCode(String provinceCode) {
        return PROVINCE_CODE_MAP.containsKey(provinceCode);
    }

    /**
     * 校验出生日期段是否为真实存在的日期
     *
     * @param birthday    出生日期段字符串
     * @param twoDigitYear true表示年份为两位（yyMMdd），false表示年份为四位（yyyyMMdd）
     * @return 日期合法返回true，否则返回false
     */
    private static boolean isValidBirthday(String birthday, boolean twoDigitYear) {
        try {
            int year;
            int begin = 0;
            if (twoDigitYear) {
                year = 1900 + Integer.parseInt(birthday.substring(0, 2));
                begin = 2;
            } else {
                year = Integer.parseInt(birthday.substring(0, 4));
                begin = 4;
            }
            int month = Integer.parseInt(birthday.substring(begin, begin + 2));
            int day = Integer.parseInt(birthday.substring(begin + 2, begin + 4));

            // 采用严格模式，月份、日期越界（如02月31日）会抛出异常
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setLenient(false);
            calendar.set(year, month - 1, day);
            calendar.getTime();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否全部为数字
     *
     * @param str 待检测的字符串
     * @return 如果字符串全部为数字返回true，否则返回false
     */
    private static boolean isDigital(String str) {
        return str != null && !"".equals(str) && str.matches("^[0-9]*$");
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再计算总和
     *
     * @param bit 身份证各位数字组成的数组
     * @return 加权因子相乘后的总和
     */
    private static int getPowerSum(int[] bit) {
        int sum = 0;
        if (power.length != bit.length) {
            return sum;
        }

        for (int i = 0; i < bit.length; i++) {
            for (int j = 0; j < power.length; j++) {
                if (i == j) {
                    sum = sum + bit[i] * power[j];
                }
            }
        }

        return sum;
    }

    /**
     * 根据加权和与11的模值获取对应的校验码
     *
     * @param sum17 加权因子相乘后的总和与11的模
     * @return 对应的校验码（0-9或x）
     */
    private static String getCheckCodeBySum(int sum17) {
        String checkCode = null;
        switch (sum17 % 11) {
            case 10:
                checkCode = "2";
                break;
            case 9:
                checkCode = "3";
                break;
            case 8:
                checkCode = "4";
                break;
            case 7:
                checkCode = "5";
                break;
            case 6:
                checkCode = "6";
                break;
            case 5:
                checkCode = "7";
                break;
            case 4:
                checkCode = "8";
                break;
            case 3:
                checkCode = "9";
                break;
            case 2:
                checkCode = "x";
                break;
            case 1:
                checkCode = "0";
                break;
            case 0:
                checkCode = "1";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + sum17 % 11);
        }
        return checkCode;
    }

    /**
     * 将字符数组转换为整型数组
     *
     * @param c 字符数组
     * @return 对应的整型数组
     * @throws NumberFormatException 如果字符不是有效的数字字符
     */
    private static int[] converCharToInt(char[] c) throws NumberFormatException {
        int[] a = new int[c.length];
        int k = 0;
        for (char temp : c) {
            a[k++] = Integer.parseInt(String.valueOf(temp));
        }
        return a;
    }

    /**
     * 从身份证号码中获取性别标识
     * <p>
     * 根据身份证号码的顺序码末位判断性别：15位身份证取第15位，18位身份证取第17位。
     * 奇数表示男性，偶数表示女性。
     *
     * @param idno 身份证号码（15位或18位）
     * @return 性别标识，1表示男性，0表示女性
     */
    public static int getUserSex(String idno) {
        if (idno == null) {
            return 1;
        }
        String sex;
        if (idno.length() > 15) {
            // 18位：第17位（索引16）
            sex = idno.substring(16, 17);
        } else if (idno.length() == 15) {
            // 15位：第15位（索引14）
            sex = idno.substring(14, 15);
        } else {
            sex = "1";
        }

        return Integer.parseInt(sex) % 2 == 0 ? 0 : 1;
    }
}

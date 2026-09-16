package com.zifang.util.ch;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 拼音生成工具类
 * <p>
 * 提供中文转拼音的各种功能，包括：
 * <ul>
 *   <li>中文转换为全拼拼音</li>
 *   <li>中文转换为首字母拼音</li>
 *   <li>判断字符串是否包含中文</li>
 *   <li>身份证号码相关操作</li>
 * </ul>
 *
 * @author zifang
 */
public class PinyinGeneratorUtil {

    /**
     * 将中文字符串转换为全拼拼音
     * <p>
     * 将输入的中文字符串转换为对应的汉语拼音全称，非汉字字符保持不变。
     * 转换结果为小写形式，且去除所有非字母数字字符。
     *
     * @param chinese 中文字符串
     * @return 中文对应的全拼拼音，字母均为小写
     */
    public static String transformToFullPinyin(String chinese) {
        // 用StringBuffer（字符串缓冲）来接收处理的数据
        StringBuffer sb = new StringBuffer();
        //字符串转换为字截数组
        char[] arr = chinese.toCharArray();
        //创建转换对象
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        //转换类型（大写or小写）
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //定义中文声调的输出格式
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            //判断是否是汉子字符
            if (arr[i] > 128) {
                try {
                    // 提取汉字的首字母
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        sb.append(temp[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                // 如果不是汉字字符，直接拼接
                sb.append(arr[i]);
            }
        }
        return sb.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 将中文字符串转换为首字母拼音
     * <p>
     * 提取中文字符串中每个汉字的拼音首字母，非汉字字符保持不变。
     * 返回结果为小写形式。
     *
     * @param chinese 中文字符串
     * @return 每个汉字对应的拼音首字母，非汉字字符保持不变
     */
    public static String transformToHeadPinyin(String chinese) {
        // 用StringBuffer（字符串缓冲）来接收处理的数据
        StringBuffer sb = new StringBuffer();
        //字符串转换为字截数组
        char[] arr = chinese.toCharArray();
        //创建转换对象
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        //转换类型（大写or小写）
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //定义中文声调的输出格式
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            //判断是否是汉子字符
            if (arr[i] > 128) {
                try {
                    // 提取汉字的首字母
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        sb.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                // 如果不是汉字字符，直接拼接
                sb.append(arr[i]);
            }
        }
        return sb.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 将字符串中的中文转换为拼音，其他字符不变
     * <p>
     * 遍历输入字符串，将每个中文字符转换为其拼音表示，非汉字字符（字母、数字、标点等）保持原样输出。
     * 转换采用不带声调的拼音格式。
     *
     * @param inputString 输入字符串
     * @return 转换后的字符串，中文部分替换为拼音，非中文部分保持不变
     */
    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();
        String output = "";

        try {
            for (int i = 0; i < input.length; i++) {
                if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                    output += temp[0];
                } else {
                    output += Character.toString(input[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * 获取汉字串拼音首字母，英文字符不变
     *
     * @param chinese 汉字串
     * @return 汉语拼音首字母，非汉字字符保持不变
     */
    public static String getFirstSpell(String chinese) {
        StringBuilder pybf = new StringBuilder();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        pybf.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(arr[i]);
            }
        }
        return pybf.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 获取汉字串全拼拼音，英文字符不变
     *
     * @param chinese 汉字串
     * @return 汉字串对应的完整拼音，非汉字字符保持不变
     */
    public static String getFullSpell(String chinese) {
        StringBuffer pybf = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    pybf.append(PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat)[0]);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pybf.append(arr[i]);
            }
        }
        return pybf.toString();
    }


    /**
     * 使用正则表达式判断字符串是否包含中文
     * <p>
     * 该方法只能判断部分CJK字符（CJK统一汉字），对于某些生僻字可能无法准确识别。
     *
     * @param str 待检测的字符串
     * @return 如果字符串包含中文返回true，否则返回false
     */
    public static boolean isChineseByREG(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
        return pattern.matcher(str.trim()).find();
    }

    /**
     * 使用Unicode块判断字符串是否包含中文
     * <p>
     * 通过检查字符是否属于CJK统一表意文字块来判断是否为中文，
     * 只能判断部分CJK字符，对于某些生僻字可能无法准确识别。
     *
     * @param str 待检测的字符串
     * @return 如果字符串包含中文返回true，否则返回false
     */
    public static boolean isChineseByName(String str) {
        if (str == null) {
            return false;
        }
        // 大小写不同：\\p 表示包含，\\P 表示不包含
        // \\p{Cn} 的意思为 Unicode 中未被定义字符的编码，\\P{Cn} 就表示 Unicode中已经被定义字符的编码
        String reg = "\\p{InCJK Unified Ideographs}&&\\P{Cn}";
        Pattern pattern = Pattern.compile(reg);
        return pattern.matcher(str.trim()).find();
    }


    /**
     * 完整的判断中文字符和符号
     * <p>
     * 遍历字符串中的每个字符，逐一判断是否为中文（包括汉字和常见中文标点符号）。
     *
     * @param strName 待检测的字符串
     * @return 如果字符串包含中文返回true，否则返回false
     */
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断单个字符是否为中文
     * <p>
     * 判断字符是否属于CJK统一表意文字、CJK兼容表意文字、CJK统一表意文字扩展A或中文标点符号范围。
     *
     * @param c 待检测的字符
     * @return 如果是中文相关字符返回true，否则返回false
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    /**
     * 获取字符串中中文字符的个数
     *
     * @param str 待统计的字符串
     * @return 字符串中中文字符的个数
     */
    public static int ChineseLength(String str) {
        Pattern p = Pattern.compile("[\u4E00-\u9FA5]+");
        Matcher m = p.matcher(str);
        int i = 0;
        while (m.find()) {
            String temp = m.group(0);
            i += temp.length();
        }
        return i;
    }

    /**
     * 判断字符串是否为乱码
     * <p>
     * 通过分析字符串中非字母数字且非中文字符的比例来判断是否为乱码。
     * 返回值为乱码字符占总字符的比例。
     *
     * @param strName 待检测的字符串
     * @return 乱码字符的比例（0.0表示无乱码，1.0表示完全乱码）
     */
    public static float isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = 0;
        float count = 0;
        for (char c : ch)
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
                chLength++;
            }
        return count / chLength;
    }

    /**
     * 将15位身份证号转换为18位标准身份证号
     * <p>
     * 15位身份证号码格式：6位地址码 + 6位出生日期（YYMMDD）+ 3位顺序码
     * 转换时在第6位后插入"19"将出生年份补全为四位，然后计算校验位生成18位身份证号。
     *
     * @param IdCardNO 15位身份证号码
     * @return 18位标准身份证号码，如果输入不符合15位格式则返回null
     */
    public static String transIDCard15to18(String IdCardNO) {
        String cardNo = null;
        if (null != IdCardNO && IdCardNO.trim().length() == 15) {
            IdCardNO = IdCardNO.trim();
            StringBuilder sb = new StringBuilder(IdCardNO);
            sb.insert(6, "19");
            sb.append(transCardLastNo(sb.toString()));
            cardNo = sb.toString();
        }
        return cardNo;
    }

    /**
     * 计算身份证最后一位校验码
     *
     * @param newCardId 补全后的17位身份证号
     * @return 校验码字符（0-9或X）
     */
    private static String transCardLastNo(String newCardId) {
        char[] ch = newCardId.toCharArray();
        int m = 0;
        int[] co = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] verCode = new char[]{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        for (int i = 0; i < newCardId.length(); i++) {
            m += (ch[i] - '0') * co[i];
        }
        int residue = m % 11;
        return String.valueOf(verCode[residue]);
    }
}

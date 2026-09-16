package com.zifang.util.ch;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金钱处理工具类
 * <p>
 * 提供人民币金额的各种转换和格式化功能，包括：
 * <ul>
 *   <li>人民币金额转换为中文大写</li>
 *   <li>人民币金额转换为会计格式（千分位分隔）</li>
 *   <li>人民币金额按指定单位格式化（如万元、亿元）</li>
 *   <li>数字金额转换为中文大写</li>
 * </ul>
 *
 * @author zifang
 */
public class MoneyUtil {

    /**
     * 汉语中数字大写
     */
    private static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    /**
     * 汉语中货币单位大写
     */
    private static final String[] CN_UPPER_MONETRAY_UNIT = {"分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟"};
    /**
     * 特殊字符：整
     */
    private static final String CN_FULL = "";

    /**
     * 特殊字符：负
     */
    private static final String CN_NEGATIVE = "负";
    /**
     * 零元整
     */
    private static final String CN_ZEOR_FULL = "零元整";

    /**
     * 金额的精度，默认值为2
     */
    private static final int MONEY_PRECISION = 2;

    /**
     * 将人民币金额转换为中文大写形式
     * <p>
     * 接收字符串格式的金额，格式为：x万x千x百x十x元x角x分
     *
     * @param numberOfMoney 传入的金额字符串
     * @return 中文大写金额字符串
     * @throws NumberFormatException 如果输入的字符串不是有效的数字格式
     */
    public static String number2CNMontray(String numberOfMoney) {
        return number2CNMontray(new BigDecimal(numberOfMoney));
    }


    /**
     * 将人民币金额（BigDecimal类型）转换为中文大写形式
     * <p>
     * 将金额转换为中文大写表示，格式为：x万x千x百x十x元x角x分
     * 支持负数，零元整返回"零元整"
     *
     * @param numberOfMoney 传入的金额（BigDecimal类型）
     * @return 中文大写金额字符串
     */
    public static String number2CNMontray(BigDecimal numberOfMoney) {
        StringBuffer sb = new StringBuffer();
        int signum = numberOfMoney.signum();
        // 零元整的情况
        if (signum == 0) {
            return CN_ZEOR_FULL;
        }
        //这里会进行金额的四舍五入
        long number = numberOfMoney.movePointRight(MONEY_PRECISION).setScale(0, 4).abs().longValue();
        // 得到小数点后两位值
        long scale = number % 100;
        int numUnit = 0;
        int numIndex = 0;
        boolean getZero = false;
        // 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
        if (!(scale > 0)) {
            numIndex = 2;
            number = number / 100;
            getZero = true;
        }
        if ((scale > 0) && (!(scale % 10 > 0))) {
            numIndex = 1;
            number = number / 10;
            getZero = true;
        }
        int zeroSize = 0;
        while (true) {
            if (number <= 0) {
                break;
            }
            // 每次获取到最后一个数
            numUnit = (int) (number % 10);
            if (numUnit > 0) {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    sb.insert(0, CN_UPPER_MONETRAY_UNIT[6]);
                }
                if ((numIndex == 13) && (zeroSize >= 3)) {
                    sb.insert(0, CN_UPPER_MONETRAY_UNIT[10]);
                }
                sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (!(getZero)) {
                    sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                }
                if (numIndex == 2) {
                    if (number > 0) {
                        sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                    }
                } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    sb.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                }
                getZero = true;
            }
            // 让number每次都去掉最后一个数
            number = number / 10;
            ++numIndex;
        }
        // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
        if (signum == -1) {
            sb.insert(0, CN_NEGATIVE);
        }
        // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
        if (!(scale > 0)) {
            sb.append(CN_FULL);
        }
        return sb.toString();
    }

    /**
     * 将人民币转换为会计格式金额（千分位分隔），保留两位小数
     * <p>
     * 格式为：xxxx,xxxx,xxxx.xx
     *
     * @param money 待转换的金额
     * @return 会计格式的金额字符串
     */
    public static String accountantMoney(BigDecimal money) {
        return accountantMoney(money, 2, 1);
    }

    /**
     * 格式化金额，按指定单位显示
     * <p>
     * 将金额除以指定的格式化值，并保留指定的小数位数。
     * 例如：传入10000元，divisor=10000，scale=2，返回"1.00万元"
     *
     * @param money   待处理的金额
     * @param scale   小数点后保留的位数
     * @param divisor 格式化值（10:十元、100:百元、1000:千元、10000:万元、100000000:亿元）
     * @return 格式化后的金额字符串，带单位后缀
     */
    public static String getFormatMoney(BigDecimal money, int scale, double divisor) {
        return formatMoney(money, scale, divisor) + getCellFormat(divisor);
    }

    /**
     * 获取会计格式的人民币，按指定单位显示
     * <p>
     * 将金额先转换为会计格式（千分位分隔），再按指定单位格式化。
     * 例如：传入123456789元，divisor=100000000，scale=2，返回"1,234.57亿元"
     *
     * @param money   待处理的金额
     * @param scale   小数点后保留的位数
     * @param divisor 格式化值（10:十元、100:百元、1000:千元、10000:万元、100000000:亿元）
     * @return 会计格式的金额字符串，带单位后缀
     */
    public static String getAccountantMoney(BigDecimal money, int scale, double divisor) {
        return accountantMoney(money, scale, divisor) + getCellFormat(divisor);
    }

    /**
     * 将人民币转换为会计格式金额（千分位分隔）
     *
     * @param money   待处理的金额
     * @param scale   小数点后保留的位数
     * @param divisor 格式化值
     * @return 会计格式的金额字符串
     */
    private static String accountantMoney(BigDecimal money, int scale, double divisor) {
        String disposeMoneyStr = formatMoney(money, scale, divisor);
        //小数点处理  
        int dotPosition = disposeMoneyStr.indexOf(".");
        String exceptDotMoeny = null;//小数点之前的字符串
        String dotMeony = null;//小数点之后的字符串
        if (dotPosition > 0) {
            exceptDotMoeny = disposeMoneyStr.substring(0, dotPosition);
            dotMeony = disposeMoneyStr.substring(dotPosition);
        } else {
            exceptDotMoeny = disposeMoneyStr;
        }
        //负数处理  
        int negativePosition = exceptDotMoeny.indexOf("-");
        if (negativePosition == 0) {
            exceptDotMoeny = exceptDotMoeny.substring(1);
        }
        StringBuffer reverseExceptDotMoney = new StringBuffer(exceptDotMoeny);
        reverseExceptDotMoney.reverse();//字符串倒转  
        char[] moneyChar = reverseExceptDotMoney.toString().toCharArray();
        StringBuffer returnMeony = new StringBuffer();//返回值
        for (int i = 0; i < moneyChar.length; i++) {
            if (i != 0 && i % 3 == 0) {
                returnMeony.append(",");//每隔3位加','  
            }
            returnMeony.append(moneyChar[i]);
        }
        returnMeony.reverse();//字符串倒转  
        if (dotPosition > 0) {
            returnMeony.append(dotMeony);
        }
        if (negativePosition == 0) {
            return "-" + returnMeony.toString();
        } else {
            return returnMeony.toString();
        }
    }

    /**
     * 格式化金额数值
     *
     * @param money   待处理的金额
     * @param scale   小数点后保留的位数
     * @param divisor 格式化值
     * @return 格式化后的金额数值字符串
     */
    private static String formatMoney(BigDecimal money, int scale, double divisor) {
        if (divisor == 0) {
            return "0.00";
        }
        if (scale < 0) {
            return "0.00";
        }
        BigDecimal divisorBD = new BigDecimal(divisor);
        return money.divide(divisorBD, scale, RoundingMode.HALF_UP).toString();
    }

    /**
     * 根据格式化值获取对应的单位字符串
     *
     * @param divisor 格式化值
     * @return 对应的单位字符串（如"元"、"万元"、"亿元"等）
     */
    private static String getCellFormat(double divisor) {
        String str = String.valueOf(divisor);
        int len = str.substring(0, str.indexOf(".")).length();
        String cell = "";
        switch (len) {
            case 1:
                cell = "元";
                break;
            case 2:
                cell = "十元";
                break;
            case 3:
                cell = "百元";
                break;
            case 4:
                cell = "千元";
                break;
            case 5:
                cell = "万元";
                break;
            case 6:
                cell = "十万元";
                break;
            case 7:
                cell = "百万元";
                break;
            case 8:
                cell = "千万元";
                break;
            case 9:
                cell = "亿元";
                break;
            case 10:
                cell = "十亿元";
                break;
        }
        return cell;
    }

    /**
     * 数字金额大写转换
     * <p>
     * 将数字金额转换为中文大写形式，支持角分显示。
     * 例如：1234.56 转换为 壹仟贰佰叁拾肆元伍角陆分
     *
     * @param n 数字金额
     * @return 中文大写金额字符串
     */
    public static String digitUppercase(double n) {
        String[] fraction = {"角", "分"};
        String[] digit = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[][] unit = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};

        String head = n < 0 ? "负" : "";
        n = Math.abs(n);

        String s = "";
        for (int i = 0; i < fraction.length; i++) {
            s += (digit[(int) (Math.floor(n * 10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
        }
        if (s.length() < 1) {
            s = "整";
        }
        int integerPart = (int) Math.floor(n);

        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            String p = "";
            for (int j = 0; j < unit[1].length && n > 0; j++) {
                p = digit[integerPart % 10] + unit[1][j] + p;
                integerPart = integerPart / 10;
            }
            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }
        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$",
                "零元整");
    }

    /**
     * yuanToCents方法。
     * 元转分：金额乘以 100 后取整数部分。
     *
     * @param yuan 元为单位的金额，null 视为 0
     * @return Long类型返回值，分为单位的金额
     */
    public static Long yuanToCents(BigDecimal yuan) {
        if (yuan == null) {
            return 0L;
        }
        return yuan.multiply(new BigDecimal("100")).longValue();
    }

    /**
     * centsToYuan方法。
     * 分转元：金额除以 100，保留 2 位小数（四舍五入）。
     *
     * @param cents 分为单位的金额，null 视为 0
     * @return BigDecimal类型返回值，元为单位的金额
     */
    public static BigDecimal centsToYuan(Long cents) {
        if (cents == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(cents).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}

package com.zifang.util.core.lang;

/**
 * 数据脱敏工具类。
 * <p>
 * 对手机号、证件号等敏感文本按保留位数做掩码处理，
 * 中间部分以等长的星号替换，便于日志输出或前端展示。
 *
 * @author zifang
 */
public class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 通用掩码处理：保留前 front 位与后 back 位，中间部分替换为等长星号。
     * <p>
     * 文本为 null 或长度不足（不超过保留位数之和）时不做处理，原样返回。
     *
     * @param text  待掩码文本
     * @param front 头部保留位数
     * @param back  尾部保留位数
     * @return 掩码后的文本
     */
    public static String mask(String text, int front, int back) {
        if (text == null) {
            return null;
        }
        int length = text.length();
        if (length <= front + back || front < 0 || back < 0) {
            return text;
        }
        StringBuilder sb = new StringBuilder(length);
        sb.append(text, 0, front);
        for (int i = 0; i < length - front - back; i++) {
            sb.append('*');
        }
        sb.append(text, length - back, length);
        return sb.toString();
    }

    /**
     * 手机号脱敏：11 位手机号保留前 3 位与后 4 位，中间 4 位掩码，
     * 例如 15638296218 得到 156****6218；非 11 位文本原样返回。
     *
     * @param phone 手机号文本
     * @return 脱敏后的文本
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return mask(phone, 3, 4);
    }

    /**
     * 证件号脱敏：15 位文本保留前 6 位与后 3 位，
     * 18 位文本保留前 6 位与后 4 位，其余位掩码；
     * 其他长度的文本原样返回。
     *
     * @param idCard 证件号文本
     * @return 脱敏后的文本
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null) {
            return null;
        }
        if (idCard.length() == 15) {
            return mask(idCard, 6, 3);
        }
        if (idCard.length() == 18) {
            return mask(idCard, 6, 4);
        }
        return idCard;
    }
}

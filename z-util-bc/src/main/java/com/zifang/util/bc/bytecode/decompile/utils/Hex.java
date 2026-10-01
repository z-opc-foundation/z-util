package com.zifang.util.bc.bytecode.decompile.utils;

/**
 * <pre>
 * 16进制值与String/Byte之间的转换
 * @author zifang
 * @email lijian@dzs.mobi
 * @data 2011-10-16
 * http://blog.csdn.net/hzbigdog/article/details/6877712
 *
 * http://www.blogjava.net/lijinglin/archive/2011/11/02/362567.html
 * </pre>
 */
public class Hex {
    /**
     * 字符串转换成十六进制字符串
     * <p>
     * str 待转换的ASCII字符串
     *
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     * <p>
     * str Byte字符串(Byte之间无分隔符 如:[616C6B])
     *
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
//			sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     * <p>
     * src Byte字符串，每个Byte之间没有分隔符
     *
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            // Byte.decode("0xFF") 会因 255 超 byte 上限抛异常，这里按无符号解析再收窄
            ret[i] = (byte) Integer.parseInt(src.substring(i * 2, i * 2 + 2), 16);
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     * <p>
     * strText 全角字符串
     *
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText) throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            str.append(String.format("\\u%04X", intAsc));
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     * <p>
     * hex 形如 "\u4E2D\u6587" 或连续 4 位十六进制组 "4E2D6587"
     *
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        String normalized = hex.replace("\\u", "").replace("\\U", "");
        StringBuilder str = new StringBuilder();
        for (int i = 0; i + 4 <= normalized.length(); i += 4) {
            str.append((char) Integer.parseInt(normalized.substring(i, i + 4), 16));
        }
        return str.toString();
    }

    /**
     * 将十六进制转化为十进制整数
     *
     * @param cp_count_hexstr
     * @return
     */
    public static int hex2Integer(String cp_count_hexstr) {
        return Integer.parseInt(cp_count_hexstr, 16);
    }


    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        String hex = "ef2c71b29202f3e642f2abd8d518f367ec3fbf6a6a61beb678ae0c871ee368ac";
        System.out.println(Hex.hexStr2Str(hex));
    }
}
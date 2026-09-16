package com.zifang.util.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * HTML和Web相关的工具类
 * <p>
 * 提供HTML标签转义、XSS过滤等功能
 */
public class HtmlUtil {

    private static final String EMPTY = "";

    /**
     * 对字符串进行编码
     *
     * @param str      需要处理的字符串
     * @param encoding 编码方式
     * @return 编码后的字符串
     */
    public static String escape(String str, String encoding) throws UnsupportedEncodingException {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }
        byte[] bytes = str.getBytes(encoding);
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) (bytes[i] & 0xFF);
        }
        return new String(chars);
    }

    /**
     * 对字符串进行解码
     *
     * @param str      需要处理的字符串
     * @param encoding 解码方式
     * @return 解码后的字符串
     */
    public static String unescape(String str, String encoding) {
        if (str == null || str.isEmpty()) {
            return EMPTY;
        }
        try {
            return URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            return str;
        }
    }

    /**
     * HTML标签转义方法
     * <p>
     * 空格	 &nbsp;
     * <	小于号	&lt;
     * >	大于号	&gt;
     * &	和号	 &amp;
     * "	引号	&quot;
     * '	撇号 	&apos;
     * ￠	分	 &cent;
     * £	镑	 &pound;
     * ¥	日圆	&yen;
     * €	欧元	&euro;
     * §	小节	&sect;
     * ©	版权	&copy;
     * ®	注册商标	&reg;
     * ™	商标	&trade;
     * ×	乘号	&times;
     * ÷	除号	&divide;
     *
     * @param content 需要转义的内容
     * @return 转义后的字符串
     */
    public static String unhtml(String content) {
        if (content == null || content.isEmpty()) {
            return EMPTY;
        }
        String html = content;
        html = html.replaceAll("'", "&apos;");
        html = html.replaceAll("\"", "&quot;");
        html = html.replaceAll("\t", "&nbsp;&nbsp;");
        html = html.replaceAll("<", "&lt;");
        html = html.replaceAll(">", "&gt;");
        return html;
    }

    /**
     * HTML转实体反转
     *
     * @param content 转义后的内容
     * @return 原始字符串
     */
    public static String html(String content) {
        if (content == null || content.isEmpty()) {
            return EMPTY;
        }
        String html = content;
        html = html.replaceAll("&apos;", "'");
        html = html.replaceAll("&quot;", "\"");
        html = html.replaceAll("&nbsp;", " ");
        html = html.replaceAll("&lt;", "<");
        html = html.replaceAll("&gt;", ">");
        return html;
    }

    /**
     * 去除带script、src的语句，转义替换后的value值
     *
     * @param value 需要过滤的值
     * @return 过滤后的字符串
     */
    public static String replaceXSS(String value) {
        if (value == null) {
            return null;
        }
        try {
            value = value.replace("+", "%2B");
            value = URLDecoder.decode(value, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        } catch (IllegalArgumentException e) {
            // ignore
        }

        // Avoid null characters
        value = value.replaceAll("\0", "");

        // Avoid anything between script tags
        Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
        value = scriptPattern.matcher(value).replaceAll("");

        // Avoid anything in a src='...' type of expression
        scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = scriptPattern.matcher(value).replaceAll("");

        scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = scriptPattern.matcher(value).replaceAll("");

        // Remove any lonesome </script> tag
        scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
        value = scriptPattern.matcher(value).replaceAll("");

        // Remove any lonesome <script ...> tag
        scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = scriptPattern.matcher(value).replaceAll("");

        // Avoid eval(...) expressions
        scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = scriptPattern.matcher(value).replaceAll("");

        // Avoid expression(...) expressions (CSS expression XSS vector)
        scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = scriptPattern.matcher(value).replaceAll("");

        // Avoid javascript:... expressions
        scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
        value = scriptPattern.matcher(value).replaceAll("");

        // Avoid alert:... expressions
        scriptPattern = Pattern.compile("alert", Pattern.CASE_INSENSITIVE);
        value = scriptPattern.matcher(value).replaceAll("");

        // Avoid onload= expressions
        scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        value = scriptPattern.matcher(value).replaceAll("");

        scriptPattern = Pattern.compile("vbscript[\r\n| | ]*:[\r\n| | ]*", Pattern.CASE_INSENSITIVE);
        value = scriptPattern.matcher(value).replaceAll("");

        return filter(value);
    }

    /**
     * 过滤特殊字符
     *
     * @param value 需要过滤的值
     * @return 过滤后的字符串
     */
    public static String filter(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); ++i) {
            switch (value.charAt(i)) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                case '\'':
                    result.append("&apos;");
                    break;
                case '%':
                    result.append("%");
                    break;
                case ';':
                    result.append(";");
                    break;
                case '(':
                    result.append("(");
                    break;
                case ')':
                    result.append(")");
                    break;
                case '&':
                    result.append("&");
                    break;
                case '+':
                    result.append("+");
                    break;
                default:
                    result.append(value.charAt(i));
                    break;
            }
        }
        return result.toString();
    }
}
package com.zifang.util.core.lang;

import com.zifang.util.core.lang.regex.Patterns;
import com.zifang.util.core.lang.validator.Validator;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.zifang.util.core.Const.Symbol.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.util.Strings.EMPTY;

/**
 * 字符串处理工具类。
 * <p>
 * 提供字符串判空、截取、编码转换、格式化、匹配等常用操作。
 *
 * @author zifang
 */
public class StringUtil {
    private static final int INDEX_NOT_FOUND = -1;
    private static final int PAD_LIMIT = 8192;
    private static final Predicate<String> NULL_STRING_PREDICATE = Objects::isNull;
    private static final Supplier<String> NULL_STRING_MSG_SUPPLIER = () -> "'value' should be not null.";
    private static final String[] EMPTY_ARRAY = new String[0];
    private static final char SEPARATOR = '_';
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Checks if a String is not empty (not null and not blank).
     *
     * @param string the String to check
     * @return true if the String is not empty, false otherwise
     */
    public static boolean isNotEmptyString(String string) {
        return string != null && !string.isEmpty();
    }

    /**
     * Checks if a String is not empty.
     * Equivalent to !isEmpty(s).
     *
     * @param s the String to check
     * @return true if the String is not empty, false otherwise
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * Checks if a String is not blank.
     * A String is considered blank if it is null, empty, or contains only whitespace.
     *
     * @param s the String to check
     * @return true if the String is not blank, false otherwise
     */
    public static boolean isNotBlank(String s) {
        return !isBlank(s);
    }

    /**
     * Checks if a String is blank.
     * A String is considered blank if it is null, empty, or contains only whitespace.
     *
     * @param s the String to check
     * @return true if the String is blank, false otherwise
     */
    public static boolean isBlank(String s) {
        return null == s || s.trim().isEmpty();
    }

    /**
     * Checks if a String is empty.
     * A String is considered empty if it is null or has a length of zero.
     *
     * @param s the String to check
     * @return true if the String is empty, false otherwise
     */
    public static boolean isEmpty(String s) {
        return null == s || s.isEmpty();
    }

    /**
     * Checks if an Object is empty when converted to String.
     * A Object is considered empty if it is null or its toString() result is empty.
     *
     * @param obj the Object to check
     * @return true if the Object is empty, false otherwise
     */
    public static boolean isEmpty(Object obj) {
        return null == obj || isEmpty(obj.toString());
    }

    /**
     * Checks if an Object is not empty when converted to String.
     *
     * @param obj the Object to check
     * @return true if the Object is not empty, false otherwise
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * Checks if all Strings in the array are not empty.
     *
     * @param strArr the String array to check
     * @return true if all Strings are not empty, false otherwise
     */
    public static boolean isNotEmpty(String... strArr) {
        for (String str : strArr) {
            if (isEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all characters in the String are lowercase.
     *
     * @param value the String to check
     * @return true if all characters are lowercase, false otherwise
     */
    public static boolean isAllLowerCase(String value) {
        return forEachCharAllMatch(value, Character::isLowerCase);
    }

    /**
     * Checks if all characters in the String are uppercase.
     *
     * @param value the String to check
     * @return true if all characters are uppercase, false otherwise
     */
    public static boolean isAllUpperCase(String value) {
        return forEachCharAllMatch(value, Character::isUpperCase);
    }

    /**
     * Tests if any character in the String matches the given predicate.
     *
     * @param value     the String to check
     * @param predicate the predicate to test each character
     * @return true if any character matches, false otherwise (including null or empty string)
     */
    public static boolean forEachCharAnyMatch(String value, Predicate<Character> predicate) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (predicate.test(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if all characters in the String match the given predicate.
     *
     * @param value     the String to check
     * @param predicate the predicate to test each character
     * @return true if all characters match, false otherwise (including null or empty string)
     */
    public static boolean forEachCharAllMatch(String value, Predicate<Character> predicate) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!predicate.test(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 在字符串末尾追加一个或多个字符串
     *
     * @param value   原始字符串
     * @param appends 要追加的字符串数组
     * @return 追加后的字符串，如果value为null则抛出异常
     */
    public static String append(String value, String... appends) {

        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);

        if (appends == null || appends.length == 0) {
            return value;
        } else {
            StringJoiner joiner = new StringJoiner("");
            Arrays.stream(appends).forEach(joiner::add);
            return value + joiner;
        }
    }

    /**
     * 文本内存在多个连续空格场合，缩进为单个空格
     * Collapses multiple consecutive whitespace characters into a single space.
     *
     * @param value The input string to collapse whitespace
     * @return The string with consecutive whitespace collapsed to single space
     */
    public static String collapseWhitespace(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.trim().replaceAll("\\s\\s+", " ");
    }

    /**
     * Checks if the value contains the needle (case-insensitive).
     *
     * @param value  the String to search in
     * @param needle the String to search for
     * @return true if value contains needle, false otherwise
     */
    public static boolean contains(String value, String needle) {
        return contains(value, needle, false);
    }

    /**
     * Checks if the value contains the needle with case sensitivity control.
     *
     * @param value         the String to search in
     * @param needle        the String to search for
     * @param caseSensitive whether the comparison should be case-sensitive
     * @return true if value contains needle, false otherwise
     */
    public static boolean contains(String value, String needle, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (caseSensitive) {
            return value.contains(needle);
        }
        return value.toLowerCase().contains(needle.toLowerCase());
    }

    /**
     * Checks if the value contains all the needles (case-insensitive).
     *
     * @param value   the String to search in
     * @param needles the array of Strings to search for
     * @return true if value contains all needles, false otherwise
     */
    public static boolean containsAll(String value, String[] needles) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return Arrays.stream(needles).allMatch(needle -> contains(value, needle, false));
    }

    /**
     * Checks if the value contains all the needles with case sensitivity control.
     *
     * @param value         the String to search in
     * @param needles       the array of Strings to search for
     * @param caseSensitive whether the comparison should be case-sensitive
     * @return true if value contains all needles, false otherwise
     */
    public static boolean containsAll(String value, String[] needles, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return Arrays.stream(needles).allMatch(needle -> contains(value, needle, caseSensitive));
    }

    /**
     * Checks if the value contains any of the needles (case-insensitive).
     *
     * @param value   the String to search in
     * @param needles the array of Strings to search for
     * @return true if value contains any needle, false otherwise
     */
    public static boolean containsAny(String value, String[] needles) {
        return containsAny(value, needles, false);
    }

    /**
     * Checks if the value contains any of the needles with case sensitivity control.
     *
     * @param value         the String to search in
     * @param needles       the array of Strings to search for
     * @param caseSensitive whether the comparison should be case-sensitive
     * @return true if value contains any needle, false otherwise
     */
    public static boolean containsAny(String value, String[] needles, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return Arrays.stream(needles).anyMatch(needle -> contains(value, needle, caseSensitive));
    }

    /**
     * 统计子串出现次数
     */
    public static long countSubstr(String value, String subStr) {
        return countSubstr(value, subStr, true, false);
    }

    /**
     * countSubstr方法。
     * * @param value String类型参数
     *
     * @param subStr           String类型参数
     * @param caseSensitive    boolean类型参数
     * @param allowOverlapping boolean类型参数
     * @return static long类型返回值
     */
    public static long countSubstr(String value, String subStr, boolean caseSensitive, boolean allowOverlapping) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return countSubstr(caseSensitive ? value : value.toLowerCase(), caseSensitive ? subStr : subStr.toLowerCase(), allowOverlapping, 0L);
    }

    /**
     * Checks if the string ends with the specified search string (case-sensitive).
     *
     * @param value  the string to check
     * @param search the search string
     * @return true if the string ends with the search string, false otherwise
     */
    public static boolean endsWith(String value, String search) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return endsWith(value, search, value.length(), true);
    }

    /**
     * Checks if the string ends with the specified search string.
     *
     * @param value         the string to check
     * @param search        the search string
     * @param caseSensitive whether the comparison should be case-sensitive
     * @return true if the string ends with the search string, false otherwise
     */
    public static boolean endsWith(String value, String search, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return endsWith(value, search, value.length(), caseSensitive);
    }

    /**
     * Checks if the string ends with the specified search string at the given position.
     *
     * @param value         the string to check
     * @param search        the search string
     * @param position      the position to check up to
     * @param caseSensitive whether the comparison should be case-sensitive
     * @return true if the string ends with the search string at the given position, false otherwise
     */
    public static boolean endsWith(String value, String search, int position, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        int remainingLength = position - search.length();
        if (caseSensitive) {
            return value.indexOf(search, remainingLength) > -1;
        }
        return value.toLowerCase().indexOf(search.toLowerCase(), remainingLength) > -1;
    }

    /**
     * Ensures that the string starts with the given prefix.
     * If the string already starts with the prefix, returns the original string unchanged.
     *
     * @param value  The input String
     * @param prefix The prefix to ensure
     * @return The string with the prefix ensured (case-sensitive)
     */
    public static String ensureLeft(String value, String prefix) {
        return ensureLeft(value, prefix, true);
    }

    /**
     * Ensures that the string starts with the given prefix.
     * If the string already starts with the prefix, returns the original string unchanged.
     *
     * @param value         The input String
     * @param prefix        The prefix to ensure
     * @param caseSensitive Whether the comparison should be case-sensitive
     * @return The string with the prefix ensured
     */
    public static String ensureLeft(String value, String prefix, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (caseSensitive) {
            return value.startsWith(prefix) ? value : prefix + value;
        }
        String _value = value.toLowerCase();
        String _prefix = prefix.toLowerCase();
        return _value.startsWith(_prefix) ? value : prefix + value;
    }

    /**
     * Ensures that the string ends with the given suffix.
     * If the string already ends with the suffix, returns the original string unchanged.
     *
     * @param value  The input String
     * @param suffix The suffix to ensure
     * @return The string with the suffix ensured (case-sensitive)
     */
    public static String ensureRight(String value, String suffix) {
        return ensureRight(value, suffix, true);
    }

    /**
     * Ensures that the string ends with the given suffix.
     * If the string already ends with the suffix, returns the original string unchanged.
     *
     * @param value         The input String
     * @param suffix        The suffix to ensure
     * @param caseSensitive Whether the comparison should be case-sensitive
     * @return The string with the suffix ensured
     */
    public static String ensureRight(String value, String suffix, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return endsWith(value, suffix, caseSensitive) ? value : append(value, suffix);
    }

    /**
     * 使用 Base64 方式解码字符串
     *
     * @param value 要解码的 Base64 编码字符串
     * @return 解码后的 UTF-8 字符串
     * @throws IllegalArgumentException 如果value为null
     */
    public static String base64Decode(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    /**
     * 使用 Base64 方式编码字符串
     *
     * @param value 要编码的字符串
     * @return Base64 编码后的字符串
     * @throws IllegalArgumentException 如果value为null
     */
    public static String base64Encode(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将二进制编码的字符串解码为普通字符串
     * 使用16位数字表示2进制进行解码
     *
     * @param value 二进制编码的字符串
     * @return 解码后的字符串
     */
    public static String binDecode(String value) {
        return decode(value, 16, 2);
    }

    /**
     * 将字符串编码为二进制表示形式
     * 使用16位数字表示2进制
     *
     * @param value 要编码的字符串
     * @return 二进制编码后的字符串
     */
    public static String binEncode(String value) {
        return encode(value, 16, 2);
    }

    /**
     * 将十六进制编码的字符串解码为普通字符串
     * 使用4位数字表示16进制进行解码
     *
     * @param value 十六进制编码的字符串
     * @return 解码后的字符串
     */
    public static String hexDecode(String value) {
        return decode(value, 4, 16);
    }

    /**
     * 将字符串编码为十六进制表示形式
     * 使用4位数字表示16进制
     *
     * @param value 要编码的字符串
     * @return 十六进制编码后的字符串
     */
    public static String hexEncode(String value) {
        return encode(value, 4, 16);
    }

    /**
     * 将十进制编码的字符串解码为普通字符串
     * 使用5位数字表示10进制进行解码
     *
     * @param value 十进制编码的字符串
     * @return 解码后的字符串
     */
    public static String decDecode(String value) {
        return decode(value, 5, 10);
    }

    /**
     * 将字符串编码为十进制表示形式
     * 使用5位数字表示10进制，char范围为[0,65536]，5位可完全展示
     *
     * @param value 要编码的字符串
     * @return 十进制编码后的字符串
     */
    public static String decEncode(String value) {
        // char=[0,65536] , 5位可完全展示
        return encode(value, 5, 10);
    }

    /**
     * 将编码字符串解码为普通字符串
     *
     * @param value  编码字符串
     * @param digits 每个字符编码的位数
     * @param radix  进制的基数（如2二进制, 10十进制, 16十六进制）
     * @return 解码后的字符串
     * @throws IllegalArgumentException 如果value为null
     */
    public static String decode(String value, int digits, int radix) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return Arrays.stream(value.split("(?<=\\G.{" + digits + "})"))
                .map(data -> String.valueOf(Character.toChars(Integer.parseInt(data, radix))))
                .collect(joining());
    }

    /**
     * 将字符串编码为指定进制表示形式
     *
     * @param value  要编码的字符串
     * @param digits 每个字符编码的位数
     * @param radix  进制的基数（如2二进制, 10十进制, 16十六进制）
     * @return 编码后的字符串
     * @throws IllegalArgumentException 如果value为null
     */
    public static String encode(String value, int digits, int radix) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.chars().mapToObj(ch -> leftPad(Integer.toString(ch, radix), digits, "0")).collect(joining());

    }

    /**
     * Returns the first n chars of String
     *
     * @param value The input String
     * @param n     Number of chars to return
     * @return The first n chars
     */
    public static Optional<String> first(String value, int n) {
        return Optional.ofNullable(value).filter(v -> !v.isEmpty()).map(v -> v.substring(0, n));
    }

    /**
     * Return the first char of String
     *
     * @param value The input String
     * @return The first char
     */
    public static Optional<String> head(String value) {
        return first(value, 1);
    }

    /**
     * Formats a string using parameters
     *
     * @param value  The value to be formatted
     * @param params Parameters to be described in the string
     * @return The formatted string
     */
    public static String format(String value, String... params) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        Pattern p = Pattern.compile("\\{(\\w+)}");
        Matcher m = p.matcher(value);
        String result = value;
        while (m.find()) {
            int paramNumber = Integer.parseInt(m.group(1));
            if (params == null || paramNumber >= params.length) {
                throw new IllegalArgumentException("params does not have value for " + m.group());
            }
            result = result.replace(m.group(), params[paramNumber]);
        }
        return result;
    }

    /**
     * 按顺序将参数填入"{}"占位符
     * <p>
     * 与日志框架的占位符风格一致：占位符按出现顺序依次取参数，多余的参数忽略，
     * 参数不足时剩余占位符保持原样。占位符不支持转义。
     *
     * @param message 带占位符的模板字符串，为null时返回null
     * @param args    占位符替换值
     * @return 格式化后的字符串
     */
    public static String formatPlaceholder(String message, Object... args) {
        if (message == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message.length() + 16);
        int argIndex = 0;
        int i = 0;
        int len = message.length();
        while (i < len) {
            char c = message.charAt(i);
            if (c == '{' && i + 1 < len && message.charAt(i + 1) == '}' && argIndex < args.length) {
                sb.append(args[argIndex++]);
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * 将模板字符串中"${key}"形式的命名占位符替换为参数映射中的对应值
     * <p>
     * 占位符以 Map 的 key 命名，例如模板 "user=${name}" 在参数 {name=alice} 作用下
     * 得到 "user=alice"。映射中不存在对应 key 或取值为 null 时，该占位符保持原样；
     * 不匹配占位符规则的内容原样保留。
     *
     * @param template 带命名占位符的模板字符串，为null时返回null
     * @param params   占位符名到替换值的映射
     * @return 替换后的字符串
     */
    public static String replacePlaceholder(String template, Map<String, Object> params) {
        if (template == null || template.isEmpty() || params == null || params.isEmpty()) {
            return template;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer(template.length() + 16);
        while (matcher.find()) {
            Object value = params.get(matcher.group(1));
            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(value)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 将模板字符串中自定义前后缀包裹的命名占位符替换为参数映射中的对应值
     * <p>
     * 前后缀由调用方指定，例如 prefix="$"、suffix="$" 时模板 "a=$name$" 在参数 {name=bob}
     * 作用下得到 "a=bob"。映射中不存在对应 key 或取值为 null 时，该占位符保持原样；
     * 后缀缺失（找不到成对前后缀）时剩余内容原样保留。
     *
     * @param template 带命名占位符的模板字符串，为null时返回null
     * @param params   占位符名到替换值的映射
     * @param prefix   占位符前缀，为null或空串时返回模板原样
     * @param suffix   占位符后缀，为null或空串时返回模板原样
     * @return 替换后的字符串
     */
    public static String replacePlaceholder(String template, Map<String, ?> params, String prefix, String suffix) {
        if (template == null || template.isEmpty() || params == null || params.isEmpty()
                || prefix == null || prefix.isEmpty() || suffix == null || suffix.isEmpty()) {
            return template;
        }
        StringBuilder sb = new StringBuilder(template);
        int startIndex = sb.indexOf(prefix);
        while (startIndex != -1) {
            int endIndex = sb.indexOf(suffix, startIndex + prefix.length());
            if (endIndex == -1) {
                break;
            }
            String key = sb.substring(startIndex + prefix.length(), endIndex);
            Object value = params.get(key);
            if (value != null) {
                String replacement = String.valueOf(value);
                sb.replace(startIndex, endIndex + suffix.length(), replacement);
                startIndex = sb.indexOf(prefix, startIndex + replacement.length());
            } else {
                startIndex = sb.indexOf(prefix, endIndex + suffix.length());
            }
        }
        return sb.toString();
    }


    /**
     * The indexOf() method returns the index within the calling String of the first occurrence of the specified value, starting the search at fromIndex.
     * Returns -1 if the value is not found.
     *
     * @param value         The input String
     * @param needle        The search String
     * @param offset        The offset to start searching from.
     * @param caseSensitive boolean to indicate whether search should be case sensitive
     * @return Returns position of first occurrence of needle.
     */
    public static int indexOf(String value, String needle, int offset, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (caseSensitive) {
            return value.indexOf(needle, offset);
        }
        return value.toLowerCase().indexOf(needle.toLowerCase(), offset);
    }

    /**
     * Tests if two Strings are inequal
     *
     * @param first  The first String
     * @param second The second String
     * @return true if first and second are not equal false otherwise
     */
    public static boolean unequal(String first, String second) {
        return !Objects.equals(first, second);
    }

    /**
     * Tests if two Strings are inequal
     *
     * @param first  The first String
     * @param second The second String
     * @return true if first and second are not equal false otherwise
     * @deprecated use unequal instead
     */
    public static boolean inequal(String first, String second) {
        return !Objects.equals(first, second);
    }

    /**
     * Inserts 'substr' into the 'value' at the 'index' provided.
     *
     * @param value  The input String
     * @param substr The String to insert
     * @param index  The index to insert substr
     * @return String with substr added
     */
    public static String insert(String value, String substr, int index) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        Validator.validate(substr, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (index > value.length()) {
            return value;
        }
        return append(value.substring(0, index), substr, value.substring(index));
    }

    /**
     * Verifies if String is uppercase
     *
     * @param value The input String
     * @return true if String is uppercase false otherwise
     */
    public static boolean isUpperCase(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLowerCase(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies if String is lower case
     *
     * @param value The input String
     * @return true if String is lowercase false otherwise
     */
    public static boolean isLowerCase(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        for (int i = 0; i < value.length(); i++) {
            if (Character.isUpperCase(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the last n chars of String
     *
     * @param value The input String
     * @param n     Number of chars to return
     * @return n Last characters
     */
    public static String last(String value, int n) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (n > value.length()) {
            return value;
        }
        return value.substring(value.length() - n);
    }


    /**
     * Checks whether Object is String
     *
     * @param value The input String
     * @return true if Object is a String false otherwise
     */
    public static boolean isString(Object value) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("value can't be null");
        }
        return value instanceof String;
    }

    /**
     * This method returns the index within the calling String object of the last occurrence of the specified value, searching backwards from the offset.
     * Returns -1 if the value is not found. The search starts from the end and case sensitive.
     *
     * @param value  The input String
     * @param needle The search String
     * @return Return position of the last occurrence of 'needle'.
     */
    public static int lastIndexOf(String value, String needle) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return lastIndexOf(value, needle, value.length(), true);
    }

    /**
     * This method returns the index within the calling String object of the last occurrence of the specified value, searching backwards from the offset.
     * Returns -1 if the value is not found. The search starts from the end and case sensitive.
     *
     * @param value         The input String
     * @param needle        The search String
     * @param caseSensitive true or false
     * @return Return position of the last occurrence of 'needle'.
     */
    public static int lastIndexOf(String value, String needle, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return lastIndexOf(value, needle, value.length(), caseSensitive);
    }

    /**
     * This method returns the index within the calling String object of the last occurrence of the specified value, searching backwards from the offset.
     * Returns -1 if the value is not found.
     *
     * @param value         The input String
     * @param needle        The search String
     * @param offset        The index to start search from
     * @param caseSensitive whether search should be case sensitive
     * @return Return position of the last occurrence of 'needle'.
     */
    public static int lastIndexOf(String value, String needle, int offset,
                                  boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        Validator.validate(needle, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (caseSensitive) {
            return value.lastIndexOf(needle, offset);
        }
        return value.toLowerCase().lastIndexOf(needle.toLowerCase(), offset);
    }

    /**
     * Removes all spaces on left
     *
     * @param value The input String
     * @return String without left border spaces
     */
    public static String leftTrim(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.replaceAll("^\\s+", "");
    }

    /**
     * Returns length of String. Delegates to java.lang.String length method.
     *
     * @param value The input String
     * @return Length of the String
     */
    public static int length(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.length();
    }

    /**
     * Return a new String starting with prepends
     *
     * @param value    The input String
     * @param prepends Strings to prepend
     * @return The prepended String
     */
    public static String prepend(String value, String... prepends) {
        return prependArray(value, prepends);
    }

    /**
     * Return a new String starting with prepends
     *
     * @param value    The input String
     * @param prepends Strings to prepend
     * @return The prepended String
     */
    public static String prependArray(String value, String[] prepends) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (prepends == null || prepends.length == 0) {
            return value;
        }
        StringJoiner joiner = new StringJoiner("");
        for (String prepend : prepends) {
            joiner.add(prepend);
        }
        return joiner.toString() + value;
    }


    /**
     * Returns a new String with the prefix removed, if present. This is case sensitive.
     *
     * @param value  The input String
     * @param prefix String to remove on left
     * @return The String without prefix
     */
    public static String removeLeft(String value, String prefix) {
        return removeLeft(value, prefix, true);
    }

    /**
     * Returns a new String with the prefix removed, if present.
     *
     * @param value         The input String
     * @param prefix        String to remove on left
     * @param caseSensitive ensure case sensitivity
     * @return The String without prefix
     */
    public static String removeLeft(String value, String prefix, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        Validator.validate(prefix, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (caseSensitive) {
            return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
        }
        return value.toLowerCase().startsWith(prefix.toLowerCase()) ? value.substring(prefix.length()) : value;
    }

    /**
     * Remove all non word characters.
     *
     * @param value The input String
     * @return String without non-word characters
     */
    public static String removeNonWords(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.replaceAll("[^\\w]+", "");
    }

    /**
     * Returns a new string with the 'suffix' removed, if present. Search is case sensitive.
     *
     * @param value  The input String
     * @param suffix The suffix to remove
     * @return The String without suffix!
     */
    public static String removeRight(String value, String suffix) {
        return removeRight(value, suffix, true);
    }

    /**
     * Returns a new string with the 'suffix' removed, if present.
     *
     * @param value         The input String
     * @param suffix        The suffix to remove
     * @param caseSensitive whether search should be case sensitive or not
     * @return The String without suffix!
     */
    public static String removeRight(String value, String suffix, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        Validator.validate(suffix, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return endsWith(value, suffix, caseSensitive) ? value
                .substring(0, value.toLowerCase().lastIndexOf(suffix.toLowerCase())) : value;
    }

    /**
     * Remove all spaces and replace for value.
     *
     * @param value The input String
     * @return String without spaces
     */
    public static String removeSpaces(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.replaceAll("\\s", "");
    }

    /**
     * Returns a repeated string given a multiplier.
     *
     * @param value      The input String
     * @param multiplier Number of repeats
     * @return The String repeated
     */
    public static String repeat(String value, int multiplier) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return Stream.generate(() -> value).limit(multiplier).collect(joining());
    }

    /**
     * Replace all occurrences of 'search' value to 'newvalue'. Uses String replace method.
     *
     * @param value         The input
     * @param search        The String to search
     * @param newValue      The String to replace
     * @param caseSensitive whether search should be case sensitive or not
     * @return String replaced with 'newvalue'.
     */
    public static String replace(String value, String search, String newValue, boolean caseSensitive) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        Validator.validate(search, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (caseSensitive) {
            return value.replace(search, newValue);
        }
        return Pattern.compile(search, Pattern.CASE_INSENSITIVE).matcher(value)
                .replaceAll(Matcher.quoteReplacement(newValue));
    }


    /**
     * Returns a new string of a given length such that the ending of the string is padded.
     *
     * @param value  The input String
     * @param length Max length of String.
     * @param pad    Character to repeat
     * @return Right padded String
     */
    public static String rightPad(String value, String pad, int length) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (value.length() > length) {
            return value;
        }
        return append(value, repeat(pad, length - value.length()));
    }

    /**
     * Remove all spaces on right.
     *
     * @param value The String
     * @return String without right boarders spaces.
     */
    public static String rightTrim(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.replaceAll("\\s+$", "");
    }

    /**
     * Truncate the string securely, not cutting a word in half. It always returns the last full word.
     *
     * @param value  The input String
     * @param length Max size of the truncated String
     * @param filler String that will be added to the end of the return string. Example: '...'
     * @return The truncated String
     */
    public static String safeTruncate(String value, int length, String filler) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (length == 0) {
            return "";
        }
        if (length >= value.length()) {
            return value;
        }

        String[] words = words(value);
        StringJoiner result = new StringJoiner(" ");
        int spaceCount = 0;
        for (String word : words) {
            if (result.length() + word.length() + filler.length() + spaceCount > length) {
                break;
            } else {
                result.add(word);
                spaceCount++;
            }
        }
        return append(result.toString(), filler);
    }

    /**
     * Splits a String to words
     *
     * @param value The input String
     * @return Words Array
     */
    public static String[] words(String value) {
        return words(value, "\\s+");
    }

    /**
     * Splits a String to words by delimiter, \s+ by default
     *
     * @param value     The input String
     * @param delimiter delimiter for splitting input String
     * @return words array
     */
    public static String[] words(String value, String delimiter) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.split(delimiter);
    }


    /**
     * Truncate the unsecured form string, cutting the independent string of required position.
     *
     * @param value  Value will be truncated unsecurely.
     * @param length Size of the returned string.
     * @param filler Value that will be added to the end of the return string. Example: '...'
     * @return String truncated unsafely.
     */
    public static String truncate(String value, int length, String filler) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        if (length == 0) {
            return "";
        }
        if (length >= value.length()) {
            return value;
        }
        return append(value.substring(0, length - filler.length()), filler);
    }

    /**
     * htmlDecode方法。
     * * @param encodedHtml String类型参数
     *
     * @return static String类型返回值
     */
    public static String htmlDecode(String encodedHtml) {
        Validator.validate(encodedHtml, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        try {
            return URLDecoder.decode(encodedHtml, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * htmlEncode方法。
     * * @param html String类型参数
     *
     * @return static String类型返回值
     */
    public static String htmlEncode(String html) {
        Validator.validate(html, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        try {
            return URLEncoder.encode(html, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 乱排string
     *
     */
    public static String slice(String value, int begin, int end) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return value.substring(begin, end);
    }

    /**
     * Convert a String to a slug
     */
    public static String slugify(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return Arrays.stream(words(value.replace("&", "-and-"), "\\W+")).collect(joining("-"));
    }

    /**
     * surround方法。
     * * @param value String类型参数
     *
     * @param prefix String类型参数
     * @param suffix String类型参数
     * @return static String类型返回值
     */
    public static String surround(String value, String prefix, String suffix) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        String _prefix = Optional.ofNullable(prefix).orElse("");
        return append(_prefix, value, Optional.ofNullable(suffix).orElse(_prefix));
    }

    /**
     * toCamelCase方法。
     * * @param value String类型参数
     *
     * @return static String类型返回值
     */
    public static String toCamelCase(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        String str = toStudlyCase(value);
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * toStudlyCase方法。
     * * @param value String类型参数
     *
     * @return static String类型返回值
     */
    public static String toStudlyCase(String value) {
        Validator.validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        String[] words = collapseWhitespace(value.trim()).split("\\s*(_|-|\\s)\\s*");
        return Arrays.stream(words).filter(w -> !w.trim().isEmpty()).map(StringUtil::upperFirst).collect(joining());
    }

    /**
     * Return tail of the String
     *
     * @param value The input String
     * @return String tail
     */
    public static Optional<String> tail(String value) {
        return Optional.ofNullable(value).filter(v -> !v.isEmpty()).map(v -> last(v, v.length() - 1));
    }

    /**
     * Decamelize String
     *
     * @param value The input String
     * @param chr   string to use
     * @return String decamelized.
     */
    public static String toDecamelize(String value, String chr) {
        String camelCasedString = toCamelCase(value);
        String[] words = camelCasedString.split("(?=\\p{Upper})");
        return Arrays.stream(words).map(String::toLowerCase).collect(joining(Optional.ofNullable(chr).orElse(" ")));
    }

    /**
     * toKebabCase方法。
     * * @param value String类型参数
     *
     * @return static String类型返回值
     */
    public static String toKebabCase(String value) {
        return toDecamelize(value, "-");
    }

    /**
     * Transform to snake_case.
     *
     * @param value The input String
     * @return String in snake_case.
     */
    public static String toSnakeCase(String value) {
        return toDecamelize(value, "_");
    }


    /**
     * Converts the first character of string to upper case and the remaining to lower case.
     *
     * @param input The string to capitalize
     * @return The capitalized string
     */
    public static String capitalize(String input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        if (input.length() == 0) {
            return "";
        }
        return head(input).map(String::toUpperCase).map(h -> tail(input).map(t -> h + t.toLowerCase()).orElse(h)).get();
    }

    /**
     * Converts the first character of string to lower case.
     *
     * @param input The string to convert
     * @return The converted string
     */
    public static String lowerFirst(String input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        if (input.length() == 0) {
            return "";
        }

        return head(input).map(String::toLowerCase).map(h -> tail(input).map(t -> h + t).orElse(h)).get();
    }

    /**
     * Verifies whether String is enclosed by encloser
     *
     * @param input    The input String
     * @param encloser String which encloses input String
     * @return true if enclosed false otherwise
     */
    public static boolean isEnclosedBetween(String input, String encloser) {
        return isEnclosedBetween(input, encloser, encloser);
    }

    /**
     * Verifies whether String is enclosed by encloser
     *
     * @param input         The input String
     * @param leftEncloser  String which encloses input String at left start
     * @param rightEncloser String which encloses input String at the right end
     * @return true if enclosed false otherwise
     */
    public static boolean isEnclosedBetween(String input, String leftEncloser, String rightEncloser) {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        if (leftEncloser == null) {
            throw new IllegalArgumentException("leftEncloser can't be null");
        }
        if (rightEncloser == null) {
            throw new IllegalArgumentException("rightEncloser can't be null");
        }
        return input.startsWith(leftEncloser) && input.endsWith(rightEncloser);
    }


    /**
     * Removes leading characters from string.
     *
     * @param input The string to trim.
     * @param chars The characters to trim.
     * @return Returns the trimmed string.
     */
    public static Optional<String> trimStart(String input, String... chars) {
        return Optional.ofNullable(input).filter(v -> !v.isEmpty()).map(v -> {
            String pattern = String.format("^[%s]+", String.join("\\", Arrays.asList(chars)));
            return v.replaceAll(pattern, "");
        });
    }

    /**
     * 左边去除空字符串
     * 右边去除空字符串
     */
    public static Map<Character, Long> charsCount(String input) {
        if (isNullOrEmpty(input)) {
            return Collections.emptyMap();
        }
        return input.chars().mapToObj(c -> (char) c).collect(groupingBy(identity(), counting()));
    }

    /**
     * Changes passed in string to all lower case and adds underscore between words.
     *
     * @param input The input string
     * @return the input string in all lower case with underscores between words
     */
    public static String underscored(String input) {
        if (input == null || input.length() == 0) {
            return "";
        }

        return input.trim().replaceAll("([a-z\\d])([A-Z]+)", "$1_$2").replaceAll("[-\\s]+", "_").toLowerCase();
    }

    /**
     * 将有换行符的字符串 转化为list
     *
     * @param input 被转化的字符串
     * @return 以换行为单元切分的list
     */
    public static String[] lines(String input) {
        if (input == null) {
            return EMPTY_ARRAY;
        }
        return input.split("\r\n?|\n");
    }

    /**
     * Converts a underscored or camelized string into an dasherized one.
     *
     * @param input The input String
     * @return dasherized String.
     */
    public static String dasherize(String input) {
        return toKebabCase(input);
    }

    /**
     * Converts an underscored, camelized, or dasherized string into a humanized one. Also removes beginning and ending whitespace.
     *
     * @param input The input String
     * @return humanized version of String
     */
    public static String humanize(String input) {
        if (input == null || input.length() == 0) {
            return "";
        }
        return upperFirst(underscored(input).replaceAll("_", " "));
    }

    /**
     * Returns a copy of the string in which all the case-based characters have had their case swapped.
     *
     * @param input Input string
     * @return String with all the case swapped
     */
    public static String swapCase(String input) {
        if (input == null || input.length() == 0) {
            return "";
        }
        StringBuilder resultBuilder = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                resultBuilder.append(Character.toLowerCase(ch));
            } else {
                resultBuilder.append(Character.toUpperCase(ch));
            }
        }
        return resultBuilder.toString();
    }

    /**
     * Returns a string representation of the number passed in where groups of three digits are delimited by comma
     *
     * @param number Input number
     * @return formatted String
     */
    public static String formatNumber(long number) {
        String stringRepresentation = Long.toString(number);
        StringBuilder sb = new StringBuilder();
        int bound = stringRepresentation.length() - 1;
        String delimiter = ",";
        int counter = 0;
        for (int i = bound; i >= 0; i--) {
            char c = stringRepresentation.charAt(i);
            if (i != bound && counter % 3 == 0) {
                sb.append(delimiter);
            }
            sb.append(c);
            counter++;
        }
        return sb.reverse().toString();
    }

    /**
     * chop方法。
     * * @param input String类型参数
     *
     * @param step int类型参数
     * @return static String[]类型返回值
     */
    public static String[] chop(String input, int step) {
        if (input == null || input.length() == 0) {
            return EMPTY_ARRAY;
        }
        if (step == 0) {
            return new String[]{input};
        }
        int strLength = input.length();
        int iterations = strLength % step == 0 ? strLength / step : strLength / step + 1;
        return IntStream.iterate(0, i -> i + step)
                .limit(iterations)
                .mapToObj(i -> input.substring(i, (i + step) < strLength ? i + step : strLength))
                .toArray(String[]::new);
    }

    /**
     * Converts a String into its Start Case version
     * https://en.wikipedia.org/wiki/Letter_case#Stylistic_or_specialised_usage
     *
     * @param input The input String
     * @return Start Case String
     */
    public static String startCase(String input) {
        Validator.validate(input, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        // split into a word when we encounter a space, or an underscore, or a dash, or a switch from lower to upper case
        String[] words = words(input, "\\s|_|-|(?<=[a-z])(?=[A-Z])");
        return Arrays.stream(words).filter(w -> !w.trim().isEmpty())
                .map(w -> upperFirst(w.toLowerCase())).collect(joining(" "));
    }

    /**
     * escapeRegExp方法。
     * * @param input String类型参数
     *
     * @return static String类型返回值
     */
    public static String escapeRegExp(String input) {
        Validator.validate(input, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return input.replaceAll("[\\\\\\^\\$\\*\\+\\-\\?\\.\\|\\(\\)\\{\\}\\[\\]]", "\\\\$0");
    }


    private static long countSubstr(String value, String subStr, boolean allowOverlapping, long count) {
        int position = value.indexOf(subStr);
        if (position == -1) {
            return count;
        }
        int offset;
        if (!allowOverlapping) {
            offset = position + subStr.length();
        } else {
            offset = position + 1;
        }
        return countSubstr(value.substring(offset), subStr, allowOverlapping, ++count);
    }

    private static boolean isNullOrEmpty(String input) {
        return input == null || input.isEmpty();
    }

    /**
     * Returns a string composed of the given character repeated the specified number of times.
     *
     * @param ch     the character to repeat
     * @param repeat the number of times to repeat the character
     * @return a string containing the character repeated {@code repeat} times, or empty string if repeat is less than or equal to 0
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return EMPTY;
        }
        final char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }

    /**
     * 将半角的符号转换成全角符号.(即英文字符转中文字符)
     */
    public static String changeToFull(String str) {
        String source = "1234567890!@#$%^&*()abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_=+\\|[];:'\",<.>/?";
        String[] decode = {"１", "２", "３", "４", "５", "６", "７", "８", "９", "０",
                "！", "＠", "＃", "＄", "％", "︿", "＆", "＊", "（", "）", "ａ", "ｂ",
                "ｃ", "ｄ", "ｅ", "ｆ", "ｇ", "ｈ", "ｉ", "ｊ", "ｋ", "ｌ", "ｍ", "ｎ",
                "ｏ", "ｐ", "ｑ", "ｒ", "ｓ", "ｔ", "ｕ", "ｖ", "ｗ", "ｘ", "ｙ", "ｚ",
                "Ａ", "Ｂ", "Ｃ", "Ｄ", "Ｅ", "Ｆ", "Ｇ", "Ｈ", "Ｉ", "Ｊ", "Ｋ", "Ｌ",
                "Ｍ", "Ｎ", "Ｏ", "Ｐ", "Ｑ", "Ｒ", "Ｓ", "Ｔ", "Ｕ", "Ｖ", "Ｗ", "Ｘ",
                "Ｙ", "Ｚ", "－", "＿", "＝", "＋", "＼", "｜", "【", "】", "；", "：",
                "'", "\"", "，", "〈", "。", "〉", "／", "？"};
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            int pos = source.indexOf(str.charAt(i));
            if (pos != -1) {
                result += decode[pos];
            } else {
                result += str.charAt(i);
            }
        }
        return result;
    }

    /**
     * 将字符转换为编码为Unicode，格式 为'\u0020'<br>
     * unicodeEscaped(' ') = "\u0020"<br>
     * unicodeEscaped('A') = "\u0041"
     *
     * @param ch 待转换的char 字符
     * @return
     */
    public static String unicodeEscaped(char ch) {
        if (ch < 0x10) {
            return "\\u005Cu000" + Integer.toHexString(ch);
        } else if (ch < 0x100) {
            return "\\u005Cu00" + Integer.toHexString(ch);
        } else if (ch < 0x1000) {
            return "\\u005Cu0" + Integer.toHexString(ch);
        }
        return "\\u" + Integer.toHexString(ch);
    }

    /**
     * 进行toString操作，若为空，返回默认值
     *
     * @param object  要进行toString操作的对象
     * @param nullStr 返回的默认值
     * @return
     */
    public static String toString(Object object, String nullStr) {
        return object == null ? nullStr : object.toString();
    }


    /**
     * 页面中去除字符串中的空格、回车、换行符、制表符
     */
    public static String replaceBlank(String str) {
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            str = m.replaceAll("");
        }
        return str;
    }

    /**
     * 字符串转换unicode.实现native2ascii.exe类似的功能
     *
     * @param string 需要处理的字符串
     */
    public static String string2Unicode(String string) {
        StringBuilder uni = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            String temp = "\\u" + Integer.toHexString(string.charAt(i));
            uni.append(temp);
        }
        return uni.toString();
    }

    /**
     * 转字符串 实现native2ascii.exe类似的功能
     *
     * @param unicode 需要处理的字符串
     */
    public static String unicode2String(String unicode) {
        StringBuilder str = new StringBuilder();
        String[] hex = unicode.split("\\\\u005Cu");
        for (int i = 1; i < hex.length; i++) {
            int data = Integer.parseInt(hex[i], 16);
            str.append((char) data);
        }
        return str.toString();
    }


    /**
     * 返回俩个字符串共同的前缀
     */
    public static String hasPrefix(String str1, String str2) {
        //@TODO-字符串方法实现
        return null;
    }

    /**
     * 返回俩个字符串共同的后缀
     */
    public static String hasSuffix(String str1, String str2) {
        //TODO-字符串方法实现
        return null;
    }

    /**
     * 数字格式化
     */
    public static String formatNumber(BigDecimal obj, String format) {
        DecimalFormat df = new DecimalFormat(format);
        if (obj.compareTo(BigDecimal.ZERO) > 0 && obj.compareTo(new BigDecimal(1)) < 0) {
            return "0" + df.format(obj);
        } else {
            return df.format(obj);
        }
    }

    /**
     * 判断字符串是否有长度
     */
    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * 下划线转驼峰命名
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            boolean nextUpperCase = true;

            if (i < (str.length() - 1)) {
                nextUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if ((i > 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    sb.append(SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }


    /**
     * underlineToBigCamelCase方法。
     * * @param source String类型参数
     *
     * @return static String类型返回值
     */
    public static String underlineToBigCamelCase(String source) {
        if (isEmpty(source)) {
            return source;
        }
        List<String> splitList = listSplit(source, UNDERLINE);
        StringBuilder builder = new StringBuilder();
        for (String s : splitList) {
            builder.append(upperFirst(s.toLowerCase()));
        }
        return builder.toString();
    }

    /**
     * strikeToBigCamelCase方法。
     * * @param source String类型参数
     *
     * @return static String类型返回值
     */
    public static String strikeToBigCamelCase(String source) {
        if (isEmpty(source)) {
            return source;
        }
        List<String> splitList = listSplit(source, MINUS);
        StringBuilder builder = new StringBuilder();
        for (String s : splitList) {
            builder.append(upperFirst(s.toLowerCase()));
        }
        return builder.toString();
    }

    /**
     * strikeToLittleCamelCase方法。
     * * @param source String类型参数
     *
     * @return static String类型返回值
     */
    public static String strikeToLittleCamelCase(String source) {
        return lowerFirst(strikeToBigCamelCase(source));
    }

    /**
     * strikeToUnderline方法。
     * * @param source String类型参数
     *
     * @return static String类型返回值
     */
    public static String strikeToUnderline(String source) {
        if (isEmpty(source)) {
            return source;
        }
        List<String> splitList = listSplit(source, MINUS);
        StringJoiner result = new StringJoiner(UNDERLINE);
        for (String s : splitList) {
            result.add(s);
        }
        return String.valueOf(result);
    }

    /**
     * underlineUpperToLittleCamelCase方法。
     * * @param source String类型参数
     *
     * @return static String类型返回值
     */
    public static String underlineUpperToLittleCamelCase(String source) {
        if (isEmpty(source)) {
            return source;
        }
        List<String> splitList = listSplit(source, UNDERLINE);
        StringBuilder builder = new StringBuilder();
        for (String s : splitList) {
            builder.append(upperFirst(s));
        }
        return builder.toString();
    }

    /**
     * @author: zifang
     * @description: Convert underscores to underscores in a string and capitalize
     * @description: 将字符串中的中划线转下划线并大写
     * @time: 2022-08-23 09:18:32
     * @params: [source] 源字符串
     * @return: java.lang.String 转换后字符串
     */
    public static String strikeToUnderlineUpper(String source) {
        if (isEmpty(source)) {
            return source;
        }
        List<String> splitList = listSplit(source, MINUS);
        StringJoiner result = new StringJoiner(UNDERLINE);
        for (String s : splitList) {
            result.add(s.toUpperCase());
        }
        return String.valueOf(result);
    }

    /**
     * @author: zifang
     * @description: Convert an underlined string to camelCase
     * @description: 将带下划线的字符串转小驼峰式
     * @time: 2019-06-13 17:31:00
     * @params: [source] 源字符串
     * @return: java.lang.String 转换后字符串
     */
    public static String underlineToLittleCamelCase(String source) {
        return lowerFirst(underlineToBigCamelCase(source));
    }

    /**
     * @author: zifang
     * @description: Capital letters
     * @description: 首字母大写
     * @time: 2019-06-13 17:31:00
     * @params: [source] 源字符串
     * @return: java.lang.String 转换后字符串
     */
    public static String upperFirst(String source) {
        if (isEmpty(source)) {
            return source;
        }
        char[] chars = source.toCharArray();
        chars[0] = 97 <= chars[0] && chars[0] <= 122 ? (char) (chars[0] - 32) : chars[0];
        return String.valueOf(chars);
    }


    /**
     * 首字母小写
     */
    public static String firstLetterLowercase(String source) {
        if (isEmpty(source)) {
            return source;
        }
        char[] chars = source.toCharArray();
        chars[0] = 65 <= chars[0] && chars[0] <= 90 ? (char) (chars[0] + 32) : chars[0];
        return String.valueOf(chars);
    }

    /**
     * @author: zifang
     * @description: Check Email Format
     * @description: 校验邮箱格式
     * @time: 2019-11-02 16:43:00
     * @params: [email] 邮箱
     * @return: boolean 是否符合邮箱格式
     */
    public static boolean validateEmailFormat(String email) {
        if (isEmpty(email)) {
            return false;
        }
        String regex = "^([a-z0-9A-Z]+[-|.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * trim方法。
     * * @param originStr String类型参数
     *
     * @return static String类型返回值
     */
    public static String trim(String originStr) {
        return trim(originStr, " ");
    }


    /**
     * @author: zifang
     * @description: Remove the specified string at the beginning and end of the original string (more
     * than one will be removed)
     * @description: 去除原始字符串中开头和结尾的指定字符串(多个都会去除)
     * @time: 2020-09-28 14:46:00
     * @params: [originStr, trim] 源字符串，首尾要去除的空格
     * @return: java.lang.String 转换后字符串
     */
    public static String trim(String originStr, String trim) {
        if (isEmpty(originStr)) {
            return originStr;
        }
        String result = trimStart(originStr, trim);
        return trimEnd(result, trim);
    }

    private static String trimStart(String str, String trim) {
        if (null == str || isEmpty(trim)) {
            return str;
        }
        while (true) {
            if (str.startsWith(trim)) {
                str = str.substring(trim.length());
            } else {
                break;
            }
        }
        return str;
    }

    private static String trimEnd(String str, String trim) {
        if (null == str || isEmpty(trim)) {
            return str;
        }
        while (true) {
            if (str.endsWith(trim)) {
                str = str.substring(0, str.length() - trim.length());
            } else {
                break;
            }
        }
        return str;
    }


    /**
     * listSplit方法。
     * * @param str String类型参数
     *
     * @param separator String类型参数
     * @return static List<String>类型返回值
     */
    public static List<String> listSplit(String str, String separator) {
        return listSplit(str, separator, true);
    }

    /**
     * split方法。
     * * @param str String类型参数
     *
     * @param separator String类型参数
     * @return static String[]类型返回值
     */
    public static String[] split(String str, String separator) {
        List<String> listSplit = listSplit(str, separator);
        String[] strArray = new String[listSplit.size()];
        listSplit.toArray(strArray);
        return strArray;
    }

    /**
     * @author: zifang
     * @description: Split the source string by the specified string
     * @description: 按照指定字符串分割源字符串
     * @time: 2021-05-21 17:04:00
     * @params: [str, delimiter, allowStrEmpty] 字符串，分隔符，空字符是否算一个字符串
     * @return: java.util.List<java.lang.String> 响应参数
     */
    public static List<String> listSplit(String str, String separator, boolean allowStrEmpty) {
        if (str == null) {
            return null;
        } else {
            int len = str.length();
            if (len == 0) {
                return Collections.emptyList();
            } else if (separator != null && !"".equals(separator)) {
                int separatorLength = separator.length();
                List<String> substrings = new ArrayList<>();
                int numberOfSubstrings = 0;
                int beg = 0;
                int end = 0;

                while (end < len) {
                    end = str.indexOf(separator, beg);
                    if (end > -1) {
                        boolean flag = allowStrEmpty ? end >= beg : end > beg;
                        if (flag) {
                            ++numberOfSubstrings;
                            if (numberOfSubstrings == -1) {
                                end = len;
                                substrings.add(str.substring(beg));
                            } else {
                                substrings.add(str.substring(beg, end));
                                beg = end + separatorLength;
                            }
                        } else {

                            beg = end + separatorLength;
                        }
                    } else {
                        substrings.add(str.substring(beg));
                        end = len;
                    }
                }

                return substrings;
            } else {
                {
                    List<String> list = new ArrayList<>();
                    int sizePlus1 = 1;
                    int i = 0;
                    int start = 0;
                    boolean match = false;
                    if (separator != null) {
                        label:
                        while (true) {
                            while (true) {
                                if (i >= len) {
                                    break label;
                                }

                                match = true;
                                ++i;
                            }
                        }
                    } else {
                        label:
                        while (true) {
                            while (true) {
                                if (i >= len) {
                                    break label;
                                }

                                if (Character.isWhitespace(str.charAt(i))) {
                                    if (match) {
                                        if (sizePlus1++ == -1) {
                                            i = len;
                                        }

                                        list.add(str.substring(start, i));
                                        match = false;
                                    }

                                    ++i;
                                    start = i;
                                } else {
                                    match = true;
                                    ++i;
                                }
                            }
                        }
                    }

                    if (match) {
                        list.add(str.substring(start, i));
                    }

                    return list;
                }
            }
        }
    }

    /**
     * Checks if the character sequence contains only numeric characters.
     *
     * @param cs the character sequence to check
     * @return true if all characters are digits, false otherwise (including null or empty)
     */
    public static boolean isNumeric(CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        } else {
            int sz = cs.length();

            for (int i = 0; i < sz; ++i) {
                if (!Character.isDigit(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Checks if the character sequence is empty (null or length of zero).
     *
     * @param cs the character sequence to check
     * @return true if the character sequence is empty, false otherwise
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 判断字符串中是否包含中文字符（CJK 统一表意符号区间 U+4E00 至 U+9FA5）。
     *
     * @param cs 待检查的字符串，为 null 或空白时返回 false
     * @return 只要包含一个中文字符即返回 true，否则返回 false
     */
    public static boolean isContainChinese(CharSequence cs) {
        if (cs == null) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            char c = cs.charAt(i);
            if (c >= '\u4e00' && c <= '\u9fa5') {
                return true;
            }
        }
        return false;
    }

    /**
     * parseString方法。
     * * @param object Object类型参数
     *
     * @return static String类型返回值
     */
    public static String parseString(Object object) {
        if (null == object) {
            return null;
        }
        return object.toString();
    }

    /**
     * parseStringOrDefault方法。
     * * @param object Object类型参数
     *
     * @param defaultValue String类型参数
     * @return static String类型返回值
     */
    public static String parseStringOrDefault(Object object, String defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        return object.toString();
    }

    /**
     * getOrDefault方法。
     * * @param str String类型参数
     *
     * @param defaultValue String类型参数
     * @return static String类型返回值
     */
    public static String getOrDefault(String str, String defaultValue) {
        return isNotEmpty(str) ? str : defaultValue;
    }


    /**
     * parseSteamToString方法。
     * * @param inputStream InputStream类型参数
     *
     * @return static String类型返回值
     */
    public static String parseSteamToString(InputStream inputStream) {
        if (null == inputStream) {
            throw new IllegalArgumentException("inputStream is null");
        }
        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().parallel().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * parseStringToStream方法。
     * * @param content String类型参数
     *
     * @param charset Charset类型参数
     * @return static InputStream类型返回值
     */
    public static InputStream parseStringToStream(String content, Charset charset) {
        if (isEmpty(content)) {
            throw new IllegalArgumentException("content is empty");
        }
        return new ByteArrayInputStream(content.getBytes(charset));
    }

    /**
     * parseStringToStream方法。
     * * @param content String类型参数
     *
     * @return static InputStream类型返回值
     */
    public static InputStream parseStringToStream(String content) {
        return parseStringToStream(content, Charset.defaultCharset());
    }

    /**
     * random方法。
     * * @param count int类型参数
     *
     * @return static String类型返回值
     */
    public static String random(int count) {
        return random(count, true, true);
    }

    /**
     * random方法。
     * * @param count int类型参数
     *
     * @param letters boolean类型参数
     * @param numbers boolean类型参数
     * @return static String类型返回值
     */
    public static String random(int count, boolean letters, boolean numbers) {
        return random(count, 0, 0, letters, numbers);
    }

    /**
     * random方法。
     * * @param count int类型参数
     *
     * @param start   int类型参数
     * @param end     int类型参数
     * @param letters boolean类型参数
     * @param numbers boolean类型参数
     * @return static String类型返回值
     */
    public static String random(int count, int start, int end, boolean letters, boolean numbers) {
        return random(count, start, end, letters, numbers, null);
    }

    /**
     * random方法。
     * * @param count int类型参数
     *
     * @param start   int类型参数
     * @param end     int类型参数
     * @param letters final类型参数
     * @param numbers final类型参数
     * @param chars   final类型参数
     * @return static String类型返回值
     */
    public static String random(int count, int start, int end, final boolean letters,
                                final boolean numbers, final char[] chars) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException(
                    "Requested random string length " + count + " is less than 0.");
        }
        if (chars != null && chars.length == 0) {
            throw new IllegalArgumentException("The chars array must not be empty");
        }

        if (start == 0 && end == 0) {
            if (chars != null) {
                end = chars.length;
            } else if (!letters && !numbers) {
                end = Character.MAX_CODE_POINT;
            } else {
                end = 'z' + 1;
                start = ' ';
            }
        } else if (end <= start) {
            throw new IllegalArgumentException(
                    "Parameter end (" + end + ") must be greater than start (" + start + ")");
        }

        final int zeroDigitAscii = 48;
        final int firstLetterAscii = 65;

        if (chars == null && (numbers && end <= zeroDigitAscii
                || letters && end <= firstLetterAscii)) {
            throw new IllegalArgumentException(
                    "Parameter end (" + end + ") must be greater then (" + zeroDigitAscii
                            + ") for generating digits " +
                            "or greater then (" + firstLetterAscii + ") for generating letters.");
        }

        final StringBuilder builder = new StringBuilder(count);
        final int gap = end - start;

        while (count-- != 0) {
            final int codePoint;
            if (chars == null) {
                codePoint = ThreadLocalRandom.current().nextInt(gap) + start;

                switch (Character.getType(codePoint)) {
                    case Character.UNASSIGNED:
                    case Character.PRIVATE_USE:
                    case Character.SURROGATE:
                        count++;
                        continue;
                    default:
                }

            } else {
                codePoint = chars[ThreadLocalRandom.current().nextInt(gap) + start];
            }

            final int numberOfChars = Character.charCount(codePoint);
            if (count == 0 && numberOfChars > 1) {
                count++;
                continue;
            }

            if (letters && Character.isLetter(codePoint) || numbers && Character.isDigit(codePoint)
                    || !letters && !numbers) {
                builder.appendCodePoint(codePoint);

                if (numberOfChars == 2) {
                    count--;
                }

            } else {
                count++;
            }
        }
        return builder.toString();
    }

    /**
     * setIfPresent方法。
     * * @param value String类型参数
     *
     * @param setter ConsumerString类型参数
     * @return static void类型返回值
     */
    public static void setIfPresent(String value, Consumer<String> setter) {
        if (isNotEmpty(value)) {
            setter.accept(value);
        }
    }


    /**
     * Left pad a String with a specified character.
     *
     * @param str     the String to pad, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return left padded String or null if null String input
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        } else {
            int pads = size - str.length();
            if (pads <= 0) {
                return str;
            } else {
                return pads > 8192 ? leftPad(str, size, String.valueOf(padChar)) : repeat(padChar, pads).concat(str);
            }
        }
    }

    /**
     * Left pad a String with a specified string.
     *
     * @param str    the String to pad, may be null
     * @param size   the size to pad to
     * @param padStr the string to pad with, space by default if empty or null
     * @return left padded String or null if null String input
     */
    public static String leftPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            // returns original String when possible
            return str;
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return leftPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return padStr.concat(str);
        } else if (pads < padLen) {
            return padStr.substring(0, pads).concat(str);
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return new String(padding).concat(str);
        }
    }


    /**
     * Right pad a String with a specified character.
     *
     * @param str     the String to pad, may be null
     * @param size    the size to pad to
     * @param padChar the character to pad with
     * @return right padded String or null if null String input
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str.length() < size) {
            String padding = repeat(padChar, size - str.length());
            return str + padding;
        }
        return str;
    }

    /**
     * Right pad a String with a specified string.
     *
     * @param str    the String to pad, may be null
     * @param size   the size to pad to
     * @param padStr the string to pad with, space by default if empty or null
     * @return right padded String or null if null String input
     */
    public static String rightPad(final String str, final int size, String padStr) {
        if (str == null) {
            return null;
        }
        if (isEmpty(padStr)) {
            padStr = SPACE;
        }
        final int padLen = padStr.length();
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (padLen == 1 && pads <= PAD_LIMIT) {
            return rightPad(str, size, padStr.charAt(0));
        }

        if (pads == padLen) {
            return str.concat(padStr);
        } else if (pads < padLen) {
            return str.concat(padStr.substring(0, pads));
        } else {
            final char[] padding = new char[pads];
            final char[] padChars = padStr.toCharArray();
            for (int i = 0; i < pads; i++) {
                padding[i] = padChars[i % padLen];
            }
            return str.concat(new String(padding));
        }
    }


    /**
     * Right pad a String with a specified string, truncating if exceeds size.
     *
     * @param str    the String to pad, may be null
     * @param size   the size to pad to
     * @param padStr the string to pad with, space by default if empty or null
     * @return right padded String truncated to size, or null if null String input
     */
    public static String rightPadWithOver(final String str, final int size, String padStr) {
        if (str.length() > size) {
            return str.substring(0, size);
        }
        return rightPad(str, size, padStr);
    }

    /**
     * join方法。
     * * @param delimiter final类型参数
     *
     * @param strings final类型参数
     * @return static String类型返回值
     */
    public static String join(final char delimiter, final String... strings) {
        if (strings.length == 0) {
            return null;
        }
        if (strings.length == 1) {
            return strings[0];
        }
        int length = strings.length - 1;
        for (final String s : strings) {
            if (s == null) {
                continue;
            }
            length += s.length();
        }
        final StringBuilder sb = new StringBuilder(length);
        if (strings[0] != null) {
            sb.append(strings[0]);
        }
        for (int i = 1; i < strings.length; ++i) {
            if (!isEmpty(strings[i])) {
                sb.append(delimiter).append(strings[i]);
            } else {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Replaces the first non-overlapping occurrence of the search string with the replacement string.
     * If no occurrence is found, the original text is returned unchanged.
     *
     * @param text         the text to search and replace in
     * @param searchString the string to search for
     * @param replacement  the string to replace the found search string with
     * @return the resulting string after replacement
     */
    public static String replace(final String text, final String searchString,
                                 final String replacement) {
        return replace(text, searchString, replacement, -1);
    }

    /**
     * Replaces the first non-overlapping occurrences of the search string with the replacement string,
     * up to the specified maximum number of replacements.
     * If max is -1, replaces all occurrences.
     *
     * @param text         the text to search and replace in
     * @param searchString the string to search for
     * @param replacement  the string to replace the found search string with
     * @param max          the maximum number of replacements to make, -1 for all
     * @return the resulting string after replacement
     */
    public static String replace(final String text, final String searchString,
                                 final String replacement, final int max) {
        return replace(text, searchString, replacement, max, false);
    }

    private static String replace(final String text, String searchString, final String replacement,
                                  int max, final boolean ignoreCase) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        if (ignoreCase) {
            searchString = searchString.toLowerCase();
        }
        int start = 0;
        int end = ignoreCase ? indexOfIgnoreCase(text, searchString, start)
                : indexOf(text, searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        final int replLength = searchString.length();
        int increase = Math.max(replacement.length() - replLength, 0);
        increase *= max < 0 ? 16 : Math.min(max, 64);
        final StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = ignoreCase ? indexOfIgnoreCase(text, searchString, start)
                    : indexOf(text, searchString, start);
        }
        buf.append(text, start, text.length());
        return buf.toString();
    }

    /**
     * 在字符串中查找子串（不区分大小写），从位置0开始
     *
     * @param str       被搜索的字符序列
     * @param searchStr 要查找的子串
     * @return 子串第一次出现的位置，未找到返回-1
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr) {
        return indexOfIgnoreCase(str, searchStr, 0);
    }

    /**
     * 在字符串中从指定位置开始查找子串（不区分大小写）
     *
     * @param str       被搜索的字符序列
     * @param searchStr 要查找的子串
     * @param startPos  开始搜索的位置
     * @return 子串第一次出现的位置，未找到返回-1
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr,
                                        int startPos) {
        if (str == null || searchStr == null) {
            return INDEX_NOT_FOUND;
        }
        if (startPos < 0) {
            startPos = 0;
        }
        final int endLimit = str.length() - searchStr.length() + 1;
        if (startPos > endLimit) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return startPos;
        }
        for (int i = startPos; i < endLimit; i++) {
            if (regionMatches(str, i, searchStr, searchStr.length())) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 在字符序列中从指定位置开始查找子串的位置（区分大小写）
     *
     * @param sequence       被搜索的字符序列
     * @param searchSequence 要查找的子串
     * @param startPos       开始搜索的位置
     * @return 子串第一次出现的位置，未找到返回-1
     */
    public static int indexOf(final CharSequence sequence, final CharSequence searchSequence,
                              final int startPos) {
        if (sequence == null || searchSequence == null) {
            return INDEX_NOT_FOUND;
        }
        if (sequence instanceof String) {
            return ((String) sequence).indexOf(searchSequence.toString(), startPos);
        } else if (sequence instanceof StringBuilder) {
            return ((StringBuilder) sequence).indexOf(searchSequence.toString(), startPos);
        } else if (sequence instanceof StringBuffer) {
            return ((StringBuffer) sequence).indexOf(searchSequence.toString(), startPos);
        }
        return sequence.toString().indexOf(searchSequence.toString(), startPos);
    }

    private static boolean regionMatches(final CharSequence cs,
                                         final int thisStart, final CharSequence substring, final int length) {
        if (cs instanceof String && substring instanceof String) {
            return ((String) cs).regionMatches(true, thisStart, (String) substring, 0, length);
        }
        int index1 = thisStart;
        int index2 = 0;
        int tmpLen = length;

        final int srcLen = cs.length() - thisStart;
        final int otherLen = substring.length();

        if ((thisStart < 0) || (length < 0)) {
            return false;
        }

        if (srcLen < length || otherLen < length) {
            return false;
        }

        while (tmpLen-- > 0) {
            final char c1 = cs.charAt(index1++);
            final char c2 = substring.charAt(index2++);

            if (c1 == c2) {
                continue;
            }

            final char u1 = Character.toUpperCase(c1);
            final char u2 = Character.toUpperCase(c2);
            if (u1 != u2 && Character.toLowerCase(u1) != Character.toLowerCase(u2)) {
                return false;
            }
        }

        return true;
    }


    /**
     * isFormat方法。
     * * @param message String类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean isFormat(String message) {
        Matcher matcher = Patterns.FORMAT_PATTERN.matcher(message);
        return matcher.find();
    }

    /**
     * getReader方法。
     * * @param str CharSequence类型参数
     *
     * @return static StringReader类型返回值
     */
    public static StringReader getReader(CharSequence str) {
        if (null == str) {
            return null;
        }
        return new StringReader(str.toString());
    }

    /**
     * str方法。
     * * @param cs CharSequence类型参数
     *
     * @return static String类型返回值
     */
    public static String str(CharSequence cs) {
        return null == cs ? null : cs.toString();
    }


    /**
     * atoi方法。
     * * @param str String类型参数
     *
     * @return static int类型返回值
     */
    public static int atoi(String str) {
        long result = 0;

        boolean start = false;
        boolean plusOrMinus = true;
        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if (Character.isDigit(currentChar)) {
                int numericValue = Character.getNumericValue(currentChar);
                if (plusOrMinus) {
                    result = result * 10 + numericValue;
                    if (result > Integer.MAX_VALUE) {
                        return Integer.MAX_VALUE;
                    }
                } else {
                    result = result * 10 - numericValue;
                    if (result < Integer.MIN_VALUE) {
                        return Integer.MIN_VALUE;
                    }
                }

                start = true;
            } else if (start) {
                break;
            } else if (MINUS_CHAR == currentChar) {
                plusOrMinus = false;
                start = true;
            } else if (PLUS_CHAR == currentChar) {
                start = true;
            } else if (SPACE_CHAR == currentChar) {
            } else {
                break;
            }
        }
        return (int) result;
    }

    /**
     * hasText方法。
     * * @param str String类型参数
     *
     * @return static boolean类型返回值
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }


    /**
     * @author: zifang
     * @description: The character sequence contains the specified character sequence
     * @description: 字符序列包含指定字符序列
     * @time: 2022-06-07 16:27:01
     * @params: [seq, searchSeq] 字符序列，被包含字符序列
     * @return: boolean 是否包含
     */
    public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
        if (seq == null || searchSeq == null) {
            return false;
        }
        return indexOf(seq, searchSeq, 0) >= 0;
    }


    /**
     * @author: zifang
     * @description: The character sequence contains the specified character
     * @description: 字符序列包含指定字符
     * @time: 2022-06-07 16:29:41
     * @params: [seq, searchChar] 字符序列，被包含字符
     * @return: boolean 是否包含
     */
    public static boolean contains(final CharSequence seq, final int searchChar) {
        if (isEmpty(seq)) {
            return false;
        }
        return indexOf(seq, searchChar, 0) >= 0;
    }

    /**
     * @author: zifang
     * @description: Retrieves the specified character starting from the specified subscript in the
     * string
     * @description: 在字符串中从指定下标开始检索指定字符
     * @time: 2022-06-07 16:06:50
     * @params: [sequence, searchChar, startPos] 字符串，检索字符，起始下标
     * @return: int 下标
     */
    public static int indexOf(final CharSequence cs, final int searchChar, int start) {
        if (cs instanceof String) {
            return ((String) cs).indexOf(searchChar, start);
        }
        final int sz = cs.length();
        if (start < 0) {
            start = 0;
        }
        if (searchChar < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            for (int i = start; i < sz; i++) {
                if (cs.charAt(i) == searchChar) {
                    return i;
                }
            }
            return INDEX_NOT_FOUND;
        }
        if (searchChar <= Character.MAX_CODE_POINT) {
            final char[] chars = Character.toChars(searchChar);
            for (int i = start; i < sz - 1; i++) {
                final char high = cs.charAt(i);
                final char low = cs.charAt(i + 1);
                if (high == chars[0] && low == chars[1]) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

//        /**
//     * @author: zifang
//     * @description: Get a random chinese character
//     * @description: 随机获取一个汉字
//     * @time: 2022-09-12 19:47:24
//     * @params: []
//     * @return: char random chinese character 随机汉字
//     */
//    public static char getRandomChineseCharacter() {
//        return (char) (CHINESE_CHARACTER_START + ThreadLocalRandom.current()
//                .nextInt(CHINESE_CHARACTER_LENGTH));
//    }
}

package com.zifang.util.core;

import java.util.List;

/**
 * @author zifang
 */
public class Const {

    /**
     * 空字符串
     */
    public static final String STR_EMPTY = "";
    /**
     * 未登陆
     */
    public final static String SYS_NOLOGIN = "Nologin";
    /**
     * 成功
     */
    public final static String SYS_SUCCESS = "Success";
    /**
     * 失败
     */
    public final static String SYS_ERROR = "Error";
    /**
     * 异常
     */
    public final static String SYS_EXCEPTION = "Exception";
    /**
     * 无记录
     */
    public final static String SYS_NORECORD = "NoRecord";
    /**
     * 业务访问
     */
    public final static String BUSINESS_ACCESS = "ACCESS";
    /**
     * 业务插入
     */
    public final static String BUSINESS_INSERT = "INSERT";
    /**
     * 业务更新
     */
    public final static String BUSINESS_UPDATE = "UPDATE";
    /**
     * 业务删除
     */
    public final static String BUSINESS_DELETE = "DELETE";
    /**
     * 业务文件上传
     */
    public final static String BUSINESS_UPLOAD = "UPLOAD";
    public static final String HEX_CHAR_STR = "0123456789ABCDEF";
    byte BYTE_FALSE = (byte) 0;
    byte BYTE_TRUE = (byte) 1;
    byte FALSE = 0;
    byte TRUE = 1;
    /**
     * script template suffix
     * 脚本模板后缀
     */
    String SCRIPT_TEMPLATE_SUFFIX = ".template";
    /**
     * calculate method name
     * 校验脚本方法名
     */
    String CALCULATE_METHOD_NAME = "calculate";
    String FUNCTION_PARAM = "$0";
    String SIMPLE_FUNCTION_PARAM = "$";
    String INVOCABLE_KEY_FORMAT = "%s:%s:%s";
    String ZERO = "0";
    String ONE = "1";
    String EMPTY = "";
    String[] EMPTY_ARRAY = new String[0];
    String DEFAULT = "default";
    String LINE_BREAK = "\n";

    interface EnumCollectors<T> {
        List<T> getList();
    }

    public static class Symbol {
        public static String AND = "&";
        public static String EQUALS = "=";
        public static String SPOT = ".";
        public static String REGEX_SPOT = "\\.";
        public static String COMMA = ",";
        public static String MINUS = "-";
        public static char MINUS_CHAR = '-';
        public static String PLUS = "+";
        public static char PLUS_CHAR = '+';
        public static String UNDERLINE = "_";
        public static String LEFT_SQ_BRACKET = "[";
        public static String RIGHT_SQ_BRACKET = "]";
        public static String SLASH = "/";
        public static String POUND = "#";
        public static String QUESTION_MARK = "?";
        public static String HYPHEN = "|";
        public static String DOUBLE_HYPHEN = "||";
        public static String COLON = ":";
        public static String SEMICOLON = ";";
        public static String ASTERISK = "*";
        public static String SINGLE_QUOTES = "'";
        public static String SPACE = " ";
        public static char SPACE_CHAR = ' ';
        public static String PER_CENT = "%";
        public static String PER_MILL = "‰";
        public static String RIGHT_CURLY_BRACE = "}";
        public static String LEFT_CURLY_BRACE = "{";
        public static String CURLY_BRACE = "{}";
        public static String AT = "@";
        public static String DOLLAR = "$";
        /**
         * "方法。
         * * @param " ";类型参数
         *
         * @return static String LEFT_BRACKET =类型返回值
         */
        public static String LEFT_BRACKET = "(";
        public static String RIGHT_BRACKET = ")";
    }

    public static class OperateSystem implements EnumCollectors<OperateSystem> {

        private final String system;
        private final String lowerSystem;

        OperateSystem WINDOWS = new OperateSystem("Windows", "windows");
        OperateSystem Mac = new OperateSystem("Mac", "mac");
        OperateSystem Unix = new OperateSystem("Unix", "x11");
        OperateSystem LINUX = new OperateSystem("LINUX", "linux");
        OperateSystem Android = new OperateSystem("Android", "android");
        OperateSystem IPhone = new OperateSystem("IPhone", "iphone");
        OperateSystem UnKnown = new OperateSystem("UnKnown", "unKnown");
        OperateSystem LOCAL_OPERATE_SYSTEM = getOperateSystem(System.getProperty("os.name"));

        /**
         * OperateSystem方法。
         * * @param system String类型参数
         *
         * @param lowerSystem String类型参数
         */
        public OperateSystem(String system, String lowerSystem) {
            this.system = system;
            this.lowerSystem = lowerSystem;
        }

        /**
         * getSystem方法。
         *
         * @return String类型返回值
         */
        public String getSystem() {
            return system;
        }

        /**
         * getLowerSystem方法。
         *
         * @return String类型返回值
         */
        public String getLowerSystem() {
            return lowerSystem;
        }

        /**
         * getOperateSystem方法。
         * * @param info String类型参数
         *
         * @return OperateSystem类型返回值
         */
        public OperateSystem getOperateSystem(String info) {
            return getList().stream()
                    .filter(system -> info.toLowerCase().contains(system.getLowerSystem()))
                    .findFirst()
                    .orElse(UnKnown);
        }

        @Override
        /**
         * getList方法。
         * @return List<OperateSystem>类型返回值
         */
        public List<OperateSystem> getList() {
            return null;
        }

        @Override
        /**
         * equals方法。
         *      * @param o Object类型参数
         * @return boolean类型返回值
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OperateSystem that = (OperateSystem) o;
            return java.util.Objects.equals(system, that.system) &&
                    java.util.Objects.equals(lowerSystem, that.lowerSystem);
        }

        @Override
        /**
         * hashCode方法。
         * @return int类型返回值
         */
        public int hashCode() {
            return java.util.Objects.hash(system, lowerSystem);
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "OperateSystem{system=" + system + ", lowerSystem=" + lowerSystem + "}";
        }
    }

    public static class Statics {
        /**
         * Unicode 基本汉字编码范围0x4e00~0x9fa5 共 20902个
         */
        int CHINESE_CHARACTER_LENGTH = 20902;
        /**
         * 汉字起始值
         */
        int CHINESE_CHARACTER_START = 0x4e00;

        /**
         * getChineseCharacterLength方法。
         *
         * @return int类型返回值
         */
        public int getChineseCharacterLength() {
            return CHINESE_CHARACTER_LENGTH;
        }

        /**
         * getChineseCharacterStart方法。
         *
         * @return int类型返回值
         */
        public int getChineseCharacterStart() {
            return CHINESE_CHARACTER_START;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "Statics{CHINESE_CHARACTER_LENGTH=" + CHINESE_CHARACTER_LENGTH +
                    ", CHINESE_CHARACTER_START=" + CHINESE_CHARACTER_START + "}";
        }

        @Override
        /**
         * equals方法。
         *      * @param o Object类型参数
         * @return boolean类型返回值
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Statics statics = (Statics) o;
            return CHINESE_CHARACTER_LENGTH == statics.CHINESE_CHARACTER_LENGTH &&
                    CHINESE_CHARACTER_START == statics.CHINESE_CHARACTER_START;
        }

        @Override
        /**
         * hashCode方法。
         * @return int类型返回值
         */
        public int hashCode() {
            return java.util.Objects.hash(CHINESE_CHARACTER_LENGTH, CHINESE_CHARACTER_START);
        }
    }

    public static class Charset {
        public static final String US_ASCII = "US-ASCII";
        public static final String ISO_8859_1 = "ISO-8859-1";
        public static final String UTF_8 = "UTF-8";
        public static final String UTF_16BE = "UTF-16BE";
        public static final String UTF_16LE = "UTF-16LE";
        public static final String UTF_16 = "UTF-16";
        public static final String GBK = "GBK";
        public static final String GB2312 = "GB2312";

        /**
         * Charset方法。
         */
        public Charset() {
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "Charset{US_ASCII=" + US_ASCII + ", ISO_8859_1=" + ISO_8859_1 +
                    ", UTF_8=" + UTF_8 + ", UTF_16BE=" + UTF_16BE + ", UTF_16LE=" + UTF_16LE +
                    ", UTF_16=" + UTF_16 + ", GBK=" + GBK + ", GB2312=" + GB2312 + "}";
        }

        @Override
        /**
         * equals方法。
         *      * @param o Object类型参数
         * @return boolean类型返回值
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Charset charset = (Charset) o;
            return true;
        }

        @Override
        /**
         * hashCode方法。
         * @return int类型返回值
         */
        public int hashCode() {
            return 0;
        }
    }

    public static class JvmProperties {
        public static final String JVM_VERSION = "java.version";
        public static final String JVM_ENCODING = "file.encoding";
        public static final String JVM_TEMPDIR = "java.io.tmpdir";

        /**
         * http代理主机标识
         */
        public static final String HTTP_PROXY_HOST = "http.proxyHost";

        /**
         * http代理主机端口
         */
        public static final String HTTP_PROXY_PORT = "http.proxyPort";

        /**
         * http代理用户标识
         */
        public static final String HTTP_PROXY_USER = "http.proxyUser";

        /**
         * http代理用户密码标识
         */
        public static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";

        /**
         * 主机架构
         */
        public static final String SYS_OS_ARCH = "os.arch";
        /**
         * 主机类型
         */
        public static final String SYS_OS_NAME = "os.name";
        /**
         * 主机类型版本
         */
        public static final String SYS_OS_VERSION = "os.version";
        /**
         * 操作系统类型
         */
        public static final String SYS_SUN_DESKTOP = "sun.desktop";

        /**
         * 系统文件分隔符key
         */
        public static final String SYS_FILE_SEPARATOR = "file.separator";
        /**
         * 系统路径分隔符key
         */
        public static final String SYS_PATH_SEPARATOR = "path.separator";

        /**
         * 系统换行符key
         */
        public static final String SYS_LINE_SEPARATOR = "line.separator";

        /**
         * JvmProperties方法。
         */
        public JvmProperties() {
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "JvmProperties{JVM_VERSION=" + JVM_VERSION + ", JVM_ENCODING=" + JVM_ENCODING +
                    ", JVM_TEMPDIR=" + JVM_TEMPDIR + ", HTTP_PROXY_HOST=" + HTTP_PROXY_HOST +
                    ", HTTP_PROXY_PORT=" + HTTP_PROXY_PORT + ", HTTP_PROXY_USER=" + HTTP_PROXY_USER +
                    ", HTTP_PROXY_PASSWORD=" + HTTP_PROXY_PASSWORD + ", SYS_OS_ARCH=" + SYS_OS_ARCH +
                    ", SYS_OS_NAME=" + SYS_OS_NAME + ", SYS_OS_VERSION=" + SYS_OS_VERSION +
                    ", SYS_SUN_DESKTOP=" + SYS_SUN_DESKTOP + ", SYS_FILE_SEPARATOR=" + SYS_FILE_SEPARATOR +
                    ", SYS_PATH_SEPARATOR=" + SYS_PATH_SEPARATOR + ", SYS_LINE_SEPARATOR=" + SYS_LINE_SEPARATOR + "}";
        }

        @Override
        /**
         * equals方法。
         *      * @param o Object类型参数
         * @return boolean类型返回值
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JvmProperties that = (JvmProperties) o;
            return true;
        }

        @Override
        /**
         * hashCode方法。
         * @return int类型返回值
         */
        public int hashCode() {
            return 0;
        }
    }

    /**
     * TimeFormat类。
     */
    public class TimeFormat {
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String TIME_FORMAT = "HH:mm:ss";
    }
}
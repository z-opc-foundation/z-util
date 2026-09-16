package com.zifang.util.core.net;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * URL 处理工具类
 * <p>
 * 提供 URL 编码、解码、参数解析、参数构建等功能。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // URL 编码
 * String encoded = UrlUtil.encode("你好");
 *
 * // URL 解码
 * String decoded = UrlUtil.decode("hello%20world");
 *
 * // 获取参数值
 * String name = UrlUtil.getParamValue("<a href="http://example.com?name=">...</a>张三", "name");
 *
 * // 添加/修改参数
 * String newUrl = UrlUtil.setParam("http://example.com", "name", "李四");
 *
 * // 移除参数
 * String removed = UrlUtil.removeParam("http://example.com?name=张三", "name");
 *
 * // 构建带参数的 URL
 * String url = new UrlUtil.Builder("/api/user")
 *     .queryParam("name", "张三")
 *     .queryParam("age", "20")
 *     .toString();
 * </pre>
 *
 * @author zifang
 * @since 1.0
 */
public class UrlUtil {

    /**
     * UTF-8 字符集
     */
    private static final String CHARSET_UTF_8 = StandardCharsets.UTF_8.name();

    private UrlUtil() {
        // 工具类禁止实例化
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 对字符串进行 URL 编码（使用 UTF-8）
     *
     * @param url 待编码的字符串
     * @return 编码后的字符串
     */
    public static String encode(String url) {
        return encode(url, CHARSET_UTF_8);
    }

    /**
     * 对字符串进行 URL 编码
     * <p>
     * 注意：{@link URLEncoder} 是 application/x-www-form-urlencoded 编码，
     * 会把空格编码为 +，但现代 URL 路径/查询通常用 %20 表示空格，因此这里把 + 替换为 %20。
     * </p>
     *
     * @param url      待编码的字符串
     * @param encoding 编码字符集
     * @return 编码后的字符串
     */
    public static String encode(String url, String encoding) {
        if (url == null) {
            return null;
        }
        try {
            return URLEncoder.encode(url, encoding).replace("+", "%20");
        } catch (Exception e) {
            // 忽略编码异常，返回原字符串
            return url;
        }
    }

    /**
     * 对字符串进行 URL 解码（使用 UTF-8）
     *
     * @param url 待解码的字符串
     * @return 解码后的字符串
     */
    public static String decode(String url) {
        return decode(url, CHARSET_UTF_8);
    }

    /**
     * 对字符串进行 URL 解码
     *
     * @param url      待解码的字符串
     * @param encoding 解码字符集
     * @return 解码后的字符串
     */
    public static String decode(String url, String encoding) {
        if (url == null) {
            return null;
        }
        try {
            return URLDecoder.decode(url, encoding);
        } catch (Exception e) {
            // 忽略解码异常，返回原字符串
            return url;
        }
    }

    /**
     * 向 URL 中添加或修改参数
     *
     * @param url        原始 URL
     * @param paramName  参数名
     * @param paramValue 参数值（会自动 URL 编码）
     * @return 添加/修改参数后的 URL
     */
    public static String setParam(String url, String paramName, String paramValue) {
        if (paramName == null) {
            return null;
        }
        if (url == null) {
            return null;
        }
        String encodedValue = encode(paramValue);
        int tempIndex = url.indexOf("?");
        if (tempIndex != -1) {
            int paramIndex = url.indexOf(paramName + "=", tempIndex + 1);
            if (paramIndex != -1) {
                int nextAmp = url.indexOf("&", paramIndex + paramName.length() + 1);
                if (nextAmp != -1) {
                    return url.substring(0, paramIndex) + paramName + "=" + encodedValue + url.substring(nextAmp);
                }
                return url.substring(0, paramIndex) + paramName + "=" + encodedValue;
            } else {
                char lastChar = url.charAt(url.length() - 1);
                if (lastChar == '&') {
                    return url + paramName + "=" + encodedValue;
                }
                return url + "&" + paramName + "=" + encodedValue;
            }
        } else {
            return url + "?" + paramName + "=" + encodedValue;
        }
    }

    /**
     * 获取 URL 中指定参数的值
     *
     * @param url       URL 字符串
     * @param paramName 参数名
     * @return 参数值（未找到返回 null）
     */
    public static String getParamValue(String url, String paramName) {
        if (url == null || paramName == null) {
            return null;
        }
        int tempIndex = url.indexOf("?");
        if (tempIndex != -1) {
            int paramIndex = url.indexOf(paramName + "=", tempIndex + 1);
            if (paramIndex != -1) {
                int nextAmp = url.indexOf("&", paramIndex + paramName.length() + 1);
                if (nextAmp != -1) {
                    return decode(url.substring(paramIndex + paramName.length() + 1, nextAmp));
                }
                return decode(url.substring(paramIndex + paramName.length() + 1));
            }
        }
        return null;
    }

    /**
     * 移除 URL 中的指定参数
     *
     * @param url        URL 字符串
     * @param paramNames 要移除的参数名（支持多个）
     * @return 移除参数后的 URL
     */
    public static String removeParam(String url, String... paramNames) {
        if (url == null || paramNames == null) {
            return url;
        }
        for (String paramName : paramNames) {
            url = removeParamSingle(url, paramName);
        }
        return url;
    }

    /**
     * 移除 URL 中的单个参数
     *
     * @param url       URL 字符串
     * @param paramName 要移除的参数名
     * @return 移除参数后的 URL
     */
    private static String removeParamSingle(String url, String paramName) {
        if (url == null || paramName == null) {
            return url;
        }
        int tempIndex = url.indexOf("?");
        if (tempIndex != -1) {
            int paramIndex = url.indexOf(paramName + "=", tempIndex + 1);
            if (paramIndex != -1) {
                int nextAmp = url.indexOf("&", paramIndex + paramName.length() + 1);
                if (nextAmp != -1) {
                    return url.substring(0, paramIndex) + url.substring(nextAmp + 1);
                }
                // 参数是最后一个，移除 ? 或 &
                if (paramIndex == tempIndex + 1) {
                    // 参数是唯一参数，移除整个查询字符串
                    return url.substring(0, tempIndex);
                }
                return url.substring(0, paramIndex - 1);
            }
        }
        return url;
    }

    /**
     * 拼接相对路径到绝对 URL
     *
     * @param baseUrl        基础 URL
     * @param locationHeader Location 响应头
     * @return 拼接后的完整 URL
     */
    public static String urlJoin(URL baseUrl, String locationHeader) {
        if (baseUrl == null || locationHeader == null) {
            return baseUrl != null ? baseUrl.toString() : locationHeader;
        }
        try {
            if (locationHeader.startsWith("http")) {
                return new URL(locationHeader).toString();
            }
            // 相对路径需要保证以 / 开头，否则 new URL(protocol, authority, file) 会拼成 example.comsubpath
            String path = locationHeader;
            if (!path.startsWith("/") && !path.startsWith("?")) {
                path = "/" + path;
            }
            return new URL(baseUrl.getProtocol(), baseUrl.getAuthority(), path).toString();
        } catch (MalformedURLException e) {
            return baseUrl.toString();
        }
    }

    /**
     * 从 HttpServletRequest 中获取所有请求参数
     *
     * @param request HTTP 请求对象
     * @return 参数名到参数值的映射
     */
    public static Map<String, String> getRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        if (request == null) {
            return params;
        }
        java.util.Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues != null && paramValues.length > 0) {
                // 单值参数直接取第一个，非空值存入
                if (paramValues.length == 1 && !paramValues[0].isEmpty()) {
                    params.put(paramName, paramValues[0]);
                } else if (paramValues.length > 1) {
                    // 多值参数用逗号连接
                    params.put(paramName, String.join(",", paramValues));
                }
            }
        }
        return params;
    }

    /**
     * 解析查询字符串为 Map
     * <p>
     * 例如：a=1&amp;b=2 解析为 {a=1, b=2}
     * </p>
     *
     * @param query   查询字符串
     * @param split1  参数分隔符（通常为 &）
     * @param split2  键值分隔符（通常为 =）
     * @param dupLink 多值连接符（为 null 时后者覆盖前者）
     * @return 解析后的 Map
     */
    public static Map<String, String> parseQuery(String query, char split1, char split2, String dupLink) {
        if (query == null || query.isEmpty() || query.indexOf(split2) <= 0) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();
        String name = null;
        StringBuilder value = null;
        int len = query.length();

        for (int i = 0; i < len; i++) {
            char c = query.charAt(i);
            if (c == split2) {
                value = new StringBuilder();
            } else if (c == split1) {
                if (name != null && value != null) {
                    putValue(result, name, value.toString(), dupLink);
                }
                name = null;
                value = null;
            } else if (value != null) {
                value.append(c);
            } else {
                name = (name != null) ? name + c : "" + c;
            }
        }

        if (name != null && value != null) {
            putValue(result, name, value.toString(), dupLink);
        }

        return result;
    }

    /**
     * 向 Map 中放入值，支持多值连接
     */
    private static void putValue(Map<String, String> map, String key, String value, String dupLink) {
        if (dupLink != null) {
            String existing = map.get(key);
            if (existing != null) {
                value = value + dupLink + existing;
            }
        }
        map.put(key, value);
    }

    /**
     * 解析 HTTP 查询字符串
     * <p>
     * 使用 &amp; 作为参数分隔符，= 作为键值分隔符。
     * </p>
     *
     * @param queryUri 查询字符串（不含 ?）
     * @return 解析后的 Map
     */
    public static Map<String, String> httpParseQuery(String queryUri) {
        if (queryUri == null || queryUri.isEmpty()) {
            return new HashMap<>();
        }
        return parseQuery(queryUri, '&', '=', ",");
    }

    /**
     * 解码 URL 查询字符串（将 + 视为空格）
     *
     * @param source 待解码的字符串
     * @return 解码后的字符串
     */
    public static String decodeQuery(String source) {
        return decodeQuery(source, CHARSET_UTF_8);
    }

    /**
     * 解码 URL 查询字符串（将 + 视为空格）
     *
     * @param source   待解码的字符串
     * @param encoding 字符编码
     * @return 解码后的字符串
     */
    public static String decodeQuery(String source, String encoding) {
        if (source == null) {
            return null;
        }
        try {
            // 将 + 替换为空格（URL 编码中 + 表示空格）
            String decoded = source.replace("+", " ");
            return URLDecoder.decode(decoded, encoding);
        } catch (Exception e) {
            return source;
        }
    }

    /**
     * URL 构建器，支持链式调用构建带参数的 URL
     * <p>
     * <b>使用示例：</b>
     * <pre>
     * String url = new UrlUtil.Builder("/api/user")
     *     .queryParam("name", "张三")
     *     .queryParam("age", "20")
     *     .toString();
     * // 结果：/api/user?name=%E5%BC%A0%E4%B8%89&amp;age=20
     * </pre>
     */
    public static class Builder {
        private final StringBuilder url;
        private final String encoding;
        private boolean hasParams;

        /**
         * 创建 URL 构建器
         *
         * @param path       初始路径
         * @param encodePath 是否对路径进行 URL 编码
         * @param encoding   编码字符集
         */
        public Builder(String path, boolean encodePath, String encoding) {
            this.encoding = encoding;
            this.url = new StringBuilder();
            if (encodePath) {
                // 路径编码：保留 /，只对每段做 URL 编码
                this.url.append(encodePath(path, encoding));
            } else {
                this.url.append(path);
            }
            this.hasParams = this.url.indexOf("?") != -1;
        }

        /**
         * 创建默认 UTF-8 编码的 URL 构建器
         *
         * @param path 初始路径
         */
        public Builder(String path) {
            this(path, true, StandardCharsets.UTF_8.name());
        }

        /**
         * 编码路径段：保留 / 分隔符，只对每段做 URL 编码
         */
        private String encodePath(String path, String encoding) {
            if (path == null) {
                return null;
            }
            // 先按 / 拆开，逐段编码，最后用 / 拼回
            String[] segments = path.split("/", -1);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                if (i > 0) {
                    sb.append('/');
                }
                sb.append(encode(segments[i], encoding));
            }
            return sb.toString();
        }

        /**
         * 添加查询参数
         *
         * @param name  参数名
         * @param value 参数值（会自动编码）
         * @return this
         */
        public Builder queryParam(String name, String value) {
            if (name == null) {
                return this;
            }
            if (hasParams) {
                url.append("&");
            } else {
                url.append("?");
                hasParams = true;
            }
            url.append(encode(name, encoding)).append("=").append(encode(value, encoding));
            return this;
        }

        /**
         * 添加多个查询参数
         *
         * @param params 参数映射
         * @return this
         */
        public Builder queryParams(Map<String, String> params) {
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    queryParam(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        /**
         * 添加路径片段（会自动处理斜杠）
         *
         * @param segment 路径片段
         * @return this
         */
        public Builder pathSegment(String segment) {
            if (segment == null || segment.isEmpty()) {
                return this;
            }
            if (!url.toString().endsWith("/") && !segment.startsWith("/")) {
                url.append("/");
            } else if (url.toString().endsWith("/") && segment.startsWith("/")) {
                segment = segment.substring(1);
            }
            url.append(segment);
            return this;
        }

        /**
         * 构建 URL 字符串
         *
         * @return URL 字符串
         */
        @Override
        public String toString() {
            return url.toString();
        }

        /**
         * 获取编码后的 URL
         *
         * @return URL 字符串
         */
        public String build() {
            return toString();
        }
    }
}

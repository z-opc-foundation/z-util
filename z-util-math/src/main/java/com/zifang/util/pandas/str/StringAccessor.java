package com.zifang.util.pandas.str;

import com.zifang.util.pandas.Series;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringAccessor 类 - Series 的字符串操作方法
 * 对标 pandas.Series.str 访问器
 * 提供字符串处理、正则表达式匹配、文本提取等功能
 */
public class StringAccessor {

    private final Series series;
    private final String[] stringData;

    /**
     * StringAccessor方法。
     * * @param series Series类型参数
     */
    public StringAccessor(Series series) {
        this.series = series;
        // 将 Series 中的数值数据转换为字符串
        double[] data = series.toArray();
        this.stringData = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            if (Double.isNaN(data[i])) {
                this.stringData[i] = "";
            } else {
                // 如果是整数值，显示为整数，否则显示为小数
                if (data[i] == Math.floor(data[i])) {
                    this.stringData[i] = String.valueOf((long) data[i]);
                } else {
                    this.stringData[i] = String.valueOf(data[i]);
                }
            }
        }
    }

    /**
     * 获取字符串长度
     */
    public Series length() {
        double[] result = new double[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i].length();
        }
        return new Series(result, series.index(), "length", null);
    }

    /**
     * 转换为小写
     */
    public Series lower() {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i].toLowerCase();
        }
        return createStringSeries(result, "lower");
    }

    /**
     * 转换为大写
     */
    public Series upper() {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i].toUpperCase();
        }
        return createStringSeries(result, "upper");
    }

    /**
     * 去除首尾空白字符
     */
    public Series strip() {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i].trim();
        }
        return createStringSeries(result, "strip");
    }

    /**
     * 去除左侧空白字符
     */
    public Series lstrip() {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            String val = stringData[i];
            int j = 0;
            while (j < val.length() && Character.isWhitespace(val.charAt(j))) {
                j++;
            }
            result[i] = val.substring(j);
        }
        return createStringSeries(result, "lstrip");
    }

    /**
     * 去除右侧空白字符
     */
    public Series rstrip() {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            String val = stringData[i];
            int j = val.length() - 1;
            while (j >= 0 && Character.isWhitespace(val.charAt(j))) {
                j--;
            }
            result[i] = val.substring(0, j + 1);
        }
        return createStringSeries(result, "rstrip");
    }

    /**
     * 字符串替换
     */
    public Series replace(String oldStr, String newStr) {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i].replace(oldStr, newStr);
        }
        return createStringSeries(result, "replace");
    }

    /**
     * 正则表达式替换
     */
    public Series replaceRegex(String pattern, String replacement) {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i].replaceAll(pattern, replacement);
        }
        return createStringSeries(result, "replace_regex");
    }

    /**
     * 检查是否包含子字符串
     */
    public Series contains(String subStr) {
        double[] result = new double[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i].contains(subStr) ? 1.0 : 0.0;
        }
        return new Series(result, series.index(), "contains", null);
    }

    /**
     * 正则表达式匹配
     */
    public Series match(String pattern) {
        double[] result = new double[stringData.length];
        Pattern p = Pattern.compile(pattern);
        for (int i = 0; i < stringData.length; i++) {
            Matcher m = p.matcher(stringData[i]);
            result[i] = m.matches() ? 1.0 : 0.0;
        }
        return new Series(result, series.index(), "match", null);
    }

    /**
     * 提取匹配正则表达式的第一个分组
     */
    public Series extract(String pattern, int group) {
        String[] result = new String[stringData.length];
        Pattern p = Pattern.compile(pattern);
        for (int i = 0; i < stringData.length; i++) {
            String val = stringData[i];
            Matcher m = p.matcher(val);
            if (m.find() && group <= m.groupCount()) {
                result[i] = m.group(group);
            } else {
                result[i] = "";
            }
        }
        return createStringSeries(result, "extract");
    }

    /**
     * 分割字符串
     */
    public Series split(String delimiter, int index) {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            String val = stringData[i];
            String[] parts = val.split(delimiter);
            if (index < parts.length) {
                result[i] = parts[index];
            } else {
                result[i] = "";
            }
        }
        return createStringSeries(result, "split");
    }

    /**
     * 切片字符串
     */
    public Series slice(int start, int end) {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            String val = stringData[i];
            int s = start >= 0 ? start : Math.max(0, val.length() + start);
            int e = end >= 0 ? Math.min(val.length(), end) : val.length() + end;
            if (s >= e || s >= val.length()) {
                result[i] = "";
            } else {
                result[i] = val.substring(s, e);
            }
        }
        return createStringSeries(result, "slice");
    }

    /**
     * 重复字符串
     */
    public Series repeat(int n) {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            String val = stringData[i];
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(val);
            }
            result[i] = sb.toString();
        }
        return createStringSeries(result, "repeat");
    }

    /**
     * 连接字符串（当前 Series 与另一个 Series 或常量）
     */
    public Series cat(String other) {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            result[i] = stringData[i] + other;
        }
        return createStringSeries(result, "cat");
    }

    /**
     * cat方法。
     * * @param other Series类型参数
     *
     * @return Series类型返回值
     */
    public Series cat(Series other) {
        String[] result = new String[stringData.length];
        for (int i = 0; i < stringData.length; i++) {
            String val1 = stringData[i];
            String val2 = i < other.length() ? String.valueOf(other.toArray()[i]) : "";
            result[i] = val1 + val2;
        }
        return createStringSeries(result, "cat");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建字符串 Series（将字符串存储为数值索引）
     */
    private Series createStringSeries(String[] values, String operationName) {
        // 将字符串转换为数值索引存储
        Map<String, Integer> stringToIndex = new HashMap<>();
        List<String> uniqueStrings = new ArrayList<>();
        double[] numericValues = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            String val = values[i];
            Integer index = stringToIndex.get(val);
            if (index == null) {
                index = uniqueStrings.size();
                stringToIndex.put(val, index);
                uniqueStrings.add(val);
            }
            numericValues[i] = index;
        }

        // 创建一个新的 Series，其中包含数值索引
        // 同时保存字符串映射（通过名称来存储）
        String seriesName = operationName + "_" + String.join(",", uniqueStrings.subList(0, Math.min(5, uniqueStrings.size())));
        return new Series(numericValues, series.index(), seriesName, null);
    }
}
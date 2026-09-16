package com.zifang.util.parser.ini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * INI 文件解析器
 */

/**
 * IniParser类。
 */
public class IniParser {

    private static final Pattern SECTION_PATTERN = Pattern.compile("^\\[([^\\]]*)\\]\\s*$");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([^=]+)=(.*)$");

    /**
     * 解析 INI 格式字符串
     */
    /**
     * parse方法。
     * * @param content String类型参数
     *
     * @return IniFile类型返回值
     */
    public IniFile parse(String content) {
        return parse(new StringReader(content));
    }

    /**
     * 解析 INI 格式 Reader
     */
    /**
     * parse方法。
     * * @param reader Reader类型参数
     *
     * @return IniFile类型返回值
     */
    public IniFile parse(Reader reader) {
        IniFile iniFile = new IniFile();
        IniSection currentSection = null;
        List<String> rawLines = readLines(reader);

        // 合并续行
        List<String> lines = mergeContinuationLines(rawLines);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 跳过空行
            if (line.isEmpty()) {
                continue;
            }

            // 跳过注释行（; 和 #）
            if (line.startsWith(";") || line.startsWith("#")) {
                continue;
            }

            // 解析 Section
            Matcher sectionMatcher = SECTION_PATTERN.matcher(line);
            if (sectionMatcher.matches()) {
                String sectionName = sectionMatcher.group(1);
                currentSection = new IniSection(sectionName);
                iniFile.addSection(currentSection);
                continue;
            }

            // 解析 Key=Value
            Matcher kvMatcher = KEY_VALUE_PATTERN.matcher(line);
            if (kvMatcher.matches()) {
                String key = kvMatcher.group(1).trim();
                String value = kvMatcher.group(2);

                // 处理 inline 注释（; 或 #）
                int commentIndex = findInlineCommentIndex(value);
                if (commentIndex >= 0) {
                    value = value.substring(0, commentIndex).trim();
                }

                // 如果没有当前 section，创建全局 section
                if (currentSection == null) {
                    currentSection = new IniSection();
                    iniFile.addSection(currentSection);
                }
                currentSection.put(key, value);
                continue;
            }

            // 无法解析的行，抛出异常
            throw new IniException("无法解析行 " + (i + 1) + ": " + lines.get(i));
        }

        return iniFile;
    }

    /**
     * 合并续行（行尾有 \ 表示继续到下一行）
     */
    private List<String> mergeContinuationLines(List<String> rawLines) {
        List<String> lines = new ArrayList<>();
        StringBuilder pendingLine = new StringBuilder();

        for (String rawLine : rawLines) {
            if (pendingLine.length() > 0) {
                pendingLine.append(rawLine);
            } else {
                pendingLine.append(rawLine);
            }

            if (pendingLine.length() > 0 && pendingLine.charAt(pendingLine.length() - 1) == '\\') {
                // 继续到下一行
                pendingLine.setLength(pendingLine.length() - 1);
            } else {
                // 完成一行
                lines.add(pendingLine.toString());
                pendingLine.setLength(0);
            }
        }

        // 处理最后一行（理论上不应该有续行残留）
        if (pendingLine.length() > 0) {
            lines.add(pendingLine.toString());
        }

        return lines;
    }

    /**
     * 查找 inline 注释的起始位置
     */
    private int findInlineCommentIndex(String value) {
        int semicolonIndex = value.indexOf(';');
        int hashIndex = value.indexOf('#');

        if (semicolonIndex >= 0 && hashIndex >= 0) {
            return Math.min(semicolonIndex, hashIndex);
        } else if (semicolonIndex >= 0) {
            return semicolonIndex;
        } else if (hashIndex >= 0) {
            return hashIndex;
        }
        return -1;
    }

    /**
     * 读取所有行
     */
    private List<String> readLines(Reader reader) {
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IniException("读取 INI 文件失败", e);
        }
        return lines;
    }

    /**
     * 将 IniFile 写回 INI 格式字符串
     */
    /**
     * store方法。
     * * @param iniFile IniFile类型参数
     *
     * @return String类型返回值
     */
    public String store(IniFile iniFile) {
        StringBuilder sb = new StringBuilder();
        for (IniSection section : iniFile.getSections()) {
            if (section.getName() != null && !section.getName().isEmpty()) {
                sb.append("[").append(section.getName()).append("]").append("\n");
            }
            for (java.util.Map.Entry<String, String> entry : section.getValues().entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
}

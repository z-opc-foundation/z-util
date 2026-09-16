package com.zifang.util.parser.ini;

import java.util.ArrayList;
import java.util.List;

/**
 * INI 文件数据模型
 */

/**
 * IniFile类。
 */
public class IniFile {

    private List<IniSection> sections;

    /**
     * IniFile方法。
     */
    public IniFile() {
        this.sections = new ArrayList<>();
    }

    /**
     * getSections方法。
     *
     * @return List<IniSection>类型返回值
     */
    public List<IniSection> getSections() {
        return sections;
    }

    /**
     * setSections方法。
     * * @param sections ListIniSection类型参数
     */
    public void setSections(List<IniSection> sections) {
        this.sections = sections;
    }

    /**
     * addSection方法。
     * * @param section IniSection类型参数
     */
    public void addSection(IniSection section) {
        sections.add(section);
    }

    /**
     * 获取指定名称的 Section
     */
    /**
     * getSection方法。
     * * @param name String类型参数
     *
     * @return IniSection类型返回值
     */
    public IniSection getSection(String name) {
        for (IniSection section : sections) {
            if (section.getName() != null && section.getName().equals(name)) {
                return section;
            }
        }
        return null;
    }

    /**
     * 获取全局 Section（第一个 name 为 null 的 Section）
     */
    /**
     * getGlobalSection方法。
     *
     * @return IniSection类型返回值
     */
    public IniSection getGlobalSection() {
        for (IniSection section : sections) {
            if (section.getName() == null || section.getName().isEmpty()) {
                return section;
            }
        }
        return null;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IniSection section : sections) {
            sb.append(section.toString());
        }
        return sb.toString();
    }
}

package com.zifang.util.visuallization.swing.manager.subpanels;


import javax.swing.*;
import java.util.Map;

/**
 * 子面板注册器
 * 用于管理界面上子面板的注册和查找
 */
public class SubPanelRegister {

    private Map<Integer, JPanel> registedPanel;

    /**
     * 注册一个子面板
     *
     * @param id     面板唯一标识
     * @param jPanel 面板实例
     * @return 当前注册器实例，支持链式调用
     * @throws RuntimeException 如果ID已存在
     */
    public SubPanelRegister register(Integer id, JPanel jPanel) {
        if (registedPanel.containsKey(id)) {
            throw new RuntimeException("id :" + id + " is dumplicate");
        }

        registedPanel.put(id, jPanel);
        return this;
    }
}

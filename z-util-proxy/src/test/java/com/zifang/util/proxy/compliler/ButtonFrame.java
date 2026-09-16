package com.zifang.util.proxy.compliler;

import javax.swing.*;

/**
 * @author zifang Horstmann
 * @version 1.00 2007-11-02
 */
public abstract class ButtonFrame extends JFrame {
    public static final int DEFAULT_WIDTH = 300;
    public static final int DEFAULT_HEIGHT = 200;

    protected JPanel panel;
    protected JButton yellowButton;
    protected JButton blueButton;
    protected JButton redButton;

    /**
     * ButtonFrame方法。
     */
    public ButtonFrame() {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        panel = new JPanel();
        add(panel);

        yellowButton = new JButton("Yellow");
        blueButton = new JButton("Blue");
        redButton = new JButton("Red");

        panel.add(yellowButton);
        panel.add(blueButton);
        panel.add(redButton);

        addEventHandlers();
    }

    /**
     * addEventHandlers方法。
     *
     * @return abstract void类型返回值
     */
    protected abstract void addEventHandlers();
}

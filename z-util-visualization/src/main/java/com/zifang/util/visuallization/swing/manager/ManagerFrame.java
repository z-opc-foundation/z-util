package com.zifang.util.visuallization.swing.manager;

import com.zifang.util.visuallization.swing.manager.tree.RegisterTreeNode;
import com.zifang.util.visuallization.swing.manager.tree.RegisterTreeNodeHelper;
import com.zifang.util.visuallization.swing.manager.tree.TreeNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Enumeration;

/**
 * 管理界面主窗口
 * 展示树形结构的企业管理系统导航面板
 */
public class ManagerFrame extends JFrame {

    private static final int DISCONTIGUOUS_TREE_SELECTION = 4;//单选的常量值为1，连选的常量值为2，多选的常量值为4

    private JSplitPane mainSplitPane = new JSplitPane();

    private JTree treePointed;

    /**
     * 启动管理界面
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        ManagerFrame managerFrame = new ManagerFrame();
        managerFrame.init();//当前管理系统的全量初始化
    }

    private void init() {
        initComponents();//初始化组件
        initListener();// 初始化监听器
        initPackage();// 初始化组件装配
        initSysConfig();// 初始化系统配置
    }

    private void initListener() {
        addJTreeListener();
    }

    private void addJTreeListener() {

        TreeSelectionModel treeSelect;
        treeSelect = treePointed.getSelectionModel();//获得树的选择模式
        treeSelect.setSelectionMode(DISCONTIGUOUS_TREE_SELECTION);//设置树的选择模式为多选
        treePointed.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            /**
             * valueChanged方法。
             *      * @param e TreeSelectionEvent类型参数
             */
            public void valueChanged(TreeSelectionEvent e) {

                if (!treePointed.isSelectionEmpty()) {//判断节点是否被选中，被选中为0，没被选中为1
                    TreePath[] selectionPath = treePointed.getSelectionPaths();//获取所有被选中节点的路径
                    for (int i = 0; i < selectionPath.length; i++) {
                        TreePath path = selectionPath[i];
                        Object[] obj = path.getPath();//以Object数组的形式返回该路径中所有节点的对象
                        for (int j = 0; j < obj.length; j++) {
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj[j];// 获得节点
                            System.out.print(node.getUserObject());
                            JPanel jPanel = new JPanel();
                            jPanel.add(new JButton(node.getUserObject().toString()));
                            mainSplitPane.setRightComponent(jPanel);
                        }
                        System.out.println();
                    }
                }
            }
        });
    }

    private void initSysConfig() {
//        this.setSize(new Dimension(500, 500));
        //this.setLocation(500, 500);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void initPackage() {
        this.setContentPane(mainSplitPane);
        mainSplitPane.setLeftComponent(treePointed);
        mainSplitPane.setRightComponent(new Button());
    }

    private void initComponents() {
        initJSplitPane();// 初始化分割面板
        initJTreePane();// 初始化左树
    }

    private void initJTreePane() {

        RegisterTreeNode registerTreeNode = new RegisterTreeNode();
        registerTreeNode.register(new TreeNode("root", "企业人事管理系统", null))
                .register(new TreeNode("1", "人事管理", "root"))
                .register(new TreeNode("2", "待遇管理", "root"))
                .register(new TreeNode("3", "系统维护", "root"))
                .register(new TreeNode("1-1", "账套管理", "1"))
                .register(new TreeNode("1-2", "考勤管理", "1"))
                .register(new TreeNode("1-3", "奖惩管理", "1"))
                .register(new TreeNode("1-4", "培训管理", "1"))
                .register(new TreeNode("2-1", "帐套管理", "2"))
                .register(new TreeNode("2-2", "人员设置", "2"))
                .register(new TreeNode("2-3", "待遇报表", "2"))
                .register(new TreeNode("3-1", "企业架构", "3"))
                .register(new TreeNode("3-2", "基本资料", "3"))
                .register(new TreeNode("3-3", "系统初始化", "3"));

        DefaultMutableTreeNode root = RegisterTreeNodeHelper.solve(registerTreeNode);
        JTree tree = new JTree(root);

        tree.setRootVisible(false);//不显示树的根节点
        tree.setRowHeight(20);//树节点的行高为20像素
        tree.setFont(new Font("宋体", Font.BOLD, 14));//设置树结点的字体

        //节点间不采用连接线
        tree.putClientProperty("JTree.lineStyle", "None");
        DefaultTreeCellRenderer treeCellRenderer;// 获得树节点的绘制对象
        treeCellRenderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        treeCellRenderer.setLeafIcon(null);// 设置叶子节点不采用图标
        treeCellRenderer.setClosedIcon(null);// 设置节点折叠时不采用图标
        treeCellRenderer.setOpenIcon(null);// 设置节点展开时不采用图标
        Enumeration<?> enumeration; // 按前序遍历所有树节点
        enumeration = root.preorderEnumeration();

        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode) enumeration.nextElement();
            if (!node.isLeaf()) {// 判断是否为叶子节点
                // 创建该节点的路径
                TreePath path = new TreePath(node.getPath());
                tree.expandPath(path);// 如果不是则展开该节点
            }
        }
        treePointed = tree;
    }

    private void initJSplitPane() {
//        mainSplitPane.setOneTouchExpandable(true);//让分割线显示出箭头
        mainSplitPane.setContinuousLayout(true);//操作箭头，重绘图形
        //jSplitPane.setPreferredSize(new Dimension (100,200));
        mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);//设置分割线方向
        mainSplitPane.setDividerSize(2);//设置分割线的宽度
        //jSplitPane.setDividerLocation(100);//设置分割线位于中央
        mainSplitPane.setDividerLocation(200);//设定分割线的距离左边的位置
    }
}
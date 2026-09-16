package com.zifang.util.core.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sort类。
 */
public class Sort {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        List<Node> nodeList = getDataList();
        new Sort().print(nodeList);

    }

    private static List<Node> getDataList() {
        List<Node> nodes = new ArrayList<>();

        nodes.add(new Node(5, 3, "EE"));
        nodes.add(new Node(8, 4, "HH"));
        nodes.add(new Node(9, 5, "II"));
        nodes.add(new Node(1, 0, "AA"));
        nodes.add(new Node(2, 1, "BB"));
        nodes.add(new Node(6, 2, "FF"));
        nodes.add(new Node(7, 2, "GG"));
        nodes.add(new Node(3, 1, "CC"));
        nodes.add(new Node(4, 3, "DD"));
        return nodes;
    }

    /**
     * print方法。
     * * @param list ListNode类型参数
     */
    public void print(List<Node> list) {
        Node root = list.stream().filter(e -> 0 == e.getParentId()).findFirst().get(); // 得到根节点
        // 根据节点进行打印
        print(list, root, "");
    }

    private void print(List<Node> list, Node root, String padding) {
        System.out.println(padding + root.getName());

        // 得到这个节点的子节点
        List<Node> subNodes = list.stream().filter(e -> root.getId() == e.getParentId()).collect(Collectors.toList());

        for (Node node : subNodes) {
            print(list, node, padding + " ");
        }
    }
}

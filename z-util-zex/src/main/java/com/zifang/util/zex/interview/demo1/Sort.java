package com.zifang.util.zex.interview.demo1;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 树形结构排序类。
 * <p>
 * 此类提供树形结构的遍历和打印功能。
 * 根据父子关系构建树形结构，并按照层级顺序打印节点。
 *
 * @author zifang
 * @version 1.0
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
     * 打印树形结构。
     *
     * @param list 节点列表
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
        if (subNodes.size() != 0) {
            for (Node node : subNodes) {
                print(list, node, padding + " ");
            }
        }
    }
}

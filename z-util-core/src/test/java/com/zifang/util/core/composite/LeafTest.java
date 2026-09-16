package com.zifang.util.core.composite;

import com.zifang.util.core.pattern.composite.tree.ILeaf;
import com.zifang.util.core.pattern.composite.tree.LeafHelper;
import com.zifang.util.core.pattern.composite.tree.LeafWrapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LeafTest类。
 */
public class LeafTest {

    @Test
    /**
     * test方法。
     */
    public void test() {

        List<Node> nodes = getDataList(); // 获得结点列表

        List<LeafWrapper<Integer, Integer, Node>> leafWrappers = nodes.stream()
                .map(e -> LeafHelper.wrapper(e.getId(), e.getParentId(), e))
                .collect(Collectors.toList()); // 转换结点

        ILeaf tree = LeafHelper.treeify(leafWrappers); // 生成树 获得根结点

        bfsVisit(tree, ""); // 广度优先

    }

    private void bfsVisit(ILeaf root, String str) {

        System.out.println(str + root.getName());

        List<ILeaf> iLeaves = root.getSubLeaves();

        if (iLeaves != null) {
            for (ILeaf iLeaf : iLeaves) {
                bfsVisit(iLeaf, str + " ");
            }
        }
    }

    private List<Node> getDataList() {
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


}

package com.zifang.util.core.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * TreeUtil工具类的单元测试
 */
public class TreeUtilTest {

    /**
     * 默认字段名的节点（id/parent/children）
     */
    private static class Node {
        private Long id;
        private Long parent;
        private List<Node> children;

        Node(Long id, Long parent) {
            this.id = id;
            this.parent = parent;
        }
    }

    /**
     * 自定义字段名在父类上的节点（menuId/subMenus 在基类）
     */
    private static class BaseMenu {
        protected Long menuId;
        protected Long menuPid;
        protected List<Menu> subMenus;
    }

    private static class Menu extends BaseMenu {
        private String name;
    }

    /**
     * 函数式风格节点（不约定字段）
     */
    private static class Region {
        private String code;
        private String parentCode;
        private List<Region> subs;

        Region(String code, String parentCode) {
            this.code = code;
            this.parentCode = parentCode;
        }
    }

    /**
     * testToTree方法：默认字段名组装三层树。
     */
    @Test
    public void testToTree() {
        Node root = new Node(1L, 0L);
        Node child1 = new Node(2L, 1L);
        Node child2 = new Node(3L, 1L);
        Node grandChild = new Node(4L, 2L);
        List<Node> tree = TreeUtil.toTree(Arrays.asList(grandChild, child2, root, child1), Node.class);

        assertEquals(1, tree.size());
        assertEquals(Long.valueOf(1L), tree.get(0).id);
        assertEquals(2, tree.get(0).children.size());
        // 子节点保持原集合出现顺序：child2 在前，child1 在后
        assertEquals(Long.valueOf(3L), tree.get(0).children.get(0).id);
        assertNull(tree.get(0).children.get(0).children);
        assertEquals(Long.valueOf(4L), tree.get(0).children.get(1).children.get(0).id);
    }

    /**
     * testToTreeWithNullParent方法：父节点为 null 的节点是根节点。
     */
    @Test
    public void testToTreeWithNullParent() {
        Node root = new Node(1L, null);
        Node child = new Node(2L, 1L);
        List<Node> tree = TreeUtil.toTree(Arrays.asList(root, child), Node.class);

        assertEquals(1, tree.size());
        assertEquals(Long.valueOf(2L), tree.get(0).children.get(0).id);
    }

    /**
     * testToTreeWithCustomFields方法：自定义字段名且字段位于父类时正常组装。
     */
    @Test
    public void testToTreeWithCustomFields() {
        Menu root = new Menu();
        root.menuId = 1L;
        root.menuPid = 0L;
        root.name = "root";
        Menu child = new Menu();
        child.menuId = 2L;
        child.menuPid = 1L;
        child.name = "child";

        List<Menu> tree = TreeUtil.toTree(Arrays.asList(child, root), "menuId", "menuPid", "subMenus", Menu.class);

        assertEquals(1, tree.size());
        assertEquals("root", tree.get(0).name);
        assertEquals(1, tree.get(0).subMenus.size());
        assertEquals("child", tree.get(0).subMenus.get(0).name);
    }

    /**
     * testToTreeWithEmptyInput方法：null 或空集合返回空列表而非 null。
     */
    @Test
    public void testToTreeWithEmptyInput() {
        assertEquals(0, TreeUtil.toTree(null, Node.class).size());
        assertEquals(0, TreeUtil.toTree(new ArrayList<Node>(), Node.class).size());
    }

    /**
     * testToTreeDoesNotModifyInput方法：组装过程不修改传入的集合。
     */
    @Test
    public void testToTreeDoesNotModifyInput() {
        Node root = new Node(1L, 0L);
        Node child = new Node(2L, 1L);
        List<Node> input = new ArrayList<>(Arrays.asList(root, child));
        TreeUtil.toTree(input, Node.class);

        assertEquals(2, input.size());
    }

    /**
     * testToTreeWithMissingField方法：字段不存在时抛出 IllegalArgumentException。
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToTreeWithMissingField() {
        TreeUtil.toTree(Arrays.asList(new Node(1L, 0L)), "noSuchId", "parent", "children", Node.class);
    }

    /**
     * testToTreeWithCycle方法：互为父子的环节点成为不可达的孤儿，不影响其余节点组装且不死循环。
     */
    @Test
    public void testToTreeWithCycle() {
        Node root = new Node(1L, 0L);
        Node child = new Node(2L, 1L);
        Node a = new Node(3L, 4L);
        Node b = new Node(4L, 3L);
        List<Node> tree = TreeUtil.toTree(Arrays.asList(a, b, root, child), Node.class);

        assertEquals(1, tree.size());
        assertEquals(1, tree.get(0).children.size());
        assertEquals(Long.valueOf(2L), tree.get(0).children.get(0).id);
        assertNull(tree.get(0).children.get(0).children);
    }

    /**
     * testAssemblyTree方法：函数式风格组装树。
     */
    @Test
    public void testAssemblyTree() {
        Region root = new Region("86", null);
        Region province = new Region("530000", "86");
        Region city = new Region("530400", "530000");
        Region orphan = new Region("999999", "888888");
        List<Region> tree = TreeUtil.assemblyTree(
                Arrays.asList(city, orphan, root, province),
                r -> r.code,
                r -> r.parentCode,
                (r, subs) -> {
                    if (subs != null) {
                        r.subs = subs;
                    }
                },
                null);

        assertEquals(1, tree.size());
        assertEquals("86", tree.get(0).code);
        assertEquals(1, tree.get(0).subs.size());
        assertEquals("530000", tree.get(0).subs.get(0).code);
        assertEquals("530400", tree.get(0).subs.get(0).subs.get(0).code);
        // 孤儿节点不会出现在树中
    }

    /**
     * testAssemblyTreeWithCycle方法：根值命中环时递归在回到已处理节点处截断，不死循环。
     */
    @Test
    public void testAssemblyTreeWithCycle() {
        Region a = new Region("1", "2");
        Region b = new Region("2", "1");
        List<Region> tree = TreeUtil.assemblyTree(
                Arrays.asList(a, b),
                r -> r.code,
                r -> r.parentCode,
                (r, subs) -> {
                    if (subs != null) {
                        r.subs = subs;
                    }
                },
                "1");

        assertEquals(1, tree.size());
        assertEquals("2", tree.get(0).code);
        assertEquals(1, tree.get(0).subs.size());
        assertEquals("1", tree.get(0).subs.get(0).code);
    }

    /**
     * testAssemblyTreeWithNoRoot方法：无根节点时返回空列表。
     */
    @Test
    public void testAssemblyTreeWithNoRoot() {
        Region a = new Region("1", "2");
        Region b = new Region("2", "1");
        List<Region> tree = TreeUtil.assemblyTree(
                Arrays.asList(a, b),
                r -> r.code,
                r -> r.parentCode,
                (r, subs) -> r.subs = subs,
                null);

        assertTrue(tree.isEmpty());
    }

    /**
     * testAssemblyTreeWithEmptyInput方法：null 或空列表返回空列表。
     */
    @Test
    public void testAssemblyTreeWithEmptyInput() {
        assertTrue(TreeUtil.assemblyTree(null, r -> r, r -> r, (r, subs) -> {
        }, null).isEmpty());
        assertTrue(TreeUtil.assemblyTree(new ArrayList<Region>(), r -> r.code, r -> r.parentCode,
                (r, subs) -> {
                }, null).isEmpty());
    }

    private static final Function<Region, List<Region>> SUBS = r -> r.subs;

    /**
     * testFlattenPreOrder方法：深度优先前序摊平，子节点按声明顺序展开。
     */
    @Test
    public void testFlattenPreOrder() {
        Region east = new Region("EAST", null);
        Region js = new Region("JS", "EAST");
        Region az = new Region("AZ", "EAST");
        Region nj = new Region("NJ", "JS");
        east.subs = Arrays.asList(js, az);
        js.subs = Collections.singletonList(nj);

        List<String> codes = new ArrayList<>();
        for (Region node : TreeUtil.flatten(Collections.singletonList(east), SUBS)) {
            codes.add(node.code);
        }
        assertEquals(Arrays.asList("EAST", "JS", "NJ", "AZ"), codes);
    }

    /**
     * testFlattenMultipleRootsKeepsOrder方法：多根按传入顺序展开，且不修改入参。
     */
    @Test
    public void testFlattenMultipleRootsKeepsOrder() {
        Region a = new Region("A", null);
        Region b = new Region("B", null);
        Region a1 = new Region("A1", "A");
        a.subs = Collections.singletonList(a1);
        List<Region> roots = Arrays.asList(a, b);

        List<Region> flat = TreeUtil.flatten(roots, SUBS);
        assertEquals(3, flat.size());
        assertEquals("A", flat.get(0).code);
        assertEquals("A1", flat.get(1).code);
        assertEquals("B", flat.get(2).code);
        assertEquals(2, roots.size());
    }

    /**
     * testCollectLeaves方法：叶子定义为 childrenGetter 返回 null 或空集合。
     */
    @Test
    public void testCollectLeaves() {
        Region east = new Region("EAST", null);
        Region js = new Region("JS", "EAST");
        Region az = new Region("AZ", "EAST");
        Region nj = new Region("NJ", "JS");
        Region emptyBranch = new Region("EMPTY", "JS");
        east.subs = Arrays.asList(js, az);
        js.subs = Arrays.asList(nj, emptyBranch);
        nj.subs = new ArrayList<>();

        List<String> leaves = new ArrayList<>();
        for (Region leaf : TreeUtil.collectLeaves(Collections.singletonList(east), SUBS)) {
            leaves.add(leaf.code);
        }
        assertEquals(Arrays.asList("NJ", "EMPTY", "AZ"), leaves);
    }

    /**
     * testFlattenHandlesCycle方法：自环与互环只访问一次，不死循环。
     */
    @Test
    public void testFlattenHandlesCycle() {
        Region self = new Region("SELF", null);
        self.subs = Collections.singletonList(self);
        assertEquals(1, TreeUtil.flatten(Collections.singletonList(self), SUBS).size());

        Region x = new Region("X", null);
        Region y = new Region("Y", "X");
        x.subs = Collections.singletonList(y);
        y.subs = Collections.singletonList(x);
        List<Region> flat = TreeUtil.flatten(Collections.singletonList(x), SUBS);
        assertEquals(2, flat.size());
        assertEquals("X", flat.get(0).code);
        assertEquals("Y", flat.get(1).code);
    }

    /**
     * testFlattenSharedNodeVisitedOnce方法：菱形引用中共享节点只输出一次。
     */
    @Test
    public void testFlattenSharedNodeVisitedOnce() {
        Region root = new Region("ROOT", null);
        Region left = new Region("L", "ROOT");
        Region right = new Region("R", "ROOT");
        Region shared = new Region("S", "L");
        root.subs = Arrays.asList(left, right);
        left.subs = Collections.singletonList(shared);
        right.subs = Collections.singletonList(shared);
        assertEquals(4, TreeUtil.flatten(Collections.singletonList(root), SUBS).size());
    }

    /**
     * testFlattenNullAndEmpty方法：null 根集合、null childrenGetter、null 子元素都不抛异常。
     */
    @Test
    public void testFlattenNullAndEmpty() {
        assertTrue(TreeUtil.flatten(null, SUBS).isEmpty());
        assertTrue(TreeUtil.flatten(new ArrayList<Region>(), SUBS).isEmpty());
        assertTrue(TreeUtil.flatten(Collections.singletonList(new Region("N", null)), null).isEmpty());
        assertTrue(TreeUtil.collectLeaves(null, SUBS).isEmpty());
        Region root = new Region("ROOT", null);
        root.subs = Arrays.asList(new Region("C", "ROOT"), null);
        assertEquals(2, TreeUtil.flatten(Collections.singletonList(root), SUBS).size());
    }
}

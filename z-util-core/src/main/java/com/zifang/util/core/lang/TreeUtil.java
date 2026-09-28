package com.zifang.util.core.lang;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 树结构构建工具类。
 * <p>
 * 提供扁平集合与树形结构之间的转换，支持两种风格：
 * <ul>
 * <li>反射风格 {@link #toTree(Collection, String, String, String, Class)}：
 * 按字段名称定位 id、父节点、子节点集合字段，适用于无法调整结构的普通 POJO；</li>
 * <li>函数式风格 {@link #assemblyTree(List, Function, Function, BiConsumer, Object)}：
 * 通过函数获取 id 与父节点值、由回调挂载子节点集合，无需反射。</li>
 * </ul>
 * 父节点值为 null、空字符串或数值 0 的节点视为根节点；返回的根节点列表保持原集合中的出现顺序。
 * 本工具不修改传入的集合；环状引用数据会按环防护截断，不会产生死循环。
 */
public class TreeUtil {

    /**
     * 默认 id 字段名
     */
    private static final String DEFAULT_ID_FIELD = "id";

    /**
     * 默认父节点字段名
     */
    private static final String DEFAULT_PARENT_FIELD = "parent";

    /**
     * 默认子节点集合字段名
     */
    private static final String DEFAULT_CHILDREN_FIELD = "children";

    private TreeUtil() {
    }

    /**
     * toTree方法。
     * 将扁平集合组装为树结构，id/parent/children 使用默认字段名。
     *
     * @param collection Collection类型参数，待组装的节点集合
     * @param clazz      Class类型参数，节点类型
     * @param <T>        节点类型
     * @return static List类型返回值，根节点列表（含各级子节点已挂载）；入参为空时返回空列表
     */
    public static <T> List<T> toTree(Collection<T> collection, Class<T> clazz) {
        return toTree(collection, null, null, null, clazz);
    }

    /**
     * toTree方法。
     * 将扁平集合组装为树结构，字段名可配置；字段不存在时沿继承链向父类查找。
     *
     * @param collection    Collection类型参数，待组装的节点集合
     * @param idField       String类型参数，id 字段名，为空时默认 "id"
     * @param parentField   String类型参数，父节点字段名，为空时默认 "parent"
     * @param childrenField String类型参数，子节点集合字段名，为空时默认 "children"
     * @param clazz         Class类型参数，节点类型
     * @param <T>           节点类型
     * @return static List类型返回值，根节点列表（含各级子节点已挂载）；入参为空时返回空列表
     * @throws IllegalArgumentException 字段在继承链上不存在或子节点字段非集合类型时抛出
     */
    public static <T> List<T> toTree(Collection<T> collection, String idField, String parentField,
                                     String childrenField, Class<T> clazz) {
        if (collection == null || collection.isEmpty()) {
            return new ArrayList<>();
        }
        if (clazz == null) {
            throw new IllegalArgumentException("clazz must not be null");
        }
        String idName = isEmpty(idField) ? DEFAULT_ID_FIELD : idField;
        String parentName = isEmpty(parentField) ? DEFAULT_PARENT_FIELD : parentField;
        String childrenName = isEmpty(childrenField) ? DEFAULT_CHILDREN_FIELD : childrenField;

        Field idF = findField(clazz, idName);
        Field parentF = findField(clazz, parentName);
        Field childrenF = findField(clazz, childrenName);
        idF.setAccessible(true);
        parentF.setAccessible(true);
        childrenF.setAccessible(true);

        List<T> nodes = new ArrayList<>(collection);
        List<T> roots = new ArrayList<>();
        for (T node : nodes) {
            if (isRootParentValue(getFieldValue(parentF, node))) {
                roots.add(node);
            }
        }
        List<T> candidates = new ArrayList<>(nodes);
        candidates.removeAll(roots);
        Set<Object> visitedIds = new HashSet<>();
        for (T root : roots) {
            fillChildren(root, candidates, idF, parentF, childrenF, visitedIds);
        }
        return roots;
    }

    /**
     * assemblyTree方法。
     * 将扁平列表组装为树结构（函数式风格）。
     * 按父节点值分组后，从根节点值对应的节点开始递归，通过 childrenSetter 回调挂载子节点列表
     * （子节点列表可能为 null，由回调自行决定如何处理）。
     *
     * @param list           List类型参数，待组装的节点列表
     * @param idGetter       Function类型参数，获取节点 id 的函数
     * @param parentGetter   Function类型参数，获取节点父节点值的函数
     * @param childrenSetter BiConsumer类型参数，挂载子节点列表的回调
     * @param rootParentValue Object类型参数，根节点的父节点值（通常为 null 或 0）
     * @param <T>            节点类型
     * @param <V>            节点 id/父节点值类型
     * @return static List类型返回值，根节点列表；入参为空或无根节点时返回空列表
     */
    public static <T, V> List<T> assemblyTree(List<T> list, Function<? super T, ? extends V> idGetter,
                                              Function<? super T, ? extends V> parentGetter,
                                              BiConsumer<T, List<T>> childrenSetter, V rootParentValue) {
        List<T> result = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return result;
        }
        Map<V, List<T>> parentMap = new HashMap<>();
        for (T item : list) {
            parentMap.computeIfAbsent(parentGetter.apply(item), key -> new ArrayList<>()).add(item);
        }
        List<T> roots = parentMap.get(rootParentValue);
        if (roots == null || roots.isEmpty()) {
            return result;
        }
        Set<V> visitedIds = new HashSet<>();
        for (T root : roots) {
            fillChildrenFunctional(root, parentMap, idGetter, childrenSetter, visitedIds);
        }
        return new ArrayList<>(roots);
    }

    /**
     * flatten方法。
     * 深度优先（前序）摊平树为列表，父子关系由 childrenGetter 提供。
     * 使用显式栈遍历，深层树不会栈溢出；按节点引用去重，因此环状或菱形引用结构中
     * 每个节点只出现一次（重复到达的节点被跳过）。
     * roots 中的 null 元素、childrenGetter 返回 null 或含 null 的子集合都会被跳过。
     *
     * @param roots         根节点集合，为 null 或空时返回空列表
     * @param childrenGetter Function类型参数，获取某节点的直接子节点集合
     * @param <T>           节点类型
     * @return static List类型返回值，前序遍历结果的新列表
     */
    public static <T> List<T> flatten(Collection<T> roots, Function<? super T, ? extends Collection<T>> childrenGetter) {
        List<T> result = new ArrayList<>();
        if (roots == null || roots.isEmpty() || childrenGetter == null) {
            return result;
        }
        Set<T> visited = Collections.newSetFromMap(new IdentityHashMap<T, Boolean>());
        Deque<T> stack = new ArrayDeque<>();
        List<T> rootList = new ArrayList<>(roots);
        for (int i = rootList.size() - 1; i >= 0; i--) {
            T root = rootList.get(i);
            if (root != null) {
                stack.push(root);
            }
        }
        while (!stack.isEmpty()) {
            T node = stack.pop();
            if (!visited.add(node)) {
                continue;
            }
            result.add(node);
            List<T> children = toChildList(childrenGetter.apply(node));
            for (int i = children.size() - 1; i >= 0; i--) {
                T child = children.get(i);
                if (child != null) {
                    stack.push(child);
                }
            }
        }
        return result;
    }

    /**
     * collectLeaves方法。
     * 收集树中的叶子节点（childrenGetter 返回 null 或空集合的节点），保持前序遍历顺序。
     * 去重与环防护语义与 {@link #flatten(Collection, Function)} 一致。
     *
     * @param roots         根节点集合，为 null 或空时返回空列表
     * @param childrenGetter Function类型参数，获取某节点的直接子节点集合
     * @param <T>           节点类型
     * @return static List类型返回值，叶子节点组成的新列表
     */
    public static <T> List<T> collectLeaves(Collection<T> roots, Function<? super T, ? extends Collection<T>> childrenGetter) {
        List<T> leaves = new ArrayList<>();
        for (T node : flatten(roots, childrenGetter)) {
            Collection<T> children = childrenGetter.apply(node);
            if (children == null || children.isEmpty()) {
                leaves.add(node);
            }
        }
        return leaves;
    }

    /**
     * toChildList方法。
     * 将子节点集合安全转为列表，null 与空集合统一返回空列表。
     */
    private static <T> List<T> toChildList(Collection<T> children) {
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        return children instanceof List ? (List<T>) children : new ArrayList<>(children);
    }

    /**
     * fillChildren方法。
     * 递归为目标节点挂载直接子节点；子节点集合为 null 时初始化为 ArrayList 并写回字段。
     */
    @SuppressWarnings("unchecked")
    private static <T> void fillChildren(T node, Collection<T> candidates, Field idField, Field parentField,
                                         Field childrenField, Set<Object> visitedIds) {
        Object id = getFieldValue(idField, node);
        if (!visitedIds.add(id)) {
            return;
        }
        Collection<T> children = null;
        for (T candidate : candidates) {
            if (Objects.equals(id, getFieldValue(parentField, candidate))) {
                if (children == null) {
                    children = (Collection<T>) getFieldValue(childrenField, node);
                    if (children == null) {
                        children = new ArrayList<>();
                    }
                }
                children.add(candidate);
                fillChildren(candidate, candidates, idField, parentField, childrenField, visitedIds);
            }
        }
        if (children != null && getFieldValue(childrenField, node) == null) {
            setFieldValue(childrenField, node, children);
        }
    }

    /**
     * fillChildrenFunctional方法。
     * 递归为目标节点挂载直接子节点（函数式风格）。
     */
    private static <T, V> void fillChildrenFunctional(T node, Map<V, List<T>> parentMap,
                                                      Function<? super T, ? extends V> idGetter,
                                                      BiConsumer<T, List<T>> childrenSetter, Set<V> visitedIds) {
        V id = idGetter.apply(node);
        if (!visitedIds.add(id)) {
            return;
        }
        List<T> children = parentMap.get(id);
        childrenSetter.accept(node, children);
        if (children == null || children.isEmpty()) {
            return;
        }
        for (T child : children) {
            fillChildrenFunctional(child, parentMap, idGetter, childrenSetter, visitedIds);
        }
    }

    /**
     * isRootParentValue方法。
     * 判断父节点值是否表示根节点：null、空字符串或数值 0。
     */
    private static boolean isRootParentValue(Object parentValue) {
        if (parentValue == null) {
            return true;
        }
        if (parentValue instanceof String) {
            return ((String) parentValue).isEmpty();
        }
        if (parentValue instanceof Number) {
            return ((Number) parentValue).longValue() == 0L;
        }
        return false;
    }

    /**
     * findField方法。
     * 沿继承链查找字段（含父类，不含 Object）。
     */
    private static Field findField(Class<?> clazz, String name) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalArgumentException(
                "field not found: " + name + " in class " + clazz.getName());
    }

    private static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setFieldValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }
}

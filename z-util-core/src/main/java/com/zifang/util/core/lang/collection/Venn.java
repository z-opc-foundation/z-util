package com.zifang.util.core.lang.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 描述两个集合之间的内容关系
 */
public class Venn<E> {

    private final Collection<E> c1;
    private final Collection<E> c2;

    /**
     * Venn方法。
     * * @param c1 CollectionE类型参数
     *
     * @param c2 CollectionE类型参数
     */
    public Venn(Collection<E> c1, Collection<E> c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    /**
     * 并集 c1 U c2
     */
    public Collection<E> union() {
        Collection<E> c = new ArrayList<>();

        c.addAll(c1.stream().distinct().collect(Collectors.toList()));
        c.addAll(c2.stream().distinct().collect(Collectors.toList()));

        return c.stream().distinct().collect(Collectors.toList());
    }

    /**
     * unionCount方法。
     *
     * @return int类型返回值
     */
    public Integer unionCount() {
        return union().size();
    }

    /**
     * 交集 c1 n c2
     */
    public Collection<E> intersection() {
        Collection<E> all = union();
        Collection<E> r = new ArrayList<>();
        for (E e : all) {
            if (c1.contains(e) && c2.contains(e)) {
                r.add(e);
            }
        }
        return r;
    }

    /**
     * 交集 c1 n ( c1 n c2 )
     */
    public Collection<E> intersectionLeft() {
        Collection<E> dc1 = c1.stream().distinct().collect(Collectors.toList());
        dc1.removeAll(intersection());
        return dc1;
    }

    /**
     * 交集 c2 n ( c1 n c2 )
     */
    public Collection<E> intersectionRight() {
        Collection<E> dc2 = c2.stream().distinct().collect(Collectors.toList());
        dc2.removeAll(intersection());
        return dc2;
    }

    /**
     * 交集 c1 n c2 后的集合大小
     */
    public Integer intersectionCount() {
        return intersection().size();
    }

    /**
     * dicript方法。
     */
    public void dicript() {
//        long left = 1;
//        long
    }
}

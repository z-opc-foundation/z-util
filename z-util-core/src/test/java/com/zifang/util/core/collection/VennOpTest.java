package com.zifang.util.core.collection;

import com.zifang.util.core.lang.collection.Venn;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * VennOpTest类。
 */
public class VennOpTest {
    @Test
    /**
     * test0方法。
     */
    public void test0() {
        List<String> l1 = Arrays.asList("0", "1", "2", "3", "3");
        List<String> l2 = Arrays.asList("2", "3", "4", "4");

        Venn<String> venn = new Venn<>(l1, l2);

        assert venn.unionCount() == 5;
        assert venn.union().size() == 5;
        assert venn.intersection().size() == 2;
        assert venn.intersectionCount() == 2;
        assert venn.intersectionLeft().size() == 2;
        assert venn.intersectionRight().size() == 1;
    }
}

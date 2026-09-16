package com.zifang.util.core.lang.beans.tuples;


import com.zifang.util.core.lang.tuples.Pair;
import org.junit.Test;

/**
 * UnitTest类。
 */
public class UnitTest {

    @Test
    /**
     * toMap方法。
     */
    public void toMap() {
        Pair pair = new Pair<>("a", "b");
        System.out.println(pair.toMap());
    }
}
package com.zifang.util.ml.association;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FPGrowthTest类。
 */
public class FPGrowthTest {

    @Test
    /**
     * testFPGrowthEmptyTransactions方法。
     */
    public void testFPGrowthEmptyTransactions() {
        FPGrowth fpGrowth = new FPGrowth(0.5, 1);

        List<int[]> transactions = new ArrayList<>();
        List<Set<Integer>> result = fpGrowth.findFrequentItemsets(transactions);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testFPGrowthInstantiation方法。
     */
    public void testFPGrowthInstantiation() {
        // Just test that FPGrowth can be instantiated with various parameters
        FPGrowth fpGrowth = new FPGrowth(0.3, 2);
        assertNotNull(fpGrowth);

        FPGrowth fpGrowth2 = new FPGrowth(0.1, 5);
        assertNotNull(fpGrowth2);
    }
}

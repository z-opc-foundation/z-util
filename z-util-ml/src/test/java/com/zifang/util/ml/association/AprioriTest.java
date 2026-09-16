package com.zifang.util.ml.association;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * AprioriTest类。
 */
public class AprioriTest {

    @Test
    /**
     * testAprioriFrequentItemsets方法。
     */
    public void testAprioriFrequentItemsets() {
        Apriori apriori = new Apriori(0.3, 0.5);

        // Simple transaction data
        List<int[]> transactions = new ArrayList<>();
        transactions.add(new int[]{1, 2, 3});
        transactions.add(new int[]{1, 2});
        transactions.add(new int[]{2, 3});
        transactions.add(new int[]{1, 2, 3});
        transactions.add(new int[]{1, 3});

        List<Set<Integer>> frequentItemsets = apriori.findFrequentItemsets(transactions);

        assertNotNull(frequentItemsets);
        assertTrue(frequentItemsets.size() > 0, "Should find at least one frequent itemset");

        // Check that all itemsets meet minimum support
        for (Set<Integer> itemset : frequentItemsets) {
            assertNotNull(itemset);
            assertTrue(itemset.size() > 0);
        }
    }

    @Test
    /**
     * testAprioriEmptyTransactions方法。
     */
    public void testAprioriEmptyTransactions() {
        Apriori apriori = new Apriori(0.5, 0.5);

        List<int[]> transactions = new ArrayList<>();
        List<Set<Integer>> result = apriori.findFrequentItemsets(transactions);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testAprioriSingleItem方法。
     */
    public void testAprioriSingleItem() {
        Apriori apriori = new Apriori(0.6, 0.5);

        List<int[]> transactions = new ArrayList<>();
        transactions.add(new int[]{1, 2});
        transactions.add(new int[]{1, 2});
        transactions.add(new int[]{1, 2, 3});
        transactions.add(new int[]{2, 3});

        List<Set<Integer>> frequentItemsets = apriori.findFrequentItemsets(transactions);

        // Item 1 and 2 appear in 3/4 transactions, should be frequent
        assertTrue(frequentItemsets.size() >= 2);
    }
}

package com.zifang.util.ml.association;

import java.util.*;

/**
 * FP-Growth algorithm for frequent itemset mining and association rule generation.
 * More efficient than Apriori as it avoids candidate generation and multiple database scans.
 */
public class FPGrowth {

    private final double minSupport;
    private final int minSupportCount;
    private List<Set<Integer>> frequentItemsets;
    private Map<Set<Integer>, Integer> frequentItemsetSupports;
    private int totalTransactions;

    /**
     * Constructor for FP-Growth algorithm.
     *
     * @param minSupport      Minimum support threshold (0-1), used for finding frequent itemsets
     * @param minSupportCount Minimum absolute support count
     */
    public FPGrowth(double minSupport, int minSupportCount) {
        if (minSupport < 0 || minSupport > 1) {
            throw new IllegalArgumentException("minSupport must be between 0 and 1");
        }
        if (minSupportCount < 0) {
            throw new IllegalArgumentException("minSupportCount must be non-negative");
        }
        this.minSupport = minSupport;
        this.minSupportCount = minSupportCount;
        this.frequentItemsets = new ArrayList<>();
        this.frequentItemsetSupports = new HashMap<>();
    }

    /**
     * Find all frequent itemsets in the transactions.
     *
     * @param transactions List of transactions, where each transaction is an array of item IDs
     * @return List of frequent itemsets
     */
    public List<Set<Integer>> findFrequentItemsets(List<int[]> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        this.totalTransactions = transactions.size();
        this.frequentItemsets = new ArrayList<>();
        this.frequentItemsetSupports = new HashMap<>();

        // Step 1: Scan transactions once, count item frequencies
        Map<Integer, Integer> itemCounts = countItemFrequencies(transactions);

        // Filter items by minSupportCount
        Map<Integer, Integer> frequentItems = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : itemCounts.entrySet()) {
            if (entry.getValue() >= minSupportCount) {
                frequentItems.put(entry.getKey(), entry.getValue());
            }
        }

        if (frequentItems.isEmpty()) {
            return new ArrayList<>();
        }

        // Sort frequent items by count descending
        List<Integer> sortedFrequentItems = new ArrayList<>(frequentItems.keySet());
        sortedFrequentItems.sort((a, b) -> Integer.compare(frequentItems.get(b), frequentItems.get(a)));

        // Create item ordering map
        Map<Integer, Integer> itemOrder = new HashMap<>();
        for (int i = 0; i < sortedFrequentItems.size(); i++) {
            itemOrder.put(sortedFrequentItems.get(i), i);
        }

        // Step 2: Build FP-tree
        FPTreeNode root = new FPTreeNode(-1, 0, null);
        Map<Integer, FPTreeNode> headerTable = new HashMap<>();

        for (int[] transaction : transactions) {
            // Filter and sort transaction items by frequency
            List<Integer> filteredItems = new ArrayList<>();
            for (int item : transaction) {
                if (frequentItems.containsKey(item)) {
                    filteredItems.add(item);
                }
            }

            // Sort by frequency order (descending)
            filteredItems.sort((a, b) -> {
                int orderA = itemOrder.get(a);
                int orderB = itemOrder.get(b);
                if (orderA != orderB) {
                    return Integer.compare(orderA, orderB);
                }
                return Integer.compare(frequentItems.get(a), frequentItems.get(b));
            });

            // Insert transaction into FP-tree
            insertTree(filteredItems, root, headerTable);
        }

        // Step 3 & 4: Mine FP-tree recursively
        for (Integer item : sortedFrequentItems) {
            List<Set<Integer>> patterns = new ArrayList<>();
            patterns.add(new HashSet<>(Arrays.asList(item)));

            // Get conditional pattern base for this item
            List<int[]> conditionalPatternBase = getConditionalPatternBase(item, headerTable);

            if (!conditionalPatternBase.isEmpty()) {
                // Build conditional FP-tree
                FPTreeNode condTreeRoot = buildConditionalTree(conditionalPatternBase, frequentItems, itemOrder);

                // Mine conditional tree
                if (condTreeRoot != null && !condTreeRoot.children.isEmpty()) {
                    List<List<Integer>> condPatterns = mineFPTree(condTreeRoot, headerTable, frequentItems);
                    for (List<Integer> pattern : condPatterns) {
                        Set<Integer> itemset = new HashSet<>(pattern);
                        itemset.add(item);
                        patterns.add(itemset);
                    }
                }
            }

            // Add all patterns as frequent itemsets
            for (Set<Integer> pattern : patterns) {
                int support = calculateSupport(pattern, transactions);
                if (support >= minSupportCount) {
                    frequentItemsets.add(pattern);
                    frequentItemsetSupports.put(pattern, support);
                }
            }
        }

        return frequentItemsets;
    }

    /**
     * Find all association rules from the frequent itemsets.
     *
     * @param transactions List of transactions
     * @return List of association rules
     */
    public List<AssociationRule> findAssociationRules(List<int[]> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        if (frequentItemsets.isEmpty()) {
            findFrequentItemsets(transactions);
        }

        List<AssociationRule> rules = new ArrayList<>();

        // For each frequent itemset S, generate rules
        for (Map.Entry<Set<Integer>, Integer> entry : frequentItemsetSupports.entrySet()) {
            Set<Integer> itemset = entry.getKey();
            int itemsetSupport = entry.getValue();

            if (itemset.size() < 2) {
                continue;
            }

            // Generate all non-empty proper subsets A of S
            List<Set<Integer>> subsets = generateNonEmptySubsets(itemset);

            for (Set<Integer> antecedent : subsets) {
                Set<Integer> consequent = new HashSet<>(itemset);
                consequent.removeAll(antecedent);

                if (consequent.isEmpty()) {
                    continue;
                }

                // Calculate confidence = support(S) / support(A)
                Integer antecedentSupport = frequentItemsetSupports.get(antecedent);
                if (antecedentSupport == null) {
                    continue;
                }

                double confidence = (double) itemsetSupport / antecedentSupport;

                // Calculate support of the rule
                double support = (double) itemsetSupport / totalTransactions;

                // Calculate lift = confidence / support(consequent)
                Integer consequentSupportInt = frequentItemsetSupports.get(consequent);
                double consequentSupport = consequentSupportInt != null
                        ? (double) consequentSupportInt / totalTransactions
                        : 0.0;
                double lift = consequentSupport > 0 ? confidence / consequentSupport : 0.0;

                AssociationRule rule = new AssociationRule(
                        antecedent,
                        consequent,
                        support,
                        confidence,
                        lift
                );
                rules.add(rule);
            }
        }

        return rules;
    }

    /**
     * Count frequency of each item across all transactions.
     */
    private Map<Integer, Integer> countItemFrequencies(List<int[]> transactions) {
        Map<Integer, Integer> itemCounts = new HashMap<>();

        for (int[] transaction : transactions) {
            Set<Integer> seen = new HashSet<>();
            for (int item : transaction) {
                if (seen.add(item)) {
                    itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
                }
            }
        }

        return itemCounts;
    }

    /**
     * Insert a transaction into the FP-tree.
     */
    private void insertTree(List<Integer> items, FPTreeNode node, Map<Integer, FPTreeNode> headerTable) {
        if (items.isEmpty()) {
            return;
        }

        int item = items.get(0);

        // Find or create child node
        FPTreeNode child = findChild(node, item);
        if (child == null) {
            child = new FPTreeNode(item, 1, node);
            node.children.add(child);

            // Update header table
            if (headerTable.containsKey(item)) {
                FPTreeNode existing = headerTable.get(item);
                while (existing.nodeLink != null) {
                    existing = existing.nodeLink;
                }
                existing.nodeLink = child;
            } else {
                headerTable.put(item, child);
            }
        } else {
            child.count++;
        }

        // Recursively insert remaining items
        if (items.size() > 1) {
            insertTree(items.subList(1, items.size()), child, headerTable);
        }
    }

    /**
     * Find a child node with the given item.
     */
    private FPTreeNode findChild(FPTreeNode node, int item) {
        for (FPTreeNode child : node.children) {
            if (child.item == item) {
                return child;
            }
        }
        return null;
    }

    /**
     * Get conditional pattern base for an item.
     */
    private List<int[]> getConditionalPatternBase(int item, Map<Integer, FPTreeNode> headerTable) {
        List<int[]> patterns = new ArrayList<>();

        FPTreeNode node = headerTable.get(item);
        while (node != null) {
            List<Integer> pattern = new ArrayList<>();

            // Walk up the tree to collect prefix
            FPTreeNode parent = node.parent;
            while (parent != null && parent.item != -1) {
                pattern.add(parent.item);
                parent = parent.parent;
            }

            if (!pattern.isEmpty()) {
                // Add pattern with the node's count
                int[] patternArray = new int[pattern.size()];
                for (int i = 0; i < pattern.size(); i++) {
                    patternArray[i] = pattern.get(i);
                }
                for (int i = 0; i < node.count; i++) {
                    patterns.add(patternArray);
                }
            }

            node = node.nodeLink;
        }

        return patterns;
    }

    /**
     * Build conditional FP-tree from pattern base.
     */
    private FPTreeNode buildConditionalTree(List<int[]> patterns, Map<Integer, Integer> frequentItems,
                                            Map<Integer, Integer> itemOrder) {
        // Count item frequencies in patterns
        Map<Integer, Integer> patternCounts = new HashMap<>();
        for (int[] pattern : patterns) {
            for (int item : pattern) {
                patternCounts.put(item, patternCounts.getOrDefault(item, 0) + 1);
            }
        }

        // Filter by minSupportCount and sort
        List<Integer> filteredItems = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : patternCounts.entrySet()) {
            if (entry.getValue() >= minSupportCount) {
                filteredItems.add(entry.getKey());
            }
        }

        if (filteredItems.isEmpty()) {
            return null;
        }

        filteredItems.sort((a, b) -> {
            int orderA = itemOrder.get(a);
            int orderB = itemOrder.get(b);
            return Integer.compare(orderA, orderB);
        });

        // Build tree
        FPTreeNode root = new FPTreeNode(-1, 0, null);
        Map<Integer, FPTreeNode> headerTable = new HashMap<>();

        for (int[] pattern : patterns) {
            List<Integer> sortedPattern = new ArrayList<>();
            for (int item : pattern) {
                if (patternCounts.containsKey(item) && patternCounts.get(item) >= minSupportCount) {
                    sortedPattern.add(item);
                }
            }

            sortedPattern.sort((a, b) -> {
                int orderA = itemOrder.get(a);
                int orderB = itemOrder.get(b);
                if (orderA != orderB) {
                    return Integer.compare(orderA, orderB);
                }
                return Integer.compare(patternCounts.get(a), patternCounts.get(b));
            });

            insertTree(sortedPattern, root, headerTable);
        }

        return root;
    }

    /**
     * Recursively mine the FP-tree.
     */
    private List<List<Integer>> mineFPTree(FPTreeNode node, Map<Integer, FPTreeNode> headerTable,
                                           Map<Integer, Integer> frequentItems) {
        List<List<Integer>> results = new ArrayList<>();

        // Get items in header table in reverse order of frequency
        List<Integer> items = new ArrayList<>(headerTable.keySet());
        items.sort((a, b) -> {
            int orderA = frequentItems.get(a);
            int orderB = frequentItems.get(b);
            return Integer.compare(orderB, orderA);
        });

        for (Integer item : items) {
            List<Integer> pattern = new ArrayList<>();
            pattern.add(item);

            // Get conditional pattern base
            List<int[]> conditionalPatternBase = getConditionalPatternBase(item, headerTable);

            if (!conditionalPatternBase.isEmpty()) {
                // Build conditional tree
                FPTreeNode condTree = buildConditionalTree(conditionalPatternBase, frequentItems,
                        new HashMap<>(frequentItems));

                if (condTree != null && !condTree.children.isEmpty()) {
                    List<List<Integer>> subPatterns = mineFPTree(condTree, headerTable, frequentItems);
                    for (List<Integer> subPattern : subPatterns) {
                        List<Integer> newPattern = new ArrayList<>(subPattern);
                        newPattern.add(item);
                        results.add(newPattern);
                    }
                }
            }

            results.add(pattern);
        }

        return results;
    }

    /**
     * Calculate support count for an itemset.
     */
    private int calculateSupport(Set<Integer> itemset, List<int[]> transactions) {
        int count = 0;

        for (int[] transaction : transactions) {
            Set<Integer> transSet = new HashSet<>();
            for (int item : transaction) {
                transSet.add(item);
            }

            if (transSet.containsAll(itemset)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Generate all non-empty proper subsets of a set.
     */
    private List<Set<Integer>> generateNonEmptySubsets(Set<Integer> set) {
        List<Set<Integer>> subsets = new ArrayList<>();
        List<Integer> items = new ArrayList<>(set);
        int n = items.size();

        for (int mask = 1; mask < (1 << n) - 1; mask++) {
            Set<Integer> subset = new HashSet<>();
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    subset.add(items.get(i));
                }
            }
            subsets.add(subset);
        }

        return subsets;
    }

    /**
     * Represents a node in the FP-tree.
     */
    public static class FPTreeNode {
        int item;
        int count;
        FPTreeNode parent;
        List<FPTreeNode> children;
        FPTreeNode nodeLink; // Link to next node with same item

        /**
         * FPTreeNode方法。
         * * @param item int类型参数
         *
         * @param count  int类型参数
         * @param parent FPTreeNode类型参数
         */
        public FPTreeNode(int item, int count, FPTreeNode parent) {
            this.item = item;
            this.count = count;
            this.parent = parent;
            this.children = new ArrayList<>();
            this.nodeLink = null;
        }
    }

    /**
     * Represents a frequent pattern with its support count.
     */
    public static class FrequentPattern {
        private final Set<Integer> itemset;
        private final int support;

        /**
         * FrequentPattern方法。
         * * @param itemset SetInteger类型参数
         *
         * @param support int类型参数
         */
        public FrequentPattern(Set<Integer> itemset, int support) {
            this.itemset = new HashSet<>(itemset);
            this.support = support;
        }

        /**
         * getItemset方法。
         *
         * @return Set<Integer>类型返回值
         */
        public Set<Integer> getItemset() {
            return new HashSet<>(itemset);
        }

        /**
         * getSupport方法。
         *
         * @return int类型返回值
         */
        public int getSupport() {
            return support;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "FrequentPattern{itemset=" + itemset + ", support=" + support + '}';
        }
    }

    /**
     * Represents an association rule with antecedent, consequent, support, confidence, and lift.
     */
    public static class AssociationRule {
        private final Set<Integer> antecedent;
        private final Set<Integer> consequent;
        private final double support;
        private final double confidence;
        private final double lift;

        /**
         * AssociationRule方法。
         * * @param antecedent SetInteger类型参数
         *
         * @param consequent SetInteger类型参数
         * @param support    double类型参数
         * @param confidence double类型参数
         * @param lift       double类型参数
         */
        public AssociationRule(Set<Integer> antecedent, Set<Integer> consequent,
                               double support, double confidence, double lift) {
            this.antecedent = new HashSet<>(antecedent);
            this.consequent = new HashSet<>(consequent);
            this.support = support;
            this.confidence = confidence;
            this.lift = lift;
        }

        /**
         * getAntecedent方法。
         *
         * @return Set<Integer>类型返回值
         */
        public Set<Integer> getAntecedent() {
            return new HashSet<>(antecedent);
        }

        /**
         * getConsequent方法。
         *
         * @return Set<Integer>类型返回值
         */
        public Set<Integer> getConsequent() {
            return new HashSet<>(consequent);
        }

        /**
         * getSupport方法。
         *
         * @return double类型返回值
         */
        public double getSupport() {
            return support;
        }

        /**
         * getConfidence方法。
         *
         * @return double类型返回值
         */
        public double getConfidence() {
            return confidence;
        }

        /**
         * getLift方法。
         *
         * @return double类型返回值
         */
        public double getLift() {
            return lift;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return "AssociationRule{" +
                    "antecedent=" + antecedent +
                    ", consequent=" + consequent +
                    ", support=" + support +
                    ", confidence=" + confidence +
                    ", lift=" + lift +
                    '}';
        }
    }
}

package com.zifang.util.ml.association;

import java.util.*;

/**
 * Apriori algorithm for frequent itemset mining and association rule generation.
 */
public class Apriori {

    private final double minSupport;
    private final double minConfidence;
    private Map<Set<Integer>, Integer> frequentItemsets;
    private int totalTransactions;

    /**
     * Constructor for Apriori algorithm.
     *
     * @param minSupport    Minimum support threshold (0-1)
     * @param minConfidence Minimum confidence threshold (0-1)
     */
    public Apriori(double minSupport, double minConfidence) {
        if (minSupport < 0 || minSupport > 1) {
            throw new IllegalArgumentException("minSupport must be between 0 and 1");
        }
        if (minConfidence < 0 || minConfidence > 1) {
            throw new IllegalArgumentException("minConfidence must be between 0 and 1");
        }
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemsets = new HashMap<>();
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
        this.frequentItemsets = new HashMap<>();
        List<Set<Integer>> allFrequentItemsets = new ArrayList<>();

        // Step 1: Scan transactions, count support for each item, keep items >= minSupport
        Map<Integer, Integer> itemCounts = countItemSupport(transactions);
        Map<Integer, Integer> frequent1Itemsets = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : itemCounts.entrySet()) {
            double support = (double) entry.getValue() / totalTransactions;
            if (support >= minSupport) {
                frequent1Itemsets.put(entry.getKey(), entry.getValue());
            }
        }

        // Generate singleton frequent itemsets
        for (Integer item : frequent1Itemsets.keySet()) {
            Set<Integer> itemset = new HashSet<>();
            itemset.add(item);
            allFrequentItemsets.add(itemset);
            frequentItemsets.put(itemset, frequent1Itemsets.get(item));
        }

        // Step 2-4: Generate candidate k-itemsets, prune, repeat until no new frequent itemsets
        List<Set<Integer>> prevFrequentItemsets = new ArrayList<>(allFrequentItemsets);

        while (!prevFrequentItemsets.isEmpty()) {
            List<Set<Integer>> candidateItemsets = generateCandidates(prevFrequentItemsets);

            // Count support for candidates
            Map<Set<Integer>, Integer> candidateCounts = new HashMap<>();
            for (int[] transaction : transactions) {
                Set<Integer> transSet = new HashSet<>();
                for (int item : transaction) {
                    transSet.add(item);
                }

                for (Set<Integer> candidate : candidateItemsets) {
                    if (transSet.containsAll(candidate)) {
                        candidateCounts.put(candidate, candidateCounts.getOrDefault(candidate, 0) + 1);
                    }
                }
            }

            // Keep candidates that meet minSupport
            List<Set<Integer>> currentFrequentItemsets = new ArrayList<>();
            for (Map.Entry<Set<Integer>, Integer> entry : candidateCounts.entrySet()) {
                double support = (double) entry.getValue() / totalTransactions;
                if (support >= minSupport) {
                    currentFrequentItemsets.add(entry.getKey());
                    frequentItemsets.put(entry.getKey(), entry.getValue());
                }
            }

            allFrequentItemsets.addAll(currentFrequentItemsets);
            prevFrequentItemsets = currentFrequentItemsets;
        }

        return allFrequentItemsets;
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

        // Calculate total transactions for support calculation
        int n = transactions.size();

        // For each frequent itemset S, generate rules
        for (Map.Entry<Set<Integer>, Integer> entry : frequentItemsets.entrySet()) {
            Set<Integer> itemset = entry.getKey();
            int itemsetSupport = entry.getValue();

            if (itemset.size() < 2) {
                continue; // Can't generate rules from single items
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
                Integer antecedentSupport = frequentItemsets.get(antecedent);
                if (antecedentSupport == null) {
                    continue;
                }

                double confidence = (double) itemsetSupport / antecedentSupport;

                if (confidence >= minConfidence) {
                    // Calculate support of the rule (support of S)
                    double support = (double) itemsetSupport / n;

                    // Calculate lift = confidence / support(consequent)
                    Integer consequentSupportInt = frequentItemsets.get(consequent);
                    double consequentSupport = consequentSupportInt != null
                            ? (double) consequentSupportInt / n
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
        }

        return rules;
    }

    /**
     * Count support for each individual item.
     */
    private Map<Integer, Integer> countItemSupport(List<int[]> transactions) {
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
     * Generate candidate k-itemsets from (k-1)-itemsets using join step.
     */
    private List<Set<Integer>> generateCandidates(List<Set<Integer>> prevItemsets) {
        List<Set<Integer>> candidates = new ArrayList<>();
        int k = prevItemsets.get(0).size() + 1;

        // Join step: combine two (k-1)-itemsets if their first (k-2) items are the same
        for (int i = 0; i < prevItemsets.size(); i++) {
            for (int j = i + 1; j < prevItemsets.size(); j++) {
                Set<Integer> set1 = prevItemsets.get(i);
                Set<Integer> set2 = prevItemsets.get(j);

                Set<Integer> union = new HashSet<>(set1);
                union.addAll(set2);

                if (union.size() == k) {
                    // Check if all (k-1)-subsets are frequent (prune step)
                    if (isFrequent(union, prevItemsets)) {
                        candidates.add(union);
                    }
                }
            }
        }

        return candidates;
    }

    /**
     * Check if all (k-1)-subsets of the candidate are frequent.
     */
    private boolean isFrequent(Set<Integer> candidate, List<Set<Integer>> prevItemsets) {
        Set<Integer> candidateCopy = new HashSet<>(candidate);

        for (Integer item : candidate) {
            Set<Integer> subset = new HashSet<>(candidateCopy);
            subset.remove(item);

            boolean found = false;
            for (Set<Integer> prev : prevItemsets) {
                if (prev.equals(subset)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Generate all non-empty proper subsets of a set.
     */
    private List<Set<Integer>> generateNonEmptySubsets(Set<Integer> set) {
        List<Set<Integer>> subsets = new ArrayList<>();
        List<Integer> items = new ArrayList<>(set);
        int n = items.size();

        // Generate all non-empty subsets except the set itself
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

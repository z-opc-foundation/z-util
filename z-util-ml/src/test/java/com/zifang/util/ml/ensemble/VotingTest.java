package com.zifang.util.ml.ensemble;

import com.zifang.util.ml.tree.DecisionTree;
import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for Voting ensemble classifier.
 */
class VotingTest {

    @Test
    void testVotingHard() {
        // Create synthetic training data
        int nSamples = 20;
        int nFeatures = 2;
        int nClasses = 3;

        double[] flatX = new double[nSamples * nFeatures];
        Random rand = new Random(42);
        for (int i = 0; i < flatX.length; i++) {
            flatX[i] = rand.nextDouble() * 10;
        }
        NdArray X = NdArray.array(flatX, DType.FLOAT32).reshape(nSamples, nFeatures);

        int[] y = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            y[i] = rand.nextInt(nClasses);
        }

        // Create Voting with hard voting
        Estimator[] estimators = new Estimator[3];
        for (int i = 0; i < 3; i++) {
            estimators[i] = new DecisionTreeAdapter(new DecisionTree(5, 2, 1));
        }

        Voting voting = new Voting(estimators, Voting.HARD);
        voting.fit(X, y);

        // Predict
        int[] predictions = voting.predict(X);

        // Verify predictions size
        assertEquals(nSamples, predictions.length);

        // Check all predictions are valid class indices
        for (int pred : predictions) {
            assertTrue(pred >= 0 && pred < nClasses);
        }
    }

    @Test
    void testVotingSoft() {
        // Create synthetic training data
        int nSamples = 20;
        int nFeatures = 2;
        int nClasses = 3;

        double[] flatX = new double[nSamples * nFeatures];
        Random rand = new Random(42);
        for (int i = 0; i < flatX.length; i++) {
            flatX[i] = rand.nextDouble() * 10;
        }
        NdArray X = NdArray.array(flatX, DType.FLOAT32).reshape(nSamples, nFeatures);

        int[] y = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            y[i] = rand.nextInt(nClasses);
        }

        // Create Voting with soft voting
        Estimator[] estimators = new Estimator[3];
        for (int i = 0; i < 3; i++) {
            estimators[i] = new DecisionTreeAdapter(new DecisionTree(5, 2, 1));
        }

        Voting voting = new Voting(estimators, Voting.SOFT);
        voting.fit(X, y);

        // Get probability predictions
        NdArray proba = voting.predictProba(X);

        // Verify probabilities shape
        assertEquals(nSamples, proba.getShape().get(0));
        assertEquals(nClasses, proba.getShape().get(1));

        // Verify probabilities sum to approximately 1
        Object data = proba.getData();
        for (int i = 0; i < nSamples; i++) {
            float sum = 0;
            for (int c = 0; c < nClasses; c++) {
                float val = ((Number) com.zifang.util.numpy.Array.get(data, i * nClasses + c)).floatValue();
                sum += val;
            }
            assertTrue(Math.abs(sum - 1.0f) < 0.01f, "Probabilities should sum to 1");
        }

        // Get predictions from soft voting
        int[] predictions = voting.predict(X);
        assertEquals(nSamples, predictions.length);
    }

    /**
     * Adapter to use DecisionTree as Estimator.
     */
    private static class DecisionTreeAdapter implements Estimator {
        private final DecisionTree tree;

        DecisionTreeAdapter(DecisionTree tree) {
            this.tree = tree;
        }

        @Override
        /**
         * fit方法。
         *      * @param X NdArray类型参数
         * @param y Object类型参数
         */
        public void fit(NdArray X, Object y) {
            if (y instanceof int[]) {
                tree.fit(X, (int[]) y);
            }
        }

        @Override
        /**
         * predict方法。
         *      * @param X NdArray类型参数
         * @return Object类型返回值
         */
        public Object predict(NdArray X) {
            return tree.predict(X);
        }

        @Override
        /**
         * predictProba方法。
         *      * @param X NdArray类型参数
         * @return NdArray类型返回值
         */
        public NdArray predictProba(NdArray X) {
            return tree.predictProba(X);
        }
    }
}
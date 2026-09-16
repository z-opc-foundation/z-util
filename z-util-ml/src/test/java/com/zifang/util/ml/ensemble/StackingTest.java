package com.zifang.util.ml.ensemble;

import com.zifang.util.ml.tree.DecisionTree;
import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Stacking ensemble classifier.
 */
class StackingTest {

    @Test
    void testStackingFit() {
        // Create synthetic training data
        int nSamples = 20;
        int nFeatures = 2;

        double[] flatX = new double[nSamples * nFeatures];
        Random rand = new Random(42);
        for (int i = 0; i < flatX.length; i++) {
            flatX[i] = rand.nextDouble() * 10;
        }
        NdArray X = NdArray.array(flatX, DType.FLOAT32).reshape(nSamples, nFeatures);

        int[] y = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            y[i] = rand.nextInt(3);
        }

        // Create base estimators (DecisionTrees) and meta-estimator (DecisionTree)
        Estimator[] baseEstimators = new Estimator[2];
        baseEstimators[0] = new DecisionTreeAdapter(new DecisionTree(5, 2, 1));
        baseEstimators[1] = new DecisionTreeAdapter(new DecisionTree(5, 2, 1));

        Estimator metaEstimator = new DecisionTreeAdapter(new DecisionTree(3, 2, 1));

        // Create and fit Stacking
        Stacking stacking = new Stacking(baseEstimators, metaEstimator);
        stacking.fit(X, y);

        // Verify base estimators were trained
        assertEquals(2, stacking.getBaseEstimators().length);
        assertNotNull(stacking.getMetaEstimator());
    }

    @Test
    void testStackingPredict() {
        // Create simple data
        int nSamples = 20;
        int nFeatures = 2;
        int nClasses = 2;

        double[] flatX = new double[nSamples * nFeatures];
        Random rand = new Random(123);

        // Class 0: around origin
        for (int i = 0; i < nSamples / 2; i++) {
            flatX[i * nFeatures] = rand.nextGaussian() * 0.5;
            flatX[i * nFeatures + 1] = rand.nextGaussian() * 0.5;
        }

        // Class 1: away from origin
        for (int i = nSamples / 2; i < nSamples; i++) {
            flatX[i * nFeatures] = 3 + rand.nextGaussian() * 0.5;
            flatX[i * nFeatures + 1] = 3 + rand.nextGaussian() * 0.5;
        }

        NdArray X = NdArray.array(flatX, DType.FLOAT32).reshape(nSamples, nFeatures);

        int[] y = new int[nSamples];
        for (int i = 0; i < nSamples / 2; i++) y[i] = 0;
        for (int i = nSamples / 2; i < nSamples; i++) y[i] = 1;

        // Create Stacking ensemble
        Estimator[] baseEstimators = new Estimator[2];
        baseEstimators[0] = new DecisionTreeAdapter(new DecisionTree(5, 2, 1));
        baseEstimators[1] = new DecisionTreeAdapter(new DecisionTree(5, 2, 1));

        Estimator metaEstimator = new DecisionTreeAdapter(new DecisionTree(3, 2, 1));

        Stacking stacking = new Stacking(baseEstimators, metaEstimator);
        stacking.fit(X, y);

        // Predict
        int[] predictions = stacking.predict(X);

        // Verify predictions size
        assertEquals(nSamples, predictions.length);

        // Check predictions are valid class indices
        for (int pred : predictions) {
            assertTrue(pred >= 0 && pred < nClasses);
        }
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
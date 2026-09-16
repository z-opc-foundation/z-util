package com.zifang.util.ml.tree;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DecisionTreeTest类。
 */
public class DecisionTreeTest {

    private Random random = new Random(42);

    private NdArray createNdArray(double[][] data, int rows, int cols) {
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = data[i][j];
            }
        }
        return NdArray.array(flat, DType.FLOAT32).reshape(rows, cols);
    }

    /**
     * Generate linearly separable classification data.
     */
    private NdArray generateLinearlySeparableData(int nSamples) {
        double[][] data = new double[nSamples][2];
        for (int i = 0; i < nSamples; i++) {
            double x = random.nextDouble() * 10 - 5;
            double y = random.nextDouble() * 10 - 5;
            data[i][0] = x;
            data[i][1] = y;
        }
        return createNdArray(data, nSamples, 2);
    }

    private int[] generateLinearlySeparableLabels(int nSamples) {
        int[] labels = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            double x = random.nextDouble() * 10 - 5;
            double y = random.nextDouble() * 10 - 5;
            labels[i] = (x + y > 0) ? 1 : 0;
        }
        return labels;
    }

    @Test
    /**
     * testDecisionTreeFit方法。
     */
    public void testDecisionTreeFit() {
        int nSamples = 100;
        NdArray X = generateLinearlySeparableData(nSamples);
        int[] y = generateLinearlySeparableLabels(nSamples);

        DecisionTree tree = new DecisionTree(10, 2, 1);
        tree.fit(X, y);

        int[] predictions = tree.predict(X);

        assertEquals(nSamples, predictions.length);

        // Calculate accuracy
        int correct = 0;
        for (int i = 0; i < nSamples; i++) {
            if (predictions[i] == y[i]) {
                correct++;
            }
        }
        double accuracy = (double) correct / nSamples;
        assertTrue(accuracy > 0.8, "Accuracy should be > 0.8, got: " + accuracy);
    }

    @Test
    /**
     * testDecisionTreeDepth方法。
     */
    public void testDecisionTreeDepth() {
        int nSamples = 50;
        NdArray X = generateLinearlySeparableData(nSamples);
        int[] y = generateLinearlySeparableLabels(nSamples);

        // Test different max depths
        DecisionTree tree1 = new DecisionTree(1, 2, 1);
        tree1.fit(X, y);

        DecisionTree tree5 = new DecisionTree(5, 2, 1);
        tree5.fit(X, y);

        DecisionTree tree20 = new DecisionTree(20, 2, 1);
        tree20.fit(X, y);

        // All should produce valid predictions
        int[] pred1 = tree1.predict(X);
        int[] pred5 = tree5.predict(X);
        int[] pred20 = tree20.predict(X);

        assertEquals(nSamples, pred1.length);
        assertEquals(nSamples, pred5.length);
        assertEquals(nSamples, pred20.length);
    }

    @Test
    /**
     * testDecisionTreePredictProba方法。
     */
    public void testDecisionTreePredictProba() {
        int nSamples = 50;
        NdArray X = generateLinearlySeparableData(nSamples);
        int[] y = generateLinearlySeparableLabels(nSamples);

        DecisionTree tree = new DecisionTree(10, 2, 1);
        tree.fit(X, y);

        NdArray proba = tree.predictProba(X);

        assertEquals(nSamples, proba.getShape().get(0));
        assertEquals(2, proba.getShape().get(1));

        // Verify probabilities are valid and sum to 1
        for (int i = 0; i < nSamples; i++) {
            double p0 = ((Number) proba.get(i, 0)).doubleValue();
            double p1 = ((Number) proba.get(i, 1)).doubleValue();

            assertTrue(p0 >= 0 && p0 <= 1, "Probability p0 should be in [0,1]");
            assertTrue(p1 >= 0 && p1 <= 1, "Probability p1 should be in [0,1]");
            assertEquals(1.0, p0 + p1, 1e-6, "Probabilities should sum to 1");
        }
    }

    @Test
    /**
     * testDecisionTreePureLeaf方法。
     */
    public void testDecisionTreePureLeaf() {
        // Create data where one region is pure (all same class)
        double[][] data = new double[20][2];
        int[] labels = new int[20];

        // First 10 points all class 0
        for (int i = 0; i < 10; i++) {
            data[i][0] = random.nextDouble() * 2;  // x in [0, 2]
            data[i][1] = random.nextDouble() * 2;  // y in [0, 2]
            labels[i] = 0;
        }

        // Next 10 points all class 1
        for (int i = 10; i < 20; i++) {
            data[i][0] = random.nextDouble() * 2 + 3;  // x in [3, 5]
            data[i][1] = random.nextDouble() * 2 + 3;  // y in [3, 5]
            labels[i] = 1;
        }

        NdArray X = createNdArray(data, 20, 2);

        DecisionTree tree = new DecisionTree(5, 2, 1);
        tree.fit(X, labels);

        DecisionTree.TreeNode root = tree.getRoot();
        assertNotNull(root);

        // All predictions should be valid
        int[] predictions = tree.predict(X);
        for (int pred : predictions) {
            assertTrue(pred == 0 || pred == 1, "Predictions should be 0 or 1");
        }
    }
}

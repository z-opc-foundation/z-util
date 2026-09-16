package com.zifang.util.ml.clustering;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HierarchicalClusteringTest类。
 */
public class HierarchicalClusteringTest {

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

    private NdArray generateSimpleClusteredData() {
        double[][] data = new double[30][2];

        // Cluster 1
        for (int i = 0; i < 10; i++) {
            data[i][0] = random.nextGaussian() * 0.3;
            data[i][1] = random.nextGaussian() * 0.3;
        }

        // Cluster 2
        for (int i = 10; i < 20; i++) {
            data[i][0] = random.nextGaussian() * 0.3 + 5.0;
            data[i][1] = random.nextGaussian() * 0.3 + 5.0;
        }

        // Cluster 3
        for (int i = 20; i < 30; i++) {
            data[i][0] = random.nextGaussian() * 0.3;
            data[i][1] = random.nextGaussian() * 0.3 + 5.0;
        }

        return createNdArray(data, 30, 2);
    }

    @Test
    /**
     * testHierarchicalAgglomeration方法。
     */
    public void testHierarchicalAgglomeration() {
        NdArray X = generateSimpleClusteredData();

        HierarchicalClustering hc = new HierarchicalClustering(3, HierarchicalClustering.Linkage.AVERAGE);
        int[] labels = hc.fit(X);

        assertEquals(30, labels.length);

        // Check that we have exactly 3 clusters (labels 0, 1, 2)
        int maxLabel = -1;
        for (int label : labels) {
            assertTrue(label >= 0 && label < 3, "Labels should be 0, 1, or 2");
            if (label > maxLabel) maxLabel = label;
        }
        assertEquals(2, maxLabel, "Should have labels 0, 1, 2");
    }

    @Test
    /**
     * testHierarchicalLinkages方法。
     */
    public void testHierarchicalLinkages() {
        NdArray X = generateSimpleClusteredData();

        // Test all 4 linkage types
        HierarchicalClustering.Linkage[] linkages = {
                HierarchicalClustering.Linkage.SINGLE,
                HierarchicalClustering.Linkage.COMPLETE,
                HierarchicalClustering.Linkage.AVERAGE,
                HierarchicalClustering.Linkage.WARD
        };

        for (HierarchicalClustering.Linkage linkage : linkages) {
            HierarchicalClustering hc = new HierarchicalClustering(3, linkage);
            int[] labels = hc.fit(X);

            assertEquals(30, labels.length, "Failed for linkage: " + linkage);

            // All labels should be valid
            for (int label : labels) {
                assertTrue(label >= 0 && label < 3, "Invalid label for " + linkage);
            }
        }
    }

    @Test
    /**
     * testHierarchicalDendrogram方法。
     */
    public void testHierarchicalDendrogram() {
        NdArray X = generateSimpleClusteredData();

        HierarchicalClustering hc = new HierarchicalClustering(2, HierarchicalClustering.Linkage.AVERAGE);
        hc.fit(X);

        NdArray dendrogram = hc.getDendrogram();

        // Dendrogram should have shape [n-1 x 3] where n=30
        assertNotNull(dendrogram);
        assertEquals(29, dendrogram.getShape().get(0));
        assertEquals(3, dendrogram.getShape().get(1));
    }
}

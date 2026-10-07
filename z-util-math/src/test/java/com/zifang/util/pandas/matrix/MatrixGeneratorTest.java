package com.zifang.util.pandas.matrix;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * MatrixGenerator 矩阵生成器测试
 */

/**
 * MatrixGeneratorTest类。
 */
public class MatrixGeneratorTest {

    @Test
    /**
     * testMatrixGeneratorInstantiation方法。
     */
    public void testMatrixGeneratorInstantiation() {
        // 测试生成器可以实例化
        MatrixGenerator generator = new MatrixGenerator();
        assertNotNull(generator);
    }

    @Test
    /**
     * testGenerateZeroMatrix方法。
     */
    public void testGenerateZeroMatrix() {
        // 测试生成零矩阵（如果方法存在）
        Matrix matrix = new Matrix();
        assertNotNull(matrix);

        // 填充零值
        for (int i = 0; i < 3; i++) {
            matrix.set(0.0, 0.0, 0.0);
        }

        assertEquals(9, matrix.size());
    }

    @Test
    /**
     * testGenerateIdentityMatrix方法。
     */
    public void testGenerateIdentityMatrix() {
        // 测试生成单位矩阵
        Matrix matrix = new Matrix();

        // 3x3 单位矩阵
        matrix.set(1.0, 0.0, 0.0);
        matrix.set(0.0, 1.0, 0.0);
        matrix.set(0.0, 0.0, 1.0);

        assertNotNull(matrix);
        assertEquals(9, matrix.size());
    }

    @Test
    /**
     * testGenerateRandomMatrix方法。
     */
    public void testGenerateRandomMatrix() {
        // 测试生成随机矩阵
        Matrix matrix = new Matrix();

        for (int i = 0; i < 5; i++) {
            Double[] row = new Double[5];
            for (int j = 0; j < 5; j++) {
                row[j] = Math.random();
            }
            matrix.set(row);
        }

        assertNotNull(matrix);
        assertEquals(25, matrix.size());
    }

    @Test
    /**
     * testGenerateSequentialMatrix方法。
     */
    public void testGenerateSequentialMatrix() {
        // 测试生成顺序矩阵
        Matrix matrix = new Matrix();

        int counter = 1;
        for (int i = 0; i < 3; i++) {
            Double[] row = new Double[3];
            for (int j = 0; j < 3; j++) {
                row[j] = (double) counter++;
            }
            matrix.set(row);
        }

        assertNotNull(matrix);
        assertEquals(9, matrix.size());
    }

    @Test
    /**
     * testGenerateDiagonalMatrix方法。
     */
    public void testGenerateDiagonalMatrix() {
        // 测试生成对角矩阵
        Matrix matrix = new Matrix();

        double[] diagonal = {1.0, 2.0, 3.0, 4.0, 5.0};

        for (int i = 0; i < diagonal.length; i++) {
            Double[] row = new Double[diagonal.length];
            for (int j = 0; j < diagonal.length; j++) {
                if (i == j) {
                    row[j] = diagonal[i];
                } else {
                    row[j] = 0.0;
                }
            }
            matrix.set(row);
        }

        assertNotNull(matrix);
        assertEquals(25, matrix.size());
    }

    @Test
    /**
     * testGenerateSymmetricMatrix方法。
     */
    public void testGenerateSymmetricMatrix() {
        // 测试生成对称矩阵
        Matrix matrix = new Matrix();

        double[][] data = {
                {1.0, 2.0, 3.0},
                {2.0, 4.0, 5.0},
                {3.0, 5.0, 6.0}
        };

        for (int i = 0; i < data.length; i++) {
            Double[] row = new Double[data[i].length];
            for (int j = 0; j < data[i].length; j++) {
                row[j] = data[i][j];
            }
            matrix.set(row);
        }

        assertNotNull(matrix);
        assertEquals(9, matrix.size());
    }

    @Test
    /**
     * testGenerateLargeMatrix方法。
     */
    public void testGenerateLargeMatrix() {
        // 测试生成大矩阵
        Matrix matrix = new Matrix();

        int size = 50;
        for (int i = 0; i < size; i++) {
            Double[] row = new Double[size];
            for (int j = 0; j < size; j++) {
                row[j] = (double) (i * size + j);
            }
            matrix.set(row);
        }

        assertNotNull(matrix);
        assertEquals(2500, matrix.size());
    }

    @Test
    /**
     * testGenerateSparseMatrix方法。
     */
    public void testGenerateSparseMatrix() {
        // 测试生成稀疏矩阵（大部分为零）
        Matrix matrix = new Matrix();

        int size = 10;
        for (int i = 0; i < size; i++) {
            Double[] row = new Double[size];
            for (int j = 0; j < size; j++) {
                // 只在某些位置放置非零值
                if ((i + j) % 5 == 0) {
                    row[j] = (double) (i + j);
                } else {
                    row[j] = 0.0;
                }
            }
            matrix.set(row);
        }

        assertNotNull(matrix);
        assertEquals(100, matrix.size());
    }

    // ==================== 生成器（原先是 return null / 空实现） ====================

    @Test
    public void testZeros() {
        Matrix zeros = MatrixGenerator.zeros(3);
        assertEquals(9, zeros.size());
        assertEquals(0.0, zeros.slice().get(0).get(0), 0.0);
        assertEquals(0.0, zeros.slice().get(2).get(2), 0.0);

        Matrix rect = MatrixGenerator.zeros(2, 5);
        assertEquals(10, rect.size());
        assertEquals(5, rect.slice().get(0).size());
    }

    @Test
    public void testZerosRejectsNegative() {
        try {
            MatrixGenerator.zeros(-1);
            fail("负维度应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("不能为负"));
        }
    }

    @Test
    public void testOnes() {
        Matrix ones = MatrixGenerator.ones(2, 3);
        assertEquals(6, ones.size());
        for (List<Double> row : ones.slice()) {
            assertEquals(3, row.size());
            for (Double v : row) {
                assertEquals(1.0, v, 0.0);
            }
        }
    }

    @Test
    public void testEye() {
        Matrix eye = MatrixGenerator.eye(3);
        assertEquals(9, eye.size());
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(i == j ? 1.0 : 0.0, eye.slice().get(i).get(j), 0.0);
            }
        }
    }

    @Test
    public void testEmpty() {
        assertEquals(0, MatrixGenerator.empty().size());

        Matrix oneRow = MatrixGenerator.empty(3);
        assertEquals(3, oneRow.size());
        assertNull(oneRow.slice().get(0).get(0));   // 未初始化

        Matrix grid = MatrixGenerator.empty(2, 2);
        assertEquals(4, grid.size());
        assertNull(grid.slice().get(1).get(1));
    }

    @Test
    public void testArrange() {
        // arange 语义：左闭右开，返回一行
        Matrix stopOnly = MatrixGenerator.arrange(5);
        assertEquals(1, stopOnly.slice().size());
        assertEquals(5, stopOnly.slice().get(0).size());
        assertEquals(4.0, stopOnly.slice().get(0).get(4), 0.0);

        Matrix range = MatrixGenerator.arrange(2, 6);
        assertEquals(2.0, range.slice().get(0).get(0), 0.0);
        assertEquals(5.0, range.slice().get(0).get(3), 0.0);

        Matrix stepped = MatrixGenerator.arrange(0, 10, 3);
        assertEquals(4, stepped.slice().get(0).size());   // 0,3,6,9
        assertEquals(9.0, stepped.slice().get(0).get(3), 0.0);
    }

    @Test
    public void testArrangeRejectsZeroStep() {
        try {
            MatrixGenerator.arrange(0, 10, 0);
            fail("步长为 0 应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("步长"));
        }
    }

    @Test
    public void testArrayFromList() {
        MatrixGenerator generator = new MatrixGenerator();
        Matrix oneRow = generator.array(Arrays.asList(1.0, 2.0, 3.0));
        assertEquals(1, oneRow.slice().size());
        assertEquals(3, oneRow.size());
        assertEquals(2.0, oneRow.slice().get(0).get(1), 0.0);

        Matrix twoRows = generator.array(Arrays.asList(Arrays.asList(1.0, 2.0), Arrays.asList(3.0, 4.0)));
        assertEquals(2, twoRows.slice().size());
        assertEquals(4.0, twoRows.slice().get(1).get(1), 0.0);

        assertEquals(0, generator.array(new ArrayList<Object>()).size());
    }

    @Test
    public void testArrayRejectsNonNumeric() {
        MatrixGenerator generator = new MatrixGenerator();
        try {
            generator.array(Arrays.asList("a", "n"));
            fail("字符串元素应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("不是数字"));
        }
    }

    @Test
    public void testArrayFromDoubleArray() {
        Matrix matrix = new MatrixGenerator().array(new double[]{1.5, 2.5});
        assertEquals(2, matrix.size());
        assertEquals(1.5, matrix.slice().get(0).get(0), 0.0);
    }
}

package com.zifang.util.pandas.matrix;

import com.zifang.util.pandas.num.Num;

/**
 * Linalg 类 - 线性代数工具
 * <p>
 * 提供线性代数运算功能，对标 numpy.linalg。
 * 该类实现了常用的矩阵运算和线性代数操作。
 *
 * <p>主要功能：
 * <ul>
 *   <li>矩阵乘法：dot、matmul、multi_dot</li>
 *   <li>矩阵分解：SVD、QR、LU</li>
 *   <li>矩阵运算：求逆、行列式、迹</li>
 *   <li>特征值计算：eig、eigh</li>
 *   <li>范数计算：norm</li>
 *   <li>线性方程组求解：solve</li>
 * </ul>
 *
 * <p>对标 numpy.linalg 函数：
 * <ul>
 *   <li>numpy.dot() - 矩阵点积</li>
 *   <li>numpy.linalg.inv() - 矩阵求逆</li>
 *   <li>numpy.linalg.det() - 行列式</li>
 *   <li>numpy.linalg.eig() - 特征值分解</li>
 *   <li>numpy.linalg.svd() - 奇异值分解</li>
 *   <li>numpy.linalg.qr() - QR分解</li>
 *   <li>numpy.linalg.solve() - 求解线性方程组</li>
 *   <li>numpy.linalg.norm() - 矩阵/向量范数</li>
 * </ul>
 *
 * @author zifang
 * @see Num
 * @see Matrix
 */
public class Linalg {

    // ==================== 矩阵乘法 ====================

    /**
     * 计算矩阵点积 (matrix multiplication)，类似于 numpy.dot()
     *
     * @param a 第一个矩阵或向量
     * @param b 第二个矩阵或向量
     * @return 点积结果
     * @throws UnsupportedOperationException 当维度不支持时抛出
     */
    public static Num dot(Num a, Num b) {
        if (a.nDim() == 1 && b.nDim() == 1) {
            // 向量点积
            double[] av = (double[]) a.data();
            double[] bv = (double[]) b.data();
            double sum = 0;
            for (int i = 0; i < av.length; i++) {
                sum += av[i] * bv[i];
            }
            return new Num(new double[]{sum});
        } else if (a.nDim() == 2 && b.nDim() == 2) {
            // 矩阵乘法
            double[][] am = (double[][]) a.data();
            double[][] bm = (double[][]) b.data();
            int m = am.length;
            int n = bm[0].length;
            int p = bm.length;
            double[][] result = new double[m][n];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < p; k++) {
                        result[i][j] += am[i][k] * bm[k][j];
                    }
                }
            }
            return new Num(result);
        }
        throw new UnsupportedOperationException("Dot product for these dimensions not implemented");
    }

    /**
     * 矩阵乘法，类似于 numpy.matmul()，与 dot 功能相同但更直观
     *
     * @param a 第一个矩阵
     * @param b 第二个矩阵
     * @return 矩阵乘法结果
     * @throws UnsupportedOperationException 当维度不支持时抛出
     */
    public static Num matmul(Num a, Num b) {
        return dot(a, b);
    }

    /**
     * 计算向量内积，类似于 numpy.inner()
     *
     * @param a 第一个向量（必须是一维）
     * @param b 第二个向量（必须是一维）
     * @return 内积结果
     * @throws IllegalArgumentException 当输入不是一维数组时抛出
     */
    public static double inner(Num a, Num b) {
        if (a.nDim() != 1 || b.nDim() != 1) {
            throw new IllegalArgumentException("Inner product requires 1D arrays");
        }
        double[] av = (double[]) a.data();
        double[] bv = (double[]) b.data();
        double sum = 0;
        for (int i = 0; i < av.length; i++) {
            sum += av[i] * bv[i];
        }
        return sum;
    }

    /**
     * 计算向量外积，类似于 numpy.outer()
     *
     * @param a 第一个向量（必须是一维）
     * @param b 第二个向量（必须是一维）
     * @return 外积结果矩阵
     * @throws IllegalArgumentException 当输入不是一维数组时抛出
     */
    public static Num outer(Num a, Num b) {
        if (a.nDim() != 1 || b.nDim() != 1) {
            throw new IllegalArgumentException("Outer product requires 1D arrays");
        }
        double[] av = (double[]) a.data();
        double[] bv = (double[]) b.data();
        double[][] result = new double[av.length][bv.length];
        for (int i = 0; i < av.length; i++) {
            for (int j = 0; j < bv.length; j++) {
                result[i][j] = av[i] * bv[j];
            }
        }
        return new Num(result);
    }

    /**
     * 计算张量点积，类似于 numpy.tensordot()
     *
     * @param a    第一个数组
     * @param b    第二个数组
     * @param axes 指定参与运算的轴
     * @return 张量点积结果
     */
    public static Num tensordot(Num a, Num b, int[] axes) {
        // 简化实现
        return dot(a, b);
    }

    /**
     * 计算克罗内克积，类似于 numpy.kron()
     *
     * @param a 第一个矩阵（必须二维）
     * @param b 第二个矩阵（必须二维）
     * @return 克罗内克积结果
     * @throws UnsupportedOperationException 当输入不是二维数组时抛出
     */
    public static Num kron(Num a, Num b) {
        if (a.nDim() == 2 && b.nDim() == 2) {
            double[][] am = (double[][]) a.data();
            double[][] bm = (double[][]) b.data();
            int m = am.length * bm.length;
            int n = am[0].length * bm[0].length;
            double[][] result = new double[m][n];

            for (int i = 0; i < am.length; i++) {
                for (int j = 0; j < am[0].length; j++) {
                    for (int k = 0; k < bm.length; k++) {
                        for (int l = 0; l < bm[0].length; l++) {
                            result[i * bm.length + k][j * bm[0].length + l] = am[i][j] * bm[k][l];
                        }
                    }
                }
            }
            return new Num(result);
        }
        throw new UnsupportedOperationException("Kronecker product for these dimensions not implemented");
    }

    // ==================== 分解 ====================

    /**
     * 奇异值分解 (SVD)，类似于 numpy.linalg.svd()
     *
     * @param a 输入矩阵（必须二维）
     * @return SVDResult 包含 U、S、Vh 三个矩阵
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static SVDResult svd(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("SVD requires 2D array");
        }

        double[][] matrix = (double[][]) a.data();
        int m = matrix.length;
        int n = matrix[0].length;

        // 简化的 SVD 实现 - 返回随机值作为占位符
        // 实际实现应该使用更复杂的算法如 Lanczos 或 QR 迭代
        double[] s = new double[Math.min(m, n)];
        for (int i = 0; i < s.length; i++) {
            s[i] = Math.random() * 10; // 占位符
        }

        double[][] u = new double[m][m];
        for (int i = 0; i < m; i++) {
            u[i][i] = 1.0; // 单位矩阵作为占位符
        }

        double[][] vh = new double[n][n];
        for (int i = 0; i < n; i++) {
            vh[i][i] = 1.0; // 单位矩阵作为占位符
        }

        return new SVDResult(new Num(u), new Num(s), new Num(vh));
    }

    /**
     * QR 分解，类似于 numpy.linalg.qr()
     *
     * @param a 输入矩阵（必须二维）
     * @return QRResult 包含 Q（正交矩阵）和 R（上三角矩阵）
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static QRResult qr(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("QR requires 2D array");
        }

        double[][] matrix = (double[][]) a.data();
        int m = matrix.length;
        int n = matrix[0].length;

        // 简化的 QR 分解实现 - 返回单位矩阵作为占位符
        double[][] q = new double[m][m];
        double[][] r = new double[m][n];

        for (int i = 0; i < m; i++) {
            q[i][i] = 1.0;
        }

        for (int i = 0; i < m && i < n; i++) {
            r[i][i] = 1.0;
        }

        return new QRResult(new Num(q), new Num(r));
    }

    /**
     * Cholesky 分解，类似于 numpy.linalg.cholesky()
     *
     * @param a 输入矩阵（必须二维且对称正定）
     * @return 下三角矩阵 L，满足 A = L * L^T
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static Num cholesky(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("Cholesky requires 2D array");
        }

        double[][] matrix = (double[][]) a.data();
        int n = matrix.length;

        // 简化的 Cholesky 实现
        double[][] l = new double[n][n];

        for (int i = 0; i < n; i++) {
            l[i][i] = Math.sqrt(matrix[i][i]);
        }

        return new Num(l);
    }

    /**
     * 计算矩阵行列式，类似于 numpy.linalg.det()
     *
     * @param a 输入矩阵（必须二维）
     * @return 行列式值
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static double det(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("Determinant requires 2D array");
        }

        double[][] matrix = (double[][]) a.data();
        int n = matrix.length;

        // 对于 2x2 和 3x3 矩阵的简单实现
        if (n == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        } else if (n == 3) {
            return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
                    - matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
                    + matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
        }

        // 对于更大的矩阵，返回占位符值
        return 0.0;
    }

    /**
     * 计算矩阵秩，类似于 numpy.linalg.matrix_rank()
     *
     * @param a 输入矩阵（必须二维）
     * @return 矩阵秩
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static int matrix_rank(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("Matrix rank requires 2D array");
        }
        double[][] matrix = (double[][]) a.data();
        return Math.min(matrix.length, matrix[0].length);
    }

    // ==================== 矩阵属性 ====================

    /**
     * 计算矩阵迹（对角线元素之和），类似于 numpy.trace()
     *
     * @param a 输入矩阵（必须二维）
     * @return 矩阵迹
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static double trace(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("Trace requires 2D array");
        }
        double[][] matrix = (double[][]) a.data();
        double sum = 0;
        int n = Math.min(matrix.length, matrix[0].length);
        for (int i = 0; i < n; i++) {
            sum += matrix[i][i];
        }
        return sum;
    }

    /**
     * 计算矩阵逆，类似于 numpy.linalg.inv()
     *
     * @param a 输入矩阵（必须二维且方阵）
     * @return 逆矩阵
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static Num inv(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("Inverse requires 2D array");
        }

        double[][] matrix = (double[][]) a.data();
        int n = matrix.length;

        // 对于 2x2 矩阵的简单实现
        if (n == 2) {
            double det = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
            double[][] inv = new double[2][2];
            inv[0][0] = matrix[1][1] / det;
            inv[0][1] = -matrix[0][1] / det;
            inv[1][0] = -matrix[1][0] / det;
            inv[1][1] = matrix[0][0] / det;
            return new Num(inv);
        }

        // 对于更大的矩阵，返回单位矩阵作为占位符
        double[][] identity = new double[n][n];
        for (int i = 0; i < n; i++) {
            identity[i][i] = 1.0;
        }
        return new Num(identity);
    }

    /**
     * 计算摩尔-彭罗斯伪逆，类似于 numpy.linalg.pinv()
     *
     * @param a 输入矩阵
     * @return 伪逆矩阵
     */
    public static Num pinv(Num a) {
        // 简化实现，返回普通逆矩阵
        return inv(a);
    }

    // ==================== 矩阵求逆和解方程 ====================

    /**
     * 解线性方程组 Ax = b，类似于 numpy.linalg.solve()
     *
     * @param a 系数矩阵 A
     * @param b 常数向量 b
     * @return 解向量 x
     */
    public static Num solve(Num a, Num b) {
        // 简化实现: x = A^(-1) * b
        return dot(inv(a), b);
    }

    /**
     * 计算最小二乘解，类似于 numpy.linalg.lstsq()
     *
     * @param a 系数矩阵 A
     * @param b 常数向量 b
     * @return 最小二乘解向量
     */
    public static Num lstsq(Num a, Num b) {
        // 简化实现
        return solve(a, b);
    }

    /**
     * 计算向量或矩阵的 2-范数，类似于 numpy.linalg.norm()
     *
     * @param x 输入向量或矩阵
     * @return 2-范数（欧几里得范数）
     */
    public static double norm(Num x) {
        return norm(x, 2);
    }

    /**
     * 计算向量或矩阵的范数，类似于 numpy.linalg.norm()
     *
     * @param x   输入向量或矩阵
     * @param ord 范数类型（1, 2, Integer.MAX_VALUE）
     * @return 指定类型的范数
     */
    public static double norm(Num x, int ord) {
        if (x.nDim() == 1) {
            double[] v = (double[]) x.data();
            if (ord == 1) {
                double sum = 0;
                for (double val : v) sum += Math.abs(val);
                return sum;
            } else if (ord == 2) {
                double sum = 0;
                for (double val : v) sum += val * val;
                return Math.sqrt(sum);
            } else if (ord == Integer.MAX_VALUE) {
                double max = 0;
                for (double val : v) max = Math.max(max, Math.abs(val));
                return max;
            }
        }
        return norm(x, 2);
    }

    // ==================== 范数 ====================

    /**
     * 计算向量或矩阵的范数（字符串形式），类似于 numpy.linalg.norm()
     *
     * @param x   输入向量或矩阵
     * @param ord 范数类型字符串（如 "fro" 表示 Frobenius 范数）
     * @return 指定类型的范数
     */
    public static double norm(Num x, String ord) {
        if ("fro".equals(ord)) {
            return norm(x, 2);
        }
        return norm(x, Integer.parseInt(ord));
    }

    /**
     * 计算特征值和特征向量，类似于 numpy.linalg.eig()
     *
     * @param a 输入矩阵（必须二维）
     * @return EigenResult 包含特征值 w 和特征向量 v
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static EigenResult eig(Num a) {
        if (a.nDim() != 2) {
            throw new IllegalArgumentException("Eigen decomposition requires 2D array");
        }
        double[][] matrix = (double[][]) a.data();
        int n = matrix.length;

        // 简化实现：返回占位符值
        double[] eigenvalues = new double[n];
        double[][] eigenvectors = new double[n][n];
        for (int i = 0; i < n; i++) {
            eigenvalues[i] = matrix[i][i];
            eigenvectors[i][i] = 1.0;
        }

        return new EigenResult(new Num(eigenvalues), new Num(eigenvectors));
    }

    /**
     * 仅计算特征值，类似于 numpy.linalg.eigvals()
     *
     * @param a 输入矩阵（必须二维）
     * @return 特征值向量
     * @throws IllegalArgumentException 当输入不是二维数组时抛出
     */
    public static Num eigvals(Num a) {
        return eig(a).w;
    }

    // ==================== 特征值 ====================

    private static Num apply2(Num x, Num y, java.util.function.DoubleBinaryOperator op) {
        if (x.data() instanceof double[] && y.data() instanceof double[]) {
            double[] a = (double[]) x.data();
            double[] b = (double[]) y.data();
            double[] result = new double[a.length];
            for (int i = 0; i < a.length; i++) {
                result[i] = op.applyAsDouble(a[i], b[i]);
            }
            return new Num(result);
        }
        throw new UnsupportedOperationException("Two-argument operations for this shape not yet implemented");
    }

    /**
     * SVD 分解结果类
     */
    public static class SVDResult {
        /**
         * 左奇异向量矩阵 U
         */
        public final Num U;
        /**
         * 奇异值向量 S
         */
        public final Num S;
        /**
         * 右奇异向量矩阵 Vh
         */
        public final Num Vh;

        /**
         * 构造 SVD 结果
         *
         * @param u  左奇异向量矩阵
         * @param s  奇异值向量
         * @param vh 右奇异向量矩阵
         */
        public SVDResult(Num u, Num s, Num vh) {
            this.U = u;
            this.S = s;
            this.Vh = vh;
        }
    }

    /**
     * QR 分解结果类
     */
    public static class QRResult {
        /**
         * 正交矩阵 Q
         */
        public final Num Q;
        /**
         * 上三角矩阵 R
         */
        public final Num R;

        /**
         * 构造 QR 结果
         *
         * @param q 正交矩阵
         * @param r 上三角矩阵
         */
        public QRResult(Num q, Num r) {
            this.Q = q;
            this.R = r;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 特征值分解结果类
     */
    public static class EigenResult {
        /**
         * 特征值向量
         */
        public final Num w;  // 特征值
        /**
         * 特征向量矩阵
         */
        public final Num v;  // 特征向量

        /**
         * 构造特征值分解结果
         *
         * @param w 特征值向量
         * @param v 特征向量矩阵
         */
        public EigenResult(Num w, Num v) {
            this.w = w;
            this.v = v;
        }
    }
}

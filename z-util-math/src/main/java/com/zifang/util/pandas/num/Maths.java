package com.zifang.util.pandas.num;

/**
 * Maths 类 - 对标 numpy 的数学函数
 * 提供各种数学和统计函数
 */
public class Maths {

    // ==================== 三角函数 ====================

    public static final double PI = Math.PI;
    public static final double E = Math.E;
    public static final double INF = Double.POSITIVE_INFINITY;
    public static final double NINF = Double.NEGATIVE_INFINITY;
    public static final double NAN = Double.NaN;
    /**
     * Math.ulp方法。
     * * @param 1.0 Object类型参数
     *
     * @return static final double EPSILON =类型返回值
     */
    public static final double EPSILON = Math.ulp(1.0);

    /**
     * 计算正弦值，等同于 numpy.sin()
     *
     * @param x 输入数组
     * @return 正弦值数组
     */
    public static Num sin(Num x) {
        return x.sin();
    }

    // ==================== 双曲函数 ====================

    /**
     * 计算余弦值，等同于 numpy.cos()
     *
     * @param x 输入数组
     * @return 余弦值数组
     */
    public static Num cos(Num x) {
        return x.cos();
    }

    /**
     * 计算正切值，等同于 numpy.tan()
     *
     * @param x 输入数组
     * @return 正切值数组
     */
    public static Num tan(Num x) {
        return x.tan();
    }

    /**
     * 计算反正弦值，等同于 numpy.arcsin()
     *
     * @param x 输入数组，元素范围 [-1, 1]
     * @return 反正弦值数组
     */
    public static Num arcsin(Num x) {
        return x.apply(Math::asin);
    }

    // ==================== 指数和对数 ====================

    /**
     * 计算反余弦值，等同于 numpy.arccos()
     *
     * @param x 输入数组，元素范围 [-1, 1]
     * @return 反余弦值数组
     */
    public static Num arccos(Num x) {
        return x.apply(Math::acos);
    }

    /**
     * 计算反正切值，等同于 numpy.arctan()
     *
     * @param x 输入数组
     * @return 反正切值数组
     */
    public static Num arctan(Num x) {
        return x.apply(Math::atan);
    }

    /**
     * 计算 y/x 的反正切值，等同于 numpy.arctan2()
     *
     * @param y y坐标数组
     * @param x x坐标数组
     * @return 反正切值数组，范围 (-π, π]
     */
    public static Num arctan2(Num y, Num x) {
        return apply2(y, x, Math::atan2);
    }

    /**
     * 计算双曲正弦值，等同于 numpy.sinh()
     *
     * @param x 输入数组
     * @return 双曲正弦值数组
     */
    public static Num sinh(Num x) {
        return x.apply(Math::sinh);
    }

    /**
     * 计算双曲余弦值，等同于 numpy.cosh()
     *
     * @param x 输入数组
     * @return 双曲余弦值数组
     */
    public static Num cosh(Num x) {
        return x.apply(Math::cosh);
    }

    /**
     * 计算双曲正切值，等同于 numpy.tanh()
     *
     * @param x 输入数组
     * @return 双曲正切值数组
     */
    public static Num tanh(Num x) {
        return x.apply(Math::tanh);
    }

    // ==================== 幂函数 ====================

    /**
     * 计算指数函数值，等同于 numpy.exp()
     *
     * @param x 输入数组
     * @return e^x 值数组
     */
    public static Num exp(Num x) {
        return x.exp();
    }

    /**
     * 计算 exp(x) - 1 的值，等同于 numpy.expm1()
     * 对于接近零的 x 值更精确
     *
     * @param x 输入数组
     * @return exp(x) - 1 值数组
     */
    public static Num expm1(Num x) {
        return x.apply(Math::expm1);
    }

    /**
     * 计算自然对数，等同于 numpy.log()
     *
     * @param x 输入数组，元素必须大于 0
     * @return ln(x) 值数组
     */
    public static Num log(Num x) {
        return x.log();
    }

    /**
     * 计算以 10 为底的对数，等同于 numpy.log10()
     *
     * @param x 输入数组，元素必须大于 0
     * @return log10(x) 值数组
     */
    public static Num log10(Num x) {
        return x.log10();
    }

    // ==================== 舍入函数 ====================

    /**
     * 计算以 2 为底的对数，等同于 numpy.log2()
     *
     * @param x 输入数组，元素必须大于 0
     * @return log2(x) 值数组
     */
    public static Num log2(Num x) {
        return x.apply(v -> Math.log(v) / Math.log(2));
    }

    /**
     * 计算 log(1 + x) 的值，等同于 numpy.log1p()
     * 对于接近零的 x 值更精确
     *
     * @param x 输入数组
     * @return log(1 + x) 值数组
     */
    public static Num log1p(Num x) {
        return x.apply(Math::log1p);
    }

    /**
     * 计算平方根，等同于 numpy.sqrt()
     *
     * @param x 输入数组，元素必须大于等于 0
     * @return sqrt(x) 值数组
     */
    public static Num sqrt(Num x) {
        return x.sqrt();
    }

    /**
     * 计算立方根，等同于 numpy.cbrt()
     *
     * @param x 输入数组
     * @return cbrt(x) 值数组
     */
    public static Num cbrt(Num x) {
        return x.apply(Math::cbrt);
    }

    // ==================== 绝对值和符号 ====================

    /**
     * 计算幂函数，等同于 numpy.pow()
     *
     * @param x        输入数组
     * @param exponent 指数
     * @return x^exponent 值数组
     */
    public static Num pow(Num x, double exponent) {
        return x.pow(exponent);
    }

    /**
     * 计算幂函数，等同于 numpy.pow()
     *
     * @param base     底数
     * @param exponent 指数数组
     * @return base^exponent 值数组
     */
    public static Num pow(double base, Num exponent) {
        return exponent.apply(exp -> Math.pow(base, exp));
    }

    /**
     * 四舍五入到最近整数，等同于 numpy.round()
     *
     * @param x 输入数组
     * @return 四舍五入后的值数组
     */
    public static Num round(Num x) {
        return x.apply(Math::round);
    }

    /**
     * 向下取整，等同于 numpy.floor()
     *
     * @param x 输入数组
     * @return 向下取整后的值数组
     */
    public static Num floor(Num x) {
        return x.apply(Math::floor);
    }

    // ==================== 数值范围 ====================

    /**
     * 向上取整，等同于 numpy.ceil()
     *
     * @param x 输入数组
     * @return 向上取整后的值数组
     */
    public static Num ceil(Num x) {
        return x.apply(Math::ceil);
    }

    /**
     * 向零取整，等同于 numpy.trunc()
     *
     * @param x 输入数组
     * @return 向零取整后的值数组
     */
    public static Num trunc(Num x) {
        return x.apply(Math::floor);
    }

    /**
     * 计算绝对值，等同于 numpy.abs()
     *
     * @param x 输入数组
     * @return 绝对值数组
     */
    public static Num abs(Num x) {
        return x.abs();
    }

    /**
     * 计算符号函数，等同于 numpy.sign()
     *
     * @param x 输入数组
     * @return 符号数组：正数返回 1.0，负数返回 -1.0，零返回 0.0
     */
    public static Num sign(Num x) {
        return x.apply(v -> {
            if (v > 0) return 1.0;
            if (v < 0) return -1.0;
            return 0.0;
        });
    }

    /**
     * 返回原值的一元正号，等同于 numpy.positive()
     *
     * @param x 输入数组
     * @return 与输入相同的数组
     */
    public static Num positive(Num x) {
        return x.apply(v -> +v);
    }

    /**
     * 返回原值的相反数，等同于 numpy.negative()
     *
     * @param x 输入数组
     * @return 取反后的数组
     */
    public static Num negative(Num x) {
        return x.apply(v -> -v);
    }

    // ==================== 常量 ====================

    /**
     * 计算最大值，等同于 numpy.max()
     *
     * @param x 输入数组
     * @return 最大值
     */
    public static double max(Num x) {
        return x.max();
    }

    /**
     * 计算最小值，等同于 numpy.min()
     *
     * @param x 输入数组
     * @return 最小值
     */
    public static double min(Num x) {
        return x.min();
    }

    /**
     * 计算总和，等同于 numpy.sum()
     *
     * @param x 输入数组
     * @return 所有元素的总和
     */
    public static double sum(Num x) {
        return x.sum();
    }

    /**
     * 计算平均值，等同于 numpy.mean()
     *
     * @param x 输入数组
     * @return 平均值
     */
    public static double mean(Num x) {
        return x.mean();
    }

    /**
     * 计算标准差，等同于 numpy.std()
     *
     * @param x 输入数组
     * @return 标准差
     */
    public static double std(Num x) {
        return x.std();
    }

    /**
     * 计算方差，等同于 numpy.var()
     *
     * @param x 输入数组
     * @return 方差
     */
    public static double var(Num x) {
        return x.var();
    }

    // ==================== 辅助方法 ====================

    private static Num apply2(Num x, Num y, java.util.function.DoubleBinaryOperator op) {
        // 简化实现，假设两个数组形状相同
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
}

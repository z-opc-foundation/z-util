package com.zifang.util.pandas.num;

import java.util.Random;

/**
 * NumRandom 类 - 对标 numpy.random
 * 提供随机数生成功能
 */
public class NumRandom {

    private Random random;
    private long seed;

    /**
     * 使用默认种子构造随机数生成器
     */
    public NumRandom() {
        this.random = new Random();
    }

    /**
     * 使用指定种子构造随机数生成器
     *
     * @param seed 随机种子
     */
    public NumRandom(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    /**
     * 获取默认的随机数生成器实例
     *
     * @return Nums 类中的默认随机数生成器
     */
    public static NumRandom getDefault() {
        return Nums.random;
    }

    // ==================== 简单随机数据 ====================

    /**
     * 设置默认随机数生成器的种子
     *
     * @param seed 随机种子
     */
    public static void setSeed(long seed) {
        Nums.random.seed(seed);
    }

    /**
     * 设置随机数种子
     *
     * @param seed 随机种子
     */
    public void seed(long seed) {
        this.seed = seed;
        this.random.setSeed(seed);
    }

    /**
     * 生成 [0, 1) 之间的随机浮点数，类似于 numpy.random.rand()
     *
     * @param shape 数组形状，如传入 3 或 3,4
     * @return 指定形状的随机数数组
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public Num rand(int... shape) {
        if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextDouble();
            }
            return new Num(array);
        } else if (shape.length == 2) {
            double[][] array = new double[shape[0]][shape[1]];
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    array[i][j] = random.nextDouble();
                }
            }
            return new Num(array);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成标准正态分布的随机数，类似于 numpy.random.randn()
     *
     * @param shape 数组形状
     * @return 指定形状的标准正态分布随机数数组
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public Num randn(int... shape) {
        if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextGaussian();
            }
            return new Num(array);
        } else if (shape.length == 2) {
            double[][] array = new double[shape[0]][shape[1]];
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    array[i][j] = random.nextGaussian();
                }
            }
            return new Num(array);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    // ==================== 分布 ====================

    /**
     * 生成 [low, high) 之间的随机整数，类似于 numpy.random.randint()
     *
     * @param low   最小值（包含）
     * @param high  最大值（不包含）
     * @param shape 数组形状
     * @return 指定形状的随机整数数组
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public Num randint(int low, int high, int... shape) {
        if (shape.length == 0) {
            return new Num(new int[]{random.nextInt(high - low) + low});
        } else if (shape.length == 1) {
            int[] array = new int[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextInt(high - low) + low;
            }
            return new Num(array);
        } else if (shape.length == 2) {
            int[][] array = new int[shape[0]][shape[1]];
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    array[i][j] = random.nextInt(high - low) + low;
                }
            }
            return new Num(array);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成随机浮点数 [0.0, 1.0)，类似于 numpy.random.random()
     *
     * @return 随机浮点数
     */
    public double random() {
        return random.nextDouble();
    }

    /**
     * 生成正态分布随机数，类似于 numpy.random.normal()
     *
     * @param loc   正态分布的均值（μ）
     * @param scale 正态分布的标准差（σ）
     * @param shape 数组形状
     * @return 指定形状的正态分布随机数数组
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public Num normal(double loc, double scale, int... shape) {
        if (shape.length == 0) {
            return new Num(new double[]{random.nextGaussian() * scale + loc});
        } else if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextGaussian() * scale + loc;
            }
            return new Num(array);
        } else if (shape.length == 2) {
            double[][] array = new double[shape[0]][shape[1]];
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    array[i][j] = random.nextGaussian() * scale + loc;
                }
            }
            return new Num(array);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成标准正态分布随机数，均值为 0，标准差为 1
     *
     * @param shape 数组形状
     * @return 指定形状的标准正态分布随机数数组
     */
    public Num normal(int... shape) {
        return normal(0.0, 1.0, shape);
    }

    // ==================== 排列 ====================

    /**
     * 生成均匀分布随机数，类似于 numpy.random.uniform()
     *
     * @param low   均匀分布的下界（包含）
     * @param high  均匀分布的上界（不包含）
     * @param shape 数组形状
     * @return 指定形状的均匀分布随机数数组
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public Num uniform(double low, double high, int... shape) {
        if (shape.length == 0) {
            return new Num(new double[]{random.nextDouble() * (high - low) + low});
        } else if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextDouble() * (high - low) + low;
            }
            return new Num(array);
        } else if (shape.length == 2) {
            double[][] array = new double[shape[0]][shape[1]];
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    array[i][j] = random.nextDouble() * (high - low) + low;
                }
            }
            return new Num(array);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成 [0.0, 1.0) 区间上的均匀分布随机数
     *
     * @param shape 数组形状
     * @return 指定形状的均匀分布随机数数组
     */
    public Num uniform(int... shape) {
        return uniform(0.0, 1.0, shape);
    }

    /**
     * 随机打乱数组顺序，类似于 numpy.random.shuffle()
     *
     * @param array 待打乱的数组（原地修改）
     */
    public void shuffle(Num array) {
        if (array.data() instanceof double[]) {
            double[] arr = (double[]) array.data();
            for (int i = arr.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                double temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
    }

    // ==================== 采样 ====================

    /**
     * 返回一个随机排列，类似于 numpy.random.permutation()
     *
     * @param n 排列范围 [0, n)
     * @return 0 到 n-1 的随机排列数组
     */
    public Num permutation(int n) {
        double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        Num result = new Num(arr);
        shuffle(result);
        return result;
    }

    /**
     * 返回数组的随机排列（副本），类似于 numpy.random.permutation()
     *
     * @param array 输入数组
     * @return 输入数组的随机排列副本
     */
    public Num permutation(Num array) {
        Num result = new Num(array.data());
        shuffle(result);
        return result;
    }

    // ==================== 静态方法 ====================

    /**
     * 从数组中随机选择元素，类似于 numpy.random.choice()
     *
     * @param array   候选数组
     * @param size    选择数量
     * @param replace 是否允许重复选择
     * @return 随机选择的数组
     */
    public Num choice(double[] array, int size, boolean replace) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            int idx = random.nextInt(array.length);
            result[i] = array[idx];
            if (!replace && i < size - 1) {
                // 简单处理，实际应该更复杂
            }
        }
        return new Num(result);
    }

    /**
     * 从数组中随机选择一个元素
     *
     * @param array 候选数组
     * @return 随机选择的元素
     */
    public double choice(double[] array) {
        return array[random.nextInt(array.length)];
    }
}

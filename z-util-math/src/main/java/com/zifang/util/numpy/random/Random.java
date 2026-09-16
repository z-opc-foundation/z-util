package com.zifang.util.numpy.random;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Random 类 - 对标 numpy.random
 * 提供随机数生成功能，支持 1D/2D 形状，使用 NdArray 作为返回类型
 * 基于 pandas/num/NumRandom.java 风格
 */
public class Random {

    private java.util.Random random;
    private long seed;

    /**
     * 使用默认种子构造随机数生成器
     */
    public Random() {
        this.random = new java.util.Random();
    }

    /**
     * 使用指定种子构造随机数生成器
     *
     * @param seed 随机种子
     */
    public Random(long seed) {
        this.seed = seed;
        this.random = new java.util.Random(seed);
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

    // ==================== 简单随机数据 ====================

    /**
     * 生成 [0, 1) 之间的随机浮点数，类似于 numpy.random.rand()
     *
     * @param shape 数组形状，如传入 3 或 3,4
     * @return 指定形状的随机数 NdArray
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public NdArray rand(int... shape) {
        if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextDouble();
            }
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 2) {
            double[] array = new double[shape[0] * shape[1]];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextDouble();
            }
            return NdArray.array(array, DType.FLOAT64).reshape(shape[0], shape[1]);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成标准正态分布的随机数，类似于 numpy.random.randn()
     *
     * @param shape 数组形状
     * @return 指定形状的标准正态分布随机数 NdArray
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public NdArray randn(int... shape) {
        if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextGaussian();
            }
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 2) {
            double[] array = new double[shape[0] * shape[1]];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextGaussian();
            }
            return NdArray.array(array, DType.FLOAT64).reshape(shape[0], shape[1]);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成 [low, high) 之间的随机整数，类似于 numpy.random.randint()
     *
     * @param low   最小值（包含）
     * @param high  最大值（不包含）
     * @param shape 数组形状
     * @return 指定形状的随机整数 NdArray
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public NdArray randint(int low, int high, int... shape) {
        if (shape.length == 0) {
            double[] array = new double[]{random.nextInt(high - low) + low};
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextInt(high - low) + low;
            }
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 2) {
            double[] array = new double[shape[0] * shape[1]];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextInt(high - low) + low;
            }
            return NdArray.array(array, DType.FLOAT64).reshape(shape[0], shape[1]);
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

    // ==================== 分布 ====================

    /**
     * 生成正态分布随机数，类似于 numpy.random.normal()
     *
     * @param loc   正态分布的均值（μ）
     * @param scale 正态分布的标准差（σ）
     * @param shape 数组形状
     * @return 指定形状的正态分布随机数 NdArray
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public NdArray normal(double loc, double scale, int... shape) {
        if (shape.length == 0) {
            double[] array = new double[]{random.nextGaussian() * scale + loc};
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextGaussian() * scale + loc;
            }
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 2) {
            double[] array = new double[shape[0] * shape[1]];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextGaussian() * scale + loc;
            }
            return NdArray.array(array, DType.FLOAT64).reshape(shape[0], shape[1]);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成标准正态分布随机数，均值为 0，标准差为 1
     *
     * @param shape 数组形状
     * @return 指定形状的标准正态分布随机数 NdArray
     */
    public NdArray normal(int... shape) {
        return normal(0.0, 1.0, shape);
    }

    /**
     * 生成均匀分布随机数，类似于 numpy.random.uniform()
     *
     * @param low   均匀分布的下界（包含）
     * @param high  均匀分布的上界（不包含）
     * @param shape 数组形状
     * @return 指定形状的均匀分布随机数 NdArray
     * @throws UnsupportedOperationException 维度超过 2 时抛出
     */
    public NdArray uniform(double low, double high, int... shape) {
        if (shape.length == 0) {
            double[] array = new double[]{random.nextDouble() * (high - low) + low};
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 1) {
            double[] array = new double[shape[0]];
            for (int i = 0; i < shape[0]; i++) {
                array[i] = random.nextDouble() * (high - low) + low;
            }
            return NdArray.array(array, DType.FLOAT64);
        } else if (shape.length == 2) {
            double[] array = new double[shape[0] * shape[1]];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextDouble() * (high - low) + low;
            }
            return NdArray.array(array, DType.FLOAT64).reshape(shape[0], shape[1]);
        }
        throw new UnsupportedOperationException("Shape dimensions > 2 not yet supported");
    }

    /**
     * 生成 [0.0, 1.0) 区间上的均匀分布随机数
     *
     * @param shape 数组形状
     * @return 指定形状的均匀分布随机数 NdArray
     */
    public NdArray uniform(int... shape) {
        return uniform(0.0, 1.0, shape);
    }

    // ==================== 排列 ====================

    /**
     * 随机打乱数组顺序，类似于 numpy.random.shuffle()
     *
     * @param array 待打乱的 NdArray（原地修改）
     */
    public void shuffle(NdArray array) {
        Object data = array.getData();
        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            for (int i = arr.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                double temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        } else if (data instanceof int[]) {
            int[] arr = (int[]) data;
            for (int i = arr.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
    }

    /**
     * 返回一个随机排列，类似于 numpy.random.permutation()
     *
     * @param n 排列范围 [0, n)
     * @return 0 到 n-1 的随机排列 NdArray
     */
    public NdArray permutation(int n) {
        double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            arr[i] = i;
        }
        NdArray result = NdArray.array(arr, DType.FLOAT64);
        shuffle(result);
        return result;
    }

    /**
     * 返回数组的随机排列（副本），类似于 numpy.random.permutation()
     *
     * @param array 输入 NdArray
     * @return 输入数组的随机排列副本 NdArray
     */
    public NdArray permutation(NdArray array) {
        NdArray result = array.copy();
        shuffle(result);
        return result;
    }

    // ==================== 采样 ====================

    /**
     * 从数组中随机选择元素，类似于 numpy.random.choice()
     *
     * @param array   候选数组
     * @param size    选择数量
     * @param replace 是否允许重复选择
     * @return 随机选择的 NdArray
     */
    public NdArray choice(double[] array, int size, boolean replace) {
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            int idx = random.nextInt(array.length);
            result[i] = array[idx];
            if (!replace && i < size - 1) {
                // 简单处理：不允许重复时，确保不选已选元素
                // 这里需要更复杂的实现来严格保证无放回采样
            }
        }
        return NdArray.array(result, DType.FLOAT64);
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

    /**
     * 从 NdArray 中随机选择元素，类似于 numpy.random.choice()
     *
     * @param array   候选 NdArray
     * @param size    选择数量
     * @param replace 是否允许重复选择
     * @return 随机选择的 NdArray
     */
    public NdArray choice(NdArray array, int size, boolean replace) {
        Object data = array.getData();
        if (data instanceof double[]) {
            return choice((double[]) data, size, replace);
        }
        throw new UnsupportedOperationException("choice only supported for 1D arrays with double data");
    }

    /**
     * 从 NdArray 中随机选择一个元素
     *
     * @param array 候选 NdArray
     * @return 随机选择的元素
     */
    public double choice(NdArray array) {
        Object data = array.getData();
        if (data instanceof double[]) {
            return choice((double[]) data);
        }
        throw new UnsupportedOperationException("choice only supported for 1D arrays with double data");
    }
}

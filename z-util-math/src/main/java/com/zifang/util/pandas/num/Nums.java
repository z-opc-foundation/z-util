package com.zifang.util.pandas.num;

import java.util.List;

/**
 * Nums 类 - numpy 函数库的静态方法封装
 * 提供数组创建和操作的静态方法，类似于 Python numpy 库
 */
public class Nums {

    /**
     * 随机数生成器实例
     */
    public static final NumRandom random = new NumRandom();

    /**
     * 从数组对象创建 Num 实例
     *
     * @param array 数组对象，支持 double[]、int[] 等
     * @return Num 实例
     */
    public static Num array(Object array) {
        return new Num(array);
    }

    /**
     * 从 List 创建 Num 实例
     *
     * @param list 列表对象
     * @return Num 实例
     */
    public static Num array(List<?> list) {
        return new Num(list.toArray());
    }

    /**
     * 使用维度创建空数组
     *
     * @param shape 数组形状
     * @param dType 数据类型
     * @return 空 Num 实例
     */
    public static Num array(int[] shape, DType dType) {
        return null;
    }

    /**
     * 使用维度、对象数组和数据类型创建数组
     *
     * @param shape 数组形状
     * @param objs  对象数组
     * @param dType 数据类型
     * @return Num 实例
     */
    public static Num array(int[] shape, Object[] objs, DType dType) {
        return null;
    }

    /**
     * 使用对象序列填充 Num
     *
     * @param num  目标 Num 实例
     * @param objs 用于填充的对象数组
     * @return 填充后的 Num 实例
     */
    public static Num fill(Num num, Object[] objs) {
        return null;
    }

    /**
     * 在给定间隔内返回均匀间隔的数值
     * <p>
     * 使用方式：
     * <ul>
     *   <li>aRange(10) 返回 0~9</li>
     *   <li>aRange(10.0) 返回 0.0 ~ 9.0</li>
     *   <li>aRange(5, 15) 返回 5 ~ 14</li>
     *   <li>aRange(5.0, 12.0, 2) 返回 5.0~12.0，步长为2</li>
     * </ul>
     *
     * @param i 可以是单个数字(结束值)或多个参数(开始, 结束, 步长)
     * @return 均匀间隔的数值数组
     */
    public static Num aRange(Object... i) {
        return null;
    }


    // inspace()可以用来返回在间隔[开始，停止]上计算的num个均匀间隔的样本：
    // print(np.linspace(10,15,num=20))

    /**
     * 选定区域，返回均匀间隔的样本数组
     * <p>
     * 类似于 numpy.linspace()
     *
     * @param i        开始数值
     * @param j        停止数值
     * @param num      均匀间隔的样本数量
     * @param endPoint 是否包含最后的值
     * @return 均匀间隔的样本数组
     */
    public static Num linSpace(Number i, Number j, Integer num, Boolean endPoint) {
        return null;
    }

    //
    // print(np.zeros((3,5),dtype=np.int)) # dtype可以将元素变成整数

    /**
     * 创建数组且用 0 填充
     * <p>
     * 类似于 numpy.zeros()
     *
     * @param shapes 数组形状
     * @param dType  数据类型
     * @return 全零 Num 实例
     */
    public static Num zeros(Integer[] shapes, DType dType) {
        return null;
    }

    // ar2=np.ones(9) # 用1填充

    /**
     * 创建用 1 填充的数组
     * <p>
     * 类似于 numpy.ones()
     *
     * @return 全一 Num 实例
     */
    public static Num ones() {
        return null;
    }

    // print(np.eye(5)) # 中间数是1，其他都是0

    /**
     * 创建单位矩阵
     * <p>
     * 类似于 numpy.eye()
     *
     * @return 单位矩阵 Num 实例
     */
    public static Num eye() {
        return null;
    }

    /**
     * 横向连接多个数组
     * <p>
     * 类似于 numpy.hstack()
     */
    public void hStack() {

    }

    /**
     * 纵向连接多个数组
     * <p>
     * 类似于 numpy.vstack()
     */
    public void vStack() {

    }

    // print(np.hsplit(ar,2)[0])
    // print(np.vsplit(ar,4))

    /**
     * 水平分割数组
     * <p>
     * 类似于 numpy.hsplit()
     */
    public void hsplit() {

    }

    /**
     * 垂直分割数组
     * <p>
     * 类似于 numpy.vsplit()
     */
    public void vsplit() {

    }
}
